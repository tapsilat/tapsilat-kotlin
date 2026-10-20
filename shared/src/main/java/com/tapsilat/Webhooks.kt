package com.tapsilat

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Verifies the signature of the callbacks Tapsilat sends to an organization's callback URLs and to
 * an order's payment_success_url and payment_failure_url.
 */
object TapsilatWebhook {
    /** The header that carries the signature of every callback Tapsilat sends. */
    const val SIGNATURE_HEADER = "Tapsilat-Signature"

    /** How many seconds a callback's signature timestamp may differ from the current time. */
    const val DEFAULT_TOLERANCE_SECONDS = 300L

    private const val LEGACY_PREFIX = "sha256="
    private val HEX_DIGITS = "0123456789abcdef".toCharArray()

    /**
     * Verifies the Tapsilat-Signature header of a callback against the organization's webhook secret.
     *
     * Tapsilat sends `t=<unix timestamp>,v1=<hex HMAC-SHA256 of "<timestamp>.<raw body>">`. The
     * timestamp must be within [toleranceSeconds] of [now] (0 disables the check) so that a
     * captured callback cannot be replayed. [payload] must be the raw request body, before any JSON
     * decoding. The comparison takes constant time.
     *
     * A bare hex HMAC-SHA256 of the payload, with or without a `sha256=` prefix, is also accepted,
     * as the other Tapsilat SDKs accept it for signatures made the way their earlier versions documented.
     *
     * @param payload the raw request body
     * @param signature the Tapsilat-Signature header
     * @param secret the webhook secret from the panel's callback settings
     * @param toleranceSeconds 0 disables the timestamp check
     * @param now the Unix time in seconds to compare the timestamp with
     */
    @JvmStatic
    @JvmOverloads
    fun verify(
        payload: String,
        signature: String?,
        secret: String?,
        toleranceSeconds: Long = DEFAULT_TOLERANCE_SECONDS,
        now: Long = Instant.now().epochSecond,
    ): Boolean = verify(payload.toByteArray(StandardCharsets.UTF_8), signature, secret, toleranceSeconds, now)

    /** Verifies the Tapsilat-Signature header of a callback whose raw body is [payload]; see the String overload. */
    @JvmStatic
    @JvmOverloads
    fun verify(
        payload: ByteArray,
        signature: String?,
        secret: String?,
        toleranceSeconds: Long = DEFAULT_TOLERANCE_SECONDS,
        now: Long = Instant.now().epochSecond,
    ): Boolean {
        val header = signature?.trim().orEmpty()
        if (header.isEmpty() || secret.isNullOrEmpty()) return false

        if (header.startsWith(LEGACY_PREFIX) || '=' !in header) {
            val expected = hmacHex(secret, payload)
            return constantTimeEquals(expected, header.removePrefix(LEGACY_PREFIX).lowercase())
        }

        var timestamp: String? = null
        val candidates = mutableListOf<String>()
        for (part in header.split(',')) {
            val pair = part.trim().split('=', limit = 2)
            if (pair.size != 2) continue
            when (pair[0]) {
                "t" -> timestamp = pair[1]
                "v1" -> candidates += pair[1].lowercase()
            }
        }
        if (timestamp.isNullOrEmpty() || !timestamp.all { it in '0'..'9' } || candidates.isEmpty()) return false
        val signedAt = timestamp.toLongOrNull() ?: return false
        if (toleranceSeconds > 0) {
            val age = runCatching { Math.subtractExact(now, signedAt) }.getOrNull() ?: return false
            if (age > toleranceSeconds || age < -toleranceSeconds) return false
        }

        val expected = hmacHex(secret, "$timestamp.".toByteArray(StandardCharsets.UTF_8), payload)
        // Check every candidate so the time taken does not depend on which one matched
        return candidates.fold(false) { matched, candidate -> constantTimeEquals(expected, candidate) || matched }
    }

    private fun hmacHex(secret: String, vararg parts: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        parts.forEach { mac.update(it) }
        val digest = mac.doFinal()
        val hex = CharArray(digest.size * 2)
        digest.forEachIndexed { i, byte ->
            hex[i * 2] = HEX_DIGITS[(byte.toInt() shr 4) and 0x0f]
            hex[i * 2 + 1] = HEX_DIGITS[byte.toInt() and 0x0f]
        }
        return String(hex)
    }

    private fun constantTimeEquals(expected: String, candidate: String): Boolean =
        MessageDigest.isEqual(
            expected.toByteArray(StandardCharsets.UTF_8),
            candidate.toByteArray(StandardCharsets.UTF_8),
        )
}

/**
 * A callback Tapsilat posts to an organization's callback URLs or to an order's payment_success_url
 * and payment_failure_url: `{"type": "...", "data": {...}, "token": "<delivery id>"}`.
 *
 * The shape of [data] depends on the [type], which each organization chooses in the panel:
 * "order_lite" (the default), "order", "order_payment_detail", "extended_order" or
 * "payment_facilitator". The properties read the order's reference ID, conversation ID and status
 * from whichever shape arrived.
 *
 * Verify the Tapsilat-Signature header with [TapsilatWebhook.verify] before trusting a callback.
 */
class WebhookEvent(
    /** The decoded payload. */
    val payload: Map<String, Any?>,
) {
    val type: String? = payload["type"] as? String

    /** Unique per delivery. */
    val token: String? = payload["token"] as? String

    /** The callback's "data" object, or the whole payload when it has none. */
    val data: Map<String, Any?> = asObject(payload["data"]) ?: payload

    /** The order's reference ID, whatever the callback type. */
    val referenceId: String?
        get() = firstString(
            data["orderReferenceId"],
            data["order_reference_id"],
            data["reference_id"],
            asObject(data["order"])?.get("reference_id"),
        )

    /** The conversation ID the order was created with. */
    val conversationId: String?
        get() = firstString(data["conversationId"], data["conversation_id"])

    /** The order status code (see [OrderStatus]), or null when this callback type carries none. */
    val statusCode: Int?
        get() = OrderStatus.code(data["status"]) ?: OrderStatus.code(asObject(data["order"])?.get("status"))

    /** The order status name, such as "Paid". */
    val status: String?
        get() = OrderStatus.name(statusCode)

    val isPaid: Boolean
        get() = OrderStatus.isPaid(statusCode)

    val isFailed: Boolean
        get() = OrderStatus.isFailed(statusCode)

    val isRefunded: Boolean
        get() = OrderStatus.isRefunded(statusCode)

    companion object {
        const val TYPE_ORDER = "order"
        const val TYPE_ORDER_LITE = "order_lite"
        const val TYPE_ORDER_PAYMENT_DETAIL = "order_payment_detail"
        const val TYPE_EXTENDED_ORDER = "extended_order"
        const val TYPE_PAYMENT_FACILITATOR = "payment_facilitator"

        private const val ZERO_UUID = "00000000-0000-0000-0000-000000000000"
        private val mapper: ObjectMapper = JsonMapper.builder().build()

        /**
         * Decodes a raw callback body.
         *
         * @throws IllegalArgumentException when the body is not a JSON object
         */
        @JvmStatic
        fun parse(payload: String): WebhookEvent {
            val decoded = try {
                mapper.readValue(payload, Any::class.java)
            } catch (e: JsonProcessingException) {
                throw IllegalArgumentException("the webhook payload is not valid JSON", e)
            }
            return WebhookEvent(asObject(decoded) ?: throw IllegalArgumentException("the webhook payload is not a JSON object"))
        }

        /** Decodes a raw UTF-8 callback body; see the String overload. */
        @JvmStatic
        fun parse(payload: ByteArray): WebhookEvent = parse(String(payload, StandardCharsets.UTF_8))

        @Suppress("UNCHECKED_CAST")
        private fun asObject(value: Any?): Map<String, Any?>? = value as? Map<String, Any?>

        private fun firstString(vararg values: Any?): String? {
            for (value in values) {
                when (value) {
                    is String -> if (value.isNotEmpty() && value != ZERO_UUID) return value
                    is Int, is Long, is BigInteger -> return value.toString()
                }
            }
            return null
        }
    }
}

package com.tapsilat

import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebhookTest {

    private companion object {
        // An order_lite callback signed the way admin's monosign.SignWebhook signs it; the expected
        // signatures were computed with openssl, independently of the SDK.
        const val PAYLOAD = """{"data":{"id":"5f0c6f1e-1111-4a5b-9c1d-2e3f4a5b6c7d",""" +
            """"orderReferenceId":"0b6a4f9e-2222-4c3d-8e9f-a1b2c3d4e5f6","conversationId":"wc-1001","status":"3"},""" +
            """"type":"order_lite","token":"9d8c7b6a-3333-4e5f-a6b7-c8d9e0f1a2b3"}"""
        const val SECRET = "whsec_test"
        const val TIMESTAMP = 1726480000L
        const val SIGNATURE = "3e003945e8122172a03ae1aa56fd099b56e6ac11a8472c03e33809c7c480aeb6"
        const val LEGACY_SIGNATURE = "402b9d6616b32be533a4fc0300e8db19c304a56137d3fb274c614f30f9be6fbb"

        fun header(signature: String = SIGNATURE, timestamp: Any = TIMESTAMP) = "t=$timestamp,v1=$signature"
    }

    // ── TapsilatWebhook.verify ────────────────────────────────────────────────

    @Test
    fun `verifies the Tapsilat-Signature header`() {
        assertEquals("Tapsilat-Signature", TapsilatWebhook.SIGNATURE_HEADER)
        assertTrue(TapsilatWebhook.verify(PAYLOAD, header(), SECRET, 300, TIMESTAMP + 10))
        assertTrue(
            TapsilatWebhook.verify(PAYLOAD, " t=$TIMESTAMP, v1=${SIGNATURE.uppercase()} ", SECRET, 300, TIMESTAMP),
        )
        assertTrue(TapsilatWebhook.verify(PAYLOAD.toByteArray(), header(), SECRET, 300, TIMESTAMP))
    }

    @Test
    fun `rejects a tampered body or the wrong secret`() {
        val tampered = PAYLOAD.replace(""""status":"3"""", """"status":"13"""")

        assertFalse(TapsilatWebhook.verify(tampered, header(), SECRET, 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), "other", 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), "", 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), null, 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, "", SECRET, 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, null, SECRET, 300, TIMESTAMP))
    }

    @Test
    fun `rejects a replayed or malformed header`() {
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), SECRET, 300, TIMESTAMP + 301))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), SECRET, 300, TIMESTAMP - 301))
        assertTrue(TapsilatWebhook.verify(PAYLOAD, header(), SECRET, 0, TIMESTAMP + 86400))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, "v1=$SIGNATURE", SECRET, 0, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, "t=$TIMESTAMP", SECRET, 0, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(timestamp = "1726480000.5"), SECRET, 0, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(timestamp = "9".repeat(40)), SECRET, 300, TIMESTAMP))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(timestamp = "9".repeat(40)), SECRET, 0, TIMESTAMP))
    }

    @Test
    fun `accepts any of several v1 signatures`() {
        val header = "t=$TIMESTAMP,v1=${"0".repeat(64)},v1=$SIGNATURE"

        assertTrue(TapsilatWebhook.verify(PAYLOAD, header, SECRET, 300, TIMESTAMP))
    }

    @Test
    fun `uses the current time by default`() {
        val now = Instant.now().epochSecond
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256")) }
        val signature = mac.doFinal("$now.$PAYLOAD".toByteArray()).joinToString("") { "%02x".format(it) }

        assertTrue(TapsilatWebhook.verify(PAYLOAD, header(signature, now), SECRET))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, header(), SECRET))
    }

    @Test
    fun `accepts the legacy bare and sha256= forms`() {
        assertTrue(TapsilatWebhook.verify(PAYLOAD, LEGACY_SIGNATURE, SECRET))
        assertTrue(TapsilatWebhook.verify(PAYLOAD, "sha256=$LEGACY_SIGNATURE", SECRET))
        assertTrue(TapsilatWebhook.verify(PAYLOAD, LEGACY_SIGNATURE.uppercase(), SECRET))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, "sha256=$SIGNATURE", SECRET))
        assertFalse(TapsilatWebhook.verify(PAYLOAD, LEGACY_SIGNATURE, "other"))
    }

    // ── WebhookEvent ──────────────────────────────────────────────────────────

    @Test
    fun `parses an order_lite callback`() {
        val event = WebhookEvent.parse(PAYLOAD)

        assertEquals(WebhookEvent.TYPE_ORDER_LITE, event.type)
        assertEquals("9d8c7b6a-3333-4e5f-a6b7-c8d9e0f1a2b3", event.token)
        assertEquals("0b6a4f9e-2222-4c3d-8e9f-a1b2c3d4e5f6", event.referenceId)
        assertEquals("wc-1001", event.conversationId)
        assertEquals(TapsilatConstants.ORDER_STATUS_PAID, event.statusCode)
        assertEquals("Paid", event.status)
        assertTrue(event.isPaid)
        assertFalse(event.isFailed)
        assertEquals(event.referenceId, WebhookEvent.parse(PAYLOAD.toByteArray()).referenceId)
    }

    @Test
    fun `parses an order callback`() {
        val event = WebhookEvent.parse(
            """{"type":"order","token":"t-1",""" +
                """"data":{"id":"o-1","reference_id":"ref-1","conversation_id":"c-1","status":13}}""",
        )

        assertEquals(WebhookEvent.TYPE_ORDER, event.type)
        assertEquals("ref-1", event.referenceId)
        assertEquals("c-1", event.conversationId)
        assertEquals("Failure", event.status)
        assertTrue(event.isFailed)
        assertFalse(event.isPaid)
    }

    @Test
    fun `parses an order_payment_detail callback`() {
        val event = WebhookEvent(
            mapOf(
                "type" to "order_payment_detail",
                "token" to "t-2",
                "data" to mapOf(
                    "id" to "d-1",
                    "order_reference_id" to "ref-2",
                    "conversation_id" to "c-2",
                    "order" to mapOf("reference_id" to "ref-2", "id" to "o-2", "status" to "Paid"),
                    "order_payment_status" to mapOf("code" to 0),
                ),
            ),
        )

        assertEquals("ref-2", event.referenceId)
        assertEquals("c-2", event.conversationId)
        assertEquals(TapsilatConstants.ORDER_STATUS_PAID, event.statusCode)
    }

    @Test
    fun `parses callbacks without a status`() {
        val extended = WebhookEvent(
            mapOf(
                "type" to "extended_order",
                "data" to mapOf(
                    "id" to "o-3",
                    "reference_id" to "ref-3",
                    "terms" to listOf(mapOf("reference_id" to "term-1", "status" to 2)),
                ),
            ),
        )
        assertEquals(WebhookEvent.TYPE_EXTENDED_ORDER, extended.type)
        assertEquals("ref-3", extended.referenceId)
        assertNull(extended.conversationId)
        assertNull(extended.statusCode)
        assertNull(extended.status)
        assertFalse(extended.isPaid)

        val facilitator = WebhookEvent(
            mapOf(
                "type" to "payment_facilitator",
                "data" to mapOf("id" to "p-1", "payment_id" to "pay-1", "reference_id" to "ref-4"),
            ),
        )
        assertEquals(WebhookEvent.TYPE_PAYMENT_FACILITATOR, facilitator.type)
        assertEquals("ref-4", facilitator.referenceId)
    }

    @Test
    fun `ignores a zero UUID and reads integer IDs`() {
        val event = WebhookEvent(
            mapOf(
                "type" to "order_payment_detail",
                "data" to mapOf(
                    "order_reference_id" to "00000000-0000-0000-0000-000000000000",
                    "order" to mapOf("reference_id" to "ref-5"),
                ),
            ),
        )
        assertEquals("ref-5", event.referenceId)

        assertEquals("42", WebhookEvent.parse("""{"data":{"reference_id":42}}""").referenceId)
    }

    @Test
    fun `uses the whole payload when it has no data object`() {
        val event = WebhookEvent.parse("""{"reference_id":"ref-6","status":"Expired"}""")

        assertNull(event.type)
        assertNull(event.token)
        assertEquals("ref-6", event.referenceId)
        assertTrue(event.isFailed)
    }

    @Test
    fun `rejects a payload that is not a JSON object`() {
        assertFailsWith<IllegalArgumentException> { WebhookEvent.parse("\"order\"") }
        assertFailsWith<IllegalArgumentException> { WebhookEvent.parse("[1]") }
        assertFailsWith<IllegalArgumentException> { WebhookEvent.parse("null") }
        assertFailsWith<IllegalArgumentException> { WebhookEvent.parse("not json") }
        assertFailsWith<IllegalArgumentException> { WebhookEvent.parse("") }
    }

    // ── OrderStatus ───────────────────────────────────────────────────────────

    @Test
    fun `order status names and groups`() {
        assertEquals(TapsilatConstants.ORDER_STATUS_PAYMENT, OrderStatus.code("waiting for payment"))
        assertEquals(TapsilatConstants.ORDER_STATUS_EXPIRED, OrderStatus.code("21"))
        assertEquals(TapsilatConstants.ORDER_STATUS_FAILURE, OrderStatus.code(13))
        assertEquals(TapsilatConstants.ORDER_STATUS_FAILURE, OrderStatus.code(13.0))
        assertEquals(TapsilatConstants.ORDER_STATUS_PARTIALLY_PAID, OrderStatus.code("Partially Paid"))
        assertEquals("Partially refunded", OrderStatus.name(15))
        assertNull(OrderStatus.code("SUCCESS"))
        assertNull(OrderStatus.code(99))
        assertNull(OrderStatus.code(13.5))
        assertNull(OrderStatus.code("9".repeat(40)))
        assertNull(OrderStatus.code(""))
        assertNull(OrderStatus.name(null))

        assertTrue(OrderStatus.isPaid("Sub merchant payment approved"))
        assertTrue(OrderStatus.isPaid(TapsilatConstants.ORDER_STATUS_COMPLETED))
        assertFalse(OrderStatus.isPaid("Waiting for payment"))
        assertTrue(OrderStatus.isFailed("Expired"))
        assertFalse(OrderStatus.isFailed("Paid"))
        assertTrue(OrderStatus.isRefunded("10"))
        assertFalse(OrderStatus.isRefunded(null))
    }
}

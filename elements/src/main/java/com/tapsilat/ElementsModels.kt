package com.tapsilat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

// ── Tapsilat Elements sessions ────────────────────────────────────────────

/** Opens a Tapsilat Elements session. Optional fields left null are not sent. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ElementsSessionCreateRequest(
    /** [ElementsSession.PURPOSE_TOKENIZE] or [ElementsSession.PURPOSE_PAY]. */
    val purpose: String,
    /** The order to pay; required for [ElementsSession.PURPOSE_PAY]. */
    val orderReferenceId: String? = null,
    /** The card owner for [ElementsSession.PURPOSE_TOKENIZE], scoped to your organization. */
    val userId: String? = null,
    val userEmail: String? = null,
    /** Tokenize mode: [ElementsSession.MODE_SECURE], [ElementsSession.MODE_HALF_SECURE] or [ElementsSession.MODE_NON_SECURE]. */
    val mode: Int? = null,
    /** Lets the buyer save the card on a [ElementsSession.PURPOSE_PAY] session. */
    val saveCard: Boolean? = null,
)

/** The card a session used. [brand] is one of the BRAND_* values, or a brand this SDK does not know yet. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ElementsCardSummary(
    val id: String? = null,
    val brand: String? = null,
    val last4: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
) {
    companion object {
        const val BRAND_VISA = "visa"
        const val BRAND_MASTERCARD = "mastercard"
        const val BRAND_AMEX = "amex"
        const val BRAND_TROY = "troy"
        const val BRAND_DISCOVER = "discover"
        const val BRAND_UNIONPAY = "unionpay"
        const val BRAND_UNKNOWN = "unknown"
    }
}

/** Why a session's last attempt failed, for example `card_declined`. A value the session carries, not an exception. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ElementsErrorDetail(
    val code: String? = null,
    val message: String? = null,
    val retryable: Boolean? = null,
)

/**
 * The authoritative state of a Tapsilat Elements session. [purpose] and [status] are one of the
 * PURPOSE_* and STATUS_* values, or a value this SDK does not know yet. Timestamps are RFC 3339 strings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ElementsSession(
    val id: String? = null,
    val purpose: String? = null,
    val status: String? = null,
    val orderReferenceId: String? = null,
    val userId: String? = null,
    val attempts: Int? = null,
    val maxAttempts: Int? = null,
    val createdAt: String? = null,
    val expiresAt: String? = null,
    val completedAt: String? = null,
    val card: ElementsCardSummary? = null,
    val lastError: ElementsErrorDetail? = null,
) {
    companion object {
        const val PURPOSE_TOKENIZE = "tokenize"
        const val PURPOSE_PAY = "pay"

        /** 3D Secure. */
        const val MODE_SECURE = 1
        const val MODE_HALF_SECURE = 2
        const val MODE_NON_SECURE = 3

        const val STATUS_REQUIRES_PAYMENT_METHOD = "requires_payment_method"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_REQUIRES_ACTION = "requires_action"
        const val STATUS_SUCCEEDED = "succeeded"
        const val STATUS_FAILED = "failed"
        const val STATUS_EXPIRED = "expired"
        const val STATUS_CANCELED = "canceled"
    }
}

/**
 * A newly created Elements session: the [ElementsSession] fields plus [clientSecret] and [elementsBaseUrl].
 *
 * Hand [clientSecret] to the app that opens the card sheet; only the create call returns it. Do not log or
 * store it. [toString] prints it as `[redacted]`; JSON serialization keeps it, so you can send it to the app.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ElementsSessionWithSecret(
    val id: String? = null,
    val purpose: String? = null,
    val status: String? = null,
    val orderReferenceId: String? = null,
    val userId: String? = null,
    val attempts: Int? = null,
    val maxAttempts: Int? = null,
    val createdAt: String? = null,
    val expiresAt: String? = null,
    val completedAt: String? = null,
    val card: ElementsCardSummary? = null,
    val lastError: ElementsErrorDetail? = null,
    val clientSecret: String? = null,
    val elementsBaseUrl: String? = null,
) {
    override fun toString(): String =
        "ElementsSessionWithSecret(id=$id, purpose=$purpose, status=$status, orderReferenceId=$orderReferenceId, " +
            "userId=$userId, attempts=$attempts, maxAttempts=$maxAttempts, createdAt=$createdAt, " +
            "expiresAt=$expiresAt, completedAt=$completedAt, card=$card, lastError=$lastError, " +
            "clientSecret=${clientSecret?.let { "[redacted]" }}, elementsBaseUrl=$elementsBaseUrl)"
}

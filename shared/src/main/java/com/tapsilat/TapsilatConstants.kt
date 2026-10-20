package com.tapsilat

object TapsilatConstants {
    const val DEFAULT_ENDPOINT = "https://panel.tapsilat.dev/api/v1"

    const val ORDER_STATUS_RECEIVED = 1
    const val ORDER_STATUS_UNPAID = 2
    const val ORDER_STATUS_PAID = 3
    const val ORDER_STATUS_PROCESSING = 4
    const val ORDER_STATUS_SHIPPED = 5
    const val ORDER_STATUS_ON_HOLD = 6
    const val ORDER_STATUS_PAYMENT = 7
    const val ORDER_STATUS_CANCELLED = 8
    const val ORDER_STATUS_COMPLETED = 9
    const val ORDER_STATUS_REFUNDED = 10
    const val ORDER_STATUS_FRAUD = 11
    const val ORDER_STATUS_REJECTED = 12
    const val ORDER_STATUS_FAILURE = 13
    const val ORDER_STATUS_RETRYING = 14
    const val ORDER_STATUS_PARTIALLY_REFUNDED = 15
    const val ORDER_STATUS_SUBMERCHANT_PAYMENT_APPROVED = 16
    const val ORDER_STATUS_SUBMERCHANT_PAYMENT_DISAPPROVED = 17
    const val ORDER_STATUS_SUBMERCHANT_PAYMENT_ERRORED = 18
    const val ORDER_STATUS_STILL_HAS_UNPAID_INSTALLMENTS = 19
    const val ORDER_STATUS_STILL_HAS_UNPAID_TERMS = 20
    const val ORDER_STATUS_EXPIRED = 21
    const val ORDER_STATUS_STILL_HAS_UNPAID_SUBMERCHANT_PAYMENTS = 22
    const val ORDER_STATUS_PARTIALLY_PAID = 23
    const val ORDER_STATUS_TERMINATED = 24
    const val ORDER_STATUS_CARD_TOKENIZATION = 25
    const val ORDER_STATUS_PRE_AUTHORIZED = 26
    const val ORDER_STATUS_DISPUTED = 27
    const val ORDER_STATUS_PARTIALLY_DISPUTED = 28
    const val ORDER_STATUS_SUSPECT = 29

    const val ORDER_TYPE_PHYSICAL = 1
    const val ORDER_TYPE_VIRTUAL = 2
    const val ORDER_TYPE_MARKETPLACE = 3
    const val ORDER_TYPE_SUBSCRIPTION = 4
    const val ORDER_TYPE_DEPOSIT = 5
    const val ORDER_TYPE_MAIL_ORDER = 6
    const val ORDER_TYPE_TELEPHONE_ORDER = 7
    const val ORDER_TYPE_INVOICE = 8
    const val ORDER_TYPE_LOAN = 9
    const val ORDER_TYPE_REMITTANCE = 10
    const val ORDER_TYPE_CAR_LOAN = 11

    const val SUBSCRIPTION_STATUS_SUCCESS = "success"
    const val SUBSCRIPTION_STATUS_FAILURE = "failure"
    const val SUBSCRIPTION_STATUS_PENDING = "pending"

    /** Order status names, as GET /order/{reference_id}/status returns them, by status code. */
    val ORDER_STATUS_NAMES: Map<Int, String> = mapOf(
        ORDER_STATUS_RECEIVED to "Received",
        ORDER_STATUS_UNPAID to "Unpaid",
        ORDER_STATUS_PAID to "Paid",
        ORDER_STATUS_PROCESSING to "Processing",
        ORDER_STATUS_SHIPPED to "Shipped",
        ORDER_STATUS_ON_HOLD to "On hold",
        ORDER_STATUS_PAYMENT to "Waiting for payment",
        ORDER_STATUS_CANCELLED to "Cancelled",
        ORDER_STATUS_COMPLETED to "Completed",
        ORDER_STATUS_REFUNDED to "Refunded",
        ORDER_STATUS_FRAUD to "Fraud",
        ORDER_STATUS_REJECTED to "Rejected",
        ORDER_STATUS_FAILURE to "Failure",
        ORDER_STATUS_RETRYING to "Retrying",
        ORDER_STATUS_PARTIALLY_REFUNDED to "Partially refunded",
        ORDER_STATUS_SUBMERCHANT_PAYMENT_APPROVED to "Sub merchant payment approved",
        ORDER_STATUS_SUBMERCHANT_PAYMENT_DISAPPROVED to "Sub merchant payment disapproved",
        ORDER_STATUS_SUBMERCHANT_PAYMENT_ERRORED to "Sub merchant payment errored",
        ORDER_STATUS_STILL_HAS_UNPAID_INSTALLMENTS to "Still has unpaid installments",
        ORDER_STATUS_STILL_HAS_UNPAID_TERMS to "Still has unpaid terms",
        ORDER_STATUS_EXPIRED to "Expired",
        ORDER_STATUS_STILL_HAS_UNPAID_SUBMERCHANT_PAYMENTS to "Still has unpaid sub merchant payments",
        ORDER_STATUS_PARTIALLY_PAID to "Partially Paid",
        ORDER_STATUS_TERMINATED to "Terminated",
        ORDER_STATUS_CARD_TOKENIZATION to "Card Tokenization",
        ORDER_STATUS_PRE_AUTHORIZED to "Pre Authorized",
        ORDER_STATUS_DISPUTED to "Disputed",
        ORDER_STATUS_PARTIALLY_DISPUTED to "Partially disputed",
        ORDER_STATUS_SUSPECT to "Suspect",
    )

    /** Returns the code for a status name such as "Paid" (case-insensitive), or 0 when the name is unknown. */
    fun getOrderStatusByString(status: String): Int =
        ORDER_STATUS_NAMES.entries.firstOrNull { it.value.equals(status, ignoreCase = true) }?.key ?: 0
}

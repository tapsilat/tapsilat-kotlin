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

    private val STATUS_BY_NAME: Map<String, Int> = mapOf(
        "Received" to ORDER_STATUS_RECEIVED,
        "Unpaid" to ORDER_STATUS_UNPAID,
        "Paid" to ORDER_STATUS_PAID,
        "Processing" to ORDER_STATUS_PROCESSING,
        "Shipped" to ORDER_STATUS_SHIPPED,
        "On hold" to ORDER_STATUS_ON_HOLD,
        "Waiting for payment" to ORDER_STATUS_PAYMENT,
        "Cancelled" to ORDER_STATUS_CANCELLED,
        "Completed" to ORDER_STATUS_COMPLETED,
        "Refunded" to ORDER_STATUS_REFUNDED,
        "Fraud" to ORDER_STATUS_FRAUD,
        "Rejected" to ORDER_STATUS_REJECTED,
        "Failure" to ORDER_STATUS_FAILURE,
        "Retrying" to ORDER_STATUS_RETRYING,
        "Partially refunded" to ORDER_STATUS_PARTIALLY_REFUNDED,
        "Sub merchant payment approved" to ORDER_STATUS_SUBMERCHANT_PAYMENT_APPROVED,
        "Sub merchant payment disapproved" to ORDER_STATUS_SUBMERCHANT_PAYMENT_DISAPPROVED,
        "Sub merchant payment errored" to ORDER_STATUS_SUBMERCHANT_PAYMENT_ERRORED,
        "Still has unpaid installments" to ORDER_STATUS_STILL_HAS_UNPAID_INSTALLMENTS,
        "Still has unpaid terms" to ORDER_STATUS_STILL_HAS_UNPAID_TERMS,
        "Expired" to ORDER_STATUS_EXPIRED,
        "Still has unpaid sub merchant payments" to ORDER_STATUS_STILL_HAS_UNPAID_SUBMERCHANT_PAYMENTS,
        "Partially paid" to ORDER_STATUS_PARTIALLY_PAID,
        "Terminated" to ORDER_STATUS_TERMINATED,
        "Card tokenization" to ORDER_STATUS_CARD_TOKENIZATION,
        "Pre authorized" to ORDER_STATUS_PRE_AUTHORIZED,
        "Disputed" to ORDER_STATUS_DISPUTED,
        "Partially disputed" to ORDER_STATUS_PARTIALLY_DISPUTED,
        "Suspect" to ORDER_STATUS_SUSPECT,
    )

    fun getOrderStatusByString(status: String): Int = STATUS_BY_NAME[status] ?: 0
}

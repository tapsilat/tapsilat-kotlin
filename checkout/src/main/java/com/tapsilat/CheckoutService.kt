package com.tapsilat

/** Card installment options and order charging. */
class CheckoutService internal constructor(
    private val client: SharedApiClient,
) {
    /** Returns installment options and commission rates for a card BIN and amount. */
    fun getCardInstallments(request: CardInstallmentsRequest): CardInstallmentsResponse =
        client.get("/card/installments", CardInstallmentsResponse::class.java, request.toQuery())

    /** Returns installment options grouped by bank card program, with an interest-free flag per row. */
    fun getInstallmentTable(request: InstallmentTableRequest): InstallmentTableResponse =
        client.get("/card/installment-table", InstallmentTableResponse::class.java, request.toQuery())

    /** Charges an order; inspect [OrderChargeResponse.isSuccess], since the request can succeed while the charge fails. */
    fun chargeOrder(request: OrderChargeRequest): OrderChargeResponse =
        client.post("/order/charge", request, OrderChargeResponse::class.java)

    @Deprecated("Checkout sessions are created with OrderService.createOrder")
    fun createCheckoutSessionPlaceholder(): Nothing {
        throw UnsupportedOperationException("Checkout sessions are created with OrderService.createOrder")
    }
}

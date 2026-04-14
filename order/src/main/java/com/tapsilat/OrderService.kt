package com.tapsilat

class OrderService internal constructor(
    private val client: SharedApiClient,
) {
    fun createOrder(payload: Order): OrderResponse {
        val buyer = payload.buyer.copy(gsmNumber = Validators.validateGsmNumber(payload.buyer.gsmNumber))
        val response = client.post("/order/create", payload.copy(buyer = buyer), OrderResponse::class.java)
        return if (!response.referenceId.isNullOrBlank()) {
            val checkoutUrl = runCatching { getCheckoutUrl(response.referenceId) }.getOrNull()
            response.copy(checkoutUrl = checkoutUrl ?: response.checkoutUrl)
        } else {
            response
        }
    }

    fun getOrder(orderReferenceId: String): OrderDetail = client.get("/order/$orderReferenceId", OrderDetail::class.java)

    fun getOrderByConversationId(conversationId: String): OrderDetail =
        client.get("/order/conversation/$conversationId", OrderDetail::class.java)

    @Deprecated("Use getOrderByConversationId", ReplaceWith("getOrderByConversationId(conversationId)"))
    fun getOrderByConversationID(conversationId: String): OrderDetail = getOrderByConversationId(conversationId)

    @Deprecated(
        "page and perPage should be Int; use getOrderList instead",
        ReplaceWith("getOrderList(page.toInt(), perPage.toInt(), buyerId = buyerId)"),
    )
    fun getOrders(page: String, perPage: String, buyerId: String? = null): PaginatedData {
        val query = linkedMapOf("page" to page, "per_page" to perPage)
        if (!buyerId.isNullOrBlank()) query["buyer_id"] = buyerId
        return client.get("/order/list", PaginatedData::class.java, query)
    }

    fun getOrderList(
        page: Int,
        perPage: Int,
        buyerId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        organizationId: String? = null,
        relatedReferenceId: String? = null,
    ): PaginatedData {
        val query = linkedMapOf("page" to page.toString(), "per_page" to perPage.toString())
        if (!buyerId.isNullOrBlank()) query["buyer_id"] = buyerId
        if (!startDate.isNullOrBlank()) query["start_date"] = startDate
        if (!endDate.isNullOrBlank()) query["end_date"] = endDate
        if (!organizationId.isNullOrBlank()) query["organization_id"] = organizationId
        if (!relatedReferenceId.isNullOrBlank()) query["related_reference_id"] = relatedReferenceId
        return client.get("/order/list", PaginatedData::class.java, query)
    }

    fun getOrderSubmerchants(page: Int, perPage: Int): PaginatedData =
        client.get(
            "/order/submerchants",
            PaginatedData::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun getCheckoutUrl(referenceId: String): String? = getOrder(referenceId).checkoutUrl

    fun getOrderStatus(orderReferenceId: String): OrderStatus =
        client.get("/order/$orderReferenceId/status", OrderStatus::class.java)

    fun getOrderPaymentDetails(referenceId: String? = null, conversationId: String? = null): Map<String, Any?> {
        val query = linkedMapOf<String, String>()
        if (!referenceId.isNullOrBlank()) {
            query["reference_id"] = referenceId
        } else if (!conversationId.isNullOrBlank()) {
            query["conversation_id"] = conversationId
        }
        return client.getMap("/order/payment-details", query)
    }

    fun getOrderTransactions(referenceId: String): Map<String, Any?> = client.getMap("/order/$referenceId/transactions")

    fun cancelOrder(payload: CancelOrder): RefundCancelOrderResponse =
        client.post("/order/cancel", payload, RefundCancelOrderResponse::class.java)

    fun refundOrder(payload: RefundOrder): RefundCancelOrderResponse =
        client.post("/order/refund", payload, RefundCancelOrderResponse::class.java)

    fun refundAllOrder(referenceId: String): RefundCancelOrderResponse = refundOrder(RefundOrder(referenceId = referenceId))

    fun getOrderTerm(termReferenceId: String): Map<String, Any?> = client.getMap("/order/term/$termReferenceId")

    fun createOrderTerm(term: OrderPaymentTermCreateDTO): Map<String, Any?> = client.postMap("/order/term/create", term)

    fun deleteOrderTerm(orderId: String, termReferenceId: String): Map<String, Any?> =
        client.postMap("/order/term/delete", mapOf("order_id" to orderId, "term_reference_id" to termReferenceId))

    fun updateOrderTerm(term: OrderPaymentTermUpdateDTO): Map<String, Any?> = client.postMap("/order/term/update", term)

    fun refundOrderTerm(term: OrderTermRefundRequest): Map<String, Any?> = client.postMap("/order/term/refund", term)

    fun orderTerminate(referenceId: String): Map<String, Any?> =
        client.postMap("/order/terminate", mapOf("reference_id" to referenceId))

    fun orderManualCallback(referenceId: String, conversationId: String? = null): Map<String, Any?> {
        val payload = linkedMapOf("reference_id" to referenceId)
        if (!conversationId.isNullOrBlank()) payload["conversation_id"] = conversationId
        return client.postMap("/order/manual-callback", payload)
    }

    fun orderRelatedUpdate(referenceId: String, relatedReferenceId: String): Map<String, Any?> = client.postMap(
        "/order/related-update",
        mapOf("reference_id" to referenceId, "related_reference_id" to relatedReferenceId),
    )
}

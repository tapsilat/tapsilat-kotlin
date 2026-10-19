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

    fun getOrder(orderReferenceId: String): OrderDetail =
        client.get("/order/${SharedApiClient.pathId(orderReferenceId, "orderReferenceId")}", OrderDetail::class.java)

    fun getOrderByConversationId(conversationId: String): OrderDetail =
        client.get("/order/conversation/${SharedApiClient.pathId(conversationId, "conversationId")}", OrderDetail::class.java)

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
        conversationId: String? = null,
        status: Int? = null,
    ): PaginatedData {
        val query = linkedMapOf("page" to page.toString(), "per_page" to perPage.toString())
        if (!buyerId.isNullOrBlank()) query["buyer_id"] = buyerId
        if (!startDate.isNullOrBlank()) query["start_date"] = startDate
        if (!endDate.isNullOrBlank()) query["end_date"] = endDate
        if (!organizationId.isNullOrBlank()) query["organization_id"] = organizationId
        if (!relatedReferenceId.isNullOrBlank()) query["related_reference_id"] = relatedReferenceId
        if (!conversationId.isNullOrBlank()) query["conversation_id"] = conversationId
        if (status != null) query["status"] = status.toString()
        return client.get("/order/list", PaginatedData::class.java, query)
    }

    /** Lists the submerchants of the organization's orders; rows are returned under `row`. */
    fun getOrderSubmerchants(page: Int, perPage: Int): OrderSubmerchantListResponse =
        client.get(
            "/order/submerchants",
            OrderSubmerchantListResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun getCheckoutUrl(referenceId: String): String? = getOrder(referenceId).checkoutUrl

    fun getOrderStatus(orderReferenceId: String): OrderStatus =
        client.get("/order/${SharedApiClient.pathId(orderReferenceId, "orderReferenceId")}/status", OrderStatus::class.java)

    /** Returns payment details by reference ID, or by conversation ID when no reference ID is given. */
    fun getOrderPaymentDetails(referenceId: String? = null, conversationId: String? = null): Map<String, Any?> =
        if (!referenceId.isNullOrBlank()) {
            client.getMap("/order/${SharedApiClient.pathId(referenceId, "referenceId")}/payment-details")
        } else {
            if (conversationId.isNullOrBlank()) {
                throw ValidationError(message = "referenceId or conversationId is required")
            }
            client.postMap("/order/payment-details", mapOf("conversation_id" to conversationId))
        }

    /** Returns the order's transactions; the list is empty when there are none. */
    fun getOrderTransactions(referenceId: String): List<Map<String, Any?>> =
        client.getList("/order/${SharedApiClient.pathId(referenceId, "referenceId")}/transactions")

    /** Lists payments made against an order, identified by order ID, reference ID or conversation ID. */
    fun getOrderPayments(payload: GetOrderPaymentsRequest): GetOrderPaymentsResponse =
        client.post("/order/payments", payload, GetOrderPaymentsResponse::class.java)

    fun cancelOrder(payload: CancelOrder): RefundCancelOrderResponse =
        client.post("/order/cancel", payload, RefundCancelOrderResponse::class.java)

    fun refundOrder(payload: RefundOrder): RefundCancelOrderResponse =
        client.post("/order/refund", payload, RefundCancelOrderResponse::class.java)

    /** Refunds the full paid amount of an order. */
    fun refundAllOrder(referenceId: String): RefundCancelOrderResponse =
        client.post("/order/refund-all", mapOf("reference_id" to referenceId), RefundCancelOrderResponse::class.java)

    /** Submits the stored item payout to the approval workflow. */
    @Deprecated("Use MarketplaceService.approveMarketplacePayouts with explicit payout IDs")
    fun approveSubmerchantPayment(payload: SubmerchantPaymentAction): SubmerchantPaymentActionResponse =
        client.post("/submerchants/payment/approve", payload, SubmerchantPaymentActionResponse::class.java)

    /** Withdraws a stored payout's approval; inspect the status and keep the operation ID. */
    fun disapproveSubmerchantPayment(payload: SubmerchantPaymentAction): SubmerchantPaymentActionResponse =
        client.post("/submerchants/payment/disapprove", payload, SubmerchantPaymentActionResponse::class.java)

    /** Changes a stored payout allocation; inspect the status and keep the operation ID. */
    fun updateSubmerchantPaymentItem(payload: SubmerchantPaymentItemUpdate): SubmerchantPaymentItemUpdateResponse =
        client.put("/submerchants/payment/item", payload, SubmerchantPaymentItemUpdateResponse::class.java)

    /** Returns a payment term by its term reference ID. */
    fun getOrderTerm(termReferenceId: String): Map<String, Any?> =
        client.getMap("/order/term", mapOf("term_reference_id" to termReferenceId))

    /** Adds a payment term to an existing order. */
    fun createOrderTerm(term: OrderPaymentTermCreateDTO): Map<String, Any?> = client.postMap("/order/term", term)

    /** Removes a payment term; [orderId] is the order's ID, not its reference ID. */
    fun deleteOrderTerm(orderId: String, termReferenceId: String): Map<String, Any?> =
        client.deleteMap("/order/term", mapOf("order_id" to orderId, "term_reference_id" to termReferenceId))

    /** Replaces a payment term with the supplied values. */
    fun updateOrderTerm(term: OrderPaymentTermUpdateDTO): Map<String, Any?> = client.patchMap("/order/term", term)

    /** Refunds a payment made against a payment term. */
    fun refundOrderTerm(term: OrderTermRefundRequest): Map<String, Any?> = client.postMap("/order/term/refund", term)

    fun orderTerminate(referenceId: String): Map<String, Any?> =
        client.postMap("/order/terminate", mapOf("reference_id" to referenceId))

    /** Asks the server to resend the order's callback. */
    fun orderManualCallback(referenceId: String, conversationId: String? = null): Map<String, Any?> {
        val payload = linkedMapOf("reference_id" to referenceId)
        if (!conversationId.isNullOrBlank()) payload["conversation_id"] = conversationId
        return client.postMap("/order/callback", payload)
    }

    /** Links an order to a related reference ID. */
    fun orderRelatedUpdate(referenceId: String, relatedReferenceId: String): Map<String, Any?> = client.patchMap(
        "/order/releated",
        mapOf("reference_id" to referenceId, "related_reference_id" to relatedReferenceId),
    )
}

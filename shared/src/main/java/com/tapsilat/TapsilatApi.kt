package com.tapsilat

import java.time.Duration

class TapsilatApi private constructor(
    endpoint: String,
    token: String,
    timeout: Duration,
) {
    private val client = SharedApiClient(endpoint, token, timeout)

    val organization = OrganizationService(client)
    private val currencyResolver = CurrencyReferenceResolver(organization).also { resolver ->
        organization.onCurrenciesChanged = resolver::invalidateCache
    }
    val order = OrderService(client)
    val submerchant = SubmerchantService(client)
    val vpos = VposService(client)
    val subscription = SubscriptionService(client)
    val checkout = CheckoutService(client)
    val credit = CreditService(client)
    val tokenization = TokenizationService(client)
    val marketplace = MarketplaceService(client, currencyResolver)
    val elements = ElementsService(client)

    companion object {
        @JvmOverloads
        fun newApi(token: String, timeout: Duration = Duration.ofSeconds(30)): TapsilatApi =
            TapsilatApi(TapsilatConstants.DEFAULT_ENDPOINT, token, timeout)

        @JvmOverloads
        fun newCustomApi(endpoint: String, token: String, timeout: Duration = Duration.ofSeconds(30)): TapsilatApi =
            TapsilatApi(endpoint, token, timeout)
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    fun createOrder(payload: Order): OrderResponse = order.createOrder(payload)

    fun getOrder(orderReferenceId: String): OrderDetail = order.getOrder(orderReferenceId)

    fun getOrderByConversationId(conversationId: String): OrderDetail = order.getOrderByConversationId(conversationId)

    @Deprecated("Use getOrderByConversationId", ReplaceWith("getOrderByConversationId(conversationId)"))
    fun getOrderByConversationID(conversationId: String): OrderDetail = getOrderByConversationId(conversationId)

    @Deprecated(
        "page and perPage should be Int; use getOrderList instead",
        ReplaceWith("getOrderList(page.toInt(), perPage.toInt(), buyerId = buyerId)"),
    )
    @Suppress("DEPRECATION")
    fun getOrders(page: String, perPage: String, buyerId: String? = null): PaginatedData =
        order.getOrders(page, perPage, buyerId)

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
    ): PaginatedData = order.getOrderList(
        page, perPage, buyerId, startDate, endDate, organizationId, relatedReferenceId, conversationId, status,
    )

    fun getOrderSubmerchants(page: Int, perPage: Int): OrderSubmerchantListResponse = order.getOrderSubmerchants(page, perPage)

    fun getCheckoutUrl(referenceId: String): String? = order.getCheckoutUrl(referenceId)

    fun getOrderStatus(orderReferenceId: String): OrderStatus = order.getOrderStatus(orderReferenceId)

    fun getOrderPaymentDetails(referenceId: String? = null, conversationId: String? = null): Map<String, Any?> =
        order.getOrderPaymentDetails(referenceId, conversationId)

    fun getOrderTransactions(referenceId: String): List<Map<String, Any?>> = order.getOrderTransactions(referenceId)

    fun getOrderPayments(payload: GetOrderPaymentsRequest): GetOrderPaymentsResponse = order.getOrderPayments(payload)

    fun chargeOrder(request: OrderChargeRequest): OrderChargeResponse = checkout.chargeOrder(request)

    @Deprecated("Use marketplace.approveMarketplacePayouts with explicit payout IDs")
    @Suppress("DEPRECATION")
    fun approveSubmerchantPayment(payload: SubmerchantPaymentAction): SubmerchantPaymentActionResponse =
        order.approveSubmerchantPayment(payload)

    fun disapproveSubmerchantPayment(payload: SubmerchantPaymentAction): SubmerchantPaymentActionResponse =
        order.disapproveSubmerchantPayment(payload)

    fun updateSubmerchantPaymentItem(payload: SubmerchantPaymentItemUpdate): SubmerchantPaymentItemUpdateResponse =
        order.updateSubmerchantPaymentItem(payload)

    fun getCardInstallments(request: CardInstallmentsRequest): CardInstallmentsResponse = checkout.getCardInstallments(request)

    fun getInstallmentTable(request: InstallmentTableRequest): InstallmentTableResponse = checkout.getInstallmentTable(request)

    fun cancelOrder(payload: CancelOrder): RefundCancelOrderResponse = order.cancelOrder(payload)

    fun refundOrder(payload: RefundOrder): RefundCancelOrderResponse = order.refundOrder(payload)

    fun refundAllOrder(referenceId: String): RefundCancelOrderResponse = order.refundAllOrder(referenceId)

    fun getOrderTerm(termReferenceId: String): Map<String, Any?> = order.getOrderTerm(termReferenceId)

    fun createOrderTerm(term: OrderPaymentTermCreateDTO): Map<String, Any?> = order.createOrderTerm(term)

    fun deleteOrderTerm(orderId: String, termReferenceId: String): Map<String, Any?> =
        order.deleteOrderTerm(orderId, termReferenceId)

    fun updateOrderTerm(term: OrderPaymentTermUpdateDTO): Map<String, Any?> = order.updateOrderTerm(term)

    fun refundOrderTerm(term: OrderTermRefundRequest): Map<String, Any?> = order.refundOrderTerm(term)

    fun orderTerminate(referenceId: String): Map<String, Any?> = order.orderTerminate(referenceId)

    fun orderManualCallback(referenceId: String, conversationId: String? = null): Map<String, Any?> =
        order.orderManualCallback(referenceId, conversationId)

    fun orderRelatedUpdate(referenceId: String, relatedReferenceId: String): Map<String, Any?> =
        order.orderRelatedUpdate(referenceId, relatedReferenceId)

    // ── Organization ─────────────────────────────────────────────────────────

    fun getOrganizationSettings(): OrganizationSettings = organization.getOrganizationSettings()

    fun getOrganizationCurrencies(): OrganizationCurrenciesResponse = organization.getOrganizationCurrencies()

    fun listOrganizationCurrencyPresets(): OrganizationCurrencyPresetsResponse =
        organization.listOrganizationCurrencyPresets()

    fun createOrganizationCurrency(currencyCode: String): CreateOrganizationCurrencyResponse =
        organization.createOrganizationCurrency(currencyCode)

    fun createOrganizationUser(payload: OrgCreateUserRequest): OrgCreateUserResponse =
        organization.createOrganizationUser(payload)

    fun createOrganizationUserToken(payload: OrgUserTokenCreateRequest): OrgUserTokenCreateResponse =
        organization.createOrganizationUserToken(payload)

    fun resolveCurrencyId(ref: String): String = currencyResolver.resolveCurrencyId(ref)

    // ── Submerchants ──────────────────────────────────────────────────────────

    fun createSubmerchant(payload: SubmerchantCreateRequest): SubmerchantMutationResponse =
        submerchant.createSubmerchant(payload)

    fun getSubmerchant(id: String): Submerchant = submerchant.getSubmerchant(id)

    fun listSubmerchants(page: Int, perPage: Int): SubmerchantListResponse = submerchant.listSubmerchants(page, perPage)

    fun updateSubmerchant(id: String, payload: SubmerchantUpdateRequest): SubmerchantMutationResponse =
        submerchant.updateSubmerchant(id, payload)

    fun deleteSubmerchant(id: String): SubmerchantMutationResponse = submerchant.deleteSubmerchant(id)

    fun getSuborganizations(page: Int, perPage: Int): SuborganizationListResponse =
        submerchant.getSuborganizations(page, perPage)

    @Deprecated("Use getSuborganizationDetail", ReplaceWith("getSuborganizationDetail(id)"))
    @Suppress("DEPRECATION")
    fun getSuborganization(id: String): SuborganizationListItem = submerchant.getSuborganization(id)

    fun getSuborganizationDetail(id: String): SuborganizationDetail = submerchant.getSuborganizationDetail(id)

    fun getSuborganizationBySubmerchant(submerchantId: String): SubmerchantSuborganizationMapping =
        submerchant.getSuborganizationBySubmerchant(submerchantId)

    fun getSubmerchantBySuborganization(suborganizationId: String): SuborganizationSubmerchantMapping =
        submerchant.getSubmerchantBySuborganization(suborganizationId)

    fun recordSubmerchantPayoutEvent(payload: SubmerchantPayoutEventRequest): SubmerchantPayoutEventResponse =
        submerchant.recordSubmerchantPayoutEvent(payload)

    fun createMarketplaceSubmerchant(payload: MarketplaceSubmerchantCreateRequest): MarketplaceSubmerchantCreateResponse =
        submerchant.createMarketplaceSubmerchant(payload)

    // ── VPOS ─────────────────────────────────────────────────────────────────

    fun listVpos(page: Int, perPage: Int): VposListResponse = vpos.listVpos(page, perPage)

    fun listVposWithFilter(page: Int, perPage: Int, filter: VposListFilter): VposListResponse =
        vpos.listVposWithFilter(page, perPage, filter)

    fun createVpos(payload: VposCreateRequest): VposMutationResponse = vpos.createVpos(payload)

    fun getVpos(id: String): Vpos = vpos.getVpos(id)

    fun updateVpos(id: String, payload: VposUpdateRequest): VposMutationResponse = vpos.updateVpos(id, payload)

    fun deleteVpos(id: String): VposMutationResponse = vpos.deleteVpos(id)

    fun listVposAcquirers(): VposAcquirerListResponse = vpos.listVposAcquirers()

    fun listCardSchemes(): CardSchemeListResponse = vpos.listCardSchemes()

    fun listVposAcquirerTemplates(): VposAcquirerTemplateListResponse = vpos.listVposAcquirerTemplates()

    fun listVposSubmerchants(
        page: Int,
        perPage: Int,
        vposId: String? = null,
        externalReferenceId: String? = null,
    ): VposSubmerchantListResponse = vpos.listVposSubmerchants(page, perPage, vposId, externalReferenceId)

    fun createVposSubmerchant(payload: VposSubmerchantCreateRequest): VposSubmerchantMutationResponse =
        vpos.createVposSubmerchant(payload)

    fun getVposSubmerchant(id: String): VposSubmerchant = vpos.getVposSubmerchant(id)

    fun updateVposSubmerchant(id: String, payload: VposSubmerchantUpdateRequest): VposSubmerchantMutationResponse =
        vpos.updateVposSubmerchant(id, payload)

    fun deleteVposSubmerchant(id: String): VposSubmerchantMutationResponse = vpos.deleteVposSubmerchant(id)

    // ── Subscriptions ─────────────────────────────────────────────────────────

    fun getSubscription(payload: SubscriptionGetRequest): SubscriptionDetail = subscription.getSubscription(payload)

    fun cancelSubscription(payload: SubscriptionCancelRequest) = subscription.cancelSubscription(payload)

    fun createSubscription(payload: SubscriptionCreateRequest): SubscriptionCreateResponse =
        subscription.createSubscription(payload)

    fun listSubscriptions(page: Int, perPage: Int): PaginatedData = subscription.listSubscriptions(page, perPage)

    fun redirectSubscription(payload: SubscriptionRedirectRequest): SubscriptionRedirectResponse =
        subscription.redirectSubscription(payload)

    // ── Tokenization ──────────────────────────────────────────────────────────

    fun tokenizeCard(payload: CardTokenizeRequest): CardTokenizeResponse = tokenization.tokenizeCard(payload)

    fun listSavedCards(page: Int, perPage: Int): ListSavedCardsResponse = tokenization.listSavedCards(page, perPage)

    fun deleteSavedCard(id: String): DeleteSavedCardResponse = tokenization.deleteSavedCard(id)

    // ── Elements ──────────────────────────────────────────────────────────────

    fun createElementsSession(payload: ElementsSessionCreateRequest): ElementsSessionWithSecret =
        elements.createElementsSession(payload)

    fun getElementsSession(id: String): ElementsSession = elements.getElementsSession(id)

    fun cancelElementsSession(id: String): ElementsSession = elements.cancelElementsSession(id)
}


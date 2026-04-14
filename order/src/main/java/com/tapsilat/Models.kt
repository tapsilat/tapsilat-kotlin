package com.tapsilat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderMetadata(
    val key: String? = null,
    val value: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPaymentTerm(
    val amount: Double? = null,
    val data: String? = null,
    val dueDate: String? = null,
    val paidDate: String? = null,
    val required: Boolean? = null,
    val status: String? = null,
    val termReferenceId: String? = null,
    val termSequence: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Order(
    val locale: String? = null,
    val amount: Double? = null,
    val taxAmount: Double? = null,
    val currency: String? = null,
    val conversationId: String? = null,
    val buyer: OrderBuyer = OrderBuyer(),
    val shippingAddress: OrderShippingAddress = OrderShippingAddress(),
    val billingAddress: OrderBillingAddress = OrderBillingAddress(),
    val basketItems: List<OrderBasketItem> = emptyList(),
    val submerchants: List<OrderSubmerchant> = emptyList(),
    val checkoutDesign: OrderCheckoutDesign = OrderCheckoutDesign(),
    val paymentMethods: Boolean? = null,
    val paymentFailureUrl: String? = null,
    val paymentSuccessUrl: String? = null,
    val pfSubMerchant: OrderPfSubMerchant = OrderPfSubMerchant(),
    val threeDForce: Boolean? = null,
    val enabledInstallments: List<Int> = emptyList(),
    val paymentOptions: List<String> = emptyList(),
    val metadata: List<OrderMetadata> = emptyList(),
    val paymentTerms: List<OrderPaymentTerm> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPfSubMerchant(
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val countryIsoCode: String? = null,
    val id: String? = null,
    val mcc: String? = null,
    val name: String? = null,
    val orgId: String? = null,
    val postalCode: String? = null,
    val terminalNo: String? = null,
    val submerchantNin: String? = null,
    val submerchantUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderDetail(
    val locale: String? = null,
    val error: String? = null,
    val code: Int? = null,
    val referenceId: String? = null,
    val amount: String? = null,
    val total: String? = null,
    val paidAmount: String? = null,
    val refundedAmount: String? = null,
    val createdAt: String? = null,
    val currency: String? = null,
    val status: Int? = null,
    val statusEnum: String? = null,
    val buyer: OrderBuyer = OrderBuyer(),
    val shippingAddress: OrderShippingAddress = OrderShippingAddress(),
    val billingAddress: OrderBillingAddress = OrderBillingAddress(),
    val basketItems: List<OrderBasketItem> = emptyList(),
    val submerchants: List<OrderSubmerchant> = emptyList(),
    val paymentFailureUrl: String? = null,
    val paymentSuccessUrl: String? = null,
    val checkoutUrl: String? = null,
    val conversationId: String? = null,
    val paymentOptions: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderBuyer(
    val id: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val email: String? = null,
    val gsmNumber: String? = null,
    val identityNumber: String? = null,
    val registrationDate: String? = null,
    val registrationAddress: String? = null,
    val lastLoginDate: String? = null,
    val city: String? = null,
    val country: String? = null,
    val zipCode: String? = null,
    val ip: String? = null,
    val birdthDate: String? = null,
    val title: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderShippingAddress(
    val address: String? = null,
    val zipCode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val contactName: String? = null,
    val trackingCode: String? = null,
    val shippingDate: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderBillingAddress(
    val billingType: String? = null,
    val citizenship: String? = null,
    val title: String? = null,
    val taxOffice: String? = null,
    val address: String? = null,
    val zipCode: String? = null,
    val city: String? = null,
    val district: String? = null,
    val country: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val vatNumber: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderBasketItemPayer(
    val address: String? = null,
    val referenceId: String? = null,
    val taxOffice: String? = null,
    val title: String? = null,
    val type: String? = null,
    val vat: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderBasketItem(
    val id: String? = null,
    val price: Double? = null,
    val name: String? = null,
    val category1: String? = null,
    val category2: String? = null,
    val itemType: String? = null,
    val status: Long? = null,
    val refundedAmount: Double? = null,
    val refundableAmount: Double? = null,
    val paidAmount: Double? = null,
    val paidableAmount: Double? = null,
    val coupon: String? = null,
    val couponDiscount: Double? = null,
    val quantity: Int? = null,
    val quantityFloat: Double? = null,
    val quantityUnit: String? = null,
    val data: String? = null,
    val commissionAmount: Double? = null,
    val subMerchantKey: String? = null,
    val subMerchantPrice: String? = null,
    val payer: OrderBasketItemPayer? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSubmerchant(
    val amount: Double? = null,
    val orderBasketItemId: String? = null,
    val merchantReferenceId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderCheckoutDesign(
    val logo: String? = null,
    val inputBackgroundColor: String? = null,
    val payButtonColor: String? = null,
    val inputTextColor: String? = null,
    val labelTextColor: String? = null,
    val leftBackgroundColor: String? = null,
    val rightBackgroundColor: String? = null,
    val textColor: String? = null,
    val orderDetailHtml: String? = null,
    val redirectUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderResponse(
    val orderId: String? = null,
    val referenceId: String? = null,
    val checkoutUrl: String? = null,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderStatus(
    val status: String? = null,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RefundOrder(
    val referenceId: String,
    val amount: Double? = null,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CancelOrder(
    val referenceId: String,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RefundCancelOrderResponse(
    val status: String? = null,
    val message: String? = null,
    val isSuccess: Boolean? = null,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaginatedData(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Int? = null,
    val rows: JsonNode? = null,
    val error: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPaymentTermCreateDTO(
    val orderReferenceId: String,
    val amount: Double,
    val dueDate: String,
    val required: Boolean,
    val data: String? = null,
    val termSequence: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPaymentTermUpdateDTO(
    val termReferenceId: String,
    val amount: Double? = null,
    val dueDate: String? = null,
    val required: Boolean? = null,
    val data: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderTermRefundRequest(
    val termReferenceId: String,
    val amount: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationSettings(
    val ttl: Long? = null,
    val retryCount: Long? = null,
    val allowPayment: Boolean? = null,
    val sessionTtl: Long? = null,
    val customCheckout: Boolean? = null,
    val domainAddress: String? = null,
    val checkoutDomain: String? = null,
    val subscriptionDomain: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationCurrency(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val symbol: String? = null,
    val currencyUnit: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationCurrenciesResponse(
    val currencies: List<OrganizationCurrency> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationCurrencyPreset(
    val currencyCode: String? = null,
    val currencyUnit: String? = null,
    val name: String? = null,
    val minorUnit: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationCurrencyPresetsResponse(
    val items: List<OrganizationCurrencyPreset> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateOrganizationCurrencyResponse(
    val code: Long? = null,
    val message: String? = null,
    val created: Boolean? = null,
    val currency: OrganizationCurrency? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantCreateRequest(
    val locale: String? = null,
    val conversationId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val gsmNumber: String? = null,
    val address: String? = null,
    val iban: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val currencyId: String,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val submerchantType: String? = null,
    val taxNumber: String? = null,
    val subMerchantKey: String? = null,
    val organizationId: String? = null,
    val status: String? = null,
    val systemTime: Long? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantUpdateRequest(
    val locale: String? = null,
    val conversationId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val gsmNumber: String? = null,
    val address: String? = null,
    val iban: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val currencyId: String,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val submerchantType: String? = null,
    val taxNumber: String? = null,
    val subMerchantKey: String? = null,
    val organizationId: String? = null,
    val status: String? = null,
    val systemTime: Long? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Submerchant(
    val id: String? = null,
    val locale: String? = null,
    val conversationId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val gsmNumber: String? = null,
    val address: String? = null,
    val iban: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val currencyId: String? = null,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val submerchantType: String? = null,
    val taxNumber: String? = null,
    val subMerchantKey: String? = null,
    val organizationId: String? = null,
    val labels: String? = null,
    val status: String? = null,
    val systemTime: Long? = null,
    val acquirer: String? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantListItem(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val submerchantType: String? = null,
    val submerchantKey: String? = null,
    val labels: String? = null,
    val status: String? = null,
    val acquirer: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantListResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val row: List<SubmerchantListItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantMutationResponse(
    val code: Long? = null,
    val message: String? = null,
    val status: String? = null,
    val locale: String? = null,
    val systemTime: Long? = null,
    val conversationId: String? = null,
    val subMerchantKey: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SuborganizationListItem(
    val id: String? = null,
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SuborganizationDetail(
    val id: String? = null,
    val name: String? = null,
    val parentId: String? = null,
    val publicStatus: Long? = null,
    val availabilityStatus: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SuborganizationListResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val rows: List<SuborganizationListItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantSuborganizationMapping(
    val submerchantId: String? = null,
    val suborganizationId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SuborganizationSubmerchantMapping(
    val suborganizationId: String? = null,
    val submerchantId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposListFilter(
    val suborganizationId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposCreateRequest(
    val name: String? = null,
    val bankName: String? = null,
    val envMode: String? = null,
    val merchant: String? = null,
    val merchantCode: String? = null,
    val merchantKey: String? = null,
    val authKey: String? = null,
    val terminal: String? = null,
    val company: String? = null,
    val username: String? = null,
    val password: String? = null,
    val storeKey: String? = null,
    val apiKey: String? = null,
    val apiSecret: String? = null,
    val guid: String? = null,
    val pid: String? = null,
    val clientId: String? = null,
    val clientCode: String? = null,
    val type: String? = null,
    val blockDate: Long? = null,
    val priority: Long? = null,
    val main: Boolean? = null,
    val forceThreeD: Boolean? = null,
    val marketplace: Boolean? = null,
    val pf: Boolean? = null,
    val paymentMode: String? = null,
    val prefix: String? = null,
    val acquirerId: String? = null,
    val cardSchemes: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposUpdateRequest(
    val name: String? = null,
    val bankName: String? = null,
    val envMode: String? = null,
    val merchant: String? = null,
    val merchantCode: String? = null,
    val merchantKey: String? = null,
    val authKey: String? = null,
    val terminal: String? = null,
    val company: String? = null,
    val username: String? = null,
    val password: String? = null,
    val storeKey: String? = null,
    val apiKey: String? = null,
    val apiSecret: String? = null,
    val guid: String? = null,
    val pid: String? = null,
    val clientId: String? = null,
    val clientCode: String? = null,
    val type: String? = null,
    val blockDate: Long? = null,
    val priority: Long? = null,
    val main: Boolean? = null,
    val forceThreeD: Boolean? = null,
    val marketplace: Boolean? = null,
    val pf: Boolean? = null,
    val paymentMode: String? = null,
    val prefix: String? = null,
    val acquirerId: String? = null,
    val cardSchemes: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vpos(
    val id: String? = null,
    val name: String? = null,
    val bankName: String? = null,
    val envMode: String? = null,
    val provider: String? = null,
    val paymentMode: String? = null,
    val merchant: String? = null,
    val merchantCode: String? = null,
    val merchantKey: String? = null,
    val authKey: String? = null,
    val terminal: String? = null,
    val company: String? = null,
    val username: String? = null,
    val password: String? = null,
    val storeKey: String? = null,
    val apiKey: String? = null,
    val apiSecret: String? = null,
    val guid: String? = null,
    val pid: String? = null,
    val clientId: String? = null,
    val clientCode: String? = null,
    val type: String? = null,
    val blockDate: Long? = null,
    val priority: Long? = null,
    val main: Boolean? = null,
    val forceThreeD: Boolean? = null,
    val marketplace: Boolean? = null,
    val pf: Boolean? = null,
    val prefix: String? = null,
    val acquirerId: String? = null,
    val cardSchemes: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposListItem(
    val id: String? = null,
    val name: String? = null,
    val bankName: String? = null,
    val envMode: String? = null,
    val provider: String? = null,
    val paymentMode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposListResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val rows: List<VposListItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposMutationResponse(
    val code: Long? = null,
    val message: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposAcquirer(
    val id: String? = null,
    val name: String? = null,
    val prefix: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposAcquirerListResponse(
    val items: List<VposAcquirer> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardScheme(
    val id: String? = null,
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardSchemeListResponse(
    val items: List<CardScheme> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposAcquirerTemplateDefaults(
    val paymentMode: String? = null,
    val main: Boolean? = null,
    val marketplace: Boolean? = null,
    val forceThreeD: Boolean? = null,
    val credentials: Map<String, String> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposAcquirerTemplate(
    val acquirerId: String? = null,
    val name: String? = null,
    val prefix: String? = null,
    val requiredFields: List<String> = emptyList(),
    val optionalFields: List<String> = emptyList(),
    val defaults: VposAcquirerTemplateDefaults = VposAcquirerTemplateDefaults(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposAcquirerTemplateListResponse(
    val items: List<VposAcquirerTemplate> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchantCreateRequest(
    val externalReferenceId: String? = null,
    val submerchantId: String? = null,
    val terminalNo: String? = null,
    val vposId: String? = null,
    val mcc: String? = null,
    val taxId: String? = null,
    val nationalId: String? = null,
    val title: String? = null,
    val switchId: String? = null,
    val city: String? = null,
    val country: String? = null,
    val countryIsocode: String? = null,
    val postalCode: String? = null,
    val address: String? = null,
    val submerchantUrl: String? = null,
    val submerchantNin: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchantUpdateRequest(
    val externalReferenceId: String? = null,
    val submerchantId: String? = null,
    val terminalNo: String? = null,
    val mcc: String? = null,
    val taxId: String? = null,
    val nationalId: String? = null,
    val title: String? = null,
    val switchId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchant(
    val id: String? = null,
    val externalReferenceId: String? = null,
    val submerchantId: String? = null,
    val terminalNo: String? = null,
    val vposId: String? = null,
    val mcc: String? = null,
    val taxId: String? = null,
    val nationalId: String? = null,
    val title: String? = null,
    val switchId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchantListItem(
    val id: String? = null,
    val externalReferenceId: String? = null,
    val submerchantId: String? = null,
    val terminalNo: String? = null,
    val vposId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchantListResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val rows: List<VposSubmerchantListItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VposSubmerchantMutationResponse(
    val code: Long? = null,
    val message: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionGetRequest(
    val externalReferenceId: String? = null,
    val referenceId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionCancelRequest(
    val externalReferenceId: String? = null,
    val referenceId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionBilling(
    val address: String? = null,
    val city: String? = null,
    val contactName: String? = null,
    val country: String? = null,
    val vatNumber: String? = null,
    val zipCode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionUser(
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val id: String? = null,
    val identityNumber: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val zipCode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionCreateRequest(
    val amount: Double? = null,
    val billing: SubscriptionBilling = SubscriptionBilling(),
    val cardId: String? = null,
    val currency: String? = null,
    val cycle: Int? = null,
    val externalReferenceId: String? = null,
    val failureUrl: String? = null,
    val paymentDate: Int? = null,
    val period: Int? = null,
    val successUrl: String? = null,
    val title: String? = null,
    val user: SubscriptionUser = SubscriptionUser(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionRedirectRequest(
    val subscriptionId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionOrder(
    val amount: String? = null,
    val currency: String? = null,
    val paymentDate: String? = null,
    val paymentUrl: String? = null,
    val referenceId: String? = null,
    val status: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionDetail(
    val amount: String? = null,
    val currency: String? = null,
    val dueDate: String? = null,
    val externalReferenceId: String? = null,
    val isActive: Boolean? = null,
    val orders: List<SubscriptionOrder> = emptyList(),
    val paymentDate: Int? = null,
    val paymentStatus: String? = null,
    val period: Int? = null,
    val title: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionCreateResponse(
    val code: Int? = null,
    val message: String? = null,
    val orderReferenceId: String? = null,
    val referenceId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionListItem(
    val amount: String? = null,
    val currency: String? = null,
    val externalReferenceId: String? = null,
    val isActive: Boolean? = null,
    val paymentDate: Int? = null,
    val paymentStatus: String? = null,
    val period: Int? = null,
    val referenceId: String? = null,
    val title: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubscriptionRedirectResponse(
    val url: String? = null,
)

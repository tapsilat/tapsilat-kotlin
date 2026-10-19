package com.tapsilat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// ── Order payments, charging and submerchant payment actions ───────────────

/** Identifies an order by any one of its identifiers. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetOrderPaymentsRequest(
    val orderId: String? = null,
    val orderReferenceId: String? = null,
    val conversationId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPayment(
    val id: String? = null,
    val date: String? = null,
    val paymentMode: String? = null,
    val amount: Double? = null,
    val cardId: String? = null,
    val maskedCard: String? = null,
    val cardHolderName: String? = null,
    val cardType: String? = null,
    val cardBrand: String? = null,
    val cardBank: String? = null,
    val cardNetwork: String? = null,
    val paid: Boolean? = null,
    val type: String? = null,
    val acquirerResponse: String? = null,
    val vposCommissionAmount: Double? = null,
    val vposCommissionRate: Double? = null,
    val vposCommissionFeeType: String? = null,
    val organizationCommissionAmount: Double? = null,
    val organizationCommissionRate: Double? = null,
    val organizationCommissionFeeType: String? = null,
    val totalAmount: Double? = null,
    val acquirerCommissionAmount: Double? = null,
    val acquirerCommissionRate: Double? = null,
    val acquirerCommissionFeeType: String? = null,
    val acquirerName: String? = null,
    val customerPaidCommissionAmount: Double? = null,
    val merchantPaidCommissionAmount: Double? = null,
    val merchantNetAmount: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetOrderPaymentsResponse(
    val payments: List<OrderPayment> = emptyList(),
    val totalAmount: Double? = null,
    val totalCustomerPaidCommissionAmount: Double? = null,
    val totalMerchantPaidCommissionAmount: Double? = null,
    val totalMerchantNetAmount: Double? = null,
    val orderAmount: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderChargeRequest(
    val orderReferenceId: String,
    val retry: Boolean? = null,
)

/** Charge result; the server sends these keys in camelCase. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderChargeResponse(
    @param:JsonProperty("isSuccess") @get:JsonProperty("isSuccess")
    val isSuccess: Boolean? = null,
    @param:JsonProperty("message") @get:JsonProperty("message")
    val message: String? = null,
    @param:JsonProperty("statusCode") @get:JsonProperty("statusCode")
    val statusCode: String? = null,
    @param:JsonProperty("errorCode") @get:JsonProperty("errorCode")
    val errorCode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPaymentAction(
    val locale: String,
    val conversationId: String,
    val paymentTransactionId: String,
    val acquirer: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPaymentActionResponse(
    val operationId: String? = null,
    val status: String? = null,
    val locale: String? = null,
    val systemTime: Long? = null,
    val conversationId: String? = null,
    val paymentTransactionId: String? = null,
    val acquirer: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPaymentItemUpdate(
    val locale: String,
    val conversationId: String,
    val paymentTransactionId: String,
    val subMerchantKey: String,
    /** Positive decimal amount as a string, for example "60.25". */
    val subMerchantPrice: String,
    val acquirer: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPaymentItemUpdateResponse(
    val operationId: String? = null,
    val status: String? = null,
    val locale: String? = null,
    val systemTime: Long? = null,
    val conversationId: String? = null,
    val paymentTransactionId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSubmerchantListItem(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val submerchantType: String? = null,
    val submerchantKey: String? = null,
    val labels: String? = null,
    val status: String? = null,
    val acquirer: String? = null,
)

/** Paginated submerchants of the organization's orders; the server sends rows under `row`. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSubmerchantListResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val row: List<OrderSubmerchantListItem> = emptyList(),
)

// ── Card installments ─────────────────────────────────────────────────────

data class CardInstallmentsRequest(
    /** The card's first digits (at least 8). */
    val bin: String,
    val amount: Double,
    /** Currency unit or name, for example "TRY". */
    val currency: String,
    val isThreeD: Boolean = false,
) {
    internal fun toQuery(): Map<String, String> = buildMap {
        put("bin", bin)
        put("amount", amount.toBigDecimal().stripTrailingZeros().toPlainString())
        put("currency", currency)
        if (isThreeD) put("is_three_d", "true")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardInstallment(
    val installment: Long? = null,
    val amount: String? = null,
    val totalAmount: String? = null,
    val commissionRate: Double? = null,
    val monthlyPayment: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardInstallmentsResponse(
    val isInstallmentAvailable: Boolean? = null,
    val installments: List<CardInstallment> = emptyList(),
)

data class InstallmentTableRequest(
    val amount: Double,
    /** Currency unit or name, for example "TRY". */
    val currency: String,
    val isThreeD: Boolean = false,
) {
    internal fun toQuery(): Map<String, String> = buildMap {
        put("amount", amount.toBigDecimal().stripTrailingZeros().toPlainString())
        put("currency", currency)
        if (isThreeD) put("is_three_d", "true")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentTableRow(
    val installment: Long? = null,
    val installmentAmount: String? = null,
    val totalAmount: String? = null,
    val commissionRate: Double? = null,
    val isInterestFree: Boolean? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentTableGroup(
    val bank: String? = null,
    val program: String? = null,
    val programCode: String? = null,
    val rows: List<InstallmentTableRow> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentTableResponse(
    val groups: List<InstallmentTableGroup> = emptyList(),
)

// ── Organization users ────────────────────────────────────────────────────

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrgCreateUserRequest(
    val email: String? = null,
    val identityNumber: String? = null,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val referenceId: String? = null,
    val conversationId: String? = null,
    val isMailVerified: Boolean? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrgCreateUserResponse(
    val code: Long? = null,
    val message: String? = null,
    val userId: String? = null,
)

/** Creates an access token for an organization user; [expire] is in minutes. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrgUserTokenCreateRequest(
    val email: String? = null,
    val expire: Int? = null,
    val invalidateOldTokens: Boolean? = null,
    val userId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrgUserTokenCreateResponse(
    val code: Long? = null,
    val message: String? = null,
    val token: String? = null,
    val userId: String? = null,
)

// ── Payout events and marketplace sellers ─────────────────────────────────

/** Records an item business event such as `service_completed`. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPayoutEventRequest(
    val idempotencyKey: String,
    val orderReference: String,
    val itemId: String,
    val eventType: String,
    /** RFC3339 timestamp. */
    val occurredAt: String? = null,
    val payload: Map<String, Any?>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmerchantPayoutEventResponse(
    val id: String? = null,
    val idempotencyKey: String? = null,
    val orderReference: String? = null,
    val itemId: String? = null,
    val eventType: String? = null,
    val occurredAt: String? = null,
    val payload: Map<String, Any?>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceAccountTarget(
    val vposId: String,
    /** TEST or PROD. */
    val environment: String,
)

/**
 * Creates a marketplace seller. Omit both [vposId] and [providerAccounts] to create only the
 * logical seller; never combine them. [currencyId] accepts a currency UUID or a unit such as "TRY".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSubmerchantCreateRequest(
    val locale: String,
    val conversationId: String,
    val name: String,
    val email: String,
    val gsmNumber: String,
    val address: String? = null,
    val iban: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val currencyId: String? = null,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val subMerchantType: String? = null,
    val taxNumber: String? = null,
    val labels: List<String>? = null,
    val status: String? = null,
    val systemTime: Long? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
    val providerAccounts: List<MarketplaceAccountTarget>? = null,
    val vposId: String? = null,
    /** auto or manual. */
    val approvalMode: String? = null,
    val releasePolicyId: String? = null,
    val idempotencyKey: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSubmerchantProvisioning(
    val vposId: String? = null,
    val vposSubmerchantId: String? = null,
    val provider: String? = null,
    val status: String? = null,
    val providerSubmerchantKey: String? = null,
    val retryable: Boolean? = null,
    val errorMessage: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSubmerchantCreateResponse(
    val id: String? = null,
    val suborganizationId: String? = null,
    val vposSubmerchantId: String? = null,
    val routingReference: String? = null,
    val provisionings: List<MarketplaceSubmerchantProvisioning> = emptyList(),
)

// ── Card tokenization ─────────────────────────────────────────────────────

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardTokenizeRequest(
    val cardNumber: String,
    val holderName: String,
    val expiryMonth: String,
    /** Two or four digits. */
    val expiryYear: String,
    val cvv: String,
    val name: String? = null,
    val default: Boolean? = null,
    /** 1 = 3D secure, 2 = half secure, 3 = non-secure. */
    val mode: Int? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val redirectSuccessUrl: String? = null,
    val redirectFailureUrl: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CardTokenizeResponse(
    val threedformUrl: String? = null,
    val threedformHtml: String? = null,
    val isSuccess: Boolean? = null,
    val message: String? = null,
    val statusCode: String? = null,
    val errorCode: String? = null,
    val orderReferenceId: String? = null,
    val cardId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SavedCard(
    val id: String? = null,
    val name: String? = null,
    val holderName: String? = null,
    val maskedNumber: String? = null,
    val lastFour: String? = null,
    val brand: String? = null,
    val bin: String? = null,
    val isDefault: Boolean? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListSavedCardsResponse(
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
    val rows: List<SavedCard> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteSavedCardResponse(
    val success: Boolean? = null,
    val message: String? = null,
)

// ── Marketplace (API version 2) ───────────────────────────────────────────

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSeller(
    val id: String? = null,
    val routingReference: String? = null,
    /** Opaque revision; send it back exactly as received. */
    val revision: String? = null,
    val name: String? = null,
    val email: String? = null,
    val gsmNumber: String? = null,
    val address: String? = null,
    val iban: String? = null,
    val currencyId: String? = null,
    val subMerchantType: String? = null,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val taxNumber: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
    val locale: String? = null,
    val conversationId: String? = null,
    val status: String? = null,
    val labels: List<String> = emptyList(),
    val approvalMode: String? = null,
    val releasePolicyId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSellerList(
    val rows: List<MarketplaceSeller> = emptyList(),
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
)

/**
 * Replaces the seller profile at [revision] (from getMarketplaceSubmerchant, sent back unchanged).
 * Preserve fields you are not changing; account selectors are not accepted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceProfileUpdate(
    val revision: String,
    val locale: String,
    val conversationId: String,
    val name: String,
    val email: String,
    val gsmNumber: String,
    val address: String? = null,
    val iban: String? = null,
    val taxOffice: String? = null,
    val legalCompanyTitle: String? = null,
    val currencyId: String? = null,
    val subMerchantExternalId: String? = null,
    val identityNumber: String? = null,
    val subMerchantType: String? = null,
    val taxNumber: String? = null,
    val labels: List<String>? = null,
    val status: String? = null,
    val contactName: String? = null,
    val contactSurname: String? = null,
    val approvalMode: String? = null,
    val releasePolicyId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceProfileUpdateResponse(
    val approvalRequired: Boolean? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSellerContract(
    val supported: Boolean? = null,
    val remote: Boolean? = null,
    val fields: Map<String, Boolean> = emptyMap(),
    val immutableFields: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceContracts(
    val contracts: Map<String, MarketplaceSellerContract> = emptyMap(),
)

data class MarketplaceContractFilter(
    /** Currency UUID or unit such as "TRY". */
    val currencyId: String,
    val sellerType: String,
    val operation: String,
    val vposIds: List<String>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSynchronization(
    val status: String? = null,
    val operation: String? = null,
    val errorMessage: String? = null,
    val needsUpdate: Boolean? = null,
    val attempts: Long? = null,
    val verifiedAt: String? = null,
    val missingFields: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSellerAccount(
    val vposId: String? = null,
    val name: String? = null,
    val provider: String? = null,
    val environment: String? = null,
    val eligible: Boolean? = null,
    val mapped: Boolean? = null,
    val status: String? = null,
    val attempts: Long? = null,
    val updatedAt: String? = null,
    val errorMessage: String? = null,
    val retryable: Boolean? = null,
    val revision: String? = null,
    val sellerRevision: String? = null,
    val accountRevision: String? = null,
    val actions: List<String> = emptyList(),
    val contracts: Map<String, MarketplaceSellerContract> = emptyMap(),
    val contractError: String? = null,
    val sync: MarketplaceSynchronization? = null,
    val registrationProfileChanged: Boolean? = null,
    val registrationMissingFields: List<String> = emptyList(),
    val providerSubmerchantKey: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceSellerAccounts(
    val rows: List<MarketplaceSellerAccount> = emptyList(),
)

/** Confirms an account action using the account's latest revision and environment. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceAccountAction(
    /** create, retry, resubmit, update, retrieve or retry_sync. */
    val action: String,
    val environment: String,
    val revision: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayout(
    /** Opaque revision; send it back exactly as received. */
    val revision: String? = null,
    val activeOperationId: String? = null,
    val id: String? = null,
    val provider: String? = null,
    val organizationId: String? = null,
    val submerchantId: String? = null,
    val submerchantName: String? = null,
    val orderId: String? = null,
    val orderReferenceId: String? = null,
    val orderDisplay: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val approvalMode: String? = null,
    val approvalRuleId: String? = null,
    val approvalRuleName: String? = null,
    val releasePolicyId: String? = null,
    val releasePolicyName: String? = null,
    val eligibilityStatus: String? = null,
    val requiredEvent: String? = null,
    val itemId: String? = null,
    val businessEligibleAt: String? = null,
    val eligibleAt: String? = null,
    val releaseStatus: String? = null,
    val status: String? = null,
    val providerReference: String? = null,
    val attempts: Long? = null,
    val releasedAt: String? = null,
    val createdAt: String? = null,
    val errorMessage: String? = null,
)

data class MarketplacePayoutFilter(
    val orderId: String? = null,
    val submerchantId: String? = null,
    val vposId: String? = null,
    val provider: String? = null,
    val status: String? = null,
    val releaseStatus: String? = null,
    val approvalMode: String? = null,
    /** Currency unit such as "TRY". */
    val currency: String? = null,
    val eligibility: String? = null,
    val search: String? = null,
    val minAmount: String? = null,
    val maxAmount: String? = null,
    /** YYYY-MM-DD. */
    val startDate: String? = null,
    /** YYYY-MM-DD. */
    val endDate: String? = null,
    val page: Int = 1,
    val perPage: Int = 20,
) {
    internal fun toQuery(): Map<String, String> = buildMap {
        mapOf(
            "order_id" to orderId,
            "submerchant_id" to submerchantId,
            "vpos_id" to vposId,
            "provider" to provider,
            "status" to status,
            "release_status" to releaseStatus,
            "approval_mode" to approvalMode,
            "currency" to currency,
            "eligibility" to eligibility,
            "search" to search,
            "min_amount" to minAmount,
            "max_amount" to maxAmount,
            "start_date" to startDate,
            "end_date" to endDate,
        ).forEach { (key, value) -> if (!value.isNullOrBlank()) put(key, value) }
        put("page", page.toString())
        put("per_page", perPage.toString())
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayoutList(
    val rows: List<MarketplacePayout> = emptyList(),
    val page: Long? = null,
    val perPage: Long? = null,
    val total: Long? = null,
    val totalPages: Long? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayoutApproval(
    /** At most 100 payout IDs. */
    val ids: List<String>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayoutAcceptance(
    val status: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceApprovalPolicy(
    val id: String? = null,
    val name: String? = null,
    val priority: Long? = null,
    val enabled: Boolean? = null,
    val provider: String? = null,
    val minAmount: String? = null,
    val maxAmount: String? = null,
    val currencyId: String? = null,
    val submerchantIds: List<String> = emptyList(),
    val labelsAny: List<String> = emptyList(),
    val mode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceApprovalPolicies(
    val rows: List<MarketplaceApprovalPolicy> = emptyList(),
)

/**
 * Creates or replaces an approval rule. [currencyId] must be a UUID on the wire; the SDK resolves
 * units such as "TRY" before sending.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceApprovalPolicyInput(
    val name: String,
    val priority: Long,
    val mode: String,
    val enabled: Boolean? = null,
    val provider: String? = null,
    val minAmount: String? = null,
    val maxAmount: String? = null,
    val currencyId: String? = null,
    val submerchantIds: List<String> = emptyList(),
    val labelsAny: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceReleasePolicy(
    val id: String? = null,
    val name: String? = null,
    val enabled: Boolean? = null,
    val default: Boolean? = null,
    val minimumDelaySeconds: Long? = null,
    val explicitNotBefore: String? = null,
    val requiredExternalEvent: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceReleasePolicies(
    val rows: List<MarketplaceReleasePolicy> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceReleasePolicyInput(
    val name: String,
    val enabled: Boolean? = null,
    val default: Boolean? = null,
    val minimumDelaySeconds: Long? = null,
    val explicitNotBefore: String? = null,
    val requiredExternalEvent: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplaceApprovalSettings(
    /** auto or manual. */
    val defaultMode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayoutOperationInput(
    /** disapprove or update_item. */
    val kind: String,
    val idempotencyKey: String,
    /** The payout's current revision, sent back exactly as received. */
    val revision: String,
    val sellerReference: String? = null,
    val netAmount: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketplacePayoutOperation(
    val id: String? = null,
    val payoutId: String? = null,
    val kind: String? = null,
    val status: String? = null,
    val attempts: Long? = null,
    val errorMessage: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

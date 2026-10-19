package com.tapsilat

/**
 * Marketplace API (version 2): sellers, provider accounts, payouts, policies and payout operations.
 *
 * Calls use the version 2 routes derived from the configured endpoint and are scoped to the API
 * token's organization. Revisions are opaque strings: send them back exactly as received.
 */
class MarketplaceService internal constructor(
    private val client: SharedApiClient,
    private val currencyResolver: CurrencyReferenceResolver,
) {
    /** Lists sellers; [externalId] selects an exact external ID match. */
    @JvmOverloads
    fun listMarketplaceSubmerchants(page: Int = 1, perPage: Int = 20, externalId: String? = null): MarketplaceSellerList {
        val query = linkedMapOf("page" to page.toString(), "per_page" to perPage.toString())
        if (!externalId.isNullOrBlank()) query["external_id"] = externalId
        return client.get("/submerchants", MarketplaceSellerList::class.java, query, V2)
    }

    /** Returns the seller profile and its current revision. */
    fun getMarketplaceSubmerchant(id: String): MarketplaceSeller =
        client.get("/submerchants/${id(id)}", MarketplaceSeller::class.java, version = V2)

    /** Replaces the seller profile at its revision; inspect [MarketplaceProfileUpdateResponse.approvalRequired]. */
    fun updateMarketplaceSubmerchant(id: String, input: MarketplaceProfileUpdate): MarketplaceProfileUpdateResponse =
        client.patch("/submerchants/${id(id)}", input, MarketplaceProfileUpdateResponse::class.java, V2)

    /** Returns the selected accounts' requirements for a seller operation. */
    fun getMarketplaceSellerContracts(filter: MarketplaceContractFilter): MarketplaceContracts {
        val query = buildString {
            append("?currency_id=").append(encode(filter.currencyId))
            append("&seller_type=").append(encode(filter.sellerType))
            append("&operation=").append(encode(filter.operation))
            filter.vposIds.forEach { append("&vpos_id=").append(encode(it)) }
        }
        return client.get("/marketplace/seller-contracts$query", MarketplaceContracts::class.java, version = V2)
    }

    /** Returns account registration, synchronization and available actions. */
    fun getMarketplaceSellerAccounts(id: String): MarketplaceSellerAccounts =
        client.get("/submerchants/${id(id)}/providers", MarketplaceSellerAccounts::class.java, version = V2)

    /** Runs an action listed on the account at its revision and environment; inspect the returned status. */
    fun runMarketplaceSellerAccountAction(
        id: String,
        vposId: String,
        input: MarketplaceAccountAction,
    ): MarketplaceSubmerchantProvisioning = client.post(
        "/submerchants/${id(id)}/providers/${id(vposId)}/actions",
        input,
        MarketplaceSubmerchantProvisioning::class.java,
        V2,
    )

    fun listMarketplacePayouts(filter: MarketplacePayoutFilter = MarketplacePayoutFilter()): MarketplacePayoutList =
        client.get("/submerchant-payouts", MarketplacePayoutList::class.java, filter.toQuery(), V2)

    /** Returns a payout's eligibility, approval and settlement state. */
    fun getMarketplacePayout(id: String): MarketplacePayout =
        client.get("/submerchant-payouts/${id(id)}", MarketplacePayout::class.java, version = V2)

    /** Submits up to 100 eligible payouts for approval without bypassing release gates. */
    fun approveMarketplacePayouts(input: MarketplacePayoutApproval): MarketplacePayoutAcceptance =
        client.post("/submerchant-payouts/approve", input, MarketplacePayoutAcceptance::class.java, V2)

    fun listMarketplaceApprovalPolicies(): MarketplaceApprovalPolicies =
        client.get("/marketplace/approval-policies", MarketplaceApprovalPolicies::class.java, version = V2)

    fun getMarketplaceApprovalPolicy(id: String): MarketplaceApprovalPolicy =
        client.get("/marketplace/approval-policies/${id(id)}", MarketplaceApprovalPolicy::class.java, version = V2)

    /** Creates an approval rule; a currency unit such as "TRY" is resolved to its UUID. */
    fun createMarketplaceApprovalPolicy(input: MarketplaceApprovalPolicyInput) =
        client.send("POST", "/marketplace/approval-policies", withResolvedCurrency(input), V2)

    /** Replaces an approval rule; a currency unit such as "TRY" is resolved to its UUID. */
    fun updateMarketplaceApprovalPolicy(id: String, input: MarketplaceApprovalPolicyInput) =
        client.send("PATCH", "/marketplace/approval-policies/${id(id)}", withResolvedCurrency(input), V2)

    fun deleteMarketplaceApprovalPolicy(id: String) =
        client.send("DELETE", "/marketplace/approval-policies/${id(id)}", version = V2)

    fun listMarketplaceReleasePolicies(): MarketplaceReleasePolicies =
        client.get("/marketplace/release-policies", MarketplaceReleasePolicies::class.java, version = V2)

    fun getMarketplaceReleasePolicy(id: String): MarketplaceReleasePolicy =
        client.get("/marketplace/release-policies/${id(id)}", MarketplaceReleasePolicy::class.java, version = V2)

    fun createMarketplaceReleasePolicy(input: MarketplaceReleasePolicyInput): MarketplaceReleasePolicy =
        client.post("/marketplace/release-policies", input, MarketplaceReleasePolicy::class.java, V2)

    fun updateMarketplaceReleasePolicy(id: String, input: MarketplaceReleasePolicyInput): MarketplaceReleasePolicy =
        client.patch("/marketplace/release-policies/${id(id)}", input, MarketplaceReleasePolicy::class.java, V2)

    fun deleteMarketplaceReleasePolicy(id: String) =
        client.send("DELETE", "/marketplace/release-policies/${id(id)}", version = V2)

    fun getMarketplaceApprovalSettings(): MarketplaceApprovalSettings =
        client.get("/marketplace/approval-settings", MarketplaceApprovalSettings::class.java, version = V2)

    fun updateMarketplaceApprovalSettings(input: MarketplaceApprovalSettings): MarketplaceApprovalSettings =
        client.patch("/marketplace/approval-settings", input, MarketplaceApprovalSettings::class.java, V2)

    /** Changes a payout allocation or withdraws approval; inspect the status before treating it as complete. */
    fun runMarketplacePayoutOperation(payoutId: String, input: MarketplacePayoutOperationInput): MarketplacePayoutOperation =
        client.post("/submerchant-payouts/${id(payoutId)}/operations", input, MarketplacePayoutOperation::class.java, V2)

    /** Returns a persisted payout operation receipt. */
    fun getMarketplacePayoutOperation(id: String): MarketplacePayoutOperation =
        client.get("/marketplace/payout-operations/${id(id)}", MarketplacePayoutOperation::class.java, version = V2)

    /** Checks the provider result without repeating the change. */
    fun reconcileMarketplacePayoutOperation(id: String): MarketplacePayoutOperation =
        client.post("/marketplace/payout-operations/${id(id)}/reconcile", null, MarketplacePayoutOperation::class.java, V2)

    /** Repeats an unresolved change only after the previous provider state is verified. */
    fun retryMarketplacePayoutOperation(id: String): MarketplacePayoutOperation =
        client.post("/marketplace/payout-operations/${id(id)}/retry", null, MarketplacePayoutOperation::class.java, V2)

    private fun withResolvedCurrency(input: MarketplaceApprovalPolicyInput): MarketplaceApprovalPolicyInput =
        if (input.currencyId.isNullOrBlank()) input else input.copy(currencyId = currencyResolver.resolveCurrencyId(input.currencyId))

    private fun id(value: String) = SharedApiClient.pathId(value)

    private fun encode(value: String) = java.net.URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

    private companion object {
        const val V2 = "v2"
    }
}

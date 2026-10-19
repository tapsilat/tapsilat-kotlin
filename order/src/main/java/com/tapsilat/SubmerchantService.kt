package com.tapsilat

/**
 * Submerchant management. Currency fields accept a currency UUID or an organization
 * currency unit such as "TRY"; the server resolves units.
 */
class SubmerchantService internal constructor(
    private val client: SharedApiClient,
) {
    /** Creates a submerchant; the response carries only code and message. */
    fun createSubmerchant(payload: SubmerchantCreateRequest): SubmerchantMutationResponse =
        client.post("/submerchants", payload, SubmerchantMutationResponse::class.java)

    fun getSubmerchant(id: String): Submerchant =
        client.get("/submerchants/${SharedApiClient.pathId(id, "id")}", Submerchant::class.java)

    fun listSubmerchants(page: Int, perPage: Int): SubmerchantListResponse =
        client.get(
            "/submerchants",
            SubmerchantListResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    /** Replaces the submerchant profile; send every field. */
    fun updateSubmerchant(id: String, payload: SubmerchantUpdateRequest): SubmerchantMutationResponse =
        client.patch("/submerchants/${SharedApiClient.pathId(id, "id")}", payload, SubmerchantMutationResponse::class.java)

    fun deleteSubmerchant(id: String): SubmerchantMutationResponse =
        client.delete("/submerchants/${SharedApiClient.pathId(id, "id")}", SubmerchantMutationResponse::class.java)

    fun getSuborganizations(page: Int, perPage: Int): SuborganizationListResponse =
        client.get(
            "/organization/suborganizations",
            SuborganizationListResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    @Deprecated("Use getSuborganizationDetail, which decodes every field the endpoint returns", ReplaceWith("getSuborganizationDetail(id)"))
    fun getSuborganization(id: String): SuborganizationListItem =
        client.get("/organization/suborganizations/${SharedApiClient.pathId(id, "id")}", SuborganizationListItem::class.java)

    fun getSuborganizationDetail(id: String): SuborganizationDetail =
        client.get("/organization/suborganizations/${SharedApiClient.pathId(id, "id")}", SuborganizationDetail::class.java)

    fun getSuborganizationBySubmerchant(submerchantId: String): SubmerchantSuborganizationMapping =
        client.get(
            "/submerchants/${SharedApiClient.pathId(submerchantId, "submerchantId")}/suborganization",
            SubmerchantSuborganizationMapping::class.java,
        )

    fun getSubmerchantBySuborganization(suborganizationId: String): SuborganizationSubmerchantMapping =
        client.get(
            "/organization/suborganizations/${SharedApiClient.pathId(suborganizationId, "suborganizationId")}/submerchant",
            SuborganizationSubmerchantMapping::class.java,
        )

    /** Records an item business event; time gates, approval and settlement remain separate. */
    fun recordSubmerchantPayoutEvent(payload: SubmerchantPayoutEventRequest): SubmerchantPayoutEventResponse =
        client.post("/submerchant-payout-events", payload, SubmerchantPayoutEventResponse::class.java)

    /** Creates a logical marketplace seller; inspect each provisioning for the VPOS account's readiness. */
    fun createMarketplaceSubmerchant(payload: MarketplaceSubmerchantCreateRequest): MarketplaceSubmerchantCreateResponse =
        client.post("/submerchants", payload, MarketplaceSubmerchantCreateResponse::class.java, version = "v2")
}

package com.tapsilat

class SubmerchantService internal constructor(
    private val client: SharedApiClient,
    private val currencyResolver: CurrencyReferenceResolver,
) {
    fun createSubmerchant(payload: SubmerchantCreateRequest): SubmerchantMutationResponse {
        val currencyID = currencyResolver.resolveCurrencyId(payload.currencyId)
        return client.post("/submerchants", payload.copy(currencyId = currencyID), SubmerchantMutationResponse::class.java)
    }

    fun getSubmerchant(id: String): Submerchant = client.get("/submerchants/$id", Submerchant::class.java)

    fun listSubmerchants(page: Int, perPage: Int): SubmerchantListResponse =
        client.get(
            "/submerchants",
            SubmerchantListResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun updateSubmerchant(id: String, payload: SubmerchantUpdateRequest): SubmerchantMutationResponse {
        val currencyID = currencyResolver.resolveCurrencyId(payload.currencyId)
        return client.patch(
            "/submerchants/$id",
            payload.copy(currencyId = currencyID),
            SubmerchantMutationResponse::class.java,
        )
    }

    fun deleteSubmerchant(id: String): SubmerchantMutationResponse =
        client.delete("/submerchants/$id", SubmerchantMutationResponse::class.java)

    fun getSuborganizations(page: Int, perPage: Int): SuborganizationListResponse =
        client.get(
            "/organization/suborganizations",
            SuborganizationListResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun getSuborganization(id: String): SuborganizationListItem =
        client.get("/organization/suborganizations/$id", SuborganizationListItem::class.java)

    fun getSuborganizationDetail(id: String): SuborganizationDetail =
        client.get("/organization/suborganizations/$id", SuborganizationDetail::class.java)

    fun getSuborganizationBySubmerchant(submerchantId: String): SubmerchantSuborganizationMapping =
        client.get("/submerchants/$submerchantId/suborganization", SubmerchantSuborganizationMapping::class.java)

    fun getSubmerchantBySuborganization(suborganizationId: String): SuborganizationSubmerchantMapping =
        client.get(
            "/organization/suborganizations/$suborganizationId/submerchant",
            SuborganizationSubmerchantMapping::class.java,
        )
}

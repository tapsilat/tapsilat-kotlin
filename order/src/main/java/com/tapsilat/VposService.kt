package com.tapsilat

class VposService internal constructor(
    private val client: SharedApiClient,
    private val currencyResolver: CurrencyReferenceResolver,
) {
    fun listVpos(page: Int, perPage: Int): VposListResponse = listVposWithFilter(page, perPage, VposListFilter())

    fun listVposWithFilter(page: Int, perPage: Int, filter: VposListFilter): VposListResponse {
        val query = linkedMapOf("page" to page.toString(), "per_page" to perPage.toString())
        if (!filter.suborganizationId.isNullOrBlank()) query["suborganization_id"] = filter.suborganizationId
        return client.get("/vpos", VposListResponse::class.java, query)
    }

    fun createVpos(payload: VposCreateRequest): VposMutationResponse {
        val normalizedCurrencies = currencyResolver.resolveCurrencyIds(payload.currencies)
        return client.post("/vpos", payload.copy(currencies = normalizedCurrencies), VposMutationResponse::class.java)
    }

    fun getVpos(id: String): Vpos = client.get("/vpos/$id", Vpos::class.java)

    fun updateVpos(id: String, payload: VposUpdateRequest): VposMutationResponse {
        val normalizedCurrencies = currencyResolver.resolveCurrencyIds(payload.currencies)
        return client.patch("/vpos/$id", payload.copy(currencies = normalizedCurrencies), VposMutationResponse::class.java)
    }

    fun deleteVpos(id: String): VposMutationResponse = client.delete("/vpos/$id", VposMutationResponse::class.java)

    fun listVposAcquirers(): VposAcquirerListResponse = client.get("/vpos/acquirers", VposAcquirerListResponse::class.java)

    fun listCardSchemes(): CardSchemeListResponse = client.get("/vpos/card-schemes", CardSchemeListResponse::class.java)

    fun listVposAcquirerTemplates(): VposAcquirerTemplateListResponse =
        client.get("/vpos/acquirer-templates", VposAcquirerTemplateListResponse::class.java)

    fun listVposSubmerchants(
        page: Int,
        perPage: Int,
        vposId: String? = null,
        externalReferenceId: String? = null,
    ): VposSubmerchantListResponse {
        val query = linkedMapOf("page" to page.toString(), "per_page" to perPage.toString())
        if (!vposId.isNullOrBlank()) query["vpos_id"] = vposId
        if (!externalReferenceId.isNullOrBlank()) query["external_reference_id"] = externalReferenceId
        return client.get("/vpos-submerchant", VposSubmerchantListResponse::class.java, query)
    }

    fun createVposSubmerchant(payload: VposSubmerchantCreateRequest): VposSubmerchantMutationResponse =
        client.post("/vpos-submerchant", payload, VposSubmerchantMutationResponse::class.java)

    fun getVposSubmerchant(id: String): VposSubmerchant = client.get("/vpos-submerchant/$id", VposSubmerchant::class.java)

    fun updateVposSubmerchant(id: String, payload: VposSubmerchantUpdateRequest): VposSubmerchantMutationResponse =
        client.patch("/vpos-submerchant/$id", payload, VposSubmerchantMutationResponse::class.java)

    fun deleteVposSubmerchant(id: String): VposSubmerchantMutationResponse =
        client.delete("/vpos-submerchant/$id", VposSubmerchantMutationResponse::class.java)
}

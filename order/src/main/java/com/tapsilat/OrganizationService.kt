package com.tapsilat

import java.util.Locale

class OrganizationService internal constructor(
    private val client: SharedApiClient,
) {
    /** Called after a currency is created so cached currency lookups are refreshed. */
    internal var onCurrenciesChanged: () -> Unit = {}

    fun getOrganizationSettings(): OrganizationSettings =
        client.get("/organization/settings", OrganizationSettings::class.java)

    fun getOrganizationCurrencies(): OrganizationCurrenciesResponse =
        client.get("/organization/currencies", OrganizationCurrenciesResponse::class.java)

    fun listOrganizationCurrencyPresets(): OrganizationCurrencyPresetsResponse =
        client.get("/organization/currency-presets", OrganizationCurrencyPresetsResponse::class.java)

    fun createOrganizationCurrency(currencyCode: String): CreateOrganizationCurrencyResponse {
        val response = client.post(
            "/organization/currencies",
            mapOf("currency_code" to currencyCode.trim().uppercase(Locale.ROOT)),
            CreateOrganizationCurrencyResponse::class.java,
        )
        onCurrenciesChanged()
        return response
    }

    /** Creates a user under the authenticated organization. */
    fun createOrganizationUser(payload: OrgCreateUserRequest): OrgCreateUserResponse =
        client.post("/organization/user/create", payload, OrgCreateUserResponse::class.java)

    /** Creates an access token for an organization user; [OrgUserTokenCreateRequest.expire] is in minutes. */
    fun createOrganizationUserToken(payload: OrgUserTokenCreateRequest): OrgUserTokenCreateResponse =
        client.post("/organization/user/token", payload, OrgUserTokenCreateResponse::class.java)
}

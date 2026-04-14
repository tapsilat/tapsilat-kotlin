package com.tapsilat

import java.util.Locale

class OrganizationService internal constructor(
    private val client: SharedApiClient,
) {
    fun getOrganizationSettings(): OrganizationSettings =
        client.get("/organization/settings", OrganizationSettings::class.java)

    fun getOrganizationCurrencies(): OrganizationCurrenciesResponse =
        client.get("/organization/currencies", OrganizationCurrenciesResponse::class.java)

    fun listOrganizationCurrencyPresets(): OrganizationCurrencyPresetsResponse =
        client.get("/organization/currency-presets", OrganizationCurrencyPresetsResponse::class.java)

    fun createOrganizationCurrency(currencyCode: String): CreateOrganizationCurrencyResponse =
        client.post(
            "/organization/currencies",
            mapOf("currency_code" to currencyCode.trim().uppercase(Locale.getDefault())),
            CreateOrganizationCurrencyResponse::class.java,
        )
}

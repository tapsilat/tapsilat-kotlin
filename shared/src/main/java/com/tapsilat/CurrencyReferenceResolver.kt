package com.tapsilat

import java.util.Locale

internal class CurrencyReferenceResolver(
    private val organizationService: OrganizationService,
) {
    private var currencyIdsByUnit: Map<String, String> = emptyMap()
    private var currencyCacheReady: Boolean = false

    fun resolveCurrencyId(ref: String): String {
        val trimmedRef = ref.trim()
        if (trimmedRef.isBlank()) {
            throw ValidationError(message = "currency_id is required")
        }

        if (UUID_REGEX.matches(trimmedRef)) {
            return trimmedRef
        }

        val currencyByUnit = getOrganizationCurrencyIdsByUnit()
        return currencyByUnit[trimmedRef.uppercase(Locale.getDefault())]
            ?: throw ValidationError(
                message = "unknown currency reference \"$ref\"; use a currency UUID or organization currency unit",
            )
    }

    fun resolveCurrencyIds(refs: List<String>): List<String> {
        if (refs.isEmpty()) {
            throw ValidationError(message = "currencies must contain at least one currency reference")
        }

        val normalized = refs.map { resolveCurrencyId(it) }.distinct()
        if (normalized.isEmpty()) {
            throw ValidationError(message = "currencies must contain at least one currency reference")
        }
        return normalized
    }

    @Synchronized
    fun invalidateCache() {
        currencyIdsByUnit = emptyMap()
        currencyCacheReady = false
    }

    @Synchronized
    private fun getOrganizationCurrencyIdsByUnit(): Map<String, String> {
        if (currencyCacheReady) {
            return currencyIdsByUnit
        }

        val response = organizationService.getOrganizationCurrencies()
        val resolved = response.currencies
            .mapNotNull {
                val unit = it.currencyUnit?.trim()?.uppercase(Locale.getDefault())
                val id = it.id?.trim()
                if (unit.isNullOrBlank() || id.isNullOrBlank()) null else unit to id
            }
            .toMap()

        currencyIdsByUnit = resolved
        currencyCacheReady = true
        return resolved
    }

    private companion object {
        val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
    }
}

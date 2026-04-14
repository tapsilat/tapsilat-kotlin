package com.tapsilat

class CreditService internal constructor(
    private val client: SharedApiClient,
) {
    fun createCreditApplicationPlaceholder(): Nothing {
        throw UnsupportedOperationException("Credit module is not implemented yet")
    }
}

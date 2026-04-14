package com.tapsilat

class CardService internal constructor(
    private val client: SharedApiClient,
) {
    fun createCardPlaceholder(): Nothing {
        throw UnsupportedOperationException("Card module is not implemented yet")
    }
}

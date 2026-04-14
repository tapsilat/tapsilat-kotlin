package com.tapsilat

class CheckoutService internal constructor(
    private val client: SharedApiClient,
) {
    fun createCheckoutSessionPlaceholder(): Nothing {
        throw UnsupportedOperationException("Checkout module is not implemented yet")
    }
}

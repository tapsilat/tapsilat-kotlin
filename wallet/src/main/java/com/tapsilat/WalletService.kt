package com.tapsilat

class WalletService internal constructor(
    private val client: SharedApiClient,
) {
    fun createWalletPlaceholder(): Nothing {
        throw UnsupportedOperationException("Wallet module is not implemented yet")
    }
}

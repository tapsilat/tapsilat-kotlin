package com.tapsilat

/** Card tokenization and saved cards. */
class TokenizationService internal constructor(
    private val client: SharedApiClient,
) {
    /** Tokenizes a card; returns 3D secure form details when verification is required. */
    fun tokenizeCard(payload: CardTokenizeRequest): CardTokenizeResponse =
        client.post("/tokenization/card/tokenize", payload, CardTokenizeResponse::class.java)

    fun listSavedCards(page: Int, perPage: Int): ListSavedCardsResponse =
        client.get(
            "/tokenization/card/list",
            ListSavedCardsResponse::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun deleteSavedCard(id: String): DeleteSavedCardResponse =
        client.delete("/tokenization/card/${SharedApiClient.pathId(id, "id")}", DeleteSavedCardResponse::class.java)
}

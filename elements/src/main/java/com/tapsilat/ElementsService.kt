package com.tapsilat

/**
 * Tapsilat Elements sessions for the native Android and iOS card sheet. Call these from your backend with
 * your secret API key, never from an app. Errors arrive as [ApiError] with a stable [ApiError.code], such as
 * `elements_not_enabled` or `session_not_found`, and [ApiError.retryable].
 */
class ElementsService internal constructor(
    private val client: SharedApiClient,
) {
    /**
     * Creates a session with POST /elements/sessions. Hand [ElementsSessionWithSecret.clientSecret] to the
     * app that opens the card sheet; only this call returns it. Do not log or store it.
     */
    fun createElementsSession(payload: ElementsSessionCreateRequest): ElementsSessionWithSecret =
        client.post("/elements/sessions", payload, ElementsSessionWithSecret::class.java)

    /**
     * Returns the session's authoritative state and outcome with GET /elements/sessions/{id}. Read it on your
     * backend before fulfilling, rather than trusting what the app reports.
     */
    fun getElementsSession(id: String): ElementsSession =
        client.get("/elements/sessions/${SharedApiClient.pathId(id)}", ElementsSession::class.java)

    /**
     * Cancels a session that is still waiting for a card with POST /elements/sessions/{id}/cancel. A session
     * that is confirming or has completed comes back unchanged.
     */
    fun cancelElementsSession(id: String): ElementsSession =
        client.postWithoutBody("/elements/sessions/${SharedApiClient.pathId(id)}/cancel", ElementsSession::class.java)
}

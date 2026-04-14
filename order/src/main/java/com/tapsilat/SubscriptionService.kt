package com.tapsilat

class SubscriptionService internal constructor(
    private val client: SharedApiClient,
) {
    fun getSubscription(payload: SubscriptionGetRequest): SubscriptionDetail =
        client.post("/subscription", payload, SubscriptionDetail::class.java)

    fun cancelSubscription(payload: SubscriptionCancelRequest) {
        client.postMap("/subscription/cancel", payload)
    }

    fun createSubscription(payload: SubscriptionCreateRequest): SubscriptionCreateResponse =
        client.post("/subscription/create", payload, SubscriptionCreateResponse::class.java)

    fun listSubscriptions(page: Int, perPage: Int): PaginatedData =
        client.get(
            "/subscription/list",
            PaginatedData::class.java,
            mapOf("page" to page.toString(), "per_page" to perPage.toString()),
        )

    fun redirectSubscription(payload: SubscriptionRedirectRequest): SubscriptionRedirectResponse =
        client.post("/subscription/redirect", payload, SubscriptionRedirectResponse::class.java)
}

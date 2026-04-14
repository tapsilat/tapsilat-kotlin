package com.tapsilat

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyReferenceResolverTest {

    private lateinit var server: MockWebServer
    private lateinit var resolver: CurrencyReferenceResolver

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        val client = SharedApiClient(
            endpoint = server.url("/").toString().trimEnd('/'),
            token = "tok",
            timeout = Duration.ofSeconds(5),
        )
        resolver = CurrencyReferenceResolver(OrganizationService(client))
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueueCurrencies() {
        server.enqueue(
            MockResponse()
                .setBody(
                    """{"currencies":[
                        {"id":"550e8400-e29b-41d4-a716-446655440000","currency_unit":"TRY","code":"TRY","name":"Turkish Lira","symbol":"₺"},
                        {"id":"6ba7b810-9dad-11d1-80b4-00c04fd430c8","currency_unit":"USD","code":"USD","name":"US Dollar","symbol":"$"}
                    ]}""",
                )
                .setResponseCode(200),
        )
    }

    @Test
    fun `UUID passthrough makes no API call`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"

        val result = resolver.resolveCurrencyId(uuid)

        assertEquals(uuid, result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `blank ref throws ValidationError`() {
        assertFailsWith<ValidationError> { resolver.resolveCurrencyId("  ") }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `unit string resolves to UUID`() {
        enqueueCurrencies()

        val result = resolver.resolveCurrencyId("TRY")

        assertEquals("550e8400-e29b-41d4-a716-446655440000", result)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `unit string resolution is case-insensitive`() {
        enqueueCurrencies()

        val result = resolver.resolveCurrencyId("try")

        assertEquals("550e8400-e29b-41d4-a716-446655440000", result)
    }

    @Test
    fun `cache prevents second API round-trip`() {
        enqueueCurrencies()

        resolver.resolveCurrencyId("TRY")
        resolver.resolveCurrencyId("USD")

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `invalidateCache causes re-fetch on next call`() {
        enqueueCurrencies()
        enqueueCurrencies()

        resolver.resolveCurrencyId("TRY")
        resolver.invalidateCache()
        resolver.resolveCurrencyId("TRY")

        assertEquals(2, server.requestCount)
    }

    @Test
    fun `unknown unit throws ValidationError`() {
        enqueueCurrencies()

        assertFailsWith<ValidationError> { resolver.resolveCurrencyId("EUR") }
    }

    @Test
    fun `resolveCurrencyIds with empty list throws`() {
        assertFailsWith<ValidationError> { resolver.resolveCurrencyIds(emptyList()) }
    }

    @Test
    fun `resolveCurrencyIds deduplicates UUIDs`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val result = resolver.resolveCurrencyIds(listOf(uuid, uuid, uuid))

        assertEquals(1, result.size)
        assertEquals(uuid, result[0])
        assertEquals(0, server.requestCount) // all UUIDs → no API call
    }

    @Test
    fun `resolveCurrencyIds resolves mixed UUID and unit`() {
        enqueueCurrencies()

        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val result = resolver.resolveCurrencyIds(listOf(uuid, "USD"))

        assertEquals(2, result.size)
    }
}

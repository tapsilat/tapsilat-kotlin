package com.tapsilat

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: SharedApiClient

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = SharedApiClient(
            endpoint = server.url("/").toString().trimEnd('/'),
            token = "test-token",
            timeout = Duration.ofSeconds(5),
        )
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `GET sends Bearer token header`() {
        server.enqueue(MockResponse().setBody("""{"referenceId":"ref1"}""").setResponseCode(200))

        client.get("/order/ref1", OrderDetail::class.java)

        val req = server.takeRequest()
        assertEquals("Bearer test-token", req.getHeader("Authorization"))
        assertEquals("GET", req.method)
    }

    @Test
    fun `GET appends query parameters to URL`() {
        server.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        client.get("/order/list", PaginatedData::class.java, mapOf("page" to "2", "per_page" to "10"))

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("page=2"), "path should contain page=2")
        assertTrue(req.path!!.contains("per_page=10"), "path should contain per_page=10")
    }

    @Test
    fun `GET skips blank query values`() {
        server.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        client.get("/order/list", PaginatedData::class.java, mapOf("page" to "1", "buyer_id" to ""))

        val req = server.takeRequest()
        assertTrue(!req.path!!.contains("buyer_id"), "blank value should not appear in query string")
    }

    @Test
    fun `POST sends JSON body and correct method`() {
        server.enqueue(MockResponse().setBody("""{"referenceId":"r"}""").setResponseCode(200))

        client.post("/order/create", mapOf("amount" to 100.0), OrderResponse::class.java)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.body.readUtf8().contains("amount"))
    }

    @Test
    fun `PATCH sends correct method`() {
        server.enqueue(MockResponse().setBody("""{"code":0,"message":"ok"}""").setResponseCode(200))

        client.patch("/submerchants/id1", mapOf("name" to "test"), SubmerchantMutationResponse::class.java)

        val req = server.takeRequest()
        assertEquals("PATCH", req.method)
    }

    @Test
    fun `DELETE sends correct method`() {
        server.enqueue(MockResponse().setBody("""{"code":0,"message":"ok"}""").setResponseCode(200))

        client.delete("/submerchants/id1", SubmerchantMutationResponse::class.java)

        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
    }

    @Test
    fun `non-2xx response throws ApiError with correct statusCode`() {
        server.enqueue(
            MockResponse()
                .setBody("""{"code":"ERR_422","message":"validation failed"}""")
                .setResponseCode(422),
        )

        val ex = assertFailsWith<ApiError> {
            client.get("/order/missing", OrderDetail::class.java)
        }

        assertEquals(422, ex.statusCode)
        assertEquals("ERR_422", ex.code)
        assertEquals("validation failed", ex.messageText)
    }

    @Test
    fun `404 response throws ApiError`() {
        server.enqueue(MockResponse().setBody("""{"message":"not found"}""").setResponseCode(404))

        val ex = assertFailsWith<ApiError> {
            client.get("/order/ghost", OrderDetail::class.java)
        }

        assertEquals(404, ex.statusCode)
    }

    @Test
    fun `empty 200 body returns default-constructed object`() {
        server.enqueue(MockResponse().setBody("").setResponseCode(200))

        val result = client.get("/order/empty", OrderDetail::class.java)

        assertNull(result.referenceId)
    }

    @Test
    fun `getMap returns deserialized map`() {
        server.enqueue(MockResponse().setBody("""{"status":"ok","count":5}""").setResponseCode(200))

        val result = client.getMap("/some/path")

        assertEquals("ok", result["status"])
        assertEquals(5, result["count"])
    }

    @Test
    fun `postMap returns deserialized map`() {
        server.enqueue(MockResponse().setBody("""{"reference_id":"ref-x","success":true}""").setResponseCode(200))

        val result = client.postMap("/order/terminate", mapOf("reference_id" to "ref-x"))

        // snake_case deserialization should work in a Map context
        assertTrue(result.containsKey("reference_id") || result.containsKey("referenceId"))
    }

    @Test
    fun `Accept header is always application json`() {
        server.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        client.get("/ping", PaginatedData::class.java)

        val req = server.takeRequest()
        assertEquals("application/json", req.getHeader("Accept"))
    }
}

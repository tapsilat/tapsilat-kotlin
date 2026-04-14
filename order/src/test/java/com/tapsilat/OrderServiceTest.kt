package com.tapsilat

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OrderServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: OrderService

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        val client = SharedApiClient(
            endpoint = server.url("/").toString().trimEnd('/'),
            token = "tok",
            timeout = Duration.ofSeconds(5),
        )
        service = OrderService(client)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `createOrder returns response and enriches checkoutUrl`() {
        // createOrder POST → OrderResponse
        server.enqueue(
            MockResponse()
                .setBody("""{"reference_id":"ref-001","checkout_url":"https://pay.example.com/ref-001"}""")
                .setResponseCode(200),
        )
        // internal getCheckoutUrl GET → OrderDetail
        server.enqueue(
            MockResponse()
                .setBody("""{"reference_id":"ref-001","checkout_url":"https://pay.example.com/ref-001"}""")
                .setResponseCode(200),
        )

        val order = Order(
            amount = 100.0,
            currency = "TRY",
            buyer = OrderBuyer(id = "b1", gsmNumber = "+905551234567"),
        )
        val result = service.createOrder(order)

        assertEquals("ref-001", result.referenceId)
        assertNotNull(result.checkoutUrl)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `createOrder normalizes buyer GSM number`() {
        server.enqueue(MockResponse().setBody("""{"referenceId":"r","checkoutUrl":null}""").setResponseCode(200))

        service.createOrder(Order(buyer = OrderBuyer(gsmNumber = "+90 555 123 4567")))

        val req = server.takeRequest()
        val body = req.body.readUtf8()
        assertTrue(body.contains("+905551234567"), "GSM should be normalized in POST body")
    }

    @Test
    fun `getOrder hits correct endpoint`() {
        server.enqueue(MockResponse().setBody("""{"reference_id":"ref-001","status":3}""").setResponseCode(200))

        val result = service.getOrder("ref-001")

        val req = server.takeRequest()
        assertEquals("/order/ref-001", req.path)
        assertEquals("ref-001", result.referenceId)
        assertEquals(3, result.status)
    }

    @Test
    fun `getOrderByConversationId hits correct endpoint`() {
        server.enqueue(MockResponse().setBody("""{"reference_id":"ref-002"}""").setResponseCode(200))

        service.getOrderByConversationId("conv-xyz")

        val req = server.takeRequest()
        assertEquals("/order/conversation/conv-xyz", req.path)
    }

    @Test
    fun `getOrderList passes all query params`() {
        server.enqueue(MockResponse().setBody("""{"page":1,"perPage":10,"total":0}""").setResponseCode(200))

        service.getOrderList(page = 1, perPage = 10, buyerId = "buyer-1", startDate = "2025-01-01")

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("page=1"))
        assertTrue(req.path!!.contains("per_page=10"))
        assertTrue(req.path!!.contains("buyer_id=buyer-1"))
        assertTrue(req.path!!.contains("start_date=2025-01-01"))
    }

    @Test
    fun `cancelOrder posts to cancel endpoint`() {
        server.enqueue(MockResponse().setBody("""{"status":"success","is_success":true}""").setResponseCode(200))

        val result = service.cancelOrder(CancelOrder(referenceId = "ref-001"))

        val req = server.takeRequest()
        assertEquals("/order/cancel", req.path)
        assertEquals("POST", req.method)
        assertTrue(result.isSuccess == true)
    }

    @Test
    fun `refundOrder posts to refund endpoint`() {
        server.enqueue(MockResponse().setBody("""{"status":"success","is_success":true}""").setResponseCode(200))

        service.refundOrder(RefundOrder(referenceId = "ref-001", amount = 50.0))

        val req = server.takeRequest()
        assertEquals("/order/refund", req.path)
        assertTrue(req.body.readUtf8().contains("ref-001"))
    }

    @Test
    fun `refundAllOrder delegates to refundOrder without amount`() {
        server.enqueue(MockResponse().setBody("""{"status":"success","is_success":true}""").setResponseCode(200))

        service.refundAllOrder("ref-001")

        val req = server.takeRequest()
        assertEquals("/order/refund", req.path)
    }

    @Test
    fun `getOrderStatus hits status endpoint`() {
        server.enqueue(MockResponse().setBody("""{"status":"Paid"}""").setResponseCode(200))

        val result = service.getOrderStatus("ref-001")

        val req = server.takeRequest()
        assertEquals("/order/ref-001/status", req.path)
        assertEquals("Paid", result.status)
    }

    @Test
    fun `orderTerminate posts correct payload`() {
        server.enqueue(MockResponse().setBody("""{"status":"ok"}""").setResponseCode(200))

        service.orderTerminate("ref-001")

        val req = server.takeRequest()
        assertEquals("/order/terminate", req.path)
        assertTrue(req.body.readUtf8().contains("ref-001"))
    }

    @Test
    fun `failed createOrder throws ApiError`() {
        server.enqueue(MockResponse().setBody("""{"code":"ERR","message":"invalid order"}""").setResponseCode(422))

        assertFailsWith<ApiError> {
            service.createOrder(Order())
        }
    }
}

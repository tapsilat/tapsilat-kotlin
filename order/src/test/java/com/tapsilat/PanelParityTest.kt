package com.tapsilat

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PanelParityTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TapsilatApi

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = TapsilatApi.newCustomApi(server.url("/api/v1").toString(), "tok", Duration.ofSeconds(5))
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun respond(body: String = "{}", code: Int = 200, contentType: String = "application/json") {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", contentType).setBody(body))
    }

    private fun next(): RecordedRequest = server.takeRequest()

    @Test
    fun `payment term calls use the panel routes and fields`() {
        repeat(5) { respond("""{"message":"ok","code":200}""") }

        api.getOrderTerm("term ref")
        next().let {
            assertEquals("GET", it.method)
            assertEquals("/api/v1/order/term?term_reference_id=term%20ref", it.path)
        }

        api.createOrderTerm(OrderPaymentTermCreateDTO(orderId = "order-1", amount = 25.0, dueDate = "2026-01-15", required = true))
        next().let {
            assertEquals("POST", it.method)
            assertEquals("/api/v1/order/term", it.path)
            val body = it.body.readUtf8()
            assertTrue(body.contains(""""order_id":"order-1"""") && body.contains(""""due_date":"2026-01-15""""), body)
        }

        api.updateOrderTerm(OrderPaymentTermUpdateDTO(termReferenceId = "term-1", amount = 30.0, status = "pending"))
        next().let {
            assertEquals("PATCH", it.method)
            assertEquals("/api/v1/order/term", it.path)
        }

        api.deleteOrderTerm("order-1", "term-1")
        next().let {
            assertEquals("DELETE", it.method)
            assertEquals("/api/v1/order/term", it.path)
            assertEquals("""{"order_id":"order-1","term_reference_id":"term-1"}""", it.body.readUtf8())
        }

        api.refundOrderTerm(OrderTermRefundRequest(referenceId = "order-ref", termId = "term-id", amount = 10.0))
        next().let {
            assertEquals("/api/v1/order/term/refund", it.path)
            val body = it.body.readUtf8()
            assertTrue(body.contains(""""reference_id":"order-ref"""") && body.contains(""""term_id":"term-id""""), body)
        }
    }

    @Test
    fun `callback related update and payment details use the panel routes`() {
        repeat(4) { respond() }

        api.orderManualCallback("ref-1", "conv-1")
        next().let { assertEquals("POST" to "/api/v1/order/callback", it.method to it.path) }

        api.orderRelatedUpdate("ref-1", "ref-2")
        next().let { assertEquals("PATCH" to "/api/v1/order/releated", it.method to it.path) }

        api.getOrderPaymentDetails(referenceId = "ref-1")
        next().let { assertEquals("GET" to "/api/v1/order/ref-1/payment-details", it.method to it.path) }

        api.getOrderPaymentDetails(conversationId = "conv-1")
        next().let {
            assertEquals("POST" to "/api/v1/order/payment-details", it.method to it.path)
            assertEquals("""{"conversation_id":"conv-1"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `order reads decode the panel shapes`() {
        respond("""[{"id":"tx-1","amount":"10.00"}]""")
        assertEquals("tx-1", api.getOrderTransactions("ref-1").single()["id"])

        respond("null")
        assertTrue(api.getOrderTransactions("ref-1").isEmpty())

        respond("""{"page":1,"total":1,"row":[{"id":"sm-1","submerchant_key":"key-1"}]}""")
        assertEquals("key-1", api.getOrderSubmerchants(1, 10).row.single().submerchantKey)

        respond("""{"payments":[{"id":"p-1","card_brand":"VISA","merchant_net_amount":9.5}],"total_amount":10.0,"order_amount":10.0}""")
        val payments = api.getOrderPayments(GetOrderPaymentsRequest(orderReferenceId = "ref-1"))
        assertEquals(10.0, payments.totalAmount)
        assertEquals(9.5, payments.payments.single().merchantNetAmount)

        respond("""{"reference_id":"ref-1","customer_paid_amount":100.5,"basket_items":[{"id":"bi-1","mcc":"5411","item_payments":[{"id":"ip-1","type":"card"}]}]}""")
        val order = api.getOrder("ref-1")
        assertEquals(100.5, order.customerPaidAmount)
        assertEquals("card", order.basketItems.single().itemPayments.single().type)
        repeat(5) { next() }
    }

    @Test
    fun `charge decodes the camelCase response and installments send their query`() {
        respond("""{"isSuccess":true,"message":"charged","statusCode":"00"}""")
        val charge = api.chargeOrder(OrderChargeRequest(orderReferenceId = "ref-1", retry = true))
        assertEquals(true, charge.isSuccess)
        assertEquals("00", charge.statusCode)
        assertEquals("""{"order_reference_id":"ref-1","retry":true}""", next().body.readUtf8())

        respond("""{"is_installment_available":true,"installments":[{"installment":3,"monthly_payment":"34.83"}]}""")
        val installments = api.getCardInstallments(CardInstallmentsRequest(bin = "54066200", amount = 100.5, currency = "TRY", isThreeD = true))
        assertEquals(true, installments.isInstallmentAvailable)
        assertEquals("/api/v1/card/installments?bin=54066200&amount=100.5&currency=TRY&is_three_d=true", next().path)

        respond("""{"groups":[{"program":"MAXIMUM","rows":[{"installment":2,"is_interest_free":true}]}]}""")
        val table = api.getInstallmentTable(InstallmentTableRequest(amount = 100.0, currency = "TRY"))
        assertEquals(true, table.groups.single().rows.single().isInterestFree)
        assertEquals("/api/v1/card/installment-table?amount=100&currency=TRY", next().path)
    }

    @Test
    fun `submerchant payment actions and organization users use the panel routes`() {
        repeat(5) { respond("""{"operation_id":"op-1","status":"accepted"}""") }
        val action = SubmerchantPaymentAction("tr", "conv", "pt-1", "iyzico")

        @Suppress("DEPRECATION")
        api.approveSubmerchantPayment(action)
        assertEquals("POST" to "/api/v1/submerchants/payment/approve", next().let { it.method to it.path })
        api.disapproveSubmerchantPayment(action)
        assertEquals("/api/v1/submerchants/payment/disapprove", next().path)
        api.updateSubmerchantPaymentItem(SubmerchantPaymentItemUpdate("tr", "conv", "pt-1", "key", "1.00", "iyzico"))
        assertEquals("PUT" to "/api/v1/submerchants/payment/item", next().let { it.method to it.path })
        api.createOrganizationUser(OrgCreateUserRequest(email = "a@b.c"))
        assertEquals("/api/v1/organization/user/create", next().path)
        api.createOrganizationUserToken(OrgUserTokenCreateRequest(userId = "u-1", expire = 60))
        next().let {
            assertEquals("/api/v1/organization/user/token", it.path)
            assertEquals("""{"email":null,"expire":60,"invalidate_old_tokens":null,"user_id":"u-1"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `currency references pass through and creating a currency refreshes the lookup`() {
        respond("""{"code":200,"message":"created"}""")
        api.createSubmerchant(SubmerchantCreateRequest(currencyId = "TRY", submerchantType = "PRIVATE_COMPANY"))
        next().let {
            assertEquals("/api/v1/submerchants", it.path)
            val body = it.body.readUtf8()
            assertTrue(body.contains(""""currency_id":"TRY"""") && body.contains(""""sub_merchant_type":"PRIVATE_COMPANY""""), body)
        }

        respond("""{"code":200}""")
        api.updateVpos("v/1", VposUpdateRequest(name = "POS", currencies = listOf("TRY", "usd"), failover = true, tls = true))
        next().let {
            assertEquals("/api/v1/vpos/v%2F1", it.requestUrl!!.encodedPath)
            val body = it.body.readUtf8()
            assertTrue(body.contains(""""currencies":["TRY","usd"]""") && body.contains(""""failover":true"""), body)
        }

        val currencies = """{"currencies":[{"id":"9f4050e8-1111-4f25-b4ef-aaaaaaaaaaaa","currency_unit":"TRY"}]}"""
        respond(currencies)
        assertEquals("9f4050e8-1111-4f25-b4ef-aaaaaaaaaaaa", api.resolveCurrencyId("try"))
        assertEquals("9f4050e8-1111-4f25-b4ef-aaaaaaaaaaaa", api.resolveCurrencyId("TRY"))
        respond("""{"created":true}""")
        api.createOrganizationCurrency("usd")
        respond(currencies)
        api.resolveCurrencyId("TRY")
        assertEquals(listOf("/api/v1/organization/currencies", "/api/v1/organization/currencies", "/api/v1/organization/currencies"), List(3) { next().path })
    }

    @Test
    fun `buyer birth date is sent as birth_date`() {
        respond("""{"order_id":"o-1"}""")
        api.createOrder(Order(locale = "tr", amount = 10.0, currency = "TRY", buyer = OrderBuyer(birdthDate = "1990-01-01")))
        val body = next().body.readUtf8()
        assertTrue(body.contains(""""birth_date":"1990-01-01"""") && !body.contains("birdth"), body)
    }

    @Test
    fun `marketplace calls use API version 2`() {
        repeat(6) { respond() }
        api.marketplace.listMarketplaceSubmerchants(externalId = "ext")
        assertEquals("/api/v2/submerchants?page=1&per_page=20&external_id=ext", next().path)

        api.marketplace.getMarketplaceSellerContracts(MarketplaceContractFilter("TRY", "PRIVATE_COMPANY", "create", listOf("v-1", "v-2")))
        assertEquals(
            "/api/v2/marketplace/seller-contracts?currency_id=TRY&seller_type=PRIVATE_COMPANY&operation=create&vpos_id=v-1&vpos_id=v-2",
            next().path,
        )

        api.marketplace.updateMarketplaceSubmerchant(
            "s-1",
            MarketplaceProfileUpdate(revision = "2026-01-02T03:04:05.123456789Z", locale = "tr", conversationId = "c", name = "A", email = "a@b.c", gsmNumber = "1"),
        )
        next().let {
            assertEquals("PATCH" to "/api/v2/submerchants/s-1", it.method to it.path)
            assertTrue(it.body.readUtf8().contains(""""revision":"2026-01-02T03:04:05.123456789Z""""))
        }

        api.marketplace.listMarketplacePayouts(MarketplacePayoutFilter(eligibility = "ready", currency = "TRY"))
        assertEquals("/api/v2/submerchant-payouts?currency=TRY&eligibility=ready&page=1&per_page=20", next().path)

        api.marketplace.runMarketplacePayoutOperation("p-1", MarketplacePayoutOperationInput("disapprove", "k", "r"))
        assertEquals("/api/v2/submerchant-payouts/p-1/operations", next().path)

        api.marketplace.reconcileMarketplacePayoutOperation("op-1")
        assertEquals("POST" to "/api/v2/marketplace/payout-operations/op-1/reconcile", next().let { it.method to it.path })
    }

    @Test
    fun `approval policies resolve currency units and deletes accept empty bodies`() {
        respond("""{"currencies":[{"id":"9f4050e8-1111-4f25-b4ef-aaaaaaaaaaaa","currency_unit":"TRY"}]}""")
        respond("""{"status":"created"}""", 201)
        api.marketplace.createMarketplaceApprovalPolicy(MarketplaceApprovalPolicyInput(name = "Manual", priority = 1, mode = "manual", currencyId = "try"))
        assertEquals("/api/v1/organization/currencies", next().path)
        next().let {
            assertEquals("/api/v2/marketplace/approval-policies", it.path)
            assertTrue(it.body.readUtf8().contains(""""currency_id":"9f4050e8-1111-4f25-b4ef-aaaaaaaaaaaa""""))
        }

        server.enqueue(MockResponse().setResponseCode(204))
        api.marketplace.deleteMarketplaceReleasePolicy("rel-1")
        assertEquals("DELETE" to "/api/v2/marketplace/release-policies/rel-1", next().let { it.method to it.path })
    }

    @Test
    fun `tokenization uses the card routes`() {
        repeat(3) { respond() }
        api.tokenizeCard(CardTokenizeRequest("4111111111111111", "J D", "12", "30", "123", userId = "u"))
        assertEquals("/api/v1/tokenization/card/tokenize", next().path)
        api.listSavedCards(1, 20)
        assertEquals("/api/v1/tokenization/card/list?page=1&per_page=20", next().path)
        api.deleteSavedCard("card-1")
        assertEquals("DELETE" to "/api/v1/tokenization/card/card-1", next().let { it.method to it.path })
    }

    @Test
    fun `errors keep field messages and HTML pages become ApiError`() {
        respond("""{"message":"INVALID_REQUEST_BODY","code":400000,"errors":{"email":"REQUIRED"}}""", 400)
        val error = assertFailsWith<ApiError> { api.createSubmerchant(SubmerchantCreateRequest(currencyId = "TRY")) }
        assertEquals("400000", error.code)
        assertEquals("REQUIRED", error.errors["email"])

        respond("<!doctype html><html></html>", 200, "text/html; charset=utf-8")
        val html = assertFailsWith<ApiError> { api.getOrder("ref-1") }
        assertEquals(200, html.statusCode)
        assertTrue(html.messageText!!.contains("non-JSON"))
        assertNull(html.code)
    }

    @Test
    fun `versioned endpoint replaces or appends the api version`() {
        val client = { endpoint: String -> SharedApiClient(endpoint, "tok", Duration.ofSeconds(1)) }
        assertEquals("https://panel.test/api/v2", client("https://panel.test/api/v1").versionedEndpoint("v2"))
        assertEquals("https://panel.test/api/v2", client("https://panel.test/api/v1/").versionedEndpoint("v2"))
        assertEquals("https://panel.test/api/v2", client("https://panel.test").versionedEndpoint("v2"))
        assertEquals("https://panel.test/x/api/v2", client("https://panel.test/x").versionedEndpoint("v2"))
    }
}

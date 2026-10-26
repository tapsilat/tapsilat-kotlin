package com.tapsilat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElementsServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TapsilatApi

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = TapsilatApi.newCustomApi(server.url("/api/v1").toString(), "token_123", Duration.ofSeconds(5))
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun respond(body: String, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setHeader("Cache-Control", "no-store")
                .setBody(body),
        )
    }

    private fun next(): RecordedRequest = server.takeRequest()

    private fun RecordedRequest.jsonBody(): Map<*, *> = ObjectMapper().readValue(body.readUtf8(), Map::class.java)

    @Test
    fun `createElementsSession posts a pay session and decodes the client secret`() {
        respond(
            """
            {
              "id": "es_test_8Kq2nV0rX4",
              "purpose": "pay",
              "status": "requires_payment_method",
              "order_reference_id": "order-ref-1",
              "attempts": 0,
              "max_attempts": 3,
              "created_at": "2026-10-22T09:00:00Z",
              "expires_at": "2026-10-22T09:30:00Z",
              "client_secret": "es_test_8Kq2nV0rX4_secret_q8Zr0bM1",
              "elements_base_url": "https://checkout.tapsilat.dev"
            }
            """.trimIndent(),
        )

        val session = api.createElementsSession(
            ElementsSessionCreateRequest(
                purpose = ElementsSession.PURPOSE_PAY,
                orderReferenceId = "order-ref-1",
                saveCard = true,
            ),
        )

        next().let {
            assertEquals("POST", it.method)
            assertEquals("/api/v1/elements/sessions", it.path)
            assertEquals("Bearer token_123", it.getHeader("Authorization"))
            assertTrue(it.getHeader("Content-Type")!!.startsWith("application/json"))
            assertEquals(mapOf("purpose" to "pay", "order_reference_id" to "order-ref-1", "save_card" to true), it.jsonBody())
        }

        assertEquals("es_test_8Kq2nV0rX4", session.id)
        assertEquals(ElementsSession.PURPOSE_PAY, session.purpose)
        assertEquals(ElementsSession.STATUS_REQUIRES_PAYMENT_METHOD, session.status)
        assertEquals("order-ref-1", session.orderReferenceId)
        assertEquals(0, session.attempts)
        assertEquals(3, session.maxAttempts)
        assertEquals("2026-10-22T09:00:00Z", session.createdAt)
        assertEquals("2026-10-22T09:30:00Z", session.expiresAt)
        assertNull(session.completedAt)
        assertNull(session.card)
        assertNull(session.lastError)
        assertEquals("es_test_8Kq2nV0rX4_secret_q8Zr0bM1", session.clientSecret)
        assertEquals("https://checkout.tapsilat.dev", session.elementsBaseUrl)
    }

    @Test
    fun `createElementsSession sends the tokenize fields`() {
        respond("""{"id":"es_test_1","purpose":"tokenize","status":"requires_payment_method","user_id":"user_123","client_secret":"es_test_1_secret_x"}""")

        val session = api.elements.createElementsSession(
            ElementsSessionCreateRequest(
                purpose = ElementsSession.PURPOSE_TOKENIZE,
                userId = "user_123",
                userEmail = "john@doe.com",
                mode = ElementsSession.MODE_NON_SECURE,
            ),
        )

        assertEquals(
            mapOf("purpose" to "tokenize", "user_id" to "user_123", "user_email" to "john@doe.com", "mode" to 3),
            next().jsonBody(),
        )
        assertEquals("user_123", session.userId)
        assertEquals("es_test_1_secret_x", session.clientSecret)
    }

    @Test
    fun `ElementsSessionWithSecret redacts the client secret in toString but keeps it in JSON`() {
        val session = ElementsSessionWithSecret(
            id = "es_test_1",
            status = ElementsSession.STATUS_REQUIRES_PAYMENT_METHOD,
            clientSecret = "es_test_1_secret_x",
            elementsBaseUrl = "https://checkout.tapsilat.dev",
        )

        for (printed in listOf(session.toString(), "$session", listOf(session).toString())) {
            assertFalse(printed.contains("es_test_1_secret_x"), printed)
            assertTrue(printed.contains("id=es_test_1"), printed)
            assertTrue(printed.contains("clientSecret=[redacted]"), printed)
            assertTrue(printed.contains("elementsBaseUrl=https://checkout.tapsilat.dev"), printed)
        }
        assertTrue(session.copy(clientSecret = null).toString().contains("clientSecret=null"))

        val snakeCase = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()
        val encoded = snakeCase.writeValueAsString(session)
        assertTrue(encoded.contains(""""client_secret":"es_test_1_secret_x""""), encoded)
        assertEquals(session, snakeCase.readValue(encoded, ElementsSessionWithSecret::class.java))
    }

    @Test
    fun `getElementsSession decodes the card and the last error`() {
        respond(
            """
            {
              "id": "es_test_8Kq2nV0rX4",
              "purpose": "pay",
              "status": "failed",
              "order_reference_id": "order-ref-1",
              "attempts": 3,
              "max_attempts": 3,
              "completed_at": "2026-10-22T09:12:00Z",
              "card": {"id": "card-1", "brand": "visa", "last4": "4242", "exp_month": 12, "exp_year": 2030},
              "last_error": {"code": "card_declined", "message": "The card was declined.", "retryable": false}
            }
            """.trimIndent(),
        )

        val session = api.getElementsSession("es_test_8Kq2nV0rX4")

        next().let {
            assertEquals("GET", it.method)
            assertEquals("/api/v1/elements/sessions/es_test_8Kq2nV0rX4", it.path)
            assertEquals("Bearer token_123", it.getHeader("Authorization"))
            assertEquals(0L, it.bodySize)
        }

        assertEquals(ElementsSession.STATUS_FAILED, session.status)
        assertEquals(3, session.attempts)
        assertEquals("2026-10-22T09:12:00Z", session.completedAt)
        assertEquals(ElementsCardSummary(id = "card-1", brand = ElementsCardSummary.BRAND_VISA, last4 = "4242", expMonth = 12, expYear = 2030), session.card)
        assertEquals(ElementsErrorDetail(code = "card_declined", message = "The card was declined.", retryable = false), session.lastError)
    }

    @Test
    fun `getElementsSession reads null card and last error`() {
        respond("""{"id":"es_mock_1","purpose":"pay","status":"requires_payment_method","card":null,"last_error":null}""")

        val session = api.getElementsSession("es_mock_1")

        assertEquals("es_mock_1", session.id)
        assertNull(session.card)
        assertNull(session.lastError)
    }

    @Test
    fun `getElementsSession accepts unknown values and fields`() {
        respond("""{"id":"es_live_1","purpose":"subscribe","status":"on_review","card":{"brand":"mir","network_token":true},"next_action":{"type":"x"}}""")

        val session = api.getElementsSession("es_live_1")

        assertEquals("subscribe", session.purpose)
        assertEquals("on_review", session.status)
        assertEquals("mir", session.card?.brand)
    }

    @Test
    fun `cancelElementsSession posts without a body`() {
        respond("""{"id":"es_test_8Kq2nV0rX4","purpose":"pay","status":"canceled","attempts":0,"max_attempts":3}""")

        val session = api.cancelElementsSession("es_test_8Kq2nV0rX4")

        next().let {
            assertEquals("POST", it.method)
            assertEquals("/api/v1/elements/sessions/es_test_8Kq2nV0rX4/cancel", it.path)
            assertEquals("Bearer token_123", it.getHeader("Authorization"))
            assertEquals(0L, it.bodySize)
            assertNull(it.getHeader("Content-Type"))
        }
        assertEquals(ElementsSession.STATUS_CANCELED, session.status)
    }

    @Test
    fun `session ids are path-escaped`() {
        respond("""{"id":"es_test_1"}""")
        respond("""{"id":"es_test_1"}""")

        api.getElementsSession("es_test_1/../x")
        assertEquals("/api/v1/elements/sessions/es_test_1%2F..%2Fx", next().requestUrl!!.encodedPath)

        api.cancelElementsSession("es_test_1?x")
        assertEquals("/api/v1/elements/sessions/es_test_1%3Fx/cancel", next().requestUrl!!.encodedPath)
    }

    @Test
    fun `a blank session id is rejected before any request`() {
        assertFailsWith<ValidationError> { api.getElementsSession(" ") }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `nested Elements errors surface code, message and retryable on ApiError`() {
        data class Case(val status: Int, val body: String, val code: String, val message: String, val retryable: Boolean, val call: () -> Unit)

        val cases = listOf(
            Case(
                403,
                """{"error":{"code":"elements_not_enabled","message":"Elements is not enabled for this organization"}}""",
                "elements_not_enabled",
                "Elements is not enabled for this organization",
                false,
            ) { api.createElementsSession(ElementsSessionCreateRequest(purpose = ElementsSession.PURPOSE_PAY)) },
            Case(
                404,
                """{"error":{"code":"session_not_found","message":"session not found"}}""",
                "session_not_found",
                "session not found",
                false,
            ) { api.getElementsSession("es_test_missing") },
            Case(
                503,
                """{"error":{"code":"unavailable","message":"try again","retryable":true}}""",
                "unavailable",
                "try again",
                true,
            ) { api.cancelElementsSession("es_test_1") },
        )

        for (case in cases) {
            respond(case.body, case.status)
            val error = assertFailsWith<ApiError> { case.call() }
            assertEquals(case.status, error.statusCode)
            assertEquals(case.code, error.code)
            assertEquals(case.message, error.messageText)
            assertEquals(case.retryable, error.retryable, case.code)
            assertTrue(error.message!!.contains("[${case.code}] ${case.message}"), error.message)
        }
    }
}

package com.tapsilat

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiErrorTest {

    @Test
    fun `from parses code, message, and status from JSON`() {
        val json = JsonNodeFactory.instance.objectNode().apply {
            put("code", "ERR_001")
            put("message", "something failed")
            put("status", "unprocessable")
        }
        val error = ApiError.from(422, "Unprocessable Entity", "raw", json)

        assertEquals(422, error.statusCode)
        assertEquals("unprocessable", error.status)
        assertEquals("ERR_001", error.code)
        assertEquals("something failed", error.messageText)
    }

    @Test
    fun `from treats JSON null nodes as kotlin null not string null`() {
        val json = JsonNodeFactory.instance.objectNode().apply {
            putNull("code")
            putNull("message")
            put("status", "error")
        }
        val error = ApiError.from(500, "Internal Server Error", "raw", json)

        assertNull(error.code)
        assertNull(error.messageText)
    }

    @Test
    fun `from falls back to error field when message is absent`() {
        val json = JsonNodeFactory.instance.objectNode().apply {
            put("error", "fallback error")
        }
        val error = ApiError.from(400, "Bad Request", "raw", json)

        assertEquals("fallback error", error.messageText)
    }

    @Test
    fun `from uses http status when JSON status is absent`() {
        val error = ApiError.from(503, "Service Unavailable", "raw", null)

        assertEquals("Service Unavailable", error.status)
    }

    @Test
    fun `from with null parsed uses raw body in message`() {
        val error = ApiError.from(503, "Service Unavailable", "raw body text", null)

        assertTrue(error.message!!.contains("raw body text"))
    }

    @Test
    fun `message includes status code`() {
        val json = JsonNodeFactory.instance.objectNode().apply {
            put("message", "not found")
        }
        val error = ApiError.from(404, "Not Found", "", json)

        assertTrue(error.message!!.contains("404"))
        assertTrue(error.message!!.contains("not found"))
    }

    @Test
    fun `ValidationError message contains error text`() {
        val ex = ValidationError(message = "phone is required")

        assertTrue(ex.message!!.contains("phone is required"))
        assertEquals(400, ex.statusCode)
    }
}

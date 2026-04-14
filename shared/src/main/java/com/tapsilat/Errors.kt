package com.tapsilat

import com.fasterxml.jackson.databind.JsonNode

class ApiError(
    val statusCode: Int,
    val status: String,
    val code: String?,
    val messageText: String?,
    val rawBody: String,
) : RuntimeException(buildMessage(statusCode, status, code, messageText, rawBody)) {
    companion object {
        fun from(statusCode: Int, status: String, body: String, parsed: JsonNode?): ApiError {
            val code = parsed?.get("code")?.takeUnless { it.isNull }?.asText()
            val message = parsed?.get("message")?.takeUnless { it.isNull }?.asText()
                ?: parsed?.get("error")?.takeUnless { it.isNull }?.asText()
            val parsedStatus = parsed?.get("status")?.takeUnless { it.isNull }?.asText() ?: status
            return ApiError(statusCode, parsedStatus, code, message, body)
        }

        private fun buildMessage(
            statusCode: Int,
            status: String,
            code: String?,
            message: String?,
            rawBody: String,
        ): String {
            return when {
                !code.isNullOrBlank() && !message.isNullOrBlank() -> {
                    "API request failed with status $statusCode ($status): [$code] $message"
                }

                !message.isNullOrBlank() -> {
                    "API request failed with status $statusCode ($status): $message"
                }

                rawBody.isNotBlank() -> {
                    "API request failed with status $statusCode ($status): $rawBody"
                }

                else -> "API request failed with status $statusCode ($status)"
            }
        }
    }
}

class ValidationError(
    val statusCode: Int = 400,
    val code: Int = 0,
    message: String,
) : IllegalArgumentException(
    "Tapsilat Validation Error\nstatus_code:$statusCode\ncode:$code\nerror:$message",
)

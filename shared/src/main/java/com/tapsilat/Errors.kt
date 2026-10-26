package com.tapsilat

import com.fasterxml.jackson.databind.JsonNode

class ApiError @JvmOverloads constructor(
    val statusCode: Int,
    val status: String,
    val code: String?,
    val messageText: String?,
    val rawBody: String,
    /** Per-field validation messages, when the server returns an `errors` object. */
    val errors: Map<String, String> = emptyMap(),
    /** True when the error object says the request can be retried, as Elements errors do. */
    val retryable: Boolean = false,
) : RuntimeException(buildMessage(statusCode, status, code, messageText, rawBody)) {
    companion object {
        fun from(statusCode: Int, status: String, body: String, parsed: JsonNode?): ApiError {
            // Elements routes nest the error: {"error": {"code": "...", "message": "...", "retryable": true}}
            val nested = parsed?.get("error")?.takeIf { it.isObject }
            val code = parsed.text("code") ?: nested.text("code")
            val message = parsed.text("message")
                ?: nested.text("message")
                ?: parsed.text("error")
            val parsedStatus = parsed.text("status") ?: status
            val errors = parsed?.get("errors")
                ?.takeIf { it.isObject }
                ?.properties()
                ?.associate { (field, value) -> field to value.asText() }
                .orEmpty()
            val retryable = nested?.get("retryable")?.takeIf { it.isBoolean }?.booleanValue() ?: false
            return ApiError(statusCode, parsedStatus, code, message, body, errors, retryable)
        }

        /** The field as text when it is a string, number or boolean; otherwise null. */
        private fun JsonNode?.text(field: String): String? =
            this?.get(field)?.takeIf { it.isValueNode && !it.isNull }?.asText()

        internal fun unexpectedResponse(statusCode: Int, status: String, contentType: String, body: String): ApiError =
            ApiError(
                statusCode,
                status,
                null,
                "unexpected non-JSON response (Content-Type \"$contentType\"); check that the endpoint URL and route are correct",
                body,
            )

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

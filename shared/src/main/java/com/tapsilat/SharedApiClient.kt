package com.tapsilat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class SharedApiClient(
    private val endpoint: String,
    private val token: String,
    timeout: Duration,
) {
    private val mapper: ObjectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .callTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .build()

    /** Base URL for another API version: replaces a trailing /api/vN segment, or appends /api/{version}. */
    fun versionedEndpoint(version: String): String {
        val url = endpoint.trimEnd('/').toHttpUrl()
        val segments = url.pathSegments.filter { it.isNotEmpty() }.toMutableList()
        if (segments.size >= 2 && segments[segments.size - 2] == "api" && segments.last().startsWith("v")) {
            segments[segments.size - 1] = version
        } else {
            segments += listOf("api", version)
        }
        val builder = url.newBuilder().encodedPath("/")
        segments.forEach { builder.addPathSegment(it) }
        return builder.build().toString().trimEnd('/')
    }

    fun <T> get(
        path: String,
        responseType: Class<T>,
        query: Map<String, String> = emptyMap(),
        version: String? = null,
    ): T = request(path, "GET", null, query, version) { mapper.readValue(it, responseType) }

    fun <T> post(path: String, payload: Any?, responseType: Class<T>, version: String? = null): T =
        request(path, "POST", payload, version = version) { mapper.readValue(it, responseType) }

    fun <T> put(path: String, payload: Any, responseType: Class<T>): T =
        request(path, "PUT", payload) { mapper.readValue(it, responseType) }

    fun <T> patch(path: String, payload: Any, responseType: Class<T>, version: String? = null): T =
        request(path, "PATCH", payload, version = version) { mapper.readValue(it, responseType) }

    fun <T> delete(path: String, responseType: Class<T>, version: String? = null): T =
        request(path, "DELETE", null, version = version) { mapper.readValue(it, responseType) }

    fun deleteMap(path: String, payload: Any): Map<String, Any?> =
        request(path, "DELETE", payload) { mapper.readValue(it, MAP_TYPE) }

    fun postMap(path: String, payload: Any): Map<String, Any?> =
        request(path, "POST", payload) { mapper.readValue(it, MAP_TYPE) }

    fun patchMap(path: String, payload: Any): Map<String, Any?> =
        request(path, "PATCH", payload) { mapper.readValue(it, MAP_TYPE) }

    fun getMap(path: String, query: Map<String, String> = emptyMap()): Map<String, Any?> =
        request(path, "GET", null, query) { mapper.readValue(it, MAP_TYPE) }

    fun getList(path: String): List<Map<String, Any?>> =
        request(path, "GET", null) { body ->
            if (body.trim() == "{}" || body.trim() == "null") emptyList() else mapper.readValue(body, LIST_TYPE)
        }

    /** Sends a request whose successful response has no body to read, such as a 204 delete. */
    fun send(method: String, path: String, payload: Any? = null, version: String? = null) {
        request(path, method, payload, version = version) { }
    }

    private fun <T> request(
        path: String,
        method: String,
        payload: Any?,
        query: Map<String, String> = emptyMap(),
        version: String? = null,
        deserialize: (String) -> T,
    ): T {
        val req = buildRequest(path, method, payload, query, version)
        http.newCall(req).execute().use { response ->
            val body = response.body.string()
            val parsed = runCatching { mapper.readTree(body) }.getOrNull()
            if (!response.isSuccessful) {
                throw ApiError.from(response.code, response.message, body, parsed)
            }
            val contentType = response.header("Content-Type").orEmpty()
            // A deployment serving the panel UI answers unknown routes with its HTML page and status 200
            if (body.isNotBlank() && (contentType.contains("text/html", ignoreCase = true) || body.trimStart().startsWith("<"))) {
                throw ApiError.unexpectedResponse(response.code, response.message, contentType, body)
            }
            return deserialize(if (body.isBlank()) "{}" else body)
        }
    }

    private fun buildRequest(
        path: String,
        method: String,
        payload: Any?,
        query: Map<String, String>,
        version: String?,
    ): Request {
        val base = if (version == null) endpoint.trimEnd('/') else versionedEndpoint(version)
        val urlBuilder = "$base$path".toHttpUrl().newBuilder()
        query.forEach { (k, v) -> if (v.isNotBlank()) urlBuilder.addQueryParameter(k, v) }

        val jsonBody: RequestBody = payload
            ?.let { mapper.writeValueAsString(it).toRequestBody(JSON_MEDIA_TYPE) }
            ?: EMPTY_JSON_BODY

        return Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .apply {
                when (method) {
                    "POST" -> post(jsonBody)
                    "PUT" -> put(jsonBody)
                    "PATCH" -> patch(jsonBody)
                    "DELETE" -> if (payload != null) delete(jsonBody) else delete()
                    else -> get()
                }
            }
            .build()
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private val EMPTY_JSON_BODY = "{}".toRequestBody(JSON_MEDIA_TYPE)
        private val MAP_TYPE = object : TypeReference<Map<String, Any?>>() {}
        private val LIST_TYPE = object : TypeReference<List<Map<String, Any?>>>() {}

        /** Encodes an identifier for use as one URL path segment, rejecting blank values. */
        fun pathId(value: String, name: String = "id"): String {
            if (value.isBlank()) throw ValidationError(message = "$name is required")
            return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
        }
    }
}

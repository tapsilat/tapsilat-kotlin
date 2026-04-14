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

    fun <T> get(path: String, responseType: Class<T>, query: Map<String, String> = emptyMap()): T =
        request(path, "GET", null, query) { mapper.readValue(it, responseType) }

    fun <T> post(path: String, payload: Any, responseType: Class<T>): T =
        request(path, "POST", payload) { mapper.readValue(it, responseType) }

    fun <T> patch(path: String, payload: Any, responseType: Class<T>): T =
        request(path, "PATCH", payload) { mapper.readValue(it, responseType) }

    fun <T> delete(path: String, responseType: Class<T>): T =
        request(path, "DELETE", null) { mapper.readValue(it, responseType) }

    fun postMap(path: String, payload: Any): Map<String, Any?> =
        request(path, "POST", payload) { mapper.readValue(it, MAP_TYPE) }

    fun getMap(path: String, query: Map<String, String> = emptyMap()): Map<String, Any?> =
        request(path, "GET", null, query) { mapper.readValue(it, MAP_TYPE) }

    private fun <T> request(
        path: String,
        method: String,
        payload: Any?,
        query: Map<String, String> = emptyMap(),
        deserialize: (String) -> T,
    ): T {
        val req = buildRequest(path, method, payload, query)
        http.newCall(req).execute().use { response ->
            val body = response.body.string()
            val parsed = runCatching { mapper.readTree(body) }.getOrNull()
            if (!response.isSuccessful) {
                throw ApiError.from(response.code, response.message, body, parsed)
            }
            return deserialize(if (body.isBlank()) "{}" else body)
        }
    }

    private fun buildRequest(
        path: String,
        method: String,
        payload: Any?,
        query: Map<String, String>,
    ): Request {
        val urlBuilder = "${endpoint.trimEnd('/')}$path".toHttpUrl().newBuilder()
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
                    "PATCH" -> patch(jsonBody)
                    "DELETE" -> delete()
                    else -> get()
                }
            }
            .build()
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
        val EMPTY_JSON_BODY = "{}".toRequestBody(JSON_MEDIA_TYPE)
        val MAP_TYPE = object : TypeReference<Map<String, Any?>>() {}
    }
}

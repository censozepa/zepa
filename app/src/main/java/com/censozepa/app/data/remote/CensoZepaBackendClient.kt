package com.censozepa.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CensoZepaBackendClient {

    suspend fun createUser(
        serverUrl: String,
        email: String,
        googleId: String? = null,
        displayName: String? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanUrl = serverUrl.trimEnd('/') + "/createuser"
                val url = URL(cleanUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val payload = buildJsonObject {
                    put("email", email)
                    put("google_id", googleId ?: "google_auth_$email")
                    put("name", displayName ?: email.substringBefore("@"))
                }.toString()

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(payload)
                    writer.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    Result.success(responseText.ifBlank { "OK" })
                } else {
                    val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    Result.failure(Exception("HTTP $code: ${errorText.ifBlank { "Error al crear usuario" }}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addRegistry(
        serverUrl: String,
        payloadJson: JsonObject
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanUrl = serverUrl.trimEnd('/') + "/addregistry"
                val url = URL(cleanUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(payloadJson.toString())
                    writer.flush()
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    Result.success(responseText.ifBlank { "OK" })
                } else {
                    val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    Result.failure(Exception("HTTP $code: ${errorText.ifBlank { "Error al guardar registros" }}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

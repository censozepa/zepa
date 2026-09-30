package com.censozepa.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CensoZepaBackendClient {

    private const val TAG = "CensoZepaBackend"
    private const val TIMEOUT_MS = 10000

    suspend fun createUser(
        serverUrl: String,
        email: String,
        googleId: String? = null,
        displayName: String? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val payload = buildJsonObject {
                put("email", email)
                put("google_id", googleId ?: "google_auth_$email")
                put("name", displayName ?: email.substringBefore("@"))
            }.toString()

            val cleanBase = serverUrl.trimEnd('/')
            val primaryUrl = "$cleanBase/createuser"
            val fallbackUrl = "$cleanBase/api/createuser"

            Log.d(TAG, "Iniciando createUser para email: $email en $primaryUrl")
            val primaryResult = postJson(primaryUrl, payload)
            if (primaryResult.isSuccess) {
                Log.d(TAG, "createUser exitoso en $primaryUrl")
                return@withContext primaryResult
            }

            if (primaryResult.exceptionOrNull()?.message?.contains("404") == true) {
                Log.d(TAG, "Ruta principal 404. Probando fallback: $fallbackUrl")
                return@withContext postJson(fallbackUrl, payload)
            }

            primaryResult
        }
    }

    suspend fun addRegistry(
        serverUrl: String,
        payloadJson: JsonObject
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val payload = payloadJson.toString()
            val cleanBase = serverUrl.trimEnd('/')
            val primaryUrl = "$cleanBase/addregistry"
            val fallbackUrl = "$cleanBase/api/addregistry"

            Log.d(TAG, "Iniciando addRegistry en $primaryUrl con payload: $payload")
            val primaryResult = postJson(primaryUrl, payload)
            if (primaryResult.isSuccess) {
                Log.d(TAG, "addRegistry exitoso en $primaryUrl")
                return@withContext primaryResult
            }

            if (primaryResult.exceptionOrNull()?.message?.contains("404") == true) {
                Log.d(TAG, "Ruta principal 404. Probando fallback: $fallbackUrl")
                return@withContext postJson(fallbackUrl, payload)
            }

            primaryResult
        }
    }

    private fun postJson(urlString: String, jsonPayload: String): Result<String> {
        return try {
            Log.d(TAG, "POST -> $urlString | Payload: $jsonPayload")
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val code = conn.responseCode
            Log.d(TAG, "Respuesta HTTP Code: $code de $urlString")
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                Log.d(TAG, "Respuesta HTTP Body: $responseText")
                Result.success(responseText.ifBlank { "OK" })
            } else {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorText = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                Log.e(TAG, "Error HTTP $code: $errorText")
                val msg = when (code) {
                    404 -> "HTTP 404: Ruta o recurso no encontrado ($urlString)"
                    400 -> "HTTP 400: Petición incorrecta ($errorText)"
                    500 -> "HTTP 500: Error interno del servidor ($errorText)"
                    else -> "HTTP $code: ${errorText.ifBlank { "Error de servidor" }}"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción de red conectando a $urlString: ${e.message}", e)
            Result.failure(Exception("Error de conexión a $urlString: ${e.localizedMessage ?: e.message}"))
        }
    }
}

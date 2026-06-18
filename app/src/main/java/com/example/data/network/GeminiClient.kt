package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "ERROR_API_KEY"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        try {
            val requestJson = JSONObject()
            
            // Build contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // systemInstruction (if present)
            if (!systemInstruction.isNullOrBlank()) {
                val systemObj = JSONObject()
                val systemParts = JSONArray()
                val systemPart = JSONObject()
                systemPart.put("text", systemInstruction)
                systemParts.put(systemPart)
                systemObj.put("parts", systemParts)
                requestJson.put("systemInstruction", systemObj)
            }

            // generationConfig
            val configObj = JSONObject()
            configObj.put("temperature", 0.7)
            requestJson.put("generationConfig", configObj)

            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    val errMsg = "HTTP ${response.code}: ${response.message}\n$responseBody"
                    Log.e("GeminiClient", errMsg)
                    return@withContext "Gagal memproses. Detail API: HTTP ${response.code}\n$responseBody"
                }

                if (responseBody.isNullOrEmpty()) {
                    return@withContext "Respon kosong dari AI."
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
                return@withContext "AI tidak menghasilkan draf dialog yang valid dalam bentuk teks."
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Exception during Gemini Call", e)
            return@withContext "Koneksi gagal: ${e.localizedMessage}"
        }
    }
}

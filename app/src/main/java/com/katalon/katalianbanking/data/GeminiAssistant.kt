package com.katalon.katalianbanking.data

import com.katalon.katalianbanking.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Mirrors components/common/AiAssistant.tsx: sends the full in-memory "database"
 * (all users/accounts) plus the user's natural-language query to a Gemini model
 * and returns a formatted answer. Requires GEMINI_API_KEY to be configured
 * (see app/build.gradle.kts / local.properties) - falls back to a canned
 * offline response otherwise, same spirit as the web app failing gracefully
 * when no API key is present.
 */
object GeminiAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun ask(query: String, allUsers: List<User>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext offlineFallback(query, allUsers)
        }

        try {
            val prompt = buildPrompt(query, allUsers)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

            val body = buildJsonObject {
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject { put("text", prompt) }
                        }
                    }
                }
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: Unable to connect to the financial intelligence engine (HTTP ${response.code})."
                }
                val text = response.body?.string().orEmpty()
                val json = Json.parseToJsonElement(text).jsonObject
                json["candidates"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("content")?.jsonObject?.get("parts")
                    ?.jsonArray?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
                    ?: "I'm sorry, I couldn't process that query."
            }
        } catch (e: Exception) {
            "Error: Unable to connect to the financial intelligence engine."
        }
    }

    private fun buildPrompt(query: String, allUsers: List<User>): String {
        return """
            You are a senior financial analyst and database assistant for Katalian Bank.
            Below is the current state of the bank's user database in JSON format.

            DATABASE:
            ${allUsers.joinToString(prefix = "[", postfix = "]") { u ->
                "{\"username\":\"${u.username}\",\"accounts\":${u.accounts.size},\"totalBalance\":${u.accounts.sumOf { it.balance }}}"
            }}

            USER QUERY:
            "$query"

            INSTRUCTIONS:
            - Answer the user's query accurately based on the provided data.
            - Use professional, helpful banking tone.
            - If asked about balances, format them as currency.
            - If the information is not in the data, state it politely.
        """.trimIndent()
    }

    /** Simple offline heuristic mirroring the web app's tone when no live model is reachable. */
    private fun offlineFallback(query: String, allUsers: List<User>): String {
        val lower = query.lowercase()
        return when {
            "balance" in lower || "total" in lower -> {
                val total = allUsers.sumOf { u -> u.accounts.sumOf { it.balance } }
                "Total assets across all Katalian accounts currently on file: $%,.2f".format(total)
            }
            "user" in lower || "account" in lower -> {
                "There are ${allUsers.size} user profile(s) and ${allUsers.sumOf { it.accounts.size }} accounts on file."
            }
            else -> "I have noted your inquiry. Configure GEMINI_API_KEY to enable live financial intelligence responses."
        }
    }
}

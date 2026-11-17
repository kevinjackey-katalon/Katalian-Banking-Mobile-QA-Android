package com.katalon.katalianbanking.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiAssistantTest {

    private fun user(name: String, vararg balances: Double) = User(
        id = "u-$name",
        username = name,
        passwordHash = "secret-never-sent",
        accounts = balances.mapIndexed { i, balance ->
            Account(id = "a$i", type = AccountType.Checking, accountNumber = "000$i", balance = balance)
        }
    )

    @Test
    fun database_json_summarises_each_user() {
        val json = GeminiAssistant.buildDatabaseJson(listOf(user("alice", 100.0, 50.5)))
        val first = json.jsonArray[0].jsonObject
        assertEquals("alice", first["username"]!!.jsonPrimitive.content)
        assertEquals(2, first["accounts"]!!.jsonPrimitive.int)
        assertEquals(150.5, first["totalBalance"]!!.jsonPrimitive.double, 0.0001)
    }

    @Test
    fun database_json_stays_valid_when_a_username_contains_quotes() {
        val tricky = "bob\",\"x\":\"injected"
        val text = GeminiAssistant.buildDatabaseJson(listOf(user(tricky))).toString()
        val parsed = Json.parseToJsonElement(text).jsonArray
        assertEquals(1, parsed.size)
        assertEquals(tricky, parsed[0].jsonObject["username"]!!.jsonPrimitive.content)
        assertFalse(parsed[0].jsonObject.containsKey("x"))
    }

    @Test
    fun prompt_never_includes_credentials() {
        val prompt = GeminiAssistant.buildPrompt("total?", listOf(user("alice", 1.0)))
        assertFalse(prompt.contains("secret-never-sent"))
    }

    @Test
    fun prompt_encodes_the_query_as_a_json_string() {
        val prompt = GeminiAssistant.buildPrompt("hi \"quoted\"\nnewline", emptyList())
        assertTrue(prompt.contains("\"hi \\\"quoted\\\"\\nnewline\""))
    }
}

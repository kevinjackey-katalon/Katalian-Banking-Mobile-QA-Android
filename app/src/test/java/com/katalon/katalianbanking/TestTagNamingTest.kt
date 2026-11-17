package com.katalon.katalianbanking

import com.katalon.katalianbanking.ui.components.toSnakeCase
import org.junit.Assert.assertEquals
import org.junit.Test

/** The enum-name -> tag conversion feeds locators such as apply_credit_card, so pin it. */
class TestTagNamingTest {
    @Test
    fun enumNamesBecomeSnakeCase() {
        assertEquals("checking", "Checking".toSnakeCase())
        assertEquals("savings", "Savings".toSnakeCase())
        assertEquals("credit_card", "CreditCard".toSnakeCase())
        assertEquals("platinum_credit_card", "PlatinumCreditCard".toSnakeCase())
        assertEquals("personal", "Personal".toSnakeCase())
        assertEquals("mortgage", "Mortgage".toSnakeCase())
    }
}

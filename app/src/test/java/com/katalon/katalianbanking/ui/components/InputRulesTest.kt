package com.katalon.katalianbanking.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputRulesTest {

    @Test
    fun decimal_allows_digits_and_up_to_two_decimal_places() {
        assertTrue(KInputType.Decimal.accepts("", null))
        assertTrue(KInputType.Decimal.accepts("250", null))
        assertTrue(KInputType.Decimal.accepts("250.", null))
        assertTrue(KInputType.Decimal.accepts("250.5", null))
        assertTrue(KInputType.Decimal.accepts("250.55", null))
    }

    @Test
    fun decimal_rejects_letters_signs_extra_separators_and_third_decimal_place() {
        assertFalse(KInputType.Decimal.accepts("250.555", null))
        assertFalse(KInputType.Decimal.accepts("abc", null))
        assertFalse(KInputType.Decimal.accepts("-5", null))
        assertFalse(KInputType.Decimal.accepts("1.2.3", null))
        assertFalse(KInputType.Decimal.accepts("1,5", null))
    }

    @Test
    fun digits_accepts_only_digits_and_honours_max_length() {
        assertTrue(KInputType.Digits.accepts("12345", 5))
        assertFalse(KInputType.Digits.accepts("123456", 5))
        assertFalse(KInputType.Digits.accepts("12a45", 5))
    }

    @Test
    fun date_accepts_digits_and_dashes_up_to_ten_characters() {
        assertTrue(KInputType.Date.accepts("2024-01-31", null))
        assertFalse(KInputType.Date.accepts("2024-01-311", null))
        assertFalse(KInputType.Date.accepts("2024/01/31", null))
    }

    @Test
    fun free_text_kinds_accept_anything() {
        assertTrue(KInputType.Text.accepts("Any text! with symbols #1", null))
        assertTrue(KInputType.Password.accepts("p@ss w0rd", null))
    }

    @Test
    fun test_tags_are_stable_lowercase_identifiers() {
        assertEquals("access_code", "Access Code".toTestTag())
        assertEquals("date_of_birth_yyyy_mm_dd", "Date of Birth (YYYY-MM-DD)".toTestTag())
        assertEquals("funding_amount", "  Funding Amount (\$)  ".toTestTag())
        assertEquals("", "".toTestTag())
    }
}

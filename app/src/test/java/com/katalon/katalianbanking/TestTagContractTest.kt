package com.katalon.katalianbanking

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Locks the set of automation test tags. Every Modifier.testTag, testTag = "..." and
 * kDropdownTrigger("...") in the app is exposed to Appium / UiAutomator2 / Katalon as a `resource-id`, so renaming or
 * removing one silently breaks existing test objects.
 *
 * The approved list lives in src/test/resources/test-tags.txt. If you change a tag ON
 * PURPOSE, update that file in the same commit so the change is visible in review.
 *
 * Tags built at runtime from a variable (for example the "<field>_visibility_toggle"
 * suffix) are derived from the tags listed here and are not scanned.
 */
class TestTagContractTest {

    // testTag = "a"  |  testTag = if (cond) "a" else "b"  |  testTag = if (c1) "a" else if (c2) "b" else "c"
    private val tagExpression = Regex(
        """testTag\s*=\s*((?:if\s*\([^)]*\)\s*)?"[^"$]*"(?:\s*else\s*(?:if\s*\([^)]*\)\s*)?"[^"$]*")*)"""
    )
    private val modifierTag = Regex("""\.testTag\(\s*"([^"$]*)"\s*\)""")
    private val dropdownTag = Regex("""kDropdownTrigger\(\s*"([^"$]*)"""")
    private val stringLiteral = Regex("\"([^\"$]*)\"")

    private fun sourceRoot(): File =
        listOf("src/main/java", "app/src/main/java").map(::File).firstOrNull { it.isDirectory }
            ?: error("Cannot find src/main/java from ${File(".").absolutePath}")

    private fun actualTags(): Set<String> {
        val tags = sortedSetOf<String>()
        sourceRoot().walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            tagExpression.findAll(text).forEach { m ->
                stringLiteral.findAll(m.groupValues[1]).forEach { tags += it.groupValues[1] }
            }
            modifierTag.findAll(text).forEach { tags += it.groupValues[1] }
            dropdownTag.findAll(text).forEach { tags += it.groupValues[1] }
        }
        return tags
    }

    private fun approvedTags(): Set<String> {
        val file = listOf("src/test/resources/test-tags.txt", "app/src/test/resources/test-tags.txt")
            .map(::File).firstOrNull { it.isFile } ?: error("test-tags.txt not found")
        return file.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSortedSet()
    }

    @Test
    fun everyTestTagMatchesTheApprovedList() {
        val actual = actualTags()
        val approved = approvedTags()
        val missing = approved - actual
        val unapproved = actual - approved
        assertTrue(
            "Test tags changed.\n" +
                "Removed or renamed (existing locators will break): $missing\n" +
                "New and not yet approved: $unapproved\n" +
                "If intentional, update app/src/test/resources/test-tags.txt.",
            missing.isEmpty() && unapproved.isEmpty()
        )
    }
}

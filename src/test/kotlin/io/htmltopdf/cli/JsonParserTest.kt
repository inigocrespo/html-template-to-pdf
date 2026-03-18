package io.htmltopdf.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

// Test Budget: 2 behaviors x 2 = 4 max unit tests
// Behaviors:
//   1. returns a map with all top-level string key-value pairs
//   2. ignores nested objects and non-string values without throwing
//      (edge case: empty/null input returns empty map — parametrized with behavior 2)

class JsonParserTest : FunSpec({

    test("returns a map with all top-level string key-value pairs") {
        val json = """{"name":"Alice","city":"Paris"}"""

        val result = JsonParser.parse(json)

        result shouldBe mapOf("name" to "Alice", "city" to "Paris")
    }

    test("ignores nested objects and non-string values without throwing") {
        val json = """{"name":"Bob","age":30,"address":{"street":"Main St"},"active":true}"""

        val result = JsonParser.parse(json)

        result shouldBe mapOf("name" to "Bob")
    }

    test("returns empty map for empty input") {
        val result = JsonParser.parse("")

        result.shouldBeEmpty()
    }
})

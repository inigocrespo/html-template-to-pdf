package io.htmltopdf

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// Test Budget: 5 behaviors x 2 = 10 unit tests max
// B1: single token replaced
// B2: multiple tokens replaced
// B3: missing key throws MissingVariableError (key, template, fail-fast)
// B4: no tokens returns template unchanged
// B5: extra keys in map are silently ignored
class TemplateEngineTest : FunSpec({

    // B1: single token replaced
    test("single token is replaced with its value") {
        TemplateEngine.resolve("<p>{{name}}</p>", mapOf("name" to "Alice")) shouldBe "<p>Alice</p>"
    }

    // B2: multiple tokens replaced
    test("multiple tokens are all replaced with their values") {
        TemplateEngine.resolve(
            "<p>{{firstName}} {{lastName}}</p>",
            mapOf("firstName" to "John", "lastName" to "Doe")
        ) shouldBe "<p>John Doe</p>"
    }

    // B3: missing key throws MissingVariableError — correct key and template reported
    test("missing key throws MissingVariableError with the missing key and original template") {
        val template = "<p>{{name}}</p>"
        val error = shouldThrow<MissingVariableError> {
            TemplateEngine.resolve(template, emptyMap())
        }
        error.key shouldBe "name"
        error.template shouldBe template
    }

    // B3: fail-fast — validation throws before any partial replacement
    test("missing key throws before any replacement occurs when template has multiple tokens") {
        val template = "{{present}} {{missing}}"
        val error = shouldThrow<MissingVariableError> {
            TemplateEngine.resolve(template, mapOf("present" to "value"))
        }
        error.key shouldBe "missing"
    }

    // B4: no tokens returns template unchanged
    test("template with no tokens and empty map returns template unchanged") {
        val template = "<p>Hello World</p>"
        TemplateEngine.resolve(template, emptyMap()) shouldBe template
    }

    // B5: extra keys in map are silently ignored
    test("extra keys in map that have no matching token are silently ignored") {
        TemplateEngine.resolve(
            "<p>{{name}}</p>",
            mapOf("name" to "Alice", "unused" to "ignored")
        ) shouldBe "<p>Alice</p>"
    }

    // B4 (empty token variant): empty token {{}} is not matched and passes through unchanged
    test("template with empty token {{}} is returned unchanged") {
        TemplateEngine.resolve("<p>{{}}</p>", emptyMap()) shouldBe "<p>{{}}</p>"
    }
})

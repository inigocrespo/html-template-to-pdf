package io.htmltopdf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class MissingVariableErrorTest : FunSpec({

    // Test budget: 4 distinct behaviors x 2 = 8 max unit tests
    // Behaviors: (1) extends RuntimeException, (2) key property, (3) template property, (4) message content
    // Using 4 tests — within budget

    val template = "<html><body><p>Hello {{name}}</p></body></html>"
    val error = MissingVariableError(key = "name", template = template)

    test("MissingVariableError is a RuntimeException") {
        error.shouldBeInstanceOf<RuntimeException>()
    }

    test("key property exposes the missing placeholder name") {
        error.key shouldBe "name"
    }

    test("template property exposes the original template string") {
        error.template shouldBe template
    }

    test("message contains the key name and indicates it is missing") {
        error.message shouldContain "name"
        error.message shouldContain "missing"
    }
})

package io.htmltopdf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class MissingVariableErrorTest : FunSpec({

    // Test budget: 2 distinct behaviors x 2 = 4 max unit tests
    // Behaviors: (1) template property, (2) message content
    // Using 2 tests — within budget

    val template = "<html><body><p>Hello {{name}}</p></body></html>"
    val subject = MissingVariableError(key = "name", template = template)

    test("template property exposes the original template string") {
        subject.template shouldBe template
    }

    test("message contains the key name and indicates it is missing") {
        subject.message shouldContain "name"
        subject.message shouldContain "missing"
    }
})

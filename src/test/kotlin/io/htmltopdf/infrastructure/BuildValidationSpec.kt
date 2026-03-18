package io.htmltopdf.infrastructure

import io.htmltopdf.MissingVariableError
import io.htmltopdf.PdfRenderer
import io.htmltopdf.htmlToPdf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class BuildValidationSpec : DescribeSpec({

    describe("library JAR") {

        it("build.gradle.kts exists and declares the openpdf-html dependency") {
            val buildFile = File("build.gradle.kts")
            buildFile.exists() shouldBe true
            val content = buildFile.readText()
            content shouldContain "openpdf"
        }

        it("htmlToPdf function is accessible from the public API") {
            // Calling htmlToPdf with a plain HTML string confirms it is resolvable
            // at compile time and reachable at runtime via the public package surface.
            val result = htmlToPdf("<html><body><p>Build validation</p></body></html>")
            result shouldNotBe null
        }

        it("MissingVariableError is a RuntimeException subclass") {
            val isRuntimeException = RuntimeException::class.java
                .isAssignableFrom(MissingVariableError::class.java)
            isRuntimeException shouldBe true
        }

        it("PdfRenderer is an interface") {
            PdfRenderer::class.java.isInterface shouldBe true
        }
    }
})

package io.htmltopdf

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf

@Tags("acceptance")
class HtmlToPdfSpec : BehaviorSpec({

    // ---------------------------------------------------------------------------
    // STORY-01: HTML string to PDF stream
    // ---------------------------------------------------------------------------

    given("valid HTML input") {

        `when`("htmlToPdf is called with plain HTML") {
            val result = htmlToPdf("<html><body><p>Receipt for order 5577</p></body></html>")

            then("stream is non-null") {
                result shouldNotBe null
            }

            then("stream starts with PDF magic bytes %PDF") {
                val magicBytes = result.readNBytes(4)
                magicBytes shouldBe "%PDF".toByteArray()
            }
        }

        `when`("htmlToPdf is called with HTML and a data map") {
            val result = htmlToPdf(
                "<html><body><h1>Hello {{name}}</h1></body></html>",
                mapOf("name" to "Maria Santos")
            )

            then("stream is non-null") {
                result shouldNotBe null
            }

            then("stream starts with PDF magic bytes %PDF") {
                val magicBytes = result.readNBytes(4)
                magicBytes shouldBe "%PDF".toByteArray()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-02: Variable injection
    // ---------------------------------------------------------------------------

    given("HTML template with a {{name}} placeholder") {

        `when`("data map contains the key") {
            val template = "<html><body><p>Hello {{name}}</p></body></html>"
            val data = mapOf("name" to "Maria Santos")

            then("the resolved template contains the substituted value") {
                val resolvedHtml = TemplateEngine.resolve(template, data)
                resolvedHtml shouldContain "Maria Santos"
                resolvedHtml shouldNotContain "{{name}}"
            }

            then("htmlToPdf returns an InputStream starting with PDF magic bytes") {
                val result = htmlToPdf(template, data)
                result.readNBytes(4) shouldBe "%PDF".toByteArray()
            }
        }

        `when`("data map contains multiple keys for multiple placeholders") {
            val template = "<html><body><p>Invoice for {{clientName}}</p><p>Amount: {{amount}}</p><p>Due: {{dueDate}}</p></body></html>"
            val data = mapOf(
                "clientName" to "Acme Corp",
                "amount" to "\$1,500.00",
                "dueDate" to "2026-04-01"
            )

            then("the resolved template contains all substituted values") {
                val resolvedHtml = TemplateEngine.resolve(template, data)
                resolvedHtml shouldContain "Acme Corp"
                resolvedHtml shouldContain "\$1,500.00"
                resolvedHtml shouldContain "2026-04-01"
                resolvedHtml shouldNotContain "{{clientName}}"
                resolvedHtml shouldNotContain "{{amount}}"
                resolvedHtml shouldNotContain "{{dueDate}}"
            }

            then("htmlToPdf returns an InputStream starting with PDF magic bytes") {
                val result = htmlToPdf(template, data)
                result.readNBytes(4) shouldBe "%PDF".toByteArray()
            }
        }

        `when`("the same placeholder appears multiple times in the template") {
            val template = "<html><body><h1>{{name}}</h1><p>Dear {{name}},</p><footer>From {{name}}</footer></body></html>"
            val data = mapOf("name" to "Carlos")

            then("the resolved template replaces all occurrences of the placeholder") {
                val resolvedHtml = TemplateEngine.resolve(template, data)
                resolvedHtml shouldNotContain "{{name}}"
                resolvedHtml.split("Carlos").size shouldBe 4 // 3 replacements → 4 parts when split
            }

            then("htmlToPdf returns an InputStream starting with PDF magic bytes") {
                val result = htmlToPdf(template, data)
                result.readNBytes(4) shouldBe "%PDF".toByteArray()
            }
        }

        `when`("data map has extra keys not present in the template") {
            then("no error is thrown and a valid PDF is returned") {
                val result = htmlToPdf(
                    "<html><body><p>Hello {{name}}</p></body></html>",
                    mapOf("name" to "Maria", "unused" to "ignored", "alsoUnused" to "stillIgnored")
                )
                result shouldNotBe null
                result.readNBytes(4) shouldBe "%PDF".toByteArray()
            }
        }

        `when`("the data parameter is omitted and the template has no placeholders") {
            then("no error is thrown and a valid PDF is returned") {
                val result = htmlToPdf("<html><body><p>Static content only</p></body></html>")
                result shouldNotBe null
                result.readNBytes(4) shouldBe "%PDF".toByteArray()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-03: Invalid input
    // ---------------------------------------------------------------------------

    given("invalid html input") {

        `when`("html is a blank string containing only whitespace") {
            then("IllegalArgumentException is thrown with a message referencing html") {
                val exception = shouldThrow<IllegalArgumentException> {
                    htmlToPdf("   ")
                }
                exception.message shouldContain "html"
            }
        }

        `when`("html is an empty string") {
            then("IllegalArgumentException is thrown") {
                shouldThrow<IllegalArgumentException> {
                    htmlToPdf("")
                }
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-04: Missing variable
    // ---------------------------------------------------------------------------

    given("HTML template with a missing variable") {

        `when`("data map does not contain the key referenced in the template") {
            then("MissingVariableError is thrown") {
                shouldThrow<MissingVariableError> {
                    htmlToPdf(
                        "<html><body><h1>Hello {{name}}</h1></body></html>",
                        emptyMap()
                    )
                }
            }
        }

        `when`("MissingVariableError is thrown for a single missing key") {
            val template = "<html><body><h1>Hello {{name}}</h1></body></html>"
            val exception = shouldThrow<MissingVariableError> {
                htmlToPdf(template, emptyMap())
            }

            then("the key field contains the missing key name") {
                exception.key shouldBe "name"
            }

            then("the template field contains the original template string") {
                exception.template shouldBe template
            }
        }

        `when`("template has multiple placeholders and one key is missing") {
            val template = "<html><body><p>{{clientName}} owes {{amount}}</p></body></html>"

            then("MissingVariableError is thrown with the first missing key identified") {
                val exception = shouldThrow<MissingVariableError> {
                    htmlToPdf(template, mapOf("clientName" to "Acme Corp"))
                }
                exception.key shouldBe "amount"
                exception.template shouldBe template
            }
        }

        `when`("MissingVariableError is thrown") {
            then("MissingVariableError is a RuntimeException") {
                val exception = shouldThrow<MissingVariableError> {
                    htmlToPdf(
                        "<html><body>{{invoiceNumber}}</body></html>",
                        emptyMap()
                    )
                }
                exception.shouldBeInstanceOf<RuntimeException>()
            }
        }
    }
})

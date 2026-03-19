package io.htmltopdf.cli

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldBeEmpty
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files

@Tags("cli-acceptance")
class CliSpec : BehaviorSpec({

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    fun captureStderr(block: () -> Int): Pair<Int, String> {
        val errBuffer = ByteArrayOutputStream()
        val capturedErr = PrintStream(errBuffer)
        val originalErr = System.err
        System.setErr(capturedErr)
        val exitCode: Int
        try {
            exitCode = block()
        } finally {
            System.setErr(originalErr)
        }
        return exitCode to errBuffer.toString()
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-01: Convert plain HTML file to PDF (no --data)
    // Walking skeleton — enabled first, all others skipped
    // ---------------------------------------------------------------------------

    given("a valid HTML file with no template variables") {
        val htmlFile = Files.createTempFile("cli-spec-plain", ".html").toFile().also {
            it.writeText("<html><body><h1>Invoice #1001</h1><p>Total: \$240.00</p></body></html>")
            it.deleteOnExit()
        }
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with --input and --output") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf("--input", htmlFile.absolutePath, "--output", outputFile.absolutePath))
            }

            then("exit code is 0") {
                exitCode shouldBe 0
            }

            then("output file exists on disk") {
                outputFile.exists() shouldBe true
            }

            then("output file starts with PDF magic bytes") {
                outputFile.readBytes().take(4) shouldBe "%PDF".toByteArray().toList()
            }

            then("stderr is empty") {
                stderr.shouldBeEmpty()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-02: Convert HTML template with JSON data to PDF
    // ---------------------------------------------------------------------------

    given("a valid HTML template file and a matching JSON data file") {
        val htmlFile = Files.createTempFile("cli-spec-template", ".html").toFile().also {
            it.writeText("<html><body><p>Hello {{name}}, your total is {{amount}}.</p></body></html>")
            it.deleteOnExit()
        }
        val dataFile = Files.createTempFile("cli-spec-data", ".json").toFile().also {
            it.writeText("""{"name": "Alice", "amount": "${'$'}99.00"}""")
            it.deleteOnExit()
        }
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with --input, --data, and --output") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf(
                    "--input", htmlFile.absolutePath,
                    "--data", dataFile.absolutePath,
                    "--output", outputFile.absolutePath
                ))
            }

            then("exit code is 0") {
                exitCode shouldBe 0
            }

            then("output PDF exists on disk") {
                outputFile.exists() shouldBe true
            }

            then("output file starts with PDF magic bytes") {
                outputFile.readBytes().take(4) shouldBe "%PDF".toByteArray().toList()
            }

            then("stderr is empty") {
                stderr.shouldBeEmpty()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-03: Missing --input flag
    // ---------------------------------------------------------------------------

    given("the --input flag is missing") {
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with only --output") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf("--output", outputFile.absolutePath))
            }

            then("exit code is 1") {
                exitCode shouldBe 1
            }

            then("stderr reports that --input is required") {
                stderr shouldContain "Error: --input is required"
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-03: Missing --output flag
    // ---------------------------------------------------------------------------

    given("the --output flag is missing") {
        val htmlFile = Files.createTempFile("cli-spec-plain", ".html").toFile().also {
            it.writeText("<html><body><p>Hello</p></body></html>")
            it.deleteOnExit()
        }

        `when`("App.run is called with only --input") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf("--input", htmlFile.absolutePath))
            }

            then("exit code is 1") {
                exitCode shouldBe 1
            }

            then("stderr reports that --output is required") {
                stderr shouldContain "Error: --output is required"
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-04: Input file not found
    // ---------------------------------------------------------------------------

    given("the --input path does not exist on disk") {
        val missingPath = "/tmp/cli-spec-does-not-exist-input.html"
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with that --input path") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf("--input", missingPath, "--output", outputFile.absolutePath))
            }

            then("exit code is 1") {
                exitCode shouldBe 1
            }

            then("stderr reports that the input file was not found") {
                stderr shouldContain "Error: input file not found: $missingPath"
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-04 (variant): Data file not found
    // ---------------------------------------------------------------------------

    given("the --data path does not exist on disk") {
        val htmlFile = Files.createTempFile("cli-spec-plain", ".html").toFile().also {
            it.writeText("<html><body><p>Hello {{name}}</p></body></html>")
            it.deleteOnExit()
        }
        val missingDataPath = "/tmp/cli-spec-does-not-exist-data.json"
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with that --data path") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf(
                    "--input", htmlFile.absolutePath,
                    "--data", missingDataPath,
                    "--output", outputFile.absolutePath
                ))
            }

            then("exit code is 1") {
                exitCode shouldBe 1
            }

            then("stderr reports that the data file was not found") {
                stderr shouldContain "Error: data file not found: $missingDataPath"
            }
        }
    }

    // ---------------------------------------------------------------------------
    // STORY-CLI-05: Template variable missing from data file
    // ---------------------------------------------------------------------------

    given("an HTML template with {{amount}} and a data file missing the 'amount' key") {
        val htmlFile = Files.createTempFile("cli-spec-template", ".html").toFile().also {
            it.writeText("<html><body><p>Total: {{amount}}</p></body></html>")
            it.deleteOnExit()
        }
        val dataFile = Files.createTempFile("cli-spec-data", ".json").toFile().also {
            it.writeText("""{"name": "Alice"}""")
            it.deleteOnExit()
        }
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with that template and data file") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf(
                    "--input", htmlFile.absolutePath,
                    "--data", dataFile.absolutePath,
                    "--output", outputFile.absolutePath
                ))
            }

            then("exit code is 1") {
                exitCode shouldBe 1
            }

            then("stderr identifies the missing template variable by name") {
                stderr shouldContain "Error: template variable 'amount' not found in data file"
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Extra keys in data file are silently ignored
    // ---------------------------------------------------------------------------

    xgiven("an HTML file and a --data flag pointing to an existing JSON file with extra keys") {
        val htmlFile = Files.createTempFile("cli-spec-plain", ".html").toFile().also {
            it.writeText("<html><body><h1>Report</h1></body></html>")
            it.deleteOnExit()
        }
        val dataFile = Files.createTempFile("cli-spec-data", ".json").toFile().also {
            it.writeText("""{"unused_key": "ignored value", "another_unused": "42"}""")
            it.deleteOnExit()
        }
        val outputFile = Files.createTempFile("cli-spec-out", ".pdf").toFile().also {
            it.deleteOnExit()
        }

        `when`("App.run is called with --input, --data, and --output") {
            val (exitCode, stderr) = captureStderr {
                App.run(arrayOf(
                    "--input", htmlFile.absolutePath,
                    "--data", dataFile.absolutePath,
                    "--output", outputFile.absolutePath
                ))
            }

            then("exit code is 0 because extra data keys are silently ignored") {
                exitCode shouldBe 0
            }

            then("output PDF exists on disk") {
                outputFile.exists() shouldBe true
            }

            then("stderr is empty") {
                stderr.shouldBeEmpty()
            }
        }
    }
})

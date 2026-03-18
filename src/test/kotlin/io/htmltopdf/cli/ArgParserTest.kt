package io.htmltopdf.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

// Test Budget: 4 behaviors x 2 = 8 max unit tests
// Behaviors:
//   1. valid --input and --output returns CliArgs with both paths set
//   2. missing --input returns error identifying "--input is required"
//   3. missing --output returns error identifying "--output is required"
//   4. --data absent from CliArgs when flag is not supplied

class ArgParserTest : FunSpec({

    test("parses --input and --output into CliArgs with both paths set") {
        val result = ArgParser.parse(arrayOf("--input", "/in/file.html", "--output", "/out/file.pdf"))

        result.shouldBeInstanceOf<ParseResult.Success>()
        val args = (result as ParseResult.Success).args
        args.inputPath shouldBe "/in/file.html"
        args.outputPath shouldBe "/out/file.pdf"
    }

    test("returns error when --input flag is absent") {
        val result = ArgParser.parse(arrayOf("--output", "/out/file.pdf"))

        result.shouldBeInstanceOf<ParseResult.Failure>()
        (result as ParseResult.Failure).message shouldBe "Error: --input is required"
    }

    test("returns error when --output flag is absent") {
        val result = ArgParser.parse(arrayOf("--input", "/in/file.html"))

        result.shouldBeInstanceOf<ParseResult.Failure>()
        (result as ParseResult.Failure).message shouldBe "Error: --output is required"
    }

    test("dataPath is null in CliArgs when --data flag is not supplied") {
        val result = ArgParser.parse(arrayOf("--input", "/in/file.html", "--output", "/out/file.pdf"))

        result.shouldBeInstanceOf<ParseResult.Success>()
        (result as ParseResult.Success).args.dataPath shouldBe null
    }

    test("parses --data path into CliArgs when flag is supplied") {
        val result = ArgParser.parse(arrayOf("--input", "/in/file.html", "--data", "/data/file.json", "--output", "/out/file.pdf"))

        result.shouldBeInstanceOf<ParseResult.Success>()
        (result as ParseResult.Success).args.dataPath shouldBe "/data/file.json"
    }
})

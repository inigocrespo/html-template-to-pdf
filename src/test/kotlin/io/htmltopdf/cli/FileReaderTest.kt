package io.htmltopdf.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Files

// Test Budget: 2 behaviors x 2 = 4 max unit tests
// Behaviors:
//   1. returns full UTF-8 content of an existing file
//   2. signals NotFound when the path does not exist on disk

class FileReaderTest : FunSpec({

    test("returns full UTF-8 content of an existing file") {
        val tempFile = Files.createTempFile("filereader-test", ".txt").toFile()
        tempFile.writeText("hello world", Charsets.UTF_8)
        tempFile.deleteOnExit()

        val result = FileReader.read(tempFile.absolutePath)

        result.shouldBeInstanceOf<FileReader.ReadResult.Success>()
        (result as FileReader.ReadResult.Success).content shouldBe "hello world"
    }

    test("signals NotFound when the path does not exist on disk") {
        val nonExistentPath = "/tmp/this-file-does-not-exist-${System.currentTimeMillis()}.txt"

        val result = FileReader.read(nonExistentPath)

        result.shouldBeInstanceOf<FileReader.ReadResult.NotFound>()
        (result as FileReader.ReadResult.NotFound).path shouldBe nonExistentPath
    }
})

package io.htmltopdf.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

// Test Budget: 3 behaviors x 2 = 6 max unit tests
// Behaviors:
//   1. Written file exists on disk after PdfWriter completes
//   2. Written file byte content is identical to the source InputStream bytes
//   3. PdfWriter does not close the provided InputStream

class PdfWriterTest : FunSpec({

    test("written file exists on disk after PdfWriter completes") {
        val tempDir = Files.createTempDirectory("pdfwriter-test")
        val outputPath = tempDir.resolve("output.pdf").toString()
        val inputStream = ByteArrayInputStream(byteArrayOf(1, 2, 3))

        PdfWriter.write(inputStream, outputPath)

        File(outputPath).exists() shouldBe true
        tempDir.toFile().deleteRecursively()
    }

    test("written file byte content is identical to source InputStream bytes") {
        val sourceBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46) // %PDF
        val tempDir = Files.createTempDirectory("pdfwriter-test")
        val outputPath = tempDir.resolve("output.pdf").toString()
        val inputStream = ByteArrayInputStream(sourceBytes)

        PdfWriter.write(inputStream, outputPath)

        val writtenBytes = File(outputPath).readBytes()
        writtenBytes.toList() shouldBe sourceBytes.toList()
        tempDir.toFile().deleteRecursively()
    }

    test("PdfWriter does not close the provided InputStream") {
        val tempDir = Files.createTempDirectory("pdfwriter-test")
        val outputPath = tempDir.resolve("output.pdf").toString()
        val trackingStream = TrackingInputStream(ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        PdfWriter.write(trackingStream, outputPath)

        trackingStream.wasClosed shouldBe false
        tempDir.toFile().deleteRecursively()
    }
})

private class TrackingInputStream(private val delegate: InputStream) : InputStream() {
    var wasClosed = false

    override fun read(): Int = delegate.read()
    override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, len)
    override fun close() {
        wasClosed = true
    }
}

package io.htmltopdf.cli

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object PdfWriter {
    fun write(inputStream: InputStream, outputPath: String) {
        val target = Paths.get(outputPath)
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING)
    }
}

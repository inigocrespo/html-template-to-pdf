package io.htmltopdf.cli

import java.io.File

object FileReader {
    sealed class ReadResult {
        data class Success(val content: String) : ReadResult()
        data class NotFound(val path: String) : ReadResult()
    }

    fun read(path: String): ReadResult {
        val file = File(path)
        return if (file.exists()) {
            ReadResult.Success(file.readText(Charsets.UTF_8))
        } else {
            ReadResult.NotFound(path)
        }
    }
}

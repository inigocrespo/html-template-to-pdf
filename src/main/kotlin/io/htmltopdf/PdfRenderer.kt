package io.htmltopdf

import java.io.InputStream

fun interface PdfRenderer {
    fun render(html: String): InputStream
}

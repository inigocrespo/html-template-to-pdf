package io.htmltopdf

import java.io.InputStream

interface PdfRenderer {
    fun render(html: String): InputStream
}

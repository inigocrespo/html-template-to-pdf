package io.htmltopdf

import org.openpdf.pdf.ITextRenderer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class OpenPdfHtmlRenderer : PdfRenderer {
    override fun render(html: String): InputStream {
        val renderer = ITextRenderer()
        renderer.setDocumentFromString(html)
        renderer.layout()
        val baos = ByteArrayOutputStream()
        renderer.createPDF(baos)
        return ByteArrayInputStream(baos.toByteArray())
    }
}

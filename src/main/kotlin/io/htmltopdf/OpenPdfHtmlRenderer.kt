package io.htmltopdf

import org.openpdf.pdf.ITextRenderer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class OpenPdfHtmlRenderer : PdfRenderer {
    override fun render(html: String): InputStream {
        val pdfRenderer = ITextRenderer()
        pdfRenderer.setDocumentFromString(html)
        pdfRenderer.layout()
        val outputBuffer = ByteArrayOutputStream()
        pdfRenderer.createPDF(outputBuffer)
        // ByteArrayInputStream wraps an in-memory buffer and does not hold external resources
        return ByteArrayInputStream(outputBuffer.toByteArray())
    }
}

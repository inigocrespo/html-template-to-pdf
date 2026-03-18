package io.htmltopdf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OpenPdfHtmlRendererTest : FunSpec({

    test("render returns InputStream starting with PDF magic bytes") {
        val renderer = OpenPdfHtmlRenderer()
        val result = renderer.render("<html><body><p>Hello</p></body></html>")
        val pdfMagicBytes = ByteArray(4)
        result.read(pdfMagicBytes)
        String(pdfMagicBytes) shouldBe "%PDF"
    }
})

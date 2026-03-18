package io.htmltopdf

import java.io.InputStream

fun htmlToPdf(html: String, data: Map<String, String> = emptyMap()): InputStream {
    require(html.isNotBlank()) { "html must not be blank" }
    val resolvedHtml = TemplateEngine.resolve(html, data)
    return OpenPdfHtmlRenderer().render(resolvedHtml)
}

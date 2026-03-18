# Component Boundaries: html-template-to-pdf

Package: `io.htmltopdf`

---

## 1. `htmlToPdf` (public API function)

**Responsibility:** Validate inputs, coordinate `TemplateEngine` and `PdfRenderer`, and return the PDF as an `InputStream`.

**Public interface:**
```kotlin
fun htmlToPdf(
    html: String,
    data: Map<String, String> = emptyMap()
): InputStream
```

**Dependencies:**
- `TemplateEngine` — to resolve `{{key}}` tokens
- `PdfRenderer` — to render resolved HTML (injected; defaults to `OpenPdfHtmlRenderer`)

**Dependency inversion compliance:** Yes. `htmlToPdf` depends on the `PdfRenderer` interface (port), not on `OpenPdfHtmlRenderer` directly. The adapter is wired at construction or via a default parameter, never referenced by type inside the function body.

**Test strategy:**
- Unit tests: inject a test double implementing `PdfRenderer` that returns a fixed `InputStream`; verify routing (template engine called first, renderer called with resolved HTML).
- Integration tests: use real `OpenPdfHtmlRenderer`; assert returned bytes begin with `%PDF`.

---

## 2. `TemplateEngine`

**Responsibility:** Replace every `{{key}}` token in the HTML string with the corresponding value from `data`, throwing `MissingVariableError` for any unresolved token.

**Public interface:**
```kotlin
object TemplateEngine {
    fun resolve(html: String, data: Map<String, String>): String
}
```

**Dependencies:** None (pure logic; no I/O, no external calls).

**Dependency inversion compliance:** Yes. `TemplateEngine` is a leaf — it holds no references to other application components.

**Test strategy:**
- Unit tests only.
- Cover: no tokens (passthrough), single token resolved, multiple tokens resolved, missing key throws `MissingVariableError` with correct `key` and `template` fields, token present multiple times resolved at every occurrence, data contains extra keys not in template (no error).

---

## 3. `PdfRenderer` (port interface)

**Responsibility:** Define the rendering contract between the application core and any HTML-to-PDF engine.

**Public interface:**
```kotlin
interface PdfRenderer {
    fun render(html: String): InputStream
}
```

**Dependencies:** None (interface declaration only).

**Dependency inversion compliance:** Yes. This is the port. All inner-layer code depends on this interface; outer-layer adapters implement it.

**Test strategy:** Not directly tested. Consumed by `htmlToPdf` tests (via test double) and validated through `OpenPdfHtmlRenderer` integration tests.

---

## 4. `OpenPdfHtmlRenderer` (adapter)

**Responsibility:** Implement `PdfRenderer` using `ITextRenderer`, managing the mandatory render sequence and `ByteArrayOutputStream` wrapping.

**Public interface:**
```kotlin
class OpenPdfHtmlRenderer : PdfRenderer {
    override fun render(html: String): InputStream
}
```

Mandatory internal sequence:
1. `ITextRenderer()`
2. `setDocumentFromString(html)`
3. `layout()`
4. `createPDF(byteArrayOutputStream)`
5. Return `ByteArrayInputStream(byteArrayOutputStream.toByteArray())`

**Dependencies:** `org.openpdf.pdf.ITextRenderer` (openpdf-html 3.x — external, at package boundary).

**Dependency inversion compliance:** Yes. `OpenPdfHtmlRenderer` depends on `PdfRenderer` (implements it) and on `ITextRenderer` (external). The application core never imports `OpenPdfHtmlRenderer` or `ITextRenderer`.

**Test strategy:**
- Integration tests only (cannot meaningfully unit-test without invoking `ITextRenderer`).
- Assert: `render("<html><body>x</body></html>")` returns non-empty `InputStream`; first bytes equal `%PDF`.
- Assert: returned `InputStream` is readable end-to-end without exception.

---

## 5. `MissingVariableError`

**Responsibility:** Communicate to the caller that a `{{key}}` token in the HTML template had no corresponding entry in `data`.

**Public interface:**
```kotlin
class MissingVariableError(
    val key: String,
    val template: String
) : RuntimeException("Missing template variable '{{$key}}' in template: $template")
```

**Dependencies:** `RuntimeException` (stdlib).

**Dependency inversion compliance:** Yes (not applicable — exception type carries no application dependencies).

**Test strategy:**
- Verified indirectly through `TemplateEngine` unit tests.
- Assert: `key` field equals the unresolved placeholder key; `template` field equals the original HTML string passed to `TemplateEngine.resolve()`; `message` is human-readable and includes the key name.

---

## Dependency Graph Summary

```
[Caller]
    │
    ▼
[htmlToPdf]  ──────────────────────► [TemplateEngine]
    │                                      │
    │                                      ▼
    │                               [MissingVariableError]
    │
    ▼
[PdfRenderer]  ◄──── implements ────[OpenPdfHtmlRenderer]
    (port)                                 │
                                           ▼
                                    [ITextRenderer]
                                    (openpdf-html 3.x)
```

Arrows represent "depends on / calls". The `PdfRenderer` interface is the inversion boundary: everything to the left is the application core; everything to the right is infrastructure.

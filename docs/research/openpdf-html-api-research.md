# openpdf-html API Research

**Topic:** `com.github.librepdf:openpdf-html` — concrete API surface for Kotlin use
**Date:** 2026-03-18
**Researcher:** Nova (nw-researcher)
**Confidence:** High (all major claims backed by 3+ independent sources: GitHub source code, Maven Central POM, README, official test suite)

---

## 1. Latest Stable Version

**`3.0.3`** (released March 9, 2026)

A `3.0.4-SNAPSHOT` is in progress on the `master` branch at time of research.

The `2.x` line (latest: `2.2.5`) is a parallel maintenance branch. The `3.x` line is the active development track and moved the package namespace to `org.openpdf.*`.

Maven coordinates:

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf-html</artifactId>
    <version>3.0.3</version>
</dependency>
```

Gradle (Kotlin DSL):

```kotlin
implementation("com.github.librepdf:openpdf-html:3.0.3")
```

---

## 2. Java Version Requirement

**Java 21** — confirmed by the parent POM (`openpdf-parent:3.0.3`), which sets `maven-compiler-plugin` with `<release>21</release>` and a `<java.version>21</java.version>` property. The compiled artifacts target Java 21 bytecode.

---

## 3. Maven Dependencies (openpdf-html 3.0.3)

Resolved from `openpdf-html-3.0.3.pom` and `openpdf-parent-3.0.3.pom`:

| Dependency | Version | Scope |
|---|---|---|
| `com.github.librepdf:openpdf` | `3.0.3` | compile |
| `org.htmlunit:neko-htmlunit` | `4.21.0` | compile |
| `org.slf4j:slf4j-api` | `2.0.17` | provided |
| `org.jspecify:jspecify` | `1.0.0` | provided |
| `com.google.errorprone:error_prone_annotations` | `2.48.0` | provided |

Provided-scope deps (`slf4j-api`, `jspecify`, `error_prone_annotations`) are compile-time only — you must supply an SLF4J implementation at runtime (e.g., `slf4j-simple`, `logback-classic`).

Test-only dependencies (not transitive): `mockito-core:5.18.0`, `junit-jupiter`, `assertj-core:3.27.7`, `slf4j-simple:2.0.17`.

---

## 4. Package Namespace

All classes are under `org.openpdf.*` (changed from `org.xhtmlrenderer.*` in the 3.0 release). The library is described as "API compatible with Flying Saucer" except for this package rename.

---

## 5. Primary Entry Point: `ITextRenderer`

**Class:** `org.openpdf.pdf.ITextRenderer`

This is the main, low-level entry point. It accepts HTML strings directly (no file or URL required), performs layout, and writes to an `OutputStream` or returns a `byte[]`.

### Constructors (most useful)

```java
// Zero-arg — the standard starting point
public ITextRenderer()

// With custom DPI
public ITextRenderer(float dotsPerPoint, int dotsPerPixel)

// Pre-supply a custom font resolver
public ITextRenderer(FontResolver fontResolver)
```

### Document Input Methods

```java
// Load HTML from a String — the primary method for in-memory HTML
public final void setDocumentFromString(String content)

// Load HTML from a String with a base URL for resolving relative resources
public final void setDocumentFromString(String content, String baseUrl)

// Static factory — equivalent to new + setDocumentFromString; returns the renderer
public static ITextRenderer fromString(String content)
public static ITextRenderer fromString(String content, String baseUrl)

// Load from a URL string
public static ITextRenderer fromUrl(String uri)

// Load from a pre-parsed W3C DOM Document
public void setDocument(Document doc)
public void setDocument(Document doc, String url)
public void setDocument(Document doc, String url, NamespaceHandler nsh)
```

`setDocumentFromString(String)` accepts an HTML string directly. No file, URL, or InputStream required for the basic case.

### Layout

```java
// Must be called after setDocument* and before createPDF
public void layout()
```

### PDF Output

```java
// Write PDF to a provided OutputStream — primary output method
public void createPDF(OutputStream os) throws DocumentException

// Write PDF to OutputStream, with control over whether to finish (close) the document
public void createPDF(OutputStream os, boolean finish) throws DocumentException

// Write PDF to OutputStream with a starting page number
public void createPDF(OutputStream os, boolean finish, int initialPageNo) throws DocumentException

// Convert a pre-parsed Document and return a byte array — used internally by Html2Pdf
public byte[] createPDF(Document source) throws DocumentException

// Write a pre-parsed Document to an OutputStream
public void createPDF(Document source, OutputStream os) throws DocumentException

// For multi-document streaming
public void writeNextDocument()
public void writeNextDocument(int initialPageNo)
public void finishPDF()
```

The most common pattern uses `createPDF(OutputStream)` — you provide the stream; the library writes to it. Use a `ByteArrayOutputStream` to capture as bytes.

### Configuration Accessors

```java
public ITextFontResolver getFontResolver()       // add custom fonts
public SharedContext getSharedContext()          // media type, interactivity, text renderer
public ITextOutputDevice getOutputDevice()
public void setPDFEncryption(PDFEncryption pdfEncryption)
public void setPDFVersion(String v)
public void setPdfPageEvent(PdfPageEvent pdfPageEvent)
public void setScaleToFit(boolean scaleToFit)
```

---

## 6. High-Level Facade: `Html2Pdf`

**Class:** `org.openpdf.pdf.Html2Pdf`

A static utility class for simple cases where you already have resources on the classpath or accessible via a `java.net.URL`. Returns a `byte[]` directly.

```java
// Load HTML file from classpath by filename; returns PDF as byte[]
public static byte[] fromClasspathResource(String fileName)

// Convert HTML at a URL to PDF; returns PDF as byte[]
public static byte[] fromUrl(URL html)
```

`Html2Pdf` does NOT have a method that accepts an HTML `String` directly. For in-memory HTML strings, use `ITextRenderer`.

Internally, `Html2Pdf.fromUrl()` does the following before calling `renderer.createPDF(doc)`:

```java
renderer.getSharedContext().setMedia("pdf");
renderer.getSharedContext().setInteractive(false);
renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
```

These three configuration calls are worth replicating in `ITextRenderer`-based usage for best results.

---

## 7. HTML Parser Configuration: `HtmlParserConfig`

**Class:** `org.openpdf.resource.HtmlParserConfig`
**Class:** `org.openpdf.resource.HtmlResource`

`HtmlResource` wraps the HTML5 parser (neko-htmlunit) and returns a W3C `Document`. This is for advanced use cases where you need to control parsing before handing the document to `ITextRenderer`.

```java
// Parse HTML string with default settings
HtmlResource resource = HtmlResource.load("<html><body>...</body></html>");
Document doc = resource.getDocument();

// Parse with configuration
HtmlParserConfig config = HtmlParserConfig.builder()
    .reportErrors(true)
    .allowSelfClosingTags(true)
    .encoding("UTF-8")
    .build();
HtmlResource resource = HtmlResource.load(html, config);
Document doc = resource.getDocument();
```

`HtmlParserConfig` builder options:

| Option | Default | Description |
|---|---|---|
| `reportErrors(boolean)` | `false` | Emit parsing diagnostics |
| `allowSelfClosingTags(boolean)` | `false` | XHTML-style `<br/>` handling |
| `allowSelfClosingIframe(boolean)` | `false` | Allow `<iframe/>` |
| `parseNoScriptContent(boolean)` | `true` | Treat `<noscript>` as HTML |
| `scriptStripCommentDelims(boolean)` | `false` | Strip `<!--` from scripts |
| `styleStripCommentDelims(boolean)` | `false` | Strip `<!--` from styles |
| `elementNameCase(String)` | default | `"upper"`, `"lower"`, `"default"` |
| `attributeNameCase(String)` | default | `"upper"`, `"lower"`, `"default"` |
| `encoding(String)` | auto-detect | e.g. `"UTF-8"` |

---

## 8. Font Loading: `ITextFontResolver`

**Class:** `org.openpdf.pdf.ITextFontResolver`

Accessed via `renderer.getFontResolver()`.

```java
// Add a single font file (TTF, OTF, TTC, AFM, PFM, PFB, PFA)
public void addFont(String path, boolean embedded)
public void addFont(String path, String encoding, boolean embedded)
public void addFont(String path, String encoding, boolean embedded, String pathToPFB)
public void addFont(String path, String fontFamilyNameOverride,
                    String encoding, boolean embedded, String pathToPFB)
public void addFont(BaseFont font, String path, String fontFamilyNameOverride)

// Add all fonts from a directory
public void addFontDirectory(String dir, boolean embedded)
public void addFontDirectory(String dir, String encoding, boolean embedded)
```

---

## 9. Usage Examples

### Java — minimal HTML string to PDF file

```java
import org.openpdf.pdf.ITextRenderer;
import java.io.FileOutputStream;

ITextRenderer renderer = new ITextRenderer();
renderer.setDocumentFromString(html);
renderer.layout();
try (FileOutputStream os = new FileOutputStream("output.pdf")) {
    renderer.createPDF(os);
}
```

Source: `HelloWorldPdf.java` in the official test suite (verbatim).

### Java — HTML string to byte array (recommended for HTTP responses)

```java
import org.openpdf.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;

ITextRenderer renderer = new ITextRenderer();
renderer.getSharedContext().setMedia("pdf");
renderer.getSharedContext().setInteractive(false);
renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
renderer.setDocumentFromString(html);
renderer.layout();
ByteArrayOutputStream baos = new ByteArrayOutputStream();
renderer.createPDF(baos);
byte[] pdfBytes = baos.toByteArray();
```

### Java — HTML string with base URL (for resolving relative CSS/image paths)

```java
ITextRenderer renderer = new ITextRenderer();
renderer.setDocumentFromString(html, "https://example.com/base/");
renderer.layout();
renderer.createPDF(outputStream);
```

### Java — static factory style

```java
ITextRenderer renderer = ITextRenderer.fromString(html);
renderer.layout();
renderer.createPDF(outputStream);
```

### Java — classpath resource via Html2Pdf facade

```java
import org.openpdf.pdf.Html2Pdf;

byte[] pdf = Html2Pdf.fromClasspathResource("templates/invoice.html");
```

### Kotlin — idiomatic equivalent

```kotlin
import org.openpdf.pdf.ITextRenderer
import java.io.ByteArrayOutputStream

fun htmlToPdf(html: String): ByteArray {
    val renderer = ITextRenderer()
    renderer.sharedContext.setMedia("pdf")
    renderer.sharedContext.isInteractive = false
    renderer.sharedContext.textRenderer.setSmoothingThreshold(0f)
    renderer.setDocumentFromString(html)
    renderer.layout()
    return ByteArrayOutputStream().use { baos ->
        renderer.createPDF(baos)
        baos.toByteArray()
    }
}
```

### Kotlin — with custom font

```kotlin
val renderer = ITextRenderer()
renderer.fontResolver.addFont("/fonts/DejaVuSans.ttf", embedded = true)
renderer.setDocumentFromString(html)
renderer.layout()
val baos = ByteArrayOutputStream()
renderer.createPDF(baos)
```

---

## 10. API Flow Summary

```
ITextRenderer()
    └── setDocumentFromString(html)        // accepts String directly
    └── setDocumentFromString(html, baseUrl)
    └── layout()                           // must call after set, before createPDF
    └── createPDF(outputStream)            // writes to caller-provided OutputStream
                                           // or: createPDF(doc) -> byte[]
```

The library does NOT return an InputStream or auto-create an OutputStream. The caller supplies the OutputStream (typically `FileOutputStream` or `ByteArrayOutputStream`). The `byte[]`-returning overload is available via `createPDF(Document)`, but the `Document` must come from a pre-parsed W3C DOM, not from the `setDocumentFromString` path.

---

## 11. Key Architectural Notes

- **Fork lineage:** openpdf-html is a fork of Flying Saucer (~v9.12.0), forked June 2025. The CSS rendering is CSS 2.1 with select CSS3 properties (border-radius, box-shadow, text-shadow, opacity, transforms, multi-column).
- **HTML parser:** `org.htmlunit:neko-htmlunit:4.21.0` — this provides HTML5-tolerant parsing (malformed HTML, missing close tags, modern void elements).
- **PDF engine:** Delegates to `com.github.librepdf:openpdf:3.0.3` for actual PDF byte generation.
- **Not a full HTML5 renderer:** JavaScript is not executed. CSS3 features beyond the above subset may not render. Complex flexbox/grid layouts are not supported.

---

## 12. Knowledge Gaps

- **`setMedia("print")` vs `"pdf"`:** The README and docs do not specify which media type produces better results in all cases. `Html2Pdf` uses `"pdf"` internally.
- **`setSmoothingThreshold(0)` significance:** This call appears in `Html2Pdf` without explanation in any documentation. The value `0` likely disables text anti-aliasing threshold for PDF rendering, but no authoritative explanation was found.
- **CSS3 support boundary:** The exact set of supported CSS3 properties is not formally documented. Only the README examples were used to infer the subset.
- **`createPDF(Document)` → `byte[]` path through `setDocumentFromString`:** The `createPDF(Document)` overload that returns `byte[]` takes a `Document` (W3C DOM), not the same Document set via `setDocumentFromString`. Whether the internal document set by `setDocumentFromString` is the same `Document` exposed by `getDocument()` and therefore passable to `createPDF(Document)` is inferred but not confirmed from test code.

---

## Sources

| Source | Type | Used For |
|---|---|---|
| [GitHub: LibrePDF/OpenPDF openpdf-html](https://github.com/LibrePDF/OpenPDF/tree/master/openpdf-html) | Primary source code | Module structure, package names |
| [GitHub: openpdf-html README.md (raw)](https://raw.githubusercontent.com/LibrePDF/OpenPDF/master/openpdf-html/README.md) | Official documentation | API classes, builder options |
| [GitHub: ITextRenderer.java (raw)](https://raw.githubusercontent.com/LibrePDF/OpenPDF/master/openpdf-html/src/main/java/org/openpdf/pdf/ITextRenderer.java) | Primary source code | All method signatures |
| [GitHub: Html2Pdf.java (raw)](https://raw.githubusercontent.com/LibrePDF/OpenPDF/master/openpdf-html/src/main/java/org/openpdf/pdf/Html2Pdf.java) | Primary source code | Facade API, byte[] return path |
| [GitHub: HelloWorldPdf.java (raw)](https://raw.githubusercontent.com/LibrePDF/OpenPDF/master/openpdf-html/src/test/java/org/openpdf/pdf/HelloWorldPdf.java) | Official usage example | Verbatim code example |
| [Maven Central: openpdf-html 3.0.3 POM](https://repo1.maven.org/maven2/com/github/librepdf/openpdf-html/3.0.3/openpdf-html-3.0.3.pom) | Artifact metadata | Dependencies, versions |
| [Maven Central: openpdf-parent 3.0.3 POM](https://repo1.maven.org/maven2/com/github/librepdf/openpdf-parent/3.0.3/openpdf-parent-3.0.3.pom) | Artifact metadata | Java version, resolved dep versions |
| [Sonatype: openpdf-html versions](https://central.sonatype.com/artifact/com.github.librepdf/openpdf-html/versions) | Version index | Available versions list |
| [GitHub: Html5FeaturesTest.java](https://github.com/LibrePDF/OpenPDF/tree/master/openpdf-html/src/test/java/org/openpdf/pdf) | Official test suite | API usage confirmation |
| [GitHub Releases: LibrePDF/OpenPDF](https://github.com/LibrePDF/OpenPDF/releases) | Release history | Version/date confirmation |

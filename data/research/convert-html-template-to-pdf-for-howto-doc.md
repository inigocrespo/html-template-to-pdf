# Research: How to Convert an HTML Template to PDF in Kotlin

Research type: HOW-TO documentation source material
Research depth: Detailed
Date: 2026-03-18
Researcher: Nova (Evidence-Driven Knowledge Researcher)

---

## 1. Library Overview

**What it does:** `html-template-to-pdf` is a Kotlin JVM library that accepts an HTML string with optional mustache-style `{{key}}` template variables, resolves those variables against a caller-supplied data map, and returns the rendered PDF as a `java.io.InputStream`.

**Language:** Kotlin 2.1.x
**JVM floor:** Java 21 (hard requirement — driven by `openpdf-html:3.0.3`, which requires Java 21 at runtime)
**Package:** `io.htmltopdf`
**Group / version:** `io.htmltopdf:html-template-to-pdf:0.1.0`
**Primary compile dependency:** `com.github.librepdf:openpdf-html:3.0.3`

Sources: `build.gradle.kts` lines 1-10, `docs/adrs/ADR-005-java21-floor.md`, `docs/adrs/ADR-001-kotlin-jvm.md`

---

## 2. Public API

### 2.1 Entry-point function: `htmlToPdf`

**File:** `src/main/kotlin/io/htmltopdf/HtmlToPdf.kt`, lines 5-13

```kotlin
fun htmlToPdf(
    html: String,
    data: Map<String, String> = emptyMap(),
    renderer: PdfRenderer = OpenPdfHtmlRenderer()
): InputStream
```

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `html` | `String` | — (required) | The HTML content or HTML template. Must not be blank. |
| `data` | `Map<String, String>` | `emptyMap()` | Key-value pairs used to resolve `{{key}}` tokens in the template. Extra keys are silently ignored. Omit when there are no template tokens. |
| `renderer` | `PdfRenderer` | `OpenPdfHtmlRenderer()` | The rendering backend. Override only for testing or to substitute the rendering engine. |

**Returns:** `java.io.InputStream` — a readable byte stream of the PDF. The concrete runtime type is `ByteArrayInputStream`. The caller is responsible for closing the stream.

**Throws:**
- `IllegalArgumentException` — when `html` is blank or empty (message contains the word "html")
- `MissingVariableError` — when the template contains a `{{key}}` token with no corresponding entry in `data`
- Any exception from `ITextRenderer` propagates unwrapped if the rendering engine fails

Source: `HtmlToPdf.kt` lines 5-13, `architecture-design.md` lines 134-138

---

### 2.2 `PdfRenderer` interface (port)

**File:** `src/main/kotlin/io/htmltopdf/PdfRenderer.kt`, lines 5-7

```kotlin
fun interface PdfRenderer {
    fun render(html: String): InputStream
}
```

`PdfRenderer` is a functional interface (SAM). This allows callers to supply a lambda as a test double without creating a full class. The default implementation wired into `htmlToPdf` is `OpenPdfHtmlRenderer`.

Source: `PdfRenderer.kt` lines 1-7

---

### 2.3 `MissingVariableError`

**File:** `src/main/kotlin/io/htmltopdf/MissingVariableError.kt`, lines 3-6

```kotlin
class MissingVariableError(
    val key: String,
    val template: String
) : RuntimeException("Template variable '{{$key}}' is missing from the data map")
```

**Fields:**

| Field | Type | Description |
|---|---|---|
| `key` | `String` | The placeholder name (without braces) that was not found in `data`. For example, for `{{name}}`, `key` is `"name"`. |
| `template` | `String` | The full original HTML template string as passed to `htmlToPdf`. |

`MissingVariableError` extends `RuntimeException` — callers do not need to declare it in `throws` clauses and can catch it with a standard `catch` block or in a Kotest `shouldThrow<MissingVariableError>` assertion.

Source: `MissingVariableError.kt` lines 3-6, `HtmlToPdfSpec.kt` lines 186-233

---

### 2.4 `TemplateEngine` (internal, but accessible)

**File:** `src/main/kotlin/io/htmltopdf/TemplateEngine.kt`, lines 3-14

```kotlin
object TemplateEngine {
    fun resolve(template: String, data: Map<String, String>): String
}
```

`TemplateEngine` is a Kotlin `object` (singleton). Its `resolve` function is used internally by `htmlToPdf` but is also directly accessible at the package level. The acceptance tests use it directly to inspect resolved HTML before rendering (see `HtmlToPdfSpec.kt` lines 61-88).

Token syntax: `\{\{(\w+)\}\}` — alphanumeric word characters only; tokens containing spaces or special characters are not matched.

Source: `TemplateEngine.kt` lines 1-14

---

### 2.5 `OpenPdfHtmlRenderer` (default adapter)

**File:** `src/main/kotlin/io/htmltopdf/OpenPdfHtmlRenderer.kt`, lines 8-18

```kotlin
class OpenPdfHtmlRenderer : PdfRenderer {
    override fun render(html: String): InputStream
}
```

The default implementation of `PdfRenderer`. Uses `org.openpdf.pdf.ITextRenderer` from `openpdf-html:3.0.3`. Internally captures output via `ByteArrayOutputStream` and returns a `ByteArrayInputStream` wrapping the captured bytes. The comment in the source explicitly notes that `ByteArrayInputStream` does not hold external resources.

Callers who do not inject a custom renderer never need to reference this class directly.

Source: `OpenPdfHtmlRenderer.kt` lines 8-18

---

## 3. Usage Patterns

All code examples below are verbatim from the test files. Line numbers are provided.

### 3.1 Plain HTML — no template variables

From `WalkingSkeletonSpec.kt`, lines 12-25:

```kotlin
val plainHtml = "<html><body><h1>Invoice #1001</h1><p>Total: $240.00</p></body></html>"
val result = htmlToPdf(plainHtml)

// result is non-null
// first 4 bytes are the PDF magic bytes "%PDF"
val magicBytes = result.readNBytes(4)
// magicBytes == "%PDF".toByteArray()
```

When there are no `{{...}}` tokens in the HTML and no `data` argument is needed, call `htmlToPdf` with the HTML string only. Both `data` and `renderer` default and can be omitted.

Source: `WalkingSkeletonSpec.kt` lines 12-25

---

### 3.2 HTML with template variables

From `HtmlToPdfSpec.kt`, lines 34-48:

```kotlin
val result = htmlToPdf(
    "<html><body><h1>Hello {{name}}</h1></body></html>",
    mapOf("name" to "Maria Santos")
)
result.readNBytes(4) // == "%PDF".toByteArray()
```

From `HtmlToPdfSpec.kt`, lines 73-95 (multiple variables):

```kotlin
val template = "<html><body><p>Invoice for {{clientName}}</p><p>Amount: {{amount}}</p><p>Due: {{dueDate}}</p></body></html>"
val data = mapOf(
    "clientName" to "Acme Corp",
    "amount" to "\$1,500.00",
    "dueDate" to "2026-04-01"
)
val result = htmlToPdf(template, data)
result.readNBytes(4) // == "%PDF".toByteArray()
```

Source: `HtmlToPdfSpec.kt` lines 34-48, 73-95

---

### 3.3 Repeated placeholder

A placeholder that appears multiple times in the template is replaced at every occurrence. From `HtmlToPdfSpec.kt`, lines 97-111:

```kotlin
val template = "<html><body><h1>{{name}}</h1><p>Dear {{name}},</p><footer>From {{name}}</footer></body></html>"
val data = mapOf("name" to "Carlos")
val result = htmlToPdf(template, data)
// All three occurrences of {{name}} are replaced with "Carlos"
```

Source: `HtmlToPdfSpec.kt` lines 97-111

---

### 3.4 Extra keys in the data map are ignored

From `HtmlToPdfSpec.kt`, lines 113-122:

```kotlin
val result = htmlToPdf(
    "<html><body><p>Hello {{name}}</p></body></html>",
    mapOf("name" to "Maria", "unused" to "ignored", "alsoUnused" to "stillIgnored")
)
// No error; valid PDF returned
```

Source: `HtmlToPdfSpec.kt` lines 113-122

---

### 3.5 Injecting a custom renderer (for testing)

From `HtmlToPdfSpec.kt`, lines 137-153:

```kotlin
val recordedHtml = mutableListOf<String>()
val recordingRenderer = PdfRenderer { html ->
    recordedHtml += html
    java.io.ByteArrayInputStream("%PDF-test".toByteArray())
}
htmlToPdf(
    "<p>Hello {{name}}</p>",
    mapOf("name" to "Alice"),
    recordingRenderer
)
// recordedHtml.single() == "<p>Hello Alice</p>"
// The renderer receives the resolved HTML, not the raw template
```

`PdfRenderer` is a functional interface, so a lambda is sufficient. This pattern is the recommended approach for unit-testing code that calls `htmlToPdf` without invoking the real rendering engine.

Source: `HtmlToPdfSpec.kt` lines 137-153

---

### 3.6 Inspecting resolved HTML directly via `TemplateEngine`

From `HtmlToPdfSpec.kt`, lines 57-65:

```kotlin
val template = "<html><body><p>Hello {{name}}</p></body></html>"
val data = mapOf("name" to "Maria Santos")
val resolvedHtml = TemplateEngine.resolve(template, data)
// resolvedHtml contains "Maria Santos"
// resolvedHtml does not contain "{{name}}"
```

Source: `HtmlToPdfSpec.kt` lines 57-65

---

## 4. Error Scenarios

### 4.1 Blank or empty HTML string

**Trigger:** `html` is `""` or contains only whitespace (e.g., `"   "`).
**Exception:** `IllegalArgumentException`
**Message:** contains the word `"html"` (exact message from `require(html.isNotBlank()) { "html must not be blank" }`)
**Thrown by:** `htmlToPdf` immediately, before template resolution or rendering.

From `HtmlToPdfSpec.kt`, lines 160-177:

```kotlin
shouldThrow<IllegalArgumentException> {
    htmlToPdf("   ")
}
shouldThrow<IllegalArgumentException> {
    htmlToPdf("")
}
```

Source: `HtmlToPdf.kt` line 10, `HtmlToPdfSpec.kt` lines 160-177

---

### 4.2 Template variable not in data map

**Trigger:** `html` contains `{{key}}` where `key` is absent from `data`.
**Exception:** `MissingVariableError`
**Fields on the exception:**
- `key: String` — the placeholder name (e.g., `"name"` for `{{name}}`)
- `template: String` — the original HTML string passed to `htmlToPdf`
**Thrown by:** `TemplateEngine.resolve()`, before `PdfRenderer.render()` is called (no rendering resources consumed).

From `HtmlToPdfSpec.kt`, lines 196-209:

```kotlin
val template = "<html><body><h1>Hello {{name}}</h1></body></html>"
val exception = shouldThrow<MissingVariableError> {
    htmlToPdf(template, emptyMap())
}
// exception.key == "name"
// exception.template == template
```

When multiple placeholders are present and one is missing, the error identifies the **first missing key** found during the scan. From `HtmlToPdfSpec.kt`, lines 211-221:

```kotlin
val template = "<html><body><p>{{clientName}} owes {{amount}}</p></body></html>"
val exception = shouldThrow<MissingVariableError> {
    htmlToPdf(template, mapOf("clientName" to "Acme Corp"))
}
// exception.key == "amount"
// exception.template == template
```

Source: `MissingVariableError.kt`, `TemplateEngine.kt` line 9, `HtmlToPdfSpec.kt` lines 183-233

---

### 4.3 Renderer failure

Any exception thrown by `ITextRenderer` (or a custom `PdfRenderer`) propagates to the caller unwrapped. The library does not swallow or wrap renderer exceptions.

Source: `architecture-design.md` line 137, `OpenPdfHtmlRenderer.kt` lines 9-17

---

### 4.4 Literal `{{...}}` text in desired PDF output

If the caller wants the string `{{name}}` to appear literally in the rendered PDF (not as a template token), the HTML must be pre-processed before being passed to `htmlToPdf`. The library treats every `{{word}}` pattern as a token to be resolved, with no escape mechanism.

Source: `ADR-004-fail-fast-variable-validation.md` lines 54-56 (Consequences — negative)

---

## 5. Build Setup

### 5.1 Gradle dependency declaration

From `build.gradle.kts`, lines 1-28:

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.librepdf:openpdf-html:3.0.3")
}
```

The library's single compile dependency is `com.github.librepdf:openpdf-html:3.0.3`. It is declared as `implementation`, meaning it is not exposed to consumers as an `api` dependency.

### 5.2 Test dependencies (not required by consumers)

```kotlin
val kotestVersion = "5.9.1"

testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
```

Source: `build.gradle.kts` lines 1-28

---

### 5.3 JVM version requirement

- **Minimum JVM at runtime:** Java 21
- **Bytecode target:** Java 21 (`jvmToolchain(21)`)
- **Reason:** `openpdf-html:3.0.3` requires Java 21 at runtime. Declaring a lower target compiles but fails at runtime on pre-21 JVMs.
- **Java callers:** fully supported; Kotlin stdlib is a transitive compile dependency, not a peer dependency of consumers.

Source: `ADR-005-java21-floor.md`, `build.gradle.kts` lines 8-10

---

## 6. Design Rationale

### 6.1 Kotlin as implementation language (ADR-001)

Kotlin was chosen over Java 21 and Groovy for null safety (compile-time nullable/non-nullable distinction), concise syntax (top-level functions, default parameters), and consistent use of Kotlin DSL in `build.gradle.kts`. Java callers are fully supported — the Kotlin stdlib ships as a transitive dependency, not a peer requirement.

Source: `ADR-001-kotlin-jvm.md`

---

### 6.2 `PdfRenderer` interface — ports and adapters (ADR-002)

`ITextRenderer` (the openpdf-html entry point) requires real HTML parsing, font resolution, and byte serialisation. Placing it behind the `PdfRenderer` interface (port) means:

- Unit tests for `htmlToPdf` inject a test double (lambda satisfying the SAM interface) without touching the real renderer
- The rendering engine can be replaced by delivering a new adapter class without modifying the public API, `TemplateEngine`, or tests
- The dependency direction is correct: `OpenPdfHtmlRenderer` (adapter/infrastructure) depends on `PdfRenderer` (port/domain), not the reverse

Callers who do not customise the renderer never interact with the interface directly.

Source: `ADR-002-ports-and-adapters-renderer.md`

---

### 6.3 Return type `InputStream` (ADR-003)

`htmlToPdf` returns `java.io.InputStream` (concrete type: `ByteArrayInputStream`) rather than `ByteArray`, `File`, or `Path` because:

- JVM streaming frameworks (Spring, Ktor, Javalin) consume `InputStream` directly in HTTP response builders
- Callers who need raw bytes call `stream.readBytes()` (one line); the reverse — wrapping `ByteArray` in `ByteArrayInputStream` — imposes that boilerplate on every streaming caller
- Returning `File` or `Path` would require the library to manage a temp-file lifecycle, which is unexpected for a pure conversion library

**Important for HOW-TO documentation:** The caller is responsible for closing the returned `InputStream`. Failure to close leaks the in-memory buffer.

Source: `ADR-003-inputstream-return.md`

---

### 6.4 Fail-fast variable validation (ADR-004)

`TemplateEngine.resolve()` scans the HTML for all `{{key}}` tokens and throws `MissingVariableError` before invoking the renderer when any token is unresolved. The rationale:

- An unresolved token is almost always a programming error (typo, missing map entry). Surfacing it immediately with a typed exception gives the caller actionable diagnostics (`key` and `template` fields).
- PDF rendering (HTML parsing, font loading, layout, serialisation) is the most expensive step. Failing before rendering avoids wasting those resources on invalid input.
- The alternative — passing unreplaced tokens to the renderer — produces a valid PDF containing the literal string `{{name}}` as visible text. This silent failure is harder to detect and debug than a thrown exception.

Source: `ADR-004-fail-fast-variable-validation.md`

---

### 6.5 Java 21 as JVM floor (ADR-005)

Java 21 is the minimum JVM because `openpdf-html:3.0.3` requires it at runtime. Java 17 and 11 were evaluated and rejected — not for preference reasons, but because the rendering dependency is incompatible with pre-21 JVMs. Java 21 is an LTS release (GA September 2023, Oracle support until September 2031).

Source: `ADR-005-java21-floor.md`

---

## 7. Knowledge Gaps

### 7.1 Acceptance criteria document targets Node.js, not Kotlin

The file `docs/requirements/acceptance-criteria.md` describes a Node.js/JavaScript API: it references `Promise`, `stream.Readable`, CommonJS `require()`, and `htmlString must be a non-empty string` error messages. None of these constructs exist in the Kotlin implementation. The actual Kotlin error message is `"html must not be blank"` (`HtmlToPdf.kt` line 10), not the Node.js message in the acceptance criteria.

**Impact on HOW-TO documentation:** Use the Kotlin test files (`HtmlToPdfSpec.kt`, `WalkingSkeletonSpec.kt`) as the authoritative source for error messages and behaviour. Do not use `acceptance-criteria.md` as a source for the Kotlin API.

**Gap type:** Document mismatch — the acceptance criteria appear to have been written for a different (Node.js) implementation of the same concept.

---

### 7.2 No KDoc on public API

None of the public-facing files (`HtmlToPdf.kt`, `PdfRenderer.kt`, `MissingVariableError.kt`) contain KDoc comments. There is no machine-readable API documentation. All API descriptions in this research are derived from source code and ADRs.

---

### 7.3 Stream lifecycle — no documented `close()` guidance in source

`ADR-003` notes that the caller must close the returned `InputStream`, and that failure to do so leaks the in-memory buffer. However, there is no KDoc on `htmlToPdf` and no README found in the project root documenting this requirement for consumers.

The HOW-TO document should include an explicit close / use-block example.

---

### 7.4 No README or published Javadoc found

A `README.md` at the project root was not found via glob. There is no published Javadoc or Maven Central artifact URL discoverable from the local files. The HOW-TO document should assume consumers will add the library as a local Gradle `implementation` dependency or via a private repository.

---

### 7.5 Token syntax constraint not surfaced in public API

The `TemplateEngine` regex `\{\{(\w+)\}\}` matches only word characters (`[a-zA-Z0-9_]`). Tokens with hyphens, spaces, or dots (e.g., `{{client-name}}`, `{{client.name}}`) will not be matched and will be treated as literal text — not as missing variables. This is a silent constraint with no public documentation found in the source files.

---

### 7.6 Thread safety of `OpenPdfHtmlRenderer` not documented

`OpenPdfHtmlRenderer` creates a new `ITextRenderer` instance on each `render()` call, which suggests it is stateless and thread-safe. However, this is not explicitly documented or tested. Thread-safety guarantees are absent from ADRs and source comments.

---

## 8. Sources

All sources are local project files. No web sources were consulted.

| # | File path | Used for |
|---|---|---|
| 1 | `src/main/kotlin/io/htmltopdf/HtmlToPdf.kt` | Public entry-point function signature and input validation |
| 2 | `src/main/kotlin/io/htmltopdf/PdfRenderer.kt` | `PdfRenderer` interface definition |
| 3 | `src/main/kotlin/io/htmltopdf/MissingVariableError.kt` | Error class fields and inheritance |
| 4 | `src/main/kotlin/io/htmltopdf/TemplateEngine.kt` | Token resolution logic, regex pattern |
| 5 | `src/main/kotlin/io/htmltopdf/OpenPdfHtmlRenderer.kt` | Default renderer implementation, stream handling |
| 6 | `src/test/kotlin/io/htmltopdf/WalkingSkeletonSpec.kt` | Plain HTML usage example, PDF magic bytes verification |
| 7 | `src/test/kotlin/io/htmltopdf/HtmlToPdfSpec.kt` | All other usage examples, error scenarios, SAM renderer injection |
| 8 | `build.gradle.kts` | Dependency coordinates, Kotlin version, JVM toolchain |
| 9 | `docs/adrs/ADR-001-kotlin-jvm.md` | Language choice rationale |
| 10 | `docs/adrs/ADR-002-ports-and-adapters-renderer.md` | `PdfRenderer` interface design rationale |
| 11 | `docs/adrs/ADR-003-inputstream-return.md` | Return type rationale, stream close requirement |
| 12 | `docs/adrs/ADR-004-fail-fast-variable-validation.md` | Fail-fast validation rationale, `MissingVariableError` design |
| 13 | `docs/adrs/ADR-005-java21-floor.md` | Java 21 requirement rationale |
| 14 | `docs/requirements/acceptance-criteria.md` | Error scenarios (with caveat: document targets Node.js, not Kotlin) |
| 15 | `docs/design/architecture-design.md` | Component responsibilities, dependency flow, error handling strategy |

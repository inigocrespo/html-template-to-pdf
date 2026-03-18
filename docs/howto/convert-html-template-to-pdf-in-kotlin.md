# How to convert an HTML template to PDF in Kotlin

Convert HTML strings (plain or templated with Mustache-style variables) into PDF documents using the `html-template-to-pdf` library.

## Prerequisites

- Kotlin 2.1.x or Java 21+
- Gradle project
- Kotlin or Java code that can import from `io.htmltopdf`

## 1. Add the dependency

Add the library to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.htmltopdf:html-template-to-pdf:0.1.0")
}
```

Ensure your project targets Java 21 or later:

```kotlin
kotlin {
    jvmToolchain(21)
}
```

## 2. Convert plain HTML to PDF

Import the function and pass an HTML string:

```kotlin
import io.htmltopdf.htmlToPdf

val html = "<html><body><h1>Invoice #1001</h1><p>Total: \$240.00</p></body></html>"
val pdf = htmlToPdf(html)

// pdf is a java.io.InputStream
// Write to file or send in HTTP response
```

The function returns a `java.io.InputStream` containing the rendered PDF. Always close the stream when done:

```kotlin
val html = "<html><body><h1>Invoice #1001</h1><p>Total: \$240.00</p></body></html>"
val pdf = htmlToPdf(html)

pdf.use { stream ->
    // Write to file
    stream.transferTo(File("invoice.pdf").outputStream())
}
```

## 3. Inject dynamic data into a template

Use `{{key}}` placeholders in your HTML. Pass a `Map<String, String>` with the data:

```kotlin
import io.htmltopdf.htmlToPdf

val template = "<html><body><h1>Hello {{name}}</h1></body></html>"
val data = mapOf("name" to "Maria Santos")
val pdf = htmlToPdf(template, data)
```

> **Token syntax**: placeholder names must contain only word characters (letters, digits, underscores). `{{client-name}}` is not valid; use `{{clientName}}` instead.

Use multiple variables:

```kotlin
val template = "<html><body><p>Invoice for {{clientName}}</p><p>Amount: {{amount}}</p><p>Due: {{dueDate}}</p></body></html>"
val data = mapOf(
    "clientName" to "Acme Corp",
    "amount" to "\$1,500.00",
    "dueDate" to "2026-04-01"
)
val pdf = htmlToPdf(template, data)
```

Placeholders can appear multiple times in the template — all occurrences are replaced:

```kotlin
val template = "<html><body><h1>{{name}}</h1><p>Dear {{name}},</p><footer>From {{name}}</footer></body></html>"
val data = mapOf("name" to "Carlos")
val pdf = htmlToPdf(template, data)
// All three {{name}} tokens are replaced with "Carlos"
```

Extra keys in the data map are silently ignored:

```kotlin
val data = mapOf("name" to "Maria", "unused" to "ignored", "alsoUnused" to "stillIgnored")
val pdf = htmlToPdf("<html><body><p>Hello {{name}}</p></body></html>", data)
// No error; valid PDF returned
```

## 4. Write the PDF to a file

Read from the returned `InputStream` and write to disk:

```kotlin
import io.htmltopdf.htmlToPdf
import java.io.File

val html = "<html><body><h1>My Document</h1></body></html>"
val pdf = htmlToPdf(html)

pdf.use { stream ->
    val output = File("output.pdf").outputStream()
    output.use { file ->
        stream.transferTo(file)
    }
}
```

Or, in a web framework (Spring Boot example):

```kotlin
import io.htmltopdf.htmlToPdf
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@GetMapping("/export")
fun exportPdf(): ResponseEntity<ByteArray> {
    val html = "<html><body><h1>Export</h1></body></html>"
    val pdf = htmlToPdf(html)
    val bytes = pdf.readBytes()

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(bytes)
}
```

## 5. Handle errors

### Blank or empty HTML

Passing an empty or whitespace-only HTML string raises `IllegalArgumentException`:

```kotlin
import io.htmltopdf.htmlToPdf

try {
    htmlToPdf("")
} catch (e: IllegalArgumentException) {
    // Message contains "html"
    println("Error: ${e.message}")
}
```

Always provide non-empty HTML content.

### Missing template variables

When a `{{key}}` placeholder exists in the template but has no corresponding entry in the data map, `MissingVariableError` is thrown:

```kotlin
import io.htmltopdf.htmlToPdf
import io.htmltopdf.MissingVariableError

val template = "<html><body><h1>Hello {{name}}</h1></body></html>"

try {
    htmlToPdf(template, emptyMap())
} catch (e: MissingVariableError) {
    println("Missing variable: {{${e.key}}}")
    println("Template: ${e.template}")
}
```

The exception has two fields:
- `key` — the placeholder name (e.g., `"name"` for `{{name}}`)
- `template` — the original HTML string

When multiple variables are missing, the error identifies the first missing key found during scanning:

```kotlin
val template = "<html><body><p>{{clientName}} owes {{amount}}</p></body></html>"

try {
    htmlToPdf(template, mapOf("clientName" to "Acme Corp"))
    // amount is missing
} catch (e: MissingVariableError) {
    println(e.key) // Prints: "amount"
}
```

Ensure all `{{key}}` tokens in your template have matching entries in the data map.

### Token syntax

Only word characters (alphanumeric and underscore) are matched as placeholders: `{{name}}`, `{{user_id}}`, `{{amount123}}`.

Tokens with hyphens, dots, or spaces are not recognized: `{{client-name}}`, `{{user.id}}`, `{{client name}}` will appear literally in the output, not treated as placeholders.

If you need dynamic values in those forms, adjust your variable names to use underscores or camelCase:

```kotlin
val template = "<html><body><p>{{clientName}} ({{userId}})</p></body></html>"
val data = mapOf("clientName" to "Alice", "userId" to "12345")
val pdf = htmlToPdf(template, data)
```

## Next steps

- See [Data Models](../design/data-models.md) for full parameter constraints, type definitions, and `InputStream` lifecycle details.
- See [Architecture Design](../design/architecture-design.md) for the rationale behind stream handling, template validation, and the renderer abstraction.

<!--
doc_review:
  verdict: approved
  reviewer: nw-documentarist-reviewer
  reviewed_at: "2026-03-18"
  type_purity: 0.94
  collapse_clean: true
  iteration: 2
  notes: "Broken links fixed (now pointing to existing design docs). Token syntax constraint moved before multi-variable example. All quality gates pass."
-->

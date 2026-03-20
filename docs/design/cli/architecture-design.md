# Architecture Design: htmltopdf CLI

Version: 1.0
Date: 2026-03-18
Feature: htmltopdf CLI (`--input` / `--data` / `--output`)

---

## Executive Summary

The `htmltopdf` CLI wraps the existing `io.htmltopdf.htmlToPdf` library function in a command-line interface. It reads an HTML template from a file, optionally reads a flat JSON data file, invokes the library, and writes the resulting PDF to a file. The CLI is distributed as a shadow (fat) JAR — a single self-contained executable artifact.

No changes are made to the library's public API. The CLI lives in a new package (`io.htmltopdf.cli`) inside the existing single-project Gradle build.

---

## Architecture Decision Summary

| Decision | Choice | Rationale |
|---|---|---|
| Project structure | Single-project, new package | Avoids restructuring existing source; CLI is not independently deployable |
| Argument parsing | Manual `args: Array<String>` parsing | Only 3 flags; no framework dependency justified |
| JSON parsing | Gson 2.11.0 | Single method call for flat `Map<String, String>`; minimal footprint |
| Distribution | Shadow (fat) JAR via shadow plugin | NFR-01: single self-contained executable; standard Gradle solution |
| Library coupling | Direct call to `htmlToPdf()` | No API changes; CLI is a thin adapter over the existing function |

---

## Component Responsibilities

| Component | Package | Responsibility |
|---|---|---|
| `Main` (main fn) | `io.htmltopdf.cli` | Entry point: orchestrates arg parsing, file I/O, library call, exit codes |
| `ArgParser` | `io.htmltopdf.cli` | Validates and extracts `--input`, `--data`, `--output` from `args` |
| `FileReader` | `io.htmltopdf.cli` | Reads HTML and JSON files from disk (UTF-8) |
| `JsonParser` | `io.htmltopdf.cli` | Parses flat JSON object into `Map<String, String>` via Gson |
| `PdfWriter` | `io.htmltopdf.cli` | Writes `InputStream` to an output file path |
| `htmlToPdf()` | `io.htmltopdf` | Existing library function — unchanged |

---

## Error Handling Flow

```
parse args
    │
    ├─ missing/unknown flag ──────────────────────────────► stderr + exit 1
    ▼
read --input file
    │
    ├─ file not found / unreadable ──────────────────────► stderr + exit 1
    ▼
read --data file (if provided)
    │
    ├─ file not found / unreadable ──────────────────────► stderr + exit 1
    ├─ JSON is not a flat object ────────────────────────► stderr + exit 1
    ▼
call htmlToPdf(html, data)
    │
    ├─ MissingVariableError ─────────────────────────────► stderr + exit 1
    ├─ any other exception ──────────────────────────────► stderr + exit 1
    ▼
write PDF to --output file
    │
    ├─ output path not writable ─────────────────────────► stderr + exit 1
    ▼
exit 0 (silent stdout)
```

---

## C4 System Context Diagram

```mermaid
C4Context
  title System Context — htmltopdf CLI

  Person(developer, "Developer", "Runs htmltopdf from the command line to convert HTML templates to PDF files")

  System(cli, "htmltopdf CLI", "Shadow JAR. Reads HTML and JSON files, converts to PDF via the html-template-to-pdf library, writes PDF to disk.")

  System_Ext(library, "html-template-to-pdf library", "Kotlin JVM library. Resolves template variables and delegates rendering to openpdf-html.")

  System_Ext(openpdf, "openpdf-html 3.x", "Open-source HTML-to-PDF rendering engine (LGPL). Bundled inside shadow JAR.")

  Rel(developer, cli, "invokes", "java -jar htmltopdf.jar --input --data --output")
  Rel(cli, library, "calls", "htmlToPdf(html, data): InputStream")
  Rel(library, openpdf, "delegates rendering to", "ITextRenderer API")
```

---

## C4 Container Diagram

```mermaid
C4Container
  title Container — htmltopdf CLI Shadow JAR

  Person(developer, "Developer", "CLI user")

  Container_Boundary(jar, "htmltopdf-all.jar (shadow JAR)") {
    Component(main, "Main", "Kotlin main function", "Orchestrates CLI flow; sets exit code; writes to stderr on error")
    Component(argparser, "ArgParser", "Kotlin object/class", "Parses --input / --data / --output from args array; returns CliArgs or error")
    Component(filereader, "FileReader", "Kotlin object/class", "Reads files from disk as UTF-8 strings")
    Component(jsonparser, "JsonParser", "Kotlin object/class", "Deserialises flat JSON to Map<String, String> via Gson")
    Component(pdfwriter, "PdfWriter", "Kotlin object/class", "Copies InputStream to output file path")
    Component(htmltopdf, "htmlToPdf()", "Kotlin top-level function (library)", "Resolves template variables; renders HTML to PDF InputStream")
    Component(gson, "Gson 2.11.0", "Bundled runtime dependency", "JSON deserialisation")
    Component(openpdf, "openpdf-html 3.x", "Bundled runtime dependency", "HTML-to-PDF rendering engine")
  }

  Rel(developer, main, "invokes via", "java -jar")
  Rel(main, argparser, "delegates arg parsing to", "parse(args): CliArgs")
  Rel(main, filereader, "reads HTML file via", "readText(path): String")
  Rel(main, filereader, "reads JSON file via", "readText(path): String")
  Rel(main, jsonparser, "parses JSON via", "parse(json): Map<String, String>")
  Rel(main, htmltopdf, "converts HTML via", "htmlToPdf(html, data): InputStream")
  Rel(main, pdfwriter, "writes PDF via", "write(stream, path)")
  Rel(jsonparser, gson, "deserialises via", "Gson.fromJson()")
  Rel(htmltopdf, openpdf, "renders via", "ITextRenderer API")
```

---

## Integration with Existing Library

### Function called

```
io.htmltopdf.htmlToPdf(
    html: String,
    data: Map<String, String> = emptyMap(),
    renderer: PdfRenderer = OpenPdfHtmlRenderer()
): InputStream
```

The CLI always uses the default `renderer` parameter. No custom renderer is injected.

### Exceptions caught by the CLI

| Exception | Source | CLI response |
|---|---|---|
| `IllegalArgumentException` | `htmlToPdf` — blank HTML guard | stderr message + exit 1 |
| `MissingVariableError` | `TemplateEngine` — unresolved `{{key}}` token | stderr message + exit 1 |
| `Exception` (catch-all) | `OpenPdfHtmlRenderer` / unexpected | stderr message + exit 1 |

The CLI does not suppress or swallow exceptions. All error paths write to `stderr` and exit with code 1.

---

## Build Changes Required

### 1. Apply shadow plugin

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
```

### 2. Add Gson dependency

```kotlin
dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    // ... existing dependencies unchanged
}
```

### 3. Configure shadow JAR manifest

```kotlin
tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "io.htmltopdf.cli.MainKt"
    }
    archiveClassifier.set("all")
}
```

The shadow JAR bundles all runtime dependencies (Gson, openpdf-html, and transitive deps) into a single artifact. The existing `jar` task produces the library JAR (unchanged). The shadow task produces `htmltopdf-all.jar`.

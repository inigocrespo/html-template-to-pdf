# Component Boundaries: htmltopdf CLI

Version: 1.0
Date: 2026-03-18

---

## Overview

All CLI components live in `io.htmltopdf.cli`. No component in this package imports from `openpdf-html` directly — the rendering boundary is enforced through the existing `io.htmltopdf.htmlToPdf()` function.

```
io.htmltopdf.cli
├── Main.kt          (main entry point)
├── ArgParser.kt
├── FileReader.kt
├── JsonParser.kt
└── PdfWriter.kt

io.htmltopdf          (existing library — no changes)
├── HtmlToPdf.kt
├── TemplateEngine.kt
├── PdfRenderer.kt
├── OpenPdfHtmlRenderer.kt
└── MissingVariableError.kt
```

---

## Component: Main

**Responsibility:** Orchestrates the CLI flow from args through exit code; owns all error handling and process exit.

### Function signature

```kotlin
fun main(args: Array<String>)
```

### Dependencies

- `ArgParser` — arg parsing
- `FileReader` — file reading
- `JsonParser` — JSON deserialisation
- `PdfWriter` — PDF file writing
- `io.htmltopdf.htmlToPdf` — library entry point
- `io.htmltopdf.MissingVariableError` — for typed error catch

### Test strategy

Integration tests only: invoke `main()` with controlled `args` arrays pointing at temp files. Verify exit behaviour via captured `System.err` output and output file existence. Unit testing is deferred to individual component tests.

---

## Component: ArgParser

**Responsibility:** Extracts and validates `--input`, `--data`, and `--output` flag values from the raw args array, returning a typed result.

### Function signatures

```kotlin
fun parse(args: Array<String>): Result<CliArgs>
```

Where `Result` is `kotlin.Result`. A `Failure` carries an `IllegalArgumentException` with a human-readable message describing the invalid or missing flag.

### Dependencies

- `CliArgs` data class (see `data-models.md`)
- Kotlin stdlib only

### Test strategy

Unit tests: table-driven. Cover valid input, missing `--input`, missing `--output`, unrecognised flags, duplicate flags, and `--data` absent (optional). No file I/O.

---

## Component: FileReader

**Responsibility:** Reads a file from a given path and returns its contents as a UTF-8 string.

### Function signatures

```kotlin
fun readText(path: String): Result<String>
```

A `Failure` carries an `IOException` (file not found, permission denied, etc.).

### Dependencies

- `java.io.File` (stdlib)
- No third-party dependencies

### Test strategy

Unit tests with temp files: valid file, missing file, empty file (valid — empty string is a legitimate result; blank HTML validation is the library's responsibility), non-readable file (permission test where OS supports it).

---

## Component: JsonParser

**Responsibility:** Deserialises a flat JSON object string into `Map<String, String>` using Gson.

### Function signatures

```kotlin
fun parse(json: String): Result<Map<String, String>>
```

A `Failure` carries an `IllegalArgumentException` when the JSON is not a flat object (e.g., nested objects, arrays, non-string values after coercion, or malformed JSON).

### Dependencies

- `com.google.gson.Gson`
- `com.google.gson.JsonParser`
- `com.google.gson.JsonObject`

### Test strategy

Unit tests: valid flat JSON, nested JSON (rejected), JSON array (rejected), malformed JSON (rejected), empty object `{}` (valid, returns empty map), values with Unicode and special characters.

---

## Component: PdfWriter

**Responsibility:** Copies the contents of an `InputStream` to a file at the given output path.

### Function signatures

```kotlin
fun write(input: InputStream, path: String): Result<Unit>
```

A `Failure` carries an `IOException` (path not writable, disk full, etc.).

### Dependencies

- `java.io.File` (stdlib)
- `java.io.InputStream` (stdlib)

### Test strategy

Unit tests with temp directories: successful write (verify bytes match source), non-writable path (returns `Failure`), write to non-existent intermediate directory (returns `Failure`).

---

## Dependency Graph

```
Main
 ├── ArgParser          (no transitive CLI deps)
 ├── FileReader         (no transitive CLI deps)
 ├── JsonParser ──────► Gson
 ├── PdfWriter          (no transitive CLI deps)
 └── htmlToPdf() ─────► TemplateEngine
                    └── PdfRenderer (port)
                         └── OpenPdfHtmlRenderer ──► openpdf-html
```

Main has no direct dependency on `openpdf-html`. The rendering boundary is maintained through `htmlToPdf()`.

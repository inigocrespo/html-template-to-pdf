# Evolution: htmltopdf-cli

**Date**: 2026-03-18 / 2026-03-19
**Feature**: CLI wrapper for the html-template-to-pdf Kotlin library
**Language**: Kotlin/JVM
**Build**: Gradle (Kotlin DSL)

---

## What Was Built

A command-line tool (`htmltopdf`) that wraps the `html-template-to-pdf` library for use outside JVM application code.

**Interface**:
```
htmltopdf --input <html-file> --output <pdf-file> [--data <json-file>]
```

**Behaviour**:
- Reads an HTML file from disk
- Optionally reads a flat JSON file and injects its string key-value pairs as template variables
- Converts the rendered HTML to PDF via the library
- Writes the PDF to the specified output path
- Exits 0 on success; exits 1 with a descriptive `Error: ...` message on stderr on any failure

**Packaging**: shadow/fat JAR via `com.github.johnrengelman.shadow:8.1.1` with `Main-Class: io.htmltopdf.cli.MainKt`.

---

## Architecture Decisions

### 1. `App.run(args): Int` returns exit code instead of calling `exitProcess()`

`Main.kt` calls `exitProcess(App.run(args))`. `App.run` itself never calls `exitProcess`. This allows Kotest to run end-to-end acceptance tests in the same JVM without the test process terminating on a failure path. The pattern is a direct response to the constraint that acceptance tests must survive error scenarios.

### 2. Sealed classes for type-safe error propagation

`ParseResult` (Success/Failure) and `ReadResult` (Success/NotFound) are sealed classes. This eliminates null returns and exception-driven control flow within the domain. Each callsite pattern-matches exhaustively, making error paths visible to the compiler.

### 3. `object` singletons for stateless utilities

`ArgParser`, `FileReader`, `JsonParser`, and `PdfWriter` are Kotlin `object` declarations. They hold no state, require no injection, and are directly callable. This keeps the wiring in `App.run` explicit without unnecessary abstraction.

### 4. Reuse of `fun interface PdfRenderer` from the library

The library already exposed a `PdfRenderer` functional interface. `App.run` constructs an anonymous implementation inline using a lambda. No adapter class was introduced.

### 5. Gson 2.11.0 for flat JSON parsing

`JsonParser` uses Gson to parse the data file into a `JsonObject`, then calls `.filterValues { it is String }` to extract only top-level string primitives. Nested objects, arrays, and non-string values are silently discarded. This matches the library's template model: keys map to string substitution values.

### 6. `Files.copy` with `REPLACE_EXISTING` in PdfWriter; stream closed by caller

`PdfWriter.write` uses `Files.copy(inputStream, target, REPLACE_EXISTING)`. The `REPLACE_EXISTING` option prevents `FileAlreadyExistsException` when tests reuse output paths. `PdfWriter` does not close the stream — closing is the caller's responsibility. `App.run` wraps the `htmlToPdf()` call in `.use { }` to ensure the stream is always closed.

---

## TDD Metrics

### Roadmap

| Phase | Name           | Steps |
|-------|----------------|-------|
| 01    | Build setup    | 1     |
| 02    | CLI core       | 4     |
| 03    | Error handling | 2     |
| **Total** |            | **7** |

### Execution

All 7 steps completed with PASS on every executed phase. Phases marked NOT_APPLICABLE were correctly skipped where acceptance tests drive `App.run` directly and no additional unit-level test coverage was warranted.

| Step  | PREPARE | RED_ACCEPTANCE      | RED_UNIT            | GREEN | COMMIT |
|-------|---------|---------------------|---------------------|-------|--------|
| 01-01 | PASS    | PASS                | SKIPPED (N/A)       | PASS  | PASS   |
| 02-01 | PASS    | SKIPPED (N/A)       | PASS                | PASS  | PASS   |
| 02-02 | PASS    | SKIPPED (N/A)       | PASS                | PASS  | PASS   |
| 02-03 | PASS    | SKIPPED (N/A)       | PASS                | PASS  | PASS   |
| 02-04 | PASS    | PASS                | SKIPPED (N/A)       | PASS  | PASS   |
| 03-01 | PASS    | PASS                | SKIPPED (N/A)       | PASS  | PASS   |
| 03-02 | PASS    | PASS                | SKIPPED (N/A)       | PASS  | PASS   |

### Test Suite

| File                    | Type        | Leaf tests |
|-------------------------|-------------|-----------|
| CliSpec.kt              | Acceptance  | 21        |
| ArgParserTest.kt        | Unit        | 5         |
| FileReaderTest.kt       | Unit        | 2         |
| JsonParserTest.kt       | Unit        | 4         |
| PdfWriterTest.kt        | Unit        | 2         |
| CliInfrastructureSpec.kt| Structural  | 3 (approx)|
| **Total**               |             | **~34**   |

---

## Mutation Testing Results

**Date**: 2026-03-19
**Tool**: Manual (PITest not configured for Kotlin/JVM)
**Threshold**: 80% kill rate

| ID  | File          | Mutation                                           | Result   |
|-----|---------------|----------------------------------------------------|----------|
| M1  | App.kt        | `return 0` → `return 1`                            | KILLED   |
| M2  | App.kt        | Remove `return 1` after `ParseResult.Failure`      | KILLED   |
| M3  | App.kt        | Remove `PdfWriter.write` call                      | KILLED   |
| M4  | App.kt        | `htmlContent` → blank string                       | KILLED   |
| M5  | ArgParser.kt  | Remove `--input` required guard                    | KILLED   |
| M6  | ArgParser.kt  | Remove `--output` required guard                   | KILLED   |
| M7  | ArgParser.kt  | `i + 1 < args.size` → `true`                      | SURVIVED |
| M8  | FileReader.kt | Invert `file.exists()` condition                   | KILLED   |
| M9  | FileReader.kt | `Charsets.UTF_8` → `Charsets.ISO_8859_1`           | SURVIVED |
| M10 | JsonParser.kt | Remove `.filterValues { it is String }`            | KILLED   |
| M11 | JsonParser.kt | `return emptyMap()` → `return mapOf("mutant" to "value")` | KILLED |
| M12 | PdfWriter.kt  | Remove `StandardCopyOption.REPLACE_EXISTING`       | KILLED   |
| M13 | PdfWriter.kt  | Replace `Files.copy` with `createNewFile()`        | KILLED   |

**Kill rate: 11/13 = 84.6% — PASS**

### Surviving mutants and remediation paths

**M7** — No test supplies a flag at the last position without a value. Adding an `ArgParserTest` case with `arrayOf("--input")` (flag with no following value) would kill this mutant by asserting the `"Error: --input requires a value"` failure message.

**M9** — All test fixtures use ASCII-only content. A test reading a file containing a multi-byte UTF-8 character (e.g., `é`) and asserting the decoded string would kill this mutant, as ISO-8859-1 decoding would produce a different result.

---

## Post-Review Fixes

Two issues were raised during peer review and resolved before closure:

**D1 — Flag-present-but-no-value path**: `ArgParser` now returns an explicit `ParseResult.Failure` with `"Error: --input/--output requires a value"` when a flag is the last token in the args array with no following value. Previously this path threw `ArrayIndexOutOfBoundsException`.

**D2 — InputStream resource leak**: `App.run` now wraps the `htmlToPdf()` call in `.use { }`, ensuring the `InputStream` is closed whether the write succeeds or fails. This was identified as a resource leak in the original implementation.

---

## Lessons Learned and Patterns Established

**`App.run(args): Int` pattern for testable CLI entry points**
Returning the exit code from the application's run method and delegating to `exitProcess` only in `Main.kt` is a clean, low-ceremony pattern that enables acceptance tests to exercise all exit paths in the same JVM. This should be the default for any future CLI feature.

**Sealed classes eliminate null-checking and exception-driven control flow**
`ParseResult` and `ReadResult` made every error path explicit and compiler-checked. The pattern scales: add a new failure variant to the sealed class and the compiler identifies every unhandled callsite.

**Acceptance tests at the `App.run` boundary cover multiple units**
Steps 02-04, 03-01, and 03-02 used acceptance tests against `App.run` directly. This meant unit tests for individual components (ArgParser, FileReader, etc.) were written only where the component had logic worth isolating, not by default. The result is a test suite where acceptance tests and unit tests are complementary rather than redundant.

**`object` singletons for stateless utilities are sufficient at this scale**
No dependency injection framework was needed. The stateless nature of all utility objects meant `object` declarations provided the right level of reuse without the overhead of constructor injection or a DI container.

**Mutation testing on a small suite (13 mutations) is fast and high-signal**
Manual mutation testing identified two concrete coverage gaps — neither of which caused a test failure in normal runs — before the feature was closed. Both gaps have concrete, low-effort remediation paths documented above.

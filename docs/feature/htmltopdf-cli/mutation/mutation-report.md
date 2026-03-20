# Mutation Report: htmltopdf-cli

Date: 2026-03-19
Tool: Manual mutations (PITest not configured for Kotlin/JVM project)
Threshold: >= 80% kill rate

## Results

| ID  | File          | Mutation                                              | Result   | Killed By                                                          |
|-----|---------------|-------------------------------------------------------|----------|--------------------------------------------------------------------|
| M1  | App.kt        | `return 0` → `return 1`                               | KILLED   | CliSpec STORY-CLI-01 (exit code 0)                                 |
| M2  | App.kt        | Remove `return 1` after `ParseResult.Failure` block   | KILLED   | CliSpec STORY-CLI-03 (exit code 1 on missing flag)                 |
| M3  | App.kt        | Remove `PdfWriter.write` call (empty lambda body)     | KILLED   | CliSpec STORY-CLI-01 (output file starts with PDF magic bytes)     |
| M4  | App.kt        | `htmlContent` → `" "` (blank HTML string)             | KILLED   | CliSpec STORY-CLI-01 (initializationError — PDF renderer failure)  |
| M5  | ArgParser.kt  | Remove `--input` required guard (suppress failure)    | KILLED   | ArgParserTest (returns error when --input flag is absent); CliSpec STORY-CLI-03 (stderr reports --input required) |
| M6  | ArgParser.kt  | Remove `--output` required guard (suppress failure)   | KILLED   | ArgParserTest (returns error when --output flag is absent); CliSpec STORY-CLI-03 (stderr reports --output required) |
| M7  | ArgParser.kt  | `i + 1 < args.size` → `true`                         | SURVIVED | No test supplies a flag without a following value                  |
| M8  | FileReader.kt | `file.exists()` → `!file.exists()` (invert condition) | KILLED   | CliSpec STORY-CLI-01 (exit code 0, file exists, stderr empty); FileReaderTest |
| M9  | FileReader.kt | `Charsets.UTF_8` → `Charsets.ISO_8859_1`              | SURVIVED | No test verifies UTF-8 specific content (e.g., multi-byte chars)  |
| M10 | JsonParser.kt | Remove `.filterValues { it is String }`               | KILLED   | JsonParserTest (ignores nested objects and non-string values)      |
| M11 | JsonParser.kt | `return emptyMap()` → `return mapOf("mutant" to "value")` | KILLED | JsonParserTest (returns empty map for empty input)             |
| M12 | PdfWriter.kt  | Remove `StandardCopyOption.REPLACE_EXISTING`          | KILLED   | CliSpec (FileAlreadyExistsException on second test run using same output path) |
| M13 | PdfWriter.kt  | `Files.copy(...)` → `target.toFile().createNewFile()` | KILLED   | CliSpec STORY-CLI-01 (output file starts with PDF magic bytes); PdfWriterTest (byte content identical) |

## Summary

- Total mutations: 13
- Killed: 11
- Survived: 2
- Kill rate: 84.6%
- **Status: PASS**

## Surviving Mutants

### M7 — ArgParser.kt: `i + 1 < args.size` → `true`

**Mutation**: Always enter the value-read branch, bypassing the bounds check.

**Why it survived**: No test supplies a flag at the last position without a following value (e.g., `arrayOf("--input")` or `arrayOf("--input", "/file.html", "--output")`). The `ArrayIndexOutOfBoundsException` that would occur at runtime is never triggered.

**Coverage gap**: Add a test case to `ArgParserTest` passing a flag without a value, verifying the `"Error: --input requires a value"` failure message is returned.

### M9 — FileReader.kt: `Charsets.UTF_8` → `Charsets.ISO_8859_1`

**Mutation**: Read files using Latin-1 encoding instead of UTF-8.

**Why it survived**: All test HTML and JSON fixtures use ASCII-only content. UTF-8 and ISO-8859-1 are identical for the ASCII range, so the mutation produces no observable difference on these inputs.

**Coverage gap**: Add a test in `FileReaderTest` or `CliSpec` that reads a file containing a multi-byte UTF-8 character (e.g., `é`, `ü`, or a Unicode symbol) and asserts the decoded string matches the original. This would fail under ISO-8859-1 decoding.

## Verdict

**PASS** (84.6% kill rate >= 80% threshold)

Both surviving mutants represent identified coverage gaps with concrete remediation paths. The test suite provides strong behavioral coverage across argument parsing, file I/O, JSON parsing, PDF writing, and end-to-end CLI flow.

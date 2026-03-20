# Data Models: htmltopdf CLI

Version: 1.0
Date: 2026-03-18

---

## Data Flow

```
args: Array<String>
    │ ArgParser.parse()
    ▼
CliArgs(inputPath, dataPath?, outputPath)
    │ FileReader.readText()
    ▼
html: String, jsonText: String
    │ JsonParser.parse()
    ▼
data: Map<String, String>
    │ htmlToPdf(html, data)
    ▼
pdfStream: InputStream
    │ PdfWriter.write()
    ▼
file at outputPath
```

---

## Data Types

### `args: Array<String>`

Raw process arguments as received by `main`. No preprocessing.

Expected shape:

```
["--input", "template.html", "--output", "invoice.pdf"]
["--input", "template.html", "--data", "data.json", "--output", "invoice.pdf"]
```

Flags may appear in any order. Unrecognised flags cause a parse failure.

---

### `CliArgs`

Typed representation of parsed CLI arguments.

```kotlin
data class CliArgs(
    val inputPath: String,
    val dataPath: String?,   // null when --data is not provided
    val outputPath: String
)
```

- `inputPath`: value of `--input`; required; non-null after successful parse
- `dataPath`: value of `--data`; optional; null when flag is absent
- `outputPath`: value of `--output`; required; non-null after successful parse

---

### `Map<String, String>` — JSON data

Flat key-value map produced by `JsonParser`. Passed directly as the `data` parameter of `htmlToPdf()`.

Constraints enforced by `JsonParser`:
- Root JSON element must be an object (`{...}`), not an array or primitive
- Values must be JSON strings or primitives coercible to string (number, boolean)
- Nested objects or arrays as values are rejected

When `--data` is absent, `JsonParser` is not called. `htmlToPdf()` receives `emptyMap()`.

---

## Exit Codes

| Code | Meaning | Condition |
|---|---|---|
| `0` | Success | PDF written to output path; stdout silent |
| `1` | Failure | Any error at any stage; message written to stderr |

---

## Stderr Message Formats

All messages are written to `System.err`. Stdout remains silent in all cases. The process exits with code 1 after writing the message.

| Error condition | Stderr message format |
|---|---|
| Missing required flag | `Error: --<flag> is required` |
| Unrecognised flag | `Error: Unknown flag: <flag>` |
| Input file not found | `Error: Input file not found: <path>` |
| Data file not found | `Error: Data file not found: <path>` |
| Data file is not a flat JSON object | `Error: Data file must be a flat JSON object: <path>` |
| Template variable missing | `Error: Missing template variable: {{<key>}}` |
| Output file not writable | `Error: Cannot write output file: <path>` |
| Unexpected error | `Error: <exception message>` |

Messages use the prefix `Error: ` consistently to allow scripted stderr parsing. No stack traces are written to stderr; stack traces are suppressed in the CLI error handler.

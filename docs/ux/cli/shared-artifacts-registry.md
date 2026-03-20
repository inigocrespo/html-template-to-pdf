# Shared Artifacts Registry: htmltopdf CLI

Project: html-template-to-pdf CLI
Epic: cli
Date: 2026-03-18
Author: Luna (nw-product-owner)

Every variable that appears in a TUI mockup or journey step is registered here with its single authoritative source and all downstream consumers. Untracked variables are integration failures waiting to happen.

---

## Registry

| Artifact        | Type                | Source                             | Consumed By                          | Journey Steps     | Notes                                                                 |
|-----------------|---------------------|------------------------------------|--------------------------------------|-------------------|-----------------------------------------------------------------------|
| `htmlFilePath`  | string              | `--input` flag (argv, S02)         | S03 (file read), SE2 (error message) | S02, S03, SE2     | Required. Relative or absolute path. Used verbatim in error messages. |
| `jsonFilePath`  | string (optional)   | `--data` flag (argv, S02)          | S03 (file read), SE3 (error message) | S02, S03, SE3     | Optional. Absent when `--data` not provided. When absent, dataMap is empty map. |
| `outputPdfPath` | string              | `--output` flag (argv, S02)        | S05 (file write)                     | S02, S05          | Required. CLI creates or overwrites the file at this path.            |
| `htmlString`    | string              | File read of `htmlFilePath` (S03)  | S04 (`htmlToPdf` html param)         | S03, S04          | Full text content of the HTML template file. May contain `{{key}}` tokens. |
| `dataMap`       | Map\<String,String\>| JSON parse of `jsonFilePath` (S03) | S04 (`htmlToPdf` data param)         | S03, S04, SE4     | Flat string-to-string map. Empty map when `--data` omitted. Extra keys are silently ignored by the library. |
| `pdfStream`     | InputStream         | `htmlToPdf()` return value (S04)   | S05 (byte write to disk)             | S04, S05          | Binary PDF bytes. Must be fully consumed and closed before exit 0.   |
| `stderrMessage` | string              | CLI error handler (SE1–SE4)        | Terminal (developer reads it)        | SE1, SE2, SE3, SE4| Written to stderr only. Never written to stdout. Must name the specific cause. |
| `exitCode`      | int (0 or 1)        | CLI process exit (S06, SE1–SE4)    | Shell (caller inspects `$?`)         | S06, SE1–SE4      | 0 = success, 1 = any error. No other exit codes.                     |

---

## Single-Source Rules

1. `htmlFilePath`, `jsonFilePath`, `outputPdfPath` are set once at flag-parse time (S02). They are never modified after that point.
2. `htmlString` is read once from disk (S03). The library receives the string as-is; the CLI does not pre-process it.
3. `dataMap` is constructed once from JSON (S03) or set to empty map. The CLI does not add or remove keys before passing to the library.
4. `pdfStream` is produced exclusively by `htmlToPdf()` (S04). The CLI never constructs or wraps the stream.
5. `stderrMessage` is produced exclusively by the CLI's error handler. The library's exception message is embedded verbatim (for `MissingVariableError.key`) but the framing text is owned by the CLI.

---

## Integration Failure Risks

| Risk                                                  | Mitigated By                                                              |
|-------------------------------------------------------|---------------------------------------------------------------------------|
| `htmlFilePath` is a relative path and CWD varies      | CLI resolves path relative to process CWD at startup; documented behavior  |
| `dataMap` values contain non-string JSON types        | FR-03 specifies flat key-value object; CLI must reject or coerce arrays/objects |
| `pdfStream` partially written before error            | S05 must complete write atomically or clean up partial file on failure     |
| `MissingVariableError.key` not surfaced in stderr     | SE4 integration checkpoint: key field MUST appear verbatim in stderr      |
| Extra keys in `dataMap` silently ignored vs. warned   | Decision: silent ignore (SE5). Consistent with library behavior for extra keys. |

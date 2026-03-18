# ADR-003: Return InputStream from htmlToPdf

## Status

Accepted

## Date

2026-03-18

## Context

`ITextRenderer.createPDF()` writes to a caller-provided `OutputStream`. The library must decide what type to return to its callers after capturing the rendered PDF bytes.

Candidates are: `InputStream`, `ByteArray`, `File`, or `Path`.

## Decision

`htmlToPdf` returns `java.io.InputStream`. The concrete runtime type is `ByteArrayInputStream`, constructed by wrapping the `ByteArrayOutputStream` used internally by `OpenPdfHtmlRenderer`.

## Rationale

- **Caller expectation:** The DISCUSS wave established that callers want to stream the PDF to an HTTP response body, write it to a file, or forward it to another process. `InputStream` fits all three without requiring the caller to hold the entire document in memory as an array.
- **Streaming semantics:** `InputStream` is the standard JVM abstraction for a readable byte sequence. Callers using frameworks (Spring, Ktor, Javalin) pass `InputStream` directly to response builders.
- **`ByteArray` is still accessible:** Callers who need raw bytes call `stream.readBytes()` (one line in Kotlin). The reverse — converting `ByteArray` to `InputStream` — requires an extra `ByteArrayInputStream` wrapper on the caller side.
- **No file I/O by default:** Returning `File` or `Path` would require the library to own a temp-file lifecycle, requiring callers to delete files. This is unexpected behaviour for a pure conversion library.

## Alternatives Considered

### Return `ByteArray`

- Simpler: no lifecycle management; fully materialised in memory; easy to inspect length.
- Rejected: callers who need `InputStream` must wrap; callers who stream to HTTP hold a redundant full copy in memory; violates streaming conventions for byte-oriented APIs.

### Return `File` (temp file)

- Viable for very large PDFs that exceed heap budget.
- Rejected: introduces file system dependency and temp-file lifecycle; caller must delete the file; unexpected for a library; no evidence of PDF sizes that would exceed heap for typical HTML documents.

### Return `Path`

- Same trade-offs as `File`.
- Rejected for the same reasons.

## Consequences

- **Positive:** Works directly with JVM streaming APIs; callers can forward to HTTP, disk, or in-memory with equal ease.
- **Negative:** Caller must close the stream; failure to close leaks an in-memory buffer. This is documented explicitly in `data-models.md` and should be noted in the public API KDoc.
- **Neutral:** The concrete type (`ByteArrayInputStream`) is an implementation detail; the declared return type is `InputStream`. The library reserves the right to change the concrete type in a future version.

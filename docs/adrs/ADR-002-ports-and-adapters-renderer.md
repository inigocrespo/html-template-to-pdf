# ADR-002: Wrap ITextRenderer Behind a PdfRenderer Interface

## Status

Accepted

## Date

2026-03-18

## Context

The project depends on `openpdf-html` and its `ITextRenderer` class for HTML-to-PDF conversion. This class performs real I/O (HTML parsing, font loading, PDF serialisation) and cannot be instantiated meaningfully in a unit test environment without significant setup or a real HTML document.

A design decision is required: should `ITextRenderer` be called directly from the application core, or should it be placed behind an interface?

## Decision

Introduce a `PdfRenderer` interface (port) in the `io.htmltopdf` package. `OpenPdfHtmlRenderer` implements this interface (adapter) and is the only component that imports or references `ITextRenderer`. The application core (`htmlToPdf` function) depends only on `PdfRenderer`.

## Rationale

### Testability
Unit tests for `htmlToPdf` inject a test double implementing `PdfRenderer`. This avoids invoking `ITextRenderer`, which requires a real HTML string, font resolution, and byte serialisation. Tests run in milliseconds and do not depend on openpdf-html's rendering pipeline.

### Replaceability
If openpdf-html is replaced (e.g., with a different rendering engine), only `OpenPdfHtmlRenderer` changes. The public API signature, `TemplateEngine`, and all tests against the port remain untouched.

### Dependency direction
The port interface belongs to the application package (`io.htmltopdf`). The adapter (`OpenPdfHtmlRenderer`) depends on both the port and the external library. The application core depends on the port only. This is the standard ports-and-adapters (hexagonal) inversion: infrastructure depends on the domain, not the reverse.

## Alternatives Considered

### Call ITextRenderer directly from htmlToPdf

- Simpler: no interface, no adapter class.
- Rejected: makes `htmlToPdf` untestable without invoking the real renderer; couples the public API to an external library's class hierarchy; any engine replacement requires modifying core logic.

### Use a function type instead of an interface (e.g., `renderer: (String) -> InputStream`)

- Viable: idiomatic Kotlin; slightly fewer lines of code.
- Rejected: an explicit named interface (`PdfRenderer`) is self-documenting and appears clearly in generated API docs; a function type provides no label in IDE hover or Javadoc; the interface approach scales better if the port needs additional methods (e.g., `renderWithOptions`) in future.

## Consequences

- **Positive:** Unit tests for `htmlToPdf` are fast and isolated; engine can be swapped by delivering a new adapter.
- **Negative:** One additional interface and one additional class file; minor conceptual overhead for contributors unfamiliar with ports-and-adapters.
- **Neutral:** `OpenPdfHtmlRenderer` is the default implementation and is instantiated by `htmlToPdf` when no custom renderer is injected; callers who do not need testing never interact with the interface directly.

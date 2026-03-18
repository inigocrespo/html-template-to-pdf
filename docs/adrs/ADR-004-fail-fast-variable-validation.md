# ADR-004: Validate Template Variables Before Rendering

## Status

Accepted

## Date

2026-03-18

## Context

When a caller provides `data` containing fewer keys than there are `{{key}}` tokens in the HTML template, the library must decide how to respond. Options range from silent substitution (leave the token as-is), to empty-string substitution, to throwing an exception before rendering.

The decision affects error discoverability, API contract clarity, and resource consumption.

## Decision

`TemplateEngine.resolve()` scans the HTML for every `{{key}}` token. After substituting all keys present in `data`, if any `{{...}}` tokens remain, a `MissingVariableError` is thrown immediately. This exception is thrown before `PdfRenderer.render()` is called.

## Rationale

### Fail-fast principle
An unresolved token is almost always a programming error (typo in key name, missing map entry). Surfacing it immediately with a descriptive typed exception allows the caller to fix the root cause rather than investigating malformed PDF output downstream.

### Descriptive diagnostics
`MissingVariableError` carries `key` (the placeholder name) and `template` (the original HTML). This gives callers precise information: which variable was missing and in what document context. A generic exception or silent failure provides neither.

### Resource efficiency
PDF rendering is the most expensive step in the pipeline (HTML parsing, layout, font loading, serialisation). Throwing before rendering avoids consuming those resources when the input is known to be invalid.

### Contract clarity
The API contract is unambiguous: every `{{key}}` token in `html` must have a corresponding entry in `data`, or the call fails. There is no partial success, no best-effort output, and no hidden fallback. This matches the principle of least surprise for a library.

## Alternatives Considered

### Pass the HTML with unreplaced tokens to the renderer (silent failure)

- Simpler implementation: no post-replacement scan required.
- Rejected: `ITextRenderer` will render the literal string `{{name}}` as visible text in the PDF. The caller receives a valid PDF containing incorrect content. This is a silent, hard-to-debug failure mode.

### Substitute missing tokens with empty string

- Avoids visible placeholder text in output; slightly more lenient.
- Rejected: still a silent failure; the caller cannot distinguish "intentionally empty" from "accidentally missing key"; data loss in the PDF is harder to detect than a thrown exception.

### Throw only if `data` is non-empty (ignore missing tokens when `data` is `emptyMap()`)

- Reduces false positives when caller passes no data.
- Rejected: if `html` contains `{{key}}` tokens and `data` is empty, that is still almost certainly a programming error. The simpler rule — all tokens must resolve — is consistent regardless of whether `data` is empty.

## Consequences

- **Positive:** Programming errors surface at call time with actionable messages; no wasted rendering resources on bad input.
- **Negative:** Callers who intentionally want literal `{{...}}` text in their PDF must escape or pre-process their HTML before passing it to `htmlToPdf`. This edge case is considered rare and is documented.
- **Neutral:** The scan over the resolved HTML string is O(n) in HTML length; negligible cost relative to rendering.

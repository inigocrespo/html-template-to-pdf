# Data Models: html-template-to-pdf

---

## Input: `html: String`

**Formal name:** `htmlString`
**Kotlin type:** `String`

### Constraints
| Rule | Behaviour on violation |
|---|---|
| Must not be blank (empty or whitespace-only) | `IllegalArgumentException` thrown by `htmlToPdf` before any processing |
| Must be a valid string (non-null enforced by Kotlin type system) | Compile-time guarantee; no null check needed at runtime |

### Semantics
- Treated as a complete HTML document fragment or full document.
- May contain zero or more `{{key}}` tokens.
- Passed verbatim to `TemplateEngine.resolve()` when `data` is non-empty; passed directly to `PdfRenderer.render()` when `data` is empty and no tokens are present.
- Encoding: UTF-8 assumed; openpdf-html inherits this assumption from `ITextRenderer`.

---

## Input: `data: Map<String, String>`

**Kotlin type:** `Map<String, String>`
**Default:** `emptyMap()`

### Constraints
| Rule | Behaviour on violation |
|---|---|
| Keys must not be null or blank (enforced by Kotlin type system for null; blank keys would match no token and be silently ignored) | No error for unused data keys |
| Values must not be null (enforced by Kotlin type system) | Compile-time guarantee |
| Every `{{key}}` token in `html` must have a corresponding entry in `data` | `MissingVariableError` thrown by `TemplateEngine` |

### Semantics
- Keys map to token names: `data["name"] = "Alice"` resolves `{{name}}` → `Alice`.
- Extra keys with no matching token in `html` are silently ignored.
- A key present in `data` but appearing zero times in `html` produces no error.
- Values are substituted as raw strings; no HTML escaping is applied by the library. The caller is responsible for ensuring values are safe for HTML context.
- Map is consumed read-only; the library never mutates the caller's map.

---

## Output: `InputStream`

**Kotlin type:** `java.io.InputStream`
**Concrete runtime type:** `java.io.ByteArrayInputStream`

### Content
- The bytes constitute a valid PDF document.
- First four bytes are `%PDF` (PDF magic number).
- Remainder is the full PDF binary produced by openpdf-html from the resolved HTML.

### Lifecycle — caller responsibility
- **The caller must close the `InputStream` after use.** The library does not retain a reference after returning.
- Failure to close leaks an in-memory byte buffer. Use try-with-resources (`use {}` in Kotlin) or equivalent.
- The `InputStream` is backed by a `ByteArrayInputStream` (fully in-memory); `close()` has no I/O side-effect but should still be called for correctness.
- The stream is not thread-safe; do not share across threads without external synchronisation.
- The stream supports `mark`/`reset` (inherited from `ByteArrayInputStream`).

### Example caller pattern
```kotlin
htmlToPdf(html, data).use { stream ->
    // read or transfer stream here
}
```

---

## Exception: `MissingVariableError`

**Kotlin type:** `class MissingVariableError : RuntimeException`
**Package:** `io.htmltopdf`

### Fields
| Field | Type | Description |
|---|---|---|
| `key` | `String` | The placeholder name that was not found in `data` (e.g. `"name"` for token `{{name}}`) |
| `template` | `String` | The full original HTML string passed into `TemplateEngine.resolve()` at the time of the error |
| `message` | `String` (from `RuntimeException`) | Human-readable description, including the key name |

### Semantics
- Thrown by `TemplateEngine.resolve()` on the **first** unresolved token encountered.
- Thrown **before** `PdfRenderer.render()` is called; no rendering resources are consumed.
- Is a `RuntimeException` (unchecked); callers may catch it explicitly for user-facing error reporting.

### Example `message` format
```
Missing template variable '{{name}}' in template: <html><body>Hello {{name}}</body></html>
```

---

## Internal artifact: `resolvedHtml: String`

**Kotlin type:** `String`
**Visibility:** Internal to `htmlToPdf` function; not exposed in any public API.

### Description
The result of `TemplateEngine.resolve(html, data)`. Identical to `html` when `data` is empty and `html` contains no tokens. Contains no remaining `{{...}}` tokens when resolution succeeds.

### Invariants
- All `{{key}}` tokens present in the original `html` have been replaced with their corresponding values from `data`.
- No `{{...}}` substrings remain after successful resolution (guaranteed by `TemplateEngine`).
- Passed directly as the argument to `PdfRenderer.render()`.

### Lifecycle
- Created inside `htmlToPdf`, consumed immediately by `PdfRenderer.render()`, then eligible for garbage collection.
- Never stored, logged, or returned to the caller.

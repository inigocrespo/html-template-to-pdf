# Requirements: html-template-to-pdf

Version: 1.0
Date: 2026-03-18
Journey artifact: docs/ux/html-to-pdf/journey-html-to-pdf.yaml

---

## Functional Requirements

### FR-01: Accept HTML string as primary input

The library MUST accept a non-empty string of HTML as its first argument.

- The parameter is named `htmlString`.
- If `htmlString` is not a string, the returned Promise MUST reject with a `TypeError`.
- If `htmlString` is an empty string, the returned Promise MUST reject with a `TypeError`.
- The error message MUST state that `htmlString` must be a non-empty string.

Journey steps: S03, S06
Stories: STORY-00, STORY-01, STORY-03

---

### FR-02: Return a Promise that resolves to a Node.js Readable stream

The library MUST return a `Promise` from every call to `htmlToPdf`.

- The Promise MUST resolve to an instance of Node.js `stream.Readable`.
- The stream MUST emit PDF bytes in binary chunks.
- The stream MUST emit an `end` event after all bytes have been written.
- The library MUST NOT throw synchronously; all errors surface as Promise rejections.

Journey steps: S03, S04
Stories: STORY-00, STORY-01

---

### FR-03: Render HTML to PDF via openpdf-html

The library MUST delegate HTML-to-PDF rendering to the `openpdf-html` package.

- The resolved HTML (after variable injection, if any) is passed to `openpdf-html`.
- The library wraps the openpdf-html output in a Node.js Readable stream before resolving.
- The library MUST NOT implement its own PDF rendering logic.

Journey steps: S03, S04, S06
Stories: STORY-00, STORY-01, STORY-02

---

### FR-04: Accept an optional data object for variable injection

The library MUST accept an optional plain object as its second argument.

- The parameter is named `dataObject`.
- When omitted or `undefined`, the library MUST treat it as an empty object `{}`.
- If `dataObject` is provided but is not a plain object (e.g., is a string, array, or class instance), the returned Promise MUST reject with a `TypeError`.
- The error message MUST state that `dataObject` must be a plain object.

Journey steps: S06
Stories: STORY-02, STORY-04

---

### FR-05: Replace `{{key}}` placeholders with values from the data object

The library MUST scan the `htmlString` for tokens matching the pattern `{{key}}` and replace each with the corresponding value from `dataObject`.

- Token format: double curly braces with no spaces: `{{key}}`.
- Replacement is case-sensitive: `{{Name}}` and `{{name}}` are distinct tokens.
- All occurrences of a given token MUST be replaced (not just the first).
- Extra keys in `dataObject` that have no matching token MUST be silently ignored.
- The resolved HTML MUST contain zero remaining `{{key}}` tokens when all referenced keys are present.
- `dataObject` values are treated as strings. Non-string values MUST be coerced via `.toString()`.

Journey steps: S06, S07
Stories: STORY-02

---

### FR-06: Reject with MissingVariableError when a placeholder key is absent

If any `{{key}}` token in `htmlString` has no matching key in `dataObject`, the library MUST reject the returned Promise before invoking the renderer.

- The rejection error MUST be an instance of `MissingVariableError` (a named Error subclass).
- The error MUST include a `key` property containing the name of the first missing key.
- The error MUST include a `template` property containing the original `htmlString`.
- The renderer (`openpdf-html`) MUST NOT be called when a missing variable is detected.

Journey steps: S08
Stories: STORY-04

---

### FR-07: Export a named function `htmlToPdf`

The library MUST export a named function `htmlToPdf` as a CommonJS named export and as an ES module named export.

```js
// CommonJS
const { htmlToPdf } = require('html-template-to-pdf');

// ESM
import { htmlToPdf } from 'html-template-to-pdf';
```

Journey steps: S02
Stories: STORY-00, STORY-01

---

## Non-Functional Requirements

### NFR-01: Stream delivery performance

- The Promise MUST resolve (stream becomes available) within 2 seconds for HTML inputs up to 50 KB on a modern developer workstation (reference: MacBook-class hardware, Node.js 18+).
- This requirement applies to the time-to-first-byte of the stream, not the time to full stream consumption.

Stories: STORY-01

---

### NFR-02: Stream correctness

- The stream returned by the library MUST be a valid Node.js `stream.Readable` instance (passes `stream instanceof require('stream').Readable`).
- The first four bytes of stream output for any valid HTML input MUST match the PDF magic number `%PDF` (hex: `25 50 44 46`).
- The stream MUST close cleanly (no hanging handles, no unclosed streams after `end` event).

Stories: STORY-01

---

### NFR-03: No silent failures

- Any error condition MUST surface as a Promise rejection with a typed, descriptive error.
- Errors MUST include a human-readable `message` property.
- The library MUST NOT swallow exceptions from `openpdf-html`; they MUST propagate as Promise rejections.

Stories: STORY-03, STORY-04

---

### NFR-04: Node.js version compatibility

- The library MUST support Node.js LTS versions: 18.x, 20.x, and 22.x.
- The library MUST NOT use Node.js APIs deprecated in Node.js 18.

---

### NFR-05: No side effects

- The library MUST NOT write to the filesystem.
- The library MUST NOT make network calls.
- The library MUST NOT modify global state or prototype chains.
- All I/O is the caller's responsibility; the library only returns a stream.

---

### NFR-06: Dependency surface

- `openpdf-html` is the only required runtime dependency.
- The library MUST NOT bundle `openpdf-html`; it MUST be listed in `peerDependencies` or `dependencies` as appropriate for the chosen integration strategy.

---

### NFR-07: Error type identifiability

- `MissingVariableError` MUST be exported so callers can use `instanceof` checks.
- `TypeError` rejections MUST use the native `TypeError` class (not a custom subclass).

Stories: STORY-04

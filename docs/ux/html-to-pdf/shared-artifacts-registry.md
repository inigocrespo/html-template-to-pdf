# Shared Artifacts Registry

Journey: html-to-pdf
Last updated: 2026-03-18

This registry tracks every artifact that crosses a step boundary or is consumed by more than one step. Every `{{variable}}` used in Gherkin scenarios and every data structure referenced in the journey YAML must have an entry here. Unregistered artifacts are treated as integration failures.

---

## Artifact Table

| Artifact Name       | Type                  | Source Step | Consumed By Steps | Format / Shape                                                                 | Feature Tag   | Notes                                                                        |
|---------------------|-----------------------|-------------|-------------------|--------------------------------------------------------------------------------|---------------|------------------------------------------------------------------------------|
| `htmlString`        | `string`              | Developer   | S03, S06          | Non-empty string of valid (or potentially malformed) HTML                      | skeleton      | Entry point of the library. Validated as non-empty string before processing. |
| `dataObject`        | `object`              | Developer   | S06, S08          | Plain JavaScript object `{ [key: string]: string }`                            | feature-1     | Optional. When omitted, treated as `{}`. Must be a plain object.             |
| `pdfPromise`        | `Promise<Readable>`   | S03 / S06   | S04               | Native JavaScript Promise resolving to a Node.js `stream.Readable`             | skeleton      | The public API return value. Must never reject synchronously.                |
| `pdfStream`         | `stream.Readable`     | S04         | S05, S07          | Node.js `Readable` stream emitting PDF bytes in binary chunks                  | skeleton      | Callers call `.pipe()` directly. Must be a proper `stream.Readable` instance.|
| `resolvedHtml`      | `string`              | S06 (internal) | openpdf-html   | HTML string with all `{{key}}` tokens replaced by their data values            | feature-1     | Internal intermediate. Not exposed on the public API.                        |
| `MissingVariableError` | `Error` subclass   | S08         | Developer (catch) | `{ name: 'MissingVariableError', key: string, template: string, message: string }` | feature-1 | Thrown when any `{{key}}` token has no matching key in `dataObject`.         |
| `output.pdf`        | File (binary)         | S05 / S07   | Developer (final) | PDF file written by developer-controlled pipe destination                       | skeleton      | Not produced by the library. Library produces the stream; developer writes.  |

---

## Variable Token Registry

These are the `{{placeholder}}` tokens referenced in Gherkin scenarios. Each must be resolvable via `dataObject`.

| Token          | Gherkin Scenario(s)                                                  | Supplied By          | Resolved In Step |
|----------------|----------------------------------------------------------------------|----------------------|------------------|
| `{{name}}`     | Variable injection (single), Missing variable, Invalid dataObject    | `dataObject.name`    | S06              |
| `{{clientName}}` | Variable injection (multiple)                                      | `dataObject.clientName` | S06           |
| `{{amount}}`   | Variable injection (multiple)                                        | `dataObject.amount`  | S06              |
| `{{dueDate}}`  | Variable injection (multiple)                                        | `dataObject.dueDate` | S06              |

---

## Integration Checkpoint Summary

| Checkpoint | From  | To            | Contract                                                                                   |
|------------|-------|---------------|--------------------------------------------------------------------------------------------|
| ICP-01     | S03   | S04           | `htmlToPdf` returns a `Promise`. Synchronous throw is a contract violation.               |
| ICP-02     | S04   | S05           | Resolved value is a `stream.Readable`. Callers call `.pipe()` without additional guards.  |
| ICP-03     | S06   | openpdf-html  | `resolvedHtml` passed to renderer contains zero remaining `{{key}}` tokens when all keys were present. |
| ICP-04     | S06   | S08           | When any `{{key}}` has no match in `dataObject`, the Promise rejects before renderer is called. No partial renders. |
| ICP-05     | S03   | openpdf-html  | For the skeleton path (no `dataObject`), `htmlString` is passed directly to openpdf-html without modification. |

---

## Artifact Lifecycle Diagram

```
Developer
  |
  |-- htmlString (string) -------> [Validation] --> [openpdf-html] --> pdfStream (Readable) --> Developer
  |                                      |
  |-- dataObject (object) --> [Injection engine] --> resolvedHtml (string) --> [openpdf-html]
                                         |
                                    key missing?
                                         |
                                   MissingVariableError --> Developer (catch)
```

---

## Notes

- `resolvedHtml` is an internal artifact. It MUST NOT be surfaced on the public API.
- `pdfStream` is the primary output artifact. The library does not write files; it returns the stream.
- `output.pdf` is a developer-owned artifact. It appears in Gherkin for verifiability but is outside library scope.
- `dataObject` values are typed as `string` in initial scope. Numeric and boolean values coerced to string are out of scope for Feature 1.

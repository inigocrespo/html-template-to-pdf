# User Stories: html-template-to-pdf

Version: 1.0
Date: 2026-03-18
Journey artifact: docs/ux/html-to-pdf/journey-html-to-pdf.yaml

---

## STORY-00: Walking Skeleton -- end-to-end pipeline proof

### Feature Tag: skeleton

### Problem (The Pain)
Marco Rossi is a Node.js developer starting integration of html-template-to-pdf into his invoicing service. He finds it impossible to know whether the library is wired correctly -- install, import, render, stream -- without running an end-to-end smoke test. Without a working skeleton, any downstream development risks building on a broken foundation.

### Who (The User)
- Node.js developer integrating the library for the first time
- Working on a local development machine
- Primary motivation: verify the pipeline works before committing to further integration

### Solution (What We Build)
The thinnest possible library that:
1. Exports `htmlToPdf`
2. Accepts a plain HTML string
3. Passes it to openpdf-html
4. Returns a Promise that resolves to a Node.js Readable stream of PDF bytes

No variable injection. No error handling beyond basic plumbing. Proves the end-to-end pipeline is connected.

### As a role / I want / So that
As a Node.js developer, I want to call `htmlToPdf` with a plain HTML string and receive a readable stream, so that I can verify the library is correctly installed and the rendering pipeline is wired end-to-end.

### Domain Examples

#### Example 1: Basic invoice HTML
Marco calls `htmlToPdf('<h1>Invoice #1001</h1><p>Total: $240.00</p>')`. The Promise resolves to a Readable stream. Marco pipes it to `fs.createWriteStream('invoice-1001.pdf')`. A valid PDF file appears on disk.

#### Example 2: Single paragraph
Marco tests with `htmlToPdf('<p>Receipt for order 5577</p>')`. The stream's first four bytes are `%PDF`. The stream ends cleanly.

#### Example 3: Complex HTML structure
Marco passes a multi-section HTML string with `<table>` rows and inline styles. The Promise resolves; the stream is non-empty. Marco does not inspect visual fidelity -- just that the pipeline completes without error.

### Size: S (1 day)
### Journey steps: S01, S02, S03, S04, S05

---

## STORY-01: HTML string to PDF readable stream (no variables)

### Feature Tag: skeleton

### Problem (The Pain)
Marco Rossi needs to generate static PDF documents from HTML strings his application assembles at runtime (e.g., simple receipts with no per-customer data). He finds it painful to wire together raw openpdf-html because its API is not stream-friendly and requires boilerplate setup on every call.

### Who (The User)
- Node.js developer generating static PDF content
- HTML is fully formed before the call (no dynamic tokens)
- Wants a minimal, promise-based API with no configuration

### Solution (What We Build)
A `htmlToPdf(htmlString)` function that:
- Accepts a non-empty HTML string
- Validates the input (rejects `TypeError` for non-string or empty string)
- Delegates rendering to openpdf-html
- Returns a Promise resolving to a Node.js Readable stream

### As a role / I want / So that
As a Node.js developer, I want to pass a fully formed HTML string to `htmlToPdf` and receive a readable PDF stream, so that I can generate static PDF documents without boilerplate renderer setup.

### Domain Examples

#### Example 1: Valid HTML, stream piped to file
`htmlToPdf('<h1>Invoice #1001</h1><p>Total: $240.00</p>')` resolves to a stream. Marco pipes it to disk. PDF is valid.

#### Example 2: Non-string input rejected
`htmlToPdf(42)` rejects with `TypeError: htmlString must be a non-empty string`.

#### Example 3: Empty string rejected
`htmlToPdf('')` rejects with `TypeError: htmlString must be a non-empty string`.

### Size: S (1 day)
### Journey steps: S03, S04, S05

---

## STORY-02: Variable injection with `{{name}}` syntax

### Feature Tag: feature-1

### Problem (The Pain)
Maria Santos is building a customer-facing invoicing service. Every PDF must contain customer-specific data: name, amount, due date. She finds it tedious and error-prone to manually concatenate strings into HTML before calling the renderer -- she wants a template syntax that separates structure from data.

### Who (The User)
- Node.js developer generating personalized PDF documents
- Template HTML is defined once; data values change per request
- Key motivation: clean separation between HTML structure and runtime data

### Solution (What We Build)
Extend `htmlToPdf` to accept an optional second argument `dataObject`. Before passing HTML to the renderer, scan for `{{key}}` tokens and replace each with the corresponding value from `dataObject`. Pass the resolved HTML to openpdf-html.

### As a role / I want / So that
As a Node.js developer, I want to pass an HTML template containing `{{key}}` placeholders and a data object to `htmlToPdf`, so that placeholders are replaced with the correct values before the PDF is rendered.

### Domain Examples

#### Example 1: Single placeholder
`htmlToPdf('<h1>Hello {{name}}</h1>', { name: 'Maria Santos' })` resolves to a stream. The rendered PDF contains "Hello Maria Santos". The literal string `{{name}}` does not appear.

#### Example 2: Multiple placeholders
Template: `<h1>Invoice for {{clientName}}</h1><p>Amount: {{amount}}</p><p>Due: {{dueDate}}</p>`
Data: `{ clientName: 'Acme Corp', amount: '$1,500.00', dueDate: '2026-04-01' }`
All three tokens are replaced. No `{{...}}` tokens remain in the PDF.

#### Example 3: Extra keys ignored
`htmlToPdf('<p>Hello {{name}}</p>', { name: 'Maria', unused: 'ignored' })` resolves successfully. The extra key causes no error.

### Size: S (1 day)
### Journey steps: S06, S07

---

## STORY-03: Error handling for malformed or invalid HTML input

### Feature Tag: skeleton

### Problem (The Pain)
Marco Rossi accidentally passes a non-string value (a parsed object, a number, undefined) as the first argument. Without input validation, the library throws an untyped synchronous error deep inside openpdf-html, giving Marco no actionable error message and no indication of where the mistake was made.

### Who (The User)
- Node.js developer who has made a programming mistake in how they call the library
- May be using JavaScript (no compile-time type checks)
- Needs a clear, immediate error at the call site to self-correct

### Solution (What We Build)
Input validation at the library boundary:
- If `htmlString` is not a string: reject with `TypeError`
- If `htmlString` is an empty string: reject with `TypeError`
- Error message must name the parameter and state the requirement

### As a role / I want / So that
As a Node.js developer, I want `htmlToPdf` to reject with a descriptive `TypeError` when I pass an invalid first argument, so that I can quickly identify and fix the mistake without reading library internals.

### Domain Examples

#### Example 1: Number passed as htmlString
`htmlToPdf(42)` rejects: `TypeError: htmlString must be a non-empty string, received number`.

#### Example 2: Undefined passed as htmlString
`htmlToPdf(undefined)` rejects: `TypeError: htmlString must be a non-empty string, received undefined`.

#### Example 3: Empty string passed as htmlString
`htmlToPdf('')` rejects: `TypeError: htmlString must be a non-empty string`.

### Size: S (0.5 days)
### Journey steps: S03

---

## STORY-04: Error handling for missing template variables

### Feature Tag: feature-1

### Problem (The Pain)
Maria Santos deploys her invoicing service. A code change introduces a new template placeholder `{{invoiceNumber}}` but the data-assembly step is not updated. The library silently renders a PDF containing the literal text `{{invoiceNumber}}` -- a confusing artifact that customers receive before anyone notices. Maria needs the library to catch this class of mistake immediately, before any PDF is rendered.

### Who (The User)
- Node.js developer whose template and data object are out of sync
- May occur during development or after a template update in production
- Needs a fail-fast, descriptive error to catch the mismatch at the call site

### Solution (What We Build)
Before passing HTML to the renderer, verify that every `{{key}}` token in the template has a corresponding key in `dataObject`. If any key is missing, reject with a `MissingVariableError` that names the missing key and includes the original template.

### As a role / I want / So that
As a Node.js developer, I want `htmlToPdf` to reject with a `MissingVariableError` when my template contains a `{{key}}` placeholder that has no matching key in the data object, so that I detect the mismatch immediately and no malformed PDF is produced.

### Domain Examples

#### Example 1: Single missing key
Template: `<h1>Hello {{name}}</h1>`, data: `{}`.
Rejects: `MissingVariableError { key: 'name', template: '<h1>Hello {{name}}</h1>', message: "Missing variable 'name' in template" }`.

#### Example 2: Multiple placeholders, one missing
Template: `<p>{{clientName}} owes {{amount}}</p>`, data: `{ clientName: 'Acme Corp' }`.
Rejects with `MissingVariableError { key: 'amount', ... }`. The renderer is not called.

#### Example 3: Invalid dataObject type
`htmlToPdf('<p>Hello {{name}}</p>', 'not-an-object')` rejects: `TypeError: dataObject must be a plain object`.

### Size: S (0.5 days)
### Journey steps: S08

# User Stories: htmltopdf CLI

Version: 1.0
Date: 2026-03-18
Author: Luna (nw-product-owner)
Journey artifact: docs/ux/cli/journey-cli.yaml
Requirements: docs/requirements/cli/requirements.md

---

## STORY-CLI-01: Convert plain HTML file to PDF

### Feature Tag: cli-core

### Problem (The Pain)
Sofia Esposito maintains a reporting service that assembles static HTML strings at runtime. She wants to produce PDF files from those reports without writing Kotlin code or setting up a programmatic rendering pipeline. She finds it frustrating that there is no simple terminal command she can run -- she has to write integration code just to call a library.

### Who (The User)
- Developer with an existing HTML file who wants a PDF output
- Comfortable with the terminal; not necessarily a Kotlin developer
- Primary motivation: produce a PDF from a file with a single command, no code required

### Solution (What We Build)
A CLI command that accepts `--input <path>` and `--output <path>`, reads the HTML file, calls `htmlToPdf()` with no data map, and writes the resulting PDF to the output path. Exits 0 silently on success.

### As a role / I want / So that
As a developer, I want to run `htmltopdf --input report.html --output report.pdf` so that the HTML file is converted to a PDF on disk without me writing any code.

### Domain Examples

#### Example 1: Simple report
Sofia has `report.html` containing `<h1>Annual Report 2025</h1><p>Revenue: $4.2M</p>`. She runs `htmltopdf --input report.html --output report.pdf`. The command exits with code 0. `report.pdf` appears in the current directory and opens correctly in a PDF viewer.

#### Example 2: Minimal HTML
Sofia tests with `stub.html` containing `<p>Test</p>`. She runs `htmltopdf --input stub.html --output stub.pdf`. The command exits 0. `stub.pdf` exists on disk and its first four bytes are `%PDF`.

#### Example 3: HTML with inline styles and table
Sofia converts a formatted HTML invoice skeleton (with `<table>`, `<style>` blocks, no `{{}}` tokens) using `htmltopdf --input invoice-skeleton.html --output invoice-skeleton.pdf`. The command exits 0 silently. The PDF is non-empty.

### Size: S (1 day)
### Journey steps: S01, S02, S03, S04, S05, S06

---

## STORY-CLI-02: Convert HTML template with JSON data to PDF

### Feature Tag: cli-core

### Problem (The Pain)
Sofia Esposito generates customer invoices where each PDF contains customer-specific values: name, amount, due date. She currently assembles the full HTML string manually in code before calling the renderer, which means template logic is mixed into her application code. She wants to keep the HTML template as a file and inject values at render time from a data source.

### Who (The User)
- Developer using `{{key}}` placeholder syntax in their HTML templates
- Data values come from a JSON file (e.g. produced by another tool or service)
- Key motivation: clean separation between HTML structure (file) and runtime data (JSON)

### Solution (What We Build)
Extend the CLI to accept `--data <path>` pointing to a JSON file with a flat key-value structure. The CLI reads the JSON, builds a `Map<String, String>`, and passes it to `htmlToPdf()`. All `{{key}}` tokens in the template are replaced by the library before rendering.

### As a role / I want / So that
As a developer, I want to run `htmltopdf --input invoice.html --data invoice-data.json --output invoice.pdf` so that the HTML template's `{{key}}` placeholders are replaced with values from the JSON file before the PDF is rendered.

### Domain Examples

#### Example 1: Two-placeholder invoice
`invoice.html`: `<h1>Invoice for {{clientName}}</h1><p>Amount due: {{amount}}</p>`
`invoice-data.json`: `{ "clientName": "Acme Corp", "amount": "$1,500.00" }`
Command: `htmltopdf --input invoice.html --data invoice-data.json --output invoice.pdf`
Result: exits 0. `invoice.pdf` exists. PDF contains "Acme Corp" and "$1,500.00". No `{{...}}` tokens appear in the PDF.

#### Example 2: Five-field contract
`contract.html` contains five `{{field}}` tokens: `recipientName`, `contractDate`, `serviceDescription`, `totalFee`, `paymentTerms`.
`contract-data.json` contains all five keys with realistic values.
Command produces `contract.pdf` with all fields replaced. Exits 0.

#### Example 3: Extra key in JSON is silently ignored
`greeting.html`: `<p>Hello {{name}}</p>`
`greeting-data.json`: `{ "name": "Sofia", "unusedRegion": "EMEA" }`
Command produces `greeting.pdf` successfully. The `unusedRegion` key does not cause an error. Exits 0.

### Size: S (1 day)
### Journey steps: S02, S03, S04, S05, S06, SE5

---

## STORY-CLI-03: Missing required flag produces helpful error and exits 1

### Feature Tag: cli-errors

### Problem (The Pain)
Dmitri Volkov is writing a shell script that wraps `htmltopdf`. He misremembers the flag names and omits `--output` on his first attempt. The CLI currently (without this story) either crashes with an unhelpful exception or silently does nothing -- giving Dmitri no clear signal about what went wrong or how to fix it.

### Who (The User)
- Developer making a mistake in CLI invocation (wrong or missing flags)
- May be scripting; needs a non-zero exit code to detect failures
- Needs a human-readable error message that names the missing flag

### Solution (What We Build)
Argument parsing validates that `--input` and `--output` are both present before any file I/O. If either is absent, the CLI prints a single-line error to stderr naming the missing flag, and exits with code 1. No files are read or created.

### As a role / I want / So that
As a developer, I want the CLI to tell me exactly which required flag I omitted so that I can correct my command without guessing.

### Domain Examples

#### Example 1: --input omitted
Dmitri runs `htmltopdf --output result.pdf`. stderr contains `Error: --input is required`. Exit code is 1. No file is created.

#### Example 2: --output omitted
Dmitri runs `htmltopdf --input template.html`. stderr contains `Error: --output is required`. Exit code is 1.

#### Example 3: Both flags omitted
Dmitri runs `htmltopdf` with no flags. stderr contains an error message (either listing both missing flags or the first one encountered). Exit code is 1.

### Size: S (0.5 days)
### Journey steps: SE1

---

## STORY-CLI-04: Input file not found produces helpful error and exits 1

### Feature Tag: cli-errors

### Problem (The Pain)
Dmitri Volkov passes `--input` with a path that has a typo. Without explicit file-not-found handling, the CLI would throw a generic JVM IOException with a stack trace -- not useful in a shell script and alarming to a developer who is not expecting Kotlin exception output.

### Who (The User)
- Developer who provided an incorrect file path to `--input` or `--data`
- Running in a shell script or CI pipeline where clean error messages matter
- Needs the error to name the file that was not found

### Solution (What We Build)
Before calling `htmlToPdf()`, the CLI checks that the file at `--input` exists and is readable. If not, it prints a specific error to stderr naming the path and exits 1. The same check applies to `--data` when provided.

### As a role / I want / So that
As a developer, I want the CLI to tell me which file path it could not find so that I can correct the path immediately.

### Domain Examples

#### Example 1: --input path does not exist
Dmitri runs `htmltopdf --input invoce.html --output out.pdf` (typo). stderr contains `Error: input file not found: invoce.html`. Exit code 1. `out.pdf` is not created.

#### Example 2: --data path does not exist
Dmitri runs `htmltopdf --input template.html --data dat.json --output out.pdf` (typo). stderr contains `Error: data file not found: dat.json`. Exit code 1.

#### Example 3: --input path is a directory, not a file
Dmitri runs `htmltopdf --input ./templates --output out.pdf`. stderr contains a file-not-found-style error naming `./templates`. Exit code 1.

### Size: S (0.5 days)
### Journey steps: SE2, SE3

---

## STORY-CLI-05: Template variable missing from data file produces error with key name and exits 1

### Feature Tag: cli-errors

### Problem (The Pain)
Sofia Esposito updates `invoice.html` to include a new `{{invoiceNumber}}` placeholder but forgets to add `invoiceNumber` to `invoice-data.json`. Without explicit error handling, the library throws `MissingVariableError` -- but the CLI currently lets that exception bubble up as a JVM stack trace, which is unreadable in a terminal context and gives no clear remediation path.

### Who (The User)
- Developer whose HTML template and JSON data file are out of sync (template was updated but data was not)
- Needs to know exactly which key is missing so they can add it to the JSON file
- Running in CI where the exit code must be non-zero to fail the build

### Solution (What We Build)
The CLI catches `MissingVariableError` thrown by `htmlToPdf()`, extracts the `key` field, prints a descriptive error to stderr that names the key, and exits with code 1. No PDF is written.

### As a role / I want / So that
As a developer, I want the CLI to tell me the name of the template variable that is missing from my data file so that I can add it without inspecting the template manually.

### Domain Examples

#### Example 1: Single missing key
`invoice.html` contains `{{invoiceNumber}}` and `{{clientName}}`. `invoice-data.json` contains only `clientName`. Command: `htmltopdf --input invoice.html --data invoice-data.json --output invoice.pdf`. stderr contains `invoiceNumber`. Exit code 1. `invoice.pdf` is not created.

#### Example 2: Template with no --data flag but contains placeholders
`letter.html` contains `{{recipientName}}`. Command: `htmltopdf --input letter.html --output letter.pdf` (no `--data`). Library throws `MissingVariableError(key="recipientName", ...)`. stderr contains `recipientName`. Exit code 1.

#### Example 3: Data file present but key value is absent
`order.html` contains `{{orderTotal}}`. `order-data.json` contains `{ "orderDate": "2026-03-18" }`. stderr contains `orderTotal` and the word `Error`. Exit code 1. `order.pdf` is not created.

### Size: S (0.5 days)
### Journey steps: SE4

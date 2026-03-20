# Acceptance Criteria: htmltopdf CLI

Version: 1.0
Date: 2026-03-18
Author: Luna (nw-product-owner)
Journey artifact: docs/ux/cli/journey-cli.yaml
User stories: docs/requirements/cli/user-stories.md

All criteria are binary-verifiable via exit code inspection, file system assertions, or stderr capture. No criterion uses subjective language.

---

## STORY-CLI-01: Convert plain HTML file to PDF

### Scenario: Plain HTML file renders to PDF successfully

```gherkin
Given a file "report.html" exists containing valid HTML with no {{}} placeholders
When "htmltopdf --input report.html --output report.pdf" is executed
Then the process exits with code 0
And a file "report.pdf" exists on disk at the output path
And the first 4 bytes of "report.pdf" are 0x25 0x50 0x44 0x46 ("%PDF")
And nothing is written to stdout
And nothing is written to stderr
```

### Acceptance Criteria

- [ ] AC-CLI-01-01: Process exits with code 0 when a valid HTML file is converted successfully.
- [ ] AC-CLI-01-02: Output PDF file exists at the path given by `--output` after a successful run.
- [ ] AC-CLI-01-03: Output file begins with magic bytes `%PDF` (0x25 0x50 0x44 0x46).
- [ ] AC-CLI-01-04: Stdout is empty on a successful run.
- [ ] AC-CLI-01-05: Stderr is empty on a successful run.

---

## STORY-CLI-02: Convert HTML template with JSON data to PDF

### Scenario: HTML template with JSON data renders to PDF

```gherkin
Given a file "invoice.html" exists containing "<h1>Invoice for {{clientName}}</h1><p>Amount: {{amount}}</p>"
And a file "invoice-data.json" exists containing { "clientName": "Acme Corp", "amount": "$1,500.00" }
When "htmltopdf --input invoice.html --data invoice-data.json --output invoice.pdf" is executed
Then the process exits with code 0
And a file "invoice.pdf" exists on disk
And the first 4 bytes of "invoice.pdf" are "%PDF"
And nothing is written to stdout
And nothing is written to stderr
```

### Scenario: Extra key in JSON data file is silently ignored

```gherkin
Given a file "greeting.html" exists containing "<p>Hello {{name}}</p>"
And a file "extra-data.json" exists containing { "name": "Sofia", "unusedKey": "ignored" }
When "htmltopdf --input greeting.html --data extra-data.json --output greeting.pdf" is executed
Then the process exits with code 0
And a file "greeting.pdf" exists on disk
And nothing is written to stderr
```

### Acceptance Criteria

- [ ] AC-CLI-02-01: Process exits with code 0 when a valid HTML template and JSON data file are both provided and all template keys are present in the data.
- [ ] AC-CLI-02-02: Output PDF file exists at the `--output` path after a successful run.
- [ ] AC-CLI-02-03: Output file begins with magic bytes `%PDF`.
- [ ] AC-CLI-02-04: Stdout is empty on a successful run.
- [ ] AC-CLI-02-05: Process exits with code 0 and PDF is written successfully when the JSON data file contains keys not referenced in the template (extra keys are silently ignored).

---

## STORY-CLI-03: Missing required flag produces helpful error and exits 1

### Scenario: --input flag is absent

```gherkin
Given no --input flag is provided
When "htmltopdf --output result.pdf" is executed
Then the process exits with code 1
And stderr contains the string "Error: --input is required"
And nothing is written to stdout
And no file is created at the --output path
```

### Scenario: --output flag is absent

```gherkin
Given a file "template.html" exists
And no --output flag is provided
When "htmltopdf --input template.html" is executed
Then the process exits with code 1
And stderr contains the string "Error: --output is required"
And nothing is written to stdout
```

### Acceptance Criteria

- [ ] AC-CLI-03-01: Process exits with code 1 when `--input` flag is absent.
- [ ] AC-CLI-03-02: Stderr contains the exact string `Error: --input is required` when `--input` is absent.
- [ ] AC-CLI-03-03: Process exits with code 1 when `--output` flag is absent.
- [ ] AC-CLI-03-04: Stderr contains the exact string `Error: --output is required` when `--output` is absent.
- [ ] AC-CLI-03-05: Stdout is empty in all missing-flag error cases.
- [ ] AC-CLI-03-06: No output file is created when a required flag is absent.

---

## STORY-CLI-04: Input file not found produces helpful error and exits 1

### Scenario: --input file does not exist

```gherkin
Given no file named "missing-template.html" exists on disk
When "htmltopdf --input missing-template.html --output out.pdf" is executed
Then the process exits with code 1
And stderr contains the string "Error: input file not found: missing-template.html"
And nothing is written to stdout
And no file "out.pdf" is created
```

### Scenario: --data file does not exist

```gherkin
Given a file "template.html" exists containing "<p>Hello {{name}}</p>"
And no file named "missing-data.json" exists on disk
When "htmltopdf --input template.html --data missing-data.json --output out.pdf" is executed
Then the process exits with code 1
And stderr contains the string "Error: data file not found: missing-data.json"
And nothing is written to stdout
And no file "out.pdf" is created
```

### Acceptance Criteria

- [ ] AC-CLI-04-01: Process exits with code 1 when the file at `--input` does not exist.
- [ ] AC-CLI-04-02: Stderr contains the string `Error: input file not found: <path>` where `<path>` matches the value passed to `--input`.
- [ ] AC-CLI-04-03: Process exits with code 1 when the file at `--data` does not exist.
- [ ] AC-CLI-04-04: Stderr contains the string `Error: data file not found: <path>` where `<path>` matches the value passed to `--data`.
- [ ] AC-CLI-04-05: Stdout is empty in all file-not-found error cases.
- [ ] AC-CLI-04-06: No output file is created when an input or data file is not found.

---

## STORY-CLI-05: Template variable missing from data file produces error with key name and exits 1

### Scenario: Template contains placeholder absent from data file

```gherkin
Given a file "invoice.html" exists containing "<h1>Invoice #{{invoiceNumber}}</h1><p>Client: {{clientName}}</p>"
And a file "partial-data.json" exists containing { "clientName": "Acme Corp" }
When "htmltopdf --input invoice.html --data partial-data.json --output invoice.pdf" is executed
Then the process exits with code 1
And stderr contains the string "invoiceNumber"
And stderr contains the string "Error"
And nothing is written to stdout
And no file "invoice.pdf" is created
```

### Scenario: Template has placeholder but no --data flag provided

```gherkin
Given a file "letter.html" exists containing "<p>Dear {{recipientName}},</p>"
When "htmltopdf --input letter.html --output letter.pdf" is executed (no --data flag)
Then the process exits with code 1
And stderr contains the string "recipientName"
And stderr contains the string "Error"
And nothing is written to stdout
And no file "letter.pdf" is created
```

### Acceptance Criteria

- [ ] AC-CLI-05-01: Process exits with code 1 when the library throws `MissingVariableError`.
- [ ] AC-CLI-05-02: Stderr contains the missing variable key name verbatim (e.g. `invoiceNumber`) when `MissingVariableError` is thrown.
- [ ] AC-CLI-05-03: Stderr contains the word `Error` as part of the error message.
- [ ] AC-CLI-05-04: Stdout is empty when a missing-variable error occurs.
- [ ] AC-CLI-05-05: No output PDF file is created when a missing-variable error occurs.

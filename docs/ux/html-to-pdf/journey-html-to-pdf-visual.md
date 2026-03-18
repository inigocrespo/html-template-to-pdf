# Journey Map: HTML Template to PDF

## Overview

Actor: Node.js developer (library consumer)
Goal: Convert an HTML template with optional dynamic variables into a PDF readable stream
Library: html-template-to-pdf (wraps openpdf-html)

---

## Journey Table

| Step | ID  | Name              | Actor     | Action                                      | System Response                                     | Emotion       | Feature Tag |
|------|-----|-------------------|-----------|---------------------------------------------|-----------------------------------------------------|---------------|-------------|
| 1    | S01 | Install           | Developer | Runs `npm install html-template-to-pdf`     | Package and openpdf-html peer dep installed         | Curious       | skeleton    |
| 2    | S02 | Import            | Developer | `const { htmlToPdf } = require('...')`      | Module resolves, no errors thrown                   | Ready         | skeleton    |
| 3    | S03 | Call API (plain)  | Developer | `htmlToPdf('<h1>Hello</h1>')`               | Validates input, starts rendering via openpdf-html  | Focused       | skeleton    |
| 4    | S04 | Receive Stream    | Developer | Awaits resolved Promise                     | Returns a Node.js Readable stream (PDF bytes)       | Relieved      | skeleton    |
| 5    | S05 | Consume Stream    | Developer | Pipes stream to `fs.createWriteStream(...)` | PDF file written to disk, stream ends cleanly       | Confident     | skeleton    |
| 6    | S06 | Call API (vars)   | Developer | `htmlToPdf(template, { name: 'Maria' })`    | Injects variables, renders, returns stream          | Empowered     | feature-1   |
| 7    | S07 | Verify Output     | Developer | Opens rendered PDF                          | Placeholders replaced with correct values           | Satisfied     | feature-1   |
| 8    | S08 | Handle Error      | Developer | Missing key in data object                  | Library rejects Promise with descriptive error      | Informed      | feature-1   |

---

## Emotional Arc

```
Curious -> Ready -> Focused -> Relieved -> Confident -> Empowered -> Satisfied
                                                                         |
                                                         (error path) Informed
```

The arc moves from uncertainty at install to growing confidence through the first successful render, then to empowerment once variable injection works. The error path lands on "Informed" rather than "Frustrated" -- the library gives enough context to self-correct without needing external help.

---

## ASCII Flow

```
Developer terminal / editor
        |
        v
  [S01: npm install]
  +---------------------------------+
  | npm install html-template-to-pdf|
  | >> added 12 packages            |
  +---------------------------------+
        |
        v
  [S02: Import module]
  +--------------------------------------------+
  | const { htmlToPdf } = require(             |
  |   'html-template-to-pdf'                   |
  | );                                          |
  +--------------------------------------------+
        |
        v
        +------ WALKING SKELETON BOUNDARY (Feature 0) ------+
        |                                                    |
        v                                                    |
  [S03: Call API -- plain HTML]                             |
  +--------------------------------------------+           |
  | htmlToPdf('<h1>Invoice</h1><p>...</p>')    |           |
  |          ^                                 |           |
  |          |-- htmlString (string)           |           |
  +--------------------------------------------+           |
        |                                                    |
        v                                                    |
  [openpdf-html render]                                     |
  +--------------------------------------------+           |
  | openpdf-html accepts HTML string            |           |
  | Produces PDF byte sequence                  |           |
  +--------------------------------------------+           |
        |                                                    |
        v                                                    |
  [S04: Receive stream]                                     |
  +--------------------------------------------+           |
  | Promise resolves --> Readable stream        |           |
  |   pdfStream (Node.js Readable)              |           |
  +--------------------------------------------+           |
        |                                                    |
        v                                                    |
  [S05: Consume stream]                                     |
  +--------------------------------------------+           |
  | pdfStream.pipe(                             |           |
  |   fs.createWriteStream('output.pdf')        |           |
  | );                                          |           |
  | // PDF file on disk                         |           |
  +--------------------------------------------+           |
        |                                                    |
        +---- END WALKING SKELETON --------------------------+
        |
        v
        +------ FEATURE 1 BOUNDARY (Variable Injection) ----+
        |                                                    |
        v                                                    |
  [S06: Call API -- with data object]                       |
  +--------------------------------------------+           |
  | const template =                            |           |
  |   '<h1>Hello {{name}}</h1>';               |           |
  | const data = { name: 'Maria Santos' };      |           |
  |                                             |           |
  | htmlToPdf(template, data)                   |           |
  |          ^         ^                        |           |
  |          |         |-- dataObject (object)  |           |
  |          |-- htmlString (string)            |           |
  +--------------------------------------------+           |
        |                                                    |
        v                                                    |
  [Variable injection engine]                               |
  +--------------------------------------------+           |
  | Scans template for {{key}} tokens           |           |
  | Replaces each with data[key]                |           |
  | Resolved HTML string passed to openpdf-html |           |
  +--------------------------------------------+           |
        |                         |                          |
   key found                 key missing                    |
        |                         |                          |
        v                         v                          |
  [S07: Verify output]    [S08: Handle error]               |
  +------------------+    +----------------------+          |
  | PDF with         |    | Promise rejects with |          |
  | "Hello Maria     |    | MissingVariableError |          |
  | Santos" visible  |    | { key: 'name',       |          |
  +------------------+    |   template: '...' }  |          |
                          +----------------------+          |
        |                                                    |
        +---- END FEATURE 1 ---------------------------------+
```

---

## Step-to-Feature Mapping Summary

| Feature Tag  | Steps Included       | Description                                      |
|--------------|----------------------|--------------------------------------------------|
| skeleton     | S01, S02, S03, S04, S05 | Install, import, plain HTML render, stream out |
| feature-1    | S06, S07, S08        | Variable injection, output verification, error   |

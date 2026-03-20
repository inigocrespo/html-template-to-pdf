# Journey Map: HTML Template to PDF -- CLI

## Overview

Actor: Developer (command-line user)
Goal: Convert an HTML template file, with optional JSON data, into a PDF file on disk using a single terminal command
Tool: htmltopdf CLI (wraps html-template-to-pdf Kotlin library)
Date: 2026-03-18

---

## Journey Table

| Step | ID  | Name                        | Actor     | Action                                                                                    | System Response                                                                 | Emotion     | Path    | Feature Tag |
|------|-----|-----------------------------|-----------|-------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|-------------|---------|-------------|
| 1    | S01 | Prepare files               | Developer | Has `template.html` and `data.json` ready in working directory                            | Files exist on disk; no system action                                           | Curious     | happy   | cli-core    |
| 2    | S02 | Run command                 | Developer | Runs `htmltopdf --input template.html --data data.json --output invoice.pdf`              | CLI parses flags; validates required flags present                               | Focused     | happy   | cli-core    |
| 3    | S03 | Read and validate inputs    | CLI       | Reads `template.html` from disk; reads and parses `data.json` into key-value map          | Files found and readable; JSON parsed to flat map; html string loaded into memory | Expectant   | happy   | cli-core    |
| 4    | S04 | Call library                | CLI       | Invokes `htmlToPdf(html, dataMap)` from the library                                       | Library injects variables, renders PDF, returns InputStream                     | Confident   | happy   | cli-core    |
| 5    | S05 | Write PDF to disk           | CLI       | Writes InputStream bytes to `invoice.pdf`                                                 | PDF file created at output path; stream closed cleanly                          | Accomplished| happy   | cli-core    |
| 6    | S06 | Silent success              | CLI       | Exits with code 0; no stdout output                                                       | Developer sees shell prompt return; output file present                          | Accomplished| happy   | cli-core    |
| E1   | SE1 | Missing required flag       | CLI       | Developer omits `--input` or `--output`                                                   | Prints usage error to stderr; exits with code 1                                 | Informed    | error   | cli-errors  |
| E2   | SE2 | Input file not found        | CLI       | `--input` path does not exist on disk                                                     | Prints "Error: input file not found: template.html" to stderr; exits code 1    | Informed    | error   | cli-errors  |
| E3   | SE3 | Data file not found         | CLI       | `--data` path does not exist on disk                                                      | Prints "Error: data file not found: data.json" to stderr; exits code 1         | Informed    | error   | cli-errors  |
| E4   | SE4 | Template variable missing   | CLI       | Template contains `{{key}}` with no matching key in data.json                             | Library throws MissingVariableError; CLI prints key name to stderr; exits code 1| Informed    | error   | cli-errors  |
| E5   | SE5 | Extra key in data (ignore)  | CLI       | data.json contains key not referenced in template                                         | Key silently ignored; PDF written successfully; exits code 0                    | Accomplished| happy   | cli-core    |

---

## Emotional Arc

```
Curious -> Focused -> Expectant -> Confident -> Accomplished
                                                     |
                               (error paths) -> Informed
```

The arc moves from curiosity (do I have the right files?) through focus (typing the command) to expectancy (waiting for render) and arrives at accomplished when the PDF appears silently on disk. Error paths land on Informed, not Frustrated: every failure message names the exact problem and the exact flag or file involved so the developer can self-correct without reading documentation.

---

## ASCII Flow

```
Developer terminal
        |
        v
  [S01: Files ready]
  +-----------------------------------------------+
  | $ ls                                          |
  | template.html  data.json                      |
  +-----------------------------------------------+
        |
        v
  [S02: Run CLI]
  +-----------------------------------------------+
  | $ htmltopdf \                                 |
  |     --input  template.html \                  |
  |     --data   data.json     \                  |
  |     --output invoice.pdf                      |
  |                                               |
  | CLI parses flags                              |
  |   htmlFilePath  <-- --input                   |
  |   jsonFilePath  <-- --data  (optional)        |
  |   outputPdfPath <-- --output                  |
  +-----------------------------------------------+
        |                    |
  flags valid          flag missing
        |                    |
        v                    v
  [S03: Read inputs]   [SE1: Usage error]
  +------------------+  +----------------------+
  | Read template.   |  | stderr:              |
  | html -> htmlStr  |  |   Error: --input is  |
  | Read data.json   |  |   required           |
  | -> dataMap       |  | exit 1               |
  +------------------+  +----------------------+
        |         |
   files found  file missing
        |         |
        v         v
  [S04: Call     [SE2/SE3: File not found]
  library]       +----------------------+
  +-----------+  | stderr:              |
  | htmlToPdf |  |   Error: input file  |
  | (html,    |  |   not found:         |
  | dataMap)  |  |   template.html      |
  +-----------+  | exit 1               |
        |        +----------------------+
   rendered |  MissingVariableError
        |         |
        v         v
  [S05: Write   [SE4: Missing variable]
  PDF]          +----------------------+
  +-----------+ | stderr:              |
  | Write     | |   Error: template    |
  | InputStream |   variable 'name'   |
  | -> invoice.pdf   not found in data |
  +-----------+ | exit 1               |
        |        +----------------------+
        v
  [S06: Silent success]
  +-----------------------------------------------+
  | $ _                  (prompt returns)         |
  | $ ls                                          |
  | template.html  data.json  invoice.pdf         |
  +-----------------------------------------------+
```

---

## Step-to-Feature Mapping Summary

| Feature Tag  | Steps Included              | Description                                                   |
|--------------|-----------------------------|---------------------------------------------------------------|
| cli-core     | S01, S02, S03, S04, S05, S06, SE5 | Happy path: flag parsing, file I/O, library call, PDF write |
| cli-errors   | SE1, SE2, SE3, SE4          | Error paths: missing flags, missing files, missing variables  |

# Requirements: htmltopdf CLI

Version: 1.0
Date: 2026-03-18
Author: Luna (nw-product-owner)
Journey artifact: docs/ux/cli/journey-cli.yaml
Feature epic: cli

---

## Functional Requirements

### FR-01: Accept --input (required), --data (optional), and --output (required) flags

The CLI MUST accept the following command-line flags:

| Flag       | Required | Description                                                         |
|------------|----------|---------------------------------------------------------------------|
| `--input`  | Yes      | Path to the HTML template file on disk                              |
| `--data`   | No       | Path to a JSON file containing key-value pairs for variable injection |
| `--output` | Yes      | Path where the output PDF file will be written                      |

If `--input` or `--output` is absent, the CLI MUST print a descriptive error to stderr (naming the missing flag) and exit with code 1. No file I/O is performed.

If `--data` is absent, the CLI proceeds with an empty data map (equivalent to passing `data = emptyMap()` to the library).

Derived from: journey steps S02, SE1.

---

### FR-02: Read HTML content from the --input file path

The CLI MUST read the entire contents of the file at the `--input` path as a UTF-8 string. The resulting string is passed to `htmlToPdf()` as the `html` argument.

If the file does not exist or cannot be read, the CLI MUST print an error to stderr in the form:

```
Error: input file not found: <path>
```

and exit with code 1.

Derived from: journey steps S03, SE2.

---

### FR-03: Parse JSON data from the --data file as a flat key-value object

When `--data` is provided, the CLI MUST:
1. Read the file at the `--data` path as a UTF-8 string.
2. Parse it as a JSON object whose top-level values are all strings.
3. Produce a `Map<String, String>` passed to `htmlToPdf()` as the `data` argument.

If the file does not exist or cannot be read, the CLI MUST print an error to stderr in the form:

```
Error: data file not found: <path>
```

and exit with code 1.

The JSON object MUST be a flat (non-nested) key-value structure. Nested objects and arrays at the top level are out of scope for this version. Extra keys (present in JSON but not referenced in the template) are silently ignored.

Derived from: journey steps S03, SE3, SE5.

---

### FR-04: Call htmlToPdf(html, data) from the library

The CLI MUST call `htmlToPdf(html = htmlString, data = dataMap)` using the html-template-to-pdf Kotlin library. The CLI MUST NOT perform its own HTML rendering or variable substitution -- those are exclusively the library's responsibility.

The CLI MUST NOT call the library if any pre-library validation has already failed (missing flags, missing files).

Derived from: journey step S04.

---

### FR-05: Write the resulting InputStream to the --output file path

The CLI MUST write all bytes from the `InputStream` returned by `htmlToPdf()` to the file at the `--output` path. The stream MUST be fully consumed and closed before the process exits. The output file is created if it does not exist, or overwritten if it does.

If writing fails (e.g. disk full, permission denied), the CLI MUST print a descriptive error to stderr and exit with code 1.

Derived from: journey step S05.

---

### FR-06: Exit code 0 on success, exit code 1 on any error

The CLI process MUST exit with code 0 if and only if:
- All flags are present and valid
- All referenced files are readable
- `htmlToPdf()` returns successfully
- The PDF is fully written to the output path

The CLI process MUST exit with code 1 for any other condition, including:
- Missing required flag
- File not found (input or data)
- `MissingVariableError` from the library
- `IllegalArgumentException` from the library
- Any I/O error during write

No other exit codes are used.

Derived from: journey steps S06, SE1–SE4.

---

### FR-07: Print a descriptive error message to stderr on failure

On any error, the CLI MUST:
1. Print a single-line error message to **stderr** (not stdout).
2. Name the specific cause (missing flag name, missing file path, missing variable key).
3. Exit with code 1.

Nothing MUST be printed to stdout at any point, including on success.

Error message formats:

| Condition                          | stderr message format                                              |
|------------------------------------|--------------------------------------------------------------------|
| `--input` flag absent              | `Error: --input is required`                                       |
| `--output` flag absent             | `Error: --output is required`                                      |
| Input file not found               | `Error: input file not found: <path>`                              |
| Data file not found                | `Error: data file not found: <path>`                               |
| Template variable missing          | `Error: template variable '<key>' not found in data file`          |
| Blank HTML string (library)        | `Error: input file is empty`                                       |

Derived from: journey steps SE1–SE4 and FR-06.

---

## Non-Functional Requirements

### NFR-01: Single executable JAR (fat JAR / shadow JAR)

The CLI MUST be distributed as a single self-contained JAR file that bundles all dependencies (including the html-template-to-pdf library and its renderer). Invocation: `java -jar htmltopdf.jar [flags]` or via a wrapper script named `htmltopdf`.

Rationale: A developer using the CLI should not need to manage a classpath or install separate JARs.

---

### NFR-02: Works on any JVM 21+ host

The CLI MUST run on any host with Java 21 or later installed. No other runtime dependency is required. The minimum JVM floor is 21, consistent with the host library (ADR-005).

---

### NFR-03: No output to stdout on success (silent success)

On a successful run the CLI MUST produce no output to stdout. The shell prompt returns immediately after the process exits. This is consistent with Unix tool conventions (clig.dev: "Only output what's necessary").

Rationale: Stdout silence allows the CLI to be composed in shell pipelines and scripts without output-filtering.

---

## Out of Scope (this version)

- Streaming from stdin (`--input -`)
- Multiple `--input` files
- Custom renderer selection
- Verbose / debug output mode
- Watch mode / live reload
- HTTP input URLs
- Non-flat (nested) JSON data structures

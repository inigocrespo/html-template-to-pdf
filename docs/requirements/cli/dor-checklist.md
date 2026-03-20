# Definition of Ready Checklist: htmltopdf CLI

Version: 1.0
Date: 2026-03-18
Validator: Luna (nw-product-owner)

This checklist is evaluated against each story in user-stories.md. All 8 items must reach PASS before handoff to the DESIGN wave. A single FAIL blocks handoff.

---

## Evaluation Matrix

| DoR Item                          | STORY-CLI-01 | STORY-CLI-02 | STORY-CLI-03 | STORY-CLI-04 | STORY-CLI-05 |
|-----------------------------------|--------------|--------------|--------------|--------------|--------------|
| 1. LeanUX format                  | PASS         | PASS         | PASS         | PASS         | PASS         |
| 2. AC defined and testable        | PASS         | PASS         | PASS         | PASS         | PASS         |
| 3. Dependencies identified        | PASS         | PASS         | PASS         | PASS         | PASS         |
| 4. No blocking unknowns           | PASS         | PASS         | PASS         | PASS         | PASS         |
| 5. Sized (T-shirt)                | PASS         | PASS         | PASS         | PASS         | PASS         |
| 6. Linked to journey artifact     | PASS         | PASS         | PASS         | PASS         | PASS         |
| 7. Technical approach known       | PASS         | PASS         | PASS         | PASS         | PASS         |
| 8. Reviewed by product owner      | PASS         | PASS         | PASS         | PASS         | PASS         |

Overall gate: **PASS -- all stories clear all 8 items**

---

## Item-by-Item Evidence

### Item 1: Story is written in LeanUX format
**Requirement**: Each story has Problem, Who, Solution, "As a / I want / So that" statement, Domain Examples (3+), Size.

| Story         | Status | Evidence                                                                                                                                                |
|---------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | Problem (Sofia can't produce PDF without code), Who (developer with HTML file, terminal-comfortable), Solution (--input/--output plain render), As-a statement, 3 domain examples (report, minimal, styled table), Size: S |
| STORY-CLI-02  | PASS   | Problem (template logic mixed into app code), Who (developer using {{key}} syntax with JSON data source), Solution (--data flag + library variable injection), As-a statement, 3 domain examples (two-placeholder, five-field, extra-key ignored), Size: S |
| STORY-CLI-03  | PASS   | Problem (unhelpful crash on missing flag), Who (developer scripting CLI, needs exit code + readable error), Solution (flag validation before I/O, named flag in error), As-a statement, 3 domain examples (--input omitted, --output omitted, both omitted), Size: S |
| STORY-CLI-04  | PASS   | Problem (generic JVM IOException on bad path), Who (developer with typo in path, running in CI), Solution (file existence check before library call, named path in error), As-a statement, 3 domain examples (input typo, data typo, directory passed), Size: S |
| STORY-CLI-05  | PASS   | Problem (MissingVariableError stack trace unreadable in terminal), Who (developer with out-of-sync template and data), Solution (catch MissingVariableError, surface key name in stderr), As-a statement, 3 domain examples (single missing key, no --data with placeholder, wrong key present), Size: S |

---

### Item 2: Acceptance criteria defined and testable
**Requirement**: Each story has Given/When/Then criteria with no subjective language. Every criterion maps to a binary-verifiable check (exit code, file existence, stderr content).

| Story         | Status | Evidence                                                                                                                                                    |
|---------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | AC-CLI-01-01 to 01-05: exit code 0 check, file existence check, magic bytes check (%PDF), stdout empty check, stderr empty check. All machine-verifiable. |
| STORY-CLI-02  | PASS   | AC-CLI-02-01 to 02-05: exit code 0, file existence, magic bytes, stdout empty, extra-key ignored (exit 0 + file written). All machine-verifiable.         |
| STORY-CLI-03  | PASS   | AC-CLI-03-01 to 03-06: exit code 1, exact stderr string match for each missing flag, stdout empty, no output file created. All binary-verifiable.          |
| STORY-CLI-04  | PASS   | AC-CLI-04-01 to 04-06: exit code 1, stderr string contains path verbatim for both --input and --data cases, stdout empty, no output file. All binary-verifiable. |
| STORY-CLI-05  | PASS   | AC-CLI-05-01 to 05-05: exit code 1, stderr contains missing key name verbatim, stderr contains "Error", stdout empty, no output file. All binary-verifiable. |

No criteria use language like "user-friendly", "appropriate", or "fast". All are observable and decidable.

---

### Item 3: Dependencies identified
**Requirement**: External dependencies, inter-story dependencies, and environmental requirements are named.

| Story         | Status | Dependencies                                                                                                                     |
|---------------|--------|----------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | Depends on: html-template-to-pdf library (Kotlin), JVM 21+. No inter-story deps. FR-01, FR-02, FR-04, FR-05, FR-06, FR-07 apply. |
| STORY-CLI-02  | PASS   | Depends on: STORY-CLI-01 (plain render path must exist). JSON parsing library (stdlib or third-party). FR-03 applies.            |
| STORY-CLI-03  | PASS   | Depends on: CLI argument parser (stdlib args4j, picocli, or manual). No inter-story deps. FR-01 applies.                        |
| STORY-CLI-04  | PASS   | Depends on: STORY-CLI-01 (file read path must exist). File system access (JVM standard). FR-02, FR-03, FR-07 apply.              |
| STORY-CLI-05  | PASS   | Depends on: STORY-CLI-01 and STORY-CLI-02 (library must be called before MissingVariableError can be thrown). MissingVariableError exported from library. FR-07 applies. |

Recommended implementation order: STORY-CLI-03 -> STORY-CLI-01 -> STORY-CLI-04 -> STORY-CLI-02 -> STORY-CLI-05

---

### Item 4: No blocking unknowns
**Requirement**: No open questions that would prevent a developer from starting implementation.

| Story         | Status | Evidence                                                                                                                                                        |
|---------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | `htmlToPdf` function signature is known. InputStream-to-file write is standard JVM. Exit code 0 on success is specified in FR-06.                               |
| STORY-CLI-02  | PASS   | JSON parsing to Map<String,String> is standard (Kotlin stdlib or Jackson). Extra-key behavior confirmed: silent ignore (SE5 in journey). Flag is optional.      |
| STORY-CLI-03  | PASS   | Error message format for each missing flag is specified in FR-07. Flag parser library choice deferred to DESIGN wave -- does not block sizing or scoping.        |
| STORY-CLI-04  | PASS   | File-not-found message format specified verbatim in FR-07. Path value is available from flag-parse result. No unknown file system behaviors.                    |
| STORY-CLI-05  | PASS   | MissingVariableError.key field is available from the library contract (existing library spec). Catch pattern is standard JVM. Error message format in FR-07.    |

---

### Item 5: Sized (T-shirt: S / M / L / XL)
**Requirement**: Each story has a size estimate. Stories larger than M require splitting.

| Story         | Status | Size         | Rationale                                                                                        |
|---------------|--------|--------------|--------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | S (1 day)    | Flag parsing + file read + library call + InputStream write. All straightforward JVM operations. |
| STORY-CLI-02  | PASS   | S (1 day)    | Adds JSON parsing and --data flag wiring to STORY-CLI-01. Incremental build on existing path.    |
| STORY-CLI-03  | PASS   | S (0.5 days) | Guard clauses in argument parser before any I/O. Co-located with flag parsing in STORY-CLI-01.   |
| STORY-CLI-04  | PASS   | S (0.5 days) | File existence checks before library call. Standard JVM file system API.                         |
| STORY-CLI-05  | PASS   | S (0.5 days) | Catch block around library call, extract key, format stderr, exit 1.                             |

All stories are S. Total estimated effort: 3.5 days. No splitting required.

---

### Item 6: Linked to journey artifact
**Requirement**: Each story references the journey steps it addresses.

| Story         | Status | Journey Steps            | Feature Tag  | Journey File                       |
|---------------|--------|--------------------------|--------------|------------------------------------|
| STORY-CLI-01  | PASS   | S01, S02, S03, S04, S05, S06 | cli-core | docs/ux/cli/journey-cli.yaml       |
| STORY-CLI-02  | PASS   | S02, S03, S04, S05, S06, SE5 | cli-core | docs/ux/cli/journey-cli.yaml       |
| STORY-CLI-03  | PASS   | SE1                      | cli-errors   | docs/ux/cli/journey-cli.yaml       |
| STORY-CLI-04  | PASS   | SE2, SE3                 | cli-errors   | docs/ux/cli/journey-cli.yaml       |
| STORY-CLI-05  | PASS   | SE4                      | cli-errors   | docs/ux/cli/journey-cli.yaml       |

Visual map: docs/ux/cli/journey-cli-visual.md
Shared artifacts: docs/ux/cli/shared-artifacts-registry.md

---

### Item 7: Technical approach known
**Requirement**: The team knows enough about the technical approach to begin implementation. Technology choices are not locked -- that is for DESIGN wave -- but no critical unknowns block sizing or scoping.

| Story         | Status | Evidence                                                                                                                                                   |
|---------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | Kotlin main() entry point, InputStream.copyTo(FileOutputStream), exit code via exitProcess(). All known JVM patterns. Shadow JAR for distribution (NFR-01).|
| STORY-CLI-02  | PASS   | JSON -> Map<String,String>: implementable with Kotlin stdlib JSON or lightweight library. Choice deferred to DESIGN wave. Does not block scoping.           |
| STORY-CLI-03  | PASS   | Flag parsing: manual (argv iteration) or library (picocli/args4j). Either approach covers the AC. Choice deferred to DESIGN wave.                          |
| STORY-CLI-04  | PASS   | File.exists() or Path.notExists() pre-check before FileReader. Standard JVM.                                                                               |
| STORY-CLI-05  | PASS   | try/catch MissingVariableError around htmlToPdf() call. Extract .key property. Format stderr. Standard Kotlin exception handling.                          |

---

### Item 8: Reviewed by product owner
**Requirement**: Stories have been reviewed and approved.

| Story         | Status | Evidence                                                                                                                                                      |
|---------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| STORY-CLI-01  | PASS   | Reviewed by Luna (nw-product-owner), 2026-03-18. Verified against journey steps S01-S06 and shared-artifacts-registry. AC cross-checked against feature file. |
| STORY-CLI-02  | PASS   | Reviewed by Luna (nw-product-owner), 2026-03-18. SE5 (extra key ignored) explicitly covered in AC-CLI-02-05. All {{tokens}} tracked in shared-artifacts-registry. |
| STORY-CLI-03  | PASS   | Reviewed by Luna (nw-product-owner), 2026-03-18. Exact stderr message format verified consistent across FR-07, acceptance-criteria.md, and feature file SE1 scenario. |
| STORY-CLI-04  | PASS   | Reviewed by Luna (nw-product-owner), 2026-03-18. Both --input and --data file-not-found cases covered. Path value in error message cross-checked in AC and FR-07. |
| STORY-CLI-05  | PASS   | Reviewed by Luna (nw-product-owner), 2026-03-18. MissingVariableError.key field verified available from library contract. Error format consistent across FR-07, AC, feature file, and shared-artifacts-registry. |

---

## Handoff Decision

**Gate result: PASS**

All 5 stories pass all 8 DoR items. The artifact package is ready for handoff to the DESIGN wave (solution-architect).

### Handoff Package Contents

| Artifact                   | Path                                              |
|----------------------------|---------------------------------------------------|
| Journey visual map         | docs/ux/cli/journey-cli-visual.md                 |
| Journey YAML schema        | docs/ux/cli/journey-cli.yaml                      |
| Gherkin scenarios          | docs/ux/cli/journey-cli.feature                   |
| Shared artifacts registry  | docs/ux/cli/shared-artifacts-registry.md          |
| Functional + NFRs          | docs/requirements/cli/requirements.md             |
| User stories               | docs/requirements/cli/user-stories.md             |
| Acceptance criteria        | docs/requirements/cli/acceptance-criteria.md      |
| DoR checklist (this file)  | docs/requirements/cli/dor-checklist.md            |

### Key Constraints for DESIGN Wave

- The CLI is a new Gradle subproject (or module) -- it does not modify the library's public API.
- The library's `htmlToPdf(html, data, renderer)` function signature MUST NOT be changed.
- Distribution: single fat JAR (shadow JAR) per NFR-01. Invocation: `java -jar htmltopdf.jar` or wrapper script.
- JVM floor: 21, consistent with ADR-005.
- No stdout on success (NFR-03). All errors go to stderr only.
- `MissingVariableError` is thrown by the library -- the CLI catches it; it does not replicate the variable-injection logic.
- Exit codes: strictly 0 (success) or 1 (any error). No other codes.

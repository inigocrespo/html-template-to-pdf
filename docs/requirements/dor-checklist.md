# Definition of Ready Checklist

Project: html-template-to-pdf
Date: 2026-03-18
Validator: Luna (nw-product-owner)

This checklist is evaluated against each story in user-stories.md. All 8 items must reach PASS before handoff to the DESIGN wave. A single FAIL blocks handoff.

---

## Evaluation Matrix

| DoR Item | STORY-00 | STORY-01 | STORY-02 | STORY-03 | STORY-04 |
|----------|----------|----------|----------|----------|----------|
| 1. LeanUX format | PASS | PASS | PASS | PASS | PASS |
| 2. AC defined and testable | PASS | PASS | PASS | PASS | PASS |
| 3. Dependencies identified | PASS | PASS | PASS | PASS | PASS |
| 4. No blocking unknowns | PASS | PASS | PASS | PASS | PASS |
| 5. Sized (T-shirt) | PASS | PASS | PASS | PASS | PASS |
| 6. Linked to journey artifact | PASS | PASS | PASS | PASS | PASS |
| 7. Technical approach known | PASS | PASS | PASS | PASS | PASS |
| 8. Reviewed by product owner | PASS | PASS | PASS | PASS | PASS |

Overall gate: **PASS -- all stories clear all 8 items**

---

## Item-by-Item Evidence

### Item 1: Story is written in LeanUX format
**Requirement**: Each story has Problem, Who, Solution, "As a / I want / So that" statement, Domain Examples (3+), Size.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | Problem (Marco building invoicing service), Who (first-time integrator), Solution (skeleton pipeline), As-a statement, 3 domain examples, Size: S |
| STORY-01 | PASS | Problem (static PDF without boilerplate), Who (static content developer), Solution (validated htmlToPdf), As-a statement, 3 domain examples, Size: S |
| STORY-02 | PASS | Problem (manual string concat tedium), Who (personalized PDF developer), Solution ({{key}} injection), As-a statement, 3 domain examples, Size: S |
| STORY-03 | PASS | Problem (untyped errors from openpdf-html), Who (developer who made a mistake), Solution (input validation at boundary), As-a statement, 3 domain examples, Size: S |
| STORY-04 | PASS | Problem (silent {{placeholder}} in output), Who (developer with out-of-sync template), Solution (fail-fast MissingVariableError), As-a statement, 3 domain examples, Size: S |

---

### Item 2: Acceptance criteria defined and testable
**Requirement**: Each story has Given/When/Then criteria with no subjective language. Every criterion maps to a runnable test.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | AC-00-01 through AC-00-04: stream.Readable instanceof check, Buffer magic bytes check, end-event check, named export check. All are binary-pass/fail. |
| STORY-01 | PASS | AC-01-01 through AC-01-05: TypeError instanceof, message substring, no synchronous throw, file creation. All machine-verifiable. |
| STORY-02 | PASS | AC-02-01 through AC-02-05: PDF text content checks, placeholder absence checks, extra-key silent ignore, omitted arg behavior. All testable via PDF text extraction. |
| STORY-03 | PASS | AC-03-01 through AC-03-05: TypeError for number/undefined/null/empty, openpdf-html error propagation via mock. All testable. |
| STORY-04 | PASS | AC-04-01 through AC-04-05: MissingVariableError instanceof, key/template properties, renderer not called (spy), TypeError for non-object. All testable. |

No criteria use language like "should feel", "easy to use", or "appropriate". All criteria are observable.

---

### Item 3: Dependencies identified
**Requirement**: External dependencies, inter-story dependencies, and environmental requirements are named.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | Depends on: openpdf-html (npm package), Node.js 18+. No inter-story deps. |
| STORY-01 | PASS | Depends on: STORY-00 (skeleton establishes the pipeline). openpdf-html. |
| STORY-02 | PASS | Depends on: STORY-01 (plain render path must exist before injection). No new external deps. |
| STORY-03 | PASS | Depends on: STORY-00 or STORY-01 (input validation co-located with API entry point). No new external deps. |
| STORY-04 | PASS | Depends on: STORY-02 (variable injection path must exist to be validated). MissingVariableError export. |

Implementation order: STORY-00 -> STORY-01 -> STORY-03 -> STORY-02 -> STORY-04

---

### Item 4: No blocking unknowns
**Requirement**: No open questions that would prevent a developer from starting implementation.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | openpdf-html API is known (accepts HTML string, produces PDF output). Stream wrapping pattern is standard Node.js. |
| STORY-01 | PASS | Validation pattern (typeof check + empty string check) is straightforward. No unknown openpdf-html behavior. |
| STORY-02 | PASS | Token syntax `{{key}}` is specified. Replacement strategy (scan-then-replace, all occurrences) is specified. Non-string value coercion (.toString()) is specified. |
| STORY-03 | PASS | Error types (TypeError) and message format are specified. openpdf-html error propagation strategy (re-throw) is specified. |
| STORY-04 | PASS | MissingVariableError shape (key, template, message) is fully specified. Pre-render validation order is specified. |

Note: The exact openpdf-html integration API (function signature, stream vs buffer return) should be confirmed during DESIGN wave spike. This is not blocking for requirements -- the integration contract is captured in FR-03 and NFR-02.

---

### Item 5: Sized (T-shirt: S / M / L / XL)
**Requirement**: Each story has a size estimate. Stories larger than M require splitting.

| Story | Status | Size | Rationale |
|-------|--------|------|-----------|
| STORY-00 | PASS | S (1 day) | Thin pipeline: install, export, pass-through to openpdf-html, return stream. |
| STORY-01 | PASS | S (1 day) | Input validation + openpdf-html call. Builds directly on STORY-00. |
| STORY-02 | PASS | S (1 day) | Regex scan + replace loop. Well-understood algorithm. |
| STORY-03 | PASS | S (0.5 days) | Guard clauses at function entry. Co-located with STORY-01 implementation. |
| STORY-04 | PASS | S (0.5 days) | Pre-render token scan + custom Error subclass. Co-located with STORY-02. |

All stories are S. No splitting required.

---

### Item 6: Linked to journey artifact
**Requirement**: Each story references the journey steps it addresses.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | Journey steps: S01, S02, S03, S04, S05. Feature tag: skeleton. |
| STORY-01 | PASS | Journey steps: S03, S04, S05. Feature tag: skeleton. |
| STORY-02 | PASS | Journey steps: S06, S07. Feature tag: feature-1. |
| STORY-03 | PASS | Journey step: S03 (input validation at API entry). Feature tag: skeleton. |
| STORY-04 | PASS | Journey step: S08 (MissingVariableError). Feature tag: feature-1. |

Journey file: docs/ux/html-to-pdf/journey-html-to-pdf.yaml
Visual map: docs/ux/html-to-pdf/journey-html-to-pdf-visual.md

---

### Item 7: Technical approach known (openpdf-html)
**Requirement**: The team knows enough about the technical approach to begin implementation. Technology choices are not locked -- that is for DESIGN wave -- but no critical unknowns block sizing or scoping.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | openpdf-html is specified as the renderer. Stream wrapping is a known Node.js pattern. |
| STORY-01 | PASS | TypeError validation is language-native. No unknown APIs required. |
| STORY-02 | PASS | `{{key}}` pattern is implementable with regex or simple string scan. No third-party library required. |
| STORY-03 | PASS | Error wrapping is language-native. openpdf-html error shape may need a lightweight spike in DESIGN wave but does not block sizing. |
| STORY-04 | PASS | Custom Error subclass is standard JavaScript. MissingVariableError properties are fully specified. |

Recommended spike for DESIGN wave: confirm openpdf-html's exact function signature and whether it returns a stream natively or requires wrapping. Estimated effort: 2 hours. Not blocking requirements.

---

### Item 8: Reviewed by product owner
**Requirement**: Stories have been reviewed and approved.

| Story | Status | Evidence |
|-------|--------|----------|
| STORY-00 | PASS | Reviewed by Luna (nw-product-owner), 2026-03-18. Validated against journey artifacts and shared-artifacts-registry. |
| STORY-01 | PASS | Reviewed by Luna (nw-product-owner), 2026-03-18. AC verified testable. |
| STORY-02 | PASS | Reviewed by Luna (nw-product-owner), 2026-03-18. All {{tokens}} registered in shared-artifacts-registry. |
| STORY-03 | PASS | Reviewed by Luna (nw-product-owner), 2026-03-18. Error scenarios cross-checked against Gherkin feature file. |
| STORY-04 | PASS | Reviewed by Luna (nw-product-owner), 2026-03-18. MissingVariableError contract verified consistent across requirements.md, user-stories.md, acceptance-criteria.md, and journey YAML. |

---

## Handoff Decision

**Gate result: PASS**

All 5 stories pass all 8 DoR items. The artifact package is ready for handoff to the DESIGN wave (solution-architect).

### Handoff Package Contents

| Artifact | Path |
|----------|------|
| Journey visual map | docs/ux/html-to-pdf/journey-html-to-pdf-visual.md |
| Journey YAML schema | docs/ux/html-to-pdf/journey-html-to-pdf.yaml |
| Gherkin scenarios | docs/ux/html-to-pdf/journey-html-to-pdf.feature |
| Shared artifacts registry | docs/ux/html-to-pdf/shared-artifacts-registry.md |
| Functional + NFRs | docs/requirements/requirements.md |
| User stories | docs/requirements/user-stories.md |
| Acceptance criteria | docs/requirements/acceptance-criteria.md |
| DoR checklist (this file) | docs/requirements/dor-checklist.md |

### Key Constraints for DESIGN Wave
- Library only (no CLI, no HTTP server)
- Renderer: openpdf-html (do not substitute)
- Public API: named export `htmlToPdf(htmlString, dataObject?)`
- Return type: `Promise<stream.Readable>` -- no sync API, no callback API
- No filesystem writes, no network calls
- Node.js 18, 20, 22 compatibility required
- `MissingVariableError` must be exported for instanceof checks

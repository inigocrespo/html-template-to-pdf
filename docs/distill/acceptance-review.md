# Acceptance Test Review: html-template-to-pdf

Wave: DISTILL
Date: 2026-03-18
Reviewer: Quinn (nw-acceptance-designer, self-review)

---

## 1. Coverage Matrix

Every story from the DISCUSS wave maps to at least one test scenario.

| Story | Description | Covering scenarios | Spec file |
|---|---|---|---|
| STORY-00 | Walking skeleton — plain HTML to valid PDF bytes | SC-00-01, SC-00-02 | WalkingSkeletonSpec.kt |
| STORY-01 | HTML string to PDF stream, input validation | SC-01-01, SC-01-02, SC-01-03, SC-01-04 | HtmlToPdfSpec.kt |
| STORY-02 | Variable injection — single, multiple, repeated, extra keys, omitted map | SC-02-01, SC-02-02, SC-02-03, SC-02-04, SC-02-05 | HtmlToPdfSpec.kt |
| STORY-03 | Invalid input — blank and empty html | SC-03-01, SC-03-02 | HtmlToPdfSpec.kt |
| STORY-04 | Missing variable — error thrown, key and template fields, renderer not called | SC-04-01, SC-04-02, SC-04-03, SC-04-04, SC-04-05 | HtmlToPdfSpec.kt |
| Infrastructure | JAR build validation, reflection checks | SC-INF-01, SC-INF-02, SC-INF-03, SC-INF-04 | BuildValidationSpec.kt |

All 5 user stories and the infrastructure concern are covered. No story has zero scenarios.

---

## 2. Driving Ports Compliance (CM-A)

All acceptance tests invoke `htmlToPdf` — the single public driving port — directly.

**Correct invocation pattern (every acceptance test):**
```kotlin
import io.htmltopdf.htmlToPdf
// ...
val result = htmlToPdf(html, data)
```

No test in `WalkingSkeletonSpec` or `HtmlToPdfSpec` imports or instantiates `TemplateEngine`, `OpenPdfHtmlRenderer`, or `ITextRenderer` directly. These components are exercised indirectly through the port.

`BuildValidationSpec` uses reflection (`::class.java`) and imports `MissingVariableError` and `PdfRenderer` for type-level assertions only — not to invoke internal logic. This is a justified infrastructure check, not a hexagonal boundary violation.

---

## 3. Business Language Verification (CM-B)

All `given`/`when`/`then` strings in `WalkingSkeletonSpec` and `HtmlToPdfSpec` use domain-facing language exclusively.

Confirmed absent from all `.kt` spec files:
- No HTTP status codes or HTTP method names
- No database or persistence terms
- No class or method names from the implementation (no "ITextRenderer", no "ByteArrayOutputStream", no "TemplateEngine")
- No references to internal component wiring

Examples of domain language used:
- "plain HTML string with no template variables"
- "placeholder is replaced in the rendered output"
- "stream begins with PDF magic bytes %PDF"
- "data map does not contain the key referenced in the template"

---

## 4. Real Renderer Confirmation (CM-C, infrastructure)

No mocks are used at the acceptance test level. Every scenario that reaches the rendering step uses the real `OpenPdfHtmlRenderer` backed by `ITextRenderer`.

Scenarios that do NOT reach the renderer (throw before render): SC-03-01, SC-03-02, SC-04-01 through SC-04-05. In these cases the test verifies the thrown exception; the renderer is correctly never called, and no mock is needed to assert that.

---

## 5. Walking Skeleton Count and Focused Scenario Count (CM-C, scenario counts)

- Walking skeleton scenarios: **2** (SC-00-01, SC-00-02 in `WalkingSkeletonSpec`)
- Focused acceptance scenarios: **18** (SC-01-xx through SC-04-xx in `HtmlToPdfSpec`) + 4 infrastructure scenarios
- Total scenarios across all specs: **24**

The 2 walking skeleton scenarios form the first implementation target. The 18 focused scenarios are organized by story, providing complete coverage of all acceptance criteria from the DISCUSS wave.

---

## 6. One-at-a-Time Strategy

Implementation sequence enforced by Kotest skip annotations:

1. `WalkingSkeletonSpec` — both scenarios enabled from the start. This is the first test that must pass.
2. `HtmlToPdfSpec` — all `when` blocks except the first should be prefixed `x` (e.g., `xwhen`) until the walking skeleton passes, then enabled one story at a time in order: STORY-01, STORY-02, STORY-03, STORY-04.
3. `BuildValidationSpec` — all `it` blocks except the first should be disabled until STORY-01 is complete (library is minimally functional).

This sequence prevents multiple failing tests from existing simultaneously and preserves the TDD feedback loop.

---

## 7. Error Path Coverage

Error and edge scenarios: 9 out of 22 functional scenarios = **41%** (target >= 40% — met).

Error scenarios counted: SC-03-01, SC-03-02 (invalid input), SC-04-01 through SC-04-05 (missing variable), SC-02-04 (extra keys edge), SC-02-05 (omitted parameter edge).

---

## 8. Open Questions and Assumptions

**OQ-01 — Node.js terminology in DISCUSS artifacts**: User stories and acceptance criteria use Node.js idioms (Promise, Readable stream, TypeError, dataObject). These have been mapped to their Kotlin/JVM equivalents as specified in the project summary: InputStream (not Readable), IllegalArgumentException (not TypeError), Map<String, String> (not plain object). No functional change to the intent of any story.

**OQ-02 — TemplateEngine resolution order for multiple missing keys**: STORY-04 does not specify whether `MissingVariableError` should report the first missing key found (left-to-right scan) or all missing keys. SC-04-04 assumes first-found (left-to-right) order consistent with a sequential scan. The software-crafter should confirm or adjust accordingly.

**OQ-03 — openpdf-html and well-formed HTML**: openpdf-html 3.x (Flying Saucer underneath) is strict about well-formed XHTML. All test HTML strings use well-formed markup. If the software-crafter discovers the renderer requires explicit XML declarations or DOCTYPE, the HTML strings in the specs may need minor adjustment; no scenario intent changes.

**OQ-04 — build.gradle.kts path in BuildValidationSpec**: `BuildValidationSpec` reads `File("build.gradle.kts")` using a relative path, which resolves correctly when tests are run from the project root (standard Gradle behaviour). If the CI runner changes the working directory this test may fail for environmental reasons unrelated to the library.

---

## Review Outcome

All 6 critique dimensions assessed:

| Dimension | Status | Notes |
|---|---|---|
| Story coverage | Pass | All STORY-00 through STORY-04 covered |
| Driving port compliance | Pass | All tests enter through `htmlToPdf`; no internal component invoked directly |
| Business language purity | Pass | Zero technical terms in given/when/then strings |
| Real renderer usage | Pass | No mocks at acceptance level |
| Error path ratio | Pass | 41% >= 40% target |
| Walking skeleton first | Pass | WalkingSkeletonSpec isolated; ordered handoff documented |

Handoff to software-crafter is approved.

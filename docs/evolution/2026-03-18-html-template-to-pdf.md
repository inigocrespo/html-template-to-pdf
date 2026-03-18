# Evolution: html-template-to-pdf

**Date:** 2026-03-18
**Status:** COMPLETE
**Feature:** html-template-to-pdf

---

## Feature Summary

A Kotlin/JVM library that converts HTML templates to PDF byte streams. The library accepts an HTML string with optional `{{key}}` placeholder tokens and a data map, substitutes variables before rendering, and returns an `InputStream` whose bytes are a valid PDF document. It is published as a Kotlin JVM library targeting Java 21.

---

## Phases Completed

| Phase | Name | Steps |
|-------|------|-------|
| 01 | Project Scaffolding | 01-01: Gradle build files and package directory structure |
| 02 | Walking Skeleton | 02-01: PdfRenderer interface and OpenPdfHtmlRenderer adapter |
| | | 02-02: htmlToPdf public API function (plain HTML) |
| 03 | Variable Injection | 03-01: MissingVariableError custom exception |
| | | 03-02: TemplateEngine with {{key}} substitution and fail-fast validation |
| | | 03-03: Integrate TemplateEngine into htmlToPdf — full acceptance suite passes |

**Total:** 3 phases, 6 steps.

---

## Execution Stats

| Metric | Value |
|--------|-------|
| Steps executed | 6 of 6 |
| TDD phases run (EXECUTED) | 23 |
| TDD phases skipped (NOT_APPLICABLE) | 4 |
| Estimated hours (roadmap) | 7.0 h |
| Execution date | 2026-03-18 |

TDD phase breakdown per step:

| Step | PREPARE | RED_ACCEPTANCE | RED_UNIT | GREEN | COMMIT |
|------|---------|---------------|----------|-------|--------|
| 01-01 | PASS | PASS | SKIPPED (no unit-testable logic in build config) | PASS | PASS |
| 02-01 | PASS | SKIPPED (public API not yet present) | PASS | PASS | PASS |
| 02-02 | PASS | PASS | SKIPPED (WalkingSkeletonSpec covers both) | PASS | PASS |
| 03-01 | PASS | PASS | PASS | PASS | PASS |
| 03-02 | PASS | PASS | PASS | PASS | PASS |
| 03-03 | PASS | PASS | SKIPPED (HtmlToPdfSpec covers integration completely) | PASS | PASS |

---

## Architecture Decisions

| ADR | Decision | Rationale |
|-----|----------|-----------|
| Language and runtime | Kotlin on JVM (Java 21) | Target Java ecosystem; openpdf-html 3.0.3 is a JVM library; Kotlin null safety reduces defensive coding overhead |
| Structural pattern | Ports-and-adapters (hexagonal) | `PdfRenderer` is the port; `OpenPdfHtmlRenderer` is the driven adapter; business logic stays independent of rendering library details |
| Return type | `InputStream` | Avoids loading the full PDF into memory; composable with standard Java IO; aligns with idiomatic JVM streaming conventions |
| Error strategy | Fail-fast validation | `TemplateEngine` scans for unresolved `{{key}}` tokens and throws `MissingVariableError` before any rendering occurs; prevents silent partial output |
| Java version | 21 (LTS) | Long-term support, modern sealed classes available, aligns with openpdf-html 3.0.3 minimum requirements |

---

## Components Delivered

| Component | Type | Location | Role |
|-----------|------|----------|------|
| `PdfRenderer` | Interface (port) | `src/main/kotlin/io/htmltopdf/PdfRenderer.kt` | Defines `render(html: String): InputStream` contract |
| `OpenPdfHtmlRenderer` | Class (adapter) | `src/main/kotlin/io/htmltopdf/OpenPdfHtmlRenderer.kt` | Implements `PdfRenderer` using `ITextRenderer` from openpdf-html 3.0.3 |
| `TemplateEngine` | Class | `src/main/kotlin/io/htmltopdf/TemplateEngine.kt` | Replaces `{{key}}` tokens; throws `MissingVariableError` on missing keys |
| `MissingVariableError` | Exception | `src/main/kotlin/io/htmltopdf/MissingVariableError.kt` | `RuntimeException` subclass; exposes `key` and `template` properties |
| `htmlToPdf` | Top-level function (public API) | `src/main/kotlin/io/htmltopdf/HtmlToPdf.kt` | Entry point: validates input, runs `TemplateEngine`, delegates to injected `PdfRenderer` |

---

## Quality Gates Passed

| Gate | Result | Detail |
|------|--------|--------|
| Roadmap review | APPROVED | Reviewer approved plan before implementation began |
| Adversarial review | APPROVED (1 revision) | Revision added renderer injection parameter to `HtmlToPdf.kt` to enable testability; re-review passed |
| Mutation testing | 6/6 killed (100%) | All surviving mutants eliminated after renderer injection enabled direct unit coverage |
| Integrity verification | PASSED | All source files present, all tests green, build reproducible |

---

## Test Suite

| Spec / Test class | Scope | Key assertions |
|-------------------|-------|----------------|
| `BuildValidationSpec` | Acceptance (Phase 01) | Gradle build resolves and compiles on an empty source tree |
| `WalkingSkeletonSpec` | Acceptance (Phase 02) | `htmlToPdf("<html>...</html>")` returns `InputStream` starting with `%PDF`; blank input throws `IllegalArgumentException` |
| `HtmlToPdfSpec` | Acceptance (Phase 03) | Variable injection produces valid PDF; missing key throws `MissingVariableError`; blank HTML throws `IllegalArgumentException` regardless of data map |
| `TemplateEngineTest` | Unit | All `{{key}}` tokens replaced; absent key throws `MissingVariableError`; no-token passthrough; fail-fast ordering |
| `MissingVariableErrorTest` | Unit | `key` and `template` properties correct; `message` contains key name and "missing" indicator |

---

## Lessons Learned

**Platform translation during acceptance review.** The acceptance criteria in the original specification were written for a Node.js implementation (CommonJS exports, Buffer return type, Jest test syntax). The Kotlin implementation required mapping every criterion to its JVM equivalent (`InputStream`, Kotest/JUnit, Gradle) during the adversarial review phase. This added one revision cycle. Future features should confirm the target platform before acceptance criteria are authored.

**Renderer injection unlocks mutation coverage.** The initial `HtmlToPdf.kt` instantiated `OpenPdfHtmlRenderer` internally, making the rendering path opaque to unit tests. The adversarial reviewer flagged this; adding a default-parameter renderer injection point allowed the mutation test harness to substitute a test double and kill all surviving mutants. Design for testability at the public API surface, not just at the unit level.

---

## Next Steps / Open Items

None. The feature is complete. All acceptance criteria pass, all quality gates are cleared, and no follow-on work was identified during delivery.

---

*Generated by Apex (platform-architect) on 2026-03-18.*

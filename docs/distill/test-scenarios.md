# Test Scenarios: html-template-to-pdf

Generated: 2026-03-18
Wave: DISTILL

---

## Scenario Inventory

| Scenario ID | Story | Description | Spec file | Tags | Uses real renderer |
|---|---|---|---|---|---|
| SC-00-01 | STORY-00 | Plain HTML string returns a non-null InputStream | WalkingSkeletonSpec.kt | walking-skeleton | Yes |
| SC-00-02 | STORY-00 | Plain HTML stream begins with PDF magic bytes %PDF | WalkingSkeletonSpec.kt | walking-skeleton | Yes |
| SC-01-01 | STORY-01 | Valid plain HTML produces a non-null stream | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-01-02 | STORY-01 | Valid plain HTML stream starts with PDF magic bytes %PDF | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-01-03 | STORY-01 | Valid HTML with data map produces a non-null stream | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-01-04 | STORY-01 | Valid HTML with data map stream starts with PDF magic bytes %PDF | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-02-01 | STORY-02 | Single placeholder replaced from data map, valid PDF returned | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-02-02 | STORY-02 | Multiple distinct placeholders all replaced, valid PDF returned | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-02-03 | STORY-02 | Repeated placeholder replaced at every occurrence, valid PDF returned | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-02-04 | STORY-02 | Extra keys in data map silently ignored, no error thrown | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-02-05 | STORY-02 | Omitted data parameter treated as empty map for template with no placeholders | HtmlToPdfSpec.kt | acceptance | Yes |
| SC-03-01 | STORY-03 | Blank (whitespace-only) html throws IllegalArgumentException with message containing "html" | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-03-02 | STORY-03 | Empty string html throws IllegalArgumentException | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-04-01 | STORY-04 | Missing key in data map throws MissingVariableError | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-04-02 | STORY-04 | MissingVariableError.key contains the missing key name | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-04-03 | STORY-04 | MissingVariableError.template contains the original template string | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-04-04 | STORY-04 | MissingVariableError thrown when one of multiple keys is missing; first missing key identified | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-04-05 | STORY-04 | MissingVariableError is a RuntimeException subclass | HtmlToPdfSpec.kt | acceptance | No (throws before render) |
| SC-INF-01 | Infrastructure | build.gradle.kts exists and declares openpdf-html dependency | BuildValidationSpec.kt | infrastructure | No |
| SC-INF-02 | Infrastructure | htmlToPdf function is accessible from the public API | BuildValidationSpec.kt | infrastructure | Yes |
| SC-INF-03 | Infrastructure | MissingVariableError is a RuntimeException subclass (reflection) | BuildValidationSpec.kt | infrastructure | No |
| SC-INF-04 | Infrastructure | PdfRenderer is an interface (reflection) | BuildValidationSpec.kt | infrastructure | No |

---

## Error Path Ratio

Total scenarios: 22
Error / edge scenarios: SC-03-01, SC-03-02, SC-04-01, SC-04-02, SC-04-03, SC-04-04, SC-04-05 = 7 error scenarios + SC-02-04 (extra keys edge), SC-02-05 (omitted parameter edge) = 9 error/edge scenarios

Error path coverage: 9 / 22 = **41%** (target >= 40% — met)

---

## One-at-a-Time Implementation Order

1. SC-00-01 / SC-00-02 — Walking skeleton (enabled; all others marked `xtest` / `!`)
2. SC-01-01 through SC-01-04 — HTML to PDF stream basics
3. SC-02-01 through SC-02-05 — Variable injection
4. SC-03-01, SC-03-02 — Invalid input rejection
5. SC-04-01 through SC-04-05 — Missing variable error
6. SC-INF-01 through SC-INF-04 — Infrastructure validation

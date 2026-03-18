# Walking Skeleton: html-template-to-pdf

Wave: DISTILL
Date: 2026-03-18
Story: STORY-00

---

## Goal

The walking skeleton proves that the end-to-end pipeline is connected: an HTML string passed into `htmlToPdf` travels through `OpenPdfHtmlRenderer`, through `ITextRenderer` (openpdf-html), and emerges as valid PDF bytes in an `InputStream`. It answers the question: "Can a caller pass a plain HTML string and receive a readable PDF stream?" before any other feature is built on top of that pipeline.

---

## Scope

**Included:**
- Public entry point `htmlToPdf(html)` with no data map
- Real `OpenPdfHtmlRenderer` invoking `ITextRenderer` — no mocks, no stubs
- Verification of PDF magic bytes (`%PDF`, 0x25 0x50 0x44 0x46) at the start of the returned stream
- Clean stream return (non-null `InputStream`)

**Excluded:**
- Variable injection (`TemplateEngine`, `{{key}}` resolution)
- `MissingVariableError` handling
- Input validation beyond what the skeleton naturally exercises
- Visual or layout fidelity of the generated PDF

---

## Success Criteria

Both of the following must hold, verified by `WalkingSkeletonSpec`:

1. `htmlToPdf("<html><body><h1>Invoice #1001</h1></body></html>")` returns a non-null `InputStream`.
2. The first 4 bytes of that stream equal `%PDF` (`result.readNBytes(4) == "%PDF".toByteArray()`).

These criteria are binary-verifiable: the test either passes or fails. No subjective assessment is required.

---

## Implementation Order

The software-crafter must implement components in this exact sequence so that the walking skeleton test can pass at the earliest possible moment:

1. **`PdfRenderer` interface** — declare `fun render(html: String): InputStream` in `io.htmltopdf`. No logic; just the port contract.
2. **`OpenPdfHtmlRenderer` class** — implement `PdfRenderer` using the mandatory `ITextRenderer` sequence:
   ```kotlin
   val renderer = ITextRenderer()
   renderer.setDocumentFromString(html)
   renderer.layout()
   val baos = ByteArrayOutputStream()
   renderer.createPDF(baos)
   return ByteArrayInputStream(baos.toByteArray())
   ```
3. **`htmlToPdf` function** — top-level function in `io.htmltopdf`. For the skeleton: validate html is not blank, instantiate `OpenPdfHtmlRenderer`, call `renderer.render(html)`, return the `InputStream`. No template engine wiring yet.
4. **Enable `WalkingSkeletonSpec`** — run the two `then` blocks. Both must pass before proceeding to STORY-01.

`TemplateEngine` and `MissingVariableError` are introduced in STORY-02 and STORY-04 respectively, layered on top of the already-working skeleton without modifying `PdfRenderer` or `OpenPdfHtmlRenderer`.

---

## Handoff Note for Software-Crafter

Implement the walking skeleton (steps 1–4 above) before any other story. `WalkingSkeletonSpec` is the first test to enable. All scenarios in `HtmlToPdfSpec` and `BuildValidationSpec` must remain marked with `x` prefix (skipped) until the walking skeleton passes. This preserves the TDD feedback loop: one red test, make it green, commit, then enable the next.

The walking skeleton is demonstrable to stakeholders as-is: run `WalkingSkeletonSpec` and show the two passing assertions. No further features are required for the demo.

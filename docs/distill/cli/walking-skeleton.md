# CLI Walking Skeleton

## Goal

Prove that a user can supply an HTML file on the command line and receive a valid PDF file on disk — an end-to-end journey through the CLI boundary, the argument parser, the library call, and the file writer — with a single observable outcome: an output file whose first four bytes are `%PDF`.

This answers the stakeholder question: "Can a user run `htmltopdf --input file.html --output out.pdf` and get a real PDF?"

## Scope

- Input: a plain HTML file on disk (no template variables, no `--data` flag)
- Processing: `App.run` parses args, reads the file, calls the real `htmlToPdf()` library function
- Output: a PDF written to the path supplied via `--output`
- No mocks. No stubs. No in-memory shortcuts.

## Success criteria

| Observable outcome | Check |
|---|---|
| `App.run` returns `0` | `exitCode shouldBe 0` |
| Output file exists at the path supplied | `outputFile.exists() shouldBe true` |
| Output file begins with PDF magic bytes | `outputFile.readBytes().take(4) shouldBe "%PDF".toByteArray().toList()` |
| Nothing written to stderr | `stderr.shouldBeEmpty()` |

## Implementation order

The skeleton compiles only when each layer exists. Build them in this sequence:

1. **ArgParser** — parse `--input`, `--output`, `--data`; return a sealed result (parsed args or validation error)
2. **FileReader** — read the input HTML file from disk; return its text content or an error if the path does not exist
3. **App.run** — orchestrate: parse args → read file → call `htmlToPdf()` → write output → return exit code
4. **PdfWriter** — write the `InputStream` from `htmlToPdf()` to the output path on disk
5. **Test passes** — CLI-ACC-01 turns green; unskip CLI-ACC-02 and continue

No step requires a prior step to be "complete" in the final sense — each can be a minimal stub that satisfies the compiler — but the skeleton test does not pass until all four collaborate end-to-end.

## Handoff note

Implement STORY-CLI-01 first. The walking skeleton scenario (`CLI-ACC-01`) in `CliSpec.kt` is the only enabled test; all remaining scenarios use `xgiven` and are skipped. Enable them one at a time as each story is implemented, in the order listed in `test-scenarios.md`.

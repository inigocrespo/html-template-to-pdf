# Acceptance Test Self-Review

## Coverage matrix

| Story | Acceptance criteria | Scenario(s) | Status |
|---|---|---|---|
| STORY-CLI-01 | Exit 0, PDF exists, `%PDF` magic bytes, stderr empty | CLI-ACC-01 | Covered |
| STORY-CLI-02 | Template resolved from JSON, exit 0, PDF exists, stderr empty | CLI-ACC-02 | Covered |
| STORY-CLI-03 | Missing `--input` → exit 1 + descriptive error; missing `--output` → exit 1 + descriptive error | CLI-ACC-03, CLI-ACC-04 | Covered |
| STORY-CLI-04 | Input path absent → exit 1 + `Error: input file not found: <path>` | CLI-ACC-05 | Covered |
| STORY-CLI-04 (data variant) | Data path absent → exit 1 + `Error: data file not found: <path>` | CLI-ACC-06 | Covered |
| STORY-CLI-05 | Missing template variable → exit 1 + `Error: template variable '<key>' not found in data file` | CLI-ACC-07 | Covered |

All five stories are covered. The extension scenario (CLI-ACC-08: extra data keys ignored) provides additional error-boundary coverage beyond the stated stories.

## Port compliance

Every test invokes `App.run(arrayOf(...))` — the single driving port of the CLI. No test reaches into `ArgParser`, `FileReader`, `PdfWriter`, or `TemplateEngine` directly. Internal components are exercised only as a side-effect of calling the driving port. This satisfies the hexagonal boundary mandate.

## Real library confirmation

`htmlToPdf()` from `io.htmltopdf` is called without mocking in all scenarios that reach PDF generation (CLI-ACC-01, CLI-ACC-02, CLI-ACC-07, CLI-ACC-08). There are no `mockk`, `mockito`, or stub implementations in any acceptance spec file. Scenarios that short-circuit before the library call (CLI-ACC-03 through CLI-ACC-06) do not need the library and correctly never reach it.

## Stderr capture strategy

Each `given` block calls the local `captureStderr` helper, which:

1. Creates a `ByteArrayOutputStream` and wraps it in a `PrintStream`
2. Replaces `System.err` with the capture stream before calling `App.run`
3. Restores `System.err` in a `finally` block, guaranteeing restoration even on test failure
4. Returns the exit code and the captured string as a pair

This pattern avoids polluting the test runner's stderr and allows `shouldContain` assertions on exact error message text.

## Temporary file cleanup strategy

All temporary files are created with `java.nio.file.Files.createTempFile`, which places them in the platform temp directory (`/tmp` on macOS/Linux). Each file is registered with `deleteOnExit()` immediately after creation, so the JVM removes them when the test process exits. Output PDF files are created as temp files before `App.run` is called so the path exists for the CLI to write into; their content is only meaningful after the call returns.

No manual `afterTest {}` cleanup hook is required because `deleteOnExit()` is sufficient for the temp-directory strategy. If test isolation becomes a concern in CI, temp files can be migrated to a Kotest `tempdir()` block without changing the assertion logic.

## Error path ratio

5 error/edge scenarios out of 8 acceptance scenarios = **62.5%** (target: 40%). Ratio is met.

## One-test-at-a-time enforcement

CLI-ACC-01 (the walking skeleton) is the only `given` block in `CliSpec.kt`. All remaining blocks use `xgiven`, which Kotest treats as ignored/skipped. `CliInfrastructureSpec.kt` is fully enabled but makes no library calls and will not cause test failures until `App` is compiled. The software crafter must unskip exactly one scenario at a time, implementing the production code to make it green before proceeding.

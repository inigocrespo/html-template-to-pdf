# CLI Acceptance Test Scenarios

All scenarios exercise `App.run(args)` — the driving port of the CLI feature.
Real file I/O and the real `htmlToPdf()` library function are used throughout; no mocks.

## Scenario inventory

| Scenario ID | Story | Description | Spec file | Tags | Uses real library |
|---|---|---|---|---|---|
| CLI-ACC-01 | STORY-CLI-01 | Plain HTML file converted to PDF; exit 0, PDF magic bytes present, stderr empty | `CliSpec.kt` | `cli-acceptance` | Yes |
| CLI-ACC-02 | STORY-CLI-02 | HTML template with `{{name}}` and `{{amount}}` resolved from JSON data; exit 0, PDF produced, stderr empty | `CliSpec.kt` | `cli-acceptance` | Yes |
| CLI-ACC-03 | STORY-CLI-03 | `--input` flag absent; exit 1, stderr contains `Error: --input is required` | `CliSpec.kt` | `cli-acceptance` | No (validation short-circuits) |
| CLI-ACC-04 | STORY-CLI-03 | `--output` flag absent; exit 1, stderr contains `Error: --output is required` | `CliSpec.kt` | `cli-acceptance` | No (validation short-circuits) |
| CLI-ACC-05 | STORY-CLI-04 | `--input` path does not exist; exit 1, stderr contains `Error: input file not found: <path>` | `CliSpec.kt` | `cli-acceptance` | No (file check short-circuits) |
| CLI-ACC-06 | STORY-CLI-04 | `--data` path does not exist; exit 1, stderr contains `Error: data file not found: <path>` | `CliSpec.kt` | `cli-acceptance` | No (file check short-circuits) |
| CLI-ACC-07 | STORY-CLI-05 | Template variable `amount` absent from data file; exit 1, stderr contains `Error: template variable 'amount' not found in data file` | `CliSpec.kt` | `cli-acceptance` | Yes (library raises MissingVariableError) |
| CLI-ACC-08 | (extension) | Extra keys in JSON data file are silently ignored; exit 0, PDF produced | `CliSpec.kt` | `cli-acceptance` | Yes |
| CLI-INF-01 | (infrastructure) | `build.gradle.kts` declares the shadow plugin | `CliInfrastructureSpec.kt` | — | No |
| CLI-INF-02 | (infrastructure) | `App` object is resolvable from the public API at runtime | `CliInfrastructureSpec.kt` | — | No |
| CLI-INF-03 | (infrastructure) | `App.run` method signature returns `Int` (verified by reflection) | `CliInfrastructureSpec.kt` | — | No |

## Error path ratio

Error/edge scenarios: CLI-ACC-03, CLI-ACC-04, CLI-ACC-05, CLI-ACC-06, CLI-ACC-07 = 5 of 8 acceptance scenarios = **62.5%**

This exceeds the 40% target.

## Implementation order (one at a time)

1. CLI-ACC-01 — walking skeleton (enabled; all others skipped)
2. CLI-ACC-02 — template + data happy path
3. CLI-ACC-03 — missing `--input` flag
4. CLI-ACC-04 — missing `--output` flag
5. CLI-ACC-05 — input file not found
6. CLI-ACC-06 — data file not found
7. CLI-ACC-07 — missing template variable
8. CLI-ACC-08 — extra data keys ignored
9. CLI-INF-01..03 — infrastructure (unblock after shadow JAR configured)

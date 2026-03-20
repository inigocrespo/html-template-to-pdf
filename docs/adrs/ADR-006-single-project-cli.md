# ADR-006: Add CLI to Existing Single-Project Build

## Status

Accepted

## Date

2026-03-18

## Context

The `htmltopdf` CLI must be distributed as a shadow JAR and must call the existing `io.htmltopdf.htmlToPdf()` function. A structural decision is required: where does the CLI code live relative to the existing library source?

The existing project is a single Gradle module with sources in `src/main/kotlin/io/htmltopdf/`. No submodules exist. The library has no separate publication artifact (it is not published to Maven Central or a private registry).

## Decision

Add the CLI as a new package (`io.htmltopdf.cli`) within the existing single-module Gradle project. Source files are placed at `src/main/kotlin/io/htmltopdf/cli/`. No multi-project restructuring is performed.

## Rationale

### No restructuring cost

The library source files (`HtmlToPdf.kt`, `TemplateEngine.kt`, etc.) do not need to move. A multi-project build would require relocating files, updating imports, and re-validating all existing tests. The single-project approach adds CLI code without disturbing existing code.

### CLI is not independently deployable

The CLI has no use case without the library — it is a direct wrapper. There is no scenario where the CLI is deployed while the library is not present. Independent modules are justified when components deploy independently; that condition is not met here.

### Library is not published separately

The library is not on Maven Central or any registry. A multi-project build would provide value if other projects consumed the library independently, but that is not a current requirement. Adding that structure prematurely introduces build complexity with no benefit.

### Single shadow JAR is the delivery unit

The shadow plugin bundles both library code and CLI code into one JAR regardless of whether they are in one module or two. Two modules would add Gradle inter-project dependency configuration without changing the output artifact.

## Alternatives Considered

### Multi-project Gradle build (`:library` + `:cli` subprojects)

- Provides strict module boundary enforced by the build system.
- Allows the library JAR to be published independently in future.
- Rejected: requires moving all existing library source files, updating package declarations, and re-validating tests — significant restructuring cost with no immediate benefit. The module boundary can be achieved at the package level within a single project using Kotlin visibility rules.

### Separate repository

- Maximum isolation; library and CLI evolve independently; library can have its own release cadence.
- Rejected: overkill for a direct wrapper; requires maintaining two repositories, two CI pipelines, and a versioned inter-repo dependency. The CLI adds no independent behaviour beyond calling `htmlToPdf()`.

## Consequences

- **Positive:** Zero restructuring of existing code; all existing tests continue to pass without modification; simpler Gradle configuration.
- **Negative:** The build system does not enforce the package boundary between `io.htmltopdf` and `io.htmltopdf.cli`; package-level access discipline must be maintained by convention. If the library is published to a registry in future, a multi-project restructuring will be needed at that point.
- **Neutral:** The shadow JAR includes both library and CLI classes; the library JAR (produced by the standard `jar` task) is unaffected and contains only library classes.

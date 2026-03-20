# ADR-007: Manual CLI Argument Parsing (No Framework)

## Status

Accepted

## Date

2026-03-18

## Context

The `htmltopdf` CLI accepts exactly three flags: `--input` (required), `--data` (optional), and `--output` (required). An argument parsing strategy must be selected.

## Decision

Parse `args: Array<String>` directly in `ArgParser` using Kotlin stdlib. No argument-parsing framework dependency is added.

## Rationale

### Scale does not justify a framework

Three flags, two required, one optional, all accepting a single string value. No subcommands, no mutually exclusive groups, no environment variable fallbacks, no type coercions beyond string. An `ArgParser` that iterates the array and matches flag strings is fewer than 30 lines of code. Any framework would introduce more lines (configuration, type annotations, help generation) than the parser itself.

### No help generation needed

A CLI tool distributed as a JAR for a specific workflow does not need auto-generated `--help` output as a priority. If a `--help` flag is added in future, it can be handled in 3 lines. This is not a reason to add a framework now.

### Dependency weight vs. benefit

The lightest option (kotlinx-cli) adds a compile dependency and increases JAR size. The shadow JAR already bundles openpdf-html and Gson. Adding a parsing framework for 3 flags is weight without benefit.

## Alternatives Considered

### Clikt (com.github.ajalt.clikt)

- Apache 2.0 license; well-maintained; idiomatic Kotlin DSL; auto-generates `--help`.
- Rejected: adds ~500 KB of compiled classes; the problem it solves (complex arg parsing with types, validation, subcommands) is disproportionate to 3 string-valued flags. Maintenance surface increases without user-visible benefit.

### kotlinx-cli (org.jetbrains.kotlinx:kotlinx-cli)

- Apache 2.0 license; JetBrains official; lightweight relative to Clikt.
- Rejected: still adds a compile dependency for a task that requires no more than one iteration over `args`. The library is experimental as of Kotlin 2.x and has had breaking API changes across minor versions.

### picocli (info.picocli:picocli)

- Apache 2.0 license; mature; annotation-driven; supports GraalVM native image reflection config generation.
- Rejected: annotation-based approach adds boilerplate (annotations on a data class + reflection at runtime) that exceeds the complexity of a 30-line manual parser. GraalVM native image support is not a current requirement.

## Consequences

- **Positive:** Zero additional compile dependency; `ArgParser` is fully readable inline; no framework API changes can break the CLI.
- **Negative:** No auto-generated `--help` or usage output; the parser must be extended manually if flags are added in future. Acceptable: 3 flags is a stable interface.
- **Neutral:** Argument order in `args` is not guaranteed by convention; the parser must tolerate any flag order. This is standard and adds no complexity to a loop-based implementation.

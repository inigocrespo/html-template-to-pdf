# ADR-009: Shadow JAR for CLI Distribution

## Status

Accepted

## Date

2026-03-18

## Context

The `htmltopdf` CLI must be distributed as a single executable artifact that users can invoke with `java -jar htmltopdf.jar`. The CLI depends on `openpdf-html`, Gson, and Kotlin stdlib — all of which must be available at runtime.

A distribution strategy must be selected that satisfies this single-artifact requirement without mandating that consumers install any dependencies separately.

## Decision

Apply the `com.github.johnrengelman.shadow` plugin (version 8.1.1, Apache 2.0) to produce a shadow (fat) JAR. The shadow JAR bundles all runtime dependencies. The manifest `Main-Class` attribute is set to `io.htmltopdf.cli.MainKt`.

## Rationale

### NFR-01: single executable JAR is non-negotiable

The distribution requirement is a single file invocable via `java -jar`. Any strategy that produces multiple files (a lib directory, a zip archive, an installer) violates this requirement. The shadow JAR is the only standard Gradle approach that produces one file satisfying this constraint.

### Standard Gradle ecosystem solution

The shadow plugin is the de facto standard for fat JAR production in Gradle projects. It is well-documented, actively maintained (8.x supports Gradle 8 configuration cache), and its behaviour with Kotlin metadata is understood and stable.

### No consumer installation burden

Users who receive the shadow JAR need only a JVM 21 installation. They do not need Gradle, Kotlin, Gson, or openpdf-html installed. This is the lowest possible barrier for CLI distribution.

### Separation from library JAR

The shadow task produces `htmltopdf-all.jar` (classifier `all`). The standard `jar` task continues to produce the library JAR without bundled dependencies. Existing library consumers are unaffected — they manage their own transitive dependency graph.

## Alternatives Considered

### Gradle application plugin + zip distribution

- Built-in; no extra plugin; produces a zip with a `lib/` directory and launch scripts (`bin/htmltopdf`, `bin/htmltopdf.bat`).
- Rejected: produces multiple files, not a single JAR. Violates NFR-01 directly. Launch scripts are platform-specific and require shell availability. A user receiving the zip must unpack and locate the script — not equivalent to `java -jar`.

### GraalVM native image (org.graalvm.buildtools.native plugin)

- Apache 2.0 license; produces a platform-specific native executable with no JVM requirement at runtime; near-instant startup.
- Rejected: adds substantial build complexity — GraalVM must be installed in the build environment, reflection configuration files must be maintained for Gson and openpdf-html, and the resulting artifact is platform-specific (separate builds for macOS arm64, macOS x86, Linux x86, Windows). The shadow JAR is platform-neutral and meets the NFR without this overhead. Native image can be evaluated in a future iteration if startup latency becomes a documented user pain point.

## Consequences

- **Positive:** Single `java -jar` invocation; platform-neutral artifact; standard, well-understood plugin; no consumer dependency installation required; library JAR is unaffected.
- **Negative:** Shadow JAR bundles all transitive dependencies, producing a larger artifact than the library JAR alone. Estimated size: ~20–30 MB (dominated by openpdf-html's transitive dependencies, which are already present in the library consumers' classpaths). This is acceptable for a CLI tool.
- **Neutral:** The shadow plugin relocates classes by default only when explicitly configured. No relocation is required here since the CLI is the sole consumer of the shadow JAR and there are no class name conflicts to resolve.

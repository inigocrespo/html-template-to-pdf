# ADR-001: Use Kotlin on JVM

## Status

Accepted

## Date

2026-03-18

## Context

The project requires a JVM library for HTML-to-PDF conversion. The JVM platform is mandated by the rendering dependency (openpdf-html, a Java library). A JVM language must be selected for source authorship.

The library will be consumed by JVM-based applications. Null safety, conciseness, and build tooling integration are the primary selection criteria.

## Decision

Kotlin (latest stable, 2.1.x series) is the implementation language.

## Rationale

- **Null safety:** Kotlin's type system distinguishes nullable and non-nullable references at compile time, eliminating an entire class of `NullPointerException` defects without runtime overhead.
- **Concise syntax:** Data classes, default parameter values, extension functions, and top-level functions reduce boilerplate relative to Java. The public API (`fun htmlToPdf(...)` as a top-level function with a default parameter) is idiomatic Kotlin and clean for Java callers via `@JvmOverloads`.
- **First-class Gradle KTS support:** Kotlin DSL build scripts (`build.gradle.kts`) are type-checked and benefit from IDE completion. Maintaining one language across source and build reduces context switching.
- **Full Java interop:** Kotlin compiles to JVM bytecode; openpdf-html and all Java transitive dependencies are callable without wrappers.
- **Open source:** Apache 2.0 license. No cost, no restriction on library distribution.

## Alternatives Considered

### Java 21

- Viable: Java 21 is an LTS release with records, pattern matching, and sealed classes.
- Rejected: more verbose than Kotlin for the same constructs; no null safety at the language level; Gradle KTS build scripts would still use Kotlin while source uses Java, adding a second language.

### Groovy

- Viable: dynamic JVM language with concise syntax; supported by Gradle.
- Rejected: dynamic typing reduces IDE support and introduces runtime type errors; Groovy's popularity has declined significantly relative to Kotlin in the JVM ecosystem; no null safety.

## Consequences

- **Positive:** Null safety reduces defects; concise API surface; consistent language across build and source.
- **Negative:** Kotlin compiler is an additional build-time dependency; developers unfamiliar with Kotlin face a short learning curve (mitigated: Kotlin is syntactically close to Java and well-documented).
- **Neutral:** Java callers can use the library without any Kotlin dependency on their side (Kotlin stdlib is bundled as a compile dependency, not a peer dependency).

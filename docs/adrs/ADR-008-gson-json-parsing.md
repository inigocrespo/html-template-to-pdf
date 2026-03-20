# ADR-008: Gson for Flat JSON Data File Parsing

## Status

Accepted

## Date

2026-03-18

## Context

The CLI `--data` flag accepts a path to a JSON file containing flat key-value pairs (string keys, string values). This JSON must be deserialised into `Map<String, String>` and passed to `htmlToPdf()`. A JSON parsing library must be selected.

The requirement is specifically for flat objects: no nested structures, no arrays at the root, no schema evolution. The library will be bundled into the shadow JAR.

## Decision

Use `com.google.code.gson:gson:2.11.0` (Apache 2.0) for JSON deserialisation.

## Rationale

### Minimal API surface for the task

Gson deserialises a flat JSON object to `Map<String, String>` in a single call using `TypeToken`. No type annotations on data classes, no plugin configuration, no registered adapters. `JsonParser.parseString(json).asJsonObject` allows direct structural validation before deserialisation.

### Single-JAR footprint

Gson's JAR is ~268 KB with no transitive dependencies. For a shadow JAR distribution this matters: every bundled dependency increases artifact size. Gson adds the least weight of the viable alternatives.

### Maturity and trust

Gson 2.x has been maintained by Google since 2008. Version 2.11.0 is the current stable release. It handles Unicode, edge cases in number parsing, and malformed input consistently. Writing a manual parser would reintroduce those edge cases.

### License compatibility

Apache 2.0 is compatible with the project's existing dependency licenses (LGPL 2.1 for openpdf-html; Apache 2.0 for Kotlin stdlib). No license conflict.

## Alternatives Considered

### kotlinx.serialization (org.jetbrains.kotlinx:kotlinx-serialization-json)

- Apache 2.0 license; idiomatic Kotlin; compile-time safe; no reflection.
- Rejected: deserialising to `Map<String, String>` requires either a `@Serializable` annotated class or manual `JsonObject` traversal. The Kotlin serialization Gradle plugin must also be applied, adding build configuration. Total setup cost exceeds Gson's one-liner for this use case. Additionally, the library module does not use kotlinx.serialization; adding it only for the CLI increases the build footprint.

### Jackson (com.fasterxml.jackson.module:jackson-module-kotlin)

- Apache 2.0 license; industry standard; feature-rich.
- Rejected: `jackson-module-kotlin` pulls in `jackson-core`, `jackson-databind`, and `jackson-annotations` — approximately 2 MB of transitive JARs bundled into the shadow JAR vs. Gson's 268 KB. No feature in those extra megabytes is needed for flat `Map<String, String>` parsing.

### Manual JSON parsing (stdlib only)

- Zero additional dependency; no JAR size increase.
- Rejected: a hand-written JSON parser must correctly handle string escaping, Unicode code points, whitespace variations, and number/boolean coercion. All of these are solved problems in Gson. The maintenance burden and risk of subtle parsing bugs outweigh the dependency cost savings, especially since Gson is already a trusted, widely-audited library.

## Consequences

- **Positive:** Single-call deserialisation; minimal JAR footprint increase (~268 KB); Apache 2.0 license; no build plugin required.
- **Negative:** Gson does not perform compile-time type checking; type errors surface at runtime (acceptable — JSON is inherently dynamic). Gson's `@SerializedName` and streaming APIs are unused and add dead weight to the bundled JAR (negligible).
- **Neutral:** Gson 2.x is in maintenance mode with no planned 3.x release; the API is stable with no anticipated breaking changes. This is a feature for a pinned-version use case, not a liability.

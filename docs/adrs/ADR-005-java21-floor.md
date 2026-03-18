# ADR-005: Java 21 as Minimum JVM Version

## Status

Accepted

## Date

2026-03-18

## Context

The library compiles to JVM bytecode and will be consumed by JVM applications. A minimum JVM version (bytecode target and runtime floor) must be declared. The primary constraint is the rendering dependency: `com.github.librepdf:openpdf-html:3.0.3`.

## Decision

The library targets Java 21 bytecode (`jvmTarget = "21"` in Gradle KTS). The declared minimum runtime JVM version is Java 21. This is documented in the library's `build.gradle.kts` and in public API documentation.

## Rationale

### Hard dependency requirement
`openpdf-html:3.0.3` requires Java 21 at runtime. Declaring a lower bytecode target would produce a library that compiles but fails at runtime on pre-21 JVMs. The declared floor must match the effective runtime requirement of the most demanding dependency.

### LTS alignment
Java 21 is a Long-Term Support (LTS) release (GA September 2023; Oracle support until September 2031). Requiring an LTS release reduces the risk of consumers being unable to upgrade.

### Modern platform features
Java 21 includes virtual threads (Project Loom), record patterns, and sequenced collections. These are available to the library and its consumers without additional flags or preview mode.

### No legacy consumer support needed
The DISCUSS wave identified no requirement to support pre-21 JVMs. There are no regulatory or contractual constraints mandating Java 11 or 17 compatibility.

## Alternatives Considered

### Java 17 (LTS)

- Would be preferred if openpdf-html supported it; Java 17 has a large installed base.
- Rejected: `openpdf-html:3.0.3` does not support Java 17 at runtime. Forcing Java 17 would require downgrading the rendering engine or forking it, neither of which is justified.

### Java 11 (LTS)

- Maximum installed-base coverage; supported in many enterprise environments through 2026.
- Rejected: same constraint as Java 17. `openpdf-html:3.0.3` requires Java 21. Additionally, Java 11 is approaching end of general support.

### Java 24 (latest non-LTS at time of writing)

- Access to the newest platform features.
- Rejected: non-LTS; short support window; would unnecessarily restrict consumers who have not yet moved past Java 21. Java 21 is the correct floor — consumers on 22, 23, or 24 can run Java 21 bytecode without issue.

## Consequences

- **Positive:** Full compatibility with `openpdf-html:3.0.3`; consumers gain access to virtual threads and modern language features; aligns with LTS lifecycle.
- **Negative:** Consumers on Java 11 or 17 cannot use this library without upgrading their JVM. This is a known, accepted constraint established in the DISCUSS wave.
- **Neutral:** The Kotlin compiler produces Java 21 compatible bytecode when `jvmTarget = "21"` is set; no additional tooling required.

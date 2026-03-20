# Technology Stack: htmltopdf CLI

Version: 1.0
Date: 2026-03-18

---

## Selected Stack

| Layer | Technology | Version | License | Rationale |
|---|---|---|---|---|
| Language | Kotlin | 2.1.0 | Apache 2.0 | Existing codebase language; consistent with library |
| Build system | Gradle (Kotlin DSL) | 8.10.2 | Apache 2.0 | Existing build tool; Kotlin DSL already in use |
| JVM floor | JVM 21 | 21 (LTS) | GPL + Classpath Exception | Hard requirement from `openpdf-html:3.0.3` (see ADR-005) |
| JSON parsing | Gson | 2.11.0 | Apache 2.0 | Single-method flat-map deserialisation; minimal footprint; no schema needed |
| PDF rendering | openpdf-html | 3.0.3 | LGPL 2.1 | Existing library dependency; bundled into shadow JAR |
| Distribution | com.github.johnrengelman.shadow | 8.1.1 | Apache 2.0 | Standard Gradle shadow/fat JAR plugin; required for single-JAR distribution |
| Argument parsing | Kotlin stdlib (manual) | 2.1.0 | Apache 2.0 | No framework needed for 3 flags; zero additional dependency |

---

## Excluded Alternatives

| Alternative | Category | Reason Excluded |
|---|---|---|
| Clikt | Argument parsing | Apache 2.0; well-maintained; but adds ~500 KB dependency for 3 flags — maintenance burden exceeds benefit at this scale |
| kotlinx-cli | Argument parsing | Apache 2.0; JetBrains official; same objection — dependency weight unjustified for 3 flags; less mature than Clikt |
| picocli | Argument parsing | Apache 2.0; feature-rich with native image support; same objection — overcomplicated for 3 flags |
| Jackson | JSON parsing | Apache 2.0; powerful; but ~2 MB of transitive dependencies vs Gson's single JAR; no advantage for flat key-value parsing |
| kotlinx.serialization | JSON parsing | Apache 2.0; idiomatic Kotlin; but requires `@Serializable` annotation and plugin configuration — schema definition overhead unjustified for flat `Map<String, String>` |
| Manual JSON parsing | JSON parsing | No dependency; but error-prone for edge cases (escaping, Unicode, whitespace); Gson is more reliable with near-zero cost |
| GraalVM native image | Distribution | Free; fast startup; but adds significant build-time complexity and CI infrastructure requirement; shadow JAR meets NFR without this cost |
| Gradle application plugin + zip | Distribution | Built-in; no extra plugin; but produces a zip archive with launch scripts — not a single executable JAR; violates NFR-01 |

# Technology Stack: html-template-to-pdf

## Selected Technologies

| Layer | Technology | Version | License | Rationale |
|---|---|---|---|---|
| Language | Kotlin | 2.1.x (latest stable) | Apache 2.0 | Null safety, concise syntax, first-class coroutines and DSL support; see ADR-001 |
| Build | Gradle with Kotlin DSL | 8.x | Apache 2.0 | Type-safe build scripts, same language as source, IDE completion; Kotlin KTS preferred over Groovy DSL |
| JVM target | Java 21 (LTS) | 21 | GPL w/ Classpath Exception | Required by openpdf-html 3.x; LTS release with virtual threads and pattern matching; see ADR-005 |
| PDF rendering | openpdf-html | 3.0.3 | LGPL 2.1 | Open-source Flying Saucer / iText fork; actively maintained; Java 21 support confirmed |
| Transitive: PDF core | openpdf | (managed by openpdf-html) | LGPL 2.1 | Pulled in transitively; no direct dependency declaration needed |
| Transitive: HTML parsing | neko-htmlunit | (managed by openpdf-html) | Apache 2.0 | Pulled in transitively; tolerant HTML parser used by openpdf-html |
| Logging facade | slf4j-api | 2.x | MIT | openpdf-html expects an SLF4J binding on the classpath; declared as `compileOnly` — consumer provides implementation |
| Unit testing | JUnit 5 (Jupiter) | 5.x | EPL 2.0 | Standard JVM test engine; integrates with Gradle test task out of the box |
| Assertion library | Kotest assertions | 5.x | Apache 2.0 | Expressive Kotlin-idiomatic matchers; used alongside JUnit 5 runner |

---

## Dependency Declaration Summary

```
implementation("com.github.librepdf:openpdf-html:3.0.3")
compileOnly("org.slf4j:slf4j-api:2.x")

testImplementation("org.junit.jupiter:junit-jupiter:5.x")
testImplementation("io.kotest:kotest-assertions-core:5.x")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
```

Transitive dependencies (`openpdf`, `neko-htmlunit`) are resolved automatically. No explicit declarations required.

---

## Excluded Technologies and Rationale

| Excluded | Category | Reason |
|---|---|---|
| Maven | Build | Groovy/XML DSL; less ergonomic for Kotlin projects; Gradle KTS preferred |
| `ByteArray` return type | API surface | See ADR-003; `InputStream` is preferred for streaming semantics and caller flexibility |
| Fat JAR / shadow JAR | Packaging | Library, not an application; fat JAR would pollute consumer classpaths and conflict with their dependency versions |
| Thymeleaf / Freemarker | Template engine | Full template engines are heavy dependencies for mustache-style `{{key}}` substitution; regex replacement is sufficient |
| MockK | Test doubles | Not excluded categorically, but `PdfRenderer` port is a single-method interface; plain Kotlin lambda or anonymous object suffices; avoids an extra test dependency |
| Java | Language | See ADR-001; Kotlin is preferred |
| Java 11 / Java 17 | JVM target | openpdf-html 3.x requires Java 21; lower targets are incompatible; see ADR-005 |

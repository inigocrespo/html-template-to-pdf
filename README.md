# html-template-to-pdf

A Kotlin library that converts HTML templates to PDF, with optional `{{variable}}` injection. Includes a CLI wrapper.

## Requirements

- Java 21+

## Library

### Installation

Add to `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.htmltopdf:html-template-to-pdf:0.1.0")
}
```

### Usage

```kotlin
import io.htmltopdf.htmlToPdf

// Plain HTML
val pdf: InputStream = htmlToPdf("<html><body><h1>Hello</h1></body></html>")

// With variable injection
val pdf: InputStream = htmlToPdf(
    html = "<html><body><h1>{{title}}</h1></body></html>",
    data = mapOf("title" to "Invoice #42")
)

// Write to file
pdf.use { Files.copy(it, Path.of("output.pdf"), StandardCopyOption.REPLACE_EXISTING) }
```

### Template syntax

Use `{{key}}` placeholders in HTML. All keys must be present in the `data` map — missing keys throw `MissingVariableError`. Extra keys are ignored.

### Errors

| Condition | Thrown |
|-----------|--------|
| `html` is blank | `IllegalArgumentException` |
| Template variable missing from `data` | `MissingVariableError` (has `.key` property) |

## CLI

### Build

```bash
./gradlew shadowJar
# produces build/libs/html-template-to-pdf-0.1.0.jar
```

### Usage

```bash
java -jar html-template-to-pdf-0.1.0.jar \
  --input template.html \
  --data data.json \
  --output invoice.pdf
```

| Flag | Required | Description |
|------|----------|-------------|
| `--input` | Yes | Path to the HTML template file |
| `--data` | No | Path to a flat JSON file with string key-value pairs |
| `--output` | Yes | Path for the output PDF |

### Exit codes

- `0` — success, PDF written to `--output`
- `1` — error, message printed to stderr

### Error messages

| Condition | stderr |
|-----------|--------|
| `--input` absent | `Error: --input is required` |
| `--output` absent | `Error: --output is required` |
| Input file not found | `Error: input file not found: <path>` |
| Data file not found | `Error: data file not found: <path>` |
| Missing template variable | `Error: template variable '<key>' not found in data file` |

Nothing is printed to stdout on success.

## Development

```bash
./gradlew test        # run tests
./gradlew shadowJar   # build fat JAR
```

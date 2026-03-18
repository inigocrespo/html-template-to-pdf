# Acceptance Criteria: html-template-to-pdf

Version: 1.0
Date: 2026-03-18
Format: Given / When / Then (BDD)
All criteria are testable via automated unit or integration tests.

---

## STORY-00: Walking Skeleton

### AC-00-01: Plain HTML resolves to a Readable stream
```
Given the html-template-to-pdf library is installed and imported
When htmlToPdf is called with the string "<h1>Invoice #1001</h1><p>Total: $240.00</p>"
Then the return value is a Promise
And the Promise resolves without error
And the resolved value is an instance of stream.Readable
```

### AC-00-02: Stream emits valid PDF bytes
```
Given htmlToPdf has been called with a valid plain HTML string
When the returned Promise resolves to pdfStream
And all chunks from pdfStream are concatenated into a Buffer
Then the Buffer is non-empty
And the first four bytes of the Buffer equal the ASCII sequence "%PDF" (0x25, 0x50, 0x44, 0x46)
```

### AC-00-03: Stream closes cleanly
```
Given htmlToPdf has been called with a valid plain HTML string
When the returned Promise resolves to pdfStream
And pdfStream is piped to a writable destination
Then the stream emits an "end" event after all bytes are written
And no "error" event is emitted on the stream
And no open handles remain after the stream ends
```

### AC-00-04: Library exports htmlToPdf as a named CommonJS export
```
Given the library is installed in a Node.js project
When require('html-template-to-pdf') is called
Then the result has a property "htmlToPdf"
And that property is a function
```

---

## STORY-01: HTML String to PDF Stream (no variables)

### AC-01-01: Valid HTML string produces a PDF stream
```
Given a developer has a non-empty HTML string
When htmlToPdf is called with that string as the only argument
Then the Promise resolves to a stream.Readable instance
And the stream contains PDF bytes
```

### AC-01-02: Non-string first argument rejects with TypeError
```
Given a developer calls htmlToPdf with a number as the first argument
When the Promise is awaited
Then the Promise rejects
And the rejection is an instance of TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-01-03: Empty string first argument rejects with TypeError
```
Given a developer calls htmlToPdf with an empty string ""
When the Promise is awaited
Then the Promise rejects
And the rejection is an instance of TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-01-04: Promise never throws synchronously
```
Given any call to htmlToPdf regardless of arguments
When htmlToPdf is invoked (not awaited)
Then no synchronous exception is thrown
And a Promise is returned
```

### AC-01-05: Stream can be piped to a file
```
Given htmlToPdf resolves to pdfStream for input "<p>Receipt for order 5577</p>"
When pdfStream is piped to fs.createWriteStream('test-output.pdf')
Then the file test-output.pdf is created on disk
And the file is non-empty
And the stream completes without error
```

---

## STORY-02: Variable Injection with `{{name}}` Syntax

### AC-02-01: Single placeholder is replaced in rendered output
```
Given Maria Santos has the template "<h1>Hello {{name}}</h1>"
And Maria has the data object { name: "Maria Santos" }
When htmlToPdf is called with the template and the data object
Then the Promise resolves to a Readable stream
And when the stream is consumed, the PDF content contains "Hello Maria Santos"
And the PDF content does not contain the literal string "{{name}}"
```

### AC-02-02: Multiple placeholders are all replaced
```
Given Maria Santos has the template "<p>{{clientName}} owes {{amount}} by {{dueDate}}</p>"
And Maria has the data object { clientName: "Acme Corp", amount: "$1,500.00", dueDate: "2026-04-01" }
When htmlToPdf is called with the template and the data object
Then the Promise resolves successfully
And the rendered PDF contains "Acme Corp"
And the rendered PDF contains "$1,500.00"
And the rendered PDF contains "2026-04-01"
And the rendered PDF does not contain "{{clientName}}"
And the rendered PDF does not contain "{{amount}}"
And the rendered PDF does not contain "{{dueDate}}"
```

### AC-02-03: All occurrences of the same placeholder are replaced
```
Given the template "<p>Dear {{name}}, your name {{name}} has been registered.</p>"
And the data object { name: "Maria Santos" }
When htmlToPdf is called with the template and the data object
Then both occurrences of "{{name}}" are replaced with "Maria Santos"
And the literal string "{{name}}" does not appear anywhere in the rendered PDF
```

### AC-02-04: Extra keys in data object are silently ignored
```
Given Maria Santos has the template "<p>Hello {{name}}</p>"
And the data object { name: "Maria", unused: "ignored" }
When htmlToPdf is called with the template and the data object
Then the Promise resolves successfully
And the rendered PDF contains "Hello Maria"
```

### AC-02-05: Omitted data object is treated as empty object
```
Given Marco Rossi has a plain HTML string with no placeholders
When htmlToPdf is called with only the HTML string (no second argument)
Then the Promise resolves to a Readable stream
And no error is thrown for the absent data argument
```

---

## STORY-03: Error Handling for Invalid HTML Input

### AC-03-01: Number as first argument rejects with TypeError
```
Given htmlToPdf is called with the number 42 as htmlString
When the Promise is awaited
Then the Promise rejects with a TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-03-02: undefined as first argument rejects with TypeError
```
Given htmlToPdf is called with undefined as htmlString
When the Promise is awaited
Then the Promise rejects with a TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-03-03: null as first argument rejects with TypeError
```
Given htmlToPdf is called with null as htmlString
When the Promise is awaited
Then the Promise rejects with a TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-03-04: Empty string rejects with TypeError
```
Given htmlToPdf is called with the empty string ""
When the Promise is awaited
Then the Promise rejects with a TypeError
And the error message contains "htmlString must be a non-empty string"
```

### AC-03-05: openpdf-html errors propagate as Promise rejections
```
Given openpdf-html throws an error during rendering
When htmlToPdf is awaited
Then the Promise rejects
And the rejection error is the error thrown by openpdf-html (or a wrapped version of it)
And no uncaught exception escapes the Promise chain
```

---

## STORY-04: Error Handling for Missing Template Variables

### AC-04-01: Missing key rejects with MissingVariableError
```
Given Maria Santos has the template "<h1>Hello {{name}}</h1>"
And Maria has the data object {} (no "name" key)
When htmlToPdf is called with the template and the data object
Then the Promise rejects
And the rejection is an instance of MissingVariableError
And the error has a "key" property equal to "name"
And the error has a "template" property equal to "<h1>Hello {{name}}</h1>"
```

### AC-04-02: Renderer is not called when a variable is missing
```
Given the template "<p>{{clientName}} owes {{amount}}</p>"
And the data object { clientName: "Acme Corp" } (missing "amount")
When htmlToPdf is called
Then the Promise rejects with MissingVariableError { key: "amount" }
And openpdf-html is NOT invoked (verifiable via spy/mock in tests)
```

### AC-04-03: MissingVariableError is exported and usable in instanceof checks
```
Given MissingVariableError is imported from html-template-to-pdf
And htmlToPdf is called with a template missing a variable
When the rejection is caught
Then (error instanceof MissingVariableError) evaluates to true
```

### AC-04-04: Invalid dataObject type rejects with TypeError
```
Given Maria Santos has the template "<p>Hello {{name}}</p>"
And Maria passes the string "not-an-object" as the second argument
When htmlToPdf is called
Then the Promise rejects with a TypeError
And the error message contains "dataObject must be a plain object"
```

### AC-04-05: Array as dataObject rejects with TypeError
```
Given the data argument is an Array ["Maria"]
When htmlToPdf is called with a template and that array
Then the Promise rejects with a TypeError
And the error message contains "dataObject must be a plain object"
```

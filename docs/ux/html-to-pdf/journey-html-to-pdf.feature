Feature: HTML Template to PDF Conversion
  As a Node.js developer
  I want to convert an HTML string (with optional variable placeholders) into a PDF
  So that I can generate dynamic PDF documents from within my application

  Background:
    Given the html-template-to-pdf library is installed
    And the htmlToPdf function is imported into my module

  # -------------------------------------------------------------------------
  # WALKING SKELETON (Feature 0)
  # Proves the pipeline end-to-end with no variable injection.
  # Thinnest possible slice: plain HTML in, PDF readable stream out.
  # -------------------------------------------------------------------------

  @skeleton
  Scenario: Walking Skeleton -- plain HTML string to PDF readable stream
    Given Marco Rossi has a plain HTML string "<h1>Invoice #1001</h1><p>Total: $240.00</p>"
    And Marco has no data object to pass
    When Marco calls htmlToPdf with the plain HTML string
    Then the call returns a Promise
    And the Promise resolves to a Node.js Readable stream
    And the Readable stream emits PDF bytes
    And Marco can pipe the stream to a file named "invoice-1001.pdf" without error
    And the stream emits an "end" event after all bytes are written

  @skeleton
  Scenario: Walking Skeleton -- stream can be consumed multiple ways
    Given Marco Rossi has a plain HTML string "<p>Receipt for order 5577</p>"
    When Marco calls htmlToPdf with the plain HTML string
    And the Promise resolves to a pdfStream
    Then Marco can collect all chunks from the pdfStream into a Buffer
    And the Buffer is non-empty
    And the first four bytes of the Buffer match the PDF magic number "%PDF"

  # -------------------------------------------------------------------------
  # FEATURE 1 -- Variable Injection
  # Template contains {{key}} placeholders; data object supplies values.
  # -------------------------------------------------------------------------

  @feature-1
  Scenario: Variable injection -- single placeholder replaced with data value
    Given Maria Santos has an HTML template "<h1>Hello {{name}}</h1><p>Your order is ready.</p>"
    And Maria has a data object with key "name" set to "Maria Santos"
    When Maria calls htmlToPdf with the template and the data object
    Then the call returns a Promise
    And the Promise resolves to a Node.js Readable stream
    And when the stream is consumed, the rendered PDF contains the text "Hello Maria Santos"
    And the rendered PDF does not contain the literal string "{{name}}"

  @feature-1
  Scenario: Variable injection -- multiple placeholders replaced in a single template
    Given Maria Santos has an HTML template:
      """
      <h1>Invoice for {{clientName}}</h1>
      <p>Amount due: {{amount}}</p>
      <p>Due date: {{dueDate}}</p>
      """
    And Maria has a data object:
      | key        | value           |
      | clientName | Acme Corp       |
      | amount     | $1,500.00       |
      | dueDate    | 2026-04-01      |
    When Maria calls htmlToPdf with the template and the data object
    And the Promise resolves to a pdfStream
    Then the rendered PDF contains "Acme Corp"
    And the rendered PDF contains "$1,500.00"
    And the rendered PDF contains "2026-04-01"
    And the rendered PDF does not contain "{{clientName}}"
    And the rendered PDF does not contain "{{amount}}"
    And the rendered PDF does not contain "{{dueDate}}"

  @feature-1
  Scenario: Variable injection -- extra keys in data object are silently ignored
    Given Maria Santos has an HTML template "<p>Hello {{name}}</p>"
    And Maria has a data object with keys "name" set to "Maria" and "unused" set to "ignored"
    When Maria calls htmlToPdf with the template and the data object
    Then the Promise resolves successfully to a Readable stream
    And the rendered PDF contains "Hello Maria"

  # -------------------------------------------------------------------------
  # ERROR HANDLING
  # -------------------------------------------------------------------------

  @feature-1
  Scenario: Missing variable -- placeholder present in template but key absent from data object
    Given Maria Santos has an HTML template "<h1>Hello {{name}}</h1>"
    And Maria has a data object that does NOT contain the key "name"
    When Maria calls htmlToPdf with the template and the data object
    Then the Promise rejects
    And the rejection error is an instance of MissingVariableError
    And the error has a "key" property equal to "name"
    And the error has a "template" property containing the original template string
    And openpdf-html is NOT invoked

  @skeleton
  Scenario: Invalid input -- htmlString is not a string
    Given Marco Rossi passes the number 42 as the first argument to htmlToPdf
    When htmlToPdf is called with 42
    Then the Promise rejects
    And the rejection error is an instance of TypeError
    And the error message states that htmlString must be a non-empty string

  @skeleton
  Scenario: Invalid input -- htmlString is an empty string
    Given Marco Rossi passes an empty string "" as the first argument to htmlToPdf
    When htmlToPdf is called with the empty string
    Then the Promise rejects
    And the rejection error is an instance of TypeError
    And the error message states that htmlString must be a non-empty string

  @feature-1
  Scenario: Invalid input -- dataObject is not a plain object
    Given Maria Santos has a valid HTML template "<p>Hello {{name}}</p>"
    And Maria passes the string "invalid" as the data argument
    When htmlToPdf is called with the template and "invalid"
    Then the Promise rejects
    And the rejection error is an instance of TypeError
    And the error message states that dataObject must be a plain object

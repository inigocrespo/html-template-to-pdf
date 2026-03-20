Feature: htmltopdf CLI
  As a developer
  I want to run htmltopdf with an HTML template and optional JSON data
  So that I get a PDF file on disk without writing Kotlin code

  Background:
    Given the htmltopdf CLI is installed and on the PATH

  # ---------------------------------------------------------------------------
  # Happy path scenarios
  # ---------------------------------------------------------------------------

  Scenario: Convert plain HTML file to PDF
    Given Sofia Esposito has a file "report.html" containing "<h1>Annual Report 2025</h1><p>Revenue: $4.2M</p>"
    And no data file is provided
    When Sofia runs "htmltopdf --input report.html --output report.pdf"
    Then the command exits with code 0
    And a file "report.pdf" exists on disk
    And "report.pdf" begins with the PDF magic bytes "%PDF"
    And nothing is written to stdout

  Scenario: Convert HTML template with JSON data to PDF
    Given Sofia Esposito has a file "invoice.html" containing "<h1>Invoice for {{clientName}}</h1><p>Amount: {{amount}}</p>"
    And Sofia has a file "invoice-data.json" containing:
      """
      {
        "clientName": "Acme Corp",
        "amount": "$1,500.00"
      }
      """
    When Sofia runs "htmltopdf --input invoice.html --data invoice-data.json --output invoice.pdf"
    Then the command exits with code 0
    And a file "invoice.pdf" exists on disk
    And "invoice.pdf" begins with the PDF magic bytes "%PDF"
    And nothing is written to stdout

  Scenario: Extra key in data file is silently ignored
    Given Sofia Esposito has a file "greeting.html" containing "<p>Hello {{name}}</p>"
    And Sofia has a file "extra-data.json" containing:
      """
      {
        "name": "Sofia",
        "unusedKey": "this value is never referenced"
      }
      """
    When Sofia runs "htmltopdf --input greeting.html --data extra-data.json --output greeting.pdf"
    Then the command exits with code 0
    And a file "greeting.pdf" exists on disk
    And nothing is written to stdout

  # ---------------------------------------------------------------------------
  # Missing required flag errors
  # ---------------------------------------------------------------------------

  Scenario: Missing --input flag produces error message and exits 1
    When Dmitri Volkov runs "htmltopdf --output result.pdf"
    Then the command exits with code 1
    And stderr contains "Error: --input is required"
    And nothing is written to stdout
    And no file "result.pdf" is created

  Scenario: Missing --output flag produces error message and exits 1
    Given Dmitri Volkov has a file "template.html" containing "<p>Hello</p>"
    When Dmitri runs "htmltopdf --input template.html"
    Then the command exits with code 1
    And stderr contains "Error: --output is required"
    And nothing is written to stdout

  # ---------------------------------------------------------------------------
  # File not found errors
  # ---------------------------------------------------------------------------

  Scenario: Input file not found produces error message and exits 1
    When Dmitri Volkov runs "htmltopdf --input missing-template.html --output out.pdf"
    Then the command exits with code 1
    And stderr contains "Error: input file not found: missing-template.html"
    And nothing is written to stdout
    And no file "out.pdf" is created

  Scenario: Data file not found produces error message and exits 1
    Given Dmitri Volkov has a file "template.html" containing "<p>Hello {{name}}</p>"
    When Dmitri runs "htmltopdf --input template.html --data missing-data.json --output out.pdf"
    Then the command exits with code 1
    And stderr contains "Error: data file not found: missing-data.json"
    And nothing is written to stdout
    And no file "out.pdf" is created

  # ---------------------------------------------------------------------------
  # Template variable missing from data
  # ---------------------------------------------------------------------------

  Scenario: Template variable not present in data file produces error with key name and exits 1
    Given Sofia Esposito has a file "invoice.html" containing "<h1>Invoice #{{invoiceNumber}}</h1><p>Client: {{clientName}}</p>"
    And Sofia has a file "partial-data.json" containing:
      """
      {
        "clientName": "Acme Corp"
      }
      """
    When Sofia runs "htmltopdf --input invoice.html --data partial-data.json --output invoice.pdf"
    Then the command exits with code 1
    And stderr contains "invoiceNumber"
    And stderr contains "Error"
    And nothing is written to stdout
    And no file "invoice.pdf" is created

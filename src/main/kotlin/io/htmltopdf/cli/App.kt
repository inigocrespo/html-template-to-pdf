package io.htmltopdf.cli

import io.htmltopdf.MissingVariableError
import io.htmltopdf.htmlToPdf

object App {
    fun run(args: Array<String>): Int {
        val parseResult = ArgParser.parse(args)
        if (parseResult is ParseResult.Failure) {
            System.err.println(parseResult.message)
            return 1
        }
        val cliArgs = (parseResult as ParseResult.Success).args

        val htmlContent = readFileContent(cliArgs.inputPath, "input") ?: return 1
        val dataMap = resolveDataMap(cliArgs.dataPath) ?: return 1

        try {
            htmlToPdf(html = htmlContent, data = dataMap).use { pdfStream ->
                PdfWriter.write(pdfStream, cliArgs.outputPath)
            }
        } catch (e: MissingVariableError) {
            System.err.println("Error: template variable '${e.key}' not found in data file")
            return 1
        }
        return 0
    }

    private fun readFileContent(path: String, label: String): String? {
        val result = FileReader.read(path)
        if (result is FileReader.ReadResult.NotFound) {
            System.err.println("Error: $label file not found: ${result.path}")
            return null
        }
        return (result as FileReader.ReadResult.Success).content
    }

    private fun resolveDataMap(dataPath: String?): Map<String, String>? {
        if (dataPath == null) return emptyMap()
        val content = readFileContent(dataPath, "data") ?: return null
        return JsonParser.parse(content)
    }
}

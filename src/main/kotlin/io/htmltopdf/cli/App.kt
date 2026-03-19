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

        val readResult = FileReader.read(cliArgs.inputPath)
        if (readResult is FileReader.ReadResult.NotFound) {
            System.err.println("Error: input file not found: ${readResult.path}")
            return 1
        }
        val htmlContent = (readResult as FileReader.ReadResult.Success).content

        val dataMap: Map<String, String> = if (cliArgs.dataPath != null) {
            val dataReadResult = FileReader.read(cliArgs.dataPath)
            if (dataReadResult is FileReader.ReadResult.NotFound) {
                System.err.println("Error: data file not found: ${dataReadResult.path}")
                return 1
            }
            JsonParser.parse((dataReadResult as FileReader.ReadResult.Success).content)
        } else {
            emptyMap()
        }

        try {
            val pdfStream = htmlToPdf(html = htmlContent, data = dataMap)
            PdfWriter.write(pdfStream, cliArgs.outputPath)
        } catch (e: MissingVariableError) {
            System.err.println("Error: template variable '${e.key}' not found in data file")
            return 1
        }
        return 0
    }
}

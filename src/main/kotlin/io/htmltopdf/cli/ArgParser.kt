package io.htmltopdf.cli

data class CliArgs(
    val inputPath: String,
    val dataPath: String?,
    val outputPath: String
)

sealed class ParseResult {
    data class Success(val args: CliArgs) : ParseResult()
    data class Failure(val message: String) : ParseResult()
}

object ArgParser {
    fun parse(args: Array<String>): ParseResult {
        val flagValues = mutableMapOf<String, String>()
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--input", "--data", "--output" -> {
                    if (i + 1 < args.size) {
                        flagValues[args[i]] = args[i + 1]
                        i += 2
                    } else {
                        return ParseResult.Failure("Error: ${args[i]} requires a value")
                    }
                }
                else -> i++
            }
        }

        val input = flagValues["--input"] ?: return ParseResult.Failure("Error: --input is required")
        val output = flagValues["--output"] ?: return ParseResult.Failure("Error: --output is required")
        val data = flagValues["--data"]

        return ParseResult.Success(CliArgs(inputPath = input, dataPath = data, outputPath = output))
    }
}

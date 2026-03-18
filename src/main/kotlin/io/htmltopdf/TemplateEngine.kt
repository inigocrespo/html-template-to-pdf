package io.htmltopdf

object TemplateEngine {
    private val TOKEN_REGEX = Regex("""\{\{(\w+)\}\}""")

    fun resolve(template: String, data: Map<String, String>): String {
        val tokens = TOKEN_REGEX.findAll(template).map { it.groupValues[1] }.toList()

        tokens.forEach { key ->
            if (!data.containsKey(key)) {
                throw MissingVariableError(key, template)
            }
        }

        return TOKEN_REGEX.replace(template) { match ->
            data.getValue(match.groupValues[1])
        }
    }
}

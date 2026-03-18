package io.htmltopdf

object TemplateEngine {
    private val TOKEN_REGEX = Regex("""\{\{(\w+)\}\}""")

    fun resolve(template: String, data: Map<String, String>): String {
        TOKEN_REGEX.findAll(template).forEach { match ->
            val key = match.groupValues[1]
            if (!data.containsKey(key)) throw MissingVariableError(key, template)
        }

        return TOKEN_REGEX.replace(template) { match -> data.getValue(match.groupValues[1]) }
    }
}

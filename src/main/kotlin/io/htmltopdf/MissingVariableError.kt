package io.htmltopdf

class MissingVariableError(
    val key: String,
    val template: String
) : RuntimeException("Template variable '{{$key}}' is missing from the data map")

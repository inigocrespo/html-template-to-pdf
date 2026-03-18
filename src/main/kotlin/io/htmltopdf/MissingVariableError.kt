package io.htmltopdf

class MissingVariableError(val key: String, val template: String) :
    RuntimeException("Missing variable '$key' in template")

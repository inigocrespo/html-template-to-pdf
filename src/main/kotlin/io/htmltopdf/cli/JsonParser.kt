package io.htmltopdf.cli

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonParser {
    fun parse(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()

        val type = object : TypeToken<Map<String, Any>>() {}.type
        val raw: Map<String, Any> = Gson().fromJson(json, type) ?: return emptyMap()

        return raw.filterValues { it is String }.mapValues { it.value as String }
    }
}

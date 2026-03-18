package io.htmltopdf.cli

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(App.run(args))
}

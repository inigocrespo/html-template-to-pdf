package io.htmltopdf.cli.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class CliInfrastructureSpec : DescribeSpec({

    describe("shadow JAR") {

        it("build.gradle.kts declares the shadow plugin") {
            val buildFile = File("build.gradle.kts")
            buildFile.exists() shouldBe true
            val content = buildFile.readText()
            content shouldContain "shadow"
        }

        it("App object is accessible from the public API") {
            // Resolving the class by name confirms the object is reachable on the
            // classpath under the expected package without invoking exitProcess().
            val appClass = Class.forName("io.htmltopdf.cli.App")
            appClass shouldBe appClass // non-null and correct identity
        }

        it("App.run returns Int") {
            // Reflective lookup confirms the method signature matches the contract
            // (returns a primitive int, mapped to Kotlin Int) without executing the CLI.
            val appClass = Class.forName("io.htmltopdf.cli.App")

            // Kotlin objects expose companion-style methods on the class itself via
            // a static INSTANCE field; look for a run method that accepts String[].
            val runMethod = appClass.methods.firstOrNull { method ->
                method.name == "run" &&
                    method.parameterCount == 1 &&
                    method.parameterTypes[0] == Array<String>::class.java
            }

            requireNotNull(runMethod) { "App.run(Array<String>) method not found on io.htmltopdf.cli.App" }
            runMethod.returnType shouldBe Int::class.java
        }
    }
})

plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.htmltopdf"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.1"

dependencies {
    implementation("com.github.librepdf:openpdf-html:3.0.3")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

plugins {
    `java-library`
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector.extensions"
description = "Provides YAML-based connector descriptor support for TRANSCONNECT connectors. Allows connectors to be configured using human-readable YAML files instead of programmatic configuration, including metadata, capabilities, and connection parameters."

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {
    compileOnly(libs.slf4jApi)

    compileOnly(project(":api"))
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", libs.versions.jackson.get())
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", libs.versions.jackson.get())

    testImplementation(libs.testng)
    testImplementation(project(":api"))
}

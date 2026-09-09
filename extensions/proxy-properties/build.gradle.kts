plugins {
    `java-library`
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector.extensions"
description = "Provides HTTP proxy configuration properties for TRANSCONNECT connectors. Enables connectors to communicate through corporate proxies with configurable authentication and connection settings."

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {
    compileOnly(libs.slf4jApi)

    compileOnly(project(":api"))

    testImplementation(libs.testng)
    testImplementation(project(":api"))
    testImplementation(testFixtures(project(":api")))
}

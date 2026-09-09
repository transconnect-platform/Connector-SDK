plugins {
    `java-library`
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector.extensions"
description = "Provides OAuth2 configuration properties support for TRANSCONNECT connectors. Allows declarative configuration of OAuth2 authentication parameters including client credentials, authorization endpoints, and token management."

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {
    compileOnly(libs.slf4jApi)

    compileOnly(project(":api"))

    testImplementation(libs.junit)
    testImplementation(project(":api"))
    testImplementation(testFixtures(project(":api")))
}
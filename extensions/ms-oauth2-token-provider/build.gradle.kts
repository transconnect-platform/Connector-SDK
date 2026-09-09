plugins {
    `java-library`
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector.extensions"
description = "Provides Microsoft OAuth2 token provider implementation using Microsoft Authentication Library (MSAL4J). Enables TRANSCONNECT connectors to authenticate with Microsoft services such as Azure, Microsoft 365, and Dynamics 365 using OAuth2 flows including authorization code, client credentials, and refresh tokens."

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {

    constraints {
        api("com.azure:azure-json:1.5.1")
    }

    compileOnly(libs.slf4jApi)

    compileOnly(project(":api"))
    api(project(":extensions:oauth2-properties"))

    api("com.microsoft.azure:msal4j:1.25.0")

    testImplementation(libs.junit)
    testImplementation(project(":api"))
    testImplementation(testFixtures(project(":api")))
}

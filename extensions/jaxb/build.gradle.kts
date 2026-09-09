
plugins {
    `java-library`
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector.extensions"
description = "Provides JAXB (Java Architecture for XML Binding) support for XML serialization and deserialization in TRANSCONNECT connectors. Enables automatic mapping between XML documents and Java objects using EclipseLink MOXy."

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {
    compileOnly(libs.slf4jApi)

    compileOnly(project(":api"))

    api("org.eclipse.persistence:org.eclipse.persistence.moxy:5.0.0-B10")
    
    // Override vulnerable angus-mail version to fix CVE-2025-7962
    api("org.eclipse.angus:angus-mail:2.0.4")

    // Force compatible ASM version for EclipseLink MOXy
    implementation("org.ow2.asm:asm:9.4")

    testImplementation(libs.junit)
    testImplementation(testFixtures(project(":api")))
    testImplementation(libs.slf4jApi)
}
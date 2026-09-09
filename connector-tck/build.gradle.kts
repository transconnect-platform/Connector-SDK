plugins {
    id("java-library")
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector"
description = "Technology Compatibility Kit (TCK) for testing TRANSCONNECT connector implementations"

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
}

dependencies {
    api(libs.junit.suite)
    api(libs.junit)
    implementation(project(":api"))
    // Explicitly add junit-platform-launcher to avoid version mismatch with Gradle's bundled version
    runtimeOnly("org.junit.platform:junit-platform-launcher:1.13.3")
}

tasks.test {
    useJUnitPlatform()
}
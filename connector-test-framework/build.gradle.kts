plugins {
    id("java")
    id("transconnect.unified-maven-publish")
}

group = "io.transconnect.connector"
description = "Reference testing framework for validating TRANSCONNECT connectors with XML-based test cases"

publishConfig {
    publishToNexus.set(false)
    publishToMavenCentral.set(false)
    generateJavadoc.set(true)
}

dependencies {
    testImplementation(libs.slf4jApi)
    testImplementation(libs.mockito)

    implementation(platform("org.junit:junit-bom:5.13.3"))
    testImplementation(libs.junit.suite)
    testImplementation(libs.junit)
    implementation("org.junit.jupiter:junit-jupiter")
    runtimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(testFixtures(project(":api")))
    testImplementation("org.xmlunit:xmlunit-core:2.11.0")
    testImplementation("org.xmlunit:xmlunit-assertj:2.11.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")

    implementation(libs.junit)
    implementation(libs.mockito)
    implementation(testFixtures(project(":api")))
    implementation("org.xmlunit:xmlunit-core:2.11.0")
    implementation("org.xmlunit:xmlunit-matchers:2.11.0")
    implementation(libs.slf4jApi)
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks.test {
    useJUnitPlatform()
}

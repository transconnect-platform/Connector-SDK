plugins {
    war
    id("com.github.bjornvester.xjc") version "1.8.2"
}

version = "1.0.0"

// Connector SDK version to compile against
val connectorSdkVersion = project.findProperty("connectorSdkVersion") as String? ?: "0.9.6"

repositories {
    mavenCentral()
}

dependencies {

    // Connector SDK
    providedCompile("io.transconnect.connector:api:${connectorSdkVersion}")
    implementation("io.transconnect.connector:war-connector-bridge:${connectorSdkVersion}")
    implementation("io.transconnect.connector.extensions:yaml-descriptor:${connectorSdkVersion}")

    providedCompile("org.slf4j:slf4j-api:1.7.36")

    // JAXB dependencies
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.eclipse.persistence:org.eclipse.persistence.moxy:5.0.0-B10")

    // JUnit 5 for testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Mockito for mocking in tests
    testImplementation("org.mockito:mockito-core:5.4.0")

    // test fixtures for the connector API
    testImplementation(testFixtures("io.transconnect.connector:api:${connectorSdkVersion}"))
}

tasks.test {
    useJUnitPlatform()
}

// Configure XJC to generate Java classes from XSD files
xjc {
    xsdDir.set(file("$projectDir/src/main/resources/com/example/connector"))
}
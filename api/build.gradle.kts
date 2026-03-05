plugins {
    `java-library`
    id("java-test-fixtures")
    id("transconnect.unified-maven-publish")
}

// we need to decide which name we should use
// base.archivesName.set("connector-api")
base.archivesName.set("api")

description = "Core API for building connectors for the TRANSCONNECT platform"

publishConfig {
    publishToNexus.set(true)
    publishToMavenCentral.set(true)
}

// Copy JavaDoc to Antora documentation (EN and DE)
val copyJavadocToAntora by tasks.registering {
    dependsOn(tasks.javadoc)
    group = "documentation"
    doLast {
        val javadocDir = tasks.javadoc.get().destinationDir

        copy {
            from(javadocDir)
            into(layout.projectDirectory.dir("../documentation/en/modules/ROOT/attachments/javadoc").asFile)
        }
        copy {
            from(javadocDir)
            into(layout.projectDirectory.dir("../documentation/de/modules/ROOT/attachments/javadoc").asFile)
        }
    }
}

dependencies {
    testImplementation(libs.testng)
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation(libs.junit)
}

tasks.test {
    useJUnitPlatform()
}

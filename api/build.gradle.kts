plugins {
    `java-library`
    id("java-test-fixtures")
}

base.archivesName.set("connector-api")

java {
    withJavadocJar()
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
}

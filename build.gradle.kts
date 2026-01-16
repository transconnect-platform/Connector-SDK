plugins {
    alias(libs.plugins.lombok) apply false
}

group = "io.transconnect.connector"
version = "0.9.6"
description = "SDK for building connectors for TRANSCONNECT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
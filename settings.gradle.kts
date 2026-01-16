plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "transconnect-connector-sdk"

include("api", "documentation:tutorial-example")

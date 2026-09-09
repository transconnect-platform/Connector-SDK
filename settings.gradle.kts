plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"

}

rootProject.name = "transconnect-connector-sdk"

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral {}
    }
}

include(
    "api",
    "connector-test-framework",
    "extensions:proxy-properties",
    "extensions:yaml-descriptor",
    "extensions:jaxb",
    "extensions:oauth2-properties",
    "extensions:ms-oauth2-token-provider",
    "connector-tck")

# Gradle Convention Plugins

> Reusable build configurations for the TRANSCONNECT Connector SDK

This directory contains Gradle convention plugins that standardize build configuration across all subprojects.

## Available Plugins

### 1. `transconnect.java-conventions`

Configures Java projects with common settings.

**What it does:**
- Java 17 toolchain with automatic JDK provisioning
- Testing frameworks (TestNG/JUnit auto-detection)
- Code coverage (JaCoCo)
- Code quality (Checkstyle)
- Code formatting (Spotless with Palantir Java Format)
- Lombok support
- Reproducible builds

**Usage:**

```kotlin
plugins {
    id("transconnect.java-conventions")
}
```

This plugin automatically applies the `transconnect.unified-maven-publish` plugin, so you don't need to apply it separately.

### 2. `transconnect.unified-maven-publish`

Handles publishing to Nexus (internal) and Maven Central (public) using the Vanniktech Maven Publish Plugin.

**What it does:**
- Publishes to Nexus (snapshots and releases)
- Publishes to Maven Central (releases only)
- Automatic version calculation from Git tags
- GPG signing with in-memory keys
- Generates sources and Javadoc JARs
- Automatic POM generation

**Version Calculation:**

- **Git tag** `v1.2.3` → `1.2.3` (release - publishes to Nexus + Maven Central)
- **Branch** `main` → `1.0.1-main-SNAPSHOT` (snapshot - publishes to Nexus only)

**Configuration:**

Set credentials via GRADLE_OPTS in CI/CD:

```yaml
GRADLE_OPTS: >-
  -DmavenSqlUsername=${TCCI_NEXUS_MAVEN_USERNAME}
  -DmavenSqlPassword=${TCCI_NEXUS_MAVEN_PASSWORD}
  -DmavenCentralUsername=${TCCI_MAVEN_CENTRAL_USERNAME}
  -DmavenCentralPassword=${TCCI_MAVEN_CENTRAL_PASSWORD}
  -DsigningInMemoryKeyId=${TCCI_GPG_SIGNING_KEY_ID}
  -DsigningInMemoryKeyPassword=${TCCI_GPG_SIGNING_KEY_PASSPHRASE}
```

For the signing key (requires base64 decoding):

```bash
export SIGNING_KEY=$(echo "${TCCI_GPG_SIGNING_KEY}" | base64 -d)
gradle publishToMavenCentral -DsigningInMemoryKey="${SIGNING_KEY}"
```

**Required CI/CD Variables:**

```yaml
# Nexus
TCCI_NEXUS_MAVEN_USERNAME
TCCI_NEXUS_MAVEN_PASSWORD

# Maven Central
TCCI_MAVEN_CENTRAL_USERNAME
TCCI_MAVEN_CENTRAL_PASSWORD

# GPG Signing
TCCI_GPG_SIGNING_KEY              # Base64-encoded private key
TCCI_GPG_SIGNING_KEY_ID           # Key ID (e.g., 9D115F104D68B48F)
TCCI_GPG_SIGNING_KEY_PASSPHRASE   # Key passphrase
```

**Gradle Tasks:**

```bash
# Publish to Nexus (requires Nexus credentials)
./gradlew publishAllPublicationsToMavenSqlRepository

# Publish to Maven Central (requires Maven Central credentials, releases only)
./gradlew publishAllPublicationsToMavenCentralRepository

# Print version
./gradlew printVersion
```

**Notes:**
- **Nexus repository**: Only registered when `mavenSqlUsername` and `mavenSqlPassword` are configured. The `publishAllPublicationsToMavenSqlRepository` task will not be available without credentials.
- **Maven Central publishing**: Automatically enabled for release versions (non-SNAPSHOT) when `mavenCentralUsername` and `mavenCentralPassword` are configured.
- **GPG signing**: Automatically enabled when `signingInMemoryKeyId` and `signingInMemoryKeyPassword` are configured (and either `signingInMemoryKey` or `signing.secretKeyRingFile`).

## Project Structure

```
buildSrc/
├── build.gradle.kts
├── src/main/kotlin/
│   ├── TransconnectJavaConventions.kt
│   └── UnifiedMavenPublishConventions.kt
└── src/main/resources/META-INF/gradle-plugins/
    ├── transconnect.java-conventions.properties
    └── transconnect.unified-maven-publish.properties
```

## Local Development

For local development, add credentials to `~/.gradle/gradle.properties`:

```properties
# Nexus Maven Repository (Vanniktech pattern)
mavenSqlUsername=your-username
mavenSqlPassword=your-password

# Maven Central Publishing (Vanniktech property names)
mavenCentralUsername=your-token
mavenCentralPassword=your-token-password

# GPG Signing (Vanniktech in-memory signing property names)
signingInMemoryKeyId=your-key-id
signingInMemoryKeyPassword=your-key-passphrase

# For local file-based signing (standard Gradle signing plugin)
signing.keyId=your-key-id
signing.password=your-key-passphrase
signing.secretKeyRingFile=/path/to/secring.gpg
```

## Dependencies

The plugins use:
- **Vanniktech Maven Publish Plugin** - Unified publishing
- **Spotless** - Code formatting
- **Lombok** - Reduce boilerplate
- **JaCoCo** - Code coverage
- **Checkstyle** - Code quality

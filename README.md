# TRANSCONNECT Connector SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.transconnect.connector/api)](https://central.sonatype.com/artifact/io.transconnect.connector/api)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)

> A powerful and extensible SDK for building custom connectors for the TRANSCONNECT integration platform.

The TRANSCONNECT Connector SDK enables developers to create custom integration connectors that seamlessly connect external systems with the TRANSCONNECT platform. Build producers to send data into TRANSCONNECT or consumers to process outbound messages with a flexible, well-documented API.

## Features

- **Producer Connectors**: Create connectors that push data into TRANSCONNECT for processing
- **Consumer Connectors**: Build connectors that receive and process messages from TRANSCONNECT
- **Extensible Architecture**: Plugin-based design with support for custom extensions
- **Type-Safe API**: Strongly-typed Java interfaces with comprehensive validation
- **Testing Framework**: Integrated TCK (Technology Compatibility Kit) for connector validation

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.transconnect.connector</groupId>
    <artifactId>api</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.transconnect.connector:api:${version}")
}
```

### Gradle (Groovy)

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.transconnect.connector:api:${version}'
}
```

## Quick Start

### Creating a Producer Connector

A producer connector sends data into TRANSCONNECT:

```java
public class MyProducerConnector implements ProducerConnector {
    @Override
    public void initialize(ConnectorContext context) {
        // Initialize your connector
    }

    @Override
    public ProducerResult produce() {
        // Fetch data from your external system
        String data = fetchDataFromExternalSystem();

        return ProducerResult.success(data);
    }
}
```

### Creating a Consumer Connector

A consumer connector processes messages from TRANSCONNECT:

```java
public class MyConsumerConnector implements ConsumerConnector {
    @Override
    public void initialize(ConnectorContext context) {
        // Initialize your connector
    }

    @Override
    public ConsumerResult consume(Message message) {
        // Process the message
        processMessage(message);

        return ConsumerResult.success();
    }
}
```

## Documentation

Comprehensive documentation is available in the `/documentation` folder:

- [Getting Started Guide](documentation/en/modules/ROOT/pages/introduction.adoc) - Introduction and core concepts
- [Architecture Overview](documentation/en/modules/ROOT/pages/architecture.adoc) - SDK design and architecture
- [Producer Connectors](documentation/en/modules/ROOT/pages/producer/intro.adoc) - Building message producers
- [Consumer Connectors](documentation/en/modules/ROOT/pages/consumer/intro.adoc) - Building message consumers
- [Configuration Reference](documentation/en/modules/ROOT/pages/reference/configuration.adoc) - Configuration options
- [How-to Guides](documentation/en/modules/ROOT/pages/howto/intro.adoc) - Step-by-step tutorials
- [API Documentation](https://javadoc.io/doc/io.transconnect.connector/api) - Complete API reference

### Building Documentation Locally

The documentation uses Antora. To build it locally:

```bash
cd documentation
npm install
npm run build
```

The built documentation will be available in the `documentation/site/` directory.

## Project Structure

```
transconnect-connector-sdk/
├── api/                          # Core SDK API
└── documentation/                # Antora documentation
```

## Building from Source

### Prerequisites

- Java 17 or higher
- Gradle 8+ (wrapper included)

### Build Commands

```bash
# Build the entire project
./gradlew build

# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

## Contributing

We welcome contributions from the community! Here's how you can help:

### Reporting Issues

If you find a bug or have a feature request:

1. Check the [issue tracker](https://github.com/transconnect-platform/Connector-SDK/issues) for existing issues
2. Create a new issue with a clear description and steps to reproduce (for bugs)
3. Use the appropriate issue template

### Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes and add tests
4. Ensure all tests pass: `./gradlew test`
5. Commit with clear messages: `git commit -m "Add feature: description"`
6. Push to your fork: `git push origin feature/my-feature`
7. Open a Pull Request

### Code Standards

- Follow Java coding conventions
- Write tests for new functionality
- Update documentation for API changes
- Use descriptive commit messages
- Ensure builds pass before submitting PRs

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.

## Support

- **Documentation**: [Full documentation](documentation/)
- **Issues**: [GitHub Issues](https://github.com/transconnect-platform/Connector-SDK/issues)
- **Website**: [transconnect.io](https://transconnect.io)

---

**Built with ❤️ by the TRANSCONNECT Team**


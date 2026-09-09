# TRANSCONNECT Connector SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.transconnect.connector/api)](https://central.sonatype.com/artifact/io.transconnect.connector/api)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.md)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)

> A powerful and extensible SDK for building custom connectors for the TRANSCONNECT integration platform.

The TRANSCONNECT Connector SDK enables developers to create custom integration connectors that seamlessly connect
external systems with the TRANSCONNECT platform. Build producers to send data into TRANSCONNECT or consumers to process
outbound messages with a flexible, well-documented API.

## Features

- **Producer Connectors**: Create connectors that push data into TRANSCONNECT for processing
- **Consumer Connectors**: Build connectors that receive and process messages from TRANSCONNECT
- **Extensible Architecture**: Plugin-based design with support for custom extensions
- **Type-Safe API**: Strongly-typed Java interfaces with comprehensive validation
- **Reference Test Framework**: Validate connectors against XML-based reference test cases
- **TCK**: Technology Compatibility Kit enforcing API compliance

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

    // optional: validate your connector against reference test cases
    testImplementation("io.transconnect.connector:connector-test-framework:${version}")
    // optional: validate API compliance
    testImplementation("io.transconnect.connector:connector-tck:${version}")
}
```

### Gradle (Groovy)

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.transconnect.connector:api:${version}'
}
```

### Extensions

Extensions are published separately under the `io.transconnect.connector.extensions` group:

| Artifact                   | Purpose                                                          |
|----------------------------|------------------------------------------------------------------|
| `jaxb`                     | XML marshalling/unmarshalling with schema validation             |
| `proxy-properties`         | Auto-injects proxy configuration properties                      |
| `yaml-descriptor`          | Define connector descriptors in YAML instead of Java             |
| `oauth2-properties`        | OAuth2 client-credentials configuration properties               |
| `ms-oauth2-token-provider` | Microsoft OAuth2 token provider (MSAL4J)                         |

## Quick Start

### Creating a Producer Connector

A producer connector generates messages that flow into TRANSCONNECT. The connector describes itself and creates
connections; the connection produces messages and hands them to the runtime through a
`ProducerConnectorListener`:

```java
public class DateTimeConnector implements ProducerConnector {

    @Override
    public ProducerConnectorDescriptor getDescription() {
        CommonConnectorDescriptor common = CommonConnectorDescriptor.builder()
                .type(URI.create("urn:example:connector:datetime-producer"))
                .version("1.0.0")
                .vendor("Example Corp")
                .displayName(new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "DateTime Producer")})
                .build();

        return ProducerConnectorDescriptor.builder()
                .common(common)
                .producedMessageXsd(URI.create("/com/example/connector/produced.xsd"))
                .build();
    }

    @Override
    public ProducerConnection createConnection(Context context) {
        return new DateTimeConnection(context);
    }
}
```

```java
public class DateTimeConnection implements ProducerConnection {

    private ProducerConnectorListener listener;

    @Override
    public void connect(MessageFactory<WritableMessage> factory, ProducerConnectorListener listener) {
        this.listener = listener;
        // start producing, then signal that the connection is up
        listener.onConnect();
    }

    private void produce(MessageFactory<WritableMessage> factory) {
        WritableMessage message = factory.createMessage();
        try (var out = message.getBodyOutputStream()) {
            out.write("<ROOT><DATETIME>%s</DATETIME></ROOT>"
                    .formatted(LocalDateTime.now())
                    .getBytes(StandardCharsets.UTF_8));
            listener.onMessage(message);
        } catch (IOException e) {
            listener.onError(e);
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        listener.onDisconnect();
    }
}
```

### Creating a Consumer Connector

A consumer connector processes messages coming from TRANSCONNECT. Its descriptor declares one or more
`Interaction`s, and the connection executes the interaction identified by its ID:

```java
public class CountCharactersConnector implements ConsumerConnector {

    @Override
    public ConsumerConnectorDescriptor getDescription() {
        CommonConnectorDescriptor common = CommonConnectorDescriptor.builder()
                .type(URI.create("urn:example:connector:count-characters-consumer"))
                .version("1.0.0")
                .vendor("Example Corp")
                .displayName(new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Count Characters")})
                .build();

        Interaction countCharacters = Interaction.builder()
                .id(URI.create("countCharacters"))
                .inMessageXsd(URI.create("/com/example/connector/in.xsd"))
                .outMessageXsd(URI.create("/com/example/connector/out.xsd"))
                .build();

        return ConsumerConnectorDescriptor.builder()
                .common(common)
                .interactions(new Interaction[] {countCharacters})
                .build();
    }

    @Override
    public ConsumerConnection createConnection(Context context) {
        return new CountCharactersConnection(context);
    }
}
```

```java
public class CountCharactersConnection implements ConsumerConnection {

    @Override
    public void connect() {
        // no connection setup needed
    }

    @Override
    public void execute(URI interactionId, Message input, WritableMessage output)
            throws TransconnectConnectorException {
        if (!"countCharacters".equals(interactionId.toString())) {
            throw new TransconnectConnectorException("Unknown interaction: '%s'".formatted(interactionId));
        }
        try (var out = output.getBodyOutputStream()) {
            long count = countCharacters(input.getXmlBody());
            out.write("<ROOT><COUNT>%d</COUNT></ROOT>".formatted(count).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new TransconnectConnectorException("Error processing message", e);
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        // no resources to release
    }
}
```

A complete, buildable version of both connectors lives in
[`documentation/tutorial-example`](documentation/tutorial-example/).

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
├── connector-test-framework/     # XML-based reference testing framework
├── connector-tck/                # Technology Compatibility Kit
├── extensions/                   # Optional add-ons (jaxb, proxy, yaml, oauth2, ...)
└── documentation/                # Antora documentation and tutorial example
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

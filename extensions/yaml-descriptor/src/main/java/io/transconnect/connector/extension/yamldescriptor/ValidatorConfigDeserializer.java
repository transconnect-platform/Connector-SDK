/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.yamldescriptor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.transconnect.connector.api.property.validator.IValidatorTypeConfig;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import io.transconnect.connector.api.property.validator.ValidatorType;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom deserializer for ValidatorConfig objects. Handles the conversion of YAML
 * data into ValidatorConfig instances, supporting various validator types and their
 * specific configurations.
 */
public class ValidatorConfigDeserializer extends StdDeserializer<ValidatorConfig> {

    private static final String TYPE_FIELD = "type";
    private static final String CONFIG_FIELD = "config";

    private static final Map<ValidatorType, Class<? extends IValidatorTypeConfig>> TYPE_TO_CONFIG_MAP = Stream.of(
                    ValidatorType.values())
            .collect(Collectors.toUnmodifiableMap(Function.identity(), ValidatorType::getConfigClass));

    /**
     * Default constructor.
     */
    public ValidatorConfigDeserializer() {
        this(null);
    }

    /**
     * Constructor with class parameter.
     *
     * @param vc the Class instance that represents the type being deserialized
     */
    public ValidatorConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Deserializes YAML content into a ValidatorConfig object.
     *
     * @param jp JsonParser that points to the start of YAML content
     * @param ctxt Context for the deserialization process
     * @return ValidatorConfig instance containing the deserialized data
     * @throws IOException if there's an error reading from JsonParser
     * @throws InvalidFormatException if the validator type is invalid or required fields are missing
     */
    @Override
    public ValidatorConfig deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);

        ValidatorType type = parseValidatorType(node)
                .orElseThrow(() ->
                        new InvalidFormatException(jp, "Missing or invalid validator type", node, ValidatorType.class));

        IValidatorTypeConfig config = parseConfig(mapper, node, type);

        return ValidatorConfig.builder().type(type).config(config).build();
    }

    /**
     * Parses the validator type from the JSON node.
     *
     * @param node JSON node containing the validator configuration
     * @return Optional containing the ValidatorType if present and valid
     */
    private Optional<ValidatorType> parseValidatorType(JsonNode node) {
        return Optional.ofNullable(node.get(TYPE_FIELD)).map(JsonNode::asText).map(typeStr -> {
            try {
                return ValidatorType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    /**
     * Parses the configuration section of the validator.
     *
     * @param mapper ObjectMapper instance for JSON parsing
     * @param node JSON node containing the validator configuration
     * @param type The type of validator being configured
     * @return Parsed configuration object or null if no configuration is present
     * @throws IOException if there's an error parsing the configuration
     */
    private IValidatorTypeConfig parseConfig(ObjectMapper mapper, JsonNode node, ValidatorType type)
            throws IOException {
        JsonNode configNode = node.get(CONFIG_FIELD);

        if (configNode == null || configNode.isNull()) {
            return null;
        }
        Class<? extends IValidatorTypeConfig> configClass = TYPE_TO_CONFIG_MAP.get(type);

        if (configClass == null) {
            return null;
        }
        return mapper.treeToValue(configNode, configClass);
    }
}

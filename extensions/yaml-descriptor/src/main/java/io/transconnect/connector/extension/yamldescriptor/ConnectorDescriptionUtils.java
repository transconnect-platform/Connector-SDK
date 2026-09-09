/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.yamldescriptor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.transconnect.connector.api.CommonConnectorDescriptor;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.IConnectorProperty;
import io.transconnect.connector.api.property.SelectConnectorProperty;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains some utility functions to deal with instances of {@link CommonConnectorDescriptor}.
 */
public final class ConnectorDescriptionUtils {

    private ConnectorDescriptionUtils() {}

    /**
     * Custom deserializer class for connector properties.
     */
    static class ConnectorPropertyDeserializer extends StdDeserializer<IConnectorProperty> {

        ConnectorPropertyDeserializer() {
            this(null);
        }

        /**
         * Creates a deserializer for the given value class.
         *
         * @param vc the class to deserialize into, may be {@code null}
         */
        protected ConnectorPropertyDeserializer(Class<?> vc) {
            super(vc);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Deserializes into {@link SelectConnectorProperty} if the node declares the type
         * {@code SELECT}, and into {@link ConnectorProperty} otherwise.
         */
        @Override
        public IConnectorProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            ObjectNode node = parser.getCodec().readTree(parser);

            // create connector property with respect to the type of the given node
            Class<? extends IConnectorProperty> connectorPropertyClass;

            if (node.has("type") && node.get("type").asText().equals("SELECT")) {
                connectorPropertyClass = SelectConnectorProperty.class;
            } else {
                connectorPropertyClass = ConnectorProperty.class;
            }
            return parser.getCodec().treeToValue(node, connectorPropertyClass);
        }
    }

    /**
     * Create an instance of {@link ConsumerConnectorDescriptor} from the given YAML input stream.
     *
     * @param yamlIn an {@link InputStream} containing a YAML description of a {@link ConsumerConnectorDescriptor} instance
     * @return an instance of {@link ConsumerConnectorDescriptor}
     * @throws RuntimeException in case no {@link ConsumerConnectorDescriptor} instance can be created from the given YAML
     */
    public static ConsumerConnectorDescriptor createConsumerDescriptionFromYaml(InputStream yamlIn) {
        return createFromYaml(yamlIn, ConsumerConnectorDescriptor.class);
    }

    /**
     * Create an instance of {@link ProducerConnectorDescriptor} from the given YAML input stream.
     *
     * @param yamlIn an {@link InputStream} containing a YAML description of a {@link ProducerConnectorDescriptor} instance
     * @return an instance of {@link ProducerConnectorDescriptor}
     * @throws RuntimeException in case no {@link ProducerConnectorDescriptor} instance can be created from the given YAML
     */
    public static ProducerConnectorDescriptor createProducerDescriptionFromYaml(InputStream yamlIn) {
        return createFromYaml(yamlIn, ProducerConnectorDescriptor.class);
    }

    private static <T> T createFromYaml(InputStream yamlIn, Class<T> resultClass) {
        ObjectMapper mapper = YAMLMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true)
                .build();

        SimpleModule module = new SimpleModule();

        // register custom deserializer for IConnectorProperty
        module.addDeserializer(IConnectorProperty.class, new ConnectorDescriptionUtils.ConnectorPropertyDeserializer());
        // register custom deserializer for ValidatorConfig
        module.addDeserializer(ValidatorConfig.class, new ValidatorConfigDeserializer());

        mapper.registerModule(module);
        try {
            return mapper.readValue(yamlIn, resultClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

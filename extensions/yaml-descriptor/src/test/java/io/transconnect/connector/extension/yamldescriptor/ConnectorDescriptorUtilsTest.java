/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.yamldescriptor;

import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorPropertyConstraintOperation;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.PropertyDependency;
import io.transconnect.connector.api.property.SelectConnectorProperty;
import io.transconnect.connector.api.property.validator.RegexValidatorConfig;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import io.transconnect.connector.api.property.validator.ValidatorType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectorDescriptorUtilsTest {

    @Test
    public void testCreateFromYaml() throws IOException, URISyntaxException {
        ConsumerConnectorDescriptor metaData = ConnectorDescriptionUtils.createConsumerDescriptionFromYaml(
                ConnectorDescriptorUtilsTest.class.getResourceAsStream("/connector.yaml"));

        Assert.assertEquals(metaData.getCommon().getType(), new URI("urn:transconnect:connector:sample"));
        Assert.assertEquals(metaData.getCommon().getVersion(), "1.0.0");
        Assert.assertEquals(metaData.getCommon().getVendor(), "SQL Projekt AG");

        Assert.assertEquals(metaData.getCommon().getLabels().length, 2);
        Assert.assertEquals(metaData.getCommon().getLabels()[0], "sample");
        Assert.assertEquals(metaData.getCommon().getLabels()[1], "greeting");

        Assert.assertEquals(metaData.getCommon().getDisplayName().length, 2);
        Assert.assertEquals(metaData.getCommon().getDisplayName()[0].getLocale(), Locale.GERMAN);
        Assert.assertEquals(metaData.getCommon().getDisplayName()[0].getMessage(), "Beispiel Connector");
        Assert.assertEquals(metaData.getCommon().getDisplayName()[1].getLocale(), Locale.ENGLISH);
        Assert.assertEquals(metaData.getCommon().getDisplayName()[1].getMessage(), "Sample Connector");

        Assert.assertEquals(metaData.getCommon().getDescription().length, 2);

        Assert.assertEquals(metaData.getInteractions().length, 2);
        Assert.assertEquals(metaData.getInteractions()[0].getId(), new URI("urn:transconnect:connector:sample:hello"));
        Assert.assertEquals(metaData.getInteractions()[0].getDisplayName().length, 2);
        Assert.assertEquals(metaData.getInteractions()[0].getDisplayName()[0].getMessage(), "Hallo");
        Assert.assertEquals(metaData.getInteractions()[0].getDescription().length, 2);
        Assert.assertEquals(
                metaData.getInteractions()[0].getDescription()[0].getMessage(), "Begrüßt den Nutzer mit Hallo");
        Assert.assertEquals(metaData.getInteractions()[0].getInMessageXsd(), new URI("in.xsd"));
        Assert.assertEquals(metaData.getInteractions()[0].getOutMessageXsd(), new URI("out.xsd"));

        Assert.assertEquals(metaData.getProperties().length, 2);
        Assert.assertEquals(metaData.getProperties()[0].getId(), "sample-property");
        Assert.assertEquals(metaData.getProperties()[0].getDisplayName().length, 2);
        Assert.assertEquals(metaData.getProperties()[0].getDescription().length, 2);
        Assert.assertEquals(metaData.getProperties()[0].getType(), ConnectorPropertyType.TEXT);
        Assert.assertEquals(metaData.getProperties()[0].getDefaultValue(), 123);
        Assert.assertEquals(metaData.getProperties()[0].isRequired(), false);

        // Validator config assertions
        ValidatorConfig validatorConfig = metaData.getProperties()[0].getValidator();
        Assert.assertNotNull(validatorConfig, "Validator config should not be null");
        Assert.assertEquals(validatorConfig.getType(), ValidatorType.REGEX);

        Assert.assertTrue(
                validatorConfig.getConfig() instanceof RegexValidatorConfig, "Config should be of type RegexI");
        RegexValidatorConfig regexConfig = (RegexValidatorConfig) validatorConfig.getConfig();
        Assert.assertEquals(regexConfig.getPattern(), "[a-zA-Z0-9]+");

        // Test sample-property dependencies
        Assert.assertNotNull(metaData.getProperties()[0].getDependsOn(), "Dependencies should not be null");
        Assert.assertEquals(metaData.getProperties()[0].getDependsOn().length, 2, "Should have 2 dependency rules");

        // Test first dependency rule
        PropertyDependency firstRule = metaData.getProperties()[0].getDependsOn()[0];
        Assert.assertEquals(firstRule.getOperation(), ConnectorPropertyConstraintOperation.MATCHES);
        Assert.assertEquals(firstRule.getValue(), "[0-9]+");
        Assert.assertEquals(firstRule.getConstraints().length, 1);

        // Test second dependency rule
        PropertyDependency secondRule = metaData.getProperties()[0].getDependsOn()[1];
        Assert.assertEquals(secondRule.getOperation(), ConnectorPropertyConstraintOperation.MATCHES);
        Assert.assertEquals(secondRule.getValue(), "[a-zA-Z]+");
        Assert.assertEquals(secondRule.getConstraints().length, 1);

        Assert.assertEquals(metaData.getProperties()[1].getId(), "language");
        Assert.assertEquals(metaData.getProperties()[1].getDisplayName().length, 2);
        Assert.assertEquals(metaData.getProperties()[1].getDescription().length, 2);
        Assert.assertEquals(metaData.getProperties()[1].getType(), ConnectorPropertyType.SELECT);
        Assert.assertEquals(((SelectConnectorProperty) metaData.getProperties()[1]).getEntries().length, 3);
        Assert.assertEquals(((SelectConnectorProperty) metaData.getProperties()[1]).getEntries()[0].getId(), "German");
        Assert.assertEquals(
                ((SelectConnectorProperty) metaData.getProperties()[1]).getEntries()[0].getDisplayName().length, 2);
        Assert.assertEquals(
                ((SelectConnectorProperty) metaData.getProperties()[1]).getEntries()[0].getDisplayName()[0].getLocale(),
                Locale.GERMAN);
        Assert.assertEquals(
                ((SelectConnectorProperty) metaData.getProperties()[1])
                        .getEntries()[0].getDisplayName()[0].getMessage(),
                "Deutsch");
        Assert.assertEquals(
                ((SelectConnectorProperty) metaData.getProperties()[1]).getEntries()[0].getDisplayName()[1].getLocale(),
                Locale.ENGLISH);
        Assert.assertEquals(
                ((SelectConnectorProperty) metaData.getProperties()[1])
                        .getEntries()[0].getDisplayName()[1].getMessage(),
                "German");
        Assert.assertEquals(metaData.getProperties()[1].getDefaultValue(), "German");
        Assert.assertEquals(metaData.getProperties()[1].isRequired(), false);
    }
}

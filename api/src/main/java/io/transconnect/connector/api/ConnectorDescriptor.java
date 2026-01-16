/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import io.transconnect.connector.api.property.IConnectorProperty;

public interface ConnectorDescriptor {
    /**
     * Returns the common connector properties, like name and version.
     *
     * @return an instance of {@link CommonConnectorDescriptor}
     */
    CommonConnectorDescriptor getCommon();

    /**
     * Returns the array of property specifications to configure the Connector.
     *
     * @return an array of {@link IConnectorProperty} instances
     */
    IConnectorProperty[] getProperties();
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository
 */
public interface PhysicalConnectorRepository extends JpaRepository<PhysicalConnector, Integer>
{

    /**
     * Gets all physical connector of the provided environment.
     *
     * @param environment {@link Environment}
     * @return List of Physical connector.
     */
    List<PhysicalConnector> findByEnvironment(String environment);

    /**
     * Gets all physical connector of the provided connector type.
     *
     * @param connectorTypeName connector type name of the physical connector
     * @return List of Physical connector filter by connector type
     */
    List<PhysicalConnector> findByConnectorTypeName(String connectorTypeName);



    /**
     * Gets all physical connector of the provided environment and connector type.
     *
     * @param environment   {@link Environment}
     * @param connectorTypeName connector type name of the physical connector
     * @return List of Physical connector.
     */
    List<PhysicalConnector> findByEnvironmentAndConnectorTypeName(String environment, String connectorTypeName);

    /**
     * Gets the only {@link PhysicalConnector} of a {@link Product}
     * of the same name on a given {@link Environment}.
     *
     * @param environment {@link Environment}
     * @param name        Name of the {@link PhysicalConnector}
     * @return PhysicalConnector
     */
    PhysicalConnector findByEnvironmentAndName(
            String environment,
            String name);

    /**
     * Gets all physical connector of the provided environment and virtual IP
     *
     * @param environment {@link Environment}
     * @param virtualIp   virtual IP
     * @return List of Physical connector.
     */
    PhysicalConnector findByEnvironmentAndVirtualIp(String environment, String virtualIp);
}


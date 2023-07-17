package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Connector type repository
 *
 * @author BBVA - XE30432
 */
public interface ConnectorTypeRepository extends JpaRepository<ConnectorType, Integer>
{
    /**
     * Find a conector type filter by name
     *
     * @param name name of the connector type
     * @return a connector type instance filter by name
     */
    ConnectorType findByName(final String name);
}

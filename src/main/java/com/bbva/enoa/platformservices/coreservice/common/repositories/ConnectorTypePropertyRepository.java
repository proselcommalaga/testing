package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Connector type property repository
 *
 * @author BBVA - XE30432
 */
public interface ConnectorTypePropertyRepository extends JpaRepository<ConnectorTypeProperty, Integer>
{

}

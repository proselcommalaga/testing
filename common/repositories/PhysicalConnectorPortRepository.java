package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Connector port repository
 *
 * @author BBVA - XE30432
 */
public interface PhysicalConnectorPortRepository extends JpaRepository<PhysicalConnectorPort, Integer>
{
}

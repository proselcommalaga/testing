package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Logical connector property repository
 *
 * @author BBVA - XE30432
 */
public interface LogicalConnectorPropertyRepository extends JpaRepository<LogicalConnectorProperty, Integer>
{
}

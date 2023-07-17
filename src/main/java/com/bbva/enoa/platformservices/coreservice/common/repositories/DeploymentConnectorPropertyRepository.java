package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repo for {@link DeploymentTypeChangeTask}.
 */
public interface DeploymentConnectorPropertyRepository extends JpaRepository<DeploymentConnectorProperty, Integer>
{
    /**
     * Delete all the logical connector properties from the list provided
     *
     * @param logicalConnectorPropertyList logical connector property list to delete
     */
    void deleteByLogicalConnectorPropertyIn(List<LogicalConnectorProperty> logicalConnectorPropertyList);

    /**
     * Delete by property id
     *
     * @param logicalConnectorPropertyId property id
     * @return number of items deleted
     */
    long deleteByLogicalConnectorPropertyId(final Integer logicalConnectorPropertyId);
}

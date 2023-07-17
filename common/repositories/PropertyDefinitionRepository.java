package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.config.entities.PropertyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author xe30000
 */
public interface PropertyDefinitionRepository extends JpaRepository<PropertyDefinition, Integer>
{
}

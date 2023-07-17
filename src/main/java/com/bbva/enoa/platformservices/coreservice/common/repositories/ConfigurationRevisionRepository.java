package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author xe30000
 */
public interface ConfigurationRevisionRepository  extends JpaRepository<ConfigurationRevision, Integer>
{

}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Configuration value repository
 *
 * @author BBVA - XE30432
 */
public interface ConfigurationValueRepository extends JpaRepository<ConfigurationValue, Integer>
{
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.config.entities.PlatformDiscoveryConfig;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for Platform Discovery configurations
 */
public interface PlatformDiscoveryConfigRepository extends JpaRepository<PlatformDiscoveryConfig, Integer>
{
	/**
	 * Method to find the configuration by name, environment and platform
	 *
	 * @param name        name of the configuration
	 * @param environment environment of the configuration
	 * @param platform    platform of the configuration
	 * @return the configuration
	 */
	@Query("select value from com.bbva.enoa.datamodel.model.config.entities.PlatformDiscoveryConfig as pdc " +
			"where pdc.name = :name and pdc.environment = :environment and pdc.platform = :platform")
	String findByNameAndEnvironmentAndPlatform(String name, String environment, Platform platform);
}

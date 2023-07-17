package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BSConfigurationFilesystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BSConfigurationFilesystemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BSConfigurationFilesystemRepository extends JpaRepository<BSConfigurationFilesystem, BSConfigurationFilesystemId>
{

    List<BSConfigurationFilesystem> findByBehaviorServiceConfigurationId(Integer behaviorServiceConfiguration);
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalUserPermissionConfigRepository extends JpaRepository<ExternalUserPermissionConfig, Integer>
{

}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalUserPermissionRepository extends JpaRepository<ExternalUserPermission, Integer>
{
    List<ExternalUserPermission> findByProductIdAndEnvironment(Integer productId, String environment);
}

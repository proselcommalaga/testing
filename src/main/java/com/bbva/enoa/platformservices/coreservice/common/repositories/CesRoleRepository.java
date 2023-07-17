package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface CesRoleRepository extends JpaRepository<CesRole, Integer>
{

    Set<CesRole> findAllByUuaaAndEnvironment(String uuaa, String environment);
}

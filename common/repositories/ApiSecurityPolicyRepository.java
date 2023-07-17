package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SyncApiSecurityPolicy Repository
 */
public interface ApiSecurityPolicyRepository extends JpaRepository<ApiSecurityPolicy, Integer>
{
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface ApiMethodRepository extends JpaRepository<ApiMethod, Integer>
{
}

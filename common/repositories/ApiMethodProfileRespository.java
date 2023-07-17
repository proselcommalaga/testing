package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiMethodProfileRespository extends JpaRepository<ApiMethodProfile, Integer>
{
    ApiMethodProfile findByPlanProfileAndApiMethod(final PlanProfile planProfile, final ApiMethod novaApiMethod);
}

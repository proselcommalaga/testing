package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodDto;
import com.bbva.enoa.datamodel.model.api.entities.IProfilableApiVersion;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;

interface IProfilableDtoBuilder
{
    /**
     * Builds the list of Methods Dto for an API
     *
     * @param releaseName release name
     * @param planProfile plan profile
     * @return list of methods
     */
    ApiMethodDto[] buildApiMethodDtoArray(final IProfilableApiVersion apiVersion, final String releaseName, final PlanProfile planProfile);
}

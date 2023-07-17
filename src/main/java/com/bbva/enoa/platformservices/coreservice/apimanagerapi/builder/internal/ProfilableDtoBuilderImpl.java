package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiRoleDto;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.IProfilableApiVersion;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiMethodProfileRespository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
class ProfilableDtoBuilderImpl implements IProfilableDtoBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger(ProfilableDtoBuilderImpl.class);
    private final ApiMethodProfileRespository apiMethodProfileRespository;

    @Override
    public ApiMethodDto[] buildApiMethodDtoArray(final IProfilableApiVersion apiVersion, final String releaseName, final PlanProfile planProfile)
    {
        LOG.debug("[{}] -> [{}]: Building API method Dto list for API Version [{}]", Constants.API_METHOD_DTO_BUILDER_IMPL,
                "getApiMethods", apiVersion.getId());

        List<ApiMethodDto> apiMethodDtoList = new ArrayList<>();

        for (ApiMethod apiMethod : apiVersion.getApiMethods())
        {

            ApiMethodDto apiMethodDto = new ApiMethodDto();
            apiMethodDto.setMethodId(apiMethod.getId());
            apiMethodDto.setDescription(apiMethod.getDescription());
            apiMethodDto.setEndpoint(apiMethod.getEndpoint());
            apiMethodDto.setVerb(apiMethod.getVerb().name());
            apiMethodDto.setSecurityResourceName(this.getSecurityResource(apiVersion, apiMethod, releaseName));
            ApiMethodProfile apiMethodProfile = this.apiMethodProfileRespository.findByPlanProfileAndApiMethod(planProfile, apiMethod);
            if (apiMethodProfile == null)
            {
                throw new NovaException(ApiManagerError.getApiMethodProfileNotFoundError(planProfile.getId(), apiMethod.getEndpoint(), apiMethod.getVerb().getVerb()));
            }
            else if (apiMethodProfile.getRoles() == null)
            {
                throw new NovaException(ApiManagerError.getApiMethodProfileRolesNotFound(planProfile.getId(), apiMethodProfile.getId()));
            }
            else
            {
                apiMethodDto.setAssociatedRoles(apiMethodProfile.getRoles()
                        .stream().map(x -> {
                                    ApiRoleDto apiRoleDto = new ApiRoleDto();
                                    apiRoleDto.setRoleName(x.getRol());
                                    apiRoleDto.setRoleId(x.getId());
                                    return apiRoleDto;
                                }
                        ).toArray(ApiRoleDto[]::new));
            }

            apiMethodDtoList.add(apiMethodDto);
        }

        return apiMethodDtoList.toArray(new ApiMethodDto[0]);
    }

    private String getSecurityResource(final IProfilableApiVersion apiVersion, final ApiMethod apiMethod, final String releaseName)
    {
        return releaseName + ":" + apiMethod.getVerb().getVerb() + ":" + apiVersion.getBasePath() + apiMethod.getEndpoint();
    }
}

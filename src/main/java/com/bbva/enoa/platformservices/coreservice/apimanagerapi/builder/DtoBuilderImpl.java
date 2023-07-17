package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiRoleDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiServiceDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiSubsystemDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUsageDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDetailDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal.IDtoBuilderModalityBased;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiImplementationRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Builder for api detail dto model
 */
@Component
@AllArgsConstructor
public class DtoBuilderImpl implements IDtoBuilder
{

    private static final Logger LOG = LoggerFactory.getLogger(DtoBuilderImpl.class);
    private final ToolsClient toolsClient;
    private final DeploymentPlanRepository deploymentPlanRepository;
    private final ApiImplementationRepository apiImplementationRepository;
    private final IDtoBuilderModalityBased dtoBuilderModalityBased;
    private final IApiManagerValidator apiManagerValidatorService;

    @Override
    public ApiPlanDetailDto buildApiPlanDetailDto(final DeploymentPlan deploymentPlan, final CesRole[] roles)
    {
        PlanProfile planProfile = deploymentPlan.getPlanProfiles().get(0);

        List<ApiSubsystemDto> apiSubsystemDtoList = new ArrayList<>();

        for (ReleaseVersionSubsystem releaseVersionSubsystem : deploymentPlan.getReleaseVersion().getSubsystems())
        {
            List<ApiServiceDto> apiServiceDtoList = new ArrayList<>();
            for (ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                ServiceType serviceType = ServiceType.valueOf(releaseVersionService.getServiceType());
                if (serviceType.isRest() && !releaseVersionService.getServers().isEmpty())
                {
                    ApiServiceDto apiServiceDto = new ApiServiceDto();
                    apiServiceDto.setName(releaseVersionService.getServiceName());
                    apiServiceDto.setApis(
                            releaseVersionService.getServers().stream()
                                    .map(apiImplementation ->
                                            this.buildApiPlanDto(apiImplementation.getApiVersion(),
                                                    deploymentPlan.getReleaseVersion().getRelease().getName(),
                                                    planProfile
                                            )
                                    )
                                    .toArray(ApiPlanDto[]::new)
                    );
                    apiServiceDtoList.add(apiServiceDto);
                }
            }

            if (!apiServiceDtoList.isEmpty())
            {
                ApiSubsystemDto apiSubsystemDto = new ApiSubsystemDto();
                apiSubsystemDto.setName(this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId()).getSubsystemName());
                apiSubsystemDto.setApiServices(apiServiceDtoList.toArray(new ApiServiceDto[0]));
                apiSubsystemDtoList.add(apiSubsystemDto);
            }
        }

        if (!apiSubsystemDtoList.isEmpty())
        {
            ApiPlanDetailDto apiPlanDetailDto = new ApiPlanDetailDto();
            apiPlanDetailDto.setApiSubsystems(apiSubsystemDtoList.toArray(new ApiSubsystemDto[0]));
            apiPlanDetailDto.setRoles(Arrays.stream(roles).map(x -> {
                ApiRoleDto apiRoleDto = new ApiRoleDto();
                apiRoleDto.setRoleId(x.getId());
                apiRoleDto.setRoleName(x.getRol());
                return apiRoleDto;
            }).toArray(ApiRoleDto[]::new));
            apiPlanDetailDto.setActive(ProfileStatus.ACTIVE.equals(planProfile.getStatus()));
            if (Boolean.TRUE.equals(apiPlanDetailDto.getActive()))
            {
                apiPlanDetailDto.setActivationDate(planProfile.getActivationDate().toString());
            }
            // Profiling is only editable if plan is in definition status
            apiPlanDetailDto.setEditable(DeploymentStatus.DEFINITION.equals(deploymentPlan.getStatus()));

            return apiPlanDetailDto;
        }

        return new ApiPlanDetailDto();
    }

    @Override
    public ApiPlanDto buildApiPlanDto(final ApiVersion<?, ?, ?> apiVersion, final String releaseName, final PlanProfile planProfile)
    {
        return this.dtoBuilderModalityBased.buildApiPlanDto(apiVersion, releaseName, planProfile);
    }

    @Override
    public ApiDetailDto buildApiDetailDto(final Api<?,?,?> api)
    {
        return this.dtoBuilderModalityBased.buildApiDetailDto(api);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiVersionDetailDto buildApiVersionDetailDto(final ApiVersion<?,?,?> apiVersion , final String filterByDeploymentStatus)
    {
        ApiVersionDetailDto apiVersionDetailDto = new ApiVersionDetailDto();
        List<ApiImplementation<?, ?, ?>> apiImplementationList = this.apiImplementationRepository.findApiImplementationByReadyReleaseVersion(
                apiVersion.getId());
        apiImplementationList.addAll(this.apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId()));
        apiImplementationList.addAll(this.apiImplementationRepository.findApiImplementationsConsumingApiVersion(apiVersion.getId()));
        apiVersionDetailDto.setUsages(this.buildApiVersionUsagesFromApiImplementationList(apiImplementationList,filterByDeploymentStatus));
        return apiVersionDetailDto;
    }

    private ApiUsageDto[] buildApiVersionUsagesFromApiImplementationList(List<ApiImplementation<?,?,?>> apiImplementationList,final String filterByDeploymentStatus)
    {
        return apiImplementationList.stream().map(apiImplementation -> {
            ApiUsageDto apiUsageDto = new ApiUsageDto();
            ReleaseVersion releaseVersion = apiImplementation.getService().getVersionSubsystem().getReleaseVersion();
            apiUsageDto.setServiceName(apiImplementation.getService().getServiceName());
            apiUsageDto.setServiceType(apiImplementation.getService().getServiceType());
            apiUsageDto.setReleaseVersionName(releaseVersion.getVersionName());
            apiUsageDto.setReleaseName(releaseVersion.getRelease().getName());
            apiUsageDto.setImplementedAs(apiImplementation.getImplementedAs().getImplementedAs());
            apiUsageDto.setDeployedOnInt(
                    !this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(
                            releaseVersion.getId(),
                            Environment.INT.getEnvironment(),
                            DeploymentStatus.DEPLOYED
                    ).isEmpty()
            );
            apiUsageDto.setDeployedOnPre(
                    !this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(
                            releaseVersion.getId(),
                            Environment.PRE.getEnvironment(),
                            DeploymentStatus.DEPLOYED
                    ).isEmpty()
            );
            apiUsageDto.setDeployedOnPro(
                    !this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(
                            releaseVersion.getId(),
                            Environment.PRO.getEnvironment(),
                            DeploymentStatus.DEPLOYED
                    ).isEmpty()
            );
            apiUsageDto.setUuaa(releaseVersion.getRelease().getProduct().getUuaa());
            apiUsageDto.setServiceVersion(apiImplementation.getService().getVersion());

             return apiUsageDto;
        }).filter(apiUsageDto-> ((apiUsageDto.getDeployedOnPro().equals(true) || apiUsageDto.getDeployedOnPre().equals(true) || apiUsageDto.getDeployedOnInt().equals(true)) && filterByDeploymentStatus.equals("deployed")) || filterByDeploymentStatus.equals("all"))
          .toArray(ApiUsageDto[]::new);
    }



    @Override
    @Transactional
    public ApiDto[] buildApiDtoArray(final Integer productId)
    {
        // List of APIs of the product
        List<Api<?,?,?>> apiList = this.apiManagerValidatorService.filterByProductId(productId);

        LOG.debug("[{}] -> [{}]: For product [{}] obtained [{}]", Constants.API_DTO_BUILDER_IMPL, Constants.GET_PRODUCT_APIS, productId, apiList);

        ApiDto[] dtos = apiList.stream()
                .map(this::buildApiDto)
                .sorted(Comparator.comparing(ApiDto::getApiName))
                .toArray(ApiDto[]::new);

        LOG.debug("[{}] -> [{}]: APIDto list built: [{}]", Constants.API_DTO_BUILDER_IMPL, Constants.GET_PRODUCT_APIS, Arrays.toString(dtos));
        return dtos;
    }

    /**
     * Builds an {@link ApiDto} from a {@link Api}
     *
     * @param api Sync API
     * @return result {@link ApiDto}
     */
    private ApiDto buildApiDto(final Api<?,?,?> api)
    {
        LOG.debug("[{}] -> [{}]: Building API dto for NovaSyncApi [{}]", Constants.API_DTO_BUILDER_IMPL, "buildApiDto", api);
        ApiDto apiDto = new ApiDto();
        apiDto.setId(api.getId());
        apiDto.setApiName(api.getName());
        apiDto.setApiType(api.getType().getApiType());
        apiDto.setApiModality(api.getApiModality().getModality());
        apiDto.setUuaa(api.getUuaa() != null ? api.getUuaa().toUpperCase() : null);

        LOG.debug("[{}] -> [{}]: ApiDto built: [{}]", Constants.API_DTO_BUILDER_IMPL, "buildApiDto", apiDto);
        return apiDto;
    }


}

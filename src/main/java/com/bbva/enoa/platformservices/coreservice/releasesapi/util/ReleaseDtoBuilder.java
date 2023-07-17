package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.TimeInterval;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for building DTO and Release from one other.
 * <p>
 * Created by xe52580 on 13/02/2017.
 */
@Service
@Slf4j
public class ReleaseDtoBuilder
{
    @Qualifier("releasesapi.VersionPlanDtoBuilderImpl")
    @Autowired
    private VersionPlanDtoBuilder versionPlanDtoBuilder;

    /**
     * Product entities repository
     */
    @Autowired
    private ProductRepository productRepository;

    private static final String[] DATE_FORMAT = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ"};

    /**
     * Builds a list of Release info with the release version
     * and services info filter by: RELEASE VERSION STATUS = READY
     *
     * @param productId Product Id
     * @return List of Releases of the selected product
     * @throws NovaException if product does not exist
     */
    public RELReleaseInfo[] buildReleaseInfoList(int productId)
    {
        Product product = this.productRepository.findById(productId)
                .orElseThrow( () -> new NovaException(ReleaseError.getNoSuchProductError()));
        return this.buildReleaseInfoList(product.getReleases());
    }

    /**
     * Creates entity from ConfigPeriodDto.
     *
     * @param dto ConfigPeriodDto
     * @return TimeInterval
     */
    public TimeInterval createEntityFromDto(ConfigPeriodDto dto)
    {
        TimeInterval interval = new TimeInterval();
        try
        {
            if (dto.getDateFrom() != null)
            {
                interval.setStart(DateUtils.parseDate(dto.getDateFrom().replaceAll("Z$", "+0000"), DATE_FORMAT));
            }
            if (dto.getDateTo() != null)
            {
                interval.setEnd(DateUtils.parseDate(dto.getDateTo().replaceAll("Z$", "+0000"), DATE_FORMAT));
            }
        }
        catch (ParseException e)
        {
            throw new NovaException(ReleaseError.getInvalidDateFormatError(), "[ReleasesAPI] -> [createEntityFromDto]: there was a error parsing a ConfigPeriodDto. DateFrom: [" + dto.getDateFrom() + "]. " +
                    "Date to: [" + dto.getDateTo() + "]. Error message: [" + e.getMessage()+ "]" );
        }
        return interval;
    }


    /**
     * Builds a list of ReleaseDto from a list of Release.
     *
     * @param entityList Original list.
     * @return DTO list.
     */
    public ReleaseDto[] buildDtoArrayFromEntityList(List<Release> entityList)
    {
        ReleaseDto[] dtoList = new ReleaseDto[entityList.size()];

        int i = 0;
        for (Release doc : entityList)
        {
            dtoList[i] = this.buildReleaseDtoFromEntity(doc);
            i++;
        }

        return dtoList;
    }

    /**
     * Builds a list of ReleaseDtoByEnvironment by environment from a list of Release.
     *
     * @param releasesList Original list.
     * @param environment environment to get Release config.
     * @return ReleaseDtoByEnvironment DTO list.
     */
    public ReleaseDtoByEnvironment[] buildReleaseDTOByEnvArrayFromEntityList(List<Release> releasesList, String environment)
    {
        ReleaseDtoByEnvironment[] releaseDtoByEnvironment = new ReleaseDtoByEnvironment[releasesList.size()];

        int i = 0;
        for (Release release : releasesList)
        {
            releaseDtoByEnvironment[i] = this.buildReleaseDTOByEnvFromEntity(release, environment);
            i++;
        }

        return releaseDtoByEnvironment;
    }

    /**
     * Builds a ReleaseInListDto from a Release.
     *
     * @param entity Original Release.
     * @return A DTO copy.
     */
    public ReleaseDto buildReleaseDtoFromEntity(final Release entity)
    {
        ReleaseDto dto = new ReleaseDto();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(entity, dto);

        // Copy properties different in DTO and entity.
        dto.setReleaseName(entity.getName());
        dto.setCreationDate(entity.getCreationDate().getTimeInMillis());
        // existe algún plan de despliegue de esta release, en cualquier entorno, en estado DEPLOYED
        dto.setDeployed(isDeployed(entity));

        dto.setReleaseConfig(buildReleaseConfigDtoFromEntity(entity));

        return dto;
    }

    /**
     * Builds a ReleaseInListDto from a Release.
     *
     * @param entity Original Release.
     * @return A DTO copy.
     */
    public ReleaseDtoByEnvironment buildReleaseDTOByEnvFromEntity(final Release entity, final String env)
    {
        ReleaseDtoByEnvironment dto = new ReleaseDtoByEnvironment();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(entity, dto);

        // Copy properties different in DTO and entity.
        dto.setReleaseName(entity.getName());
        dto.setCreationDate(entity.getCreationDate().getTimeInMillis());
        // existe algún plan de despliegue de esta release, en cualquier entorno, en estado DEPLOYED
        dto.setDeployed(isDeployed(entity));
        // environment
        dto.setEnvironment(env);

        dto.setReleaseConfig(buildReleaseEnvConfigDtoFromEntity(entity, env));

        return dto;
    }

    /**
     * Builds a list of ReleaseDto from a list of Release.
     *
     * @param entityList Original list.
     * @return DTO list.
     */
    public ReleaseVersionInListDto[] buildDtoFromReleaseVersionList(
            List<ReleaseVersion> entityList)
    {
        ReleaseVersionInListDto[] dtoList = new ReleaseVersionInListDto[entityList.size()];
        int i = 0;
        for (ReleaseVersion doc : entityList)
        {
            dtoList[i] = this.buildDtoFromEntity(doc);
            i++;
        }
        return dtoList;
    }


    //////////////////////////////////////////////  PRIVATE METHODS  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    private RELReleaseInfo[] buildReleaseInfoList(List<Release> releases)
    {
        List<RELReleaseInfo> releaseInfoList = new ArrayList<>();

        releases.forEach(release ->
        {
            RELReleaseInfo releaseInfo = new RELReleaseInfo();
            releaseInfo.setId(release.getId());
            releaseInfo.setReleaseName(release.getName());
            releaseInfo.setDescription(release.getDescription());
            releaseInfo.setReleaseVersions(this.buildReleaseVersionInfoList(release.getReleaseVersions()));
            releaseInfoList.add(releaseInfo);
        });

        return releaseInfoList.toArray(new RELReleaseInfo[releaseInfoList.size()]);
    }

    private RELReleaseVersionInfo[] buildReleaseVersionInfoList(List<ReleaseVersion> versions)
    {
        List<RELReleaseVersionInfo> releaseVersionInfoList = new ArrayList<>();

        versions.forEach(releaseVersion ->
        {
            // Filter only by release version in status = READY
            if (releaseVersion.getStatus() == ReleaseVersionStatus.READY_TO_DEPLOY)
            {
                RELReleaseVersionInfo releaseVersionInfo = new RELReleaseVersionInfo();
                releaseVersionInfo.setId(releaseVersion.getId());
                releaseVersionInfo.setReleaseVersionName(releaseVersion.getVersionName());
                releaseVersionInfo.setDescription(releaseVersion.getDescription());
                releaseVersionInfo.setServices(this.buildServiceInfoList(releaseVersion.getSubsystems()));
                releaseVersionInfoList.add(releaseVersionInfo);
            }
        });

        return releaseVersionInfoList.toArray(new RELReleaseVersionInfo[releaseVersionInfoList.size()]);
    }

    private RELServiceInfo[] buildServiceInfoList(List<ReleaseVersionSubsystem> subsystems)
    {
        List<RELServiceInfo> serviceInfoList = new ArrayList<>();
        subsystems.forEach(subsystem ->
        {
            subsystem.getServices().forEach(service ->
            {
                RELServiceInfo serviceInfo = new RELServiceInfo();
                serviceInfo.setId(service.getId());
                serviceInfo.setServiceName(service.getServiceName());
                serviceInfo.setDescription(service.getDescription());
                serviceInfoList.add(serviceInfo);
            });
        });

        return serviceInfoList.toArray(new RELServiceInfo[serviceInfoList.size()]);
    }

    private ReleaseConfigDto buildReleaseConfigDtoFromEntity(final Release entity)
    {
        final ReleaseConfigDto dto = new ReleaseConfigDto();

        // INT config
        dto.setIntConfig(buildReleaseEnvConfigFromEntity(entity, Environment.INT));
        // PRE config
        ReleaseEnvConfigDto releaseConfigPreDto = buildReleaseEnvConfigFromEntity(entity, Environment.PRE);
        ManagementConfigDto managementConfigPreDto = new ManagementConfigDto();
        managementConfigPreDto.setAutoManage(this.buildDtoFromEntity(entity.getAutomanageInPre()));
        managementConfigPreDto.setAutoDeploy(this.buildDtoFromEntity(entity.getAutodeployInPre()));
        releaseConfigPreDto.setManagementConfig(managementConfigPreDto);
        dto.setPreConfig(releaseConfigPreDto);
        // PRO config
        ReleaseEnvConfigDto releaseConfigProDto = buildReleaseEnvConfigFromEntity(entity, Environment.PRO);
        ManagementConfigDto managementConfigProDto = new ManagementConfigDto();
        managementConfigProDto.setAutoManage(this.buildDtoFromEntity(entity.getAutoManageInPro()));
        managementConfigProDto.setAutoDeploy(this.buildDtoFromEntity(entity.getAutodeployInPro()));
        managementConfigProDto.setDeploymentType(String.valueOf(entity.getDeploymentTypeInPro()));
        releaseConfigProDto.setManagementConfig(managementConfigProDto);
        dto.setProConfig(releaseConfigProDto);

        return dto;
    }

    private ReleaseEnvConfigDto buildReleaseEnvConfigDtoFromEntity(final Release entity, String env)
    {
        final ReleaseEnvConfigDto dto = new ReleaseEnvConfigDto();


        switch (env) {
            case "INT":
                // INT config
                dto.setSelectedPlatforms(buildReleaseEnvConfigFromEntity(entity, Environment.INT).getSelectedPlatforms());
                break;
            case "PRE":
                // PRE config
                ManagementConfigDto managementConfigPreDto = new ManagementConfigDto();
                managementConfigPreDto.setAutoManage(this.buildDtoFromEntity(entity.getAutomanageInPre()));
                managementConfigPreDto.setAutoDeploy(this.buildDtoFromEntity(entity.getAutodeployInPre()));
                dto.setManagementConfig(managementConfigPreDto);
                dto.setSelectedPlatforms(buildReleaseEnvConfigFromEntity(entity, Environment.PRE).getSelectedPlatforms());
                break;
            case "PRO":
                // PRO config
                ManagementConfigDto managementConfigProDto = new ManagementConfigDto();
                managementConfigProDto.setAutoManage(this.buildDtoFromEntity(entity.getAutoManageInPro()));
                managementConfigProDto.setAutoDeploy(this.buildDtoFromEntity(entity.getAutodeployInPro()));
                managementConfigProDto.setDeploymentType(String.valueOf(entity.getDeploymentTypeInPro()));
                dto.setManagementConfig(managementConfigProDto);
                dto.setSelectedPlatforms(buildReleaseEnvConfigFromEntity(entity, Environment.PRO).getSelectedPlatforms());
                break;
        }

        return dto;
    }

    private ReleaseEnvConfigDto buildReleaseEnvConfigFromEntity(final Release entity, Environment env)
    {
        final ReleaseEnvConfigDto dto = new ReleaseEnvConfigDto();
        dto.setSelectedPlatforms(buildSelectedPlatformsDtoFromEntity(entity, env));

        return dto;
    }

    private SelectedPlatformsDto buildSelectedPlatformsDtoFromEntity(final Release entity, Environment env)
    {
        final SelectedPlatformsDto dto = new SelectedPlatformsDto();
        switch (env)
        {
            case INT:
                dto.setDeploymentPlatform(entity.getSelectedDeployInt().name());
                dto.setLoggingPlatform(entity.getSelectedLoggingInt().name());
                break;
            case PRE:
                dto.setDeploymentPlatform(entity.getSelectedDeployPre().name());
                dto.setLoggingPlatform(entity.getSelectedLoggingPre().name());
                break;
            case PRO:
                dto.setDeploymentPlatform(entity.getSelectedDeployPro().name());
                dto.setLoggingPlatform(entity.getSelectedLoggingPro().name());
                break;
        }

        return dto;
    }

    /**
     * Return {@code true} if there is at least one deployment plan executed for the release
     *
     * @param release - The Release
     * @return {@code true} if there is at least one deployment plan executed for the release
     */
    private boolean isDeployed(Release release)
    {
        // Para cada release version
        for (ReleaseVersion version : release.getReleaseVersions())
        {
            // Para cada deployment plan
            for (DeploymentPlan plan : version.getDeployments())
            {
                // ¿Está desplegado?
                if (plan.getStatus() == DeploymentStatus.DEPLOYED)
                {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Builds DTO for TimeInterval.
     *
     * @param entity Time interval.
     * @return ConfigPeriodDto
     */
    private ConfigPeriodDto buildDtoFromEntity(TimeInterval entity)
    {
        if (entity == null)
        {
            return null;
        }

        ConfigPeriodDto dto = new ConfigPeriodDto();
        if (entity.getStart() != null)
        {
            dto.setDateFrom(entity.getStart().toInstant().toString());
        }
        if (entity.getEnd() != null)
        {
            dto.setDateTo(entity.getEnd().toInstant().toString());
        }
        return dto;
    }


    /**
     * Build DTO for ReleaseVersion.
     *
     * @param entity ReleaseVersion.
     * @return ReleaseVersionInListDto
     */
    private ReleaseVersionInListDto buildDtoFromEntity(ReleaseVersion entity)
    {
        if (entity == null)
        {
            return null;
        }

        ReleaseVersionInListDto dto = new ReleaseVersionInListDto();

        dto.setId(entity.getId());
        dto.setReleaseVersionName(entity.getVersionName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus().name());
        dto.setStatusDescription(entity.getStatusDescription());
        dto.setQualityValidation(entity.getQualityValidation());

        if (entity.getCreationDate() != null)
        {
            dto.setCreationDate(entity.getCreationDate().getTimeInMillis());
        }
        if (entity.getDeletionDate() != null)
        {
            dto.setDeletionDate(entity.getDeletionDate().getTimeInMillis());
        }

        // Set the deployments on each environment if any.

        // INT
        dto.setDeploymentOnInt(
                this.versionPlanDtoBuilder.build(
                        entity.getId(),
                        Environment.INT,
                        DeploymentStatus.DEPLOYED));

        // PRE
        dto.setDeploymentOnPre(
                this.versionPlanDtoBuilder.build(
                        entity.getId(),
                        Environment.PRE,
                        DeploymentStatus.DEPLOYED));

        // PRO
        dto.setDeploymentOnPro(
                this.versionPlanDtoBuilder.build(
                        entity.getId(),
                        Environment.PRO,
                        DeploymentStatus.DEPLOYED));

        // And return.
        return dto;
    }

}

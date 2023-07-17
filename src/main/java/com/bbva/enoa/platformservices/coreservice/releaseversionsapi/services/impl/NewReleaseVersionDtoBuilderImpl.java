package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.INewReleaseVersionDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Builds a NewReleaseVersionDto from a ReleaseVersion:
 * <p>
 * - Gets Product, Subsystems and Release data.
 * <p>
 * Created by xe52580 on 14/02/2017.
 */
@Slf4j
@Service
public class NewReleaseVersionDtoBuilderImpl implements INewReleaseVersionDtoBuilder
{
    /**
     * Client of the VCS API.
     */
    @Autowired
    private IVersioncontrolsystemClient vcsClient;

    /**
     * Builds a subsystem DTO
     */
    @Autowired
    private ISubsystemDtoBuilder iSubsystemDtoBuilder;

    @Override
    public NewReleaseVersionDto build(Release release, Product product, final String ivUser)
    {
        // Add basic data, using defaults.
        NewReleaseVersionDto newReleaseVersionDto = new NewReleaseVersionDto();

        // Add Release data.
        newReleaseVersionDto.setRelease(this.buildReleaseDto(release));

        // Add Subsystems data, including tags, services and validation.
        newReleaseVersionDto.setSubsystems(this.iSubsystemDtoBuilder.buildNewSubsystemsFromProduct(product, ivUser, release.getName()));

        this.addServiceNames(product, newReleaseVersionDto);

        //other information
        newReleaseVersionDto.setIssueID(product.getDesBoard());

        log.debug("[{}] -> [{}]: Successfully built a new release version DTO for Product: [{}] and Release: [{}]", Constants
                .NEW_RELEASE_VERSION_BUILDER, "build", product.getName(), release.getName());

        log.trace("[{}] -> [{}]: New release version DTO contents: [{}]", Constants.NEW_RELEASE_VERSION_BUILDER, "build", newReleaseVersionDto);

        // Return DTO.
        return newReleaseVersionDto;
    }

    @Override
    public RVRequestDTO build(Product product, Release release)
    {
        // Initialize RVRequestDTO object to set values
        RVRequestDTO requestDTO = new RVRequestDTO();

        // Add Release information
        requestDTO.setRelease(this.buildReleaseDto(release));

        // Add Subsystems data, including tags
        requestDTO.setSubsystems(this.iSubsystemDtoBuilder.buildRVSubsystemDTO(product));

        log.debug("[{}] -> [{}]: Successfully built RVRequestDTO for Release: [{}]", Constants
                .RELEASE_VERSIONS_API_NAME, "build", release.getName());

        log.trace("[{}] -> [{}]:RVRequestDTO contents: [{}]", Constants.RELEASE_VERSIONS_API_NAME, "build", requestDTO);

        // Return DTO.
        return requestDTO;
    }

    ////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Sets the NOVA canonical service name in all services.
     *
     * @param dto - the NewReleaseVersionDto
     */
    private void addServiceNames(Product product, NewReleaseVersionDto dto)
    {
        // Ugly but necessary iteration. :-S
        for (NewReleaseVersionSubsystemDto subsystem : dto.getSubsystems())
        {
            this.addServiceNamesForSubsystemTag(product, dto, subsystem);
        }
    }

    private void addServiceNamesForSubsystemTag(Product product, NewReleaseVersionDto dto, NewReleaseVersionSubsystemDto subsystem)
    {
        for (SubsystemTagDto tag : subsystem.getTags())
        {
            this.addServiceNamesForService(product, dto, tag);
        }
    }

    private void addServiceNamesForService(Product product, NewReleaseVersionDto dto, SubsystemTagDto tag)
    {
        for (NewReleaseVersionServiceDto service : tag.getServices())
        {
            this.setNovaServiceName(product, dto, service);
        }
    }

    private void setNovaServiceName(Product product, NewReleaseVersionDto dto, NewReleaseVersionServiceDto service)
    {
        if ((!Constants.INVALID_SERVICE_NAME.equals(service.getServiceName())) &&
                (service.getGroupId() != null && service.getArtifactId() != null &&
                        service.getVersion() != null && dto.getRelease() != null &&
                        dto.getRelease().getReleaseName() != null))
        {
            // Set the canonical service name.
            if (ServiceType.isLibrary(ServiceType.getValueOf(service.getServiceType())))
            {
                service.setNovaServiceName(
                        ServiceNamingUtils.getNovaServiceName(
                                product.getUuaa().toLowerCase(),
                                dto.getRelease().getReleaseName().toLowerCase(),
                                service.getServiceName().toLowerCase(),
                                service.getVersion()
                        )
                );
            }
            else
            {
                service.setNovaServiceName(
                        ServiceNamingUtils.getNovaServiceName(
                                service.getGroupId(),
                                service.getArtifactId(),
                                dto.getRelease().getReleaseName(),
                                service.getVersion().split("\\.")[0])
                );
            }
        }
    }

    /**
     * Builds a RVReleaseDto from a Release.
     *
     * @param release Release
     * @return RVReleaseDto
     */
    private RVReleaseDTO buildReleaseDto(Release release)
    {
        log.debug("[ReleaseVersion API] -> [buildReleaseDto] Create RVReleaseDTO by given release. ID: {}, name: {} and description: {}", release.getId(), release.getName(),
                release.getDescription());

        RVReleaseDTO dto = new RVReleaseDTO();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(release, dto);

        // Copy all the different properties.
        dto.setReleaseName(release.getName());

        log.debug("[ReleaseVersion API] -> [buildReleaseDto] Created RVReleaseDTO with following information. ID: {}, name: {} and description: {}", dto.getId(), dto.getReleaseName(),
                dto.getDescription());

        return dto;
    }
}

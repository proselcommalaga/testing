package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service DTO builder
 * Builds the DTO of a subsystem
 */
@Service
@Slf4j
public class SubsystemDtoBuilderImpl implements ISubsystemDtoBuilder
{
    /**
     * Client of the VCS API.
     */
    @Autowired
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;
    /**
     * Validates if a subsystem is NOVA compliant.
     */
    @Autowired
    private ISubsystemValidator iSubsystemValidator;
    /**
     * Builds a service DTO
     */
    @Autowired
    private IServiceDtoBuilder iServiceDtoBuilder;

    /**
     * Tools Service client
     */
    @Autowired
    private IToolsClient toolsService;

    /**
     * Number of Gitlab tags to connsider
     */
    @Value("${nova.maxTags:3}")
    private int maxTags;

    @Override
    public NewReleaseVersionSubsystemDto[] buildNewSubsystemsFromProduct(Product product, final String ivUser, final String releaseName)
    {
        // Get product subsystems.
        List<TOSubsystemDTO> subsystemDTOList = this.toolsService.getProductSubsystems(product.getId(), false);

        log.debug("[{}] -> [{}]: Product subsystems: [{}]", Constants.SUBSYSTEM_DTO_BUILDER, "buildNewSubsystemsFromProduct",
                subsystemDTOList);

        if (subsystemDTOList.isEmpty())
        {
            log.warn("[{}] -> [{}]: Product: [{}] has no subsystems, aborting", Constants.SUBSYSTEM_DTO_BUILDER,
                    "buildNewSubsystemsFromProduct", product.getName());
            return new NewReleaseVersionSubsystemDto[0];
        }

        // Create array of release version subsystems.
        NewReleaseVersionSubsystemDto[] subsystems = new NewReleaseVersionSubsystemDto[subsystemDTOList.size()];

        // For each product subsystem, build a release version subsystem.
        int i = 0;
        for (TOSubsystemDTO toSubsystemDTO : subsystemDTOList)
        {
            // Build DTO.
            subsystems[i] = new NewReleaseVersionSubsystemDto();

            // Set the product subsystem.
            subsystems[i].setProductSubsystem(this.buildProductSubsystemDto(toSubsystemDTO));

            // Get all its tags and services from that tag.
            subsystems[i].setTags(this.buildTagsFromSubsystem(toSubsystemDTO, ivUser, releaseName, product));

            // Go next.
            i++;
        }

        return subsystems;
    }

    @Override
    public RVSubsystemDTO[] buildRVSubsystemDTO(Product product)
    {
        // Get product subsystems from toolservice.
        List<TOSubsystemDTO> toSubsystemDTOList = this.toolsService.getProductSubsystems(product.getId(), false);

        log.debug("[{}] -> [{}]: Product subsystems: [{}]", Constants.RELEASE_VERSIONS_API_NAME, "buildRVSubsystemDTO", toSubsystemDTOList);

        if (toSubsystemDTOList.isEmpty())
        {
            log.warn("[{}] -> [{}]: Product: [{}] has no subsystems, aborting", Constants.RELEASE_VERSIONS_API_NAME,
                    "buildRVSubsystemDTO", product.getName());

            return new RVSubsystemDTO[0];
        }

        // Initialize the result list.
        List<RVSubsystemDTO> rvSubsystemDTOList = new ArrayList<>();

        // For each subsystem, set DTO content
        for (TOSubsystemDTO toSubsystemDTO : toSubsystemDTOList)
        {

            RVSubsystemDTO rvSubsystemDTO = new RVSubsystemDTO();

            //Copy basic info
            BeanUtils.copyProperties(toSubsystemDTO, rvSubsystemDTO);
            rvSubsystemDTO.setId(toSubsystemDTO.getSubsystemId());

            log.debug("[{}] -> [{}]: Built RVSubsystemDTO [{}]", Constants.RELEASE_VERSIONS_API_NAME, "buildRVSubsystemDTO", rvSubsystemDTO);

            // Get all its tags and services from that tag.
            rvSubsystemDTO.setTags(this.buildSubsystemTags(toSubsystemDTO));

            // Add subsystem to list
            rvSubsystemDTOList.add(rvSubsystemDTO);
        }

        log.trace("[{}] -> [{}]: RVRequestDTO subsystem contents: [{}]", Constants.RELEASE_VERSIONS_API_NAME, "buildRVSubsystemDTO", rvSubsystemDTOList);

        return rvSubsystemDTOList.toArray(new RVSubsystemDTO[0]);
    }


    ///////////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    /**
     * Build a ProductSubsystemDto from ProductSubsystem.
     *
     * @param toSubsystemDTO ProductSubsystem
     * @return ProductSubsystemDto
     */
    private ProductSubsystemDto buildProductSubsystemDto(TOSubsystemDTO toSubsystemDTO)
    {
        ProductSubsystemDto dto = new ProductSubsystemDto();
        BeanUtils.copyProperties(toSubsystemDTO, dto);

        // Copy all the different properties.
        dto.setId(toSubsystemDTO.getSubsystemId());
        dto.setProductSubsystemName(toSubsystemDTO.getSubsystemName());
        dto.setSubsystemType(toSubsystemDTO.getSubsystemType());

        return dto;
    }

    /**
     * Builds an array of SubsystemTagDto from the product subsystem tag and the
     * services that were in it.
     *
     * @param toSubsystemDTO Product subsystem
     * @param ivUser         user requester
     * @param releaseName    Name of the release
     * @param product        product
     * @return Array of SubsystemTagDto
     */
    private SubsystemTagDto[] buildTagsFromSubsystem(TOSubsystemDTO toSubsystemDTO, final String ivUser,
                                                     final String releaseName, Product product)
    {
        // Get subsystem repo tags from VCS.
        List<VCSTag> tagList = this.iVersioncontrolsystemClient.getTags(toSubsystemDTO.getRepoId());

        if (tagList.isEmpty())
        {
            log.warn("[{}] -> [{}]: Subsystem: [{}] has no tags, aborting", Constants.SUBSYSTEM_DTO_BUILDER,
                    "buildTagsFromSubsystem", toSubsystemDTO.getSubsystemName());
            return new SubsystemTagDto[0];
        }
        else if (tagList.size() > this.maxTags)
        {
            tagList.subList(this.maxTags, tagList.size()).clear();
        }

        // Build tag DTO for release version.
        SubsystemTagDto[] subsystemTagDtos = new SubsystemTagDto[tagList.size()];

        // For each tag.
        for (int i = 0; i < subsystemTagDtos.length; i++)
        {
            // Creates a subsystem tag DTO
            subsystemTagDtos[i] = new SubsystemTagDto();
            subsystemTagDtos[i].setValidationErrors(new ValidationErrorDto[0]);

            // Get the name of the tag
            subsystemTagDtos[i].setTagName(tagList.get(i).getTagName());

            // Get the URL to the tag on the VCS repo.
            // URL ends on .git
            subsystemTagDtos[i].setTagUrl(toSubsystemDTO.getUrl().replace(".git", "") +
                    Constants.TREE + subsystemTagDtos[i].getTagName());

            subsystemTagDtos[i].setMessage(tagList.get(i).getMessage());

            // Build the services of the tagged subsystem
            List<NewReleaseVersionServiceDto> serviceDtos = this.iServiceDtoBuilder.buildServicesFromSubsystemTag(toSubsystemDTO.getRepoId(),
                    subsystemTagDtos[i], SubsystemType.getValueOf(toSubsystemDTO.getSubsystemType()), ivUser, releaseName, product, toSubsystemDTO.getSubsystemName());
            subsystemTagDtos[i].setServices(serviceDtos.toArray(new NewReleaseVersionServiceDto[0]));

            // Validate the tag and set the errors (if any)
            this.iSubsystemValidator.validateSubsystemTagDto(subsystemTagDtos[i], SubsystemType.getValueOf(toSubsystemDTO.getSubsystemType()));
        }

        // And finally return it.
        return subsystemTagDtos;
    }

    /**
     * Build all tag of given subsystem
     *
     * @param toSubsystemDTO subsystem to build their tags
     * @return list with all subsystem tags
     */
    private RVTagDTO[] buildSubsystemTags(TOSubsystemDTO toSubsystemDTO)
    {
        // Get subsystem repo tags from VCS.
        List<VCSTag> tagList = this.iVersioncontrolsystemClient.getTags(toSubsystemDTO.getRepoId());

        if (tagList.isEmpty())
        {
            log.warn("[{}] -> [{}]: Subsystem: [{}] has no tags, aborting", Constants.RELEASE_VERSIONS_API_NAME, "buildSubsystemTags", toSubsystemDTO.getSubsystemName());

            return new RVTagDTO[0];
        }

        // Build tag DTO for release version.
        List<RVTagDTO> rvTagDTOList = new ArrayList<>();

        for (VCSTag tag : tagList)
        {
            RVTagDTO tagDTO = new RVTagDTO();

            //Get the name of the tag
            tagDTO.setTagName(tag.getTagName());
            tagDTO.setMessage(tag.getMessage());
            // Get the URL to the tag on the VCS repo. URL ends on .git
            tagDTO.setTagUrl(toSubsystemDTO.getUrl().replace(".git", "") + Constants.TREE + tag.getTagName());

            log.debug("[{}] -> [{}]: Built RVTagDTO [{}]", Constants.RELEASE_VERSIONS_API_NAME, "buildSubsystemTags", tagDTO);

            rvTagDTOList.add(tagDTO);
        }

        log.trace("[{}] -> [{}]: RVSubsystemDTO tags contents: [{}]", Constants.RELEASE_VERSIONS_API_NAME, "buildSubsystemTags", rvTagDTOList);

        // And finally return it.
        return rvTagDTOList.toArray(new RVTagDTO[0]);
    }
}

package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorInstance;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants.TREE;


/**
 * Build DTOS related to behavior versions
 */
@Slf4j
@Service
public class BehaviorVersionDtoBuilderImpl implements IBehaviorVersionDtoBuilder
{
    /**
     * Class name
     */
    private static final String CLASS_NAME = "BehaviorVersionDtoBuilderImpl";

    /**
     * Tools Service client
     */
    private final IToolsClient toolsService;

    /**
     * Client of the VCS API
     */
    private final IVersioncontrolsystemClient versionControlSystemClient;

    /**
     * @param toolsService               Tools Service client
     * @param versionControlSystemClient Client of the VCS API
     */
    @Autowired
    public BehaviorVersionDtoBuilderImpl(final IToolsClient toolsService, final IVersioncontrolsystemClient versionControlSystemClient)
    {
        this.toolsService = toolsService;
        this.versionControlSystemClient = versionControlSystemClient;

    }

    @Override
    public BVRequestDTO build(Product product)
    {
        BVRequestDTO requestDTO = new BVRequestDTO();

        // Add Subsystems data, including tags
        requestDTO.setSubsystems(this.buildBVSubsystemDTO(product));
        log.debug("[{}] -> [{}]: Successfully built BVRequestDTO for product: [{}]", CLASS_NAME, "build", product.getUuaa());
        log.trace("[{}] -> [{}]: BVRequestDTO contents: [{}]", CLASS_NAME, "build", requestDTO);
        return requestDTO;
    }

    @Override
    public BVBehaviorVersionSummaryInfoDTO[] buildDtoFromBehaviorVersionList(List<BehaviorVersion> behaviorVersions)
    {
        BVBehaviorVersionSummaryInfoDTO[] bvBehaviorVersionSummaryInfoDTOS = new BVBehaviorVersionSummaryInfoDTO[behaviorVersions.size()];

        int i = 0;
        for (BehaviorVersion behaviorVersion : behaviorVersions)
        {
            bvBehaviorVersionSummaryInfoDTOS[i] = this.buildBehaviorVersionDTO(behaviorVersion);
            i++;
        }
        return bvBehaviorVersionSummaryInfoDTOS;
    }

    @Override
    public BVBehaviorInstanceDTO buildDTOFromBehaviorInstance(final BehaviorInstance behaviorInstance)
    {
        BVBehaviorInstanceDTO behaviorInstanceDTO = new BVBehaviorInstanceDTO();

        BehaviorServiceConfiguration bsConfiguration = behaviorInstance.getBehaviorServiceConfiguration();
        BehaviorService behaviorService = bsConfiguration.getBehaviorService();
        BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();

        behaviorInstanceDTO.setBehaviorExecutionId(behaviorInstance.getBehaviorExecutionId());
        behaviorInstanceDTO.setBehaviorInstanceId(behaviorInstance.getId());
        behaviorInstanceDTO.setBehaviorVersionId(behaviorVersion.getId());
        behaviorInstanceDTO.setBehaviorServiceId(behaviorService.getId());
        behaviorInstanceDTO.setProductId(behaviorVersion.getProduct().getId());
        behaviorInstanceDTO.setPackIdConfigured(bsConfiguration.getHardwarePack().getId());
        behaviorInstanceDTO.setReleaseName(behaviorService.getReleaseVersion().getRelease().getName());
        behaviorInstanceDTO.setReleaseVersionName(behaviorService.getReleaseVersion().getVersionName());
        behaviorInstanceDTO.setTagName(behaviorService.getBehaviorSubsystem().getTagName());

        return behaviorInstanceDTO;
    }

    @Override
    public BVBehaviorVersionSummaryInfoDTO buildBehaviorVersionDTO(BehaviorVersion behaviorVersion)
    {
        if (behaviorVersion == null)
        {
            return null;
        }

        BVBehaviorVersionSummaryInfoDTO bvBehaviorVersionSummaryInfoDTO = new BVBehaviorVersionSummaryInfoDTO();

        bvBehaviorVersionSummaryInfoDTO.setId(behaviorVersion.getId());
        bvBehaviorVersionSummaryInfoDTO.setVersionName(behaviorVersion.getVersionName());
        bvBehaviorVersionSummaryInfoDTO.setDescription(behaviorVersion.getDescription());

        if (behaviorVersion.getCreatedAt() != null)
        {
            bvBehaviorVersionSummaryInfoDTO.setCreationDate(behaviorVersion.getCreatedAt().getTimeInMillis());
        }

        if (behaviorVersion.getDeletionAt() != null)
        {
            bvBehaviorVersionSummaryInfoDTO.setDeletionDate(behaviorVersion.getDeletionAt().getTimeInMillis());
        }

        bvBehaviorVersionSummaryInfoDTO.setStatus(behaviorVersion.getStatus().toString());
        bvBehaviorVersionSummaryInfoDTO.setQualityValidation(behaviorVersion.getQualityValidation());
        bvBehaviorVersionSummaryInfoDTO.setBehaviorVersionType(behaviorVersion.getProduct().getType());

        return bvBehaviorVersionSummaryInfoDTO;
    }

    ////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Build a BVSubsystemTagDTO list with all subsystems of given product
     *
     * @param product product
     * @return an array with all subsystems with their tags
     */
    private BVSubsystemTagDTO[] buildBVSubsystemDTO(Product product)
    {
        // Get product subsystems from tool service.
        List<TOSubsystemDTO> toSubsystemDTOList = this.toolsService.getProductSubsystems(product.getId(), true);

        log.debug("[BehaviorVersionDtoBuilderImpl] -> [buildBVSubsystemDTO]: Product subsystems: [{}]", toSubsystemDTOList);

        // Initialize the result list.
        List<BVSubsystemTagDTO> bvSubsystemTagDTOList = new ArrayList<>();

        // For each subsystem, set DTO content
        for (TOSubsystemDTO toSubsystemDTO : toSubsystemDTOList)
        {
            BVSubsystemTagDTO bvSubsystemTagDTO = new BVSubsystemTagDTO();

            //Copy basic info
            BeanUtils.copyProperties(toSubsystemDTO, bvSubsystemTagDTO);
            bvSubsystemTagDTO.setId(toSubsystemDTO.getSubsystemId());

            log.debug("[BehaviorVersionDtoBuilderImpl] -> [buildBVSubsystemDTO]: Built RVSubsystemDTO [{}]", bvSubsystemTagDTO);

            // Get all its tags and services from that tag.
            bvSubsystemTagDTO.setTags(this.buildSubsystemTags(toSubsystemDTO));
            // Add subsystem to list
            bvSubsystemTagDTOList.add(bvSubsystemTagDTO);
        }
        log.trace("[{}] -> [{}]: RVRequestDTO subsystem contents: [{}]", CLASS_NAME, "buildBVSubsystemDTO", bvSubsystemTagDTOList);
        return bvSubsystemTagDTOList.toArray(new BVSubsystemTagDTO[0]);
    }

    /**
     * Build all tag of given subsystem
     *
     * @param toSubsystemDTO subsystem to build their tags
     * @return list with all subsystem tags
     */
    private BVTagDTO[] buildSubsystemTags(TOSubsystemDTO toSubsystemDTO)
    {
        // Get subsystem repo tags from VCS.
        List<VCSTag> tagList = this.versionControlSystemClient.getTags(toSubsystemDTO.getRepoId());

        if (tagList.isEmpty())
        {
            log.warn("[BehaviorVersionDtoBuilderImpl] -> [buildSubsystemTags]: Subsystem: [{}] has no tags, aborting", toSubsystemDTO.getSubsystemName());
            return new BVTagDTO[0];
        }

        // Build tag DTO for behavior version.
        List<BVTagDTO> bvTagDTOList = new ArrayList<>();

        for (VCSTag tag : tagList)
        {
            BVTagDTO tagDTO = new BVTagDTO();

            //Get the name of the tag
            tagDTO.setTagName(tag.getTagName());
            tagDTO.setMessage(tag.getMessage());
            // Get the URL to the tag on the VCS repo. URL ends on .git
            tagDTO.setTagUrl(toSubsystemDTO.getUrl().replace(".git", "") + TREE + tag.getTagName());

            log.debug("[BehaviorVersionDtoBuilderImpl] -> [buildSubsystemTags]: Built BVTagDTO [{}]", tagDTO);

            bvTagDTOList.add(tagDTO);
        }
        log.trace("[{}] -> [{}]: BVSubsystemTagDTO tags contents: [{}]", CLASS_NAME, "buildSubsystemTags", bvTagDTOList);
        // And finally return it.
        return bvTagDTOList.toArray(new BVTagDTO[0]);
    }
}

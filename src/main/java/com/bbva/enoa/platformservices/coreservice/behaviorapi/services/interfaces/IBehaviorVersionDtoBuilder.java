package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorInstanceDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorVersionSummaryInfoDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVRequestDTO;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorInstance;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.List;

/**
 * Build DTOS related to behavior versions
 */
public interface IBehaviorVersionDtoBuilder
{
    /**
     * Builds a DTO for create a new behavior version
     *
     * @param product Product of the behavior version.
     * @return RVRequestDTO object
     */
    BVRequestDTO build(Product product);

    /**
     * Builds a DTO for behavior version
     *
     * @param behaviorVersion behavior version.
     * @return BVBehaviorVersionSummaryInfoDTO object
     */
    BVBehaviorVersionSummaryInfoDTO buildBehaviorVersionDTO(BehaviorVersion behaviorVersion);

    /**
     * Build dto from behavior version list
     *
     * @param behaviorVersions behavior versions list
     * @return array BVBehaviorVersionSummaryInfoDTO
     */
    BVBehaviorVersionSummaryInfoDTO[] buildDtoFromBehaviorVersionList(List<BehaviorVersion> behaviorVersions);


    /**
     * Build dto from behavior instance entity
     *
     * @param behaviorInstance behavior instance
     * @return dto with info
     */
    BVBehaviorInstanceDTO buildDTOFromBehaviorInstance(final BehaviorInstance behaviorInstance);
}

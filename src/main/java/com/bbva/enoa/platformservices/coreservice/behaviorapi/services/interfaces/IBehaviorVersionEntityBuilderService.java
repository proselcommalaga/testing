package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorVersionDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVSubsystemDTO;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.Set;

/**
 * Build a new {@link BehaviorVersion} entity.
 */
public interface IBehaviorVersionEntityBuilderService
{

    /**
     * Create behavior version from behavior version dto
     *
     * @param behaviorVersionDto    behavior version to add
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return behavior version
     */
    BehaviorVersion buildBehaviorVersionEntityFromDto(Product product, BVBehaviorVersionDTO behaviorVersionDto, String ivUser, boolean isIvUserPlatformAdmin);

    /**
     * Creates a new BehaviorSubsystem from a bvSubsystemDTO.
     *
     * @param bvSubsystemDTO        BVSubsystemDTO
     * @param versionName           name of the version. Used to generate de image name
     * @param serviceVersionSet     set of Services (with version) already present
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return a {@code BehaviorSubsystem} entity.
     */
    BehaviorSubsystem buildBehaviorVersionSubsystemFromDTO(BVSubsystemDTO bvSubsystemDTO, Set<String> serviceVersionSet, Product product,
                                                           String versionName, String ivUser, boolean isIvUserPlatformAdmin);

}

package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;


public interface IBehaviorVersionDtoBuilderService
{

    /**
     * Process template
     *
     * @param behaviorVersion {@link BehaviorVersion}
     * @param ivUser          user requester
     */
    void processTemplates(BehaviorVersion behaviorVersion, String ivUser);

    /**
     * Build the subsystem of the product version.
     *
     * @param product         product associated to the behavior version
     * @param behaviorVersion BehaviorVersion
     * @param ivUser          user that generate the problem
     */
    void buildSubsystems(Product product, BehaviorVersion behaviorVersion, String ivUser);
    
}

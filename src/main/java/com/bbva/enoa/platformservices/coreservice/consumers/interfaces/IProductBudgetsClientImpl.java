package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorExecutionInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorPotentialCostInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBCheckExecutionInfo;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;

public interface IProductBudgetsClientImpl
{
    boolean checkBehaviorBudgets(PBCheckExecutionInfo executionInfo);

    boolean checkBehaviorProductInitiatives(Integer productId);

    void insertNewBehaviorVersionCostInformation(Product product, BehaviorVersion behaviorVersion);

    void updateBehaviorVersionCurrentCost(PBBehaviorExecutionInfo behaviorExecutionInfo, String behaviorAction);

    void updateBehaviorVersionPotentialCost(PBBehaviorPotentialCostInfo behaviorPotentialCostInfo);
}

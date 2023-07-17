package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.IRestHandlerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.IRestListenerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.impl.RestHandlerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorExecutionInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorPotentialCostInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBCheckExecutionInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBNewBehaviorVersionExtraInfo;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductBudgetsClientImpl;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ProductBudgetsClientImpl implements IProductBudgetsClientImpl
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductBudgetsClientImpl.class);

    /**
     * API services
     */
    private final IRestHandlerProductbudgetsapi iRestHandlerProductbudgetsapi;

    /**
     * Handler
     */
    private RestHandlerProductbudgetsapi restHandlerProductbudgetsapi;


    @Autowired
    public ProductBudgetsClientImpl(final IRestHandlerProductbudgetsapi iRestHandlerProductbudgetsapi)
    {
        this.iRestHandlerProductbudgetsapi = iRestHandlerProductbudgetsapi;
    }

    /**
     * Init
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerProductbudgetsapi = new RestHandlerProductbudgetsapi(this.iRestHandlerProductbudgetsapi);
    }

    @Override
    public boolean checkBehaviorBudgets(PBCheckExecutionInfo executionInfo)
    {
        LOG.debug("[{}] -> [checkExecution]: Checking potential execution for: [{}]", this.getClass().getSimpleName(), executionInfo);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerProductbudgetsapi.checkBehaviorBudgets(new IRestListenerProductbudgetsapi()
        {

            @Override
            public void checkBehaviorBudgets(Boolean outcome)
            {
                LOG.debug("[{}] -> [checkBehaviorBudgets]: Is the service [serviceId->{}] executable with configured pack? {}",
                        this.getClass().getSimpleName(),
                        executionInfo.getBehaviorServiceId(),
                        outcome);
                response.set(outcome);
            }

            @Override
            public void checkBehaviorBudgetsErrors(Errors outcome)
            {
                LOG.error("[{}] -> [checkExecution]: Error occurred calling product budget service to check execution with parameters [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), executionInfo, outcome.getBodyExceptionMessage());
                throw new NovaException(BehaviorError.getApiCommunicationError(), outcome,
                        "[UsersAPI Client] -> [checkBehaviorBudgets]: Error trying to call 'checkBehaviorBudgets' for execution info" +
                                executionInfo);
            }
        }, executionInfo);

        return response.get();
    }

    @Override
    public boolean checkBehaviorProductInitiatives(Integer productId)
    {
        LOG.debug("[{}] -> [checkBehaviorProductInitiatives]: Checking initiatives for: [{}]", this.getClass().getSimpleName(), productId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerProductbudgetsapi.checkBehaviorProductInitiatives(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void checkBehaviorProductInitiatives(Boolean outcome)
            {
                LOG.debug("[{}] -> [checkBehaviorProductInitiatives]: Checking product initiatives for product {}",
                        this.getClass().getSimpleName(), productId);
                response.set(outcome);
            }

            @Override
            public void checkBehaviorProductInitiativesErrors(Errors outcome)
            {
                LOG.error("[{}] -> [checkBehaviorProductInitiatives]: Error occurred calling product budget service to check initiatives for product [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), productId, outcome.getBodyExceptionMessage());
                throw new NovaException(BehaviorError.getApiCommunicationError(), outcome,
                        "[ProductBudgets API Client] -> [checkBehaviorProductInitiatives]: " +
                                "Error trying to call 'checkBehaviorProductInitiativesErrors' for check initiatives");
            }
        }, productId);

        return response.get();
    }

    @Override
    public void insertNewBehaviorVersionCostInformation(Product product, BehaviorVersion behaviorVersion)
    {
        LOG.debug("[{}] -> [insertNewBehaviorVersionCostInformation]: Inserting behavior cost information for product: [{}] and behaviorVersionId [{}]",
                this.getClass().getSimpleName(), product.getId(), behaviorVersion.getId());

        PBNewBehaviorVersionExtraInfo behaviorVersionExtraInfo = new PBNewBehaviorVersionExtraInfo();
        behaviorVersionExtraInfo.setProductName(product.getName());
        behaviorVersionExtraInfo.setBehaviorVersionName(behaviorVersion.getVersionName());

        this.restHandlerProductbudgetsapi.insertNewBehaviorVersionCostInformation(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void insertNewBehaviorVersionCostInformation()
            {
                LOG.debug("[{}] -> [insertNewBehaviorVersionCostInformation]: Inserting behavior cost information for product: [{}] and behaviorVersionId [{}]",
                        this.getClass().getSimpleName(), product.getId(), behaviorVersion.getId());
            }

            @Override
            public void insertNewBehaviorVersionCostInformationErrors(Errors outcome)
            {
                LOG.error("[{}] -> [insertNewBehaviorVersionCostInformation]: Error occurred calling product budget service to Insert behavior cost information for product: [{}] and behaviorVersionId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), product.getId(), behaviorVersion.getId(), outcome.getBodyExceptionMessage());
                throw new NovaException(BehaviorError.getApiCommunicationError(), outcome,
                        "[ProductBudgets API Client] -> [insertNewBehaviorVersionCostInformation]: " +
                                "Error trying to call 'insertNewBehaviorVersionCostInformationErrors' for Insert behavior cost information");
            }
        }, behaviorVersionExtraInfo, product.getId(), behaviorVersion.getId());
    }

    @Override
    public void updateBehaviorVersionCurrentCost(PBBehaviorExecutionInfo behaviorExecutionInfo, String behaviorAction)
    {
        LOG.debug("[{}] -> [updateBehaviorVersionCurrentCost]: Updating behavior budgets ", this.getClass().getSimpleName());

        this.restHandlerProductbudgetsapi.updateBehaviorVersionCurrentCost(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void updateBehaviorVersionCurrentCost()
            {
                LOG.debug("[{}] -> [updateBehaviorVersionCurrentCost]: Updating behavior budgets due to action: [{}] : [{}]",
                        this.getClass().getSimpleName(), behaviorAction, behaviorExecutionInfo);
            }

            @Override
            public void updateBehaviorVersionCurrentCostErrors(Errors outcome)
            {
                LOG.error("[{}] -> [updateBehaviorVersionCurrentCost]: Error occurred calling product budget service to update budgets for behavior service [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), behaviorExecutionInfo.getBehaviorServiceExecutedId(), outcome.getBodyExceptionMessage());

                throw new NovaException(BehaviorError.getApiCommunicationError(), outcome,
                        "[ProductBudgets API Client] -> [updateBehaviorVersionCurrentCost]: " +
                                "Error trying to call 'updateBehaviorVersionCurrentCostErrors' for budgets update");
            }
        }, behaviorExecutionInfo);
    }

    @Override
    public void updateBehaviorVersionPotentialCost(PBBehaviorPotentialCostInfo behaviorPotentialCostInfo)
    {
        LOG.debug("[{}] -> [updateBehaviorVersionPotentialCost]: Updating behavior version potential cost for behaviorVersionId [{}]",
                this.getClass().getSimpleName(), behaviorPotentialCostInfo.getBehaviorVersionId());

        this.restHandlerProductbudgetsapi.updateBehaviorVersionPotentialCost(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void updateBehaviorVersionPotentialCost()
            {
                LOG.debug("[{}] -> [updateBehaviorVersionPotentialCost]: Updating behavior version potential cost behaviorVersionId [{}]",
                        this.getClass().getSimpleName(), behaviorPotentialCostInfo.getBehaviorVersionId());
            }

            @Override
            public void updateBehaviorVersionPotentialCostErrors(Errors outcome)
            {
                LOG.error("[{}] -> [updateBehaviorVersionPotentialCost]: Error occurred calling Update behavior version potential cost for behaviorVersionId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), behaviorPotentialCostInfo.getBehaviorVersionId(), outcome.getBodyExceptionMessage());
                throw new NovaException(BehaviorError.getApiCommunicationError(), outcome,
                        "[ProductBudgets API Client] -> [updateBehaviorVersionPotentialCost]: " +
                                "Error trying to call 'updateBehaviorVersionPotentialCostErrors' for Update behavior version potential cost");
            }
        }, behaviorPotentialCostInfo);
    }
}

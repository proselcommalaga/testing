package com.bbva.enoa.platformservices.coreservice.budgetsapi.listener;


import com.bbva.enoa.apirestgen.budgetsapi.model.*;
import com.bbva.enoa.apirestgen.budgetsapi.server.spring.nova.rest.IRestListenerBudgetsapi;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.utils.BudgetsConstants;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.utils.BudgetsConstants.BudgetErrors;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceDeploymentException;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xe68445 on 11/10/2017.
 */
@Service
public class ListenerBudgets implements IRestListenerBudgetsapi
{
    /**
     * Budgets service
     */
    private final IBudgetsService budgetsService;


    /**
     * All args constructor for dependency injection
     *
     * @param budgetsService BudgetsService dependency
     */
    @Autowired
    public ListenerBudgets(final IBudgetsService budgetsService)
    {
        this.budgetsService = budgetsService;
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR)
    public DateObject calculateFinalDate(final NovaMetadata novaMetadata, final DateObject startDate) throws Errors
    {
        return this.budgetsService.calculateFinalDate(startDate);
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public BUDGServiceSummaryItem[] getProductServicesSummary(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        try
        {
            return this.budgetsService.getProductServicesSummary(productId).toArray(BUDGServiceSummaryItem[]::new);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR)
    public Boolean checkDeployabilityStatus(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return this.budgetsService.checkDeploymentPlanDeployabilityStatus(deploymentId);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceDeploymentException(exception, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public BUDGServiceDetail getServiceDetail(final NovaMetadata novaMetadata, final Long serviceId) throws Errors
    {
        return budgetsService.getServiceDetail(serviceId);
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR)
    public void updateProductService(final NovaMetadata novaMetadata, BUDGUpdatedService updatedService, final Long serviceId) throws Errors
    {
        this.budgetsService.updateService(updatedService, serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public BUDGServiceDetailItem[] getProductServicesDetail(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        try
        {
            return this.budgetsService.getProductServicesDetail(productId).toArray(BUDGServiceDetailItem[]::new);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = BudgetsConstants.BUDGETS_API, runtimeExceptionErrorCode = BudgetErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public BUDGProductBudgetsDTO getProductBudgets(final NovaMetadata novaMetadata, final Integer productId, final String env) throws Errors
    {
        try
        {
            return this.budgetsService.getProductBudgets(productId, env);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }
}

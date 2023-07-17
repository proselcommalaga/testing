package com.bbva.enoa.platformservices.coreservice.consumers.impl;


import com.bbva.enoa.apirestgen.schedulecontrolmapi.client.feign.nova.rest.IRestHandlerSchedulecontrolmapi;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.client.feign.nova.rest.IRestListenerSchedulecontrolmapi;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.client.feign.nova.rest.impl.RestHandlerSchedulecontrolmapi;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * Schedule request client
 */
@Slf4j
@Service
public class ScheduleControlMClient
{

    /**
     * Rest interfaces
     */
    @Autowired
    private IRestHandlerSchedulecontrolmapi restInterface;

    /**
     * API services.
     */
    private RestHandlerSchedulecontrolmapi restHandler;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerSchedulecontrolmapi(this.restInterface);
    }

    /**
     * Get active schedule request at the selected date
     *
     * @param productId   product Id
     * @param environment environment (INT/PRE/PRO)
     * @param atDate      Date
     * @return Schedule Batch Id
     */
    public ScheduleRequest getActiveRequestAt(Long productId, String environment, String atDate)
    {

        final SingleApiClientResponseWrapper<ScheduleRequest> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getActiveRequestAt(
                new IRestListenerSchedulecontrolmapi()
                {
                    @Override
                    public void getActiveRequestAt(ScheduleRequest outcome)
                    {
                        log.debug("[ScheduleControlMClient] Response from Schedule Control-M API: [{}]", outcome);
                        response.set(outcome);
                    }

                    @Override
                    public void getActiveRequestAtErrors(Errors outcome)
                    {
                        log.error("[ScheduleControlMClient] Error getting running active schedule request product [{}] at environment: [{}]",
                                productId, environment);
                        response.set(null);
                    }
                },
                productId, environment, atDate
        );
        // Return the number.
        return response.get();
    }

    /**
     * Fetch (ordered by status desc) all the instances, that are in a status valid for a Deployment Plan, for a given Product and Environment.
     *
     * @param productId   The ID of the given Product.
     * @param environment The given Environment.
     * @return The fetched instances (ordered by status).
     */
    public ScheduleRequest[] getValidForDeployment(Integer productId, String environment)
    {
        String className = this.getClass().getSimpleName();
        String methodName = "getValidForDeployment";

        final SingleApiClientResponseWrapper<ScheduleRequest[]> response = new SingleApiClientResponseWrapper<>();

        log.debug("[{}] -> [{}]: getting valid Schedule Requests for deployment for product [{}] and environment [{}]", className, methodName, productId, environment);
        this.restHandler.getValidForDeployment(
                new IRestListenerSchedulecontrolmapi()
                {
                    @Override
                    public void getValidForDeployment(ScheduleRequest[] outcome)
                    {
                        log.debug("[{}] -> [{}]: successfully got valid Schedule Requests for deployment for product [{}] and environment [{}]", className, methodName, productId, environment);
                        response.set(outcome);
                    }

                    @Override
                    public void getValidForDeploymentErrors(Errors outcome)
                    {
                        String detail = "";
                        if (outcome != null)
                        {
                            Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
                            if (firstErrorMessage.isPresent())
                            {
                                detail = firstErrorMessage.get().getMessage();
                            }
                        }
                        log.error("[{}] -> [{}]: error getting valid Schedule Requests for deployment for product [{}] and environment [{}]: {}", className, methodName, productId, environment, detail);
                        throw new NovaException(CommonError.getErrorCallingScheduleControlMApi(className, detail));
                    }
                },
                productId, environment
        );
        return response.get();
    }

}

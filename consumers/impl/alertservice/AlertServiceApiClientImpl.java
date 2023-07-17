package com.bbva.enoa.platformservices.coreservice.consumers.impl.alertservice;

import com.bbva.enoa.apirestgen.alertserviceapi.client.feign.nova.rest.IRestHandlerAlertserviceapi;
import com.bbva.enoa.apirestgen.alertserviceapi.client.feign.nova.rest.IRestListenerAlertserviceapi;
import com.bbva.enoa.apirestgen.alertserviceapi.client.feign.nova.rest.impl.RestHandlerAlertserviceapi;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASOverviewRootDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASPageableQueryDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASScheduledDeploymentAlertServiceDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Alert Service API Client
 */
@Service
public class AlertServiceApiClientImpl implements IAlertServiceApiClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AlertServiceApiClientImpl.class);

    /**
     * Rest interface
     */
    @Autowired
    private IRestHandlerAlertserviceapi iRestHandlerAlertserviceapi;

    /**
     * API service
     */
    private RestHandlerAlertserviceapi restHandlerAlertserviceapi;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerAlertserviceapi = new RestHandlerAlertserviceapi(this.iRestHandlerAlertserviceapi);
    }

    @Override
    public ASRequestAlertsDTO checkDeployPlanAlertInfo(Integer deployPlanId, String[] stateList)
    {
        SingleApiClientResponseWrapper<ASRequestAlertsDTO> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[AlertServiceAPI Client] -> [checkDeployPlanAlertInfo]: Calling Alert Service to obtain info for deployPlan with Id: {} ", deployPlanId);

        this.restHandlerAlertserviceapi.existAlertsInPlan(new IRestListenerAlertserviceapi()
        {
            @Override
            public void existAlertsInPlan(ASRequestAlertsDTO outcome)
            {
                LOG.debug("[AlertServiceApiClientImpl Client] -> [checkDeployPlanAlertInfo]: Launched deploying plan [{}]",
                        deployPlanId);
                response.set(outcome);
            }

            @Override
            public void existAlertsInPlanErrors(Errors outcome)
            {
                LOG.error("[AlertServiceApiClientImpl Client] -> [checkDeployPlanAlertInfo]: Error obtaining deployment plan alert info for deploy plan id: [{}]. Error Message: [{}]", deployPlanId, outcome.getBodyExceptionMessage());
                response.set(null);
            }
        }, deployPlanId, stateList);

        return response.get();
    }

    @Override
    public ASRequestAlertsDTO getAlertsByRelatedIdAndStatus(final String[] relatedIds, final Integer productId, final String uuaa, final String status)
    {
        SingleApiClientResponseWrapper<ASOverviewRootDTO> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[AlertServiceAPI Client] -> [getAlertsByRelatedIdAndStatus]: Calling Alert Service to obtain info for relatedIds: {} and Statuses [{}] ",
                Arrays.toString(relatedIds), status);

        // build object to make query by relatedId and status
        ASPageableQueryDTO asPageableQueryDTO = new ASPageableQueryDTO();
        asPageableQueryDTO.setPageNumber(0);
        asPageableQueryDTO.setPageSize(50);
        asPageableQueryDTO.setProductIdList(new int[]{productId});
        asPageableQueryDTO.setRelatedIdList(relatedIds);
        asPageableQueryDTO.setStatus(status);

        this.restHandlerAlertserviceapi.getPageableOverview(new IRestListenerAlertserviceapi()
        {
            @Override
            public void getPageableOverview(ASOverviewRootDTO outcome)
            {
                LOG.debug("[AlertServiceApiClientImpl Client] -> [getAlertsByRelatedIdAndStatus]: Launched deploying plan {}", Arrays.toString(relatedIds));
                response.set(outcome);
            }

            @Override
            public void getPageableOverviewErrors(Errors outcome)
            {
                LOG.error("[AlertServiceApiClientImpl Client] -> [getAlertsByRelatedIdAndStatus]: Error obtaining data for related Ids {}. Error: ", Arrays.toString(relatedIds), outcome);
                throw new NovaException(FilesystemsError.getAlertServiceCallError(), outcome.getMessage());
            }
        }, asPageableQueryDTO);

        return this.buildASRequestAlertsDTOFromPageableOverview(response.get(), uuaa);
    }

    @Override
    public ASBasicAlertInfoDTO[] getProductAlertsSinceDaysAgo(Integer daysAgo, String environment, String type, String uuaa)
    {
        SingleApiClientResponseWrapper<ASBasicAlertInfoDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[AlertServiceApiClientImpl] -> [getProductAlertsSinceDaysAgo]: getting Alerts since [{}] days ago, with Environment [{}], Type [{}], and UUAA [{}]", daysAgo, environment, type, uuaa);

        this.restHandlerAlertserviceapi.getProductAlertsSinceDaysAgo(new IRestListenerAlertserviceapi()
        {
            @Override
            public void getProductAlertsSinceDaysAgo(ASBasicAlertInfoDTO[] outcome)
            {
                LOG.debug("[AlertServiceApiClientImpl] -> [getProductAlertsSinceDaysAgo]: successfully got Alerts since [{}] days ago, with Environment [{}], Type [{}], and UUAA [{}]", daysAgo, environment, type, uuaa);
                response.set(outcome);
            }

            @Override
            public void getProductAlertsSinceDaysAgoErrors(Errors outcome)
            {
                LOG.error("[AlertServiceApiClientImpl] -> [getProductAlertsSinceDaysAgo]: Error trying to get Alerts since [{}] days ago, with Environment [{}], Type [{}], and UUAA [{}]: [{}]", daysAgo, environment, type, uuaa, outcome.getFirstErrorMessage());
                throw new NovaException(StatisticsError.getAlertServiceError(), outcome);
            }

        }, daysAgo, environment, type, uuaa);

        return response.get();
    }

    @Override
    public void registerProductAlert(final DeploymentScheduleDTO deploymentScheduleDTO)
    {
        // creating the ASAlertDTO with the new alert data
        var productAlertDTO = new ASAlertDTO();
        productAlertDTO.setAlertCode("SCHEDULED_DEPLOYMENT_ERROR_001");
        productAlertDTO.setGeneratePatrol(true);
        productAlertDTO.setGenerateEmail(true);
        productAlertDTO.setEnvironment(deploymentScheduleDTO.getEnvironment().getEnvironment());
        productAlertDTO.setAlertLifePeriod(60);

        // creating the specific data associated to the failed scheduled deployment
        var deploymentScheduledAlertServiceDTO = new ASScheduledDeploymentAlertServiceDTO();
        deploymentScheduledAlertServiceDTO.setDeploymentDate(deploymentScheduleDTO.getDeploymentDate());
        deploymentScheduledAlertServiceDTO.setDeploymentPlatform(deploymentScheduleDTO.getDeploymentPlatform());
        deploymentScheduledAlertServiceDTO.setIdentifier(deploymentScheduleDTO.getIdentifier());
        deploymentScheduledAlertServiceDTO.setError(deploymentScheduleDTO.getError());
        productAlertDTO.setDeploymentScheduledAlertServiceDTO(deploymentScheduledAlertServiceDTO);

        this.restHandlerAlertserviceapi.registerAlert(new IRestListenerAlertserviceapi()
        {
            @Override
            public void registerAlert()
            {
                LOG.info("[AlertServiceApiClientImpl] -> [registerProductAlert]: Registering product alert with Alert Service [{}]", productAlertDTO);
            }

            @Override
            public void registerAlertErrors(Errors outcome)
            {
                var message = outcome.getFirstErrorMessage().map(ErrorMessage::getMessage).orElse(String.valueOf(outcome.getBodyExceptionMessage()));
                LOG.error("[AlertServiceApiClientImpl] ->[registerProductAlert]: There was an error trying to register a product alert: [{}]. Error: [{}]", productAlertDTO, message);
                throw new NovaException(DeploymentError.getUnexpectedError(), outcome.getBodyExceptionMessage().toString());
            }
        }, productAlertDTO);

    }


    @Override
    public void registerGenericAlert(final ASAlertDTO alertDTO)
    {
        this.restHandlerAlertserviceapi.registerAlert(new IRestListenerAlertserviceapi()
        {
            @Override
            public void registerAlert()
            {
                LOG.info("[AlertServiceApiClientImpl] -> [registerGenericAlert]: Registered alert with Alert Service [{}]", alertDTO);
            }

            @Override
            public void registerAlertErrors(Errors outcome)
            {
                var message = outcome.getFirstErrorMessage().map(ErrorMessage::getMessage).orElse(String.valueOf(outcome.getBodyExceptionMessage()));
                LOG.error("[AlertServiceApiClientImpl] ->[registerGenericAlert]: There was an error trying to register an alert: [{}]. Error: [{}]", alertDTO, message);
                throw new NovaException(DeploymentError.getUnexpectedError(), outcome.getBodyExceptionMessage().toString());
            }
        }, alertDTO);

    }

    @Override
    public void closePlanRelatedAlerts(final DeploymentPlan deploymentPlan)
    {
        this.restHandlerAlertserviceapi.closeAssociatedAlerts(new IRestListenerAlertserviceapi()
        {
            @Override
            public void closeAssociatedAlerts()
            {
                LOG.info("[AlertServiceApiClientImpl] -> [closePlanRelatedAlerts]: Closed every related current alert of the deployment plan [{}]", deploymentPlan.getId());
            }

            @Override
            public void closeAssociatedAlertsErrors(final Errors outcome)
            {
                LOG.error("[AlertServiceApiClientImpl] -> [closePlanRelatedAlerts]: There was an error trying to close every related alert associated to plan [{}]. Error: [{}]",
                        deploymentPlan.getId(), outcome.getBodyExceptionMessage());
                throw new NovaException(DeploymentError.getUnexpectedError(), outcome.getBodyExceptionMessage().toString());
            }
        }, "DEPLOY", deploymentPlan.getId().toString());
    }

    @Override
    public void closeBrokerRelatedAlerts(final String relatedId)
    {
        this.restHandlerAlertserviceapi.closeAssociatedAlerts(new IRestListenerAlertserviceapi()
        {
            @Override
            public void closeAssociatedAlerts()
            {
                LOG.info("[AlertServiceApiClientImpl] -> [closeBrokerRelatedAlerts]: Closed every related broker alert with relatedId [{}]", relatedId);
            }

            @Override
            public void closeAssociatedAlertsErrors(final Errors outcome)
            {
                LOG.error("[AlertServiceApiClientImpl] -> [closeBrokerRelatedAlerts]: There was an error trying to close every related broker alert with relatedId [{}]. Error: [{}]", relatedId,
                        outcome.getBodyExceptionMessage());
                throw new NovaException(DeploymentError.getUnexpectedError(), outcome.getBodyExceptionMessage().toString());
            }
        }, "BROKER", relatedId);
    }

    /**
     * Build ASrequestAleertsDTO from PageableOverviewDto
     *
     * @param asOverviewRootDTO as overview root dto returned by alert service client
     * @param uuaa              the uuaa
     * @return the as request alerts dto
     */
    private ASRequestAlertsDTO buildASRequestAlertsDTOFromPageableOverview(final ASOverviewRootDTO asOverviewRootDTO, final String uuaa)
    {
        LOG.debug("[AlertServiceApiClientImpl Client] -> [buildASRequestAlertsDTOFromPageableOverview]: Building ASRequestDto from PageableOverview [{}]",
                asOverviewRootDTO);

        ASRequestAlertsDTO requestAlertsDTO = new ASRequestAlertsDTO();
        List<ASBasicAlertInfoDTO> basicAlertInfoDTOList = new ArrayList<>();
        // Set have alerts to false
        requestAlertsDTO.setHaveAlerts(false);

        // Get all alerts
        if (asOverviewRootDTO != null && asOverviewRootDTO.getTotalElementsCount() != 0)
        {
            requestAlertsDTO.setHaveAlerts(true);
            Arrays.stream(asOverviewRootDTO.getFields())
                    .filter(fieldsDTO -> fieldsDTO.getUuaa().equals(uuaa))
                    .forEach(fieldsDTO -> Arrays.stream(fieldsDTO.getAlertsInfo())
                            .forEach(overviewDetailsDTO -> {
                                ASBasicAlertInfoDTO basicAlertInfoDTO = new ASBasicAlertInfoDTO();
                                basicAlertInfoDTO.setAlertId(overviewDetailsDTO.getId());
                                basicAlertInfoDTO.setAlertRelatedId(overviewDetailsDTO.getRelatedId());
                                basicAlertInfoDTO.setAlertType(overviewDetailsDTO.getAlertType());
                                basicAlertInfoDTO.setStatus(overviewDetailsDTO.getStatus());
                                basicAlertInfoDTOList.add(basicAlertInfoDTO);
                            })
                    );
            requestAlertsDTO.setBasicAlertInfo(basicAlertInfoDTOList.toArray(new ASBasicAlertInfoDTO[0]));
        }
        LOG.debug("[AlertServiceApiClientImpl Client] -> [buildASRequestAlertsDTOFromPageableOverview]: Building ASRequestDto from PageableOverview [{}]. Result:[{}]",
                asOverviewRootDTO,
                requestAlertsDTO);

        return requestAlertsDTO;
    }
}

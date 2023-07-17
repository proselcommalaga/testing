package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.schedulermanagerapi.client.feign.nova.rest.IRestHandlerSchedulermanagerapi;
import com.bbva.enoa.apirestgen.schedulermanagerapi.client.feign.nova.rest.IRestListenerSchedulermanagerapi;
import com.bbva.enoa.apirestgen.schedulermanagerapi.client.feign.nova.rest.impl.RestHandlerSchedulermanagerapi;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.BatchServiceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.ContextParamDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.CreateDeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.CreateDeploymentPlanContextParamsDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DisabledDateDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.ScheduledDeploymentDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SchedulerDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.StateBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.UpdateDeploymentBatchScheduleDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.enums.BatchSchedulerInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.common.base.Strings;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Feign Client for communication with Scheduler Manager service
 */
@Slf4j
@Service
public class SchedulerManagerClientImpl implements ISchedulerManagerClient
{
    private static final String CREATE_DEPLOYMENT_PLAN_CONTEXT_PARAMS_STRING = "createDeploymentPlanContextParams";
    private static final String RELEASE_VERSION_SERVICE_ID_CHUNK_MESSAGE = "release version service id: [";
    private static final String DEPLOYMENT_PLAN_ID_CHUNK_MESSAGE = "] and deployment plan id: [";
    private static final String ERROR_MESSAGE_CHUNK_MESSAGE = "]. Error message: [";
    private static final String ERROR_MESSAGE_CHUNK_STARTING_MESSAGE = "Error message: [";
    /**
     * Attribute - Rest Handler - Interface
     */
    private final IRestHandlerSchedulermanagerapi iRestHandlerSchedulermanagerapi;

    /**
     * Attribute - Rest Handler - Impl
     */
    private RestHandlerSchedulermanagerapi restHandlerSchedulerapi;

    /**
     * Constructor
     *
     * @param iRestHandlerSchedulermanagerapi iRestHandlerSchedulermanagerapi
     */
    @Autowired
    public SchedulerManagerClientImpl(final IRestHandlerSchedulermanagerapi iRestHandlerSchedulermanagerapi)
    {
        this.iRestHandlerSchedulermanagerapi = iRestHandlerSchedulermanagerapi;
    }

    /**
     * Post construct
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerSchedulerapi = new RestHandlerSchedulermanagerapi(this.iRestHandlerSchedulermanagerapi);
    }

    ////////////////////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////

    @Override
    public void saveBatchSchedulerService(final int batchSchedulerServiceId, final byte[] schedulerYmlFile, final String batchSchedulerServiceName,
                                          final String novaYmlFile, Map<Integer, String> batchIdServiceNameMap)
    {
        log.debug("[{}] -> [{}]: saving a batch scheduler with release version service id: [{}] from BBDD calling Scheduler Manager ",
                Constants.SCHEDULER_MANAGER_CLIENT, "saveBatchSchedulerService", batchSchedulerServiceId);

        // Build SchedulerDTO
        SchedulerDTO schedulerDTO = this.buildSchedulerDTO(schedulerYmlFile, batchSchedulerServiceName, novaYmlFile, batchIdServiceNameMap);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.saveSchedule(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void saveSchedule()
            {
                log.debug("[{}] -> [{}]: saved a batch scheduler with release version service id: [{}] from BBDD calling Scheduler Manager ",
                        Constants.SCHEDULER_MANAGER_CLIENT, "saveBatchSchedulerService", batchSchedulerServiceId);
            }

            @Override
            public void saveScheduleErrors(Errors errors)
            {
                Optional<ErrorMessage> firstErrorMessage = errors.getFirstErrorMessage();
                String errorDetails = firstErrorMessage.isPresent() ? firstErrorMessage.get().getMessage() : errors.getBodyExceptionMessage().toString();
                String message = "[SchedulerManagerClientImpl] -> [saveBatchSchedulerService]: there was an error trying to save a batch schedule service id: [" + batchSchedulerServiceId + "] with name: [" +
                        batchSchedulerServiceName + "] into Scheduler Manager BBDD. Error message: [" + errorDetails + "]";
                log.error(message);
                throw new NovaException(ReleaseVersionError.getBatchSchedulerSaveError(errorDetails), errors, message);
            }
        }, schedulerDTO, batchSchedulerServiceId);
    }

    @Override
    public DeploymentBatchScheduleDTO getDeploymentBatchSchedule(final Integer releaseVersionServiceId, final Integer deploymentPlanId)
    {
        log.debug("[{}] -> [getDeploymentBatchSchedule]: getting the deployment batch schedule for release version service id: [{}] and deployment plan id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, releaseVersionServiceId, deploymentPlanId);

        SingleApiClientResponseWrapper<DeploymentBatchScheduleDTO> response = new SingleApiClientResponseWrapper<>(new DeploymentBatchScheduleDTO());

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.getDeploymentBatchSchedule(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void getDeploymentBatchSchedule(final DeploymentBatchScheduleDTO outcome)
            {
                response.set(outcome);
                log.debug("[{}] -> [getDeploymentBatchSchedule]: obtained the deployment batch schedule for release version service id: [{}] and deployment plan id: [{}]. Response: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, releaseVersionServiceId, deploymentPlanId, outcome);
            }

            @Override
            public void getDeploymentBatchScheduleErrors(Errors errors)
            {
                log.error("[{}] -> [getDeploymentBatchSchedule]: error getting the deployment batch schedule for release version service id: [{}] and deployment plan id: [{}]. Error message: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, releaseVersionServiceId, deploymentPlanId, errors.getBodyExceptionMessage());
            }
        }, releaseVersionServiceId, deploymentPlanId);

        return response.get();
    }

    @Override
    public void removeBatchSchedulerService(final int batchSchedulerServiceId)
    {
        log.debug("[{}] -> [{}]: removing a batch scheduler with release version service id: [{}] from BBDD calling Scheduler Manager successfully.",
                Constants.SCHEDULER_MANAGER_CLIENT, "removeBatchSchedulerService", batchSchedulerServiceId);


        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.removeSchedule(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void removeSchedule()
            {
                log.debug("[{}] -> [{}]: removed a batch scheduler with release version service id: [{}] from BBDD calling Scheduler Manager successfully.",
                        Constants.SCHEDULER_MANAGER_CLIENT, "removeBatchSchedulerService", batchSchedulerServiceId);
            }

            @Override
            public void removeScheduleErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [removeBatchSchedulerService]: there was an error trying to delete a batch schedule service id: [" + batchSchedulerServiceId + "] " +
                        "from Scheduler Manager BBDD. Error message: [" + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(ReleaseVersionError.getBatchScheluderDeleteError(), standardHttpException, message);
            }

        }, batchSchedulerServiceId);
    }

    @Override
    public void createDeploymentPlanContextParams(final int releaseVersionServiceId, final int deploymentPlanId, final String environment)
    {
        log.debug("[{}] -> [{}]: creating the deployment context params for batch service id: [{}] and deployment plan id: [{}] in environment: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, CREATE_DEPLOYMENT_PLAN_CONTEXT_PARAMS_STRING, releaseVersionServiceId, deploymentPlanId, environment);

        CreateDeploymentPlanContextParamsDTO createDeploymentBatchScheduleDTO = new CreateDeploymentPlanContextParamsDTO();
        createDeploymentBatchScheduleDTO.setEnvironment(environment);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.createDeploymentPlanContextParams(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void createDeploymentPlanContextParams()
            {
                log.debug("[{}] -> [{}]: created the deployment context params for batch service id: [{}] and deployment plan id: [{}] in environment: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, CREATE_DEPLOYMENT_PLAN_CONTEXT_PARAMS_STRING, releaseVersionServiceId, deploymentPlanId, environment);
            }

            @Override
            public void createDeploymentPlanContextParamsErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [createDeploymentPlanContextParams]: there was an error trying to create a deployment plan context params with " +
                        RELEASE_VERSION_SERVICE_ID_CHUNK_MESSAGE + releaseVersionServiceId + DEPLOYMENT_PLAN_ID_CHUNK_MESSAGE + deploymentPlanId + "] in environment: [" + environment + ERROR_MESSAGE_CHUNK_MESSAGE + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(DeploymentError.getDeploymentContextParamsError(), standardHttpException, message);
            }

        }, createDeploymentBatchScheduleDTO, releaseVersionServiceId, deploymentPlanId, null, null);
    }

    @Override
    public void createDeploymentBatchSchedule(final int releaseVersionServiceId, final int deploymentPlanId, final int releaseSubsystemId, final String environment)
    {
        log.debug("[{}] -> [{}]: creating the deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}] in environment: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "createDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId, environment);

        CreateDeploymentBatchScheduleDTO createDeploymentBatchScheduleDTO = new CreateDeploymentBatchScheduleDTO();
        createDeploymentBatchScheduleDTO.setEnvironment(environment);
        createDeploymentBatchScheduleDTO.setReleaseSubsystemId(releaseSubsystemId);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.createDeploymentBatchSchedule(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void createDeploymentBatchSchedule()
            {
                log.debug("[{}] -> [{}]: created a deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}] in environment: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "createDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId, environment);
            }

            @Override
            public void createDeploymentBatchScheduleErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [createDeploymentBatchSchedule]: there was an error trying to create a deployment plan batch schedule with " +
                        RELEASE_VERSION_SERVICE_ID_CHUNK_MESSAGE + releaseVersionServiceId + "]- release subsystem id: [" + releaseSubsystemId + DEPLOYMENT_PLAN_ID_CHUNK_MESSAGE + deploymentPlanId + "]" +
                        " in environment: [" + environment + ERROR_MESSAGE_CHUNK_MESSAGE + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(DeploymentError.getDeploymentBatchScheduleError(), standardHttpException, message);
            }

        }, createDeploymentBatchScheduleDTO, releaseVersionServiceId, deploymentPlanId);


    }

    @Override
    public void updateDeploymentBatchSchedule(final int releaseVersionServiceId, final int deploymentPlanId, final DeploymentBatchScheduleStatus deploymentBatchScheduleStatus)
    {
        log.debug("[{}] -> [{}]: updating the deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "updateDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId);

        UpdateDeploymentBatchScheduleDTO updateDeploymentBatchScheduleDTO = new UpdateDeploymentBatchScheduleDTO();
        updateDeploymentBatchScheduleDTO.setState(deploymentBatchScheduleStatus.getDeploymentBatchScheduleStatus());

        try
        {
            // Call Scheduler Manager client
            this.restHandlerSchedulerapi.updateDeploymentBatchSchedule(new IRestListenerSchedulermanagerapi()
            {
                @Override
                public void updateDeploymentBatchSchedule()
                {
                    log.debug("[{}] -> [{}]: undeployed the deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}]",
                            Constants.SCHEDULER_MANAGER_CLIENT, "updateDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId);
                }

                @Override
                public void updateDeploymentBatchScheduleErrors(Errors errors)
                {
                    String message = "[SchedulerManagerClientImpl] -> [updateDeploymentBatchSchedule]: there was an error trying to update a deployment plan batch schedule with " +
                            RELEASE_VERSION_SERVICE_ID_CHUNK_MESSAGE + releaseVersionServiceId + "], deployment plan id: [" + deploymentPlanId + "] and deployment plan status: [" + deploymentBatchScheduleStatus.name() + "]. " +
                            ERROR_MESSAGE_CHUNK_STARTING_MESSAGE + errors.getBodyExceptionMessage() + "]";
                    log.error(message);
                    if (DeploymentBatchScheduleStatus.ENABLED == deploymentBatchScheduleStatus)
                    {
                        String errorCode = errors.getFirstErrorMessage().orElse(new ErrorMessage("")).getCode();
                        if ("SCHEDULERMANAGER-008".equals(errorCode))
                        {
                            throw new NovaException(ServiceRunnerError.getCronExpressionError(), errors, message);
                        }
                        else if ("SCHEDULERMANAGER-025".equals(errorCode))
                        {
                            throw new NovaException(ServiceRunnerError.getCronExpressionRequirementsError(), errors, message);
                        }
                        throw new NovaException(ServiceRunnerError.getEnableBatchScheduleServiceError(), errors, message);
                    }
                    else if (DeploymentBatchScheduleStatus.DISABLED == deploymentBatchScheduleStatus)
                    {
                        throw new NovaException(ServiceRunnerError.getDisableBatchScheduleServiceError(), errors, message);
                    }
                    else
                    {
                        throw new NovaException(DeploymentError.getUpdateDeploymentBatchScheduleError(), errors, message);
                    }
                }

            }, updateDeploymentBatchScheduleDTO, releaseVersionServiceId, deploymentPlanId);
        }
        catch (HystrixRuntimeException | FeignException e)
        {
            log.error("[{}] -> [updateDeploymentBatchSchedule]: there was an unexpected error updating deploymetn batch schedule for release version service id: [{}] - deployment plan ind: [{}] and deploymentBatchScheduleStatus: [{}]. Error message: [{}]",
                    Constants.SCHEDULER_MANAGER_CLIENT, releaseVersionServiceId, deploymentPlanId, deploymentBatchScheduleStatus, e.getMessage());
        }
    }

    @Override
    public void removeDeploymentBatchSchedule(final int releaseVersionServiceId, final int deploymentPlanId)
    {
        log.debug("[{}] -> [{}]: removing the deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "removeDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.removeDeploymentBatchSchedule(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void removeDeploymentBatchSchedule()
            {
                log.debug("[{}] -> [{}]: removed the deployment batch schedule for batch schedule service id: [{}] and deployment plan id: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "removeDeploymentBatchSchedule", releaseVersionServiceId, deploymentPlanId);
            }

            @Override
            public void removeDeploymentBatchScheduleErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [removeDeploymentBatchSchedule]: there was an error trying to remove a deployment plan schedule with " +
                        RELEASE_VERSION_SERVICE_ID_CHUNK_MESSAGE + releaseVersionServiceId + DEPLOYMENT_PLAN_ID_CHUNK_MESSAGE + deploymentPlanId + ERROR_MESSAGE_CHUNK_MESSAGE + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(DeploymentError.getRemoveDeploymentBatchScheduleError(), standardHttpException, message);
            }
        }, releaseVersionServiceId, deploymentPlanId);

    }

    @Override
    public void removeDeploymentPlanContextParams(final int deploymentPlanId)
    {
        log.debug("[{}] -> [{}]: removing the deployment plan context params of the deployment plan id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "removeDeploymentPlanContextParams", deploymentPlanId);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.deleteDeploymentPlanContextParams(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void deleteDeploymentPlanContextParams()
            {
                log.debug("[{}] -> [{}]: removed the deployment plan context params of the deployment plan id: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "removeDeploymentPlanContextParams", deploymentPlanId);
            }

            @Override
            public void deleteDeploymentPlanContextParamsErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [removeDeploymentPlanContextParams]: there was an error trying to remove a deployment plan context params of " +
                        "deployment plan id: [" + deploymentPlanId + ERROR_MESSAGE_CHUNK_MESSAGE + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(DeploymentError.getRemoveDeploymentContextParamsError(), standardHttpException, message);
            }

        }, deploymentPlanId);

    }

    @Override
    public void copyDeploymentPlanContextParams(final int originalReleaseVersionServiceId, final int oldDeploymentPlanId, final int newDeploymentPlanId, final String environment, final Integer targetReleaseVersionServiceId)
    {
        log.debug("[{}] -> [{}]: copying the deployment context params for batch service id: [{}] and old deployment plan id: [{}] - new deployment plan id: [{}] in environment: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, CREATE_DEPLOYMENT_PLAN_CONTEXT_PARAMS_STRING, originalReleaseVersionServiceId, oldDeploymentPlanId, newDeploymentPlanId, environment);

        CreateDeploymentPlanContextParamsDTO createDeploymentBatchScheduleDTO = new CreateDeploymentPlanContextParamsDTO();
        createDeploymentBatchScheduleDTO.setEnvironment(environment);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.createDeploymentPlanContextParams(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void createDeploymentPlanContextParams()
            {
                log.debug("[{}] -> [{}]: copied the deployment context params for batch service id: [{}] and old deployment plan id: [{}] - new deployment plan id: [{}] in environment: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, CREATE_DEPLOYMENT_PLAN_CONTEXT_PARAMS_STRING, originalReleaseVersionServiceId, oldDeploymentPlanId, newDeploymentPlanId, environment);
            }

            @Override
            public void createDeploymentPlanContextParamsErrors(Errors standardHttpException)
            {
                String message = "[SchedulerManagerClientImpl] -> [copyDeploymentPlanContextParams]: there was an error trying to copy a deployment plan context params of " +
                        "old deployment plan id: [" + oldDeploymentPlanId + "] to new deployment plan id: [" + newDeploymentPlanId + "] in environment: [" + environment + "]. " +
                        "Release version service id: [" + originalReleaseVersionServiceId + ERROR_MESSAGE_CHUNK_MESSAGE + standardHttpException.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(DeploymentError.getCopyDeploymentContextParamsError(), standardHttpException, message);
            }

        }, createDeploymentBatchScheduleDTO, targetReleaseVersionServiceId, newDeploymentPlanId, originalReleaseVersionServiceId, oldDeploymentPlanId);
    }

    @Override
    public DeploymentBatchScheduleInstanceDTO stateBatchScheduleInstance(final Integer batchScheduleInstanceId, final BatchSchedulerInstanceStatus instanceTypeState)
    {
        log.debug("[{}] -> [{}]: updating the state [{}] for the batch schedule instance id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "stateBatchScheduleInstance", instanceTypeState.getTypeInstance(), batchScheduleInstanceId);

        StateBatchScheduleInstanceDTO stateBatchScheduleInstanceDTO = new StateBatchScheduleInstanceDTO();
        stateBatchScheduleInstanceDTO.setStatus(instanceTypeState.getTypeInstance());

        SingleApiClientResponseWrapper<DeploymentBatchScheduleInstanceDTO> response = new SingleApiClientResponseWrapper<>();

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.stateBatchScheduleInstance(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void stateBatchScheduleInstance(final DeploymentBatchScheduleInstanceDTO outcome)
            {
                log.debug("[{}] -> [{}]: updated the state [{}] for the batch schedule instance id: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "stateBatchScheduleInstance", stateBatchScheduleInstanceDTO.getStatus(), batchScheduleInstanceId);
                response.set(outcome);
            }

            @Override
            public void stateBatchScheduleInstanceErrors(Errors errors)
            {
                String message = "[SchedulerManagerClientImpl] -> [stateBatchScheduleInstance]: there was an error trying to update the state: [" +
                        stateBatchScheduleInstanceDTO.getStatus() + "], batch scheduler instance id: [" + batchScheduleInstanceId + "]. " +
                        ERROR_MESSAGE_CHUNK_STARTING_MESSAGE + errors.getBodyExceptionMessage() + "]";
                log.error(message);
                //TODO @Oscar ¿esta condicion se da alguna vez? ¿Alguna vez se informa el detailMessage de un objeto Errors?
                if (HttpStatus.NOT_FOUND.value() == errors.getStatus() &&
                        errors.getMessage().startsWith("java.lang.RuntimeException: com.netflix.client.ClientException"))
                {
                    throw new NovaException(ServiceRunnerError.getBatchSchedulerError(), errors, message);
                }
                else if (BatchSchedulerInstanceStatus.START_INSTANCE == instanceTypeState)
                {
                    throw new NovaException(ServiceRunnerError.getStartInstanceError(), errors, message);
                }
                else if (BatchSchedulerInstanceStatus.STOP_INSTANCE == instanceTypeState)
                {
                    throw new NovaException(ServiceRunnerError.getStopInstanceError(), errors, message);
                }
                else if (BatchSchedulerInstanceStatus.PAUSE_INSTANCE == instanceTypeState)
                {
                    throw new NovaException(ServiceRunnerError.getPauseInstanceError(), errors, message);
                }
                else if (BatchSchedulerInstanceStatus.RESUME_INSTANCE == instanceTypeState)
                {
                    throw new NovaException(ServiceRunnerError.getResumeInstanceError(), errors, message);
                }

            }

        }, stateBatchScheduleInstanceDTO, batchScheduleInstanceId);

        return response.get();
    }

    @Override
    public void startScheduleInstance(final Integer releaseVersionServiceId, final Integer deploymentPlanId)
    {
        log.debug("[{}] -> [{}]: start a batch scheduler instance with release version service id [{}] and the deployment plan id: [{}]",
                Constants.SCHEDULER_MANAGER_CLIENT, "startScheduleInstance", releaseVersionServiceId, deploymentPlanId);


        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.startScheduleInstance(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void startScheduleInstance()
            {
                log.debug("[{}] -> [{}]: started a batch scheduler instance with release version service id [{}] and the deployment plan id: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "startScheduleInstance", releaseVersionServiceId, deploymentPlanId);
            }

            @Override
            public void startScheduleInstanceErrors(Errors errors)
            {
                String message = "[SchedulerManagerClientImpl] -> [startScheduleInstance]: there was an error trying to start the release version service id: [" +
                        releaseVersionServiceId + "], deployment plan id: [" + deploymentPlanId + "]. " +
                        ERROR_MESSAGE_CHUNK_STARTING_MESSAGE + errors.getBodyExceptionMessage() + "]";
                log.error(message);

                String errorCode = errors.getFirstErrorMessage().orElse(new ErrorMessage("")).getCode();

                if ("SCHEDULERMANAGER-036".equals(errorCode))
                {
                    throw new NovaException(ServiceRunnerError.getBatchAgentError(), errors, message);
                }
                else if ("SCHEDULERMANAGER-037".equals(errorCode))
                {
                    throw new NovaException(ServiceRunnerError.getBatchManagerError(), errors, message);
                }
                //TODO @Oscar ¿esta condicion se da alguna vez? ¿Alguna vez se informa el detailMessage de un objeto Errors?
                else if (HttpStatus.NOT_FOUND.value() == errors.getStatus() &&
                        errors.getMessage().startsWith("java.lang.RuntimeException: com.netflix.client.ClientException"))
                {
                    throw new NovaException(ServiceRunnerError.getBatchSchedulerError(), errors, message);
                }
                else
                {
                    throw new NovaException(ServiceRunnerError.getStartInstanceError(), errors, message);
                }
            }

        }, releaseVersionServiceId, deploymentPlanId);

    }

    @Override
    public boolean scheduleDeployment(final DeploymentPlan deploymentPlan)
    {
        log.debug("[{}] -> [{}]: schedule a nova planned for the deploymentPlan id: [{}]", Constants.SCHEDULER_MANAGER_CLIENT, "scheduleDeployment", deploymentPlan.getId());

        SingleApiClientResponseWrapper<Boolean> booleanSingleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        ScheduledDeploymentDTO scheduledDeploymentDTO = new ScheduledDeploymentDTO();
        scheduledDeploymentDTO.setBatchPlanId(deploymentPlan.getNova().getBatch());
        scheduledDeploymentDTO.setDeploymentDateTime(deploymentPlan.getNova().getDeploymentDateTime().toInstant().toString());
        if (!Strings.isNullOrEmpty(deploymentPlan.getNova().getDeploymentList()))
        {
            scheduledDeploymentDTO.setDeploymentList(Arrays.stream(deploymentPlan.getNova().getDeploymentList().split(",")).mapToInt(Integer::parseInt).toArray());
        }
        scheduledDeploymentDTO.setPriorityLevel(deploymentPlan.getNova().getPriorityLevel().getPriority());
        scheduledDeploymentDTO.setUndeployRelease(deploymentPlan.getNova().getUndeployRelease());

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.scheduleDeployment(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void scheduleDeployment()
            {
                log.debug("[{}] -> [{}]: schedule a nova planned for the deploymentPlan Id: [{}]", Constants.SCHEDULER_MANAGER_CLIENT, "scheduleDeployment", deploymentPlan.getId());
                booleanSingleApiClientResponseWrapper.set(true);
            }

            @Override
            public void scheduleDeploymentErrors(Errors standardHttpException)
            {
                log.error("[SchedulerManagerClientImpl] -> [scheduleDeployment]: there was an error trying to schedule a nova planned deploymentPlanid: [{}] and current deployment plan status: [{}]. Error message: [{}]",
                        deploymentPlan.getId(), deploymentPlan.getStatus().name(), standardHttpException.getBodyExceptionMessage());
                booleanSingleApiClientResponseWrapper.set(false);
            }
        }, scheduledDeploymentDTO, deploymentPlan.getId());

        return booleanSingleApiClientResponseWrapper.get();
    }

    @Override
    public void unscheduleDeployment(Integer deploymentPlanId)
    {
        log.debug("[{}] -> [{}]: Unschedule a nova planned for the deploymentPlan id: [{}]", Constants.SCHEDULER_MANAGER_CLIENT, "unscheduleDeployment", deploymentPlanId);

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.unscheduleDeployment(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void unscheduleDeployment()
            {
                log.debug("[{}] -> [{}]: Unschedule a nova planned for the deploymentPlan Id: [{}]",
                        Constants.SCHEDULER_MANAGER_CLIENT, "unscheduleDeployment", deploymentPlanId);
            }

            @Override
            public void unscheduleDeploymentErrors(Errors errors)
            {
                String message = "[SchedulerManagerClientImpl] -> [unscheduleDeployment]: there was an error trying to unschedule a nova planned deploymentPlanid: [" + deploymentPlanId + "]. " +
                        ERROR_MESSAGE_CHUNK_STARTING_MESSAGE + errors.getBodyExceptionMessage() + "]";
                log.error(message);
                throw new NovaException(ServiceRunnerError.getPlannedUnScheduleError(), errors, message);
            }
        }, deploymentPlanId);
    }

    @Override
    public DeploymentBatchScheduleInstanceDTO[] getDeploymentBatchScheduleInstances(final ReleaseVersionService releaseVersionService, final DeploymentPlan deploymentPlan)
    {
        log.debug("[SchedulerManagerClientImpl] -> [getDeploymentBatchScheduleInstances]: get deployment batch schedule instances with " +
                "release version service id: [{}] - release version service name: [{}] and the deployment plan id: [{}]", releaseVersionService.getId(), releaseVersionService.getServiceName(), deploymentPlan.getId());

        SingleApiClientResponseWrapper<DeploymentBatchScheduleInstanceDTO[]> response = new SingleApiClientResponseWrapper<>();

        // Call Scheduler Manager client
        this.restHandlerSchedulerapi.getDeploymentBatchScheduleInstances(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void getDeploymentBatchScheduleInstances(final DeploymentBatchScheduleInstanceDTO[] outcome)
            {
                log.debug("[SchedulerManagerClientImpl] -> [getDeploymentBatchScheduleInstances]: get deployment batch schedule instances for release version service id: [{}] into deployment plan id: [{}] - environment: [{}]",
                        releaseVersionService.getId(), deploymentPlan.getId(), deploymentPlan.getEnvironment());
                response.set(outcome);
            }

            @Override
            public void getDeploymentBatchScheduleInstancesErrors(Errors standardHttpException)
            {
                throw new NovaException(DeploymentError.getErrorBatchSchedulerServiceWhenUndeployPlan(deploymentPlan.getId(), deploymentPlan.getEnvironment(), releaseVersionService.getServiceName(),
                        standardHttpException.getBodyExceptionMessage().toString()), standardHttpException);
            }

        }, releaseVersionService.getId(), deploymentPlan.getId());

        return response.get();
    }

    @Override
    public boolean isDisabledDateForDeploy(final Date date, final String uuaa, final Platform platform)
    {
        log.debug("[SchedulerManagerClientImpl] -> [isDisabledDateForDeploy]: check if date [{}], selected for deployment, is disabled for UUAA [{}]", date, uuaa);

        final String strDate = new SimpleDateFormat("yyyy/MM/dd").format(date);

        final DisabledDateDTO[] disabledDates = this.getDisabledDates(strDate, strDate, uuaa, platform.name());

        // if some date is returned it means it is disabled
        return disabledDates != null && disabledDates.length > 0;
    }

    @Override
    public SMBatchSchedulerExecutionsSummaryDTO getBatchSchedulerExecutionsSummary(int[] deploymentPlanIds)
    {
        SingleApiClientResponseWrapper<SMBatchSchedulerExecutionsSummaryDTO> response = new SingleApiClientResponseWrapper<>(new SMBatchSchedulerExecutionsSummaryDTO());
        this.restHandlerSchedulerapi.getBatchSchedulerExecutionsSummary(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void getBatchSchedulerExecutionsSummary(SMBatchSchedulerExecutionsSummaryDTO outcome)
            {
                log.debug("[SchedulerManagerClient] -> [getBatchSchedulerExecutionsSummary]: getting scheduled batch instances from deployment plan ids: {}", deploymentPlanIds);
                response.set(outcome);
            }

            @Override
            public void getBatchSchedulerExecutionsSummaryErrors(Errors outcome)
            {
                log.error("[SchedulerManagerClient] -> [getBatchSchedulerExecutionsSummary]: there was an error trying to get scheduled batch instances from deployment plan ids: {}. Error message: [{}]",
                        deploymentPlanIds, outcome.getBodyExceptionMessage());
            }
        }, deploymentPlanIds);
        return response.get();
    }

    @Override
    public DeploymentBatchScheduleInstanceDTO getDeploymentBatchScheduleInstanceById(final Integer deploymentBatchScheduleInstanceId)
    {
        SingleApiClientResponseWrapper<DeploymentBatchScheduleInstanceDTO> response = new SingleApiClientResponseWrapper<>(new DeploymentBatchScheduleInstanceDTO());
        this.restHandlerSchedulerapi.getDeploymentBatchScheduleInstanceById(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void getDeploymentBatchScheduleInstanceById(DeploymentBatchScheduleInstanceDTO outcome)
            {
                log.debug("[SchedulerManagerClient] -> [getDeploymentBatchScheduleInstanceById]: getting scheduled batch instance from deployment batch scheduler instance id: [{}]", deploymentBatchScheduleInstanceId);
                response.set(outcome);
            }

            @Override
            public void getDeploymentBatchScheduleInstanceByIdErrors(Errors outcome)
            {
                log.error("[SchedulerManagerClient] -> [getDeploymentBatchScheduleInstanceById]: there was an error trying to get scheduled batch deployment instance id: [{}]. Error message: [{}]",
                        deploymentBatchScheduleInstanceId, outcome.getBodyExceptionMessage());
            }
        }, deploymentBatchScheduleInstanceId);
        return response.get();
    }

    ////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * Build the Scheduler DTO to send the Scheduler manager client API
     *
     * @param schedulerYmlFile   the scheduler file byte array
     * @param batchSchedulerName the release version service name associated to the batch scheduler service
     * @param novaYmlFile        nova yml in string to get the context params needed to create the batch scheduler
     * @param batchNameMap       map with the release service version id of each batch scheduled
     * @return a SchedulerDTO
     */
    private SchedulerDTO buildSchedulerDTO(byte[] schedulerYmlFile, String batchSchedulerName, String novaYmlFile, Map<Integer, String> batchNameMap)
    {
        // Create SchedulerDto
        SchedulerDTO schedulerDTO = new SchedulerDTO();

        // Set scheduler yml and batch scheduler
        schedulerDTO.setSchedule(new String(Base64.getEncoder().encode(schedulerYmlFile)));
        schedulerDTO.setName(batchSchedulerName);

        // Create and set Batch Service DTO Array
        schedulerDTO.setBatches(this.createBatchServiceDTOs(batchNameMap));

        // Create and set ContextParamsDTO
        schedulerDTO.setContextParams(this.createContextParams(novaYmlFile));

        log.debug("[{}] -> [{}]: scheduler DTO built [{}] ", Constants.SCHEDULER_MANAGER_CLIENT, "buildSchedulerDTO", schedulerDTO);
        return schedulerDTO;
    }

    /**
     * Creates the Context Params DTO
     *
     * @param novaYmlFile the nova file in string format
     * @return a ContextParams DTO array
     */
    private ContextParamDTO[] createContextParams(String novaYmlFile)
    {
        Yaml yaml = new Yaml();

        // Get the nova.yml
        Map<String, Object> novaFileMap = new LinkedHashMap<>();
        String novaYmlString = novaYmlFile.replace("@", "");
        novaFileMap.putAll((Map<String, Object>) yaml.load(novaYmlString));

        ArrayList<Map<String, Object>> contextParamsMap = (ArrayList<Map<String, Object>>) novaFileMap.get("contextParams");
        ContextParamDTO[] contextParamsDTOList = new ContextParamDTO[contextParamsMap.size()];

        int i = 0;
        for (Map<String, Object> contextParam : contextParamsMap)
        {
            ContextParamDTO contextParamsDTO = new ContextParamDTO();
            contextParamsDTO.setName((String) contextParam.get("name"));
            contextParamsDTO.setParamType((String) contextParam.get("type"));
            contextParamsDTO.setValue((String) contextParam.get("defaultValue"));
            contextParamsDTO.setDescription((String) contextParam.get("description"));

            contextParamsDTOList[i] = contextParamsDTO;
            i++;
        }

        log.debug("[{}] -> [{}]: the context params created are [{}] ", Constants.SCHEDULER_MANAGER_CLIENT, "createContextParams", Arrays.toString(contextParamsDTOList));
        return contextParamsDTOList;
    }

    /**
     * Creates a Batch Service DTO array
     *
     * @param batchNameMap a map with the id and names of the batch services
     * @return a Batch Service DTO array
     */
    private BatchServiceDTO[] createBatchServiceDTOs(Map<Integer, String> batchNameMap)
    {
        BatchServiceDTO[] batchServiceDTOS = new BatchServiceDTO[batchNameMap.size()];

        int i = 0;
        for (Map.Entry<Integer, String> integerStringSet : batchNameMap.entrySet())
        {
            BatchServiceDTO batchServiceDTO = new BatchServiceDTO();
            batchServiceDTO.setId(integerStringSet.getKey());
            batchServiceDTO.setName(integerStringSet.getValue());

            batchServiceDTOS[i] = batchServiceDTO;
            i++;
        }

        log.debug("[{}] -> [{}]: the job DTO array created are [{}] ", Constants.SCHEDULER_MANAGER_CLIENT, "createBatchServiceDTOs", Arrays.toString(batchServiceDTOS));
        return batchServiceDTOS;
    }

    /**
     * Gets a list of disabled dates for deployment within a date range for specific UUAA and platform
     *
     * @param fromDate Begin of date range
     * @param toDate   End of date range
     * @param uuaa     Associated UUAA
     * @param platform Platfom where deployment will occur
     * @return list of disabled dates
     */
    private DisabledDateDTO[] getDisabledDates(final String fromDate, final String toDate, final String uuaa, final String platform)
    {
        log.debug("[SchedulerManagerClientImpl] -> [getDisabledDates]: get disabled dates from [{}] to [{}] for UUAA [{}]", fromDate, toDate, uuaa);

        SingleApiClientResponseWrapper<DisabledDateDTO[]> response = new SingleApiClientResponseWrapper<>(new DisabledDateDTO[0]);

        this.restHandlerSchedulerapi.getDisabledDates(new IRestListenerSchedulermanagerapi()
        {
            @Override
            public void getDisabledDates(final DisabledDateDTO[] outcome)
            {
                log.debug("[SchedulerManagerClientImpl] -> [getDisabledDates]: found [{}] disabled dates from [{}] to [{}] for UUAA [{}]: [{}]", outcome.length, fromDate, toDate, uuaa, outcome);
                response.set(outcome);
            }

            @Override
            public void getDisabledDatesErrors(Errors outcome)
            {
                final String message = MessageFormat.format("[SchedulerManagerClientImpl] -> [getDisabledDates]: there was an error trying to find disabled dates from [{0}] to [{1}] for UUAA [{2}]: {3}", fromDate, toDate, uuaa, outcome.getBodyExceptionMessage());
                log.error(message);
                throw new NovaException(DeploymentError.getImpossibleToGetDisabledDatesForDeploymentError(), outcome, message);
            }
        }, fromDate, uuaa, platform, toDate);

        return response.get();
    }
}

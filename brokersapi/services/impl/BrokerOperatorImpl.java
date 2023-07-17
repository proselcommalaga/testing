package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.NewBrokerInfo;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerOperator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerTaskProcessor;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Calendar;

@Service
public class BrokerOperatorImpl implements IBrokerOperator
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerOperatorImpl.class);

    /**
     * Permissions
     */
    protected static final NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

    /**
     * JPA repository for Broker
     */
    private final BrokerRepository brokerRepository;

    /**
     * Broker Deployment API client
     */
    private final IBrokerDeploymentClient brokerDeploymentClient;

    /**
     * User service client
     */
    private final IUsersClient usersService;

    /**
     * ProductBudgets service
     */
    private final IProductBudgetsService budgetsService;

    /**
     * Broker builder
     */
    private final IBrokerBuilder brokerBuilder;

    /**
     * Broker validator
     */
    private final IBrokerValidator brokerValidator;

    /**
     * Broker Api todo task service
     */
    private final BrokerAPIToDoTaskService brokerAPIToDoTaskService;

    /**
     * TaskProcessor
     */
    private final IBrokerTaskProcessor taskProcessor;

    /**
     *  Validations Util
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Instantiates a new Broker service.
     *
     * @param brokerRepository       the broker repository
     * @param brokerDeploymentClient the broker deployment client
     * @param usersService           the users service
     * @param budgetsService         the budgets service
     * @param brokerBuilder          the broker builder
     * @param brokerValidator        the broker validator
     * @param brokerAPIToDoTaskService  the broker api todo task service
     * @param taskProcessor          the task processor
     */
    @Autowired
    public BrokerOperatorImpl(final BrokerRepository brokerRepository,
                              final IBrokerDeploymentClient brokerDeploymentClient,
                              final IUsersClient usersService,
                              final IProductBudgetsService budgetsService,
                              final IBrokerBuilder brokerBuilder,
                              final IBrokerValidator brokerValidator,
                              final BrokerAPIToDoTaskService brokerAPIToDoTaskService,
                              final IBrokerTaskProcessor taskProcessor,
                              final ManageValidationUtils manageValidationUtils)
    {
        this.brokerRepository = brokerRepository;
        this.brokerDeploymentClient = brokerDeploymentClient;
        this.usersService = usersService;
        this.budgetsService = budgetsService;
        this.brokerBuilder = brokerBuilder;
        this.brokerValidator = brokerValidator;
        this.brokerAPIToDoTaskService = brokerAPIToDoTaskService;
        this.taskProcessor = taskProcessor;
        this.manageValidationUtils = manageValidationUtils;
    }

    @Override
    public BrokerDTO createBroker(String ivUser, BrokerDTO brokerDTO)
    {
        // Check permissions
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.CREATE_BROKER, brokerDTO.getProductId(), PERMISSION_DENIED);

        // Perform validations and create broker entity
        Broker broker = this.brokerBuilder.validateAndBuildBrokerEntity(brokerDTO);

        // Persist
        broker = this.brokerRepository.saveAndFlush(broker);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerDeploymentResultOK = this.brokerDeploymentClient.createBroker(broker.getId());

        if (brokerDeploymentResultOK)
        {
            LOG.info("[BrokerOperatorImpl] -> [createBroker]: Requested broker creation to BrokerDeploymentAPI");

            // Update budget
            this.updateBrokerBudget(broker.getId(), brokerDTO.getEnvironment(), brokerDTO.getNumberOfNodes(), brokerDTO.getHardwarePackId(), brokerDTO.getProductId());

            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }
        else
        {
            LOG.error("[BrokerOperatorImpl] -> [createBroker]: Error occurred while requesting broker creation to BrokerDeploymentAPI");

            this.brokerRepository.delete(broker);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentCreateError(broker.getId()));
        }
    }

    @Override
    public void deleteBroker(String ivUser, Integer brokerId)
    {
        // Check broker exists
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // Check permissions
        Integer productId = broker.getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.DELETE_BROKER, broker.getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerCanBeDeleted(broker);

        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.DELETING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerDeletingResultOK = this.brokerDeploymentClient.deleteBroker(brokerId);

        if (brokerDeletingResultOK)
        {
            LOG.info("[BrokerOperatorImpl] -> [deleteBroker]: Requested broker deletion to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // End. Wait for async notification of result (callback)
        }
        else
        {
            LOG.error("[BrokerOperatorImpl] -> [deleteBroker]: Error occurred while requesting broker deletion to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // Update status
            updateBrokerStatus(broker, BrokerStatus.DELETE_ERROR);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentDeleteError(brokerId));
        }
    }

    @Override
    public BrokerDTO stopBroker(String ivUser, Integer brokerId)
    {
        // Check broker exists
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // Check permissions
        Integer productId = broker.getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.STOP_BROKER, broker.getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerCanBeStopped(broker);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, broker);

        if (!brokerActionManagedOk)
        {
            String messageInfo = MessageFormat.format("parar en {0} el bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(broker, ToDoTaskType.BROKER_STOP_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.STOPPING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerStoppingResultOK = this.brokerDeploymentClient.stopBroker(brokerId);

        if (brokerStoppingResultOK)
        {
            LOG.info("[BrokerOperatorImpl] -> [stopBroker]: Requested broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }
        else
        {
            LOG.error("[BrokerOperatorImpl] -> [stopBroker]: Error occurred while requesting broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentStopError(brokerId));
        }
    }

    @Override
    public BrokerDTO startBroker(String ivUser, Integer brokerId)
    {
        // Check broker exists
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // Check permissions
        Integer productId = broker.getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.START_BROKER, broker.getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerCanBeStarted(broker);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, broker);

        if (!brokerActionManagedOk)
        {
            String messageInfo = MessageFormat.format("arrancar en {0} el bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(broker, ToDoTaskType.BROKER_START_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.STARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerStartingResultOK = this.brokerDeploymentClient.startBroker(brokerId);

        if (brokerStartingResultOK)
        {
            LOG.info("[BrokerOperatorImpl] -> [startBroker]: Requested broker start to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }
        else
        {
            LOG.error("[BrokerOperatorImpl] -> [startBroker]: Error occurred while requesting broker start to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.STOPPED);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentStartError(brokerId));
        }
    }

    @Override
    public BrokerDTO restartBroker(String ivUser, Integer brokerId)
    {
        // Check broker exists
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // Check permissions
        Integer productId = broker.getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.RESTART_BROKER, broker.getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerCanBeStopped(broker);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, broker);

        if (!brokerActionManagedOk)
        {
            String messageInfo = MessageFormat.format("reiniciar en {0} el bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(broker, ToDoTaskType.BROKER_RESTART_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.RESTARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerRestartingResultOK = this.brokerDeploymentClient.restartBroker(brokerId);

        if (brokerRestartingResultOK)
        {
            LOG.info("[BrokerOperatorImpl] -> [restartBroker]: Requested broker restart to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }
        else
        {
            LOG.error("[BrokerOperatorImpl] -> [restartBroker]: Error occurred while requesting broker restart to BrokerDeploymentAPI, for broker id [{}]", brokerId);

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentRestartError(brokerId));
        }
    }

    private Broker updateBrokerStatus(Broker broker, BrokerStatus newStatus)
    {
        broker.setStatus(newStatus);
        broker.setStatusChanged(Calendar.getInstance());
        broker = this.brokerRepository.saveAndFlush(broker);
        return broker;
    }

    private void updateBrokerBudget(Integer brokerId, String environment, Integer numberOfNodes, Integer hardwarePackId, Integer productId)
    {
        NewBrokerInfo brokerInfo = new NewBrokerInfo();
        brokerInfo.setBrokerId(brokerId);
        brokerInfo.setEnvironment(environment);
        brokerInfo.setNumberOfNodes(numberOfNodes);
        brokerInfo.setHardwarePackId(hardwarePackId);

        this.budgetsService.insertBroker(brokerInfo, productId);
    }

    @Override
    public void onTaskReply(Integer taskId, Integer brokerId)
    {
        LOG.debug("[BrokerOperatorService] -> [onTaskReply]: Executing onTaskReply");
        // Get the broker
        Broker broker = brokerRepository.findById(brokerId).orElseThrow(() -> new NovaException(BrokerError.getBrokerNotFoundError(brokerId), "Broker [" + brokerId + "] not found"));

        // Process the task
        taskProcessor.onTaskreply(broker, null, taskId);

        LOG.debug("[BrokerOperatorService] -> [onTaskReply]: DONE this on task reply: [{}]", broker);
    }

    private void createToDoTask(Broker broker, ToDoTaskType toDoTaskType, String messageInfo, String ivUser)
    {
        brokerAPIToDoTaskService.createRequestBrokerTask(ivUser, broker, null, BrokerConstants.BrokerErrorCode.BROKER_ENVIRONMENT_ERROR_CODE, toDoTaskType, messageInfo);
    }
}

package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNotificationDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerNodeOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNotificationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFilesystemManagerClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.INovaActivitiesClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class BrokerNotificationServiceImpl implements IBrokerNotificationService
{

    private static final Logger LOG = LoggerFactory.getLogger(BrokerNotificationServiceImpl.class);

    /**
     * Broker repository
     */
    private final BrokerRepository brokerRepository;

    /**
     * Nova activities client
     */
    private final INovaActivitiesClient novaActivitiesClient;

    /**
     * Budgets service
     */
    private final IProductBudgetsService budgetsService;

    /**
     * Broker validator
     */
    private final IBrokerValidator brokerValidator;

    /**
     * Filesystem manager client
     */
    private final IFilesystemManagerClient filesystemManagerClient;

    /**
     * Alert service client
     */
    private final IAlertServiceApiClient alertServiceClient;

    /**
     * Broker Api service
     */
    private final BrokerAPIToDoTaskService brokerAPIToDoTaskService;

    /**
     * The broker task repository
     */
    private final BrokerTaskRepository brokerTaskRepository;


    @Autowired
    public BrokerNotificationServiceImpl(final BrokerRepository brokerRepository, final INovaActivitiesClient novaActivitiesClient,
                                         final IProductBudgetsService budgetsService, final IBrokerValidator brokerValidator,
                                         final IFilesystemManagerClient filesystemManagerClient, final IAlertServiceApiClient alertServiceClient,
                                         final BrokerAPIToDoTaskService brokerAPIToDoTaskService, final BrokerTaskRepository brokerTaskRepository)
    {
        this.brokerRepository = brokerRepository;
        this.novaActivitiesClient = novaActivitiesClient;
        this.budgetsService = budgetsService;
        this.brokerValidator = brokerValidator;
        this.filesystemManagerClient = filesystemManagerClient;
        this.alertServiceClient = alertServiceClient;
        this.brokerAPIToDoTaskService = brokerAPIToDoTaskService;
        this.brokerTaskRepository = brokerTaskRepository;
    }

    @Override
    @Transactional
    public void notifyBrokerOperationResult(Integer brokerId, BrokerNotificationDTO brokerNotificationDTO)
    {
        // Check valid broker operation
        BrokerOperation brokerOperation = this.brokerValidator.validateAndGetBrokerOperation(brokerNotificationDTO.getOperation());

        // Check broker exists
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // NOTE: permissions are not check because currently there is no tamper-resistant way

        boolean operationResult = brokerNotificationDTO.getResultOK();

        // Ignore if not expected broker status
        if (this.isNotExpectedStatus(brokerOperation.name(), broker.getStatus(), operationResult))
        {
            LOG.warn("[BrokerNotificationServiceImpl] -> [notifyBrokerOperationResult] Broker id [{}] is not in expected status. Current status: [{}]. Ignoring notification.",
                    brokerId, broker.getStatus());
            return;
        }

        String requesterIvUser = brokerNotificationDTO.getRequesterIvUser();
        String descriptionMessage = brokerNotificationDTO.getMessage();

        LOG.info("notifyBrokerOperationResult: operation: {}", brokerOperation.name());
        switch (brokerOperation)
        {
            case CREATE:
                this.processNotifyBrokerCreate(broker, operationResult, requesterIvUser, descriptionMessage);
                break;
            case DELETE:
                this.processNotifyBrokerDelete(broker, operationResult, requesterIvUser, descriptionMessage);
                break;
            case STOP:
            case START:
            case RESTART:
                this.processNotifyBrokerGenericOperation(brokerOperation.getActivityAction(), broker, operationResult, requesterIvUser, descriptionMessage);
                break;
        }
    }

    @Override
    public void notifyBrokerNodeOperationResult(Integer brokerNodeId, BrokerNotificationDTO brokerNotificationDTO)
    {
        // Check valid broker node operation
        BrokerNodeOperation brokerNodeOperation = this.brokerValidator.validateAndGetBrokerNodeOperation(brokerNotificationDTO.getOperation());

        // Check node exists
        BrokerNode brokerNode = this.brokerValidator.validateAndGetBrokerNode(brokerNodeId);

        boolean operationResult = brokerNotificationDTO.getResultOK();

        // Ignore if not expected broker node status
        if (this.isNotExpectedStatus(brokerNodeOperation.name(), brokerNode.getStatus(), operationResult))
        {
            LOG.warn("[BrokerNotificationServiceImpl] -> [notifyBrokerNodeOperationResult] Broker node id [{}] is not in expected status. Current status: [{}]. Ignoring notification.",
                    brokerNodeId, brokerNode.getStatus());
            return;
        }

        String requesterIvUser = brokerNotificationDTO.getRequesterIvUser();
        String descriptionMessage = brokerNotificationDTO.getMessage();
        this.processNotifyBrokerNodeGenericOperation(brokerNodeOperation.getActivityAction(), brokerNode, operationResult, requesterIvUser, descriptionMessage);
    }

    private void processNotifyBrokerCreate(Broker broker, boolean wasSuccessfulCreation, String requesterIvUser, String descriptionMessage)
    {
        if (wasSuccessfulCreation)
        {
            LOG.info("[BrokerNotificationServiceImpl] -> [notifyBrokerCreate] Notified successful creation of broker id [{}].", broker.getId());
        }
        else
        {
            LOG.info("[BrokerNotificationServiceImpl] -> [notifyBrokerCreate] Notified failed creation of broker id [{}]. Error message: [{}]", broker.getId(), descriptionMessage);
        }

        // Register activity
        registerBrokerActivity(ActivityAction.CREATED, broker, wasSuccessfulCreation, descriptionMessage, requesterIvUser);
    }

    private void processNotifyBrokerGenericOperation(ActivityAction action, Broker broker, boolean operationResult, String requesterIvUser, String descriptionMessage)
    {
        if (operationResult)
        {
            LOG.debug("[BrokerNotificationServiceImpl] -> [processNotifyBrokerNodeOperation] Notified successful operation [{}] of broker id [{}].", action, broker.getId());
        }
        else
        {
            LOG.info("[BrokerNotificationServiceImpl] -> [processNotifyBrokerNodeOperation] Notified failed operation [{}] of broker id [{}]. Error message: [{}]", action, broker.getId(), descriptionMessage);
        }

        // Register activity
        registerBrokerActivity(action, broker, operationResult, descriptionMessage, requesterIvUser);
    }

    private void processNotifyBrokerDelete(Broker broker, boolean wasSuccessfulDeletion, String requesterIvUser, String descriptionMessage)
    {
        if (!wasSuccessfulDeletion)
        {
            LOG.info("[BrokerNotificationServiceImpl] -> [notifyBrokerDelete] Notified failed deletion of broker id [{}]. Error message: [{}]", broker.getId(), descriptionMessage);

            // Register activity with error message
            registerBrokerActivity(ActivityAction.DELETED, broker, false, descriptionMessage, requesterIvUser);

            return;
        }

        LOG.debug("[BrokerNotificationServiceImpl] -> [notifyBrokerDelete] Notified successful deletion of broker id [{}].", broker.getId());

        // Clean filesystem space associated to broker
        if (cleanFilesystem(broker))
        {
            // Restore broker budget
            this.budgetsService.deleteBroker(broker.getId());

            // Close open alerts
            this.alertServiceClient.closeBrokerRelatedAlerts(broker.getId().toString());
            broker.getNodes().forEach(node ->
                    this.alertServiceClient.closeBrokerRelatedAlerts(node.getId().toString() + "-" + broker.getId().toString())
            );

            // Delete todo task
            deleteBrokerTodoTask(broker.getId());

            // Register activity
            registerBrokerActivity(ActivityAction.DELETED, broker, true, "", requesterIvUser);

            // Delete broker from DB
            this.brokerRepository.delete(broker);
        }
        else
        {
            // Change status a DELETE_ERROR
            broker.setStatus(BrokerStatus.DELETE_ERROR);
            broker.setStatusChanged(Calendar.getInstance());
            this.brokerRepository.save(broker);

            // Register activity with error message
            String errorMessage = "Error cleaning filesystem";
            registerBrokerActivity(ActivityAction.DELETED, broker, false, errorMessage, requesterIvUser);
        }
    }

    private void processNotifyBrokerNodeGenericOperation(ActivityAction action, BrokerNode brokerNode, boolean operationResult, String requesterIvUser, String descriptionMessage)
    {
        if (operationResult)
        {
            LOG.debug("[BrokerNotificationServiceImpl] -> [processNotifyBrokerNodeOperation] Notified successful operation [{}] of broker node id [{}].", action, brokerNode.getId());
        }
        else
        {
            LOG.info("[BrokerNotificationServiceImpl] -> [processNotifyBrokerNodeOperation] Notified failed operation [{}] of broker node id [{}]. Error message: [{}]", action, brokerNode.getId(),
                    descriptionMessage);
        }

        // Register activity
        registerBrokerNodeActivity(action, brokerNode, operationResult, descriptionMessage, requesterIvUser);
    }

    private boolean cleanFilesystem(Broker broker)
    {
        Filesystem filesystem = broker.getFilesystem();

        if (filesystem != null)
        {
            FSFileLocationModel pathToDelete = new FSFileLocationModel();
            pathToDelete.setPath("/" + broker.getProduct().getUuaa() + "/" + filesystem.getName().toLowerCase() + "/resources/brokers");
            pathToDelete.setFilename(broker.getName());
            try
            {
                filesystemManagerClient.callDeleteFile(broker.getFilesystem().getId(), pathToDelete);
            }
            catch (NovaException e)
            {
                String errorMessage = MessageFormat.format("Error cleaning filesystem in broker [{0}] ",  broker.getName());
                LOG.error("[BrokerNotificationServiceImpl] -> [cleanFilesystem]: {}", errorMessage);
                brokerAPIToDoTaskService.createBrokerTodoTask(broker, null, e, ToDoTaskType.BROKER_DELETE_ERROR, "cleanFilesystem", errorMessage);
                return false;
            }
        }
        return true;
    }

    private boolean isNotExpectedStatus(String operation, BrokerStatus currentStatus, boolean operationResult)
    {
        boolean isExpectedStatus = false;

        switch (operation)
        {
            case "CREATE":
                isExpectedStatus = isExpectedStatusForCreate(operationResult, currentStatus);
                break;
            case "DELETE":
                isExpectedStatus = isExpectedStatusForDelete(currentStatus, operationResult);
                break;
            case "START":
                isExpectedStatus = isExpectedStatusForStart(currentStatus, operationResult);
                break;
            case "STOP":
                isExpectedStatus = isExpectedStatusForStop(currentStatus, operationResult);
                break;
            case "RESTART":
                isExpectedStatus = isExpectedStatusForRestart(currentStatus);
                break;
            default:
        }
        return !isExpectedStatus;
    }

    private static boolean isExpectedStatusForRestart(final BrokerStatus currentStatus)
    {
        return currentStatus == BrokerStatus.RUNNING;
    }

    private static boolean isExpectedStatusForStop(final BrokerStatus currentStatus, final boolean operationResult)
    {
        return (operationResult && currentStatus == BrokerStatus.STOPPED)
                || (!operationResult && currentStatus == BrokerStatus.RUNNING);
    }

    private static boolean isExpectedStatusForStart(final BrokerStatus currentStatus, final boolean operationResult)
    {
        return (operationResult && currentStatus == BrokerStatus.RUNNING)
                || (!operationResult && (currentStatus == BrokerStatus.STOPPED || currentStatus == BrokerStatus.RUNNING));
    }

    private static boolean isExpectedStatusForDelete(final BrokerStatus currentStatus, final boolean operationResult)
    {
        return (operationResult && currentStatus == BrokerStatus.DELETING)
                || (!operationResult && currentStatus == BrokerStatus.DELETE_ERROR);
    }

    private static boolean isExpectedStatusForCreate(final boolean operationResult, final BrokerStatus currentStatus)
    {
        return (operationResult && currentStatus == BrokerStatus.RUNNING)
                || (!operationResult && currentStatus == BrokerStatus.CREATE_ERROR);
    }

    public void registerBrokerActivity(ActivityAction action, Broker broker, boolean wasSuccessfulOperation, String descriptionMessage, String requesterIvUser)
    {
        // Prepare activity with requester ivUser
        GenericActivity.Builder activityBuilder = new GenericActivity
                .Builder(broker.getProduct().getId(), ActivityScope.BROKER, action)
                .entityId(broker.getId())
                .environment(broker.getEnvironment())
                .addParam("brokerName", broker.getName())
                .addParam("brokerType", broker.getType())
                .addParam("platform", broker.getPlatform())
                .addParam("numberOfNodes", broker.getNumberOfNodes())
                .addParam("resultOperation", wasSuccessfulOperation ? "OK" : "ERROR");

        if (!wasSuccessfulOperation)
        {
            activityBuilder.addParam("errorMessage", descriptionMessage);
        }
        else if (descriptionMessage != null)
        {
            activityBuilder.addParam("extraInfo", descriptionMessage);
        }

        // Register activity
        this.novaActivitiesClient.registerActivity(activityBuilder.build(), requesterIvUser);
    }

    private void registerBrokerNodeActivity(ActivityAction action, BrokerNode brokerNode, boolean wasSuccessfulOperation, String errorMessage, String requesterIvUser)
    {
        Broker broker = brokerNode.getBroker();

        // Prepare activity with requester ivUser
        GenericActivity.Builder activityBuilder = new GenericActivity
                .Builder(broker.getProduct().getId(), ActivityScope.BROKER_NODE, action)
                .entityId(brokerNode.getId())
                .environment(broker.getEnvironment())
                .addParam("brokerName", broker.getName())
                .addParam("containerName", brokerNode.getContainerName())
                .addParam("resultOperation", wasSuccessfulOperation ? "OK" : "ERROR");

        if (!wasSuccessfulOperation)
        {
            activityBuilder.addParam("errorMessage", errorMessage);
        }

        // Register activity
        this.novaActivitiesClient.registerActivity(activityBuilder.build(), requesterIvUser);
    }

    public void deleteBrokerTodoTask(Integer brokerId)
    {
        List<BrokerTask> taskList = brokerTaskRepository.findByBrokerId(brokerId);
        brokerTaskRepository.deleteAll(taskList);
    }
}

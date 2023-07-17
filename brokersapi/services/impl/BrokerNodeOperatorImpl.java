package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNodeOperator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerTaskProcessor;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerNodeRepository;
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
import java.util.Objects;

@Service
public class BrokerNodeOperatorImpl implements IBrokerNodeOperator
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerNodeOperatorImpl.class);

    /**
     * Permissions
     */
    protected static final NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

    /**
     * JPA repository for BrokerNode
     */
    private final BrokerNodeRepository brokerNodeRepository;

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
     * Broker builder
     */
    private final IBrokerBuilder brokerBuilder;

    /**
     * Instantiates a new Broker service.
     *
     * @param brokerNodeRepository      the broker node repository
     * @param brokerRepository          the broker repository
     * @param brokerDeploymentClient    the broker deployment client
     * @param usersService              the users service
     * @param brokerValidator           the broker validator
     * @param brokerAPIToDoTaskService     the broker api todo task service
     * @param taskProcessor             the task processor
     * @param manageValidationUtils     the manage validations utils
     * @param brokerBuilder             the broker builder
     */
    @Autowired
    public BrokerNodeOperatorImpl(final BrokerNodeRepository brokerNodeRepository,
                                  final BrokerRepository brokerRepository,
                                  final IBrokerDeploymentClient brokerDeploymentClient,
                                  final IUsersClient usersService,
                                  final IBrokerValidator brokerValidator,
                                  BrokerAPIToDoTaskService brokerAPIToDoTaskService,
                                  final IBrokerTaskProcessor taskProcessor,
                                  final ManageValidationUtils manageValidationUtils,
                                  final IBrokerBuilder brokerBuilder)
    {
        this.brokerNodeRepository = brokerNodeRepository;
        this.brokerRepository = brokerRepository;
        this.brokerDeploymentClient = brokerDeploymentClient;
        this.usersService = usersService;
        this.brokerValidator = brokerValidator;
        this.brokerAPIToDoTaskService = brokerAPIToDoTaskService;
        this.taskProcessor = taskProcessor;
        this.manageValidationUtils = manageValidationUtils;
        this.brokerBuilder = brokerBuilder;
    }

    @Override
    public BrokerDTO stopBrokerNode(String ivUser, Integer brokerNodeId)
    {

        // Check broker node exists
        BrokerNode brokerNode = this.brokerValidator.validateAndGetBrokerNode(brokerNodeId);

        // Check permissions
        Integer productId = brokerNode.getBroker().getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.STOP_BROKER, brokerNode.getBroker().getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerNodeCanBeStopped(brokerNode);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, brokerNode.getBroker());

        if (!brokerActionManagedOk)
        {
            Broker broker = brokerNode.getBroker();
            String messageInfo = MessageFormat.format("parar en {0} uno de los nodos del bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(brokerNode, ToDoTaskType.BROKER_NODE_STOP_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.STOPPING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeStoppingResultOK = this.brokerDeploymentClient.stopBrokerNode(brokerNodeId);

        if (brokerNodeStoppingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [stopBrokerNode]: Requested broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerNodeId);

            // Update broker status when it is last running node
            boolean isLastRunningNode = this.areRestOfNodesNotRunning(brokerNode);
            if (isLastRunningNode)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.STOPPING);
            }
                // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(brokerNode.getBroker());
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [stopBrokerNode]: Error occurred while requesting broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerNodeId);

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentStopError(brokerNodeId));
        }
    }

    @Override
    public BrokerDTO startBrokerNode(String ivUser, Integer brokerNodeId)
    {
        // Check broker node exists
        BrokerNode brokerNode = this.brokerValidator.validateAndGetBrokerNode(brokerNodeId);

        // Check permissions
        Integer productId = brokerNode.getBroker().getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.START_BROKER, brokerNode.getBroker().getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerNodeCanBeStarted(brokerNode);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, brokerNode.getBroker());

        if (!brokerActionManagedOk)
        {
            Broker broker = brokerNode.getBroker();
            String messageInfo = MessageFormat.format("arrancar en {0} uno de los nodos del bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(brokerNode,  ToDoTaskType.BROKER_NODE_START_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.STARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeStartingResultOK = this.brokerDeploymentClient.startBrokerNode(brokerNodeId);

        if (brokerNodeStartingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [startBrokerNode]: Requested broker node start to BrokerDeploymentAPI, for broker node id [{}]", brokerNodeId);

            // Update broker status if it is the first node to start
            boolean isFirstNodeToStart = this.areRestOfNodesStoppedOrStopping(brokerNode);
            if (isFirstNodeToStart)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.STARTING);
            }
            // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(brokerNode.getBroker());
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [startBrokerNode]: Error occurred while requesting broker node start to BrokerDeploymentAPI, for broker node id [{}]", brokerNodeId);

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.STOPPED);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentStartError(brokerNodeId));
        }

    }

    @Override
    public BrokerDTO restartBrokerNode(String ivUser, Integer brokerNodeId)
    {
        // Check broker node exists
        BrokerNode brokerNode = this.brokerValidator.validateAndGetBrokerNode(brokerNodeId);

        // Check permissions
        Integer productId = brokerNode.getBroker().getProduct().getId();
        this.usersService.checkHasPermission(ivUser, BrokerPermissions.RESTART_BROKER, brokerNode.getBroker().getEnvironment(), productId, PERMISSION_DENIED);

        // Check action can be performed
        this.brokerValidator.validateBrokerNodeCanBeStopped(brokerNode);

        // Check broker action and user role
        boolean brokerActionManagedOk = manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(ivUser, brokerNode.getBroker());

        if (!brokerActionManagedOk)
        {
            Broker broker = brokerNode.getBroker();
            String messageInfo = MessageFormat.format("reiniciar en {0} uno de los nodos del bróker {1}", broker.getEnvironment(), broker.getName());
            this.createToDoTask(brokerNode, ToDoTaskType.BROKER_NODE_RESTART_REQUEST, messageInfo, ivUser);
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.RESTARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeRestartingResultOK = this.brokerDeploymentClient.restartBrokerNode(brokerNodeId);

        if (brokerNodeRestartingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [restartBrokerNode]: Requested broker node restart to BrokerDeploymentAPI, for broker node id [{}]", brokerNodeId);

            // Update broker status when it is last running node
            boolean isTheUniqueRunningNode = this.areRestOfNodesNotRunning(brokerNode);
            if (isTheUniqueRunningNode)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.RESTARTING);
            }

            // End. Wait for async notification of result (callback)
            return this.brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(brokerNode.getBroker());
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [restartBrokerNode]: Error occurred while requesting broker node restart to BrokerDeploymentAPI, for broker node id [{}]", brokerNodeId);

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentRestartError(brokerNodeId));
        }
    }

    private BrokerNode updateBrokerNodeStatus(BrokerNode brokerNode, BrokerStatus newStatus)
    {
        brokerNode.setStatus(newStatus);
        brokerNode.setStatusChanged(Calendar.getInstance());
        brokerNode = this.brokerNodeRepository.saveAndFlush(brokerNode);
        return brokerNode;
    }

    private void updateBrokerStatus(Broker broker, BrokerStatus newStatus)
    {
        broker.setStatus(newStatus);
        broker.setStatusChanged(Calendar.getInstance());
        this.brokerRepository.saveAndFlush(broker);
    }

    private boolean areRestOfNodesNotRunning(BrokerNode brokerNode)
    {
        return brokerNode.getBroker().getNodes().stream()
                .filter(node -> !Objects.equals(node.getId(), brokerNode.getId()))
                .allMatch(node -> node.getStatus() != BrokerStatus.RUNNING);
    }

    private boolean areRestOfNodesStoppedOrStopping(BrokerNode brokerNode)
    {
        return brokerNode.getBroker().getNodes().stream()
                .filter(node -> !Objects.equals(node.getId(), brokerNode.getId()))
                .allMatch(node -> node.getStatus() == BrokerStatus.STOPPED || node.getStatus() == BrokerStatus.STOPPING);
    }

    @Override
    public void onTaskReply(Integer taskId, Integer brokerNodeId)
    {
        LOG.debug("[BrokerNodeOperatorService] -> [onTaskReply]: Executing onTaskReply");

        // Get the broker node
        BrokerNode brokerNode = brokerNodeRepository.findById(brokerNodeId).orElseThrow(() -> new NovaException(BrokerError.getBrokerNodeNotFoundError(brokerNodeId), "Broker Node [" + brokerNodeId + "] not found"));

        // Process the task
        taskProcessor.onTaskreply(null, brokerNode, taskId);

        LOG.debug("[BrokerNodeOperatorService] -> [onTaskReply]: DONE this on task reply: [{}]", brokerNode);
    }

    private void createToDoTask(BrokerNode brokerNode, ToDoTaskType toDoTaskType, String messageInfo, String ivUser)
    {
        brokerAPIToDoTaskService.createRequestBrokerTask(ivUser, brokerNode.getBroker(), brokerNode.getId(), BrokerConstants.BrokerErrorCode.BROKER_ENVIRONMENT_ERROR_CODE, toDoTaskType, messageInfo);
    }
}

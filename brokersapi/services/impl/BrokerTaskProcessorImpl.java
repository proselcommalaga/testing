package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerTaskProcessor;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerNodeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Objects;

@Service
public class BrokerTaskProcessorImpl implements IBrokerTaskProcessor
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerOperatorImpl.class);

    /**
     * Broker repository
     */
    private final BrokerRepository brokerRepository;

    /**
     * Broker node repository
     */
    private BrokerNodeRepository brokerNodeRepository;

    /**
     * Broker Deployment API client
     */
    private final IBrokerDeploymentClient brokerDeploymentClient;

    /**
     * Broker Task Repository
     */
    private final BrokerTaskRepository brokerTaskRepository;

    /**
     * Constructor by params
     *
     * @param brokerRepository
     * @param brokerDeploymentClient
     * @param brokerNodeRepository
     * @param brokerTaskRepository
     */
    @Autowired
    public BrokerTaskProcessorImpl(final BrokerRepository brokerRepository, final IBrokerDeploymentClient brokerDeploymentClient, final BrokerNodeRepository brokerNodeRepository,
                             final BrokerTaskRepository brokerTaskRepository)
    {
        this.brokerRepository = brokerRepository;
        this.brokerDeploymentClient = brokerDeploymentClient;
        this.brokerTaskRepository = brokerTaskRepository;
        this.brokerNodeRepository = brokerNodeRepository;
    }

    /**
     *
     * @param broker
     * @param brokerNode
     * @param taskId
     */
    @Override
    public void onTaskreply(Broker broker, BrokerNode brokerNode, Integer taskId)
    {
        BrokerTask brokerTask = brokerTaskRepository.findById(taskId)
                .orElseThrow(() -> new NovaException(BrokerError.getUnexpectedError()));

        switch (brokerTask.getTaskType())
        {
            case BROKER_START_REQUEST:
                closeStartBrokerTask(broker);
                break;
            case BROKER_STOP_REQUEST:
                closeStopBrokerTask(broker);
                break;
            case BROKER_RESTART_REQUEST:
                closeRestartBrokerTask(broker);
                break;
            case BROKER_NODE_START_REQUEST:
                closeStartBrokerNodeTask(brokerNode);
                break;
            case BROKER_NODE_STOP_REQUEST:
                closeStopBrokerNodeTask(brokerNode);
                break;
            case BROKER_NODE_RESTART_REQUEST:
                closeRestartBrokerNodeTask(brokerNode);
                break;
            default:
                LOG.error("Error trying to process a todotask [{}]  of wrong tasktype [{}]", taskId, brokerTask.getTaskType());
        }
    }

    /**
     * Close the task starting broker
     *
     * @param broker
     */
    private void closeStartBrokerTask(Broker broker)
    {
        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.STARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerStartingResultOK = this.brokerDeploymentClient.startBroker(broker.getId());

        if (!brokerStartingResultOK)
        {
            LOG.error("[BrokerOperatorImpl] -> [startBroker]: Error occurred while requesting broker start to BrokerDeploymentAPI, for broker id [{}]", broker.getId());

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.STOPPED);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentStartError(broker.getId()));
        }
    }

    /**
     * Close the task stopping broker
     *
     * @param broker
     */
    private void closeStopBrokerTask(Broker broker)
    {
        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.STOPPING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerStoppingResultOK = this.brokerDeploymentClient.stopBroker(broker.getId());

        if (!brokerStoppingResultOK)
        {
            LOG.error("[BrokerOperatorImpl] -> [stopBroker]: Error occurred while requesting broker stop to BrokerDeploymentAPI, for broker id [{}]", broker.getId());

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentStopError(broker.getId()));
        }
    }

    /**
     * Close the task restarting broker
     *
     * @param broker
     */
    private void closeRestartBrokerTask(Broker broker)
    {
        // Update broker status
        broker = updateBrokerStatus(broker, BrokerStatus.RESTARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerRestartingResultOK = this.brokerDeploymentClient.restartBroker(broker.getId());

        if (!brokerRestartingResultOK)
        {
            LOG.error("[BrokerOperatorImpl] -> [restartBroker]: Error occurred while requesting broker restart to BrokerDeploymentAPI, for broker id [{}]", broker.getId());

            // Restore previous status
            updateBrokerStatus(broker, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerDeploymentRestartError(broker.getId()));
        }
    }

    /**
     * Close the task starting broker node
     *
     * @param brokerNode
     */
    private void closeStartBrokerNodeTask(BrokerNode brokerNode)
    {
        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.STARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeStartingResultOK = this.brokerDeploymentClient.startBrokerNode(brokerNode.getId());

        if (brokerNodeStartingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [startBrokerNode]: Requested broker node start to BrokerDeploymentAPI, for broker node id [{}]", brokerNode.getId());

            // Update broker status if it is the first node to start
            boolean isFirstNodeToStart = this.areRestOfNodesStoppedOrStopping(brokerNode);
            if (isFirstNodeToStart)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.STARTING);
            }

            // End. Wait for async notification of result (callback)
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [startBrokerNode]: Error occurred while requesting broker node start to BrokerDeploymentAPI, for broker node id [{}]", brokerNode.getId());

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.STOPPED);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentStartError(brokerNode.getId()));
        }
    }

    /**
     * Close the task stopping the broker node task
     *
     * @param brokerNode
     */
    private void closeStopBrokerNodeTask(BrokerNode brokerNode)
    {
        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.STOPPING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeStoppingResultOK = this.brokerDeploymentClient.stopBrokerNode(brokerNode.getId());

        if (brokerNodeStoppingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [stopBrokerNode]: Requested broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerNode.getId());

            // Update broker status when it is last running node
            boolean isLastRunningNode = this.areRestOfNodesNotRunning(brokerNode);
            if (isLastRunningNode)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.STOPPING);
            }
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [stopBrokerNode]: Error occurred while requesting broker stop to BrokerDeploymentAPI, for broker id [{}]", brokerNode.getId());

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentStopError(brokerNode.getId()));
        }
    }

    /**
     * Close the task restarting the broker node task
     *
     * @param brokerNode
     */
    private void closeRestartBrokerNodeTask(BrokerNode brokerNode)
    {
        // Update node status
        brokerNode = updateBrokerNodeStatus(brokerNode, BrokerStatus.RESTARTING);

        // Call to DeploymentManager (in the future: call EtherManager if given platform = ETHER)
        boolean brokerNodeRestartingResultOK = this.brokerDeploymentClient.restartBrokerNode(brokerNode.getId());

        if (brokerNodeRestartingResultOK)
        {
            LOG.info("[BrokerNodeOperatorImpl] -> [restartBrokerNode]: Requested broker node restart to BrokerDeploymentAPI, for broker node id [{}]", brokerNode.getId());

            // Update broker status when it is last running node
            boolean isTheUniqueRunningNode = this.areRestOfNodesNotRunning(brokerNode);
            if (isTheUniqueRunningNode)
            {
                updateBrokerStatus(brokerNode.getBroker(), BrokerStatus.RESTARTING);
            }

            // End. Wait for async notification of result (callback)
        }
        else
        {
            LOG.error("[BrokerNodeOperatorImpl] -> [restartBrokerNode]: Error occurred while requesting broker node restart to BrokerDeploymentAPI, for broker node id [{}]", brokerNode.getId());

            // Restore previous status
            updateBrokerNodeStatus(brokerNode, BrokerStatus.RUNNING);

            throw new NovaException(BrokerError.getFailedBrokerNodeDeploymentRestartError(brokerNode.getId()));
        }
    }

    /**
     * Update the broker status
     *
     * @param broker
     * @param newStatus
     * @return
     */
    private Broker updateBrokerStatus(Broker broker, BrokerStatus newStatus)
    {
        broker.setStatus(newStatus);
        broker.setStatusChanged(Calendar.getInstance());
        broker = this.brokerRepository.saveAndFlush(broker);
        return broker;
    }

    /**
     * Update the broker node status
     *
     * @param brokerNode
     * @param newStatus
     * @return
     */
    private BrokerNode updateBrokerNodeStatus(BrokerNode brokerNode, BrokerStatus newStatus)
    {
        brokerNode.setStatus(newStatus);
        brokerNode.setStatusChanged(Calendar.getInstance());
        brokerNode = this.brokerNodeRepository.saveAndFlush(brokerNode);
        return brokerNode;
    }

    /**
     *
     * @param brokerNode
     * @return
     */
    private boolean areRestOfNodesStoppedOrStopping(BrokerNode brokerNode)
    {
        return brokerNode.getBroker().getNodes().stream()
                .filter(node -> !Objects.equals(node.getId(), brokerNode.getId()))
                .allMatch(node -> node.getStatus() == BrokerStatus.STOPPED || node.getStatus() == BrokerStatus.STOPPING);
    }

    /**
     *
     * @param brokerNode
     * @return
     */
    private boolean areRestOfNodesNotRunning(BrokerNode brokerNode)
    {
        return brokerNode.getBroker().getNodes().stream()
                .filter(node -> !Objects.equals(node.getId(), brokerNode.getId()))
                .allMatch(node -> node.getStatus() != BrokerStatus.RUNNING);
    }
}

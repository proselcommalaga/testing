package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASNodeBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerMonitoringAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl.BrokerBuilderImpl;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl.BrokerValidatorImpl;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.MailUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl.BrokerAgentApiClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.logutils.annotations.TraceBackgroundProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Scheduler that checks if any broker node is unavailable 
 */
@Slf4j
@Component
public class NodeAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final NodeChecker nodeChecker;

    @Autowired
    public NodeAlertScheduler(BrokerRepository brokerRepository, NodeChecker nodeChecker)
    {
        this.brokerRepository = brokerRepository;
        this.nodeChecker = nodeChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.nodeBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.nodeBrokerAlert.initialDelay:45000}")
    public void checkRunningBrokers()
    {
        log.info("[NodeAlertScheduler] -> [checkRunningBrokers]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            nodeChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class NodeChecker
    {
        private final BrokerAgentApiClient brokerAgentClient;
        private final BrokerValidatorImpl brokerValidator;
        private final BrokerBuilderImpl brokerBuilder;
        private final IAlertServiceApiClient alertServiceClient;

        /**
         * Number of seconds to wait to check a node or broker after a start or restart operation
         */
        private static final int SECONDS_TO_WAIT_FOR_FULL_START = 60;

        public static final int FIFTEEN_MINUTES_IN_MILLISECONDS = 900000;

        @Autowired
        public NodeChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
                           BrokerBuilderImpl brokerBuilder, IAlertServiceApiClient alertServiceClient)
        {
            this.brokerAgentClient = brokerAgentClient;
            this.brokerValidator = brokerValidator;
            this.brokerBuilder = brokerBuilder;
            this.alertServiceClient = alertServiceClient;
        }

        @Async
        @Transactional(readOnly = true)
        @TraceBackgroundProcess
        public void checkBroker(Integer brokerId)
        {
            Broker broker = brokerValidator.validateAndGetBroker(brokerId);

            log.debug("[NodeChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check alert exists and is enabled
            BrokerAlertConfig alertConfig = (broker.getNumberOfNodes() > 1) ? brokerValidator.getAndValidateGenericAlertConfig(broker, GenericBrokerAlertType.UNAVAILABLE_NODE) : null;
            if (alertConfig == null || !alertConfig.isActive())
            {
                log.debug("[NodeChecker] -> [checkBroker]: Alerts are not active, skipping");
                return;
            }

            // Get list of running nodes for almost 60 seconds
            List<BrokerNode> runningNodes = broker.getNodes().stream()
                    .filter(node -> node.getStatus() == BrokerStatus.RUNNING)
                    .filter(node -> (System.currentTimeMillis() - node.getStatusChanged().getTimeInMillis()) / 1000 > SECONDS_TO_WAIT_FOR_FULL_START)
                    .collect(Collectors.toList());

            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);

            for (BrokerNode node : runningNodes)
            {
                ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, List.of(node));
                try
                {
                    if (!brokerAgentClient.isUp(broker.getEnvironment(), connectionDTO))
                    {
                        fireAlert(alertConfig, node);
                    }
                }
                catch (NovaException e)
                {
                    // Ignore connection problems with nova agent service and continue with next node
                }
            }

            log.debug("[NodeChecker] -> [checkBroker]: Checked broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
        }

        private void fireAlert(BrokerAlertConfig alertConfig, BrokerNode node)
        {
            log.debug("[NodeChecker] -> [fireAlert]: Alert is going to be fired! Node id [{}], container name [{}], reason: Node is down", node.getId(), node.getContainerName());
            try
            {
                Broker broker = node.getBroker();

                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_UNAVAILABLE_NODE_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(FIFTEEN_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASNodeBrokerAlertServiceDTO nodeAlertDTO = new ASNodeBrokerAlertServiceDTO();
                nodeAlertDTO.setProductId(broker.getProduct().getId());
                nodeAlertDTO.setUuaa(broker.getProduct().getUuaa());
                nodeAlertDTO.setBrokerId(broker.getId());
                nodeAlertDTO.setBrokerName(broker.getName());
                nodeAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                nodeAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                nodeAlertDTO.setBrokerNodeId(node.getId());
                nodeAlertDTO.setNodeContainerName(node.getContainerName());
                nodeAlertDTO.setOpeningPeriod(alertConfig.getOpeningPeriod());

                alertDTO.setNodeBrokerAlertServiceDTO(nodeAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);
                log.debug("[NodeChecker] -> [fireAlert]: Fired node alert! Node id [{}], container name [{}], reason: Node is down", node.getId(), node.getContainerName());
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

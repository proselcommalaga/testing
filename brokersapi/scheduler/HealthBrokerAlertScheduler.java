package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASHealthBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.BANodeInfoDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Scheduler that checks if any broker node is unavailable or broker health is not ok
 */
@Slf4j
@Component
public class HealthBrokerAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final HealthChecker healthChecker;

    @Autowired
    public HealthBrokerAlertScheduler(BrokerRepository brokerRepository, HealthChecker healthChecker)
    {
        this.brokerRepository = brokerRepository;
        this.healthChecker = healthChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.nodeAndHealthBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.nodeAndHealthBrokerAlert.initialDelay:40000}")
    public void checkRunningBrokers()
    {
        log.info("[HealthBrokerAlertScheduler] -> [checkRunningBrokers]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            healthChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class HealthChecker
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

        public static final String NO_NODES_UP = "No nodes are up";
        public static final String DISK_ALARM_ACTIVE = "Disk alarm is active";
        public static final String MEMORY_ALARM_ACTIVE = "Memory alarm is active";

        @Autowired
        public HealthChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
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

            log.debug("[HealthChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check if alert is enabled
            BrokerAlertConfig alertConfig = brokerValidator.getAndValidateGenericAlertConfig(broker, GenericBrokerAlertType.BROKER_HEALTH);
            if (!alertConfig.isActive())
            {
                log.debug("[HealthChecker] -> [checkBroker]: Alerts is not active, skipping");
                return;
            }

            // Get list of running nodes for almost 60 seconds
            List<BrokerNode> runningNodes = broker.getNodes().stream()
                    .filter(node -> node.getStatus() == BrokerStatus.RUNNING)
                    .filter(node -> (System.currentTimeMillis() - node.getStatusChanged().getTimeInMillis()) / 1000 > SECONDS_TO_WAIT_FOR_FULL_START)
                    .collect(Collectors.toList());

            if (runningNodes.isEmpty())
            {
                log.debug("[HealthChecker] -> [checkBroker]: No running nodes to check, skipping");
                return;
            }

            // Check if all running nodes are up
            if (allRunningNodesAreDown(broker, runningNodes))
            {
                this.fireAlert(alertConfig, broker, NO_NODES_UP);
                return;
            }

            // Check disk or memory alarms
            String alarmActive = getActiveAlarm(broker, runningNodes);
            if (alarmActive != null)
            {
                this.fireAlert(alertConfig, broker, alarmActive);
            }
        }

        private boolean allRunningNodesAreDown(Broker broker, List<BrokerNode> runningNodes)
        {
            int downNodes = 0;
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);

            for (BrokerNode node : runningNodes)
            {
                try
                {
                    ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, List.of(node));

                    if (!brokerAgentClient.isUp(broker.getEnvironment(), connectionDTO))
                    {
                        downNodes++;
                    }
                    else
                    {
                        // No need to check other nodes
                        return false;
                    }
                }
                catch (NovaException e)
                {
                    // Ignore connection problems with nova agent service
                    return false;
                }
            }

            long numStillStartingNodes = broker.getNodes().stream()
                    .filter(node -> node.getStatus() == BrokerStatus.RUNNING)
                    .filter(node -> (System.currentTimeMillis() - node.getStatusChanged().getTimeInMillis()) / 1000 <= SECONDS_TO_WAIT_FOR_FULL_START)
                    .count();

            // If all running nodes are down and none is still starting
            return downNodes == runningNodes.size() && numStillStartingNodes == 0;
        }

        // Check disk and memory alarms
        private String getActiveAlarm(Broker broker, List<BrokerNode> runningNodes)
        {
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);

            for (BrokerNode node : runningNodes)
            {
                try
                {
                    ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, List.of(node));

                    BANodeInfoDTO[] nodesInfo = brokerAgentClient.getNodesInfo(broker.getEnvironment(), connectionDTO);

                    if (Arrays.stream(nodesInfo).anyMatch(BANodeInfoDTO::getIsDiskAlarmActive))
                    {
                        return DISK_ALARM_ACTIVE;
                    }

                    if (Arrays.stream(nodesInfo).anyMatch(BANodeInfoDTO::getIsMemoryAlarmActive))
                    {
                        return MEMORY_ALARM_ACTIVE;
                    }
                }
                catch (NovaException e)
                {
                    // Ignore connection problems with nova agent service
                }
            }

            // No active alarm
            return null;
        }

        private void fireAlert(BrokerAlertConfig alertConfig, Broker broker, String cause)
        {
            log.debug("[HealthChecker] -> [fireAlert]: Alert is going to fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has bad health. Cause: {}",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), cause);

            try
            {
                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_HEALTH_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(FIFTEEN_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASHealthBrokerAlertServiceDTO healthAlertDTO = new ASHealthBrokerAlertServiceDTO();
                healthAlertDTO.setProductId(broker.getProduct().getId());
                healthAlertDTO.setUuaa(broker.getProduct().getUuaa());
                healthAlertDTO.setBrokerId(broker.getId());
                healthAlertDTO.setBrokerName(broker.getName());
                healthAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                healthAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                healthAlertDTO.setCause(cause);
                healthAlertDTO.setOpeningPeriod(alertConfig.getOpeningPeriod());

                alertDTO.setHealthBrokerAlertServiceDTO(healthAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);

                log.debug("[HealthChecker] -> [fireAlert]: Alert is fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has bad health. Cause: {}",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), cause);
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

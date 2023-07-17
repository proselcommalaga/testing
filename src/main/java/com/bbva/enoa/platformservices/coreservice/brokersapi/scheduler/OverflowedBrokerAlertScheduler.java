package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASCommonBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.BAConnectionInfoDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
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


/**
 * Scheduler that checks if any broker has overflowed (i.e. has connections marked as "flow")
 */
@Slf4j
@Component
public class OverflowedBrokerAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final OverflowedBrokerChecker overflowedBrokerChecker;

    @Autowired
    public OverflowedBrokerAlertScheduler(BrokerRepository brokerRepository, OverflowedBrokerChecker overflowedBrokerChecker)
    {
        this.brokerRepository = brokerRepository;
        this.overflowedBrokerChecker = overflowedBrokerChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.overflowedBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.overflowedBrokerAlert.initialDelay:50000}")
    public void checkOverflowedBrokers()
    {
        log.info("[OverflowedBrokerAlertScheduler] -> [checkOverflowedBrokers]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            overflowedBrokerChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class OverflowedBrokerChecker
    {
        private final BrokerAgentApiClient brokerAgentClient;
        private final BrokerValidatorImpl brokerValidator;
        private final BrokerBuilderImpl brokerBuilder;
        private final IAlertServiceApiClient alertServiceClient;

        public static final String FLOW_STATE = "flow";
        public static final int FIFTEEN_MINUTES_IN_MILLISECONDS = 900000;

        @Autowired
        public OverflowedBrokerChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
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

            log.debug("[OverflowedBrokerChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check alert is enabled
            BrokerAlertConfig alertConfig = brokerValidator.getAndValidateGenericAlertConfig(broker, GenericBrokerAlertType.OVERFLOWED_BROKER);
            if (!alertConfig.isActive())
            {
                log.debug("[OverflowedBrokerChecker] -> [checkBroker]: Alert is not active, skipping");
                return;
            }

            // Get connections from broker
            BAConnectionInfoDTO[] connectionsInfo = this.getConnectionsInfo(broker);
            if (connectionsInfo == null)
            {
                log.debug("[OverflowedBrokerChecker] -> [checkBroker]: Broker not accessible, skipping");
                return;
            }

            // Check if any connection is in flow state
            boolean hasFlowConnections = Arrays.stream(connectionsInfo)
                    .anyMatch(connectionInfoDTO -> connectionInfoDTO.getState() != null && connectionInfoDTO.getState().equals(FLOW_STATE));

            if (hasFlowConnections)
            {
                this.fireAlert(alertConfig, broker);
            }

            log.debug("[OverflowedBrokerChecker] -> [checkBroker]: Checked broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
        }

        private BAConnectionInfoDTO[] getConnectionsInfo(Broker broker)
        {
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
            ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, broker.getNodes());

            BAConnectionInfoDTO[] connectionsInfo = null;
            try
            {
                connectionsInfo = brokerAgentClient.getConnectionsInfo(broker.getEnvironment(), connectionDTO);
            }
            catch (NovaException e)
            {
                log.debug("[OverflowedBrokerChecker] -> [getConnectionsInfo]: Error checking broker with id [{}] name [{}] in environment [{}] of product [{}], Error: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), e.getMessage());
            }

            return connectionsInfo;
        }

        private void fireAlert(BrokerAlertConfig alertConfig, Broker broker)
        {
            log.debug("[OverflowedBrokerChecker] -> [fireAlert]: Alert is going to be fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has overflowed connections",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            try
            {
                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_OVERFLOWED_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(FIFTEEN_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASCommonBrokerAlertServiceDTO overflowedAlertDTO = new ASCommonBrokerAlertServiceDTO();
                overflowedAlertDTO.setProductId(broker.getProduct().getId());
                overflowedAlertDTO.setUuaa(broker.getProduct().getUuaa());
                overflowedAlertDTO.setBrokerId(broker.getId());
                overflowedAlertDTO.setBrokerName(broker.getName());
                overflowedAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                overflowedAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                overflowedAlertDTO.setOpeningPeriod(alertConfig.getOpeningPeriod());

                alertDTO.setOverflowedBrokerAlertServiceDTO(overflowedAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);

                log.debug("[OverflowedBrokerChecker] -> [fireAlert]: Alert fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has overflowed connections",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

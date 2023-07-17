package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASQueueBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueInfoDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
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
 * Scheduler that checks if there is a broker with any queue with number of enqueued messages higher than the configured threshold.
 */
@Slf4j
@Component
public class QueueLengthAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final QueueLengthChecker queueLengthChecker;

    @Autowired
    public QueueLengthAlertScheduler(BrokerRepository brokerRepository, QueueLengthChecker queueLengthChecker)
    {
        this.brokerRepository = brokerRepository;
        this.queueLengthChecker = queueLengthChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.queueLengthBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.queueLengthBrokerAlert.initialDelay:55000}")
    public void checkQueueLength()
    {
        log.info("[QueueAlertScheduler] -> [checkQueueLength]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            queueLengthChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class QueueLengthChecker
    {
        private final BrokerAgentApiClient brokerAgentClient;
        private final BrokerValidatorImpl brokerValidator;
        private final BrokerBuilderImpl brokerBuilder;
        private final IAlertServiceApiClient alertServiceClient;

        private static final int ANY_VALID_AGE = 1; // 1 minute
        public static final int THIRTY_MINUTES_IN_MILLISECONDS = 1800000;

        @Autowired
        public QueueLengthChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
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

            log.debug("[QueueLengthChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check if alert is enabled
            QueueBrokerAlertConfig alertConfig = brokerValidator.getAndValidateQueueAlertConfig(broker, QueueBrokerAlertType.LENGTH_ABOVE);
            if (!alertConfig.isActive())
            {
                log.debug("[QueueLengthChecker] -> [checkBroker]: No alerts are active, skipping");
                return;
            }

            // Check alert configuration is valid
            if (!alertConfig.hasValidConfig())
            {
                log.debug("[QueueLengthChecker] -> [checkBroker]: Alert configuration is not valid, skipping");
                return;
            }

            // Get threshold from configuration
            Integer messageThreshold = alertConfig.getThresholdQueueLength();

            // Get queues info from broker
            QueueInfoDTO[] queuesInfoDTO = this.getQueuesInfo(broker);
            if (queuesInfoDTO == null)
            {
                log.debug("[QueueLengthChecker] -> [checkBroker]: Broker not accessible, skipping");
                return;
            }

            // Check if any queue has more enqueued messages than threshold
            List<String> queuesOverMessageThreshold = Arrays.stream(queuesInfoDTO)
                    .filter(queueInfoDTO -> queueInfoDTO.getQueuedMessages() > messageThreshold)
                    .map(QueueInfoDTO::getName)
                    .collect(Collectors.toList());

            if (!queuesOverMessageThreshold.isEmpty())
            {
                this.fireAlert(alertConfig, broker, queuesOverMessageThreshold);
            }

            log.debug("[QueueLengthChecker] -> [checkBroker]: Checked broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
        }

        private QueueInfoDTO[] getQueuesInfo(Broker broker)
        {
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
            ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, broker.getNodes());

            QueueInfoDTO[] queuesInfoDTO = null;
            try
            {
                queuesInfoDTO = brokerAgentClient.getQueuesInfo(broker.getEnvironment(), connectionDTO, ANY_VALID_AGE);
            }
            catch (NovaException e)
            {
                log.debug("[QueueLengthChecker] -> [getQueuesInfo]: Error checking broker with id [{}] name [{}] in environment [{}] of product [{}], Error: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), e.getMessage());
            }
            return queuesInfoDTO;
        }

        private void fireAlert(QueueBrokerAlertConfig alertConfig, Broker broker, List<String> queuesOverMessageThreshold)
        {
            log.debug("[QueueLengthChecker] -> [fireAlert]: Alert is going to be fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following queues "
                            + "with number of message above the threshold [{} messages]: [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdQueueLength(), queuesOverMessageThreshold);
            try
            {
                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_QUEUE_THRESHOLD_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(THIRTY_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASQueueBrokerAlertServiceDTO queueAlertDTO = new ASQueueBrokerAlertServiceDTO();
                queueAlertDTO.setProductId(broker.getProduct().getId());
                queueAlertDTO.setUuaa(broker.getProduct().getUuaa());
                queueAlertDTO.setBrokerId(broker.getId());
                queueAlertDTO.setBrokerName(broker.getName());
                queueAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                queueAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                queueAlertDTO.setThresholdQueueLength(alertConfig.getThresholdQueueLength());
                queueAlertDTO.setQueuesNames(queuesOverMessageThreshold.toArray(new String[0]));

                alertDTO.setQueueBrokerAlertServiceDTO(queueAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);

                log.debug("[QueueLengthChecker] -> [fireAlert]: Alert fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following queues "
                                + "with number of message above the threshold [{} messages]: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdQueueLength(), queuesOverMessageThreshold);
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

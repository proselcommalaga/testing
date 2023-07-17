package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASConsumerRateBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueInfoDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerMonitoringAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl.BrokerBuilderImpl;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl.BrokerValidatorImpl;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerUtils;
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
 * Scheduler that checks if there is any broker that has any queue with a consumer rate lower than the configured threshold.
 */
@Slf4j
@Component
public class ConsumerRateAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final ConsumerRateChecker consumerRateChecker;

    @Autowired
    public ConsumerRateAlertScheduler(BrokerRepository brokerRepository, ConsumerRateChecker consumerRateChecker)
    {
        this.brokerRepository = brokerRepository;
        this.consumerRateChecker = consumerRateChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.consumerRateBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.consumerRateBrokerAlert.initialDelay:65000}")
    public void checkConsumerRateInBrokers()
    {
        log.info("[ConsumerRateAlertScheduler] -> [checkConsumerRateInBrokers]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            consumerRateChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class ConsumerRateChecker
    {
        private final BrokerAgentApiClient brokerAgentClient;
        private final BrokerValidatorImpl brokerValidator;
        private final BrokerBuilderImpl brokerBuilder;
        private final IAlertServiceApiClient alertServiceClient;

        private static final int ONE_MINUTE_IN_SECONDS = 60;
        public static final int THIRTY_MINUTES_IN_MILLISECONDS = 1800000;

        @Autowired
        public ConsumerRateChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
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

            log.debug("[ConsumerRateChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check alert is enabled
            RateThresholdBrokerAlertConfig alertConfig = brokerValidator.getAndValidateRateThresholdAlertConfig(broker, RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW);
            if (!alertConfig.isActive())
            {
                log.debug("[ConsumerRateChecker] -> [checkBroker]: Alert is not active, skipping");
                return;
            }

            // Check alert configuration is valid
            if (!alertConfig.hasValidConfig())
            {
                log.debug("[ConsumerRateChecker] -> [checkBroker]: Alert configuration is not valid, skipping");
                return;
            }

            // Get queues info from broker
            Integer age = alertConfig.getCalculationTimeInterval() / ONE_MINUTE_IN_SECONDS; // In minutes

            QueueInfoDTO[] queuesInfoDTO = this.getQueuesInfo(broker, age);
            if (queuesInfoDTO == null)
            {
                log.debug("[ConsumerRateChecker] -> [checkBroker]: Broker not accessible, skipping");
                return;
            }

            // Check if any queue has a consumer rate below the threshold
            Double rateThreshold = alertConfig.getThresholdRate(); // In messages per minute

            List<String> queuesBelowRateThreshold = Arrays.stream(queuesInfoDTO)
                    .filter(queueInfoDTO -> BrokerUtils.getRateInMessagesPerMinute(queueInfoDTO.getConsumerACKRate()) < rateThreshold)
                    .map(QueueInfoDTO::getName)
                    .collect(Collectors.toList());

            if (!queuesBelowRateThreshold.isEmpty())
            {
                this.fireAlert(alertConfig, broker, queuesBelowRateThreshold);
            }

            log.debug("[ConsumerRateChecker] -> [checkBroker]: Checked broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
        }

        private QueueInfoDTO[] getQueuesInfo(Broker broker, Integer age)
        {
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
            ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, broker.getNodes());

            QueueInfoDTO[] queuesInfoDTO = null;
            try
            {
                queuesInfoDTO = brokerAgentClient.getQueuesInfo(broker.getEnvironment(), connectionDTO, age);
            }
            catch (NovaException e)
            {
                log.debug("[ConsumerRateChecker] -> [getQueuesInfo]: Error checking broker with id [{}] name [{}] in environment [{}] of product [{}], Error: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), e.getMessage());
            }
            return queuesInfoDTO;
        }

        private void fireAlert(RateThresholdBrokerAlertConfig alertConfig, Broker broker, List<String> queuesBelowRateThreshold)
        {
            log.debug("[ConsumerRateChecker] -> [fireAlert]: Alert is going to be fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following queues "
                            + "are being consumed below threshold [{} messages/min] during last [{} minutes]: [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdRate(), alertConfig.getCalculationTimeInterval(), queuesBelowRateThreshold);

            try
            {
                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_CONSUMER_RATE_THRESHOLD_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(THIRTY_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASConsumerRateBrokerAlertServiceDTO consumerAlertDTO = new ASConsumerRateBrokerAlertServiceDTO();
                consumerAlertDTO.setProductId(broker.getProduct().getId());
                consumerAlertDTO.setUuaa(broker.getProduct().getUuaa());
                consumerAlertDTO.setBrokerId(broker.getId());
                consumerAlertDTO.setBrokerName(broker.getName());
                consumerAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                consumerAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                consumerAlertDTO.setThresholdRate(alertConfig.getThresholdRate());
                consumerAlertDTO.setCalculationTimeInterval(alertConfig.getCalculationTimeInterval());
                consumerAlertDTO.setQueuesNames(queuesBelowRateThreshold.toArray(new String[0]));

                alertDTO.setConsumerRateBrokerAlertServiceDTO(consumerAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);

                log.debug("[ConsumerRateChecker] -> [fireAlert]: Alert fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following queues "
                                + "are being consumed below threshold [{} messages/min] during last [{} minutes]: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdRate(), alertConfig.getCalculationTimeInterval(), queuesBelowRateThreshold);
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

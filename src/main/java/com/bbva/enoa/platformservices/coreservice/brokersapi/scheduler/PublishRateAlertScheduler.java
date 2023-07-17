package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASPublishRateBrokerAlertServiceDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.ExchangeInfoDTO;
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
 * Scheduler that checks if there is any broker that has any exchange with a publishing rate higher than the configured threshold.
 */
@Slf4j
@Component
public class PublishRateAlertScheduler
{
    private final BrokerRepository brokerRepository;
    private final PublishRateChecker publishRateChecker;

    @Autowired
    public PublishRateAlertScheduler(BrokerRepository brokerRepository, PublishRateChecker publishRateChecker)
    {
        this.brokerRepository = brokerRepository;
        this.publishRateChecker = publishRateChecker;
    }

    @Scheduled(fixedDelayString = "${nova.scheduledTasks.publishRateBrokerAlert.fixedRate:60000}", initialDelayString = "${nova.scheduledTask.publishRateBrokerAlert.initialDelay:60000}")
    public void checkPublishRateInBrokers()
    {
        log.info("[PublishRateAlertScheduler] -> [checkPublishRateInBrokers]: starting scheduler ...");

        List<Broker> runningBrokers = brokerRepository.findByStatus(BrokerStatus.RUNNING);

        for (Broker broker : runningBrokers)
        {
            publishRateChecker.checkBroker(broker.getId());
        }
    }

    @Slf4j
    @Component
    static class PublishRateChecker
    {
        private final BrokerAgentApiClient brokerAgentClient;
        private final BrokerValidatorImpl brokerValidator;
        private final BrokerBuilderImpl brokerBuilder;
        private final IAlertServiceApiClient alertServiceClient;

        private static final int ONE_MINUTE_IN_SECONDS = 60;
        public static final int THIRTY_MINUTES_IN_MILLISECONDS = 1800000;

        @Autowired
        public PublishRateChecker(BrokerAgentApiClient brokerAgentClient, BrokerValidatorImpl brokerValidator,
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

            log.debug("[PublishRateChecker] -> [checkBroker]: Checking broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());

            // Check alert is enabled
            RateThresholdBrokerAlertConfig alertConfig = brokerValidator.getAndValidateRateThresholdAlertConfig(broker, RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE);
            if (!alertConfig.isActive())
            {
                log.debug("[PublishRateChecker] -> [checkBroker]: Alert is not active, skipping");
                return;
            }

            // Check alert configuration is valid
            if (!alertConfig.hasValidConfig())
            {
                log.debug("[PublishRateChecker] -> [checkBroker]: Alert configuration is not valid, skipping");
                return;
            }

            // Get exchanges info
            Integer age = alertConfig.getCalculationTimeInterval() / ONE_MINUTE_IN_SECONDS; // In minutes

            ExchangeInfoDTO[] exchangesInfo = this.getExchangesInfo(broker, age);
            if (exchangesInfo == null)
            {
                log.debug("[PublishRateChecker] -> [checkBroker]: Broker not accessible, skipping");
                return;
            }

            // Check if any exchange has a publishing rate above the threshold
            Double rateThreshold = alertConfig.getThresholdRate(); //In messages per minute

            List<String> exchangesOverRateThreshold = Arrays.stream(exchangesInfo)
                    .filter(exchangeInfoDTO -> BrokerUtils.getRateInMessagesPerMinute(exchangeInfoDTO.getMessagesInRate()) > rateThreshold)
                    .map(ExchangeInfoDTO::getName)
                    .collect(Collectors.toList());

            if (!exchangesOverRateThreshold.isEmpty())
            {
                this.fireAlert(alertConfig, broker, exchangesOverRateThreshold);
            }

            log.debug("[PublishRateChecker] -> [checkBroker]: Checked broker with id [{}] name [{}] in environment [{}] of product [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa());
        }

        private ExchangeInfoDTO[] getExchangesInfo(Broker broker, Integer age)
        {
            BrokerUser adminUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
            ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(adminUser, broker.getNodes());

            ExchangeInfoDTO[] exchangesInfoDTO = null;
            try
            {
                exchangesInfoDTO = brokerAgentClient.getExchangesInfo(broker.getEnvironment(), connectionDTO, age);
            }
            catch (NovaException e)
            {
                log.debug("[PublishRateChecker] -> [getExchangesInfo]: Error checking broker with id [{}] name [{}] in environment [{}] of product [{}], Error: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), e.getMessage());
            }

            return exchangesInfoDTO;
        }

        private void fireAlert(RateThresholdBrokerAlertConfig alertConfig, Broker broker, List<String> exchangesOverRateThreshold)
        {
            log.debug("[PublishRateChecker] -> [fireAlert]: Alert is going to be fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following exchanges publishing " +
                            "over threshold [{} messages/min] during last [{} minutes]: [{}]",
                    broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdRate(), alertConfig.getCalculationTimeInterval(), exchangesOverRateThreshold);

            try
            {
                ASAlertDTO alertDTO = new ASAlertDTO();
                alertDTO.setAlertCode(BrokerMonitoringAlertType.APP_PUBLISH_RATE_THRESHOLD_BROKER.getAlertCode());
                alertDTO.setEnvironment(broker.getEnvironment());
                alertDTO.setAlertLifePeriod(THIRTY_MINUTES_IN_MILLISECONDS);
                alertDTO.setGenerateEmail(alertConfig.isSendMail());
                alertDTO.setGeneratePatrol(alertConfig.isSendPatrol());

                ASPublishRateBrokerAlertServiceDTO publishAlertDTO = new ASPublishRateBrokerAlertServiceDTO();
                publishAlertDTO.setProductId(broker.getProduct().getId());
                publishAlertDTO.setUuaa(broker.getProduct().getUuaa());
                publishAlertDTO.setBrokerId(broker.getId());
                publishAlertDTO.setBrokerName(broker.getName());
                publishAlertDTO.setTimeBetweenNotifications(alertConfig.getTimeBetweenNotifications());
                publishAlertDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(alertConfig.getEmailAddresses()));
                publishAlertDTO.setThresholdRate(alertConfig.getThresholdRate());
                publishAlertDTO.setCalculationTimeInterval(alertConfig.getCalculationTimeInterval());
                publishAlertDTO.setExchangesNames(exchangesOverRateThreshold.toArray(new String[0]));

                alertDTO.setPublishRateBrokerAlertServiceDTO(publishAlertDTO);

                this.alertServiceClient.registerGenericAlert(alertDTO);

                log.debug("[PublishRateChecker] -> [fireAlert]: Alert fired! Broker with id [{}] name [{}] in environment [{}] of product [{}] has the following exchanges publishing " +
                                "over threshold [{} messages/min] during last [{} minutes]: [{}]",
                        broker.getId(), broker.getName(), broker.getEnvironment(), broker.getProduct().getUuaa(), alertConfig.getThresholdRate(), alertConfig.getCalculationTimeInterval(), exchangesOverRateThreshold);
            }
            catch (NovaException e)
            {
                // Ignore and do not generate INTERNAL_ERROR
            }
        }
    }
}

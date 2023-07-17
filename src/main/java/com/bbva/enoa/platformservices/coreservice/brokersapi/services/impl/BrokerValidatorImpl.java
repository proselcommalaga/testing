package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.*;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.BrokerInfo;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerNodeOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.model.BrokerValidatedObjects;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerUtils;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.MailUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Broker validator service
 */
@Service
public class BrokerValidatorImpl implements IBrokerValidator
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerValidatorImpl.class);

    /**
     * Class name
     */
    private static final String CLASS_NAME = "BrokerValidatorImpl";

    /**
     * Max. number of brokers allowed for a product in an environment
     */
    @Value("${nova.brokers.maxBrokersByEnvironmentLimit:1}")
    private int maxBrokersByEnvAndProductLimit;

    /**
     * JPA repository for Product
     */
    private final ProductRepository productRepository;

    /**
     * JPA repository for Filesystem
     */
    private final FilesystemRepository filesystemRepository;

    /**
     * JPA repository for BrokerPack
     */
    private final BrokerPackRepository brokerPackRepository;

    /**
     * ProductBudgets service
     */
    private final IProductBudgetsService budgetsService;

    /**
     * JPA repository for Broker
     */
    private final BrokerRepository brokerRepository;

    /**
     * JPA repository for BrokerNode
     */
    private final BrokerNodeRepository brokerNodeRepository;

    /**
     *  Validations Util
     */
    private final ManageValidationUtils manageValidationUtils;



    /**
     * Instantiates a new Broker validator.
     *
     * @param productRepository      the product repository
     * @param filesystemRepository   the filesystem repository
     * @param brokerPackRepository   the broker hardware pack repository
     * @param budgetsService         the budgets service
     * @param brokerRepository       the broker repository
     * @param brokerNodeRepository   the broker node repository
     * @param manageValidationUtils  the manage validations utils
     */
    @Autowired
    public BrokerValidatorImpl(final ProductRepository productRepository,
                               final FilesystemRepository filesystemRepository,
                               final IProductBudgetsService budgetsService,
                               final BrokerRepository brokerRepository,
                               final BrokerNodeRepository brokerNodeRepository,
                               final BrokerPackRepository brokerPackRepository,
                               final ManageValidationUtils manageValidationUtils)
    {
        this.productRepository = productRepository;
        this.filesystemRepository = filesystemRepository;
        this.budgetsService = budgetsService;
        this.brokerRepository = brokerRepository;
        this.brokerNodeRepository = brokerNodeRepository;
        this.manageValidationUtils = manageValidationUtils;
        this.brokerPackRepository = brokerPackRepository;
    }

    @Override
    public Broker validateAndGetBroker(final Integer brokerId)
    {
        return this.brokerRepository.findById(brokerId).orElseThrow(() -> {
            throw new NovaException(BrokerError.getBrokerNotFoundError(brokerId));
        });
    }

    @Override
    public BrokerNode validateAndGetBrokerNode(final Integer brokerNodeId)
    {
        return this.brokerNodeRepository.findById(brokerNodeId).orElseThrow(() -> {
            throw new NovaException(BrokerError.getBrokerNodeNotFoundError(brokerNodeId));
        });
    }


    @Override
    public BrokerOperation validateAndGetBrokerOperation(final String brokerOperation)
    {
        if (!EnumUtils.isValidEnum(BrokerOperation.class, brokerOperation))
        {
            throw new NovaException(BrokerError.getBrokerOperationNotValidError(brokerOperation));
        }
        return BrokerOperation.valueOf(brokerOperation);
    }

    @Override
    public BrokerNodeOperation validateAndGetBrokerNodeOperation(final String brokerNodeOperation)
    {
        if (!EnumUtils.isValidEnum(BrokerNodeOperation.class, brokerNodeOperation))
        {
            throw new NovaException(BrokerError.getBrokerNodeOperationNotValidError(brokerNodeOperation));
        }
        return BrokerNodeOperation.valueOf(brokerNodeOperation);
    }

    @Override
    public BrokerAlertConfig getAndValidateGenericAlertConfig(final Broker broker, final GenericBrokerAlertType alertType)
    {
        List<BrokerAlertConfig> alertConfigs = broker.getGenericAlertConfigs().stream()
                .filter(alertConfig -> alertConfig.getType() == alertType)
                .collect(Collectors.toList());

        if (alertConfigs.size() != 1)
        {
            throw new NovaException(BrokerError.getAlertConfigOccurrencesNotValidError(broker.getId(), alertType.name()));
        }

        return alertConfigs.get(0);
    }

    @Override
    public QueueBrokerAlertConfig getAndValidateQueueAlertConfig(final Broker broker, final QueueBrokerAlertType alertType)
    {
        List<QueueBrokerAlertConfig> alertConfigs = broker.getQueueAlertConfigs().stream()
                .filter(alertConfig -> alertConfig.getType() == alertType)
                .collect(Collectors.toList());

        if (alertConfigs.size() != 1)
        {
            throw new NovaException(BrokerError.getAlertConfigOccurrencesNotValidError(broker.getId(), alertType.name()));
        }

        return alertConfigs.get(0);
    }

    @Override
    public RateThresholdBrokerAlertConfig getAndValidateRateThresholdAlertConfig(final Broker broker, final RateThresholdBrokerAlertType alertType)
    {
        List<RateThresholdBrokerAlertConfig> alertConfigs = broker.getRateAlertConfigs().stream()
                .filter(alertConfig -> alertConfig.getType() == alertType)
                .collect(Collectors.toList());

        if (alertConfigs.size() != 1)
        {
            throw new NovaException(BrokerError.getAlertConfigOccurrencesNotValidError(broker.getId(), alertType.name()));
        }

        return alertConfigs.get(0);
    }

    @Override
    public BrokerValidatedObjects validateBrokerDTO(final BrokerDTO brokerDTO)
    {
        validateBrokerType(brokerDTO);
        validatePlatform(brokerDTO);
        brokerNameValidations(brokerDTO);
        Product product = validateAndGetProduct(brokerDTO.getProductId());
        environmentValidations(brokerDTO, product);
        budgetsValidations(brokerDTO);
        Filesystem filesystem = fileSystemValidations(brokerDTO);
        BrokerPack hardwarePack = hardwarePackValidations(brokerDTO);

        return new BrokerValidatedObjects(product, filesystem, hardwarePack);
    }

    @Override
    public Product validateAndGetProduct(final Integer productId)
    {
        return this.productRepository.findById(productId).orElseThrow(() ->
        {
            throw new NovaException(BrokerError.getProductNotFoundError(productId));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void validateBrokerCanBeStopped(final Broker broker)
    {
        validateBrokerCurrentStatusIsRunning(broker);
        validateAnyNodeIsNotInTransitoryStatus(broker);
    }

    @Override
    public void validateBrokerCanBeDeleted(final Broker broker)
    {
        validateBrokerCurrentStatusForDeletion(broker);
        validateBrokerNotUsedByAnyDeploymentServices(broker);
    }

    @Override
    public void validateBrokerCanBeStarted(final Broker broker)
    {
        validateBrokerCurrentStatusIsStopped(broker);
        validateAnyNodeIsNotInTransitoryStatus(broker);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateBrokerNodeCanBeStopped(final BrokerNode brokerNode)
    {
        validateBrokerNodeCurrentStatusIsRunning(brokerNode);
        validateBrokerIsCreated(brokerNode.getBroker());
    }

    @Override
    public void validateBrokerNodeCanBeStarted(final BrokerNode brokerNode)
    {
        validateBrokerNodeCurrentStatusIsStopped(brokerNode);
        validateBrokerIsCreated(brokerNode.getBroker());
    }

    @Override
    public void validateAnyNodeIsNotInTransitoryStatus(final Broker broker)
    {
        boolean isAnyNodeInTransitoryStatus = broker.getNodes().stream()
                .anyMatch(node -> node.getStatus().isTransitory());

        if (isAnyNodeInTransitoryStatus)
        {
            throw new NovaException(BrokerError.getBrokerNodeInTransitoryStatusError());
        }
    }

    @Override
    public int[] getValidNumberOfNodes(Environment environment, boolean isMonoCPD)
    {
        switch (environment)
        {
            case INT:
                return isMonoCPD ? new int[]{1, 2} : new int[]{1, 2, 3};
            case PRE:
            case PRO:
                return isMonoCPD ? new int[]{1, 2} : new int[]{2, 3};
            default:
                return new int[0];
        }
    }

    @Override
    public void validateBrokerCanBeOperable(final Broker broker)
    {
        if (BrokerStatus.RUNNING != broker.getStatus())
        {
            throw new NovaException(BrokerError.getCantOperateOverBrokerError());
        }
    }

    @Override
    public void validateBrokerAlertConfig(final BrokerAlertConfigDTO brokerAlertConfigDTO, final Broker broker)
    {
        // Validate emails in list have valid format
        if (!MailUtils.checkEmailValid(brokerAlertConfigDTO.getEmailAddresses()))
        {
            throw new NovaException(BrokerError.generateInstanceNoValidEmail(), "There is, at least, one invalid email in the list");
        }

        // Validate queue threshold
        if (brokerAlertConfigDTO.getQueueLengthAlertConfig() != null
                && brokerAlertConfigDTO.getQueueLengthAlertConfig().getIsActive()
                && (brokerAlertConfigDTO.getQueueLengthAlertConfig().getThresholdQueueLength() == null
                || brokerAlertConfigDTO.getQueueLengthAlertConfig().getThresholdQueueLength() <= 0))
        {
            throw new NovaException(BrokerError.generateInvalidQueueThreshold());
        }

        // Validate publish rate threshold
        if (brokerAlertConfigDTO.getPublishRateAlertConfig() != null
                && brokerAlertConfigDTO.getPublishRateAlertConfig().getIsActive()
                && (brokerAlertConfigDTO.getPublishRateAlertConfig().getThresholdRate() == null
                || Double.compare(brokerAlertConfigDTO.getPublishRateAlertConfig().getThresholdRate(), 0) < 0))
        {
            throw new NovaException(BrokerError.generateInvalidPublishRateThreshold());
        }

        // Validate consumer rate threshold
        if (brokerAlertConfigDTO.getConsumerRateAlertConfig() != null
                && brokerAlertConfigDTO.getConsumerRateAlertConfig().getIsActive()
                && (brokerAlertConfigDTO.getConsumerRateAlertConfig().getThresholdRate() == null
                ||Double.compare(brokerAlertConfigDTO.getConsumerRateAlertConfig().getThresholdRate(), 0) <= 0))
        {
            throw new NovaException(BrokerError.generateInvalidConsumerRateThreshold());
        }

        // Validate product has remedy group if Patrol is being activated
        if (tryToActivatePatrol(brokerAlertConfigDTO) && !productHasRemedyGroup(broker.getProduct()))
        {
            throw new NovaException(BrokerError.generateProductWithoutRemedyGroupError());
        }
    }

    private boolean productHasRemedyGroup(Product product)
    {
        return product.getRemedySupportGroup() != null && !product.getRemedySupportGroup().isEmpty();
    }

    private boolean tryToActivatePatrol(BrokerAlertConfigDTO brokerAlertConfigDTO)
    {
        GenericBrokerAlertConfigDTO healthAlertConfig = brokerAlertConfigDTO.getBrokerHealthAlertConfig();
        if (healthAlertConfig != null && healthAlertConfig.getIsActive() && healthAlertConfig.getSendPatrol())
        {
            return true;
        }

        GenericBrokerAlertConfigDTO nodeAlertConfig = brokerAlertConfigDTO.getUnavailableNodeAlertConfig();
        if (nodeAlertConfig != null && nodeAlertConfig.getIsActive() && nodeAlertConfig.getSendPatrol())
        {
            return true;
        }

        GenericBrokerAlertConfigDTO overflowedAlertConfig = brokerAlertConfigDTO.getOverflowedBrokerAlertConfig();
        if (overflowedAlertConfig != null && overflowedAlertConfig.getIsActive() && overflowedAlertConfig.getSendPatrol())
        {
            return true;
        }

        QueueBrokerAlertConfigDTO queueAlertConfig = brokerAlertConfigDTO.getQueueLengthAlertConfig();
        if (queueAlertConfig != null && queueAlertConfig.getIsActive() && queueAlertConfig.getSendPatrol())
        {
            return true;
        }

        RateThresholdBrokerAlertConfigDTO publishRateAlertConfig = brokerAlertConfigDTO.getPublishRateAlertConfig();
        if (publishRateAlertConfig != null && publishRateAlertConfig.getIsActive() && publishRateAlertConfig.getSendPatrol())
        {
            return true;
        }

        RateThresholdBrokerAlertConfigDTO consumerRateAlertConfig = brokerAlertConfigDTO.getConsumerRateAlertConfig();
        if (consumerRateAlertConfig != null && consumerRateAlertConfig.getIsActive() && consumerRateAlertConfig.getSendPatrol())
        {
            return true;
        }

        return false;
    }

    public BrokerUser validateAndGetBrokerAdminUser(final Broker broker)
    {
        return broker.getUsers().stream().filter(bu -> BrokerRole.ADMIN.equals(bu.getRole())).findFirst().orElseThrow(() -> new NovaException(BrokerError.getUserAdminNotFoundInBroker(broker.getId())));
    }


    private void validateBrokerCurrentStatusIsStopped(final Broker broker)
    {
        if (BrokerStatus.STOPPED != broker.getStatus())
        {
            throw new NovaException(BrokerError.getUnexpectedBrokerStatusError(BrokerStatus.STOPPED.name()));
        }
    }

    private void validateBrokerNotUsedByAnyDeploymentServices(final Broker broker)
    {
        if (!broker.getDeploymentServices().isEmpty())
        {
            throw new NovaException(BrokerError.getBrokerUsedByServicesError(broker.getName()));
        }
    }

    private void validateBrokerCurrentStatusForDeletion(final Broker broker)
    {
        if (BrokerStatus.STOPPED != broker.getStatus()
                && BrokerStatus.CREATE_ERROR != broker.getStatus()
                && BrokerStatus.DELETE_ERROR != broker.getStatus())
        {
            throw new NovaException(BrokerError.getUnexpectedBrokerStatusError(
                    BrokerStatus.STOPPED.name() + ", " + BrokerStatus.CREATE_ERROR.name() + " or " + BrokerStatus.DELETE_ERROR));
        }
    }

    private void validateBrokerCurrentStatusIsRunning(final Broker broker)
    {
        if (BrokerStatus.RUNNING != broker.getStatus())
        {
            throw new NovaException(BrokerError.getUnexpectedBrokerStatusError(BrokerStatus.RUNNING.name()));
        }
    }

    private void validateBrokerNodeCurrentStatusIsRunning(final BrokerNode brokerNode)
    {
        if (BrokerStatus.RUNNING != brokerNode.getStatus())
        {
            throw new NovaException(BrokerError.getUnexpectedBrokerNodeStatusError(BrokerStatus.RUNNING.name()));
        }
    }

    private void validateBrokerIsCreated(final Broker broker)
    {
        if (!broker.getStatus().isCreatedAndStable())
        {
            throw new NovaException(BrokerError.getBrokerNotCreatedAndStableError());
        }
    }


    private void validateBrokerNodeCurrentStatusIsStopped(final BrokerNode brokerNode)
    {
        if (BrokerStatus.STOPPED != brokerNode.getStatus())
        {
            throw new NovaException(BrokerError.getUnexpectedBrokerNodeStatusError(BrokerStatus.STOPPED.name()));
        }
    }

    /**
     * Validate platform and type for a broker
     *
     * @param dto broker DTO
     */
    private void validateBrokerType(final BrokerDTO dto)
    {
        if (BrokerType.PUBLISHER_SUBSCRIBER.getType().equals(dto.getType()))
        {
            return;
        }
        logErrorValidation(dto, "validateBrokerType");
        throw new NovaException(BrokerError.getUnsupportedBrokerTypeError(dto.getType()));
    }

    /**
     * validate Platform
     *
     * @param dto broker DTO
     */
    private void validatePlatform(final BrokerDTO dto)
    {
        if (Platform.NOVA.getName().equals(dto.getPlatform()))
        {
            return;
        }
        logErrorValidation(dto, "validatePlatform");
        throw new NovaException(BrokerError.getUnsupportedBrokerPlatformError(dto.getPlatform()));
    }

    /**
     * Validate broker budgets
     *
     * @param dto broker DTO
     */
    private void budgetsValidations(final BrokerDTO dto)
    {
        // Check enough broker budget
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setEnvironment(dto.getEnvironment());
        brokerInfo.setNumberOfNodes(dto.getNumberOfNodes());
        brokerInfo.setHardwarePackId(dto.getHardwarePackId());
        if (!budgetsService.checkBroker(brokerInfo, dto.getProductId()))
        {
            logErrorValidation(dto, "budgetsValidations");
            throw new NovaException(BrokerError.getNotEnoughBrokerBudgetError(dto.getEnvironment()));
        }
    }

    /**
     * Validate broker name
     *
     * @param dto broker dto
     */
    private void brokerNameValidations(final BrokerDTO dto)
    {
        // Validate allowed characters in broker name
        if (!validateBrokerNameCharacters(dto.getName()))
        {
            logErrorValidation(dto, "brokerNameValidations");
            throw new NovaException(BrokerError.getInvalidBrokerNameError(dto.getName()));
        }
    }

    /**
     * Validate broker hardware pack
     *
     * @param dto brokerDto
     * @return BrokerPack validated
     */
    private BrokerPack hardwarePackValidations(final BrokerDTO dto)
    {
        // Check hardware pack exists
        BrokerPack brokerPack = this.brokerPackRepository.findById(dto.getHardwarePackId()).orElseThrow(() ->
        {
            logErrorValidation(dto, "hardwarePackValidations");
            throw new NovaException(BrokerError.getHardwarePackNotFoundError(dto.getHardwarePackId()));
        });

        if (brokerPack.getHardwarePackType() != BrokerUtils.getHardwarePackTypeForPlatform(Platform.valueOf(dto.getPlatform())))
        {
            logErrorValidation(dto, "hardwarePackValidations");
            throw new NovaException(BrokerError.getInvalidHardwarePackError(dto.getHardwarePackId()));
        }
        return brokerPack;
    }

    /**
     * Validate broker fs
     *
     * @param dto brokerDto
     * @return fs validated
     */
    private Filesystem fileSystemValidations(final BrokerDTO dto)
    {
        // Check filesystem exists
        Filesystem filesystem = this.filesystemRepository.findById(dto.getFilesystemId()).orElseThrow(() ->
        {
            logErrorValidation(dto, "fileSystemValidations");
            throw new NovaException(BrokerError.getFilesystemNotFoundError(dto.getFilesystemId()));
        });

        // Check filesystem product, environment, platform and status
        if (!filesystem.getProduct().getId().equals(dto.getProductId()) ||
                !filesystem.getEnvironment().equals(dto.getEnvironment()) ||
                filesystem.getType() != BrokerUtils.getFilesystemTypeForPlatform(Platform.valueOf(dto.getPlatform())) ||
                FilesystemStatus.CREATED != filesystem.getFilesystemStatus())
        {
            logErrorValidation(dto, "fileSystemValidations");
            throw new NovaException(BrokerError.getInvalidFilesystemError(dto.getFilesystemId()));
        }
        return filesystem;
    }

    /**
     * Validate broker environment
     *
     * @param dto broker dto
     */
    private void environmentValidations(final BrokerDTO dto, final Product product)
    {
        Environment environment = Environment.valueOf(dto.getEnvironment());

        // Check max number of brokers for product and environment
        List<Broker> existingBrokersByEnv = this.brokerRepository.findByProductIdAndEnvironment(dto.getProductId(), environment.getEnvironment());
        if (existingBrokersByEnv.size() >= maxBrokersByEnvAndProductLimit)
        {
            logErrorValidation(dto, "environmentValidations");
            throw new NovaException(BrokerError.getMaxBrokersReachedError());
        }

        // Check unique broker name for product and environment (non case-sensitive)
        if (existingBrokersByEnv.stream().anyMatch(b -> b.getName().equals(dto.getName())))
        {
            logErrorValidation(dto, "environmentValidations");
            throw new NovaException(BrokerError.getGivenBrokerNameAlreadyExistsError(dto.getName()));
        }

        // Check valid number of nodes according to deployment infrastructure in PRO
        boolean isMonoCPD = !product.getMultiCPDInPro();
        int[] validNumberOfNodes = this.getValidNumberOfNodes(environment, isMonoCPD);
        if (!ArrayUtils.contains(validNumberOfNodes, dto.getNumberOfNodes()))
        {
            throw new NovaException((BrokerError.getUnsupportedNumberOfNodesError(dto.getNumberOfNodes())));
        }

        // Validations that are exclusive for production
        this.productionBrokerValidations(dto);
    }

    private void productionBrokerValidations(final BrokerDTO brokerDTO)
    {
        // Next validations are exclusive for production
        if (!Environment.PRO.name().equals(brokerDTO.getEnvironment()))
        {
            return;
        }

        // Must exist a broker in PRE with same name
        Broker analogueBrokerInPre = brokerRepository.findByProductIdAndNameAndEnvironment(brokerDTO.getProductId(), brokerDTO.getName(), Environment.PRE.getEnvironment())
                .orElseThrow(() -> {
                    logErrorValidation(brokerDTO, "productionBrokerValidations");
                    throw new NovaException(BrokerError.getErrorCreationBrokerNoBrokerWithNameOnEnvironment(brokerDTO.getName()));
                });

        // ... and number of nodes must be the same
        if (!Objects.equals(brokerDTO.getNumberOfNodes(), analogueBrokerInPre.getNumberOfNodes()))
        {
            logErrorValidation(brokerDTO, "productionBrokerValidations");
            throw new NovaException(BrokerError.getErrorNotSameNumberOFNodesAsPreproduction());
        }
    }

    private boolean areRestOfNodesNotRunning(BrokerNode brokerNode)
    {
        return brokerNode.getBroker().getNodes().stream()
                .filter(node -> !Objects.equals(node.getId(), brokerNode.getId()))
                .allMatch(node -> node.getStatus() != BrokerStatus.RUNNING);
    }

    private boolean validateBrokerNameCharacters(String name)
    {
        return name.matches(BrokerConstants.BROKER_NAME_VALIDATION_REGEX);
    }

    private void logErrorValidation(final BrokerDTO dto, final String methodName)
    {
        LOG.error("[{}] -> [{}]: Error validating BrokerDTO [{}]", CLASS_NAME, methodName, dto);
    }


    @Override
    public void validateBrokerActionCanBeManagedByUser(Broker broker, String user)
    {
        if(!manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(user, broker))
        {
            throw new NovaException(BrokerError.generateEnvironmentError());
        }
    }

}

package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.exception.LogicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorValidator;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class dedicated to validate operations of the logical connector
 */
@Service
public class LogicalConnectorValidatorImpl implements ILogicalConnectorValidator
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogicalConnectorValidatorImpl.class);

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * Logical Connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Connector type repository
     */
    private final ConnectorTypeRepository connectorTypeRepository;

    /**
     * CPD repository
     */
    private final CPDRepository cpdRepository;

    /**
     * Management action task repository
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;

    /**
     * Manage validation utils
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Nova Context for getting user code
     */
    private final NovaContext novaContext;

    /**
     * Doc system service.
     */
    private final IDocSystemService docSystemService;

    /**
     * Dependency injection constructor
     *
     * @param productRepository              productRepository
     * @param logicalConnectorRepository     logicalConnectorRepository
     * @param connectorTypeRepository        connectorTypeRepository
     * @param cpdRepository                  cpdRepository
     * @param managementActionTaskRepository managementActionTaskRepository
     * @param manageValidationUtils          manageValidationUtils
     * @param novaContext                    novaContext
     * @param docSystemService               Doc system service.
     */
    @Autowired
    public LogicalConnectorValidatorImpl(final ProductRepository productRepository,
                                         final LogicalConnectorRepository logicalConnectorRepository,
                                         final ConnectorTypeRepository connectorTypeRepository,
                                         final CPDRepository cpdRepository,
                                         final ManagementActionTaskRepository managementActionTaskRepository,
                                         final ManageValidationUtils manageValidationUtils,
                                         final NovaContext novaContext,
                                         final IDocSystemService docSystemService
                                         )
    {
        this.productRepository = productRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.connectorTypeRepository = connectorTypeRepository;
        this.cpdRepository = cpdRepository;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.manageValidationUtils = manageValidationUtils;
        this.novaContext = novaContext;
        this.docSystemService = docSystemService;
    }

    ///////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////

    @Override
    @Transactional(readOnly = true)
    public Product validateAndGetProduct(int productId)
    {
        Product product = this.productRepository.findById(productId).orElse(null);

        if (product == null)
        {
            String message = "[LogicalConnectorAPI] -> [validateAndGetProduct]: the product ID [" + productId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getNoSuchProductError(), message);
        }

        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public LogicalConnector validateAndGetLogicalConnector(int logicalConnectorId)
    {
        LogicalConnector logicalConnector = this.logicalConnectorRepository.findById(logicalConnectorId).orElse(null);

        if (logicalConnector == null)
        {
            String message = "[LogicalConnectorAPI] -> [validateAndGetLogicalConnector]: the logicalConnector ID: [" + logicalConnectorId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getNoSuchLogicalConnectorError(), message);
        }

        return logicalConnector;
    }

    @Override
    public void validateLogicalConnectorCreatedStatus(final LogicalConnector logicalConnector)
    {
        if (logicalConnector.getLogicalConnectorStatus() != LogicalConnectorStatus.CREATED)
        {
            String message = "[LogicalConnectorAPI] -> [validateLogicalConnectorCreatedStatus]: the logicalConnector: [" + logicalConnector.getName() + "] is not on CREATED status." +
                    "Opertion not allowed.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getLogicalConnectorNotCreatedError(), message);
        }

        if (logicalConnector.getPhysicalConnector() == null)
        {
            String message = "[LogicalConnectorAPI] -> [validateLogicalConnectorCreatedStatus]: the logicalConnector: [" + logicalConnector.getName() + "] is not associated to any physical connector.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getLogicalConnectorNotCreatedError(), message);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String validateLogicalConnectorName(final String logicalConnectorNameDTO, final String environment, final Integer productId)
    {
        LogicalConnector logicalConnector = this.logicalConnectorRepository.findByProductIdAndEnvironmentAndName(productId, environment, logicalConnectorNameDTO);

        // Check logical connector instance
        if (logicalConnector == null)
        {
            LOG.debug("[{}] -> [validateLogicalConnectorName]: the logical connector name: [{}] has been validated. Results: OK.", Constants.LOGICAL_CONNECTOR_API, logicalConnectorNameDTO);
        }
        else
        {
            // The logical connector with this name is already exist
            String message = "[LogicalConnectorAPI] -> [validateLogicalConnectorName]. The name of the logical connector: [" + logicalConnectorNameDTO + "] is already exists into BBDD.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getDuplicatedLogicalConnectorNameError(), message);
        }

        return logicalConnectorNameDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectorType validateConnectorType(final String connectorTypeName)
    {
        ConnectorType connectorType = this.connectorTypeRepository.findByName(connectorTypeName);

        if (connectorType == null)
        {
            String message = "[LogicalConnectorAPI] -> [validateConnectorType]: the connector type name: [" + connectorTypeName + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getNoSuchConnectorTypeError(), message);
        }
        else
        {
            LOG.debug("[{}] -> [validateConnectorType]: the connector type: [{}] has been validated. Results OK.", Constants.LOGICAL_CONNECTOR_API, connectorTypeName);
        }

        return connectorType;
    }

    @Override
    public void canRequestCheckPropertiesTask(final LogicalConnectorStatus logicalConnectorStatus)
    {
        if (logicalConnectorStatus == LogicalConnectorStatus.CREATED)
        {
            LOG.debug("[{}] -> [canRequestCheckPropertiesTask]: the request check properties to do task can be requested. " +
                    "Logical connector status: [{}] OK.", Constants.LOGICAL_CONNECTOR_API, logicalConnectorStatus.name());
        }
        else
        {
            String message = "[LogicalConnectorAPI] -> [canRequestCheckPropertiesTask]: cannot request a check properties to do task due to logical connector status " +
                    "is not CREATED status. Current logical connector status: [" + logicalConnectorStatus.name() + "]";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getCannotRequestPropertiesError(), message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void checkPendingTodoTaskByLogicalConnector(final LogicalConnector logicalConnector)
    {
        List<ManagementActionTask> creationConnectorTaskList = this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                ToDoTaskType.CREATION_LOGICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.debug("[{}] -> [checkPendingCheckPropertyTodoTask]: the creation connector todo task list associated to logical connector: [{}]-ENV:[{}]-Product:[{}] are: [{}]",
                Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName(), creationConnectorTaskList);

        List<ManagementActionTask> checkConnectorPropertiesTaskList = this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.debug("[{}] -> [checkPendingCheckPropertyTodoTask]: the check properties todo task list associated to logical connector: [{}]-ENV:[{}]-Product:[{}] are: [{}]",
                Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName(), checkConnectorPropertiesTaskList);

        if (creationConnectorTaskList.isEmpty() && checkConnectorPropertiesTaskList.isEmpty())
        {
            LOG.debug("[{}] -> [checkPendingCheckPropertyTodoTask]: logical connector [{}]:ENV:[{}]:Product:[{}] - does not have to do task associated in 'PENDING' or PENDING_ERROR status.",
                    Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName());
        }
        else
        {
            String message = "[LogicalConnectorAPI] -> [checkPendingTodoTaskByLogicalConnector]: the logical Connector: [" + logicalConnector.getName() + "] has PENDING or PENDING_ERROR to do task and can" +
                    " not be deleted/archived.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getHasToDoTaskPendingError(), message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer checkPendingPropertyTask(final LogicalConnector logicalConnector)
    {
        Integer toDoTaskId = null;

        List<ManagementActionTask> checkConnectorPropertiesTaskList = this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.trace("[{}] -> [checkPendingPropertyTask]: the check properties todo task list associated to logical connector: [{}]-ENV:[{}]-Product:[{}] are: [{}]",
                Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName(), checkConnectorPropertiesTaskList);

        if (checkConnectorPropertiesTaskList.isEmpty())
        {
            LOG.debug("[{}] -> [checkPendingPropertyTask]: the logical connector [{}]:ENV:[{}]:Product:[{}] - does not have 'check logical connector properties'" +
                    "to do task associated in 'PENDING' or 'PENDING_ERROR' status.", Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName());
        }
        else
        {
            // Must only have one to do task of type management action task = check logical connector properties
            toDoTaskId = checkConnectorPropertiesTaskList.get(0).getId();
            LOG.debug("[{}] -> [checkPendingPropertyTask]: the logical connector [{}]:ENV:[{}]:Product:[{}] - has 'check logical connector properties'" +
                            "to do task associated in 'PENDING' or 'PENDING_ERROR' status. TodoTaskId: [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(),
                    logicalConnector.getProduct().getName(), toDoTaskId);
        }

        return toDoTaskId;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLogicalConnectorFrozen(final LogicalConnector logicalConnector)
    {
        final AtomicBoolean isLogicalConnectorFrozen = new AtomicBoolean(false);

        // Check if the logical connector is on Environment PRE is available (flag automanage must be true or userCode must be Platform admin or SQA admin)
        if(Environment.PRE.getEnvironment().equals(logicalConnector.getEnvironment()))
        {
            // Check if this logical connector has any deployment service being used and the deployment plan is DEPLOYED
            isLogicalConnectorFrozen.set(logicalConnector.getDeploymentServices().stream()
                    .map(deploymentService -> deploymentService.getDeploymentSubsystem().getDeploymentPlan())
                    .anyMatch(deploymentPlan -> !this.manageValidationUtils.checkIfPlanCanBeManagedByUser(this.novaContext.getIvUser(), deploymentPlan)
                            && deploymentPlan.getStatus() == DeploymentStatus.DEPLOYED));
            LOG.debug("[LogicalConnectorValidatorImpl] -> [isLogicalConnectorFrozen]: the logical connector id: [{}] - environment: [{}] is frozen due to the deployment service is being used and " +
                    "deployment plan is deployed", logicalConnector.getId(), logicalConnector.getEnvironment());
        }
        else
        {
            LOG.debug("[LogicalConnectorValidatorImpl] -> [isLogicalConnectorFrozen]: the logical connector id: [{}] is not on PRE environment. Environment: [{}] frozen always be false",
                    logicalConnector.getId(), logicalConnector.getEnvironment());
        }

        return isLogicalConnectorFrozen.get();
    }

    @Override
    public void checkIfLogicalConnectorIsFrozen(LogicalConnector logicalConnector) throws NovaException
    {
        if(this.isLogicalConnectorFrozen(logicalConnector))
        {
            throw new NovaException(LogicalConnectorError.getActionFrozenError(logicalConnector.getName(), logicalConnector.getId(), logicalConnector.getEnvironment(), this.novaContext.getIvUser()));
        }
    }

    @Override
    public DocSystem validateAndGetDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        return this.docSystemService.getDocSystemWithIdAndCategoryAndType(docSystemId, docSystemCategory, docSystemType).orElseThrow(() -> new NovaException(LogicalConnectorError.getDocSystemNotFoundError(docSystemId, docSystemCategory, docSystemType)));
    }

    @Override
    public DocSystem getDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        return this.docSystemService.getDocSystemWithIdAndCategoryAndType(docSystemId, docSystemCategory, docSystemType).orElse(null);
    }
}

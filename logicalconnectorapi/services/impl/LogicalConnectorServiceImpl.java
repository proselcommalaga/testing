package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorUpdateDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.NewLogicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.connector.entities.*;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.UserValidationService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.exception.LogicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorValidator;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Logical connector service to create, get logical connector, delete and such operations..
 */
@Service
public class LogicalConnectorServiceImpl implements ILogicalConnectorService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogicalConnectorServiceImpl.class);

    /**
     * Timeout for making the connection tests for logical connectors method
     */
    @Value("${nova.test.connection.timeout:5000}")
    private int connectionTestTimeout;

    /**
     * Logical Connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Connector type repository
     */
    private final ConnectorTypeRepository connectorTypeRepository;

    /**
     * Logical connector validator service
     */
    private final ILogicalConnectorValidator iLogicalConnectorValidator;

    /**
     * Logical Connector Dto builder instance
     */
    private final ILogicalConnectorBuilder iLogicalConnectorBuilder;

    /**
     * To do task service client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Logical connector property repository
     */
    private final LogicalConnectorPropertyRepository logicalConnectorPropertyRepository;

    /**
     * Logical connector repositories service
     */
    private final ILogicalConnectorRepositoriesService iLogicalConnectorRepositoriesService;

    /**
     * Deployment change repository
     */
    private final DeploymentChangeRepository deploymentChangeRepository;

    /**
     * User service client
     */
    private final IProductUsersClient usersService;

    /**
     * User validation service
     */
    private final UserValidationService userValidationService;

    /**
     * Logical connector validator
     */
    private final ILogicalConnectorValidator logicalConnectorValidator;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * groups
     */
    @Value("${nova.drive.groups:novaadmin.group@bbva.com}")
    private String groups;

    /**
     * Dependency injection constructor
     *
     * @param logicalConnectorRepository           logicalConnectorRepository
     * @param connectorTypeRepository              connectorTypeRepository
     * @param iLogicalConnectorValidatorImpl       iLogicalConnectorValidatorImpl
     * @param iLogicalConnectorBuilder             iLogicalConnectorBuilder
     * @param todoTaskServiceClient                todoTaskServiceClient
     * @param logicalConnectorPropertyRepository   logicalConnectorPropertyRepository
     * @param iLogicalConnectorRepositoriesService iLogicalConnectorRepositoriesService
     * @param deploymentChangeRepository           deployment change repository
     * @param userValidationService                userValidationService
     * @param usersService                         usersService
     * @param logicalConnectorValidator            logicalConnectorValidator
     * @param novaActivityEmitter                  NovaActivity emitter
     */
    @Autowired
    public LogicalConnectorServiceImpl(final LogicalConnectorRepository logicalConnectorRepository,
                                       final ConnectorTypeRepository connectorTypeRepository,
                                       final ILogicalConnectorValidator iLogicalConnectorValidatorImpl,
                                       final ILogicalConnectorBuilder iLogicalConnectorBuilder,
                                       final TodoTaskServiceClient todoTaskServiceClient,
                                       final LogicalConnectorPropertyRepository logicalConnectorPropertyRepository,
                                       final ILogicalConnectorRepositoriesService iLogicalConnectorRepositoriesService,
                                       final DeploymentChangeRepository deploymentChangeRepository,
                                       final UserValidationService userValidationService, final IProductUsersClient usersService,
                                       final ILogicalConnectorValidator logicalConnectorValidator,
                                       final INovaActivityEmitter novaActivityEmitter)
    {
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.connectorTypeRepository = connectorTypeRepository;
        this.iLogicalConnectorValidator = iLogicalConnectorValidatorImpl;
        this.iLogicalConnectorBuilder = iLogicalConnectorBuilder;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.logicalConnectorPropertyRepository = logicalConnectorPropertyRepository;
        this.iLogicalConnectorRepositoriesService = iLogicalConnectorRepositoriesService;
        this.deploymentChangeRepository = deploymentChangeRepository;
        this.userValidationService = userValidationService;
        this.usersService = usersService;
        this.logicalConnectorValidator = logicalConnectorValidator;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    ///////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////////

    @Override
    @Transactional(readOnly = true)
    public Integer requestProperties(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);

        this.iLogicalConnectorValidator.canRequestCheckPropertiesTask(logicalConnector.getLogicalConnectorStatus());

        String todoTaskDescription =
                " solicita configurar las properties del conector lógico con las siguientes características:" + java.lang.System.getProperty("line.separator") +
                        " Nombre: [" + logicalConnector.getName() + "]" + java.lang.System.getProperty("line.separator") +
                        " Tipo: [" + logicalConnector.getConnectorType().getName().toUpperCase() + "]" + java.lang.System.getProperty("line.separator") +
                        " Descripción: [" + logicalConnector.getDescription() + "]" + java.lang.System.getProperty("line.separator") +
                        " Entorno: [" + logicalConnector.getEnvironment().toUpperCase() + "]" + java.lang.System.getProperty("line.separator") +
                        " Producto asociado: [" + logicalConnector.getProduct().getName() + "]";

        try
        {
            String assignedRole = RoleType.SERVICE_SUPPORT.name();

            Integer toDoTaskId = this.todoTaskServiceClient.createManagementTask(ivUser, null, ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES.name(),
                    assignedRole, todoTaskDescription, logicalConnector.getProduct(), logicalConnector.getId());

            // Emit Send Request Configuration Connector Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(logicalConnector.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.SEND_REQUEST_CONFIGURATED)
                    .entityId(logicalConnectorId)
                    .environment(logicalConnector.getEnvironment())
                    .addParam("connectorName", logicalConnector.getName())
                    .addParam("connectorType", logicalConnector.getConnectorType().getName())
                    .addParam("connectorDescription", logicalConnector.getDescription())
                    .addParam("todoTaskId", toDoTaskId)
                    .build());

            LOG.debug("[{}] -> [{}]: the userCode [{}] of the logical connector name: [{}] has created the to do task type: [{}] successfully. TodoTaskId associated: [{}]",
                    Constants.LOGICAL_CONNECTOR_API, Constants.REQUEST_PROPERTIES_METHOD, ivUser, logicalConnector.getName(), ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES, toDoTaskId);

            return toDoTaskId;
        }
        catch (NovaException e)
        {
            String message = "[LogicalConnectorAPI] -> [requestProperties]: the logical Connector: [" + logicalConnector.getName() + "] cannot create to do tasks due to the own product" +
                    " does not have Jira project associated";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getNoSuchJiraProjectKeyError(), e, message);
        }
    }

    @Override
    public LogicalConnectorDto[] getAllFromProduct(final Integer productId, final String connectorType, final String environment) throws NovaException
    {
        // Check the productId
        this.iLogicalConnectorValidator.validateAndGetProduct(productId);

        // Execute the queries depending on the parameters
        List<LogicalConnector> logicalConnectorList;

        // If environment and connector type is empty or null or just the environment is empty or null
        if (StringUtils.isEmpty(environment) && StringUtils.isEmpty(connectorType))
        {
            // Get all the filesystems from the product.
            logicalConnectorList = this.logicalConnectorRepository.findByProductId(productId);
            LOG.trace("[{}] -> [{}]: Not filters. Results: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.GET_ALL_FROM_PRODUCT_METHOD, logicalConnectorList);
        }
        else if (StringUtils.isEmpty(connectorType))
        {
            logicalConnectorList = this.logicalConnectorRepository.findByProductIdAndEnvironment(productId, environment);
            LOG.trace("[{}] -> [{}]: Filter by environment: [{}]. Results: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.GET_ALL_FROM_PRODUCT_METHOD, environment, logicalConnectorList);
        }
        else if (StringUtils.isEmpty(environment))
        {
            logicalConnectorList = this.logicalConnectorRepository.findByProductIdAndConnectorTypeName(productId, connectorType);
            LOG.trace("[{}] -> [{}]: Filter by connector type: [{}]. Results: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.GET_ALL_FROM_PRODUCT_METHOD, connectorType, logicalConnectorList);
        }
        else
        {
            logicalConnectorList = this.logicalConnectorRepository.findByProductIdAndEnvironmentAndConnectorTypeName(
                    productId, environment, connectorType);
            LOG.trace("[{}] -> [{}]: Filters actives: environment: [{}] and connectorType: [{}]. Results: [{}]", Constants.LOGICAL_CONNECTOR_API,
                    Constants.GET_ALL_FROM_PRODUCT_METHOD, environment, connectorType, logicalConnectorList);
        }

        // Call logical connector dto builder
        LogicalConnectorDto[] logicalConnectorDtoArray = this.iLogicalConnectorBuilder.buildLogicalConnectorDtoList(logicalConnectorList);

        LOG.trace("[{}] -> [{}]: a logical connector found: [{}] - Logical connectors details: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.GET_ALL_FROM_PRODUCT_METHOD,
                logicalConnectorDtoArray.length, logicalConnectorDtoArray);

        return logicalConnectorDtoArray;
    }

    @Override
    @Transactional
    public LogicalConnector deleteLogicalConnector(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        // Validate and get the logical connector
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);
        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);

        this.usersService.checkHasPermission(ivUser, Constants.DELETE_CONNECTOR, logicalConnector.getProduct().getId(), new
                NovaException(LogicalConnectorError.getForbiddenError(), LogicalConnectorError.getForbiddenError().toString()));

        // Validate and get the portal user
        this.userValidationService.validateAndGet(ivUser,
                new NovaException(LogicalConnectorError.getNoSuchPortalUserError()));

        // First: Check if the logical connector has to do task associated in pending status
        this.iLogicalConnectorValidator.checkPendingTodoTaskByLogicalConnector(logicalConnector);

        // Second. Check that the logical connector is not archived
        if (logicalConnector.getLogicalConnectorStatus() == LogicalConnectorStatus.ARCHIVED)
        {
            String message = "[LogicalConnectorAPI] -> [deleteLogicalConnector]: the logical Connector: [" + logicalConnector.getName() + "] is already: " +
                    "[" + logicalConnector.getLogicalConnectorStatus() + "] status. It can not be deleted anyway.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getLogicalConnectionDeletionError(), message);
        }
        LOG.debug("[{}] -> [{}]: Logical connector [{}]:ENV:[{}]:Product:[{}] - is not archived. Logical Status: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.DELETE_LOGICAL_CONNECTOR_METHOD,
                logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName(), logicalConnector.getLogicalConnectorStatus());

        // Third A. Check if there is some deployment service in deployed or scheduled status.
        for (DeploymentService deploymentService : logicalConnector.getDeploymentServices())
        {
            if (deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() == DeploymentStatus.DEPLOYED ||
                    deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() == DeploymentStatus.SCHEDULED)
            {
                String message = "[LogicalConnectorAPI] -> [deleteLogicalConnector]: the logical Connector: [" + logicalConnector.getName() + "] is being used by at least one " +
                        "deployment service name: [" + deploymentService.getService().getServiceName() + " ]. " +
                        "Status: [" + deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() + "] ";
                LOG.error(message);
                throw new NovaException(LogicalConnectorError.getDeleteUsedLogicalConnectorError(), message);
            }
        }

        // Third B. Create a historic for all deployments plans
        String deployChangeDescription = "Eliminado del producto el conector logico con nombre: [" + logicalConnector.getName() + "] de tipo: [" + logicalConnector.getConnectorType().getName() +
                "] del entorno: [" + logicalConnector.getEnvironment() + "]. Descripción: [" + logicalConnector.getDescription() + "].";
        for (DeploymentService deploymentService : logicalConnector.getDeploymentServices())
        {
            this.createDeploymentChange(deploymentService.getDeploymentSubsystem().getDeploymentPlan(), deploymentService.getDeploymentSubsystem().getDeploymentPlan().getCurrentRevision(),
                    deployChangeDescription, ivUser);
        }

        // Fourth. Remove the logical connector properties from DeploymentConnectorProperty Entity
        this.iLogicalConnectorRepositoriesService.deleteDeploymentConnectorPropertyList(logicalConnector.getLogConnProp());

        // Fifth. Remove the logical connector from the deployment service relationship
        this.iLogicalConnectorRepositoriesService.deleteLogicalConnectorOfDeploymentService(logicalConnector.getDeploymentServices(), logicalConnector);

        // Finally. Delete logical connector instance from NOVA BBDD
        this.iLogicalConnectorRepositoriesService.deleteLogicalConnector(logicalConnector);

        // Emit Delete Connector Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(logicalConnector.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.DELETED)
                .entityId(logicalConnectorId)
                .environment(logicalConnector.getEnvironment())
                .addParam("cnnectorName", logicalConnector.getName())
                .addParam("connectorType", logicalConnector.getConnectorType().getName())
                .addParam("connectorDescription", logicalConnector.getDescription())
                .build());

        LOG.debug("[{}] -> [{}]: logical connector [{}]:ENV:[{}]:Product:[{}] - has been deleted from BBDD successfully.", Constants.LOGICAL_CONNECTOR_API, Constants.DELETE_LOGICAL_CONNECTOR_METHOD,
                logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName());

        return logicalConnector;
    }

    @Override
    @Transactional
    public LogicalConnector archiveLogicalConnector(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        // Validate and get the logical connector
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);
        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);

        this.usersService.checkHasPermission(ivUser, Constants.ARCHIVE_CONNECTOR, logicalConnector.getProduct().getId(),
                new NovaException(LogicalConnectorError.getForbiddenError(), LogicalConnectorError.getForbiddenError().toString()));

        // First: Check the logical connector status. It can only be archived if the logical connector status is CREATED
        if (logicalConnector.getLogicalConnectorStatus() != LogicalConnectorStatus.CREATED)
        {
            String message = "[LogicalConnectorAPI] -> [archiveLogicalConnector]: the logical Connector: [" + logicalConnector.getName() + "] " +
                    "can not be archived due the logical connector status must be: [CREATED].";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getTriedToArchiveLogicalConnectorNotCreatedError(), message);
        }

        // Second: Check if the logical connector has to do task associated in pending status
        this.iLogicalConnectorValidator.checkPendingTodoTaskByLogicalConnector(logicalConnector);

        // Third. Check that the logical connector has been used almost once and is not being used by any deployment plan
        this.checkDeploymentsPlanStatusForArchiving(logicalConnector);

        // Finally. Set the logical connector status to ARCHIVED and save it into BBDD
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        LogicalConnector logicalConnectorEntity = this.logicalConnectorRepository.saveAndFlush(logicalConnector);

        // Emit Archive Connector Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(logicalConnectorEntity.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.ARCHIVED)
                .entityId(logicalConnectorEntity.getId())
                .environment(logicalConnectorEntity.getEnvironment())
                .addParam("cnnectorName", logicalConnector.getName())
                .addParam("connectorType", logicalConnector.getConnectorType().getName())
                .addParam("connectorDescription", logicalConnector.getDescription())
                .build());

        LOG.debug("[{}] -> [{}]: Logical connector [{}]:ENV:[{}]:Product:[{}] - has been archived into BBDD successfully. Logical connector status: [{}]",
                Constants.LOGICAL_CONNECTOR_API, Constants.ARCHIVE_LOGICAL_CONNECTOR_METHOD, logicalConnector.getName(), logicalConnector.getEnvironment(),
                logicalConnector.getProduct().getName(), logicalConnector.getLogicalConnectorStatus());

        return logicalConnector;
    }

    @Override
    @Transactional
    public LogicalConnector restoreLogicalConnector(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        // Validate and get the logical connector
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);

        this.usersService.checkHasPermission(ivUser, Constants.RESTORE_CONNECTOR, logicalConnector.getProduct().getId(), new
                NovaException(LogicalConnectorError.getForbiddenError(), LogicalConnectorError.getForbiddenError().toString()));

        // Check the logical connector status. It can only be archived if the logical connector status is ARCHIVED
        if (logicalConnector.getLogicalConnectorStatus() != LogicalConnectorStatus.ARCHIVED)
        {
            String message = "[LogicalConnectorAPI] -> [restoreLogicalConnector]: The logical Connector: [" + logicalConnector.getName() + "] can not be restore" +
                    " due the logical connector status must be: [ARCHIVED].";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getTriedToRestoreLogicalConnectorError(), message);
        }

        // Set the logical connector status to CREATED and save it into BBDD
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        LogicalConnector logicalConnectorEntity = this.logicalConnectorRepository.saveAndFlush(logicalConnector);

        // Emit Restore Connector Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(logicalConnectorEntity.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.RESTORED)
                .entityId(logicalConnectorEntity.getId())
                .environment(logicalConnectorEntity.getEnvironment())
                .addParam("cnnectorName", logicalConnector.getName())
                .addParam("connectorType", logicalConnector.getConnectorType().getName())
                .addParam("connectorDescription", logicalConnector.getDescription())
                .build());

        LOG.debug("[{}] -> [{}]: the logical connector [{}]:ENV:[{}]:Product:[{}] - has been restored successfully. Logical connector status: [{}]",
                Constants.LOGICAL_CONNECTOR_API, Constants.RESTORE_LOGICAL_CONNECTOR_METHOD, logicalConnector.getName(), logicalConnector.getEnvironment(),
                logicalConnector.getProduct().getName(), logicalConnector.getLogicalConnectorStatus());

        return logicalConnector;
    }

    @Override
    public String[] getConnectorTypes()
    {
        // Find all the connectors types
        List<ConnectorType> connectorTypeList = this.connectorTypeRepository.findAll();

        // Create the connector types names array size
        String[] connnectorTypesNamesArray = new String[connectorTypeList.size()];

        // Fill the the connector types names array
        for (int i = 0; i < connectorTypeList.size(); i++)
        {
            connnectorTypesNamesArray[i] = connectorTypeList.get(i).getName();
        }

        LOG.debug("[{}] -> [{}]: Find the following connectors types: [{}] ", Constants.LOGICAL_CONNECTOR_API, Constants.GET_CONNECTOR_TYPES_METHOD,
                connnectorTypesNamesArray);

        return connnectorTypesNamesArray;
    }

    @Override
    @Transactional
    public String testLogicalConnector(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);

        this.usersService.checkHasPermission(ivUser, Constants.TEST_CONNECTOR, logicalConnector.getProduct().getId(), new NovaException
                (LogicalConnectorError.getForbiddenError()));

        // Validate the logical connector status
        this.iLogicalConnectorValidator.validateLogicalConnectorCreatedStatus(logicalConnector);

        // Get the physical connector and physical connector port associated
        PhysicalConnector physicalConnector = logicalConnector.getPhysicalConnector();
        PhysicalConnectorPort physicalConnectorPort = logicalConnector.getPhysicalConnectorPort();

        // Create a list with virtualIp to check
        List<String> virtualIpList = new ArrayList<>();
        if (physicalConnector.getVirtualIp() != null && !physicalConnector.getVirtualIp().isEmpty())
        {
            virtualIpList.add(physicalConnector.getVirtualIp());
        }
        if (physicalConnector.getVaguadaVirtualIp() != null && !physicalConnector.getVaguadaVirtualIp().isEmpty())
        {
            virtualIpList.add(physicalConnector.getVaguadaVirtualIp());
        }

        // Create result message
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"Resultados [").append(logicalConnector.getName()).append("]:").append("\\n");

        for (String virtualIp : virtualIpList)
        {
            try (Socket socket = new Socket())
            {
                // Try to listen to the ip:port and close connection
                SocketAddress socketAddress = new InetSocketAddress(virtualIp, physicalConnectorPort.getInputPort());
                socket.connect(socketAddress, this.connectionTestTimeout);

                // Test OK. Save the results
                LOG.debug("[{}] -> [{}]: the physical connector name: [{}] - virtual ip: [{}] - input port: [{}] is listen successfully.", Constants.LOGICAL_CONNECTOR_API, Constants.TEST_LOGICAL_CONNECTOR_METHOD, physicalConnector.getName(), virtualIp, physicalConnectorPort.getInputPort());
                stringBuilder.append("Conexión realizada con éxito. El conector está escuchando correctamente en la ip virtual: [").append(virtualIp).append("] en el puerto: [").append(physicalConnectorPort.getInputPort()).append("]").append("\\n");
            }
            catch (IOException e)
            {
                LOG.error("[{}] -> [{}]: the physical connector name: [{}], virtual Ip: [{}] - input port: [{}] is not listen. Error message: [{}]", Constants.LOGICAL_CONNECTOR_API, Constants.TEST_LOGICAL_CONNECTOR_METHOD, physicalConnector.getName(), virtualIp, physicalConnectorPort.getInputPort(), e.getMessage());
                // Test not OK. Save the results
                stringBuilder.append("Conexión fallida. El conector NO está escuchando en la ip virtual: [").append(virtualIp).append("] en el puerto: [").append(physicalConnectorPort.getInputPort()).append("].").append(" Error: ").append(e.getMessage()).append("\\n");
            }
        }

        return stringBuilder.append("\"").toString();
    }

    @Override
    @Transactional
    public LogicalConnectorDto getLogicalConnector(final Integer logicalConnectorId, final String ivUser) throws NovaException
    {
        LogicalConnector logicalConnector = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);
        this.usersService.checkHasPermission(ivUser, Constants.CONNECTOR_DETAIL, logicalConnector.getProduct().getId(), new
                NovaException(LogicalConnectorError.getForbiddenError()));
        // Build the logical connector Dto and return
        return this.iLogicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, ivUser);
    }

    @Override
    @Transactional
    public Integer createLogicalConnector(final NewLogicalConnectorDto newLogicalConnectorDto, final Integer productId,
                                          final String ivUser) throws NovaException
    {
        this.usersService.checkHasPermission(ivUser, Constants.CREATE_CONNECTOR, productId, new NovaException
                (LogicalConnectorError.getForbiddenError()));

        // First. Get the product
        Product product = this.iLogicalConnectorValidator.validateAndGetProduct(productId);

        // Get the logical connector with
        LogicalConnector logicalConnector = this.createNewLogicalConnector(newLogicalConnectorDto, product);

        // Save logical connector into BBDD
        LogicalConnector logicalConnectorEntity = this.logicalConnectorRepository.saveAndFlush(logicalConnector);

        // Create to do task "CREATION_CONNECTOR_REQUEST"
        Integer toDoTaskId = this.createConnectorTodoTask(ivUser, product, logicalConnector);

        // Emit Add Connector Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(logicalConnectorEntity.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.SEND_REQUEST_ADDED)
                .entityId(logicalConnectorEntity.getId())
                .environment(logicalConnectorEntity.getEnvironment())
                .addParam("connectorName", logicalConnector.getName())
                .addParam("connectorType", logicalConnector.getConnectorType().getName())
                .addParam("connectorDescription", logicalConnector.getDescription())
                .addParam("todoTaskId", toDoTaskId)
                .build());

        LOG.debug("[{}] -> [{}]: the logical connector name: [{}] has been created successfully. Status: [{}]. TodoTaskId associated: [{}]",
                Constants.LOGICAL_CONNECTOR_API, Constants.CREATE_LOGICAL_CONNECTOR_METHOD, logicalConnector.getName(), logicalConnector.getLogicalConnectorStatus().name(), toDoTaskId);

        return toDoTaskId;
    }

    @Override
    public Boolean isLogicalConnectorFrozen(Integer logicalConnectorId)
    {
        LogicalConnector logicalConnector = this.logicalConnectorRepository.findById(logicalConnectorId).orElseThrow(() -> new NovaException(LogicalConnectorError.getNoSuchLogicalConnectorError()));
        return this.logicalConnectorValidator.isLogicalConnectorFrozen(logicalConnector);
    }

    @Override
    public String[] getLogicalConnectorsStatuses()
    {
        return Arrays.stream(LogicalConnectorStatus.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    @Transactional
    public void updateLogicalConnector(LogicalConnectorUpdateDto logicalConnectorUpdateDto, Integer logicalConnectorId, String ivUser)
    {
        // Validate and get the logical connector
        LogicalConnector logicalConnectorEntity = this.iLogicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);

        // Check if the user has permissions.
        this.usersService.checkHasPermission(ivUser, Constants.UPDATE_CONNECTOR, logicalConnectorEntity.getProduct().getId(),
                new NovaException(LogicalConnectorError.getForbiddenError()));

        // Validate and set MSA document.
        DocSystem msaDocument = this.iLogicalConnectorValidator.validateAndGetDocument(logicalConnectorUpdateDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE);
        logicalConnectorEntity.setMsaDocument(msaDocument);

        // Validate and set ARA document.
        DocSystem araDocument = this.iLogicalConnectorValidator.getDocument(logicalConnectorUpdateDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE);
        logicalConnectorEntity.setAraDocument(araDocument);

        // Update description.
        if (Strings.isNullOrEmpty(logicalConnectorUpdateDto.getDescription()))
        {
            throw new NovaException(LogicalConnectorError.getValidationError("Description is mandatory"));
        }
        logicalConnectorEntity.setDescription(logicalConnectorUpdateDto.getDescription());

        // Save logical connector into BBDD.
        this.logicalConnectorRepository.saveAndFlush(logicalConnectorEntity);

        // Emit Modified Connector Activity.
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(logicalConnectorEntity.getProduct().getId(), ActivityScope.CONNECTOR, ActivityAction.MODIFIED)
                .entityId(logicalConnectorEntity.getId())
                .environment(logicalConnectorEntity.getEnvironment())
                .addParam("connectorSolutionsArchitectDocumentation", logicalConnectorUpdateDto.getMsaDocumentId())
                .addParam("connectorSecurityDocumentation", logicalConnectorUpdateDto.getAraDocumentId())
                .build());
    }

    @Override
    public List<LogicalConnector> getLogicalConnectorUsingMsaDocument(Integer msaDocumentId)
    {
        return this.logicalConnectorRepository.findByMsaDocumentId(msaDocumentId);
    }

    @Override
    public List<LogicalConnector> getLogicalConnectorUsingAraDocument(Integer araDocumentId)
    {
        return this.logicalConnectorRepository.findByAraDocumentId(araDocumentId);
    }

    ////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////////////

    /**
     * Get a new logical connector. Fill all the logical connector with the beginning properties
     *
     * @param newLogicalConnectorDto logical connector properties from the form
     * @param product                product associated
     * @return a logical connector instance
     * @throws NovaException logicalConnectorException
     */
    private LogicalConnector createNewLogicalConnector(final NewLogicalConnectorDto newLogicalConnectorDto, final Product product) throws NovaException
    {
        LogicalConnector logicalConnector = new LogicalConnector();

        // Validate and set the logical connector name
        logicalConnector.setName(this.iLogicalConnectorValidator.validateLogicalConnectorName(newLogicalConnectorDto.getLogicalConnectorName(),
                newLogicalConnectorDto.getEnvironment(), product.getId()));

        // Validate and set the connector type
        ConnectorType connectorType = this.iLogicalConnectorValidator.validateConnectorType(newLogicalConnectorDto.getConnectorType());
        logicalConnector.setConnectorType(connectorType);

        // Validate and set MSA document
        DocSystem msaDocument = this.iLogicalConnectorValidator.validateAndGetDocument(newLogicalConnectorDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE);
        logicalConnector.setMsaDocument(msaDocument);

        // Validate and set ARA document.
        DocSystem araDocument = this.iLogicalConnectorValidator.getDocument(newLogicalConnectorDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE);
        logicalConnector.setAraDocument(araDocument);

        // Set the description and the environment
        logicalConnector.setDescription(newLogicalConnectorDto.getLogicalConnectorDescription());
        logicalConnector.setEnvironment(newLogicalConnectorDto.getEnvironment());

        // Set the logical connector status. At first, the logical connector has "CREATING" status
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATING);
        LOG.trace("[{}] -> [createNewLogicalConnector]: the status of the logical connector name: [{}] has been set to: [{}]",
                Constants.LOGICAL_CONNECTOR_API, newLogicalConnectorDto.getLogicalConnectorName(), LogicalConnectorStatus.CREATING);

        // Associate the product to logical connector
        logicalConnector.setProduct(product);
        LOG.debug("[{}] -> [createNewLogicalConnector]: added logical connector to the product: [{}]", Constants.LOGICAL_CONNECTOR_API, product.getName());

        // Get the logical connector properties
        logicalConnector.setLogConnProp(this.createLogicalConnectorPropertiesList(connectorType, logicalConnector));

        return logicalConnector;
    }

    /**
     * Gets the logical connector properties list by connector type name
     *
     * @param connectorType    connector type
     * @param logicalConnector the logical connector
     * @return a logical connector properties list by connector name
     */
    private List<LogicalConnectorProperty> createLogicalConnectorPropertiesList(final ConnectorType connectorType, final LogicalConnector logicalConnector)
    {
        List<LogicalConnectorProperty> logicalConnectorPropertyList = new ArrayList<>();

        // Get the properties of the connector type
        List<ConnectorTypeProperty> connectorTypePropertyList = connectorType.getConnectorTypeProperties();
        LOG.debug("[{}] -> [createLogicalConnectorPropertiesList]: find the following connector type properties list: [{}]"
                + " for the connector type: [{}]", Constants.LOGICAL_CONNECTOR_API, connectorTypePropertyList, connectorType);

        for (ConnectorTypeProperty connectorTypeProperty : connectorTypePropertyList)
        {
            if (Environment.PRO.getEnvironment().equals(logicalConnector.getEnvironment()) || !connectorTypeProperty.getCpdName().equalsIgnoreCase(Constants.V_CPD_NAME))
            {
                LogicalConnectorProperty logicalConnectorProperty = new LogicalConnectorProperty();

                // Modify the name of the connector type property as the patter = 'LOGICAL_CONNECTOR_NAMNE'_'CONNECTOR_TYPE_PROPERTY_NAME'
                String name = MessageFormat.format("{0}.{1}", logicalConnector.getName().toUpperCase(), connectorTypeProperty.getName());
                logicalConnectorProperty.setName(name);
                // Default property name will be the same than the name of the connector type property name
                logicalConnectorProperty.setDefaultName(name);

                logicalConnectorProperty.setDescription(connectorTypeProperty.getDescription());
                logicalConnectorProperty.setManagement(connectorTypeProperty.getManagement());
                logicalConnectorProperty.setPropertyType(connectorTypeProperty.getPropertyType());
                logicalConnectorProperty.setScope(connectorTypeProperty.getScope());
                logicalConnectorProperty.setSecurity(connectorTypeProperty.isSecurity());

                // Save the new logical connector property into BBDD
                this.logicalConnectorPropertyRepository.saveAndFlush(logicalConnectorProperty);

                LOG.debug("[{}] -> [createLogicalConnectorPropertiesList]: added and saved the new logical connector property: [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnectorProperty);

                // Add to the list
                logicalConnectorPropertyList.add(logicalConnectorProperty);
            }
        }

        LOG.debug("[{}] -> [createLogicalConnectorPropertiesList]: the logical connector properties list is: [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnectorPropertyList);

        return logicalConnectorPropertyList;
    }

    /**
     * Create the to do task connector request to do task
     *
     * @param ivUser           user that generates the to do task
     * @param product          product associated to the to do task
     * @param logicalConnector logical connector to get the details
     * @return id of the to do task created
     */
    private Integer createConnectorTodoTask(final String ivUser, final Product product, final LogicalConnector logicalConnector)
    {
        String documents = "";
        String todoTaskDescription = " solicita asociar el siguiente conector lógico a un conector físico con estas características:" + java.lang.System.getProperty("line.separator") +
                " Nombre: [" + logicalConnector.getName() + "]" + java.lang.System.getProperty("line.separator") +
                " Tipo: [" + logicalConnector.getConnectorType().getName() + "]" + java.lang.System.getProperty("line.separator") +
                " Descripción: [" + logicalConnector.getDescription() + "]" + java.lang.System.getProperty("line.separator") +
                " Entorno: [" + logicalConnector.getEnvironment().toUpperCase() + "]" + java.lang.System.getProperty("line.separator");
        if (logicalConnector.getMsaDocument() != null)
        {
            todoTaskDescription += String.format(" Documentación de Solutions Architect: [%s: %s]%s", logicalConnector.getMsaDocument().getSystemName(), logicalConnector.getMsaDocument().getUrl(), java.lang.System.getProperty("line.separator"));
            documents = logicalConnector.getMsaDocument().getSystemName();
        }
        if (logicalConnector.getAraDocument() != null)
        {
            todoTaskDescription += String.format(" Documentación de Seguridad: [%s: %s]", logicalConnector.getAraDocument().getSystemName(), logicalConnector.getAraDocument().getUrl());
            documents += " y " + logicalConnector.getAraDocument().getSystemName();
        }

        todoTaskDescription += String.format(" \n\n[NOTA sobre la Documentación]:\n" +
                "Para que los administradores de la plataforma puedan revisar los documentos, recuerda que debes compartir el documento: %s " +
                "(o la carpeta raíz) con los siguientes grupos para que sea visible: [%s]", documents, groups);

        try
        {
            return this.todoTaskServiceClient.createManagementTask(ivUser, null, ToDoTaskType.CREATION_LOGICAL_CONNECTOR_REQUEST.name(),
                    RoleType.PLATFORM_ADMIN.name(), todoTaskDescription, product, logicalConnector.getId());
        }
        catch (NovaException e)
        {
            String message = "[LogicalConnectorAPI] -> [createConnectorTodoTask]: the logical Connector: [" + logicalConnector.getName() + "] cannot create to do tasks. " + "ErrorCode: [" + e.getNovaError().getErrorCode() + "] - ErrorMessage: [" + e.getMessage() + "] - ErrorException: [" + e.getCause().getMessage() + "]";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getNoSuchJiraProjectKeyError(), e, message);
        }
    }

    /**
     * Check if the deployment plan associated to logical connector can be archived
     *
     * @param logicalConnector the logical connector to check.
     * @throws NovaException logicalConnectorException
     */
    private void checkDeploymentsPlanStatusForArchiving(final LogicalConnector logicalConnector) throws NovaException
    {
        List<DeploymentService> deploymentServiceList = logicalConnector.getDeploymentServices();

        if (deploymentServiceList.isEmpty())
        {
            String message = "[LogicalConnectorAPI] -> [checkDeploymentsPlanStatusForArchiving]: the logical Connector: [" + logicalConnector.getName() +
                    "] has never been used by any deployment plan. It can not be archived.";
            LOG.error(message);
            throw new NovaException(LogicalConnectorError.getTriedToArchiveLogicalConnectorNotUsedError(), message);
        }
        else
        {
            int definitionPlansCounter = 0;
            for (DeploymentService deploymentService : deploymentServiceList)
            {
                if (deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() == DeploymentStatus.DEPLOYED)
                {
                    String message = "[LogicalConnectorAPI] -> [checkDeploymentsPlanStatusForArchiving]: the logical Connector: [" + logicalConnector.getName() + "] can not be archived" +
                            " due to is being used by active (DEPLOYED) deployment plan ID: [" + deploymentService.getDeploymentSubsystem().getDeploymentPlan().getId() +
                            "] - ServiceName: [" + deploymentService.getService().getServiceName() + "].";
                    LOG.error(message);
                    throw new NovaException(LogicalConnectorError.getTriedToArchiveUsedLogicalConnectorError(), message);
                }

                if (deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() == DeploymentStatus.DEFINITION)
                {
                    definitionPlansCounter++;
                }
            }

            // Check that the number of definition plan is lower than the total deployments plan of this logical connector
            if (definitionPlansCounter == deploymentServiceList.size())
            {
                String message = "[LogicalConnectorAPI] -> [checkDeploymentsPlanStatusForArchiving]: the logical Connector: [" + logicalConnector.getName() + "] can not be archived" +
                        " due to all the deployments plan are in DEFINITION status, so has never been used really.";
                LOG.error(message);
                throw new NovaException(LogicalConnectorError.getTriedToArchiveLogicalConnectorNotUsedError(), message);
            }
        }

        LOG.debug("[{}] -> [checkDeploymentsPlanStatusForArchiving]: the logical connector [{}]:ENV:[{}]:Product:[{}] - has been used by some deployment plan.", Constants.LOGICAL_CONNECTOR_API,
                logicalConnector.getName(), logicalConnector.getEnvironment(), logicalConnector.getProduct().getName());
    }

    /**
     * Create new deploymentPlan change if the deploymentPlan plan is DEPLOYED
     *
     * @param deploymentPlan          the deploymentPlan plan to save the change
     * @param configurationRevision   the configuration revision to save
     * @param deployChangeDescription deployChangeDescription of the deployment change
     * @param userCode                user that makes the change
     */
    private void createDeploymentChange(final DeploymentPlan deploymentPlan,
                                        final ConfigurationRevision configurationRevision, final String deployChangeDescription,
                                        final String userCode)
    {
        DeploymentChange deploymentChange = new DeploymentChange(deploymentPlan, deployChangeDescription, Calendar.getInstance(), ChangeType.DELETE_LOGICAL_CONNECTOR,
                userCode, configurationRevision, null);
        deploymentPlan.getChanges().add(deploymentChange);

        this.deploymentChangeRepository.saveAndFlush(deploymentChange);
        LOG.debug("[{}] -> [createDeploymentChange]: Created and saved a new deployment deploymentChange with the following parameters: [{}]", Constants.LOGICAL_CONNECTOR_API,
                deploymentChange);
    }
}

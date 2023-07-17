package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.EditPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.config.enumerates.PropertyType;
import com.bbva.enoa.datamodel.model.config.enumerates.ScopeType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypePropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception.PhysicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorService;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorValidator;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Physical connector service to create, get physical connector, delete and such
 * operations..
 */
@Service
public class PhysicalConnectorServiceImpl implements IPhysicalConnectorService
{

    private static final NovaException UNAUTHORIZED_EXCEPTION = new NovaException(PhysicalConnectorError.getForbiddenError(), PhysicalConnectorError.getForbiddenError().toString());

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalConnectorServiceImpl.class);

    /**
     * Physical Connector repository
     */
    private final PhysicalConnectorRepository physicalConnectorRepository;

    /**
     * Logical Connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Connector type repository
     */
    private final ConnectorTypeRepository connectorTypeRepository;

    /**
     * Connector type property repository
     */
    private final ConnectorTypePropertyRepository connectorTypePropertyRepository;

    /**
     * Physical connector validator service
     */
    private final IPhysicalConnectorValidator iPhysicalConnectorValidator;

    /**
     * Physical Connector DTO builder instance
     */
    private final IPhysicalConnectorBuilder iPhysicalConnectorBuilder;

    /**
     * To-do Task Service Client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Deployment change repository
     */
    private final DeploymentChangeRepository deploymentChangeRepository;

    /**
     * Logical connector property repository
     */
    private final LogicalConnectorPropertyRepository logicalConnectorPropertyRepository;

    /**
     * Physical Connector Repository services
     */
    private final IPhysicalConnectorRepositoriesService iPhysicalConnectorRepositoriesService;

    private final IProductUsersClient usersClient;

    /**
     * Dependency injection constructor
     *
     * @param physicalConnectorRepository           physical Connector Repository
     * @param logicalConnectorRepository            logical Connector Repository
     * @param connectorTypeRepository               connector Type Repository
     * @param connectorTypePropertyRepository       connector Type Property Repository
     * @param iPhysicalConnectorValidator           iPhysical Connector Validator
     * @param iPhysicalConnectorBuilder             iPhysical Connector Builder
     * @param todoTaskServiceClient                 todoTaskServiceClient
     * @param logicalConnectorPropertyRepository    logicalConnectorPropertyRepository
     * @param iPhysicalConnectorRepositoriesService ILogicalConnectorRepositoriesService
     * @param deploymentChangeRepository            deployment change repository
     * @param userClient                            Client for integrating with usersservice.
     */
    @Autowired
    public PhysicalConnectorServiceImpl(final PhysicalConnectorRepository physicalConnectorRepository,
                                        final LogicalConnectorRepository logicalConnectorRepository,
                                        final ConnectorTypeRepository connectorTypeRepository,
                                        final ConnectorTypePropertyRepository connectorTypePropertyRepository,
                                        final IPhysicalConnectorValidator iPhysicalConnectorValidator,
                                        final IPhysicalConnectorBuilder iPhysicalConnectorBuilder,
                                        final TodoTaskServiceClient todoTaskServiceClient,
                                        final LogicalConnectorPropertyRepository logicalConnectorPropertyRepository,
                                        final IPhysicalConnectorRepositoriesService iPhysicalConnectorRepositoriesService,
                                        final DeploymentChangeRepository deploymentChangeRepository, final IProductUsersClient userClient)
    {

        this.physicalConnectorRepository = physicalConnectorRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.connectorTypeRepository = connectorTypeRepository;
        this.connectorTypePropertyRepository = connectorTypePropertyRepository;
        this.iPhysicalConnectorValidator = iPhysicalConnectorValidator;
        this.iPhysicalConnectorBuilder = iPhysicalConnectorBuilder;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.logicalConnectorPropertyRepository = logicalConnectorPropertyRepository;
        this.iPhysicalConnectorRepositoriesService = iPhysicalConnectorRepositoriesService;
        this.deploymentChangeRepository = deploymentChangeRepository;
        this.usersClient = userClient;
    }

    ////////////////////////////////////////// IMPLEMENTATION ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public PhysicalConnectorDto[] getAllPhysicalConnector(final String connectorType, final String environment)
    {

        // Execute the queries depending on the parameters
        List<PhysicalConnector> physicalConnectorList;

        // If environment and connector type is empty or null or just the
        // environment is empty or null
        if (StringUtils.isEmpty(environment) && StringUtils.isEmpty(connectorType))
        {
            // Get all the physical connectors.
            physicalConnectorList = this.physicalConnectorRepository.findAll();
            LOG.debug("[{}] -> [{}]: Not filters actives. Physical connector list results: [{}]", Constants.PHYSICAL_CONNECTOR_API, Constants.GET_ALL_PHYSICAL_CONNECTORS_METHOD, physicalConnectorList);
        }
        else if (StringUtils.isEmpty(connectorType))
        {
            physicalConnectorList = this.physicalConnectorRepository.findByEnvironment(environment);
            LOG.debug("[{}] -> [{}]: Filter by environment: [{}]. Physical connector list results: [{}]", Constants.PHYSICAL_CONNECTOR_API, Constants.GET_ALL_PHYSICAL_CONNECTORS_METHOD, environment, physicalConnectorList);
        }
        else if (StringUtils.isEmpty(environment))
        {
            physicalConnectorList = this.physicalConnectorRepository.findByConnectorTypeName(connectorType);
            LOG.debug("[{}] -> [{}]: Filter by connector type: [{}]. Physical connector list results: [{}]", Constants.PHYSICAL_CONNECTOR_API, Constants.GET_ALL_PHYSICAL_CONNECTORS_METHOD, connectorType, physicalConnectorList);
        }
        else
        {
            physicalConnectorList = this.physicalConnectorRepository.findByEnvironmentAndConnectorTypeName(environment, connectorType);
            LOG.debug("[{}] -> [{}]: Filters actives: environment: [{}] and connectorType: [{}]. Physical connector list results: [{}]",
                    Constants.PHYSICAL_CONNECTOR_API, Constants.GET_ALL_PHYSICAL_CONNECTORS_METHOD, environment, connectorType, physicalConnectorList);
        }

        // Call physical connector dto builder
        return this.iPhysicalConnectorBuilder.buildPhysicalConnectorDtoList(physicalConnectorList);
    }

    @Override
    public ConnectorTypeDto[] getConnectorTypes()
    {
        ConnectorTypeDto[] connectorTypesDtoArray;

        // Find all the connectors types
        List<ConnectorType> connectorTypeList = this.connectorTypeRepository.findAll();

        if (connectorTypeList.isEmpty())
        {
            connectorTypesDtoArray = new ConnectorTypeDto[0];
            LOG.debug("[{}] -> [{}]: Do not find any connectors types [0].", Constants.PHYSICAL_CONNECTOR_API, Constants.GET_CONNECTOR_TYPES_METHOD);
        }
        else
        {
            // Create the connector types names array size
            connectorTypesDtoArray = new ConnectorTypeDto[connectorTypeList.size()];

            // Fill the the connector types names array
            for (int i = 0; i < connectorTypeList.size(); i++)
            {
                ConnectorTypeDto connectorTypeDto = new ConnectorTypeDto();
                connectorTypeDto.setPhysicalConnectorTypeDescription(connectorTypeList.get(i).getDescription());
                connectorTypeDto.setPhysicalConnectorTypeName(connectorTypeList.get(i).getName());
                connectorTypeDto.setId(connectorTypeList.get(i).getId());

                connectorTypesDtoArray[i] = connectorTypeDto;
            }

            LOG.debug("[{}] -> [{}]: Found the following connectors types: [{}] ", Constants.PHYSICAL_CONNECTOR_API, Constants.GET_CONNECTOR_TYPES_METHOD, connectorTypesDtoArray);
        }

        return connectorTypesDtoArray;
    }

    @Override
    public String[] getManagementTypes()
    {

        // Get the management Types and create the management type string array
        ManagementType[] managementTypes = ManagementType.values();
        String[] managementTypeStringArray = new String[managementTypes.length];

        for (int i = 0; i < managementTypes.length; i++)
        {
            managementTypeStringArray[i] = managementTypes[i].name();
        }

        return managementTypeStringArray;
    }

    @Override
    public ConnectorTypeDto getConnectorType(final Integer connectorTypeId)
    {

        ConnectorType connectorType = this.iPhysicalConnectorValidator.validateConnectorType(connectorTypeId);

        // Create the connector type dto and return
        ConnectorTypeDto connectorTypeDto = new ConnectorTypeDto();
        connectorTypeDto.setPhysicalConnectorTypeDescription(connectorType.getDescription());
        connectorTypeDto.setPhysicalConnectorTypeName(connectorType.getName());
        connectorTypeDto.setId(connectorType.getId());

        return connectorTypeDto;
    }

    @Override
    public String[] getScopeTypes()
    {
        // Get the scope types and create the scope type string array
        ScopeType[] scopeTypeArray = ScopeType.values();
        String[] scopeTypeStringArray = new String[scopeTypeArray.length];

        for (int i = 0; i < scopeTypeArray.length; i++)
        {
            scopeTypeStringArray[i] = scopeTypeArray[i].name();
        }

        return scopeTypeStringArray;
    }

    @Override
    public String[] getPropertyTypes()
    {
        // Get the property types and create the property type string array
        PropertyType[] propertyTypeArray = PropertyType.values();
        String[] propertyTypeStringArray = new String[propertyTypeArray.length];

        for (int i = 0; i < propertyTypeArray.length; i++)
        {
            propertyTypeStringArray[i] = propertyTypeArray[i].name();
        }

        return propertyTypeStringArray;
    }

    @Override
    public PhysicalConnectorDto getPhysicalConnector(final Integer physicalConnectorId)
    {
        // Validate physical connector id
        PhysicalConnector physicalConnector = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(physicalConnectorId);

        // Get the physical connector DTO
        return this.iPhysicalConnectorBuilder.buildPhysicalConnectorDto(physicalConnector);
    }

    @Override
    @Transactional
    public PhysicalConnector createPhysicalConnector(final NewPhysicalConnectorDto newPhysicalConnectorDto, final String userCode)
    {
        this.iPhysicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto);

        this.usersClient.checkHasPermission(userCode, Constants.CREATE_PHYSICAL_CONNECTOR, UNAUTHORIZED_EXCEPTION);

        // Validate if the physical connector name exist in this environment
        this.iPhysicalConnectorValidator.validatePhysicalConnectorName(newPhysicalConnectorDto.getPhysicalConnectorName(), newPhysicalConnectorDto.getEnvironment());

        // Get the physical connector
        PhysicalConnector physicalConnector = this.iPhysicalConnectorBuilder.buildNewPhysicalConnector(newPhysicalConnectorDto);

        // Save the new physical connector into BBDD
        this.physicalConnectorRepository.saveAndFlush(physicalConnector);

        return physicalConnector;
    }

    @Override
    @Transactional
    public ConnectorTypeProperty addConnectorTypeProperty(final ConnectorTypePropertyDto connectorTypePropertyDto, final Integer connectorTypeId, final String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.ADD_PROPERTY_CONNECTOR_TYPE, UNAUTHORIZED_EXCEPTION);
        ValidationUtils.verifyNotNull(connectorTypePropertyDto, new NovaException(PhysicalConnectorError.getConnectorTypePropertyRequiredError()));

        // Validate and get the connector type
        ConnectorType connectorType = this.iPhysicalConnectorValidator.validateConnectorType(connectorTypeId);

        // Validate the connector type property name
        this.iPhysicalConnectorValidator.validateConnectorTypePropertyName(connectorType, connectorTypePropertyDto.getPropertyName());

        // Create and save the new connectorTypePropertyDto
        ConnectorTypeProperty connectorTypeProperty = this.iPhysicalConnectorBuilder.createConnectorTypeProperty(connectorTypePropertyDto, connectorType.getName());

        // Add the new connector type property to the connector type list
        connectorType.getConnectorTypeProperties().add(connectorTypeProperty);

        // Update all the logical connector with the new connector type property
        this.updateAllLogicalConnectorsProperties(connectorType.getName(), connectorTypeProperty, ivUser);

        return connectorTypeProperty;
    }

    @Override
    public ConnectorTypePropertyDto[] getConnectorTypeProperties(final Integer connectorTypeId)
    {
        // Validate and get the connector type
        ConnectorType connectorType = this.iPhysicalConnectorValidator.validateConnectorType(connectorTypeId);

        // Get the connector type property list from BBDD
        List<ConnectorTypeProperty> connectorTypePropertyList = connectorType.getConnectorTypeProperties();

        // Build and return the connector type property dto list
        return this.iPhysicalConnectorBuilder.buildConnectorTypePropertyDtoArray(connectorTypePropertyList);
    }

    @Override
    @Transactional
    public ConnectorTypeProperty deleteConnectorTypeProperty(final Integer connectorTypePropertyId, final Integer connectorTypeId, final String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.DELETE_PROPERTY_CONNECTOR_TYPE, UNAUTHORIZED_EXCEPTION);
        // Validate and get the connector type
        ConnectorType connectorType = this.iPhysicalConnectorValidator.validateConnectorType(connectorTypeId);

        // Validate the connector type property name
        ConnectorTypeProperty connectorTypeProperty = this.iPhysicalConnectorValidator.validateConnectorTypeProperty(connectorTypePropertyId);

        // Remove all references in all logical connector of this property
        this.deleteConnectorTypePropertyReferences(connectorType.getName(), connectorTypeProperty, ivUser);

        // Remove from connector type property from connector type
        connectorType.getConnectorTypeProperties().remove(connectorTypeProperty);

        // Delete the connector type property
        this.connectorTypePropertyRepository.delete(connectorTypeProperty);

        return connectorTypeProperty;
    }

    @Override
    @Transactional
    public ConnectorType deleteConnectorType(final Integer connectorTypeId, String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.DELETE_CONNECTOR_TYPE, UNAUTHORIZED_EXCEPTION);
        // Validate and get the connector type
        ConnectorType connectorType = this.iPhysicalConnectorValidator.validateConnectorType(connectorTypeId);

        // Validate if the connector type can be deleted
        this.iPhysicalConnectorValidator.isConnectorTypeRemovable(connectorType.getName());

        // Delete the connector type
        this.connectorTypeRepository.deleteById(connectorTypeId);

        return connectorType;
    }

    @Override
    public PhysicalConnector editPhysicalConnector(final EditPhysicalConnectorDto editPhysicalConnectorDto, final Integer physicalConnectorId, String ivUser)
    {
        ValidationUtils.verifyNotNull(editPhysicalConnectorDto, new NovaException(PhysicalConnectorError.getPhysicalConnectorRequiredError()));

        this.usersClient.checkHasPermission(ivUser, Constants.EDIT_PHYSICAL_CONNECTOR, UNAUTHORIZED_EXCEPTION);
        PhysicalConnector physicalConnector = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(physicalConnectorId);

        // Set new values for virtual IP, description and observations fields
        physicalConnector.setVirtualIp(editPhysicalConnectorDto.getVirtualIp());
        physicalConnector.setVaguadaVirtualIp(editPhysicalConnectorDto.getVaguadaVirtualIp());
        physicalConnector.setDescription(editPhysicalConnectorDto.getEditDescription());
        physicalConnector.setObservations(editPhysicalConnectorDto.getEditObservation());

        // Manage the created/deletions
        this.iPhysicalConnectorBuilder.editAndManagePhysicalConnectorPort(editPhysicalConnectorDto.getPorts(), physicalConnector);

        return physicalConnector;
    }

    @Override
    public Integer deletePhysicalConnector(final Integer physicalConnectorId, final String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.DELETE_PHYSICAL_CONNECTOR, UNAUTHORIZED_EXCEPTION);

        // Validate physical connector id
        PhysicalConnector physicalConnector = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(physicalConnectorId);

        // Check that there is not any logical connector associated to the physical connector
        this.iPhysicalConnectorValidator.validatePhysicalConnectorCanBeDeleted(physicalConnector);

        // Create the to do task "DELETE_CONNECTOR_REQUEST"
        return this.createDeletionConnectorTodoTask(ivUser, physicalConnector);
    }

    @Override
    @Transactional
    public ConnectorType createNewConnectorType(final NewConnectorTypeDto newConnectorTypeDto, String ivUser)
    {
        ValidationUtils.verifyNotNull(newConnectorTypeDto, new NovaException(PhysicalConnectorError.getConnectorTypeRequiredError()));
        this.usersClient.checkHasPermission(ivUser, Constants.CREATE_CONNECTOR_TYPE, UNAUTHORIZED_EXCEPTION);

        // Check if the connector type is really new
        this.iPhysicalConnectorValidator.validateNewConnectorType(newConnectorTypeDto.getPhysicalConnectorTypeName());

        // Create the connector type
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName(newConnectorTypeDto.getPhysicalConnectorTypeName().toUpperCase());
        connectorType.setDescription(newConnectorTypeDto.getPhysicalConnectorTypeDescription());

        // Save the connector type into the BBDD
        this.connectorTypeRepository.saveAndFlush(connectorType);

        return connectorType;
    }

    @Override
    @Transactional
    public void disassociateConnectors(final Integer physicalConnectorId, final Integer logicalConnectorId, String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.DISASSOCIATE_CONNECTORS, UNAUTHORIZED_EXCEPTION);
        // Validate both logical connector and physical connector exists
        PhysicalConnector physicalConnector = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(physicalConnectorId);
        LogicalConnector logicalConnector = this.iPhysicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);

        // Validate that the logical connector can be disassociated
        this.iPhysicalConnectorValidator.validateIfConnectorsCanBeDisassociated(logicalConnector, physicalConnector);

        String physicalConnectorPortName = logicalConnector.getPhysicalConnectorPort().getPortName();

        // Remove the association in the physical connector and physical, connector port, set new status of the logical connector to CREATING and set the new message observation
        logicalConnector.setPhysicalConnector(null);
        logicalConnector.setPhysicalConnectorPort(null);
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATING);
        logicalConnector.setObservations("Este conector ha sido desasociado del conector fisico: ["
                + physicalConnector.getName() + "] y puerto físico con nombre: [" + physicalConnectorPortName + "]"
                + " por los administradores de la plataforma NOVA. Queda a la espera de ser asociado o puede eliminar el conector del producto si prefiere.");

        LOG.debug("[{}] -> [{}]: disassociated the physical connector: [{}] - physical connector port: [{}] from the logical connector: [{}].",
                Constants.PHYSICAL_CONNECTOR_API, Constants.DISASSOCIATE_CONNECTORS_METHOD, physicalConnector.getLogicalConnectors(), physicalConnectorPortName, logicalConnector.getName());

        // Save the logical connector
        this.logicalConnectorRepository.save(logicalConnector);
    }

    @Override
    @Transactional
    public void associateConnectors(final Integer physicalConnectorId, final Integer logicalConnectorId, final Integer physicalConnectorPortId, String ivUser)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.ASSOCIATE_CONNECTORS, UNAUTHORIZED_EXCEPTION);
        // Validate both logical connector and physical connector exists
        PhysicalConnector physicalConnector = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(physicalConnectorId);
        LogicalConnector logicalConnector = this.iPhysicalConnectorValidator.validateAndGetLogicalConnector(logicalConnectorId);
        PhysicalConnectorPort physicalConnectorPort = this.iPhysicalConnectorValidator.validateAndGetPhysicalConnectorPort(physicalConnectorPortId);

        // Validate both logical connector and physical connector can be associated
        this.iPhysicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort);

        // Save the logical connector into the physical connector list
        physicalConnector.getLogicalConnectors().add(logicalConnector);
        this.physicalConnectorRepository.save(physicalConnector);

        // Associate the physical connector to the logical connector
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);

        // Check if there are 'Creation logical connector' to-do task in DONE status. This means that the logical connector has been created before. If true, update the logical connector status to CREATED
        if (this.iPhysicalConnectorValidator.checkCreationLogicalConnectorTodoTask(logicalConnector))
        {
            logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
            logicalConnector.setDescription("El conector ha sido asociado correctamente");
        }

        this.logicalConnectorRepository.save(logicalConnector);

        LOG.debug("[{}] -> [{}]: the logical connector name: [{}] has been associated to physical connector name: [{}] - physical connector port: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, Constants.ASSOCIATE_CONNECTORS_METHOD, logicalConnector.getName(), physicalConnector.getName(), physicalConnectorPort.getPortName());
    }

    ////////////////////////////////////// PRIVATE METHODS   ////////////////////////////////////// /////////////////////////////////////////////////////////////

    /**
     * Update and add the new connector type property to all the logical connectors
     *
     * @param connectorTypeName     the connector type name to filter
     * @param connectorTypeProperty the new connector type property to add to all logical connectors
     * @param userCode              the user code requester
     */
    private void updateAllLogicalConnectorsProperties(final String connectorTypeName, final ConnectorTypeProperty connectorTypeProperty, final String userCode)
    {
        // Find all the logical connectors that are using the connector type
        List<LogicalConnector> logicalConnectorList = this.logicalConnectorRepository.findByConnectorTypeName(connectorTypeName);
        LOG.debug("[{}] -> [updateAllLogicalConnectorsProperties]: Get the following logical connectors filter by connector type name: [{}] to updated the properties: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, connectorTypeName, logicalConnectorList);

        for (LogicalConnector logicalConnector : logicalConnectorList)
        {
            if (Environment.PRO.getEnvironment().equals(logicalConnector.getEnvironment())
                    || !connectorTypeProperty.getCpdName().equalsIgnoreCase(Constants.V_CPD_NAME))
            {
                this.updateLogicalConnectorProperty(connectorTypeProperty, userCode, logicalConnector);
            }

        }
    }

    /**
     * Update a logical connector property into the logical connector
     *
     * @param connectorTypeProperty the connector property to update
     * @param userCode              the user requester
     * @param logicalConnector      the logical connector to update the the logical connector
     *                              property
     */
    private void updateLogicalConnectorProperty(final ConnectorTypeProperty connectorTypeProperty, final String userCode, final LogicalConnector logicalConnector)
    {
        // Create the new logical connector property with the new connector type property
        LogicalConnectorProperty logicalConnectorProperty = new LogicalConnectorProperty();

        // Modify the name of the connector type property as the patter = 'LOGICAL_CONNECTOR_NAMNE'_'CONNECTOR_TYPE_PROPERTY_NAME'
        logicalConnectorProperty.setDescription(connectorTypeProperty.getDescription());
        logicalConnectorProperty.setManagement(connectorTypeProperty.getManagement());
        logicalConnectorProperty.setPropertyType(connectorTypeProperty.getPropertyType());
        logicalConnectorProperty.setScope(connectorTypeProperty.getScope());
        logicalConnectorProperty.setSecurity(connectorTypeProperty.isSecurity());
        logicalConnectorProperty.setDefaultName(MessageFormat.format("{0}.{1}", logicalConnector.getName().toUpperCase(), connectorTypeProperty.getName()));
        logicalConnectorProperty.setName(MessageFormat.format("{0}.{1}", logicalConnector.getName().toUpperCase(), connectorTypeProperty.getName()));

        // Save the new logical connector property into BBDD
        this.logicalConnectorPropertyRepository.saveAndFlush(logicalConnectorProperty);

        LOG.debug("[{}] -> [updateAllLogicalConnectorsProperties]: added and saved the new logical connector property: [{}]", Constants.PHYSICAL_CONNECTOR_API, logicalConnectorProperty);

        // Add to the logical connector property list
        logicalConnector.getLogConnProp().add(logicalConnectorProperty);

        LOG.debug("[{}] -> [updateAllLogicalConnectorsProperties]: updated the logical connector: [{}] and saved into BBDD with the new logical connector property list: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, logicalConnector, logicalConnector.getLogConnProp());

        // Add the new logical connector property to the deployment service (create a new deployment connector property and add it to the list of the current configuration revision)
        // And add a new deployment change
        for (DeploymentService deploymentService : logicalConnector.getDeploymentServices())
        {
            // Get the deployment plan, current configuration revision and
            // create the new DeploymentConnectorProperty instance
            DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
            ConfigurationRevision configurationRevision = deploymentPlan.getCurrentRevision();
            DeploymentConnectorProperty deploymentConnectorProperty = new DeploymentConnectorProperty();

            deploymentConnectorProperty.setRevision(configurationRevision);
            deploymentConnectorProperty.setDeploymentService(deploymentService);
            deploymentConnectorProperty.setLogicalConnectorProperty(logicalConnectorProperty);

            // Add the new deployment connector property to the deployment
            // connector property list of the current configuration revision
            configurationRevision.getDeploymentConnectorProperties().add(deploymentConnectorProperty);

            // Add the new deployment change for this deployment plan
            String deploymentChangeDescription = "La propiedad del conector: [" + logicalConnector.getName() + "] con nombre: [" + logicalConnectorProperty.getDefaultName() + "] ha sido creada. ";
            this.createDeploymentChange(deploymentPlan, configurationRevision, ChangeType.CREATE_LOGICAL_CONNECTOR_PROPERTY, deploymentChangeDescription, userCode);
        }
    }

    /**
     * Delete all the connector type property references in all the logical connectors. Does not matter the status or the deployment status of the logical connector
     *
     * @param connectorTypeName     connector type name to remove the property
     * @param connectorTypeProperty connector type property to remove
     * @param userCode              the user requester
     */
    private void deleteConnectorTypePropertyReferences(final String connectorTypeName, final ConnectorTypeProperty connectorTypeProperty, final String userCode)
    {

        // Find all the logical connectors that are using the connector type
        List<LogicalConnector> logicalConnectorList = this.logicalConnectorRepository.findByConnectorTypeName(connectorTypeName);
        LOG.debug("[{}] -> [deleteConnectorTypePropertyReferences]: Get the following logical connectors filter by connector type name: [{}] to updated the properties: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, connectorTypeName, logicalConnectorList);

        // Find the logical connector and the logical connector property to remove
        for (LogicalConnector logicalConnector : logicalConnectorList)
        {
            // Get the logical connector properties list
            List<LogicalConnectorProperty> logicalConnectorPropertiesList = logicalConnector.getLogConnProp();

            // Remove the logical connector property that match with the connector type property. First create the original name of the property
            String logicalConnectorPropertyName = MessageFormat.format("{0}.{1}", logicalConnector.getName().toUpperCase(), connectorTypeProperty.getName());

            Optional<LogicalConnectorProperty> logicalConnectorProperty = logicalConnector.getLogConnProp().stream().filter(logicalConnectorProperty1 -> logicalConnectorProperty1.getName()
                    .equalsIgnoreCase(logicalConnectorPropertyName)).findFirst();

            if (logicalConnectorProperty.isPresent())
            {
                // Remove the property from the deployment connector property
                this.iPhysicalConnectorRepositoriesService.deleteDeploymentConnectorProperty(logicalConnectorProperty.get());

                // Remove the property from the logical connector
                boolean result = logicalConnectorPropertiesList.removeIf(logicalConnectorProperty2 -> logicalConnectorProperty2.getName().equalsIgnoreCase(logicalConnectorPropertyName));
                LOG.trace("[{}] -> [deleteConnectorTypePropertyReferences]: trying to remove from the logical connector properties list: [{}] the connector type property: [{}] Results: [{}]",
                        Constants.PHYSICAL_CONNECTOR_API, logicalConnectorPropertiesList, connectorTypeProperty, result);

                // Save the logical connector into BBDD
                this.logicalConnectorRepository.save(logicalConnector);

                LOG.debug("[{}] -> [deleteConnectorTypePropertyReferences]: updated the logical connector: [{}] and saved into BBDD  removing the connector type property: [{}] from the logical " +
                        "connector property list: [{}]", Constants.PHYSICAL_CONNECTOR_API, logicalConnector, connectorTypeProperty, logicalConnectorPropertiesList);

                // Update the historic deployment plan for each service associated to logical connector
                for (DeploymentService deploymentService : logicalConnector.getDeploymentServices())
                {
                    String deploymentChangeDescription = "La propiedad del conector: [" + logicalConnector.getName()
                            + "] con nombre: [" + logicalConnectorProperty.get().getDefaultName()
                            + "] ha sido eliminada. ";

                    DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
                    this.createDeploymentChange(deploymentPlan, deploymentPlan.getCurrentRevision(), ChangeType.DELETE_LOGICAL_CONNECTOR_PROPERTY, deploymentChangeDescription, userCode);
                }
            }
        }
    }

    /**
     * Create the to do task delete connector request task
     *
     * @param ivUser            user that generates the to do task
     * @param physicalConnector physical connector to get the details for deleting
     * @return id of the to do task created
     */
    private Integer createDeletionConnectorTodoTask(final String ivUser, final PhysicalConnector physicalConnector)
    {

        String todoTaskDescription = " solicita eliminar el conector físico con las siguientes características:"
                + java.lang.System.getProperty("line.separator") + " Nombre: ["
                + physicalConnector.getName().toUpperCase() + "]" + java.lang.System.getProperty("line.separator")
                + " Tipo: [" + physicalConnector.getConnectorType().getName().toUpperCase() + "]"
                + java.lang.System.getProperty("line.separator") + " Descripción: ["
                + physicalConnector.getDescription() + "]" + java.lang.System.getProperty("line.separator")
                + " Entorno: [" + physicalConnector.getEnvironment().toUpperCase() + "]";

        return this.todoTaskServiceClient.createManagementTask(ivUser, null,
                ToDoTaskType.DELETION_PHYSICAL_CONNECTOR_REQUEST.name(), RoleType.PLATFORM_ADMIN.name(),
                todoTaskDescription, null, physicalConnector.getId());
    }

    /**
     * Create new deploymentPlan change if the deploymentPlan plan is DEPLOYED
     *
     * @param deploymentPlan          the deploymentPlan plan to save the change
     * @param configurationRevision   the configuration revision to save
     * @param deployChangeDescription deployChangeDescription of the deployment change
     * @param userCode                user that makes the change
     */
    private void createDeploymentChange(final DeploymentPlan deploymentPlan, final ConfigurationRevision configurationRevision, final ChangeType changeType,
                                        final String deployChangeDescription, final String userCode)
    {
        if (deploymentPlan.getStatus() == DeploymentStatus.DEPLOYED)
        {
            DeploymentChange deploymentChange = new DeploymentChange(deploymentPlan, deployChangeDescription, Calendar.getInstance(), changeType, userCode, configurationRevision, null);
            deploymentPlan.getChanges().add(deploymentChange);

            this.deploymentChangeRepository.saveAndFlush(deploymentChange);
            LOG.debug("[{}] -> [createDeploymentChange]: created and saved a new deployment deploymentChange with the following parameters: [{}]", Constants.PHYSICAL_CONNECTOR_API, deploymentChange);
        }
    }
}

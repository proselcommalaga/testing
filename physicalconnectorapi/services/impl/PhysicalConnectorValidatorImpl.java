package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.*;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.connector.enumerates.PhysicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception.PhysicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorValidator;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * Class dedicated to validate operations of the physical connector
 */
@Service
public class PhysicalConnectorValidatorImpl implements IPhysicalConnectorValidator
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalConnectorValidatorImpl.class);

    /**
     * Range max port num
     */
    private static final int RANGE_MAX_PORT_NUM = 65535;

    /**
     * Physical Connector repository
     */
    private final PhysicalConnectorRepository physicalConnectorRepository;

    /**
     * Connector type repository
     */
    private final ConnectorTypeRepository connectorTypeRepository;

    /**
     * CPD repository
     */
    private final CPDRepository cpdRepository;

    /**
     * Connector type property repository
     */
    private final ConnectorTypePropertyRepository connectorTypePropertyRepository;

    /**
     * Management action task repository
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;

    /**
     * Logical Connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Physical connector port repository
     */
    private final PhysicalConnectorPortRepository physicalConnectorPortRepository;

    /**
     * Dependency injection constructor
     *
     * @param physicalConnectorRepository     physicalConnectorRepository
     * @param connectorTypeRepository         connectorTypeRepository
     * @param cpdRepository                   cpdRepository
     * @param connectorTypePropertyRepository connectorTypePropertyRepository
     * @param managementActionTaskRepository  managementActionTaskRepository
     * @param logicalConnectorRepository      logicalConnectorRepository
     * @param physicalConnectorPortRepository physicalConnectorPortRepository
     */
    @Autowired
    public PhysicalConnectorValidatorImpl(final PhysicalConnectorRepository physicalConnectorRepository,
                                          final ConnectorTypeRepository connectorTypeRepository, final CPDRepository cpdRepository,
                                          final ConnectorTypePropertyRepository connectorTypePropertyRepository,
                                          final ManagementActionTaskRepository managementActionTaskRepository,
                                          final LogicalConnectorRepository logicalConnectorRepository,
                                          final PhysicalConnectorPortRepository physicalConnectorPortRepository)
    {

        this.physicalConnectorRepository = physicalConnectorRepository;
        this.connectorTypeRepository = connectorTypeRepository;
        this.cpdRepository = cpdRepository;
        this.connectorTypePropertyRepository = connectorTypePropertyRepository;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.physicalConnectorPortRepository = physicalConnectorPortRepository;
    }

    /////////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Transactional(readOnly = true)
    public PhysicalConnector validateAndGetPhysicalConnector(final Integer physicalConnectorId)
    {

        PhysicalConnector physicalConnector = this.physicalConnectorRepository.findById(physicalConnectorId).orElse(null);

        if (physicalConnector == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateAndGetPhysicalConnector]: the physicalConnector ID: ["
                    + physicalConnectorId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchPhysicalConnectorError(), message);
        }

        return physicalConnector;
    }

    @Override
    public void validateConnectorTypePropertyName(final ConnectorType connectorType, String propertyName)
    {

        List<ConnectorTypeProperty> connectorTypePropertyList = connectorType.getConnectorTypeProperties();

        // Check with the final name of the connector property name, follow the
        // patter: '"CONNECTOR_TYPE_NAME"_"connector type name"'
        String connectorTypePropertyName = MessageFormat.format("{0}.{1}", connectorType.getName().toUpperCase(),
                propertyName);

        for (ConnectorTypeProperty connectorTypeProperty : connectorTypePropertyList)
        {
            if (connectorTypeProperty.getName().equalsIgnoreCase(connectorTypePropertyName))
            {
                String message = "[PhysicalConnectorAPI] -> [validateConnectorTypePropertyName]: the connector type property name: ["
                        + connectorType + "] is " + "already exists into the connector type: ["
                        + connectorType.getName() + "]";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getDuplicatedConnectorTypePropertyNameError(),
                        message);
            }
        }

        LOG.debug(
                "[{}] -> [validateConnectorTypePropertyName]: the connector type property name: [{}]"
                        + " does not exists into the connector type name: [{}]. Results OK.",
                Constants.PHYSICAL_CONNECTOR_API, connectorTypePropertyName, connectorType.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectorTypeProperty validateConnectorTypeProperty(final Integer connectorTypePropertyId)
    {

        ConnectorTypeProperty connectorTypeProperty = this.connectorTypePropertyRepository
                .findById(connectorTypePropertyId).orElse(null);

        if (connectorTypeProperty == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateConnectorTypeProperty]: the connector type property id: ["
                    + connectorTypePropertyId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchConnectorTypePropertyIdError(), message);
        }
        else
        {
            LOG.debug(
                    "[{}] -> [validateConnectorTypeProperty]: the connector type property id: [{}]"
                            + " exists on the NOVA BBDD. Results OK.",
                    Constants.PHYSICAL_CONNECTOR_API, connectorTypePropertyId);
        }

        return connectorTypeProperty;
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectorType validateConnectorType(final String connectorTypeName)
    {

        ConnectorType connectorType = this.connectorTypeRepository.findByName(connectorTypeName);

        if (connectorType == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateConnectorType]: the connector type name: ["
                    + connectorTypeName + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchConnectorTypeError(), message);
        }
        else
        {
            LOG.debug("[{}] -> [validateConnectorType]: the connector type: [{}] has been validated. Results OK.",
                    Constants.PHYSICAL_CONNECTOR_API, connectorTypeName);
        }

        return connectorType;
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectorType validateConnectorType(final Integer connectorTypeId)
    {

        ConnectorType connectorType = this.connectorTypeRepository.findById(connectorTypeId).orElse(null);

        if (connectorType == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateConnectorType]: the connector type ID: ["
                    + connectorTypeId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchConnectorTypeError(), message);
        }
        else
        {
            LOG.debug("[{}] -> [validateConnectorType]: the connector type ID: [{}] has been validated. Results OK.",
                    Constants.PHYSICAL_CONNECTOR_API, connectorTypeId);
        }

        return connectorType;
    }

    @Override
    @Transactional(readOnly = true)
    public String validatePhysicalConnectorName(final String physicalConnectorNameDTO, final String environment)
    {
        PhysicalConnector physicalConnector = this.physicalConnectorRepository.findByEnvironmentAndName(environment, physicalConnectorNameDTO);

        // Check physical connector instance
        if (physicalConnector == null)
        {
            LOG.debug("[{}] -> [validatePhysicalConnectorName]: the physical connector name: [{}] has been validated. Results: OK.", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorNameDTO);
        }
        else
        {
            throw new NovaException(PhysicalConnectorError.getDuplicatedPhysicalConnectorNameError(physicalConnector.getName(), environment));
        }

        return physicalConnectorNameDTO;
    }

    @Override
    public void validatePortRange(final String port)
    {

        try
        {
            int portNumber = Integer.parseInt(port);

            if (portNumber < 0 || portNumber > RANGE_MAX_PORT_NUM)
            {
                String message = "[PhysicalConnectorAPI] -> [validatePortRange]: the number port: [" + portNumber
                        + "] is not in the range of the allowed ports. Number port must be between 0-65535";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getRangeNumberPortNotValidError(), message);
            }
            else
            {
                LOG.debug("[{}] -> [validatePortRange]: the port number [{}] has been validated successfully.",
                        Constants.PHYSICAL_CONNECTOR_API, port);
            }
        }
        catch (NumberFormatException e)
        {
            String message = "[PhysicalConnectorAPI] -> [validatePortRange]: the outport port value: [" + port
                    + "] is not valid. It must be a number between 0-65535";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNumberPortNotValidError(), message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void isConnectorTypeRemovable(final String connectorTypeName)
    {

        List<LogicalConnector> logicalConnectorList = this.logicalConnectorRepository
                .findByConnectorTypeName(connectorTypeName);

        if (logicalConnectorList.isEmpty())
        {
            // Means there is not any logical connector using this kind of the
            // connector type
            LOG.debug(
                    "[{}] -> [isConnectorTypeRemovable]: The logical connector list for this connector type name: [{}] is empty.",
                    Constants.PHYSICAL_CONNECTOR_API, connectorTypeName);
        }
        else
        {
            String message = "[PhysicalConnectorAPI] -> [isConnectorTypeRemovable]: the connector type name: ["
                    + connectorTypeName + "] has been used by" + "the following number of the logical connector: ["
                    + logicalConnectorList.size() + "]";
            LOG.error(message);
            throw new NovaException(
                    PhysicalConnectorError.getUnableDeleteConnectorTypeDueLogicalConnectorError(), message);
        }

        List<PhysicalConnector> physicalConnectorList = this.physicalConnectorRepository
                .findByConnectorTypeName(connectorTypeName);

        if (physicalConnectorList.isEmpty())
        {
            // Means there is not any physical connector using this kind of the
            // connector type
            LOG.debug(
                    "[{}] -> [isConnectorTypeRemovable]: the physical connector list for this connector type name: [{}] is empty.",
                    Constants.PHYSICAL_CONNECTOR_API, connectorTypeName);
        }
        else
        {
            String message = "[PhysicalConnectorAPI] -> [isConnectorTypeRemovable]: the connector type name: ["
                    + connectorTypeName + "] has been used by" + "the following number of the physical connector: ["
                    + physicalConnectorList.size() + "]";
            LOG.error(message);
            throw new NovaException(
                    PhysicalConnectorError.getUnableDeleteConnectorTypeDuePhysicalConnectorError(), message);
        }
    }

    @Override
    public void validateIfConnectorsCanBeAssociated(final LogicalConnector logicalConnector,
                                                    final PhysicalConnector physicalConnector, final PhysicalConnectorPort physicalConnectorPort)
    {

        // Validate if the physical connector is on CREATED status
        if (physicalConnector.getPhysicalConnectorStatus() != PhysicalConnectorStatus.CREATED)
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeAssociated]: the physical connector status is:"
                    + " [" + physicalConnector.getPhysicalConnectorStatus()
                    + "]. A physical connector can only be associated in 'CREATED' status.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getPendingPortPhysicalConnectorStatusError(),
                    message);
        }

        // Validate if the logical connector does not have a ERROR status
        if (logicalConnector.getLogicalConnectorStatus() == LogicalConnectorStatus.CREATE_ERROR)
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeAssociated]: the logical connector status is:"
                    + " [" + logicalConnector.getLogicalConnectorStatus()
                    + "]. A logical connector cannot be associated in 'CREATE_ERROR' status.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getUnableToAssociateLogicalConnectorErrorStatusError(),
                    message);
        }

        // Validate that the physical connector has not to do task
        this.checkPendingDeletePhysicalConnectorTodoTask(physicalConnector);

        if (logicalConnector.getPhysicalConnector() != null && logicalConnector.getPhysicalConnectorPort() != null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeAssociated]: the logical connector name: ["
                    + logicalConnector.getName() + "]" + " is already associated to the physical connector: ["
                    + logicalConnector.getPhysicalConnector().getName() + "] to the physical connector port: ["
                    + logicalConnector.getPhysicalConnectorPort().getPortName() + "]";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getUnableToAssociateLogicalConnectorError(), message);
        }

        if (!physicalConnector.getEnvironment().equals(logicalConnector.getEnvironment()))
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeAssociated]: the logical connector name: ["
                    + logicalConnector.getName() + "]" + " has different environment: ["
                    + logicalConnector.getEnvironment() + "] than the physical connector: ["
                    + physicalConnector.getName() + "] " + "- environment: ["
                    + physicalConnector.getEnvironment() + "]";
            LOG.error(message);
            throw new NovaException(
                    PhysicalConnectorError.getUnableToAssociateLogicalConnectorDueEnvironmentError(), message);
        }

        if (!physicalConnector.getConnectorType().getName().equals(logicalConnector.getConnectorType().getName()))
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeAssociated]: the logical connector name: ["
                    + logicalConnector.getName() + "]" + " has different connector type: ["
                    + logicalConnector.getConnectorType().getName() + "] than the physical connector: ["
                    + physicalConnector.getName() + "] " + "- connector type: ["
                    + physicalConnector.getConnectorType().getName() + "]";
            LOG.error(message);
            throw new NovaException(
                    PhysicalConnectorError.getUnableToAssociateLogicalConnectorDueConnectorTypeError(), message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PhysicalConnectorPort validateAndGetPhysicalConnectorPort(int physicalConnectorPortId)
    {

        PhysicalConnectorPort physicalConnectorPort = this.physicalConnectorPortRepository
                .findById(physicalConnectorPortId).orElse(null);

        if (physicalConnectorPort == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateAndGetPhysicalConnectorPort]: the physical connector port ID: ["
                    + physicalConnectorPortId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchPhysicalConnectorPortError(), message);
        }

        return physicalConnectorPort;
    }

    @Override
    public void validateIfConnectorsCanBeDisassociated(final LogicalConnector logicalConnector, final PhysicalConnector physicalConnector)
    {

        // Validate that the logical connector has associated previously some
        // physical connector
        if (logicalConnector.getPhysicalConnector() == null
                || !logicalConnector.getPhysicalConnector().equals(physicalConnector))
        {
            String message = "[PhysicalConnectorAPI] -> [validateIfConnectorsCanBeDisassociated]: the logical connector name: ["
                    + logicalConnector.getName() + "]" + " is not associated to the physical connector: ["
                    + physicalConnector.getName() + "].";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getUnableToDisassociateLogicalConnectorError(),
                    message);
        }

        // Validate that the logical connector does not have to do task of type
        // CHECK_PROPERTIES in pending status
        this.checkPendingCheckPropertyTodoTask(logicalConnector);

        // Validate that the logical connector is not being used at the moment
        this.checkLogicalConnectorServiceStatus(logicalConnector, physicalConnector.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public LogicalConnector validateAndGetLogicalConnector(int logicalConnectorId)
    {

        LogicalConnector logicalConnector = this.logicalConnectorRepository.findById(logicalConnectorId).orElse(null);

        if (logicalConnector == null)
        {
            String message = "[PhysicalConnectorAPI] -> [validateAndGetLogicalConnector]: the logicalConnector ID: ["
                    + logicalConnectorId + "] does not exists into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoSuchLogicalConnectorError(), message);
        }

        return logicalConnector;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateNewConnectorType(final String connectorTypeName)
    {
        // Check if the connector type is already exist into NOVA data base
        ConnectorType connectorType = this.connectorTypeRepository.findByName(connectorTypeName.toUpperCase());

        if (connectorType == null)
        {
            LOG.debug("[{}] -> [validateNewConnectorType]: The type name: [{}] does not exist. Continue.", Constants.PHYSICAL_CONNECTOR_API, connectorTypeName);
        }
        else
        {
            String message = "[PhysicalConnectorAPI] -> [validateNewConnectorType]: the type name: [" + connectorType.getName() + "] " + " is already into NOVA BBDD.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getDuplicatedConnectorTypeError(), message);
        }
    }

    @Override
    public void validateNewPhysicalConnectorPort(final PhysicalConnector physicalConnector, final ConnectorPortInfo connectorPortInfo)
    {

        LOG.debug(
                "[{}] -> [validateNewPhysicalConnectorPort]: Validating a new physical connector port: [{}] in the physical connector name: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, connectorPortInfo, physicalConnector.getName());

        for (PhysicalConnectorPort physicalConnectorPort : physicalConnector.getConnectorPortList())
        {
            // Check port name, it can not be repeated
            if (physicalConnectorPort.getPortName().equals(connectorPortInfo.getPortName()))
            {
                String message = "[PhysicalConnectorAPI] -> [validateNewPhysicalConnectorPort]: the input port name: ["
                        + connectorPortInfo.getPortName() + "] "
                        + " is already in use in the physical connector name: [" + physicalConnector.getName() + "].";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getInputPortNameAlreadyInUseError(), message);
            }

            // Check input port value, it can not be repeated
            if (physicalConnectorPort.getInputPort() == connectorPortInfo.getInputPort())
            {
                String message = "[PhysicalConnectorAPI] -> [validateNewPhysicalConnectorPort]: the input port: ["
                        + connectorPortInfo.getInputPort() + "] "
                        + " is already in use by the physical connector name: [" + physicalConnector.getName()
                        + "] by the " + " physical connector port name: [" + physicalConnectorPort.getPortName() + "]";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getInputPortValueAlreadyInUseError(), message);
            }
        }

        LOG.debug(
                "[{}] -> [validateNewPhysicalConnectorPort]: Validated. The physical connector port: [{}] in the physical connector port name: [{}] is not assigned in the physical connector port list.",
                Constants.PHYSICAL_CONNECTOR_API, connectorPortInfo, physicalConnector.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePhysicalConnectorCanBeDeleted(final PhysicalConnector physicalConnector)
    {

        // Validate that is already create the to do task
        // DELETE_PHYSICAL_CONNECTOR
        this.checkPendingDeletePhysicalConnectorTodoTask(physicalConnector);

        // Validate that the physical connector does not have any logical
        // connector associated
        if (!physicalConnector.getLogicalConnectors().isEmpty())
        {
            String message = "[PhysicalConnectorAPI] -> [validatePhysicalConnectorCanBeDeleted]: the physical connector name: ["
                    + physicalConnector + "] "
                    + " can not be deleted due has the following number of logical connector associated yet: ["
                    + physicalConnector.getLogicalConnectors().size() + "]";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getUnableToDeletePhysicalConnectorError(), message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer checkPendingDeletePhysicalConnector(final PhysicalConnector physicalConnector)
    {

        Integer toDoTaskId = null;

        List<ManagementActionTask> checkDeletionPhysicalConnectorTaskList = this.managementActionTaskRepository
                .findByRelatedIdAndTaskTypeAndStatusIn(physicalConnector.getId(),
                        ToDoTaskType.DELETION_PHYSICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.debug(
                "[{}] -> [checkPendingDeletePhysicalConnector]: checked the deletion physical connector request todo task list in PENDING or PENDING_ERROR status associated to physical connector: [{}]-ENV:[{}]. Results: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getName(), physicalConnector.getEnvironment(),
                checkDeletionPhysicalConnectorTaskList);

        if (checkDeletionPhysicalConnectorTaskList == null || checkDeletionPhysicalConnectorTaskList.isEmpty())
        {
            LOG.debug(
                    "[{}] -> [checkPendingDeletePhysicalConnector]: Physical connector [{}]:ENV:[{}] - does not have delete physical to do task "
                            + "associated in 'PENDING or PENDING_ERROR' status.",
                    Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getName(), physicalConnector.getEnvironment());
        }
        else
        {
            // Must only have one to do task of type management action task =
            // check logical connector properties
            toDoTaskId = checkDeletionPhysicalConnectorTaskList.get(0).getId();
            LOG.debug(
                    "[{}] -> checkPendingDeletePhysicalConnector: the physical connector [{}]:ENV:[{}] - has 'delete physical connector properties'"
                            + "to do task associated in 'PENDING or PENDING_ERROR' status. TodoTaskId: [{}]",
                    Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getName(), physicalConnector.getEnvironment(),
                    toDoTaskId);
        }

        return toDoTaskId;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkCreationLogicalConnectorTodoTask(final LogicalConnector logicalConnector)
    {

        boolean result = false;

        // Do query
        List<ManagementActionTask> checkConnectorPropertiesTaskList = this.managementActionTaskRepository
                .findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                        ToDoTaskType.CREATION_LOGICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.DONE));

        if (checkConnectorPropertiesTaskList == null || checkConnectorPropertiesTaskList.isEmpty())
        {
            LOG.debug(
                    "[{}] -> [checkCreationLogicalConnectorTodoTask]: Logical connector [{}]:ENV:[{}]:Product:[{}] - does not have creation logical connector to do task "
                            + "associated in 'DONE' status.",
                    Constants.PHYSICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(),
                    logicalConnector.getProduct().getName());
        }
        else
        {
            result = true;
            LOG.debug(
                    "[{}] -> [checkCreationLogicalConnectorTodoTask]: checked the creation logical connector todo task list in status DONE associated to logical connector: [{}]-ENV:[{}]-Product:[{}]. "
                            + "Results: [{}]",
                    Constants.PHYSICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(),
                    logicalConnector.getProduct().getName(), checkConnectorPropertiesTaskList);
        }

        return result;
    }

    @Override
    public void checkNewPhysicalConnectorInput(final NewPhysicalConnectorDto newPhysicalConnectorDto)
    {
        if(Objects.isNull(newPhysicalConnectorDto) ||
                StringUtils.isBlank(newPhysicalConnectorDto.getEnvironment()) ||
                StringUtils.isBlank(newPhysicalConnectorDto.getConnectorType()) ||
                StringUtils.isBlank(newPhysicalConnectorDto.getPhysicalConnectorName()) ||
                StringUtils.isBlank(newPhysicalConnectorDto.getVirtualIp())  )
        {
            throw new NovaException(PhysicalConnectorError.getPhysicalConnectorRequiredError());
        }

        LOG.debug("[{}] -> [checkNewPhysicalConnectorInput]: validated the new physical connector: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, newPhysicalConnectorDto);
    }
    ////////////////////////////////////// PRIVATE METHODS
    ////////////////////////////////////// ////////////////////////////////////////////////////////////

    /**
     * Check if a physical connector has delete physical connector to do task
     * type associated in PENDING or PENDING_ERROR status
     *
     * @param physicalConnector the logical connector to check
     */
    private void checkPendingDeletePhysicalConnectorTodoTask(final PhysicalConnector physicalConnector)
    {

        List<ManagementActionTask> checkDeletionPhysicalConnectorTaskList = this.managementActionTaskRepository
                .findByRelatedIdAndTaskTypeAndStatusIn(physicalConnector.getId(),
                        ToDoTaskType.DELETION_PHYSICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.debug(
                "[{}] -> [checkPendingDeletePhysicalConnectorTodoTask]: checked the deletion physical connector request todo task list in PENDING or PENDING_ERROR status associated to physical connector: [{}]-ENV:[{}]. Results: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getName(), physicalConnector.getEnvironment(),
                checkDeletionPhysicalConnectorTaskList);

        if (checkDeletionPhysicalConnectorTaskList == null || checkDeletionPhysicalConnectorTaskList.isEmpty())
        {
            LOG.debug(
                    "[{}] -> [checkPendingDeletePhysicalConnectorTodoTask]: Physical connector [{}]:ENV:[{}] - does not have delete physical to do task "
                            + "associated in 'PENDING or PENDING_ERROR' status.",
                    Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getName(), physicalConnector.getEnvironment());
        }
        else
        {
            String message = "[PhysicalConnectorAPI] -> [checkPendingDeletePhysicalConnectorTodoTask]: the physical Connector: ["
                    + physicalConnector.getName() + "] has DELETION_PHYSICAL_CONNECTOR_REQUEST in PENDING or PENDING_ERROR "
                    + "status thus it can not be associated";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getHasDeletePhysicalConnectorTaskPendingError(),
                    message);
        }
    }

    /**
     * Check if a logical connector has logical connector properties to do task
     * type associated in PENDING or PENDING_ERROR status
     *
     * @param logicalConnector the logical connector to check
     */
    private void checkPendingCheckPropertyTodoTask(final LogicalConnector logicalConnector)
    {

        List<ManagementActionTask> checkConnectorPropertiesTaskList = this.managementActionTaskRepository
                .findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                        ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR));
        LOG.debug(
                "[{}] -> [checkPendingCheckPropertyTodoTask]: checked the check logical connector properties todo task list 'PENDING or PENDING_ERROR' status associated to logical connector: [{}]-ENV:[{}]-Product:[{}]. Results: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(),
                logicalConnector.getProduct().getName(), checkConnectorPropertiesTaskList);

        if (checkConnectorPropertiesTaskList == null || checkConnectorPropertiesTaskList.isEmpty())
        {
            LOG.debug(
                    "[{}] -> [checkPendingCheckPropertyTodoTask]: Logical connector [{}]:ENV:[{}]:Product:[{}] - does not have to do task "
                            + "associated in 'PENDING or PENDING_ERROR' status.",
                    Constants.PHYSICAL_CONNECTOR_API, logicalConnector.getName(), logicalConnector.getEnvironment(),
                    logicalConnector.getProduct().getName());
        }
        else
        {
            String message = "[PhysicalConnectorAPI] -> [checkPendingCheckPropertyTodoTask]: the logical Connector: ["
                    + logicalConnector.getName() + "] has CHECK_LOGICAL_CONNECTOR_PROPERTIES in PENDING or PENDING_ERROR "
                    + "status thus it can not be disassociated";
            LOG.error(message);
            throw new NovaException(
                    PhysicalConnectorError.getHasCheckLogicalConnectorPropertyTaskPendingError(), message);
        }
    }

    /**
     * Check if the logical connector is being used at the moment. Check
     * DEPLOYED status of the services.
     *
     * @param logicalConnector      logical connector to use
     * @param physicalConnectorName physical connector associated
     */
    private void checkLogicalConnectorServiceStatus(final LogicalConnector logicalConnector, final String physicalConnectorName)
    {

        // Validate if the logical connector is being used by some service in
        // some deployment plan.
        for (DeploymentService deploymentService : logicalConnector.getDeploymentServices())
        {
            if (deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus() == DeploymentStatus.DEPLOYED
                    || deploymentService.getDeploymentSubsystem().getDeploymentPlan()
                    .getStatus() == DeploymentStatus.DEFINITION
                    || deploymentService.getDeploymentSubsystem().getDeploymentPlan()
                    .getStatus() == DeploymentStatus.STORAGED)
            {
                String message = "[PhysicalConnectorAPI] -> [checkLogicalConnectorServiceStatus]: error while disassociating ["
                        + logicalConnector.getName() + "] from [" + physicalConnectorName
                        + "]: logical connector is being used at the moment.";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getUnableToDisassociateLogicalConnectorError(),
                        message);
            }
        }
    }
}

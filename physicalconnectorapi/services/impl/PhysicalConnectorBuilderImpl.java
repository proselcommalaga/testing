package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.datamodel.model.connector.enumerates.PhysicalConnectorStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypePropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorPortRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception.PhysicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.listener.ListenerPhysicalconnectorapi;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.model.PhysicalConnectorPortAction;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorValidator;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class dedicate to build the physical connector DTOs
 *
 * @author BBVA - XE72018
 */
@Service
public class PhysicalConnectorBuilderImpl implements IPhysicalConnectorBuilder
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListenerPhysicalconnectorapi.class);

    /**
     * Physical connector validator service
     */
    private final IPhysicalConnectorValidator iPhysicalConnectorValidator;

    /**
     * Connector type property repository
     */
    private final ConnectorTypePropertyRepository connectorTypePropertyRepository;

    /**
     * Physical connector port repository
     */
    private final PhysicalConnectorPortRepository physicalConnectorPortRepository;

    /**
     * Physical connector repository
     */
    private final PhysicalConnectorRepository physicalConnectorRepository;

    /**
     * Port of the HA Proxy stats
     */
    @Value("${nova.connectors.monitoring.port}")
    private String connectorMonitoringPort;

    /**
     * URL of the HA Proxy stats
     */
    @Value("${nova.connectors.monitoring.url}")
    private String connectorMonitoringUrl;

    /**
     * Dependency injection constructor
     *
     * @param iPhysicalConnectorValidator     physical connector validator
     * @param connectorTypePropertyRepository connector type property repository
     * @param physicalConnectorPortRepository physical connector port repository
     * @param physicalConnectorRepository     physical connector repository
     */
    @Autowired
    public PhysicalConnectorBuilderImpl(final IPhysicalConnectorValidator iPhysicalConnectorValidator,
                                        final ConnectorTypePropertyRepository connectorTypePropertyRepository,
                                        final PhysicalConnectorPortRepository physicalConnectorPortRepository,
                                        final PhysicalConnectorRepository physicalConnectorRepository)
    {
        this.physicalConnectorPortRepository = physicalConnectorPortRepository;
        this.iPhysicalConnectorValidator = iPhysicalConnectorValidator;
        this.connectorTypePropertyRepository = connectorTypePropertyRepository;
        this.physicalConnectorRepository = physicalConnectorRepository;
    }


    ////////////////////////////////////////////// IMPLEMENTATIONS ///////////////////////////////////////////////////////

    @Override
    public PhysicalConnector buildNewPhysicalConnector(final NewPhysicalConnectorDto newPhysicalConnectorDto)
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();

        // Validate and set the logical connector name
        physicalConnector.setName(this.iPhysicalConnectorValidator.validatePhysicalConnectorName(newPhysicalConnectorDto.getPhysicalConnectorName(),
                newPhysicalConnectorDto.getEnvironment()));

        // Get the environment
        Environment environment = Environment.valueOf(newPhysicalConnectorDto.getEnvironment());

        // Set the list of logical connectors associated as empty
        physicalConnector.setLogicalConnectors(new ArrayList<>());

        // Set the physical port list and set the status of the physical connector.
        // At beginning, the physical port list is empty and the status of the physical connector is 'PENDING_PORT'
        physicalConnector.setConnectorPortList(new ArrayList<>());
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.PENDING_PORT);
        LOG.debug("[{}] -> [buildNewPhysicalConnector]: Physical connector port not established. The physical connector status is: [{}]", Constants.PHYSICAL_CONNECTOR_API,
                PhysicalConnectorStatus.PENDING_PORT.name());

        // Validate and set connector type
        physicalConnector.setConnectorType(this.iPhysicalConnectorValidator.validateConnectorType(newPhysicalConnectorDto.getConnectorType()));

        // Set the description and the environment
        physicalConnector.setDescription(newPhysicalConnectorDto.getPhysicalConnectorDescription());
        physicalConnector.setEnvironment(environment.getEnvironment());

        // The physical connectors are always multiCPD
        LOG.debug("[{}] -> [buildNewPhysicalConnector]: setting virutal IPs for CPD/CPDs", Constants.PHYSICAL_CONNECTOR_API);
        physicalConnector.setVirtualIp(newPhysicalConnectorDto.getVirtualIp());
        physicalConnector.setVaguadaVirtualIp(newPhysicalConnectorDto.getVaguadaVirtualIp());
        LOG.debug("[{}] -> [buildNewPhysicalConnector]: assigned TC virtual ip: [{}] and Vaguada virtual Ip: [{}].",
                Constants.PHYSICAL_CONNECTOR_API, newPhysicalConnectorDto.getVirtualIp(), newPhysicalConnectorDto.getVaguadaVirtualIp());

        // Set the observations
        physicalConnector.setObservations(newPhysicalConnectorDto.getObservations());

        return physicalConnector;
    }

    @Transactional
    @Override
    public void editAndManagePhysicalConnectorPort(final ConnectorPortInfo[] connectorPortInfoDTO, final PhysicalConnector physicalConnector)
    {
        LOG.debug("[{}] -> [editAndManagePhysicalConnectorPort]: managing the physical connector port list: [{}] for the physical connector name: [{}]", Constants.PHYSICAL_CONNECTOR_API,
                Arrays.toString(connectorPortInfoDTO), physicalConnector.getName());

        // First. Order the connector port info list by action
        List<ConnectorPortInfo> deletionConnectorPortList = new ArrayList<>();
        List<ConnectorPortInfo> creationConnectorPortList = new ArrayList<>();
        for (ConnectorPortInfo connectorPortInfo : connectorPortInfoDTO)
        {
            if (connectorPortInfo.getAction() != null && !connectorPortInfo.getAction().isEmpty())
            {
                // Manage the creation of a new physical connector port
                if (PhysicalConnectorPortAction.CREATE.name().equals(connectorPortInfo.getAction()))
                {
                    creationConnectorPortList.add(connectorPortInfo);
                }
                // Manage the deletion of a physical connector port
                if (PhysicalConnectorPortAction.DELETE.name().equals(connectorPortInfo.getAction()))
                {
                    deletionConnectorPortList.add(connectorPortInfo);
                }
            }
            else
            {
                // This physical connector port do nothing.
                LOG.debug("[{}] -> [editAndManagePhysicalConnectorPort]: this physical connector port: [{}] does not have action. Continue", Constants.PHYSICAL_CONNECTOR_API, connectorPortInfo);
            }
        }

        // Second. Remove the physical connector port from the list
        this.deletePhysicalConnectorPort(physicalConnector, deletionConnectorPortList);

        // Third. Create add to the physical connector the new physical connector ports from the list
        this.createAndSavePhysicalConnectorPort(physicalConnector, creationConnectorPortList);

        //Finally. Update the status of the physical connector depending on the port list. If the port list == 0, physical connector status = PENDING_PORT. If port list >= 1, CREATED
        if (physicalConnector.getConnectorPortList() == null || physicalConnector.getConnectorPortList().isEmpty())
        {
            String message = "[PhysicalConnectorAPI] -> [editPhysicalConnector]: Physical connector port has not been established. Any physical connector " +
                    "must have at least one o more physical connector port.";
            LOG.error(message);
            throw new NovaException(PhysicalConnectorError.getNoFoundPhysicalConnectorPortError(), message);
        }
        else
        {
            physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
            LOG.debug("[{}] -> [editPhysicalConnector]: Physical connector port established. The physical connector status is: [{}]", Constants.PHYSICAL_CONNECTOR_API,
                    PhysicalConnectorStatus.CREATED.name());
        }

        // Save the physical connector
        this.physicalConnectorRepository.save(physicalConnector);
    }

    @Override
    public PhysicalConnectorDto[] buildPhysicalConnectorDtoList(final List<PhysicalConnector> physicalConnectorList)
    {
        LOG.trace("[{}] -> [buildPhysicalConnectorDtoList]: Building a physical connector Dto array with values [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorList);

        PhysicalConnectorDto[] physicalConnectorDtos = new PhysicalConnectorDto[physicalConnectorList.size()];

        int i = 0;
        for (PhysicalConnector physicalConnector : physicalConnectorList)
        {
            physicalConnectorDtos[i] = this.buildPhysicalConnectorDto(physicalConnector);
            i++;
        }

        LOG.debug("[{}] -> [buildPhysicalConnectorDtoList]: Built physical connector Dto array with values [{}]", Constants.PHYSICAL_CONNECTOR_API, Arrays.toString(physicalConnectorDtos));

        return physicalConnectorDtos;
    }

    @Override
    public PhysicalConnectorDto buildPhysicalConnectorDto(final PhysicalConnector physicalConnector)
    {
        LOG.trace("[{}] -> [buildPhysicalConnectorDto]: building physical connector Dto with original values [{}]",
                Constants.PHYSICAL_CONNECTOR_API, physicalConnector);

        // Create the physicalConnectorDto
        PhysicalConnectorDto physicalConnectorDto = new PhysicalConnectorDto();

        physicalConnectorDto.setEnvironment(physicalConnector.getEnvironment());
        physicalConnectorDto.setId(physicalConnector.getId());
        physicalConnectorDto.setPhysicalConnectorDescription(physicalConnector.getDescription());
        physicalConnectorDto.setPhysicalConnectorName(physicalConnector.getName());
        physicalConnectorDto.setType(physicalConnector.getConnectorType().getName());
        physicalConnectorDto.setStatus(physicalConnector.getPhysicalConnectorStatus().name());

        // Create the url monitoring of the physical connector
        String urlMonitoring = "http://" +
                physicalConnector.getVirtualIp() +
                ":" +
                this.connectorMonitoringPort +
                this.connectorMonitoringUrl;
        physicalConnectorDto.setUrlMonitoring(urlMonitoring);

        // Get the physical connector ports
        if (physicalConnector.getConnectorPortList() == null)
        {
            // If null, initialize to zero
            physicalConnectorDto.setPorts(new ConnectorPortInfo[0]);
        }
        else
        {
            // Creates connector port list Dto array
            physicalConnectorDto.setPorts(this.buildConnectorPortInfoListDto(physicalConnector.getConnectorPortList()));
        }

        // Logical connector can be null or not
        if (physicalConnector.getLogicalConnectors() == null)
        {
            physicalConnectorDto.setLogicalConnectorsId(new int[0]);
        }
        else
        {
            int[] ids = new int[physicalConnector.getLogicalConnectors().size()];
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = physicalConnector.getLogicalConnectors().get(i).getId();
            }
            physicalConnectorDto.setLogicalConnectorsId(ids);
        }

        // The physcial connector always are multiCPD value depending on the value of the CPD and the virtual IP
        physicalConnectorDto.setVirtualIp(physicalConnector.getVirtualIp());
        physicalConnectorDto.setVaguadaVirtualIp(physicalConnector.getVaguadaVirtualIp());

        // Set observations field
        physicalConnectorDto.setObservations(physicalConnector.getObservations());

        // Set if the physical connector has a delete to do task associated
        physicalConnectorDto.setDeleteTaskId(this.iPhysicalConnectorValidator.checkPendingDeletePhysicalConnector(physicalConnector));

        LOG.debug("[{}] -> [buildPhysicalConnectorDto]: built the logical connector Dto: [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorDto);

        return physicalConnectorDto;
    }

    @Override
    public ConnectorTypePropertyDto[] buildConnectorTypePropertyDtoArray(final List<ConnectorTypeProperty> connectorTypePropertyList)
    {
        ConnectorTypePropertyDto[] connectorTypePropertyDtoArray = new ConnectorTypePropertyDto[connectorTypePropertyList.size()];

        for (int i = 0; i < connectorTypePropertyList.size(); i++)
        {
            ConnectorTypeProperty connectorTypeProperty = connectorTypePropertyList.get(i);
            ConnectorTypePropertyDto connectorTypePropertyDto = new ConnectorTypePropertyDto();

            connectorTypePropertyDto.setId(connectorTypeProperty.getId());
            connectorTypePropertyDto.setPropertyDescription(connectorTypeProperty.getDescription());
            connectorTypePropertyDto.setPropertyManagement(connectorTypeProperty.getManagement());
            connectorTypePropertyDto.setPropertyName(connectorTypeProperty.getName());
            connectorTypePropertyDto.setPropertyScope(connectorTypeProperty.getScope());
            connectorTypePropertyDto.setPropertySecurity(connectorTypeProperty.isSecurity());
            connectorTypePropertyDto.setPropertyType(connectorTypeProperty.getPropertyType());
            connectorTypePropertyDto.setPropertyCPDName(connectorTypeProperty.getCpdName());

            connectorTypePropertyDtoArray[i] = connectorTypePropertyDto;
            LOG.trace("[{}] -> [buildConnectorTypePropertyDtoArray]: Added new Connector type property Dto: [{}]", Constants.PHYSICAL_CONNECTOR_API, connectorTypePropertyDto);
        }

        LOG.debug("[{}] -> [buildConnectorTypePropertyDtoArray]: returned the following connector port info list: [{}]", Constants.PHYSICAL_CONNECTOR_API, Arrays.toString(connectorTypePropertyDtoArray));

        return connectorTypePropertyDtoArray;
    }

    @Override
    @Transactional
    public ConnectorTypeProperty createConnectorTypeProperty(final ConnectorTypePropertyDto connectorTypePropertyDto, final String connectorTypeName)
    {
        // Create a connector type property
        ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();

        connectorTypeProperty.setName(MessageFormat.format("{0}.{1}", connectorTypeName.toUpperCase(), connectorTypePropertyDto.getPropertyName()));
        connectorTypeProperty.setPropertyType(connectorTypePropertyDto.getPropertyType());
        connectorTypeProperty.setManagement(connectorTypePropertyDto.getPropertyManagement());
        connectorTypeProperty.setScope(connectorTypePropertyDto.getPropertyScope());
        connectorTypeProperty.setSecurity(connectorTypePropertyDto.getPropertySecurity());
        connectorTypeProperty.setDescription(connectorTypePropertyDto.getPropertyDescription());
        connectorTypeProperty.setCpdName(connectorTypePropertyDto.getPropertyCPDName());

        // Save the connector property int the BBDD
        this.connectorTypePropertyRepository.save(connectorTypeProperty);
        LOG.debug("[{}] -> [createConnectorTypeProperty]: created and saved into BBDD a new connector type property [{}]", Constants.PHYSICAL_CONNECTOR_API, connectorTypeProperty);

        return connectorTypeProperty;
    }

    //////////////////////////////////////////////////////// PRIVATE METHODS ///////////////////////////////////////////

    /**
     * Build the connector port info array
     *
     * @param connectorPortList connector port list from Physical connector
     * @return a connector port info array
     */
    private ConnectorPortInfo[] buildConnectorPortInfoListDto(final List<PhysicalConnectorPort> connectorPortList)
    {
        ConnectorPortInfo[] connectorPortInfoArray = new ConnectorPortInfo[connectorPortList.size()];

        for (int i = 0; i < connectorPortList.size(); i++)
        {
            ConnectorPortInfo connectorPortInfo = new ConnectorPortInfo();
            PhysicalConnectorPort physicalConnectorPort = connectorPortList.get(i);

            // Fill the connector port info
            connectorPortInfo.setId(physicalConnectorPort.getId());
            connectorPortInfo.setPortName(physicalConnectorPort.getPortName());
            connectorPortInfo.setDescription(physicalConnectorPort.getDescription());
            connectorPortInfo.setInputPort(physicalConnectorPort.getInputPort());
            connectorPortInfo.setOutput(physicalConnectorPort.getOutput());
            // In this case, the action field can be null or empty
            connectorPortInfo.setAction(null);

            // Add into connector port info array
            connectorPortInfoArray[i] = connectorPortInfo;
            LOG.trace("[{}] -> [buildConnectorPortInfoListDto]: added new Connector port info: [{}]", Constants.PHYSICAL_CONNECTOR_API, connectorPortInfo);
        }

        LOG.debug("[{}] -> [buildConnectorPortInfoListDto]: returned the following connector port info list: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, Arrays.toString(connectorPortInfoArray));
        return connectorPortInfoArray;
    }

    /**
     * Validate the physical connector port list to removed and after removing
     *
     * @param physicalConnector         the physical connector to manage
     * @param deletionConnectorPortList he list with physical connector port list to remove
     */
    private void deletePhysicalConnectorPort(final PhysicalConnector physicalConnector, final List<ConnectorPortInfo> deletionConnectorPortList)
    {
        LOG.debug("[{}] -> [deletePhysicalConnectorPort]: trying to delete the following physical connector port list: [{}].", Constants.PHYSICAL_CONNECTOR_API, deletionConnectorPortList);

        // Validate that the physical connector port is not in use by some logical connector
        for (ConnectorPortInfo connectorPortInfo : deletionConnectorPortList)
        {
            for (LogicalConnector logicalConnector : physicalConnector.getLogicalConnectors())
            {
                if (logicalConnector.getPhysicalConnectorPort().getPortName().equals(connectorPortInfo.getPortName()))
                {
                    String message = "[PhysicalConnectorAPI] -> [deletePhysicalConnectorPort]: the logical Connector: [" + logicalConnector.getName() + "] is already being used by the " +
                            "physical connector port name: [" + connectorPortInfo.getPortName() + "] - with ID: [" + connectorPortInfo.getId() + "]. Physical connector port cannot be deleted]";
                    LOG.error(message);
                    throw new NovaException(PhysicalConnectorError.getPhysicalConnectorPortAlreadyInUseError(), message);
                }
            }
        }

        // Delete physical connectors port from BBDD of the list to remove
        List<PhysicalConnectorPort> physicalConnectorPortListToRemove = new ArrayList<>();
        for (ConnectorPortInfo connectorPortInfo : deletionConnectorPortList)
        {
            if (connectorPortInfo.getId() == null)
            {
                LOG.warn("[{}] -> [deletePhysicalConnectorPort]: physical connector port does not exist yet into NOVA BBDD. It won't be deleted: [{}]",
                        Constants.PHYSICAL_CONNECTOR_API, connectorPortInfo);
            }
            else
            {
                PhysicalConnectorPort physicalConnectorPort = this.physicalConnectorPortRepository.findById(connectorPortInfo.getId()).orElse(null);

                if (physicalConnectorPort == null)
                {
                    String message = "[PhysicalConnectorAPI] -> [deletePhysicalConnectorPort]: the physical connector port id: [" + connectorPortInfo.getId() + "] does not exist into NOVA BBDD";
                    LOG.error(message);
                    throw new NovaException(PhysicalConnectorError.getNoSuchPhysicalConnectorPortError(), message);
                }
                else
                {
                    // Remove physical connector port from the physical connector list
                    physicalConnector.getConnectorPortList().remove(physicalConnectorPort);

                    // Add to removal list
                    physicalConnectorPortListToRemove.add(physicalConnectorPort);
                    LOG.debug("[{}] -> [deletePhysicalConnectorPort]: physical connector port added to deletion list: [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorPort);
                }
            }
        }

        // Delete physical connector port list from NOVA BBDD
        this.physicalConnectorPortRepository.deleteAll(physicalConnectorPortListToRemove);
        LOG.debug("[{}] -> [deletePhysicalConnectorPort]: physical connector port list deleted: [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorPortListToRemove);
    }

    /**
     * Validate the physical connector port list from Dto and after creating and saving the new physical connector port list into NOVA BBDD
     *
     * @param physicalConnector         the physical connector to manage
     * @param creationConnectorPortList the list with new physical connector port list
     */
    private void createAndSavePhysicalConnectorPort(final PhysicalConnector physicalConnector, final List<ConnectorPortInfo> creationConnectorPortList)
    {
        LOG.debug("[{}] -> [createAndSavePhysicalConnectorPort]: trying to create the following physical connector port list: [{}].", Constants.PHYSICAL_CONNECTOR_API, creationConnectorPortList);

        for (ConnectorPortInfo connectorPortInfo : creationConnectorPortList)
        {
            //Validate the new physical connector port (name and input port cannot be repeated)
            this.iPhysicalConnectorValidator.validateNewPhysicalConnectorPort(physicalConnector, connectorPortInfo);

            PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();

            physicalConnectorPort.setPortName(connectorPortInfo.getPortName());
            physicalConnectorPort.setInputPort(connectorPortInfo.getInputPort());
            physicalConnectorPort.setDescription(connectorPortInfo.getDescription());
            physicalConnectorPort.setOutput(this.validatePhysicalConnectorPortOutput(connectorPortInfo.getOutput()));

            LOG.debug("[{}] -> [createAndSavePhysicalConnectorPort]: new connector port added: [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorPort);

            // Save the new physical connector port list into BBDD
            this.physicalConnectorPortRepository.saveAndFlush(physicalConnectorPort);
            LOG.debug("[{}] -> [createAndSavePhysicalConnectorPort]: save into BBDD a new physical connector port: [{}]", Constants.PHYSICAL_CONNECTOR_API, physicalConnectorPort);

            // Add the new physical connector port to the physical connector
            physicalConnector.getConnectorPortList().add(physicalConnectorPort);
            LOG.debug("[{}] -> [createAndSavePhysicalConnectorPort]: added new physical connector port of the physical connector: [{}]",
                    Constants.PHYSICAL_CONNECTOR_API, physicalConnector.getConnectorPortList());
        }
    }

    /**
     * Validate the output of the physical connector port.
     * The output must be exactly a list comma separated of ip:port. This is an example: ip1:port1,ip2:port2
     * This method get the comma separated ip:port and after check each ip and port separately.
     *
     * @param output the output field
     * @return the output string validated
     */
    private String validatePhysicalConnectorPortOutput(final String output)
    {
        // First, split by ','
        String[] splitOutput = output.split(",");
        LOG.debug("[{}] -> [validatePhysicalConnectorPortOutput]: output comma separated result: [{}]", Constants.PHYSICAL_CONNECTOR_API, Arrays.toString(splitOutput));

        for (String outputIpPort : splitOutput)
        {
            // Second, split by ':'
            String[] splitOutputIpPort = outputIpPort.split(":");
            LOG.debug("[{}] -> [validatePhysicalConnectorPortOutput]: output colon separated result: [{}]", Constants.PHYSICAL_CONNECTOR_API, Arrays.toString(splitOutputIpPort));

            // The length must be exactly 2
            if (splitOutputIpPort.length == 2)
            {
                // Validate the port number and port range
                this.iPhysicalConnectorValidator.validatePortRange(splitOutputIpPort[1]);
            }
            else
            {
                String message = "[PhysicalConnectorAPI] -> [validatePhysicalConnectorPortOutput]: the one output value is not following the format [ip:port].";
                LOG.error(message);
                throw new NovaException(PhysicalConnectorError.getIpPortFormatNotValidError(), message);
            }
        }

        LOG.debug("[{}] -> [validatePhysicalConnectorPortOutput]: output has been validated: [{}]", Constants.PHYSICAL_CONNECTOR_API, output);

        return output;
    }
}


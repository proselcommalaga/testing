package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.configurationmanagerapi.model.CMLogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LC_DeploymentServiceDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LC_PhysicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDocument;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.PhysicalConnectorPortDto;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.DocSystemUtils;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.listener.ListenerLogicalconnectorapi;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class dedicate to build the logical connector Dtos
 *
 * @author BBVA - XE30432
 */
@Service
public class LogicalConnectorBuilderImpl implements ILogicalConnectorBuilder
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListenerLogicalconnectorapi.class);

    /**
     * logical connector validator
     */
    private final LogicalConnectorValidatorImpl logicalConnectorValidatorImpl;

    /**
     * Configuration manager client
     */
    private final ConfigurationmanagerClient configurationmanagerClient;

    /**
     * To do task service client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Manage validation utils
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Nova Context for getting user code
     */
    private final NovaContext novaContext;

    /**
     * Dependency injection constructor
     *
     * @param logicalConnectorValidatorImpl logicalConnectorValidatorImpl
     * @param configurationmanagerClient    configuration manager Client
     * @param todoTaskServiceClient         to do task service client
     * @param manageValidationUtils         manageValidationUtils
     * @param novaContext                   the nova context
     */
    @Autowired
    public LogicalConnectorBuilderImpl(final LogicalConnectorValidatorImpl logicalConnectorValidatorImpl,
                                       final ConfigurationmanagerClient configurationmanagerClient,
                                       final TodoTaskServiceClient todoTaskServiceClient,
                                       final ManageValidationUtils manageValidationUtils,
                                       final NovaContext novaContext)
    {
        this.logicalConnectorValidatorImpl = logicalConnectorValidatorImpl;
        this.configurationmanagerClient = configurationmanagerClient;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.manageValidationUtils = manageValidationUtils;
        this.novaContext = novaContext;
    }

    ///////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////

    @Override
    public LogicalConnectorDto[] buildLogicalConnectorDtoList(final List<LogicalConnector> logicalConnectorList)
    {
        LogicalConnectorDto[] logicalConnectorDtos = new LogicalConnectorDto[logicalConnectorList.size()];

        int i = 0;
        for (LogicalConnector logicalConnector : logicalConnectorList)
        {
            logicalConnectorDtos[i] = this.buildAllLogicalConnectorDto(logicalConnector);
            i++;
        }
        LOG.trace("[{}] -> [buildLogicalConnectorDtoList]: built logical connector Dto array with values [{}]", Constants.LOGICAL_CONNECTOR_API, Arrays.toString(logicalConnectorDtos));

        return logicalConnectorDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public LogicalConnectorDto buildLogicalConnectorDto(final LogicalConnector logicalConnector, final String userCode)
    {
        LOG.trace("[{}] -> [buildLogicalConnectorDto]: building logical connector Dto with original values [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnector);

        // Create the logicalConnectorDto
        LogicalConnectorDto logicalConnectorDto = new LogicalConnectorDto();

        logicalConnectorDto.setId(logicalConnector.getId());
        logicalConnectorDto.setLogicalConnectorDescription(logicalConnector.getDescription());
        logicalConnectorDto.setLogicalConnectorName(logicalConnector.getName());
        logicalConnectorDto.setEnvironment(logicalConnector.getEnvironment());
        logicalConnectorDto.setObservations(logicalConnector.getObservations());
        logicalConnectorDto.setProductId(logicalConnector.getProduct().getId());
        logicalConnectorDto.setStatus(logicalConnector.getLogicalConnectorStatus().getLogicalConnectorStatus());
        logicalConnectorDto.setType(logicalConnector.getConnectorType().getName());
        this.setLogicalConnectorDtoDocuments(logicalConnectorDto, logicalConnector);

        // By default can be manage by user
        logicalConnectorDto.setCanBeManagedByUser(true);
        // Check all services where connector is used
        for(DeploymentService service : logicalConnector.getDeploymentServices())
        {
            // If the plan of the service can not be manage by user and status plan is deployed -> logical connector can not be manage by user
            if (service.getDeploymentSubsystem().getDeploymentPlan().getStatus().equals(DeploymentStatus.DEPLOYED) &&
                    !this.manageValidationUtils.checkIfPlanCanBeManagedByUser(userCode,service.getDeploymentSubsystem().getDeploymentPlan()))
            {
                logicalConnectorDto.setCanBeManagedByUser(false);
            }
        }

        // Fill the physical connector dto for this logical connector
        this.fillPhysicalConnectorDto(logicalConnector, logicalConnectorDto);

        // Build and set the services list dto
        logicalConnectorDto.setServices(this.buildServiceDtoList(logicalConnector.getDeploymentServices()));

        // Build and set the logical connector properties dto
        logicalConnectorDto.setProperties(this.buildLogicalConnectorPropertyDtoArray(logicalConnector, userCode));

        // Build the configuration to do task id
        logicalConnectorDto.setPropertiesTaskId(this.logicalConnectorValidatorImpl.checkPendingPropertyTask(logicalConnector));

        LOG.debug("[{}] -> [buildLogicalConnectorDto]: built the logical connector Dto: [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnectorDto);

        return logicalConnectorDto;
    }


    ////////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////

    /**
     * Fill the physical connector of the logical connector dto
     *
     * @param logicalConnector    the logical connector instance
     * @param logicalConnectorDto the logical connector dto to fill the physical connector
     */
    private void fillPhysicalConnectorDto(final LogicalConnector logicalConnector, final LogicalConnectorDto logicalConnectorDto)
    {
        // Physical connector can be null or not
        PhysicalConnector physicalConnector = logicalConnector.getPhysicalConnector();

        if (physicalConnector == null)
        {
            logicalConnectorDto.setPhysicalConnector(null);
        }
        else
        {
            // Fill the physical connector Dto
            LC_PhysicalConnectorDto lc_physicalConnectorDto = new LC_PhysicalConnectorDto();

            lc_physicalConnectorDto.setId(physicalConnector.getId());
            lc_physicalConnectorDto.setPhysicalConnectorName(physicalConnector.getName());
            lc_physicalConnectorDto.setVirtualIp(physicalConnector.getVirtualIp());
            lc_physicalConnectorDto.setVaguadaVirtualIp(physicalConnector.getVaguadaVirtualIp());
            lc_physicalConnectorDto.setStatus(physicalConnector.getPhysicalConnectorStatus().name());

            // Fill the physical connector port Dto
            PhysicalConnectorPort physicalConnectorPort = logicalConnector.getPhysicalConnectorPort();

            if (physicalConnectorPort == null)
            {
                lc_physicalConnectorDto.setPort(null);
            }
            else
            {
                PhysicalConnectorPortDto physicalConnectorPortDto = new PhysicalConnectorPortDto();

                physicalConnectorPortDto.setId(physicalConnectorPort.getId());
                physicalConnectorPortDto.setPortName(physicalConnectorPort.getPortName());
                physicalConnectorPortDto.setDescription(physicalConnectorPort.getDescription());
                physicalConnectorPortDto.setInputPort(physicalConnectorPort.getInputPort());
                physicalConnectorPortDto.setOutput(physicalConnectorPort.getOutput());

                lc_physicalConnectorDto.setPort(physicalConnectorPortDto);
                LOG.trace("[{}] -> [buildLogicalConnectorDto]: added physical connector port: [{}]", Constants.LOGICAL_CONNECTOR_API, physicalConnectorPortDto);
            }

            // Add the new physical connector to response
            logicalConnectorDto.setPhysicalConnector(lc_physicalConnectorDto);
            LOG.trace("[{}] -> [buildLogicalConnectorDto]: added new physical connector dto: [{}]", Constants.LOGICAL_CONNECTOR_API, lc_physicalConnectorDto);
        }
    }

    /**
     * Builds an array of {@link LC_DeploymentServiceDto} from the {@link DeploymentService}
     * using a {@link LogicalConnector}.
     *
     * @param deploymentServiceList List of services from the logical connector.
     * @return Array of LC_Deployment Service Dtos.
     */
    private LC_DeploymentServiceDto[] buildServiceDtoList(final List<DeploymentService> deploymentServiceList)
    {
        LOG.trace("[{}] -> [buildServiceDtoList]: building deployment service Dto array with original values [{}]", Constants.LOGICAL_CONNECTOR_API, deploymentServiceList);

        List<LC_DeploymentServiceDto> lc_deploymentServiceDtoList = new ArrayList<>();

        for (DeploymentService deploymentService : deploymentServiceList)
        {
            LC_DeploymentServiceDto lc_deploymentServiceDto = new LC_DeploymentServiceDto();

            // Copy properties from original service.
            lc_deploymentServiceDto.setServiceName(deploymentService.getService().getServiceName());
            lc_deploymentServiceDto.setVersion(deploymentService.getService().getVersion());
            lc_deploymentServiceDto.setArtifactId(deploymentService.getService().getArtifactId());
            lc_deploymentServiceDto.setGroupId(deploymentService.getService().getGroupId());
            lc_deploymentServiceDto.setNumberOfInstances(deploymentService.getNumberOfInstances());
            lc_deploymentServiceDto.setId(deploymentService.getId());
            lc_deploymentServiceDto.setServiceFinalname(deploymentService.getService().getFinalName());
            lc_deploymentServiceDto.setServiceType(deploymentService.getService().getServiceType());
            lc_deploymentServiceDto.setDescription(deploymentService.getService().getDescription());

            // Deployment deploymentPlan data.
            DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();

            lc_deploymentServiceDto.setPlanId(deploymentPlan.getId());
            lc_deploymentServiceDto.setPlanStatus(deploymentPlan.getStatus().name());

            // Release and version.
            lc_deploymentServiceDto.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());
            lc_deploymentServiceDto.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());
            lc_deploymentServiceDto.setReleaseVersionId(deploymentPlan.getReleaseVersion().getId());
            lc_deploymentServiceDto.setReleaseVersionName(deploymentPlan.getReleaseVersion().getVersionName());

            // Product.
            Product product = deploymentPlan.getReleaseVersion().getRelease().getProduct();
            lc_deploymentServiceDto.setProductId(product.getId());
            lc_deploymentServiceDto.setProductName(product.getName());

            // Add the Dto to the list.
            lc_deploymentServiceDtoList.add(lc_deploymentServiceDto);
        }

        LOG.debug("[{}] -> [buildServiceDtoList]: Built LC_Deployment sevice Dto array [{}]", Constants.LOGICAL_CONNECTOR_API, lc_deploymentServiceDtoList);

        // Return as an array.
        return lc_deploymentServiceDtoList.toArray(new LC_DeploymentServiceDto[lc_deploymentServiceDtoList.size()]);
    }

    /**
     * Build a logical connector Dto with only the optimal information to the get all logical connector
     *
     * @param logicalConnector the logical connector instance
     * @return a logical connector Dto instance
     */
    private LogicalConnectorDto buildAllLogicalConnectorDto(final LogicalConnector logicalConnector)
    {
        LOG.trace("[{}] -> [buildAllLogicalConnectorDto]: building all the logical connector Dto with original values [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnector);

        // Create the logicalConnectorDto
        LogicalConnectorDto logicalConnectorDto = new LogicalConnectorDto();

        logicalConnectorDto.setEnvironment(logicalConnector.getEnvironment());
        logicalConnectorDto.setId(logicalConnector.getId());
        logicalConnectorDto.setLogicalConnectorDescription(logicalConnector.getDescription());
        logicalConnectorDto.setLogicalConnectorName(logicalConnector.getName());
        logicalConnectorDto.setProductId(logicalConnector.getProduct().getId());
        logicalConnectorDto.setStatus(logicalConnector.getLogicalConnectorStatus().getLogicalConnectorStatus());
        logicalConnectorDto.setType(logicalConnector.getConnectorType().getName());
        this.setLogicalConnectorDtoDocuments(logicalConnectorDto, logicalConnector);

        // By default can be manage by user
        logicalConnectorDto.setCanBeManagedByUser(true);
        // Check all services where connector is used
        for(DeploymentService service : logicalConnector.getDeploymentServices())
        {
            // If the plan of the service can not be manage by user and status plan is deployed -> logical connector can not be manage by user
            if (service.getDeploymentSubsystem().getDeploymentPlan().getStatus().equals(DeploymentStatus.DEPLOYED) &&
                    !this.manageValidationUtils.checkIfPlanCanBeManagedByUser(this.novaContext.getIvUser(),service.getDeploymentSubsystem().getDeploymentPlan()))
            {
                logicalConnectorDto.setCanBeManagedByUser(false);
            }
        }

        return logicalConnectorDto;
    }

    /**
     * Get the logical connector property array calling the Configuration Manager client API -> getLogicalConnectorPropertyDto method.
     * If the call fails, create to do task and return empty logical connector properties.
     *
     * @param logicalConnector the logical connector to get the properties
     * @param userCode         the user code that request
     * @return a LogicalConnectorPropertyDto with the Dtos or empty array if the call to Configuration manager fails
     */
    private LogicalConnectorPropertyDto[] buildLogicalConnectorPropertyDtoArray(final LogicalConnector logicalConnector, final String userCode)
    {
        // Create the logical connector property dto array to return
        LogicalConnectorPropertyDto[] logicalConnectorPropertyDtoArray;

        // Build the logical connector properties Dto array from Configuration manager logical connector property Dto (CMLogicalConnectorPropertyDto)
        CMLogicalConnectorPropertyDto[] configurationManagerDtoArray =
                this.configurationmanagerClient.getLogicalConnectorPropertiesDto(logicalConnector.getId());

        if (configurationManagerDtoArray == null || configurationManagerDtoArray.length == 0)
        {
            // Create the to do task, log the error and set empty logical connector properties dto array
            String todoTaskDescription = "[LogicalConnectorAPI] -> [buildLogicalConnectorPropertyDtoArray]: there was an error trying to get the" +
                    " logical connector properties of the logical connector: [" + logicalConnector.getName() + "]"
                    + " of the product name: [" + logicalConnector.getProduct().getName() + "]- UUAA:[" + logicalConnector.getProduct().getUuaa() +
                    "]. Review the Configuration manger service status.";
            LOG.error(todoTaskDescription);
            this.todoTaskServiceClient.createGenericTask(userCode, null, ToDoTaskType.LOGICAL_CONNECTOR_PROPERTIES_ERROR.name(),
                    RoleType.PLATFORM_ADMIN.name(), todoTaskDescription, logicalConnector.getProduct().getId());

            // Set the logical connector property dto array to empty
            logicalConnectorPropertyDtoArray = new LogicalConnectorPropertyDto[0];
        }
        else
        {
            // Create model mapper to make conversion ConfigurationManager DTO(LogicalConnectorProperty) -> LogicalConnectorProperty DTO
            ModelMapper modelMapper = new ModelMapper();
            logicalConnectorPropertyDtoArray = new LogicalConnectorPropertyDto[configurationManagerDtoArray.length];

            // Fill the logical connector properties dto array from the dto of the configuration manager
            for (int i = 0; i < configurationManagerDtoArray.length; i++)
            {
                LogicalConnectorPropertyDto logicalConnectorPropertyDto = modelMapper.map(configurationManagerDtoArray[i], LogicalConnectorPropertyDto.class);
                logicalConnectorPropertyDtoArray[i] = logicalConnectorPropertyDto;
                LOG.trace("[{}] -> [buildLogicalConnectorPropertyDtoArray]: added new logical connector property dto int array: [{}]", Constants.LOGICAL_CONNECTOR_API, logicalConnectorPropertyDto);
            }
        }

        LOG.debug("[{}] -> [buildLogicalConnectorPropertyDtoArray]: built the following logical connector property dto array: [{}]", Constants.LOGICAL_CONNECTOR_API,
                Arrays.toString(logicalConnectorPropertyDtoArray));

        return logicalConnectorPropertyDtoArray;
    }

    /**
     * Set the MSA document of a LogicalConnectorDto from a LogicalConnector entity.
     *
     * @param logicalConnectorDto       The LogicalConnector entity.
     * @param logicalConnectorEntity    The LogicalConnectorDto.
     */
    private void setLogicalConnectorDtoDocuments(LogicalConnectorDto logicalConnectorDto, LogicalConnector logicalConnectorEntity)
    {
        if (logicalConnectorEntity.getMsaDocument() != null)
        {
            logicalConnectorDto.setMsaDocument(DocSystemUtils.fillResourceDocumentWithDocSystem(new LogicalConnectorDocument(), logicalConnectorEntity.getMsaDocument()));
        }
        if (logicalConnectorEntity.getAraDocument() != null)
        {
            logicalConnectorDto.setAraDocument(DocSystemUtils.fillResourceDocumentWithDocSystem(new LogicalConnectorDocument(), logicalConnectorEntity.getAraDocument()));
        }
    }
}

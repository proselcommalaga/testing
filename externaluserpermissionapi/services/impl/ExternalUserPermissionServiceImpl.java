package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.services.impl;

import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEpsilonEther;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermission;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermissionConfig;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.exceptions.ExternalUserPermissionError;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.services.interfaces.IExternalUserPermission;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.util.ExternalUserConstants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Service for External User permissions
 */
@Service
public class ExternalUserPermissionServiceImpl implements IExternalUserPermission
{
    /**
     * User service client
     */
    private final IProductUsersClient usersClient;

    private final ExternalUserPermissionRepository externalUserPermissionRepository;

    private final ExternalUserPermissionConfigRepository externalUserPermissionConfigRepository;

    private final ProductRepository productRepository;

    private final FilesystemRepository filesystemRepository;

    private final DeploymentPlanRepository deploymentPlanRepository;

    private final INovaActivityEmitter novaActivityEmitter;

    private final IEtherManagerClient etherManagerClient;

    @Autowired
    public ExternalUserPermissionServiceImpl(final IProductUsersClient usersClient, final ExternalUserPermissionRepository externalUserPermissionRepository, final ExternalUserPermissionConfigRepository externalUserPermissionConfigRepository, final ProductRepository productRepository, final FilesystemRepository filesystemRepository, final DeploymentPlanRepository deploymentPlanRepository, final INovaActivityEmitter novaActivityEmitter, final IEtherManagerClient etherManagerClient)
    {
        this.usersClient = usersClient;
        this.externalUserPermissionRepository = externalUserPermissionRepository;
        this.externalUserPermissionConfigRepository = externalUserPermissionConfigRepository;
        this.productRepository = productRepository;
        this.filesystemRepository = filesystemRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.novaActivityEmitter = novaActivityEmitter;
        this.etherManagerClient = etherManagerClient;
    }

    @Override
    @Transactional
    public void createPermission(final PermissionDTO permission, final String ivUser) throws NovaException
    {
        // check if the user has permissions
        this.usersClient.checkHasPermission(ivUser, ExternalUserConstants.EXTERNAL_USER_PERMISSION, new NovaException(ExternalUserPermissionError.getForbiddenError()));

        // validate and parse the permission
        final ExternalUserPermission externalPermission = validatePermission(permission);

        // get the product info
        final Product product = this.productRepository.fetchById(permission.getProductId());

        if (product == null)
        {
            throw new NovaException(ExternalUserPermissionError.getProductNotFoundError(permission.getProductId()));
        }

        if (Platform.ETHER.equals(externalPermission.getPlatform()))
        {
            // get the namespace
            final String namespace = fetchNamespaceFromProduct(product, Environment.valueOf(externalPermission.getEnvironment()));

            // set the namespace as property product
            externalPermission.setProductProperty(namespace);

            // create permission
            this.etherManagerClient.setPermission(externalPermission, namespace, externalPermission.getEnvironment());
        }
        else
        {
            // throw exception
            throw new NovaException(ExternalUserPermissionError.getPlatformNotSupportedError(externalPermission.getPlatform()));
        }

        // save it
        this.externalUserPermissionRepository.saveAndFlush(externalPermission);

        // Emit Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(permission.getProductId(), ActivityScope.PERMISSION, ActivityAction.SET_PERMISSION)
                .entityId(permission.getId())
                .environment(permission.getEnvironment())
                .addParam("Platform", permission.getPlatform())
                .addParam("Service", permission.getService())
                .addParam("ResourceType", permission.getResourceType())
                .addParam("Resources", permission.getResources())
                .addParam("Consumer", permission.getUser())
                .addParam("Permission Type", permission.getPermissionType())
                .addParam("Permission", permission.getPermission())
                .addParam("Created by", ivUser)
                .build());
    }

    @Override
    public void deletePermission(final Integer id, final String ivUser)
    {
        // check if the user has permissions
        this.usersClient.checkHasPermission(ivUser, ExternalUserConstants.EXTERNAL_USER_PERMISSION, new NovaException(ExternalUserPermissionError.getForbiddenError()));

        if (id == null)
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("permission id", null));
        }

        final ExternalUserPermission permission = this.externalUserPermissionRepository.findById(id).orElseThrow(() -> new NovaException(ExternalUserPermissionError.getPermissionNotFoundError(id)));

        if (Platform.ETHER.equals(permission.getPlatform()))
        {
            // get the namespace from the property
            final String namespace = permission.getProductProperty();

            // delete permission
            this.etherManagerClient.unsetPermission(permission, namespace, permission.getEnvironment());
        }
        else
        {
            // throw exception
            throw new NovaException(ExternalUserPermissionError.getPlatformNotSupportedError(permission.getPlatform()));
        }

        // delete it
        this.externalUserPermissionRepository.deleteById(id);

        // Emit Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(permission.getProductId(), ActivityScope.PERMISSION, ActivityAction.UNSET_PERMISSION)
                .entityId(permission.getId())
                .environment(permission.getEnvironment())
                .addParam("Platform", permission.getPlatform())
                .addParam("Service", permission.getService())
                .addParam("ResourceType", permission.getResourceType())
                .addParam("Resources", permission.getResources())
                .addParam("Consumer", permission.getConsumer())
                .addParam("Permission Type", permission.getPermissionType())
                .addParam("Permission", permission.getPermission())
                .addParam("Created by", ivUser)
                .build());
    }

    @Override
    public PermissionDTO[] getPermissions(final String environment, final Integer productId)
    {
        if (StringUtils.isBlank(environment))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("environment", environment));
        }
        else if (productId == null || productId < 1)
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("product", ""+productId));
        }

        final List<ExternalUserPermission> externalUserPermissionList = this.externalUserPermissionRepository.findByProductIdAndEnvironment(productId, environment);

        return externalUserPermissionList.stream()
                .map(ExternalUserPermissionServiceImpl::parseToPermissionDTO)
                .toArray(PermissionDTO[]::new);
    }

    @Override
    public PlatformDTO[] getPlatformPermissions(final String environment, final Integer productId)
    {
        // get the configurations
        final List<ExternalUserPermissionConfig> configList = this.externalUserPermissionConfigRepository.findAll();

        final Map<Platform, Map<String, Map<String, List<PlatformPermissionDTO>>>> configMap = new TreeMap<>();

        // save the permissions in map
        configList.forEach(config ->
                configMap.computeIfAbsent(config.getPlatform(), k -> new TreeMap<>())
                        .computeIfAbsent(config.getService(), k -> new TreeMap<>())
                        .computeIfAbsent(config.getResourceType(), k -> new ArrayList<>())
                        .add(buildPlatformPermissionDTO(config.getPermission(), config.getPermissionType()))
        );

        // parse the map
        return buildPlatformDTO(environment, productId, configMap);
    }

    private static ExternalUserPermission validatePermission(final PermissionDTO permission) throws NovaException
    {
        final ExternalUserPermission externalUserPermission = new ExternalUserPermission();

        if (permission == null)
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("data", "null"));
        }
        else if (StringUtils.isBlank(permission.getPlatform()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("platform", permission.getPlatform()));
        }
        else if (StringUtils.isBlank(permission.getEnvironment()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("environment", permission.getEnvironment()));
        }
        else if (StringUtils.isBlank(permission.getPermission()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("permission", permission.getPermission()));
        }
        else if (StringUtils.isBlank(permission.getPermissionType()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("permission type", permission.getPermissionType()));
        }
        else if (StringUtils.isBlank(permission.getService()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("service", permission.getService()));
        }
        else if (StringUtils.isBlank(permission.getResourceType()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("resource type", permission.getResourceType()));
        }
        else if (StringUtils.isBlank(permission.getUser()))
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("user", permission.getUser()));
        }
        else if (permission.getProductId() == null || permission.getProductId() < 1)
        {
            throw new NovaException(ExternalUserPermissionError.getBadRequestError("product", ""+permission.getProductId()));
        }

        externalUserPermission.setPlatform(Platform.valueOf(permission.getPlatform().toUpperCase()));
        externalUserPermission.setEnvironment(permission.getEnvironment().toUpperCase());
        externalUserPermission.setPermission(permission.getPermission());
        externalUserPermission.setPermissionType(permission.getPermissionType());
        externalUserPermission.setService(permission.getService());
        externalUserPermission.setConsumer(permission.getUser());
        externalUserPermission.setProductId(permission.getProductId());
        externalUserPermission.setResources(permission.getResources());
        externalUserPermission.setResourceType(permission.getResourceType());
        externalUserPermission.setProductId(permission.getProductId());

        return externalUserPermission;
    }

    private static PermissionDTO parseToPermissionDTO(final ExternalUserPermission externalUserPermission)
    {
        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPermission(externalUserPermission.getPermission());
        permissionDTO.setPermissionType(externalUserPermission.getPermissionType());
        permissionDTO.setService(externalUserPermission.getService());
        permissionDTO.setEnvironment(externalUserPermission.getEnvironment());
        permissionDTO.setPlatform(externalUserPermission.getPlatform().name());
        permissionDTO.setId(externalUserPermission.getId());
        permissionDTO.setProductId(externalUserPermission.getProductId());
        permissionDTO.setResources(externalUserPermission.getResources());
        permissionDTO.setResourceType(externalUserPermission.getResourceType());
        permissionDTO.setUser(externalUserPermission.getConsumer());

        return permissionDTO;
    }

    private static String fetchNamespaceFromProduct(final Product product, final Environment environment)
    {
        final String namespace;
        switch (environment)
        {
            case INT:
                namespace = product.getEtherNsInt();
                break;
            case PRE:
                namespace = product.getEtherNsPre();
                break;
            case PRO:
                namespace = product.getEtherNsPro();
                break;
            default:
                throw new NovaException(ExternalUserPermissionError.getBadRequestError("environment", environment.name()));
        }

        return namespace;
    }

    private static PlatformPermissionDTO buildPlatformPermissionDTO(final String permission, final String permissionType)
    {
        final PlatformPermissionDTO permissionDTO = new PlatformPermissionDTO();
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        return permissionDTO;
    }

    public PlatformDTO[] buildPlatformDTO(final String environment, final Integer productId, final Map<Platform, Map<String, Map<String, List<PlatformPermissionDTO>>>> permissions)
    {
        final PlatformDTO[] platformDTOArray = new PlatformDTO[permissions.size()];

        int permissionIterator = 0;
        PlatformDTO platformDTO;
        for (Map.Entry<Platform, Map<String, Map<String, List<PlatformPermissionDTO>>>> platform : permissions.entrySet())
        {
            platformDTO = new PlatformDTO();
            platformDTO.setName(platform.getKey().getName());

            final ServiceDTO[] serviceDTOArray = new ServiceDTO[platform.getValue().size()];

            int serviceIterator = 0;
            ServiceDTO serviceDTO;
            for (Map.Entry<String, Map<String, List<PlatformPermissionDTO>>> service : platform.getValue().entrySet())
            {
                serviceDTO = new ServiceDTO();
                serviceDTO.setName(service.getKey());

                final ResourceTypeDTO[] resourceDTOArray = new ResourceTypeDTO[service.getValue().size()];

                int resourceIterator = 0;
                ResourceTypeDTO resourceDTO;
                for (Map.Entry<String, List<PlatformPermissionDTO>> resource : service.getValue().entrySet())
                {
                    resourceDTO = new ResourceTypeDTO();
                    resourceDTO.setName(resource.getKey());
                    resourceDTO.setPermissions(resource.getValue().toArray(PlatformPermissionDTO[]::new));

                    resourceDTO.setResources(this.getResourcesOfResourceType(environment, productId, resource.getKey(), platform.getKey()));

                    resourceDTOArray[resourceIterator] = resourceDTO;
                    resourceIterator++;
                }

                serviceDTO.setResourcesType(resourceDTOArray);

                serviceDTOArray[serviceIterator] = serviceDTO;
                serviceIterator++;
            }

            platformDTO.setServices(serviceDTOArray);

            platformDTOArray[permissionIterator] = platformDTO;
            permissionIterator++;
        }

        return platformDTOArray;
    }

    private String[] getResourcesOfResourceType(final String environment, final Integer productId, final String resourceType, final Platform platform)
    {
        final String[] resources;
        if (platform.equals(Platform.ETHER))
        {
            if ("bucket".equalsIgnoreCase(resourceType))
            {
                // get Epsilon buckets
                final List<Filesystem> filesystemList = this.filesystemRepository.findByProductIdAndEnvironmentAndFilesystemTypeOrderByCreationDateDesc(productId, environment, FilesystemEpsilonEther.class);

                resources = filesystemList.stream()
                        .map(Filesystem::getName)
                        .toArray(String[]::new);
            }
            else if ("MonitorResource".equalsIgnoreCase(resourceType))
            {
                // get deployed services
                final List<DeploymentPlan> deploymentPlans = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(productId, environment, DeploymentStatus.DEPLOYED);

                resources = deploymentPlans.stream()
                        .filter(dp -> Platform.ETHER.equals(dp.getSelectedDeploy()))
                        .flatMap(dp -> dp.getDeploymentSubsystems().stream())
                        .flatMap(ds -> ds.getDeploymentServices().stream())
                        .map(service -> service.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName() + "-" + service.getService().getServiceName())
                        .toArray(String[]::new);
            }
            else
            {
                resources = new String[0];
            }
        }
        else
        {
            resources = new String[0];
        }

        return resources;
    }
}

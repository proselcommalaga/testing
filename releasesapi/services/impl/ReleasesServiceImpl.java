package com.bbva.enoa.platformservices.coreservice.releasesapi.services.impl;

import com.bbva.enoa.apirestgen.releasesapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.TimeInterval;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ProductReleaseUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.platformservices.coreservice.releasesapi.services.interfaces.IReleasesService;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ReleasesServiceImpl implements IReleasesService
{

    private static final NovaException PERMISSION_DENIED = new NovaException(ReleaseError.getForbiddenError(), ReleaseError.getForbiddenError().toString());

    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseDtoBuilder dtoBuilder;
    private final ReleaseValidator validator;
    private final CPDRepository cpdRepository;
    private final IProductUsersClient usersService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    @Autowired
    public ReleasesServiceImpl(final ProductRepository productRepository, final ReleaseRepository releaseRepository, final ReleaseDtoBuilder dtoBuilder,
                               final ReleaseValidator validator, final CPDRepository cpdRepository, final IProductUsersClient usersService, final INovaActivityEmitter novaActivityEmitter)
    {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.dtoBuilder = dtoBuilder;
        this.validator = validator;
        this.cpdRepository = cpdRepository;
        this.usersService = usersService;
        this.novaActivityEmitter = novaActivityEmitter;
    }


    @Override
    public String[] getCpds(final String environment)
    {
        if (EnumUtils.isValidEnum(Environment.class, environment))
        {
            return this.cpdRepository.getByEnvironment(environment).stream().map(CPD::getName).distinct().toArray(String[]::new);
        }
        else
        {
            //unknown environment
            return new String[0];
        }

    }

    @Override
    public void createRelease(final String ivUser, final NewReleaseRequest releaseToAdd)
    {
        // First of all check if product does exist in NOVA.
        // Will throw an exception if not.
        Product product = this.productRepository.findById(releaseToAdd.getProductId()).orElse(null);
        this.validator.checkProductExistence(releaseToAdd.getProductId(), product);

        // Check release name.
        this.validator.checkReleaseName(releaseToAdd.getReleaseName());

        //check Permissions
        this.usersService.checkHasPermission(ivUser, ReleaseConstants.CREATE_RELEASE_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check if there is already a Release with the same name.
        this.validator.existsReleaseWithSameName(releaseToAdd.getProductId(), releaseToAdd.getReleaseName());

        // Create a new Release.
        Release release = new Release();
        release.setName(releaseToAdd.getReleaseName());
        release.setCreationDate(Calendar.getInstance());
        release.setDescription(releaseToAdd.getDescription());
        release.setProduct(product);

        release.setSelectedDeployInt(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.INT.getEnvironment()));
        release.setSelectedLoggingInt(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.INT.getEnvironment()));

        release.setSelectedDeployPre(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.PRE.getEnvironment()));
        release.setSelectedLoggingPre(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.PRE.getEnvironment()));

        release.setSelectedDeployPro(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.PRO.getEnvironment()));
        release.setSelectedLoggingPro(
                product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.PRO.getEnvironment()));

        TimeInterval interval = new TimeInterval();
        interval.setStart(Calendar.getInstance().getTime());
        // Set Default values defined in product
        if (product.getDefaultAutodeployInPre())
        {
            release.setAutodeployInPre(interval);
        }
        if (product.getDefaultAutodeployInPro())
        {
            release.setAutodeployInPro(interval);
        }
        if (product.getDefaultAutomanageInPre())
        {
            release.setAutomanageInPre(interval);
        }
        if (product.getDefaultAutomanageInPro())
        {
            release.setAutoManageInPro(interval);
        }
        release.setDeploymentTypeInPro(product.getDefaultDeploymentTypeInPro());

        // And associate it to the Product.
        product.getReleases().add(release);

        // Emit Add Release Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.RELEASE, ActivityAction.CREATED)
                .entityId(release.getId())
                .addParam("releaseName", releaseToAdd.getReleaseName())
                .addParam("releaseDescription", releaseToAdd.getDescription())
                .build());

        log.debug("[ReleasesAPI] -> [createRelease]: added to Product: [{}] a new Release: [{}] by PortalUser: [{}]",
                releaseToAdd.getProductId(), release, ivUser);
    }

    @Override
    public String[] getCpdsHistorical()
    {
        return this.cpdRepository.findAllByOrderByNameAsc().stream().map(CPD::getName).distinct().toArray(String[]::new);
    }

    @Override
    public Integer getReleasesMaxVersions(final Integer productId)
    {
        // First check if product does exist in NOVA. Will throw an exception if not.
        Product product = this.productRepository.findById(productId).orElse(null);
        this.validator.checkProductExistence(productId, product);

        // Return maximum number of release versions
        return ProductReleaseUtils.getMaxReleaseVersions(product.getReleaseSlots());
    }

    @Override
    public ReleaseDto releaseInfo(final Integer releaseId)
    {
        // Build the Sto object
        return this.dtoBuilder.buildReleaseDtoFromEntity(this.validator.checkReleaseExistence(releaseId));
    }


    @Override
    @Transactional
    public void updateReleaseConfig(
            final String ivUser, final ReleaseConfigDto releaseConfig, final Integer releaseId,
            final ReleaseConstants.ENVIRONMENT environment)
    {
        //check permissions
        this.usersService.checkHasPermission(ivUser, ReleaseConstants.EDIT_RELEASE_CONFIG_PERMISSION, PERMISSION_DENIED);

        //Update the release with management config data
        this.updateManagementConfig(releaseConfig, releaseId, environment);

        //Update release with selected Platforms data
        this.updateSelectedPlatforms(releaseConfig, releaseId, environment);
    }

    @Override
    public void deleteRelease(final String ivUser, final Integer releaseId)
    {
        // Check if the Release does exist.
        Release release = this.validator.checkReleaseExistence(releaseId);

        // Get the product
        Product product = release.getProduct();
        this.validator.checkProductExistence(product);
        Integer productId = product.getId();

        //check Permissions
        this.usersService.checkHasPermission(ivUser, ReleaseConstants.DELETE_RELEASE_PERMISSION, productId, PERMISSION_DENIED);

        // Check if Release have any ReleaseVersion attached.
        if (!release.getReleaseVersions().isEmpty())
        {
            throw new NovaException(ReleaseError.getReleaseWithVersionsError(), "[ReleasesAPI] -> [deleteRelease]: cannot be deleted the release id: [" + releaseId + "] for the user code: [" + ivUser + "] due to "
                    + "the release still have the following release versions name associated: ["
                    + Arrays.toString(release.getReleaseVersions().stream().map(ReleaseVersion::getVersionName).collect(Collectors.toList()).toArray()) + "]");
        }

        // Remove it from the product.
        product.getReleases().remove(release);

        // Emit Delete Release Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.RELEASE, ActivityAction.DELETED)
                .entityId(releaseId)
                .addParam("releaseName", release.getName())
                .addParam("releaseDescription", release.getDescription())
                .build());

        log.debug("Deleted Release: {} from Product: {} by User: {}", releaseId, product.getName(), ivUser);
    }

    @Override
    public ReleaseVersionInListDto[] getReleaseVersions(final Integer releaseId, final String status)
    {
        // Check if the Release does exist.
        Release release = this.validator.checkReleaseExistence(releaseId);

        List<ReleaseVersion> retList;

        if (Strings.isNullOrEmpty(status))
        {
            log.debug("[ReleasesServiceImpl] -> [getReleaseVersions]: finding all releases version associated to release id: [{}]", releaseId);
            // Not filter, get all release versions
            retList = release.getReleaseVersions();
        }
        else
        {
            log.debug("[ReleasesServiceImpl] -> [getReleaseVersions]: finding all releases version associated to release id: [{}] filter by status: [{}]", releaseId, status);

            // Filter by release status
            ReleaseVersionStatus releaseVersionStatus;
            try
            {
                releaseVersionStatus = ReleaseVersionStatus.valueOf(status);
            }
            catch (IllegalArgumentException e)
            {
                throw new NovaException(ReleaseError.getInvalidReleaseVersionStatus(status, Arrays.toString(ReleaseVersionStatus.values())));
            }

            retList = release.getReleaseVersions().stream().filter(x -> x.getStatus() == releaseVersionStatus).collect(Collectors.toList());
        }

        this.sortReleaseVersions(retList);

        log.debug("[ReleasesServiceImpl] -> [getReleaseVersions]: found the following releases version: [{}]", retList);
        return this.dtoBuilder.buildDtoFromReleaseVersionList(retList);
    }

    @Override
    public RELReleaseInfo[] getAllReleasesAndServices(final Integer productId)
    {
        try
        {
            return this.dtoBuilder.buildReleaseInfoList(productId);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    public ReleaseDto[] getProductReleases(final String ivUser, final Integer productId)
    {
        try
        {
            // First of all check if product does exist in NOVA.
            // Will throw an exception if not.
            Product product = this.productRepository.findById(productId).orElse(null);
            this.validator.checkProductExistence(productId, product);

            // Get list.
            List<Release> releaseList = product.getReleases();

            // Convert to DTO array as expected by the API.
            ReleaseDto[] dtoArray = this.dtoBuilder.buildDtoArrayFromEntityList(releaseList);

            log.debug("List of Releases from Product id: {} requested by PortalUser: {}. DtoArray: [{}]", productId, ivUser, dtoArray);

            return dtoArray;
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    public ReleaseDtoByEnvironment[] getReleasesProductConfiguration(final String ivUser, final Integer productId, String environment)
    {
        try
        {
            // First of all check if product does exist in NOVA.
            // Will throw an exception if not.
            Product product = this.productRepository.findById(productId).orElse(null);
            this.validator.checkProductExistence(productId, product);

            // Get list.
            List<Release> releaseList = product.getReleases();

            // Convert to DTO array as expected by the API.
            ReleaseDtoByEnvironment[] releaseDtoByEnvironmentArray = this.dtoBuilder.buildReleaseDTOByEnvArrayFromEntityList(releaseList, environment);

            log.debug("List of Releases from Product id: {} requested by PortalUser: {}. DtoArray: [{}]", productId, ivUser, releaseDtoByEnvironmentArray);

            return releaseDtoByEnvironmentArray;
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }



    // ############################ PRIVATE METHODS ############################

    private void updateSelectedPlatforms(
            final ReleaseConfigDto releaseConfig, final Integer releaseId,
            final ReleaseConstants.ENVIRONMENT environment)
    {
        // Check if the Release does exist.
        Release release = this.validator.checkReleaseExistence(releaseId);

        Product product = release.getProduct();
        this.validator.checkProductExistence(product);

        //check the default and enabled values
        ReleaseEnvConfigDto releaseEnvConfig;
        switch (environment)
        {
            case INT:
                releaseEnvConfig = releaseConfig.getIntConfig();
                log.debug(
                        "[ReleasesAPI] -> [updateReleaseConfig]: configuring release with releaseId [{}], environment INT "
                                + " with [{}]", releaseId, releaseEnvConfig.getSelectedPlatforms());

                // Check that the selected values is inside the enabled options and save
                final Platform selectedDeployInt = validator.checkInputDeploymentPlatformValue(
                        product.getDeployPlatformsAvailableByEnv(Environment.INT.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedDeployInt(),
                        environment.name());
                release.setSelectedDeployInt(selectedDeployInt);

                final Platform selectedLoggingInt = validator.checkInputLoggingPlatformValue(
                        product.getLoggingPlatformsAvailableByEnv(Environment.INT.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedLoggingInt(),
                        selectedDeployInt,
                        environment.name());
                release.setSelectedLoggingInt(selectedLoggingInt);

                // Emit Release Configuration Product Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.RELEASE_CONFIGURED)
                        .entityId(releaseId)
                        .environment(environment.name())
                        .addParam("destinationPlatformDeployType", release.getSelectedDeployInt())
                        .addParam("destinationPlatformLoggingType", release.getSelectedLoggingInt())
                        .build());

                break;
            case PRE:
                releaseEnvConfig = releaseConfig.getPreConfig();
                log.debug(
                        "[ReleasesAPI] -> [updateReleaseConfig]: configuring release with releaseId [{}], environment PRE "
                                + " with [{}]", releaseId, releaseEnvConfig.getSelectedPlatforms());

                // Check that the selected values is inside the enabled options and save
                final Platform selectedDeployPre = validator.checkInputDeploymentPlatformValue(
                        product.getDeployPlatformsAvailableByEnv(Environment.PRE.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedDeployPre(),
                        environment.name());
                release.setSelectedDeployPre(selectedDeployPre);
                final Platform selectedLoggingPre = validator.checkInputLoggingPlatformValue(
                        product.getLoggingPlatformsAvailableByEnv(Environment.PRE.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedLoggingPre(),
                        selectedDeployPre,
                        environment.name());
                release.setSelectedLoggingPre(selectedLoggingPre);

                // Emit Release Configuration Product Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.RELEASE_CONFIGURED)
                        .entityId(releaseId)
                        .environment(environment.name())
                        .addParam("destinationPlatformDeployType", release.getSelectedDeployPre())
                        .addParam("destinationPlatformLoggingType", release.getSelectedLoggingPre())
                        .build());

                break;
            case PRO:
                releaseEnvConfig = releaseConfig.getProConfig();
                log.debug(
                        "[ReleasesAPI] -> [updateReleaseConfig]: configuring release with releaseId [{}], environment PRO "
                                + " with [{}]", releaseId, releaseEnvConfig.getSelectedPlatforms());

                // Check that the selected values is inside the enabled options and save
                final Platform selectedDeployPro = validator.checkInputDeploymentPlatformValue(
                        product.getDeployPlatformsAvailableByEnv(Environment.PRO.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedDeployPro(),
                        environment.name());
                release.setSelectedDeployPro(selectedDeployPro);
                final Platform selectedLoggingPro = validator.checkInputLoggingPlatformValue(
                        product.getLoggingPlatformsAvailableByEnv(Environment.PRO.getEnvironment()),
                        releaseEnvConfig,
                        release.getSelectedLoggingPro(),
                        selectedDeployPro,
                        environment.name());
                release.setSelectedLoggingPro(selectedLoggingPro);

                // Emit Release Configuration Product Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.RELEASE_CONFIGURED)
                        .entityId(releaseId)
                        .environment(environment.name())
                        .addParam("destinationPlatformDeployType", release.getSelectedDeployPro())
                        .addParam("destinationPlatformLoggingType", release.getSelectedLoggingPro())
                        .build());
        }

        // Flush to the database
        releaseRepository.saveAndFlush(release);
    }

    /**
     * Update release Management Preproduction Config
     *
     * @param managementPreConfigDto config to update
     * @param releaseId              release to update
     * @param environment            environment
     */
    private void updateReleaseManagementInPre(final ManagementConfigDto managementPreConfigDto, final Integer releaseId, final ReleaseConstants.ENVIRONMENT environment)
    {
        log.debug("[ReleaseServiceImpl] -> [updateReleaseManagementInPre]: Changing release with id [{}], deploy config in PRE to -> ManagementConfigDto: [{}].",
                releaseId, managementPreConfigDto.toString());

        // Check if the Release does exist.
        Release release = this.validator.checkReleaseExistence(releaseId);

        Product product = release.getProduct();
        this.validator.checkProductExistence(product);

        // Auto manage in Pre
        TimeInterval dtoAutomanageInterval = new TimeInterval();
        if (release.getAutomanageInPre() == null)
        {
            release.setAutomanageInPre(new TimeInterval());
        }
        if (managementPreConfigDto.getAutoManage() != null)
        {
            dtoAutomanageInterval = this.dtoBuilder.createEntityFromDto(managementPreConfigDto.getAutoManage());
        }

        // If defaultAutomanageInpre is active OR no chanegs in automanage, we can set the values
        if (product.getDefaultAutomanageInPre() || this.compareTimeInterval(release.getAutomanageInPre(), dtoAutomanageInterval))
        {
            release.setAutomanageInPre(dtoAutomanageInterval);
        }
        else
        {
            throw new NovaException(ReleaseError.getDefaultAutoManageInPreError());
        }

        // Auto deploy in Pre
        TimeInterval dtoAutodeployInterval = new TimeInterval();
        if (release.getAutodeployInPre() == null)
        {
            release.setAutodeployInPre(new TimeInterval());
        }
        if (managementPreConfigDto.getAutoDeploy() != null)
        {
            dtoAutodeployInterval = this.dtoBuilder.createEntityFromDto(managementPreConfigDto.getAutoDeploy());
        }

        // If defaultAutodeployInpre is active OR no chanegs in autodeploy, we can set the values
        if (product.getDefaultAutodeployInPre() || this.compareTimeInterval(release.getAutodeployInPre(), dtoAutodeployInterval))
        {
            release.setAutodeployInPre(dtoAutodeployInterval);
        }
        else
        {
            throw new NovaException(ReleaseError.getDefaultAutoDeployInPreError());
        }

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.RELEASE_CONFIGURED)
                .entityId(releaseId)
                .environment(environment.name())
                .addParam("autodeployInPre", release.getAutodeployInPre())
                .build());

        // Store the release with changes
        this.releaseRepository.save(release);

        log.debug("[ReleasesAPI] -> [updateReleaseManagementInPre]: Changed release with id [{}], deploy config in PRE to -> managementConfigDto: [{}].",
                releaseId, managementPreConfigDto);
    }

    /**
     * Update release Management Production Config
     *
     * @param managementProConfigDto config to update
     * @param releaseId              release to update
     * @param environment            environment
     */
    private void updateReleaseManagementInPro(final ManagementConfigDto managementProConfigDto,
                                              final Integer releaseId, final ReleaseConstants.ENVIRONMENT environment)
    {
        log.debug("[ReleaseServiceImpl] -> [updateReleaseManagementInPro]: Changing release with id [{}], deploy config in PRO to -> ManagementConfigDto: [{}].",
                releaseId, managementProConfigDto.toString());

        // Check if the Release does exist.
        Release release = this.validator.checkReleaseExistence(releaseId);

        Product product = release.getProduct();
        this.validator.checkProductExistence(product);

        // Set deployment Type in Production for this release (NOVA_PLANNED, PLANNED or ON_DEMAND)
        if (managementProConfigDto.getDeploymentType() != null)
        {
            release.setDeploymentTypeInPro(DeploymentType.valueOf(managementProConfigDto.getDeploymentType()));
        }
        else
        {

            throw new NovaException(ReleaseError.getInvalidDeploymentTypeError());
        }

        // Auto manage in Pro
        TimeInterval dtoAutomanageInterval = new TimeInterval();

        if (release.getAutoManageInPro() == null)
        {
            release.setAutoManageInPro(new TimeInterval());
        }
        if (managementProConfigDto.getAutoManage() != null)
        {
            dtoAutomanageInterval = this.dtoBuilder.createEntityFromDto(managementProConfigDto.getAutoManage());
        }

        // SI defaultAtomanageInpro o no hay cambios en el automanage, podemos seguir
        if (product.getDefaultAutomanageInPro() || dtoAutomanageInterval.equals(release.getAutoManageInPro()))
        {
            release.setAutoManageInPro(dtoAutomanageInterval);
        }
        else
        {
            throw new NovaException(ReleaseError.getDefaultAutoManageInProError());
        }

        // Auto deploy in Pro
        TimeInterval dtoAutodeployInterval = new TimeInterval();
        if (release.getAutodeployInPro() == null)
        {
            release.setAutodeployInPro(new TimeInterval());
        }
        if (managementProConfigDto.getAutoDeploy() != null)
        {
            dtoAutodeployInterval = this.dtoBuilder.createEntityFromDto(managementProConfigDto.getAutoDeploy());
        }

        // SI defaultAutodeployInpro OR no hay cambios en el automanage, podemos seguir
        if (product.getDefaultAutodeployInPro() || dtoAutodeployInterval.equals(release.getAutodeployInPro()))
        {
            release.setAutodeployInPro(dtoAutodeployInterval);
        }
        else
        {
            throw new NovaException(ReleaseError.getDefaultAutoDeployInProError());
        }

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.RELEASE_CONFIGURED)
                .entityId(releaseId)
                .environment(environment.name())
                .addParam("autodeployInPro", release.getAutodeployInPro())
                .build());

        // Store the release with changes
        this.releaseRepository.save(release);

        log.debug("[ReleasesAPI] -> [updateReleaseManagementInPro]: Changed release with id [{}], deploy config in PRO to -> managementConfigDto: [{}].",
                releaseId, managementProConfigDto);
    }

    /**
     * Update release management config by enviroment (automanage, autodeploy, deployment type)
     *
     * @param releaseConfig config to update
     * @param releaseId     release
     * @param environment   environment
     */
    private void updateManagementConfig(final ReleaseConfigDto releaseConfig,
                                        final Integer releaseId, final ReleaseConstants.ENVIRONMENT environment)
    {

        switch (environment)
        {
            case PRE:
                if (releaseConfig.getPreConfig() != null && releaseConfig.getPreConfig().getManagementConfig() != null)
                {
                    this.updateReleaseManagementInPre(releaseConfig.getPreConfig().getManagementConfig(), releaseId, environment);
                }
                break;
            case PRO:
                if (releaseConfig.getProConfig() != null &&
                        releaseConfig.getProConfig().getManagementConfig() != null)
                {
                    this.updateReleaseManagementInPro(releaseConfig.getProConfig().getManagementConfig(), releaseId, environment);
                }
                break;
            case INT:
                //DO NOTHING
                break;
            default:
                throw new NovaException(ReleaseError.getInvalidEnvironmentError(), "Invalid Environment");
        }
    }

    /**
     * Compare two time intervals - True if are equals
     *
     * @param timeInterval      time interval to compare
     * @param otherTimeInterval another time interval to compare
     * @return True if both time intervals are equals
     */
    private boolean compareTimeInterval(TimeInterval timeInterval, TimeInterval otherTimeInterval)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Get start dates
        String start = "";
        if (timeInterval.getStart() != null)
        {
            start = formatter.format(timeInterval.getStart());
        }
        String otherStart = "";
        if (otherTimeInterval.getStart() != null)
        {
            otherStart = formatter.format(otherTimeInterval.getStart());
        }

        // Get end dates
        String end = "";
        if (timeInterval.getEnd() != null)
        {
            end = formatter.format(timeInterval.getEnd());
        }
        String otherEnd = "";
        if (otherTimeInterval.getEnd() != null)
        {
            otherEnd = formatter.format(otherTimeInterval.getEnd());
        }

        // True if all dates are equals
        return start.equals(otherStart) && end.equals(otherEnd);
    }


    /**
     * Sort release versions by version name
     *
     * @param retList list
     */
    private void sortReleaseVersions(List<ReleaseVersion> retList)
    {
        retList.sort(new Comparator<ReleaseVersion>()
        {
            @Override
            public int compare(ReleaseVersion o1, ReleaseVersion o2)
            {
                return o1.getVersionName().compareTo(o2.getVersionName());
            }
        });
    }

}

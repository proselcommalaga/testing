package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.configurationmanagerapi.client.feign.nova.rest.IRestHandlerConfigurationmanagerapi;
import com.bbva.enoa.apirestgen.configurationmanagerapi.client.feign.nova.rest.IRestListenerConfigurationmanagerapi;
import com.bbva.enoa.apirestgen.configurationmanagerapi.client.feign.nova.rest.impl.RestHandlerConfigurationmanagerapi;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.CMLogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.PropertiesExtraRequest;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.PropertiesFileRequest;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.PropertiesRequest;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.common.model.param.ServiceOperationParams;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Configurations client
 */
@Service
public class ConfigurationmanagerClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationmanagerClient.class);

    /**
     * IRest Handler Configuration manager api interfaces
     */
    private final IRestHandlerConfigurationmanagerapi iRestHandlerConfigurationmanagerapi;

    /**
     * Rest Handler Configuration manager api services.
     */
    private RestHandlerConfigurationmanagerapi restHandlerConfigurationmanagerapi;

    /**
     * Dependencies injector constructor
     *
     * @param iRestHandlerConfigurationmanagerapi iRestHandlerConfigurationmanagerapi
     */
    @Autowired
    public ConfigurationmanagerClient(final IRestHandlerConfigurationmanagerapi iRestHandlerConfigurationmanagerapi)
    {
        this.iRestHandlerConfigurationmanagerapi = iRestHandlerConfigurationmanagerapi;
    }

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerConfigurationmanagerapi = new RestHandlerConfigurationmanagerapi(this.iRestHandlerConfigurationmanagerapi);
    }

    /////////////////////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////////////////

    /**
     * Save the current configuration revision plan of the deployment plan provided as parameter
     *
     * @param planId the plan id to get the deployment plan
     */
    public void saveCurrentConfigurationRevision(final Integer planId)
    {
        long initTime = System.currentTimeMillis();
        LOG.debug("Calling saveCurrentConfigurationRevision. INIT {}", initTime);
        this.restHandlerConfigurationmanagerapi.saveCurrentConfigurationRevision(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void saveCurrentConfigurationRevision()
                    {
                        LOG.info("[ConfigurationManagerAPI Client] -> [saveCurrentConfigurationRevision]: Saved the current configuration" +
                                " revision of the " +
                                "deployment plan id: [{}] successfully", planId);
                    }

                    @Override
                    public void saveCurrentConfigurationRevisionErrors(final Errors outcome)
                    {
                        String message = "[ConfigurationManagerAPI Client] -> [saveCurrentConfigurationRevision]: Failed saving the current configuration revision " +
                                " of the deployment plan id: [" + planId + "]. Exception: [" + outcome.getBodyExceptionMessage() + "]";
                        LOG.error(message);
                        throw new NovaException(DeploymentError.getSavingConfigurationsError(), message);
                    }
                }, planId);

        LOG.debug("Called saveCurrentConfigurationRevision. INIT {}. END {}", initTime, System.currentTimeMillis() - initTime);
    }

    /**
     * Get the logical connector properties Dto from logical connector by user code.
     * Return the logical connector properties Dto if the call was successfully.
     * If not, return a empty logical connector property Dto array.
     *
     * @param logicalConnectorId the logical connector id
     * @return a CMLogicalConnectorPropertyDto instance
     */
    public CMLogicalConnectorPropertyDto[] getLogicalConnectorPropertiesDto(final Integer logicalConnectorId)
    {
        SingleApiClientResponseWrapper<CMLogicalConnectorPropertyDto[]> atomicLogicalConnectorPropertyDto = new SingleApiClientResponseWrapper<>();

        this.restHandlerConfigurationmanagerapi.getLogicalConnectorProperties(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void getLogicalConnectorProperties(final CMLogicalConnectorPropertyDto[] logicalConnectorPropertyDtoArray)
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [getLogicalConnectorProperties]: get the logical connector properties dto from logical " +
                                "connector id: [{}] successfully", logicalConnectorId);
                        atomicLogicalConnectorPropertyDto.set(logicalConnectorPropertyDtoArray);
                    }

                    @Override
                    public void getLogicalConnectorPropertiesErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [getLogicalConnectorProperties]: there was an error trying to get the logical connector properties dto from logical " +
                                "connector id: [{}]. Error: [{}]", logicalConnectorId, outcome.getBodyExceptionMessage());
                        atomicLogicalConnectorPropertyDto.set(new CMLogicalConnectorPropertyDto[0]);
                    }
                }, logicalConnectorId);

        return atomicLogicalConnectorPropertyDto.get();
    }

    /**
     * Delete the current configuration revision (properties list of the configuration value and connectors) when a plan is undeployed from the Config server data base
     *
     * @param deploymentPlan the deployment plan to remove the configuration revision
     * @return true if the operation was success. False in any case
     */
    public boolean deleteCurrentConfigurationRevision(final DeploymentPlan deploymentPlan)
    {
        SingleApiClientResponseWrapper<Boolean> results = new SingleApiClientResponseWrapper<>();

        this.restHandlerConfigurationmanagerapi.deleteCurrentConfigurationRevision(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void deleteCurrentConfigurationRevision()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [deleteCurrentConfigurationRevision]: deleted all the properties (Template and Connectors)" +
                                "of the current configuration revision of the deployment plan id: [{}].", deploymentPlan.getId());
                        results.set(true);
                    }

                    @Override
                    public void deleteCurrentConfigurationRevisionErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [deleteCurrentConfigurationRevision]: there was an error trying to delete all the properties (template and Connectors)" +
                                        " of the current configuration revision of the deployment plan: [{}]. Currenrt configuration Revision id: [{}] - Configuration Manager error response: [{}]",
                                deploymentPlan, deploymentPlan.getCurrentRevision().getId(), outcome.getBodyExceptionMessage());
                        results.set(false);
                    }
                }, deploymentPlan.getId());

        return results.get();
    }

    /**
     * Validate the service template file of the release version service
     *
     * @param modulePath              the path where the template file is
     * @param tag                     the tag of the release
     * @param versionControlServiceId the version control service id
     * @param serviceType             the service type for validating the template.yml
     * @param ivUser                  user code of the requester
     * @return true if the service template file was successfully. False in any case
     */
    public boolean validateServiceTemplateFile(final String modulePath, final String tag, final Integer versionControlServiceId,
                                               final String serviceType, final String ivUser)
    {
        SingleApiClientResponseWrapper<Boolean> results = new SingleApiClientResponseWrapper<>();

        // Get the process template request instance
        PropertiesFileRequest propertiesFileRequest = this.createProcessTemplateRequest(modulePath, tag, versionControlServiceId, serviceType, Collections.emptyList());

        this.restHandlerConfigurationmanagerapi.validateServicePropertiesFile(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void validateServicePropertiesFile()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [validateServiceTemplate]: validated the service template file. " +
                                "UserRequester: [{}]. Parameters: [{}]", ivUser, propertiesFileRequest);
                        results.set(true);
                    }

                    @Override
                    public void validateServicePropertiesFileErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [validateServiceTemplate]: failed the validation the service template file. " +
                                "UserRequester: [{}]. Parameters: [{}]. Error: [{}]", ivUser, propertiesFileRequest, outcome.getBodyExceptionMessage());
                        results.set(false);
                    }
                }, propertiesFileRequest);

        return results.get();
    }

    /**
     * Validate service novaYml file. Set errors validations if some file contains errors.
     *
     * @param serviceName             the service name
     * @param modulePath              the path where the template file is
     * @param tag                     the tag of the release
     * @param versionControlServiceId the version control service id
     * @param serviceType             the service type for validating the template.yml
     * @param ivUser                  user code of the requester
     * @param errorList               the list with validation errors
     * @return true if the service template file was successfully. False in any case
     */
    public boolean validateServiceNovaYmlFile(final String serviceName, final String modulePath, final String tag, final Integer versionControlServiceId,
                                              final String serviceType, final String ivUser, List<ValidationErrorDto> errorList)
    {
        SingleApiClientResponseWrapper<Boolean> results = new SingleApiClientResponseWrapper<>();

        // Get the process template request instance
        PropertiesFileRequest propertiesFileRequest = this.createProcessTemplateRequest(modulePath, tag, versionControlServiceId, serviceType, Collections.emptyList());

        this.restHandlerConfigurationmanagerapi.validateServicePropertiesFile(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void validateServicePropertiesFile()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [validateServiceNovaYmlFile]: validated the service template file. " +
                                "UserRequester: [{}]. Parameters: [{}]", ivUser, propertiesFileRequest);
                        results.set(true);
                    }

                    @Override
                    public void validateServicePropertiesFileErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [validateServiceNovaYmlFile]: failed the validation of novaYml file. " +
                                "UserRequester: [{}]. Parameters: [{}]. Error: [{}]", ivUser, propertiesFileRequest, outcome.getBodyExceptionMessage());
                        // Add error to errorList in order to show it in the portal.
                        Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
                        String errorCode = "";
                        String errorMessage = "";
                        if (firstErrorMessage.isPresent())
                        {
                            ErrorMessage message = firstErrorMessage.get();
                            errorCode = message.getCode();
                            errorMessage = message.getMessage();
                        }
                        ErrorListUtils
                                .addError(errorList, serviceName, errorCode, errorMessage);
                        results.set(false);
                    }
                }, propertiesFileRequest);

        return results.get();
    }

    /**
     * Validate service novaYml file. Set errors validations if some file contains errors.
     *
     * @param serviceName             the service name
     * @param modulePath              the path where the template file is
     * @param tag                     the tag of the release
     * @param versionControlServiceId the version control service id
     * @param serviceType             the service type for validating the template.yml
     * @param ivUser                  user code of the requester
     * @param errorList               the list with validation errors
     * @return true if the service template file was successfully. False in any case
     */
    public boolean validateBehaviorServiceNovaYmlFile(final String serviceName, final String modulePath, final String tag, final Integer versionControlServiceId,
                                                      final String serviceType, final String ivUser, List<ValidationErrorDto> errorList)
    {
        SingleApiClientResponseWrapper<Boolean> results = new SingleApiClientResponseWrapper<>();

        // Get the process template request instance
        PropertiesFileRequest propertiesFileRequest = this.createProcessTemplateRequest(modulePath, tag, versionControlServiceId, serviceType, Collections.emptyList());

        this.restHandlerConfigurationmanagerapi.validateBehaviorServicePropertiesFile(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void validateBehaviorServicePropertiesFile()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [validateBehaviorServicePropertiesFile]: validated the service template file. " +
                                "UserRequester: [{}]. Parameters: [{}]", ivUser, propertiesFileRequest);
                        results.set(true);
                    }

                    @Override
                    public void validateBehaviorServicePropertiesFileErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [validateBehaviorServicePropertiesFile]: failed the validation of novaYml file. " +
                                "UserRequester: [{}]. Parameters: [{}]. Error: [{}]", ivUser, propertiesFileRequest, outcome.getBodyExceptionMessage());
                        // Add error to errorList in order to show it in the portal.
                        Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
                        String errorCode = "";
                        String errorMessage = "";
                        if (firstErrorMessage.isPresent())
                        {
                            ErrorMessage message = firstErrorMessage.get();
                            errorCode = message.getCode();
                            errorMessage = message.getMessage();
                        }
                        ErrorListUtils
                                .addError(errorList, serviceName, errorCode, errorMessage);
                        results.set(false);
                    }
                }, propertiesFileRequest);

        return results.get();
    }

    /**
     * Set the properties definition of the release version service from the template file of the release version service
     *
     * @param params Necessary parameters for setting property definitions.
     */
    public void setPropertiesDefinitionService(ServiceOperationParams params)
    {

        PropertiesRequest propertiesRequest = this.buildPropertiesRequest(params.getModulePath(), params.getTag(), params.getVersionControlServiceId(), params.getServiceType(), params.getLibraries(), params.getExtraProperties());
        Integer releaseVersionServiceId = params.getReleaseVersionServiceId();
        String ivUser = params.getIvUser();

        this.restHandlerConfigurationmanagerapi.setPropertiesDefinitionService(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void setPropertiesDefinitionService()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [setPropertiesDefinitionService]: set property " +
                                        "definition list from the service template file of the release version service: [{}]. UserRequester: [{}]. Parameters: [{}]",
                                releaseVersionServiceId, ivUser, propertiesRequest);
                    }

                    @Override
                    public void setPropertiesDefinitionServiceErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [setPropertiesDefinitionService]: failed trying to" +
                                        " set a property definition from the service template file of the release " +
                                        "version service: [{}]. UserRequester: [{}]. Parameters: [{}]. Error: {}",
                                releaseVersionServiceId, ivUser, propertiesRequest, outcome.getBodyExceptionMessage());
                    }
                }, propertiesRequest, releaseVersionServiceId);
    }

    /**
     * Set the properties of the behavior version service from the template file of the behavior version service
     *
     * @param params Necessary parameters for setting property definitions.
     */
    public void setPropertiesBehaviorService(ServiceOperationParams params)
    {

        PropertiesRequest propertiesRequest = this.buildBehaviorPropertiesRequest(params.getModulePath(), params.getTag(), params.getVersionControlServiceId(), params.getServiceType());
        Integer releaseVersionServiceId = params.getReleaseVersionServiceId();
        String ivUser = params.getIvUser();

        this.restHandlerConfigurationmanagerapi.setPropertiesBehaviorService(
                new IRestListenerConfigurationmanagerapi()
                {
                    @Override
                    public void setPropertiesBehaviorService()
                    {
                        LOG.debug("[ConfigurationManagerAPI Client] -> [setPropertiesDefinitionService]: set property " +
                                        "definition list from the service template file of the behavior version service: [{}]. UserRequester: [{}]. Parameters: [{}]",
                                releaseVersionServiceId, ivUser, propertiesRequest);
                    }

                    @Override
                    public void setPropertiesBehaviorServiceErrors(final Errors outcome)
                    {
                        LOG.error("[ConfigurationManagerAPI Client] -> [setPropertiesDefinitionService]: failed trying to" +
                                        " set a property definition from the service template file of the behavior " +
                                        "version service: [{}]. UserRequester: [{}]. Parameters: [{}]. Error: {}",
                                releaseVersionServiceId, ivUser, propertiesRequest, outcome.getBodyExceptionMessage());
                    }
                }, propertiesRequest, releaseVersionServiceId);
    }


    //////////////////////////////////////// PRIVATE METHODS ///////////////////////////////////////////////////////////


    /**
     * Build a properties Request DTO to send to configuration Manager
     *
     * @param modulePath              module path
     * @param tag                     tag
     * @param versionControlServiceId vcsId
     * @param serviceType             service type
     * @param libraries               libraries
     * @param extraProperties         extraProerties
     * @return propertiesRequest
     */
    private PropertiesRequest buildPropertiesRequest(final String modulePath, final String tag, final Integer versionControlServiceId, final String serviceType, final List<LMUsedLibrariesDTO> libraries, final Map<String, Map<String, String>> extraProperties)
    {
        PropertiesRequest propertiesRequest = new PropertiesRequest();

        // Get the process template request instance
        PropertiesFileRequest propertiesFileRequest = this.createProcessTemplateRequest(modulePath, tag, versionControlServiceId, serviceType, libraries);

        // Get the extraParameters DTO
        List<PropertiesExtraRequest> propertiesExtraRequestList = new ArrayList<>();
        if (extraProperties != null)
        {
            extraProperties.forEach((propertyTypeKey, propertyMapValue) -> {
                if (propertyMapValue != null)
                {
                    propertyMapValue.forEach((key, value) -> {
                        PropertiesExtraRequest propertiesExtraRequest = new PropertiesExtraRequest();
                        propertiesExtraRequest.setName(key);
                        propertiesExtraRequest.setValue(value);
                        propertiesExtraRequest.setType(propertyTypeKey);
                        propertiesExtraRequestList.add(propertiesExtraRequest);
                    });
                }
            });
        }

        propertiesRequest.setPropertiesFileRequest(propertiesFileRequest);
        propertiesRequest.setPropertiesExtraRequest(propertiesExtraRequestList.toArray(PropertiesExtraRequest[]::new));
        return propertiesRequest;
    }

    /**
     * Build a properties Request DTO to send to configuration Manager
     *
     * @param modulePath              module path
     * @param tag                     tag
     * @param versionControlServiceId vcsId
     * @param serviceType             service type
     * @return propertiesRequest
     */
    private PropertiesRequest buildBehaviorPropertiesRequest(final String modulePath, final String tag, final Integer versionControlServiceId, final String serviceType)
    {
        PropertiesRequest propertiesRequest = new PropertiesRequest();

        // Get the process template request instance
        PropertiesFileRequest propertiesFileRequest = this.createBehaviorProcessTemplateRequest(modulePath, tag, versionControlServiceId, serviceType);

        propertiesRequest.setPropertiesFileRequest(propertiesFileRequest);
        return propertiesRequest;
    }

    /**
     * Create a process template request instance
     *
     * @param modulePath              the path of the module
     * @param tag                     the tag of the service
     * @param versionControlServiceId the version control service id
     * @return a ProcessTemplateRequest instnace
     */
    private PropertiesFileRequest createProcessTemplateRequest(final String modulePath, final String tag,
                                                               final Integer versionControlServiceId, final String serviceType, final List<LMUsedLibrariesDTO> libraries)
    {
        PropertiesFileRequest propertiesFileRequest = new PropertiesFileRequest();

        propertiesFileRequest.setVersionControlSystemProjectId(versionControlServiceId);
        propertiesFileRequest.setModulePath(modulePath);
        propertiesFileRequest.setTag(tag);
        propertiesFileRequest.setServiceType(serviceType);

        propertiesFileRequest.setLibraries(libraries.stream().mapToInt(LMUsedLibrariesDTO::getReleaseVersionServiceId).toArray());

        return propertiesFileRequest;
    }

    /**
     * Create a process template request instance
     *
     * @param modulePath              the path of the module
     * @param tag                     the tag of the service
     * @param versionControlServiceId the version control service id
     * @return a ProcessTemplateRequest instnace
     */
    private PropertiesFileRequest createBehaviorProcessTemplateRequest(final String modulePath, final String tag,
                                                                       final Integer versionControlServiceId, final String serviceType)
    {
        PropertiesFileRequest propertiesFileRequest = new PropertiesFileRequest();

        propertiesFileRequest.setVersionControlSystemProjectId(versionControlServiceId);
        propertiesFileRequest.setModulePath(modulePath);
        propertiesFileRequest.setTag(tag);
        propertiesFileRequest.setServiceType(serviceType);

        return propertiesFileRequest;
    }
}

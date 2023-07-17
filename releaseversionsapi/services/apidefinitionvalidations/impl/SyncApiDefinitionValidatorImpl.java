package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiConsumedBy;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlApi;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ComparisonUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.*;

/**
 * Swagger Validator
 */
@Service
@Slf4j
@AllArgsConstructor
public class SyncApiDefinitionValidatorImpl implements IApiDefinitionValidator
{

    private final SyncApiVersionRepository syncApiVersionRepository;
    private final IVersioncontrolsystemClient versioncontrolsystemClient;
    private final ProductRepository productRepository;
    private final ReleaseVersionServiceRepository releaseVersionServiceRepository;
    private final SwaggerConverter swaggerConverter;

    /**
     * It is a real validator
     */
    private static final boolean IS_VALIDATOR = true;

    /**
     * Check if it is the resolver of validators or a real validator
     *
     * @return true if it is a real validator. False if it is the resolver
     */
    @Override
    public boolean isValidator()
    {
        return IS_VALIDATOR;
    }

    /**
     * Validate Swagger
     *
     * @param novaYml                     Nova.yml with the api's info
     * @param newReleaseVersionServiceDto dto
     * @param repoId                      repository id
     * @param tag                         tag
     */
    @Override
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateApi(NovaYml novaYml, NewReleaseVersionServiceDto newReleaseVersionServiceDto, int repoId,
                                                            String tag, String releaseName)
    {
        log.info("[SyncApiDefinitionValidatorImpl] -> [validateAndAssociateApi]: Starting validation in Thread: " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + " for repoId:" + repoId +
                " tag:" + tag + " releaseName:" + releaseName);

        // Create errorList
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Check if the served API is unique
        List<RVApiDTO> servedApiList = new ArrayList<>();

        for (NovaYmlApi api : novaYml.getApiServed())
        {
            RVApiDTO apiDto = new RVApiDTO();
            this.validateApi(api.getApi(), newReleaseVersionServiceDto, repoId, tag, ImplementedAs.SERVED.name(),
                    errorList, apiDto, releaseName, api.getBackwardCompatibleVersions(), servedApiList);
            for (String consumedApi : api.getConsumedApi())
            {
                this.validateApi(consumedApi, newReleaseVersionServiceDto, repoId, tag, ImplementedAs.CONSUMEDBY.name(),
                        errorList, apiDto, "", new ArrayList<>(), servedApiList);
            }
            for (String consumedExternalApi : api.getExternalApi())
            {
                this.validateApi(consumedExternalApi, newReleaseVersionServiceDto, repoId, tag, ImplementedAs.CONSUMEDBY_EXTERNAL.name(),
                        errorList, apiDto, "", new ArrayList<>(), servedApiList);
            }
        }

        for (NovaYmlApi api : novaYml.getApiConsumed())
        {
            RVApiDTO apiDto = new RVApiDTO();
            this.validateApi(api.getApi(), newReleaseVersionServiceDto, repoId, tag, ImplementedAs.CONSUMED.name(),
                    errorList, apiDto, "", new ArrayList<>(), servedApiList);
        }
        for (NovaYmlApi api : novaYml.getApiExternal())
        {
            RVApiDTO apiDto = new RVApiDTO();
            this.validateApi(api.getApi(), newReleaseVersionServiceDto, repoId, tag, ImplementedAs.EXTERNAL.name(),
                    errorList, apiDto, "", new ArrayList<>(), servedApiList);
        }

        return errorList;
    }

    ////////////////////////////////////// PRIVATE METHODS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    private void validateApi(String swaggerPath, NewReleaseVersionServiceDto newReleaseVersionServiceDto, int repoId,
                             String tag, String implementedAs, List<ValidationErrorDto> errorList, RVApiDTO apiDto,
                             String releaseName, List<String> backwardCompatibleVersions, List<RVApiDTO> servedApiList)
    {

        // Get the swagger file from VCS
        String filePath = newReleaseVersionServiceDto.getFolder() + "/" + swaggerPath;
        Swagger swaggerProject;
        try
        {
            swaggerProject = new SwaggerParser().parse(new String(this.versioncontrolsystemClient.getSwaggerFromProject(filePath, repoId, tag), StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            log.debug("[SyncApiDefinitionValidatorImpl] -> [validateApi]: Error reading swagger", e);
            swaggerProject = null;
        }

        if (swaggerProject == null)
        {
            log.warn("[SyncApiDefinitionValidatorImpl] -> [validateApi]: Swagger is null");
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_PATH_NOT_FOUND,
                    "For version " + releaseName + " and tag " + tag + API_PATH_NOT_FOUND_MSG);
        }
        else
        {
            SyncApiVersion apiVersion = this.getApiVersion(newReleaseVersionServiceDto, errorList, swaggerProject, implementedAs);
            if (apiVersion == null)
            {
                log.warn("[SyncApiDefinitionValidatorImpl] -> [validateApi]: the NOVA API section found problems for release version service: [{}] - swagger project base path: [{}] - implemented type: [{}]. Cannot create release version.",
                        releaseName, swaggerPath, implementedAs);
            }
            else
            {
                String basePathSwagger = this.validateStoredSwagger(newReleaseVersionServiceDto, errorList, swaggerProject, apiVersion, implementedAs);
                String basePathApi = this.apiBasePath(apiVersion, implementedAs, releaseName);

                if (Strings.isNullOrEmpty(basePathSwagger))
                {
                    log.warn("[SyncApiDefinitionValidatorImpl] -> [validateApi]: the swagger base path for release version service name: [{}] - implemented type: [{}] found some errors. Cannot create release version.",
                            releaseName, implementedAs);
                }
                else
                {
                    boolean hasPolicies = this.checkApiPoliciesTask(apiVersion, errorList, newReleaseVersionServiceDto.getServiceName());
                    this.checkApiSupportedVersions(apiVersion, newReleaseVersionServiceDto, errorList, backwardCompatibleVersions);
                    this.buildRVApiDto(newReleaseVersionServiceDto, apiVersion, implementedAs, errorList, basePathSwagger, basePathApi, apiDto, hasPolicies, releaseName, backwardCompatibleVersions, servedApiList);
                }
            }
        }
    }

    private boolean checkApiPoliciesTask(final SyncApiVersion apiVersion, final List<ValidationErrorDto> errorList, final String serviceName)
    {
        if (ApiPolicyStatus.arePoliciesSet(apiVersion.getApi().getPolicyStatus()))
        {
            return true;
        }
        else
        {
            ErrorListUtils.addError(errorList, serviceName, API_INCORRECT_POLICIES,
                    "Api " + apiVersion.getApi().getName() + ":" + apiVersion.getVersion() + API_INCORRECT_POLICIES_MSG);
            return false;
        }

    }

    private void buildRVApiDto(NewReleaseVersionServiceDto newReleaseVersionServiceDto, SyncApiVersion apiVersion,
                               String implementedAs, List<ValidationErrorDto> errorList, String basePathSwagger,
                               String basePathApi, RVApiDTO apiDto, boolean hasPolicies, String releaseName, List<String> backwardCompatibleVersions,
                               List<RVApiDTO> servedApiList)
    {
        SyncApi api = apiVersion.getApi();
        switch (ImplementedAs.valueOf(implementedAs))
        {
            case SERVED:
                apiDto.setUuaa(api.getProduct().getUuaa());
                apiDto.setProduct(api.getProduct().getName());
                apiDto.setProductId(api.getProduct().getId());
                apiDto.setId(apiVersion.getId());
                apiDto.setApiName(api.getName());
                apiDto.setVersion(apiVersion.getVersion());
                apiDto.setDescription(apiVersion.getDescription());
                apiDto.setBasePathSwagger(basePathSwagger);
                apiDto.setBasePathApi(basePathApi);
                apiDto.setExternal(false);
                apiDto.setPoliciesConfigured(hasPolicies);
                apiDto.setModality(api.getApiModality().getModality());
                apiDto.setSupportedVersions(backwardCompatibleVersions.toArray(new String[0]));
                this.addApiEntry(newReleaseVersionServiceDto, apiDto, implementedAs);
                this.validateServedApi(apiDto, newReleaseVersionServiceDto, errorList, releaseName, servedApiList);
                break;
            case CONSUMED:
                apiDto.setUuaa(api.getProduct().getUuaa());
                apiDto.setProduct(api.getProduct().getName());
                apiDto.setProductId(api.getProduct().getId());
                apiDto.setId(apiVersion.getId());
                apiDto.setApiName(api.getName());
                apiDto.setVersion(apiVersion.getVersion());
                apiDto.setDescription(apiVersion.getDescription());
                apiDto.setBasePathSwagger(basePathSwagger);
                apiDto.setBasePathApi(basePathApi);
                apiDto.setExternal(false);
                apiDto.setPoliciesConfigured(hasPolicies);
                apiDto.setModality(api.getApiModality().getModality());
                this.addApiEntry(newReleaseVersionServiceDto, apiDto, implementedAs);
                break;
            case EXTERNAL:
                apiDto.setUuaa(api.getUuaa());
                apiDto.setProduct(api.getUuaa());
                apiDto.setProductId(api.getProduct().getId());
                apiDto.setId(apiVersion.getId());
                apiDto.setApiName(api.getName());
                apiDto.setVersion(apiVersion.getVersion());
                apiDto.setDescription(apiVersion.getDescription());
                apiDto.setBasePathSwagger(basePathSwagger);
                apiDto.setBasePathApi(basePathApi);
                apiDto.setExternal(true);
                apiDto.setPoliciesConfigured(hasPolicies);
                apiDto.setModality(api.getApiModality().getModality());
                this.addApiEntry(newReleaseVersionServiceDto, apiDto, implementedAs);
                break;
            case CONSUMEDBY:
                RVApiConsumedBy apiConsumedBy = new RVApiConsumedBy();
                apiConsumedBy.setUuaa(api.getProduct().getUuaa());
                apiConsumedBy.setProduct(api.getProduct().getName());
                apiConsumedBy.setId(apiVersion.getId());
                apiConsumedBy.setApiName(api.getName());
                apiConsumedBy.setVersion(apiVersion.getVersion());
                apiConsumedBy.setDescription(apiVersion.getDescription());
                apiConsumedBy.setBasePath(basePathSwagger);
                apiConsumedBy.setProductId(api.getProduct().getId());
                apiConsumedBy.setExternal(false);
                apiConsumedBy.setPoliciesConfigured(hasPolicies);
                this.addConsumedEntry(apiDto, apiConsumedBy);
                break;
            case CONSUMEDBY_EXTERNAL:
                RVApiConsumedBy apiConsumedExternalBy = new RVApiConsumedBy();
                apiConsumedExternalBy.setUuaa(api.getUuaa());
                apiConsumedExternalBy.setProduct(api.getUuaa());
                apiConsumedExternalBy.setId(apiVersion.getId());
                apiConsumedExternalBy.setApiName(api.getName());
                apiConsumedExternalBy.setVersion(apiVersion.getVersion());
                apiConsumedExternalBy.setDescription(apiVersion.getDescription());
                apiConsumedExternalBy.setBasePath(basePathSwagger);
                apiConsumedExternalBy.setProductId(api.getProduct().getId());
                apiConsumedExternalBy.setExternal(true);
                apiConsumedExternalBy.setPoliciesConfigured(hasPolicies);
                this.addConsumedEntry(apiDto, apiConsumedExternalBy);
                break;
        }
    }

    /**
     * Validates the swagger and the base path stored
     *
     * @param newReleaseVersionServiceDto dto
     * @param errorList                   error list
     * @param swaggerProject              swagger project
     * @param apiVersion                  api list
     * @return a validated base path
     */
    private String validateStoredSwagger(NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                         List<ValidationErrorDto> errorList, Swagger swaggerProject,
                                         SyncApiVersion apiVersion, String implementedAs)
    {
        // Compare Swagger: database Swagger does not have the BasePath, it must not be compared
        Swagger swaggerStored = this.swaggerConverter.parseSwaggerFromString(apiVersion.getDefinitionFile().getContents());
        swaggerStored.setBasePath(swaggerProject.getBasePath());

        String basePath = this.validateStoredBasePath(swaggerProject.getBasePath(), implementedAs, swaggerStored);
        if (Arrays.equals(this.swaggerConverter.swaggerYamlToByteArray(swaggerStored), this.swaggerConverter.swaggerYamlToByteArray(swaggerProject)))
        {
            log.debug("[SyncApiDefinitionValidatorImpl] -> [validateStoredSwagger]: the swagger stored: [{}] is the same from swagger project: [{}]. Release version name: [{}] - NovaApi name: [{}]",
                    swaggerStored, swaggerProject, newReleaseVersionServiceDto.getFinalName(), apiVersion.getApi().getName());
        }
        else
        {
            log.warn("[SyncApiDefinitionValidatorImpl] -> [validateStoredSwagger]: the swagger stored: [{}] is NOT the same from swagger project: [{}]. Release version name: [{}] - NovaApi name: [{}]",
                    swaggerStored, swaggerProject, newReleaseVersionServiceDto.getFinalName(), apiVersion.getApi().getName());
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_MODIFIED, swaggerStored.getInfo().getTitle() + API_MODIFIED_MSG);
            basePath = null;
        }
        return basePath;
    }

    private String validateStoredBasePath(String basePath, String implementedAs,
                                          Swagger swaggerStored)
    {
        if (implementedAs.equalsIgnoreCase(ImplementedAs.CONSUMED.name()) || implementedAs.equalsIgnoreCase(ImplementedAs.CONSUMEDBY.name())
                || implementedAs.equalsIgnoreCase(ImplementedAs.EXTERNAL.name()) || implementedAs.equalsIgnoreCase(ImplementedAs.CONSUMEDBY_EXTERNAL.name()))
        {
            return basePath;
        }
        else
        {
            return swaggerStored.getBasePath();
        }
    }

    /**
     * Set used api list
     *
     * @param apiVersion api list
     */
    private String apiBasePath(final SyncApiVersion apiVersion, final String implementedAs, final String releaseVersionName)
    {
        String basePath = "";
        // Compare Swaggers

        switch (ImplementedAs.valueOf(implementedAs))
        {
            case SERVED:
                basePath = this.getBasePath(apiVersion, releaseVersionName);
                break;
            case EXTERNAL:
            case CONSUMEDBY_EXTERNAL:
                basePath = apiVersion.getBasePathXmas();
                break;
            default:
                log.warn("[SyncApiDefinitionValidatorImpl] -> [apiBasePath]: there´s no option for {}", implementedAs);
        }

        return basePath;
    }

    private void validateServedApi(RVApiDTO servedApi, NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                   List<ValidationErrorDto> errorList, String releaseName, List<RVApiDTO> servedApiList)
    {

        // FIXME: posibles problemas de concurrencia.
        if (servedApiList.contains(servedApi))
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_IMPLEMENTED_TWICE,
                    servedApi.getApiName() + API_IMPLEMENTED_TWICE_MSG);
        }

        List<ReleaseVersionService> releaseVersionServiceList = this.releaseVersionServiceRepository.findAllDeployedImplementations(servedApi.getId());

        // FIXME: actualmente con este código, es imposible aumentar la major version de un servicio, que esté sirviendo un api, sin aumentar la major version del api. fuerte acoplamiento con el api
        // Checking if the api is served in 2 different locations at the same time, what makes routing impossible
        releaseVersionServiceList = releaseVersionServiceList.stream().filter(oldService ->
                        oldService.getVersionSubsystem().getReleaseVersion().getRelease().getName().equals(releaseName)
                                && !oldService.getVersion().split("\\.")[0].equals(newReleaseVersionServiceDto.getVersion().split("\\.")[0]))
                .collect(Collectors.toList());

        if (!releaseVersionServiceList.isEmpty())
        {
            HashSet<String> releaseVersionServiceSet = new HashSet<>();
            releaseVersionServiceList.forEach(rvs -> releaseVersionServiceSet.add(this.generateServiceName(rvs.getGroupId(), rvs.getArtifactId(),
                    releaseName, rvs.getVersion())));
            for (String serviceName : releaseVersionServiceSet)
            {
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_SERVED_IN_DIFFERENT_LOCATIONS,
                        servedApi.getApiName() + ":" + servedApi.getVersion() + " is already being served with service name: "
                                + serviceName + " and you are trying to serve it with service name: " +
                                this.generateServiceName(newReleaseVersionServiceDto.getGroupId(),
                                        newReleaseVersionServiceDto.getArtifactId(), releaseName,
                                        newReleaseVersionServiceDto.getVersion()) + ". " + API_SERVED_IN_DIFFERENT_LOCATIONS_MSG);
            }
        }

        servedApiList.add(servedApi);
    }

    private void addApiEntry(NewReleaseVersionServiceDto newReleaseVersionServiceDto, RVApiDTO apiDto,
                             String implementedAs)
    {

        if (implementedAs.equalsIgnoreCase(ImplementedAs.SERVED.toString()))
        {
            List<RVApiDTO> implementedApiList = new ArrayList<>();
            if (newReleaseVersionServiceDto.getApisServed() != null)
            {
                implementedApiList = new ArrayList<>(Arrays.asList(newReleaseVersionServiceDto.getApisServed()));
            }
            implementedApiList.add(apiDto);
            newReleaseVersionServiceDto.setApisServed(implementedApiList.toArray(new RVApiDTO[0]));
        }
        if (implementedAs.equalsIgnoreCase(ImplementedAs.CONSUMED.toString()) || implementedAs.equalsIgnoreCase(ImplementedAs.EXTERNAL.toString()))
        {
            List<RVApiDTO> implementedApiList = new ArrayList<>();
            if (newReleaseVersionServiceDto.getApisConsumed() != null)
            {
                implementedApiList = new ArrayList<>(Arrays.asList(newReleaseVersionServiceDto.getApisConsumed()));
            }
            implementedApiList.add(apiDto);
            newReleaseVersionServiceDto.setApisConsumed(implementedApiList.toArray(new RVApiDTO[0]));
        }
    }

    private void addConsumedEntry(RVApiDTO apiDto, RVApiConsumedBy apiConsumedBy)
    {

        List<RVApiConsumedBy> consumedList = new ArrayList<>();
        if (apiDto.getConsumedApis() != null)
        {
            consumedList = new ArrayList<>(Arrays.asList(apiDto.getConsumedApis()));
        }
        consumedList.add(apiConsumedBy);
        apiDto.setConsumedApis(consumedList.toArray(new RVApiConsumedBy[0]));
    }

    /**
     * Get Api list
     *
     * @param newReleaseVersionServiceDto dto
     * @param errorList                   error list
     * @param swaggerProject              swagger project
     * @return the Nova Api instance. Could be null if there is some error with the NovaApi
     */
    private SyncApiVersion getApiVersion(NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                         List<ValidationErrorDto> errorList, Swagger swaggerProject, String implementedAs)
    {
        SyncApiVersion apiVersion = null;

        if (swaggerProject.getInfo() == null || Strings.isNullOrEmpty(swaggerProject.getInfo().getTitle()) || Strings.isNullOrEmpty(swaggerProject.getInfo().getVersion()))
        {
            log.warn("[SyncApiDefinitionValidatorImpl] -> [getSyncApiVersion]: the swagger of release version service: [{}] does not have or info section, title or version TAGs. Review the Swagger: [{}]",
                    newReleaseVersionServiceDto.getFinalName(), swaggerProject.getBasePath());
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_TAG_NOT_FOUND,
                    swaggerProject.getInfo().getTitle() + API_TAG_NOT_FOUND_MSG);
        }
        else
        {
            LinkedHashMap<String, Object> customProperties = (LinkedHashMap<String, Object>) swaggerProject.getVendorExtensions().get("x-generator-properties");
            String uuaa = "";

            if (customProperties.get("business-unit") != null)
            {
                uuaa = customProperties.get("business-unit").toString().toUpperCase();
            }

            if (implementedAs.equals(ImplementedAs.SERVED.name()))
            {
                apiVersion = this.getApiVersionServed(newReleaseVersionServiceDto, errorList, swaggerProject, uuaa);
            }
            else if (implementedAs.equals(ImplementedAs.CONSUMED.name()) || implementedAs.equals(ImplementedAs.CONSUMEDBY.name()))
            {
                apiVersion = this.getApiVersionConsumed(newReleaseVersionServiceDto, errorList, swaggerProject, uuaa);
            }
            else if (implementedAs.equals(ImplementedAs.EXTERNAL.name()) || implementedAs.equals(ImplementedAs.CONSUMEDBY_EXTERNAL.name()))
            {
                apiVersion = this.getApiVersionExternal(newReleaseVersionServiceDto, errorList, swaggerProject, uuaa);
            }
            else
            {
                log.error("[SyncApiDefinitionValidatorImpl] -> [getNovaApi]: the implemented type: [{}] of the swagger: [{}] of release version service: [{}] does not exists into NOVA Data Base.",
                        implementedAs, newReleaseVersionServiceDto.getFinalName(), swaggerProject.getBasePath());
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_IMPLEMENTED_TYPE_NOT_FOUND,
                        swaggerProject.getInfo().getTitle() + API_IMPLEMENTED_TYPE_NOT_FOUND_MSG);
            }
        }

        return apiVersion;
    }

    /**
     * Generates the base path of the swagger for the service
     *
     * @param apiVersion         Nova API
     * @param releaseVersionName releaseVersionName
     * @return Base path of the API for the service
     */
    private String getBasePath(final SyncApiVersion apiVersion, final String releaseVersionName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(apiVersion.getApi().getProduct().getUuaa());
        sb.append("/");
        sb.append(apiVersion.getApi().getName());
        sb.append("/");
        sb.append(apiVersion.getVersion());
        sb.append("/");
        return sb.toString().toLowerCase();
    }

    /**
     * Gets the NOVA Api served from BBDD.
     * Return null instance if the uuaa is not the same or the nova API is not found from NOVA BBDD
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param errorList                   errorList
     * @param swaggerProject              swaggerProject
     * @param uuaa                        uuaa from business unit (nova.yml)
     * @return a NovaApi instance. Could be null.
     */
    private SyncApiVersion getApiVersionServed(NewReleaseVersionServiceDto newReleaseVersionServiceDto, List<ValidationErrorDto> errorList, Swagger swaggerProject, String uuaa)
    {
        SyncApiVersion apiVersion;

        if (newReleaseVersionServiceDto.getUuaa().equalsIgnoreCase(uuaa) && this.productRepository.findOneByUuaaIgnoreCase(uuaa).isPresent())
        {
            // The UUAA is the same from product and swagger yml - business unit
            apiVersion = this.syncApiVersionRepository.findByProductIdAndApiNameAndVersion(this.getProductId(uuaa), swaggerProject.getInfo().getTitle(),
                    swaggerProject.getInfo().getVersion());

            if (apiVersion == null)
            {
                log.warn("[SwaggerValidator] -> [getSyncApiVersionServed]: the NOVA API served not found for release version service: [{}] - uuaa: [{}]. [{}]", newReleaseVersionServiceDto.getFinalName(), uuaa, Constants.MSG_API_NOT_REGISTERED);
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_NOT_REGISTERED,
                        swaggerProject.getInfo().getTitle() + Constants.MSG_CONTENT_VERSION + swaggerProject.getInfo().getVersion() + API_NOT_REGISTERED_MSG);
            }
        }
        else
        {
            // The uuaa from swagger yml - business unit and product is not the same. Must be the same. NovaAPI set as null
            apiVersion = null;
            log.warn("[SwaggerValidator] -> [getSyncApiVersionServed]: the uuaa from swagger - business unit: [{}] and the product: [{}] is not the same for the release version service name: [{}]. NovaAPI set to null",
                    uuaa, newReleaseVersionServiceDto.getUuaa(), newReleaseVersionServiceDto.getFinalName());
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_FROM_ANOTHER_UUAA,
                    swaggerProject.getInfo().getTitle() + API_FROM_ANOTHER_UUAA_MSG);
        }

        return apiVersion;
    }

    /**
     * Gets the NovaApi consumed
     * Return null if the nova API is not found from NOVA BBDD
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param errorList                   errorList
     * @param swaggerProject              swaggerProject
     * @param uuaa                        uuaa
     * @return a NovaApi instance. Could be null.
     */
    private SyncApiVersion getApiVersionConsumed(NewReleaseVersionServiceDto newReleaseVersionServiceDto, List<ValidationErrorDto> errorList, Swagger swaggerProject, String uuaa)
    {
        SyncApiVersion apiVersion = this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(swaggerProject.getInfo().getTitle(), swaggerProject.getInfo().getVersion(), uuaa);

        if (apiVersion == null)
        {
            log.warn("[SwaggerValidator] -> [getSyncApiVersionConsumed]: the NOVA API consumed [{}] with version [{}] not found for release version service: [{}] - uuaa: [{}]", swaggerProject.getInfo().getTitle(), swaggerProject.getInfo().getVersion(), newReleaseVersionServiceDto.getFinalName(), uuaa);
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_NOT_FOUND, API_NOT_FOUND_MSG);
        }

        return apiVersion;
    }

    /**
     * Gets the external ApiVersion
     * Return null if the nova API is not found from NOVA BBDD
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param errorList                   errorList
     * @param swaggerProject              swaggerProject
     * @param uuaa                        uuaa
     * @return a ApiVersion instance. Could be null.
     */
    private SyncApiVersion getApiVersionExternal(NewReleaseVersionServiceDto newReleaseVersionServiceDto, List<ValidationErrorDto> errorList, Swagger swaggerProject, String uuaa)
    {
        SyncApiVersion syncApiVersion = this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndProductId(swaggerProject.getInfo().getTitle(),
                swaggerProject.getInfo().getVersion(), uuaa, this.getProductId(newReleaseVersionServiceDto.getUuaa()));

        if (syncApiVersion == null)
        {
            log.warn("[SwaggerValidator] -> [getSyncApiVersionExternal]: the external NOVA API not found for release version service: [{}] - uuaa: [{}]", newReleaseVersionServiceDto.getFinalName(), uuaa);
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_NOT_FOUND, API_NOT_FOUND_MSG);
        }

        return syncApiVersion;
    }

    private String generateServiceName(String groupId, String artifactId, String release, String version)
    {
        return ServiceNamingUtils.getNovaServiceName(groupId, artifactId, release, version.split("\\.")[0]);
    }

    private void checkApiSupportedVersions(SyncApiVersion apiVersion, NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                           List<ValidationErrorDto> errorList, List<String> backwardCompatibleVersions)
    {
        if (backwardCompatibleVersions.size() > MAXIMUM_API_SUPPORTED_VERSIONS)
        {
            log.warn("[SwaggerValidator] -> [checkApiSupportedVersions]: defined [{}] supported versions for NOVA API when maximum supported" +
                            "versions are [{}] for release version service: [{}] - uuaa: [{}]", backwardCompatibleVersions.size()
                    , MAXIMUM_API_SUPPORTED_VERSIONS, newReleaseVersionServiceDto.getFinalName(), apiVersion.getApi().getUuaa());
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), EXCEED_SUPPORTED_VERSION_API_NUMBER,
                    apiVersion.getApi().getName() + " has " + backwardCompatibleVersions.size() + EXCEED_SUPPORTED_VERSION_API_NUMBER_MSG +
                            MAXIMUM_API_SUPPORTED_VERSIONS);
        }
        for (String supportedVersion : backwardCompatibleVersions)
        {
            if (ComparisonUtils.compareVersion(apiVersion.getVersion(), supportedVersion, apiVersion.getApi().getName(), "API Version", errorList) == 0)
            {
                log.warn("[SwaggerValidator] -> [checkApiSupportedVersions]: the supported version [{}] for NOVA API is greater than" +
                                "implemented api version [{}] for release version service: [{}] - uuaa: [{}]", supportedVersion, apiVersion.getVersion(),
                        newReleaseVersionServiceDto.getFinalName(), apiVersion.getApi().getUuaa());
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), SUPPORTED_VERSION_GREATER_THAN_IMPLEMENTED_VERSION,
                        apiVersion.getApi().getName() + ":" + supportedVersion + " : " + SUPPORTED_VERSION_GREATER_THAN_IMPLEMENTED_VERSION_MSG);
            }
            else
            {
                SyncApiVersion supportedNovaApi = this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndProductId(apiVersion.getApi().getName(),
                        supportedVersion, apiVersion.getApi().getUuaa(), this.getProductId(newReleaseVersionServiceDto.getUuaa()));

                if (supportedNovaApi == null)
                {
                    log.warn("[SwaggerValidator] -> [getNovaApiExternal]: the supported version [{}] NOVA API not found " +
                            "for release version service: [{}] - uuaa: [{}]", supportedVersion, newReleaseVersionServiceDto.getFinalName(), apiVersion.getApi().getUuaa());
                    ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), SUPPORTED_VERSION_API_NOT_FOUND,
                            apiVersion.getApi().getName() + ":" + supportedVersion + " : " + SUPPORTED_VERSION_API_NOT_FOUND_MSG);
                }
            }
        }
    }

    /**
     * Get the ID of a Product given its UUAA.
     *
     * @param uuaa The given UUAA.
     * @return The ID of the Product.
     * @throws NovaException If the Product does not exist.
     */
    private Integer getProductId(String uuaa)
    {
        return this.productRepository.findOneByUuaaIgnoreCase(uuaa).orElseThrow(() -> new NovaException(ReleaseVersionError.getNoSuchProductError(uuaa))).getId();
    }

}

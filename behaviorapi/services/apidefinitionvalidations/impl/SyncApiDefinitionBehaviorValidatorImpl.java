package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVApiDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.interfaces.IApiDefinitionBehaviorValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlApi;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.*;

/**
 * Swagger Validator
 */
@Service
@Slf4j
@AllArgsConstructor
public class SyncApiDefinitionBehaviorValidatorImpl implements IApiDefinitionBehaviorValidator
{

    private final SyncApiVersionRepository syncApiVersionRepository;
    private final IVersioncontrolsystemClient versionControlSystemClient;
    private final ProductRepository productRepository;
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

    @Override
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateApiBehavior(final NovaYml novaYml, final BVServiceInfoDTO bvServiceInfoDTO, final int repoId, final String tag)
    {
        log.info("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateAndAssociateApiBehavior]: Starting validation in Thread: " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + " for repoId:" + repoId +
                " tag:" + tag);

        // Create errorList
        List<ValidationErrorDto> errorList = new ArrayList<>();

        for (NovaYmlApi api : novaYml.getApiConsumed())
        {
            BVApiDTO apiDto = new BVApiDTO();
            this.validateApi(api.getApi(), bvServiceInfoDTO, repoId, tag, ImplementedAs.CONSUMED.name(),
                    errorList, apiDto);
        }
        for (NovaYmlApi api : novaYml.getApiExternal())
        {
            BVApiDTO apiDto = new BVApiDTO();
            this.validateApi(api.getApi(), bvServiceInfoDTO, repoId, tag, ImplementedAs.EXTERNAL.name(),
                    errorList, apiDto);
        }

        return errorList;
    }

    ////////////////////////////////////// PRIVATE METHODS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Validate Api declared in the service
     *
     * @param swaggerPath      Swagger path
     * @param bvServiceInfoDTO Behavior service DTO
     * @param repoId           Repository id
     * @param tag              Repository tag
     * @param implementedAs    Api implementation type
     * @param errorList        Error list
     * @param apiDto           BVApiDTO
     */
    private void validateApi(String swaggerPath, BVServiceInfoDTO bvServiceInfoDTO, int repoId,
                             String tag, String implementedAs, List<ValidationErrorDto> errorList, BVApiDTO apiDto)
    {

        // Get the swagger file from VCS
        String filePath = bvServiceInfoDTO.getFolder() + "/" + swaggerPath;
        Swagger swaggerProject;
        try
        {
            swaggerProject = new SwaggerParser().parse(new String(this.versionControlSystemClient.getSwaggerFromProject(filePath, repoId, tag), StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            log.debug("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateApi]: Error reading swagger", e);
            swaggerProject = null;
        }

        if (swaggerProject == null)
        {
            log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateApi]: Swagger is null");
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_PATH_NOT_FOUND,
                    "For tag " + tag + API_PATH_NOT_FOUND_MSG);
        }
        else
        {
            SyncApiVersion apiVersion = this.getApiVersion(bvServiceInfoDTO, errorList, swaggerProject, implementedAs);
            if (apiVersion == null)
            {
                log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateApi]: the NOVA API section found problems for service name: [{}] - swagger project base path: [{}] - implemented type: [{}]. Cannot create Behavior version.",
                        bvServiceInfoDTO.getServiceName(), swaggerPath, implementedAs);
            }
            else
            {
                String basePathSwagger = this.validateStoredSwagger(bvServiceInfoDTO, errorList, swaggerProject, apiVersion, implementedAs);
                String basePathApi = apiVersion.getBasePathXmas();

                if (Strings.isNullOrEmpty(basePathSwagger))
                {
                    log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateApi]: the swagger base path for version service name: [{}] - implemented type: [{}] found some errors. Cannot create behavior version.",
                            bvServiceInfoDTO.getServiceName(), implementedAs);
                }
                else
                {
                    boolean hasPolicies = this.checkApiPoliciesTask(apiVersion, errorList, bvServiceInfoDTO.getServiceName());
                    this.buildBVApiDto(bvServiceInfoDTO, apiVersion, implementedAs, basePathSwagger, basePathApi, apiDto, hasPolicies);
                }
            }
        }
    }

    /**
     * Check the status of the API policies
     *
     * @param apiVersion  Api version
     * @param errorList   Error list
     * @param serviceName Service name
     * @return Policy check result
     */
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

    /**
     * Build BVApiDTO from SyncApi
     *
     * @param bvServiceInfoDTO Service dto
     * @param apiVersion       Api version
     * @param implementedAs    Api implementation type
     * @param basePathSwagger  Swagger path
     * @param basePathApi      Api path
     * @param apiDto           Api dto
     * @param hasPolicies      Api policies
     */
    private void buildBVApiDto(BVServiceInfoDTO bvServiceInfoDTO, SyncApiVersion apiVersion,
                               String implementedAs, String basePathSwagger,
                               String basePathApi, BVApiDTO apiDto, boolean hasPolicies)
    {
        SyncApi api = apiVersion.getApi();
        switch (ImplementedAs.valueOf(implementedAs))
        {
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
                this.addApiEntry(bvServiceInfoDTO, apiDto);
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
                this.addApiEntry(bvServiceInfoDTO, apiDto);
                break;
            default:
                log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [buildBVApiDto]: invalid implementation type");
                break;
        }
    }

    /**
     * Validates the swagger and the base path stored
     *
     * @param bvServiceInfoDTO dto
     * @param errorList        error list
     * @param swaggerProject   swagger project
     * @param apiVersion       api list
     * @return a validated base path
     */
    private String validateStoredSwagger(BVServiceInfoDTO bvServiceInfoDTO,
                                         List<ValidationErrorDto> errorList, Swagger swaggerProject,
                                         SyncApiVersion apiVersion, String implementedAs)
    {
        // Compare Swagger: database Swagger does not have the BasePath, it must not be compared
        Swagger swaggerStored = this.swaggerConverter.parseSwaggerFromString(apiVersion.getDefinitionFile().getContents());
        swaggerStored.setBasePath(swaggerProject.getBasePath());

        String basePath = this.validateStoredBasePath(swaggerProject.getBasePath(), implementedAs, swaggerStored);
        if (Arrays.equals(this.swaggerConverter.swaggerYamlToByteArray(swaggerStored), this.swaggerConverter.swaggerYamlToByteArray(swaggerProject)))
        {
            log.debug("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateStoredSwagger]: the swagger stored: [{}] is the same from swagger project: [{}]. Service final name: [{}] - NovaApi name: [{}]",
                    swaggerStored, swaggerProject, bvServiceInfoDTO.getFinalName(), apiVersion.getApi().getName());
        }
        else
        {
            log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [validateStoredSwagger]: the swagger stored: [{}] is NOT the same from swagger project: [{}]. Service final name: [{}] - NovaApi name: [{}]",
                    swaggerStored, swaggerProject, bvServiceInfoDTO.getFinalName(), apiVersion.getApi().getName());
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_MODIFIED, swaggerStored.getInfo().getTitle() + API_MODIFIED_MSG);
            basePath = null;
        }
        return basePath;
    }
    
    private String validateStoredBasePath(String basePath, String implementedAs,
                                          Swagger swaggerStored)
    {
        if (implementedAs.equalsIgnoreCase(ImplementedAs.CONSUMED.name()) || implementedAs.equalsIgnoreCase(ImplementedAs.EXTERNAL.name()))
        {
            return basePath;
        }
        else
        {
            return swaggerStored.getBasePath();
        }
    }

    /**
     * Add the consumed APIs to the service dto
     *
     * @param bvServiceInfoDTO Behavior service
     * @param apiDto           Api dto
     */
    private void addApiEntry(BVServiceInfoDTO bvServiceInfoDTO, BVApiDTO apiDto)
    {

        List<BVApiDTO> implementedApiList = new ArrayList<>();
        if (bvServiceInfoDTO.getApisConsumed() != null)
        {
            implementedApiList = new ArrayList<>(Arrays.asList(bvServiceInfoDTO.getApisConsumed()));
        }
        implementedApiList.add(apiDto);
        bvServiceInfoDTO.setApisConsumed(implementedApiList.toArray(new BVApiDTO[0]));

    }

    /**
     * Get Api list
     *
     * @param bvServiceInfoDTO dto
     * @param errorList        error list
     * @param swaggerProject   swagger project
     * @return the Nova Api instance. Could be null if there is some error with the NovaApi
     */
    private SyncApiVersion getApiVersion(BVServiceInfoDTO bvServiceInfoDTO,
                                         List<ValidationErrorDto> errorList, Swagger swaggerProject, String implementedAs)
    {
        SyncApiVersion apiVersion = null;

        if (swaggerProject.getInfo() == null || Strings.isNullOrEmpty(swaggerProject.getInfo().getTitle()) || Strings.isNullOrEmpty(swaggerProject.getInfo().getVersion()))
        {
            log.warn("[SyncApiDefinitionBehaviorValidatorImpl] -> [getSyncApiVersion]: the swagger of release version service: [{}] does not have or info section, title or version TAGs. Review the Swagger: [{}]",
                    bvServiceInfoDTO.getFinalName(), swaggerProject.getBasePath());
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_TAG_NOT_FOUND,
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

            if (implementedAs.equals(ImplementedAs.CONSUMED.name()))
            {
                apiVersion = this.getApiVersionConsumed(bvServiceInfoDTO, errorList, swaggerProject, uuaa);
            }
            else if (implementedAs.equals(ImplementedAs.EXTERNAL.name()))
            {
                apiVersion = this.getApiVersionExternal(bvServiceInfoDTO, errorList, swaggerProject, uuaa);
            }
            else
            {
                log.error("[SyncApiDefinitionBehaviorValidatorImpl] -> [getNovaApi]: the implemented type: [{}] of the swagger: [{}] of release version service: [{}] does not exists into NOVA Data Base.",
                        implementedAs, bvServiceInfoDTO.getFinalName(), swaggerProject.getBasePath());
                ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_IMPLEMENTED_TYPE_NOT_FOUND,
                        swaggerProject.getInfo().getTitle() + API_IMPLEMENTED_TYPE_NOT_FOUND_MSG);
            }
        }

        return apiVersion;
    }

    /**
     * Gets the NovaApi consumed
     * Return null if the nova API is not found from NOVA BBDD
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     * @param errorList        errorList
     * @param swaggerProject   swaggerProject
     * @param uuaa             uuaa
     * @return a NovaApi instance. Could be null.
     */
    private SyncApiVersion getApiVersionConsumed(BVServiceInfoDTO bvServiceInfoDTO, List<ValidationErrorDto> errorList, Swagger swaggerProject, String uuaa)
    {
        SyncApiVersion apiVersion = this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(swaggerProject.getInfo().getTitle(), swaggerProject.getInfo().getVersion(), uuaa);

        if (apiVersion == null)
        {
            log.warn("[SwaggerValidator] -> [getSyncApiVersionConsumed]: the NOVA API consumed [{}] with version [{}] " +
                            "not found for release version service: [{}] - uuaa: [{}]", swaggerProject.getInfo().getTitle(),
                    swaggerProject.getInfo().getVersion(), bvServiceInfoDTO.getFinalName(), uuaa);
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_NOT_FOUND, API_NOT_FOUND_MSG);
        }

        return apiVersion;
    }

    /**
     * Gets the external ApiVersion
     * Return null if the nova API is not found from NOVA BBDD
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     * @param errorList        errorList
     * @param swaggerProject   swaggerProject
     * @param uuaa             uuaa
     * @return a ApiVersion instance. Could be null.
     */
    private SyncApiVersion getApiVersionExternal(BVServiceInfoDTO bvServiceInfoDTO, List<ValidationErrorDto> errorList, Swagger swaggerProject, String uuaa)
    {
        SyncApiVersion syncApiVersion = this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndProductId(swaggerProject.getInfo().getTitle(),
                swaggerProject.getInfo().getVersion(), uuaa, this.getProductId(bvServiceInfoDTO.getUuaa()));

        if (syncApiVersion == null)
        {
            log.warn("[SwaggerValidator] -> [getSyncApiVersionExternal]: the external NOVA API not found for behavior version service: [{}] - uuaa: [{}]", bvServiceInfoDTO.getFinalName(), uuaa);
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), API_NOT_FOUND, API_NOT_FOUND_MSG);
        }

        return syncApiVersion;
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

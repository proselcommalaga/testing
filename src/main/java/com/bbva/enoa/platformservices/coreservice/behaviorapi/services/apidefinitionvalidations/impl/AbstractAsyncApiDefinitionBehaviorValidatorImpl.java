package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVApiDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_PATH_NOT_FOUND;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_PATH_NOT_FOUND_ASYNC_MSG;

/**
 * Async Validator this class have the common functionalities for Async Definition Api Validators
 */
@Slf4j
public abstract class AbstractAsyncApiDefinitionBehaviorValidatorImpl
        <A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
{
    /**
     * versioncontrolsystemClient
     */
    private final VersioncontrolsystemClientImpl versionControlSystemClient;

    /**
     * Instantiates a new AsyncApi validator.
     */
    AbstractAsyncApiDefinitionBehaviorValidatorImpl(
            final VersioncontrolsystemClientImpl versionControlSystemClient)
    {
        this.versionControlSystemClient = versionControlSystemClient;
    }

    /**
     * Validate asyncApi
     *
     * @param bvServiceInfoDTO service
     * @param repoId           repository id
     * @param tagName          tagName
     * @param novaYml          Nova.yml with the asyncapi's info
     */
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateAsyncApi(final BVServiceInfoDTO bvServiceInfoDTO,
                                                                 final int repoId, final String tagName, final NovaYml novaYml)
    {
        log.info("[AbstractAsyncApiDefinitionBehaviorValidatorImpl] -> [validateAndAssociateAsyncApi]: Starting validation in Thread: " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName());

        // Create errorList
        List<ValidationErrorDto> errorList = new ArrayList<>();

        //Initialize ApisConsumed arrays in bvServiceInfoDTO if necessary
        initializeNewBehaviorVersionServiceApiArrays(bvServiceInfoDTO);

        // Check APIs
        for (String asyncApiPath : this.getAsyncAPIDefinitionList(novaYml))
        {
            // Retrieve api from VCS
            byte[] rawAsyncAPIfromVCS = this.getRawAsyncAPIfromVCS(bvServiceInfoDTO.getFolder(),
                    asyncApiPath, repoId, tagName);

            // Get ApiVersion from database with MD5 Hash
            AV apiVersion = this.apiVersionByMd5Hash(rawAsyncAPIfromVCS);

            // check if the database contains the HashMd5 of the file
            errorList.addAll(this.validateApi(bvServiceInfoDTO, apiVersion, asyncApiPath, tagName));

            //Build RV Apis
            this.buildBVApiDto(bvServiceInfoDTO, apiVersion);
        }

        // Validation for the whole set of apis
        errorList.addAll(this.validateWholeApiSet(bvServiceInfoDTO));

        return errorList;
    }

    /**
     * Abstract method to get the ApiVersion from database by its Md5Hash
     *
     * @param rawAsyncAPIfromVCS raw file
     * @return the ApiVersion stored in database
     */
    abstract AV apiVersionByMd5Hash(final byte[] rawAsyncAPIfromVCS);

    /**
     * Build specific info for each asyncapi implementation
     *
     * @param bvApiDTO   the bvApiDTO to build
     * @param apiVersion the apiVersion to get the information
     * @return the rvApiDTO with specific information
     */
    abstract BVApiDTO buildRVApiDtoSpecificInformation(final BVApiDTO bvApiDTO, final AV apiVersion);

    /**
     * There are some validations that are done over the whole set of apis
     * This functionality have to be delegated to proper implementation.
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     * @return errorList where errors are accumulated
     */
    abstract List<ValidationErrorDto> validateWholeApiSet(
            final BVServiceInfoDTO bvServiceInfoDTO);

    /**
     * Retrieves the asyncapi definitions specific for each implementation (backToBack or BacktoFront)
     *
     * @param novaYml novaYml that contains all the asyncapi definitions
     * @return the specific async api definitions
     */
    abstract Set<String> getAsyncAPIDefinitionList(final NovaYml novaYml);

    /**
     * Check if the apiVersion is implemented as a Served by the releaseVersionService
     *
     * @param novaYml    novaYml that contains Information about releaseVersionService
     * @param apiVersion the apiVersion to be checked
     * @return true if is implemented as Served
     */
    abstract boolean isImplementedAsServed(final NovaYml novaYml, final AV apiVersion);

    /**
     * Do all validations for a specific Implementation (backToback or BackToFront)
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO that contains info about the bvs
     * @param apiVersion       the api Version to be validate
     * @param asyncApiPath     the async api path
     * @param tagName          the tag name
     * @return validation error list
     */
    abstract List<ValidationErrorDto> specificValidationsApi(final BVServiceInfoDTO bvServiceInfoDTO,
                                                             final AV apiVersion, final String asyncApiPath, final String tagName);

    /**
     * Initialize the ApiConsumed arrays if necessary
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     */
    private void initializeNewBehaviorVersionServiceApiArrays(final BVServiceInfoDTO bvServiceInfoDTO)
    {
        if (bvServiceInfoDTO.getApisConsumed() == null)
        {
            bvServiceInfoDTO.setApisConsumed(new BVApiDTO[0]);
        }
    }

    /**
     * Common and specific validations for apiVersion
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     * @param apiVersion       apiVersion
     * @param asyncApiPath     asyncApiPath
     * @param tagName          tagName
     * @return validation error list
     */
    private List<ValidationErrorDto> validateApi(final BVServiceInfoDTO bvServiceInfoDTO,
                                                 final AV apiVersion, final String asyncApiPath, final String tagName)
    {
        // Common validations
        List<ValidationErrorDto> errorList = new ArrayList<>();
        if (Objects.isNull(apiVersion))
        {
            log.warn("[AbstractAsyncApiDefinitionBehaviorValidatorImpl] -> [validateApi]: the asyncApi definition saved into the git repository does not match in database." +
                    " Path:" + asyncApiPath + " and tagName:" + tagName);

            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(),
                    API_PATH_NOT_FOUND, API_PATH_NOT_FOUND_ASYNC_MSG + asyncApiPath);
        }

        // Specific validation for backtoback or backtofront
        errorList.addAll(this.specificValidationsApi(bvServiceInfoDTO, apiVersion, asyncApiPath, tagName));

        return errorList;
    }

    /**
     * Build the BVApiDTO with common and specific (backToback or backtoFront) information
     *
     * @param bvServiceInfoDTO bvServiceInfoDTO
     * @param apiVersion       apiVersion
     */
    private void buildBVApiDto(final BVServiceInfoDTO bvServiceInfoDTO,
                               final AV apiVersion)
    {
        BVApiDTO bvApiDTO = new BVApiDTO();
        if (apiVersion != null && apiVersion.getApi() != null && apiVersion.getApi().getProduct() != null)
        {
            // Build common dto info
            A api = apiVersion.getApi();
            bvApiDTO.setUuaa(api.getProduct().getUuaa());
            bvApiDTO.setProduct(api.getProduct().getName());
            bvApiDTO.setProductId(api.getProduct().getId());
            bvApiDTO.setId(apiVersion.getId());
            bvApiDTO.setApiName(api.getName());
            bvApiDTO.setVersion(apiVersion.getVersion());
            bvApiDTO.setDescription(apiVersion.getDescription());
            bvApiDTO.setModality(api.getApiModality().getModality());
            bvApiDTO.setExternal(false);

            // Build specific info for each asyncApi implementation
            bvApiDTO = buildRVApiDtoSpecificInformation(bvApiDTO, apiVersion);

            // Associate bvApiDTO to newBvsDTO
            this.addRVApiDtoToNewReleaseVersionServiceDTO(bvServiceInfoDTO, bvApiDTO);

        }
        else
        {
            log.warn("[AbstractAsyncApiDefinitionBehaviorValidatorImpl] -> [buildBVApiDto]: Skipping buildBVApiDto because ApiVersion, Api or Api.product are null");
        }
    }

    /**
     * Get an asyncAPI file from VCS and map it to a NovaAsyncAPI
     *
     * @param folder         folder
     * @param definitionPath local path in the service where the api is storedstored
     * @param repoId         repo Id
     * @param tagName        tag name
     * @return a well-formed NovaAsyncAPI
     */
    private byte[] getRawAsyncAPIfromVCS(final String folder, final String definitionPath,
                                         final int repoId, final String tagName)
    {
        String filePath = folder + "/" + definitionPath;
        return versionControlSystemClient.getFileFromProject(filePath, repoId, tagName);
    }

    /**
     * Associate the BVApiDTO to the bvServiceInfoDTO as api consumed or api served.
     *
     * @param bvServiceInfoDTO newReleaseVersionServiceDto
     * @param bvApiDto         bvApiDto
     */
    private void addRVApiDtoToNewReleaseVersionServiceDTO(final BVServiceInfoDTO bvServiceInfoDTO, final BVApiDTO bvApiDto)
    {
        bvServiceInfoDTO.setApisConsumed(
                Stream.concat(Arrays.stream(bvServiceInfoDTO.getApisConsumed()), Stream.of(bvApiDto))
                        .toArray(BVApiDTO[]::new));
    }
}

package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_PATH_NOT_FOUND;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_PATH_NOT_FOUND_ASYNC_MSG;

/**
 * Async Validator this class have the common functionalities for Async Definition Api Validators
 */
@Slf4j
public abstract class AbstractAsyncApiDefinitionValidatorImpl
        <A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
{
    /**
     * versioncontrolsystemClient
     */
    private final VersioncontrolsystemClientImpl versioncontrolsystemClient;

    /**
     * Instantiates a new Asyncapi validator.
     */
    AbstractAsyncApiDefinitionValidatorImpl(
            final VersioncontrolsystemClientImpl versioncontrolsystemClient)
    {
        this.versioncontrolsystemClient = versioncontrolsystemClient;
    }

    /**
     * Validate asyncapi
     *
     * @param newReleaseVersionServiceDto service
     * @param repoId                      repository id
     * @param tagName                     tagName
     * @param novaYml                     Nova.yml with the asyncapi's info
     */
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateAsyncApi(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                                                 final int repoId, final String tagName, final NovaYml novaYml)
    {
        log.info("[AbstractAsyncapiApiDefinitionValidatorImpl] -> [validateAndAssociateAsyncApi]: Starting validation in Thread: " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName());

        // Create errorList
        List<ValidationErrorDto> errorList = new ArrayList<>();

        //Initialize ApisServed and ApisConsumed arrays in newReleaseVersionServiceDto if necessary
        initializenewReleaseVersionServiceApiArrays(newReleaseVersionServiceDto);

        // Check APIs
        for (String asyncApiPath : this.getAsyncAPIDefinitionList(novaYml))
        {
            // Retrieve api from VCS
            byte[] rawAsyncAPIfromVCS = this.getRawAsyncAPIfromVCS(newReleaseVersionServiceDto.getFolder(),
                    asyncApiPath, repoId, tagName);

            // Get ApiVersion from database with MD5 Hash
            AV apiVersion = this.apiVersionByMd5Hash(rawAsyncAPIfromVCS);

            // check if the database contains the HashMd5 of the file
            errorList.addAll(this.validateApi(newReleaseVersionServiceDto, apiVersion, asyncApiPath, tagName));

            //Build RV Apis
            this.buildRVApiDto(newReleaseVersionServiceDto, apiVersion, this.isImplementedAsServed(novaYml, apiVersion));
        }

        // Validation for the whole set of apis
        errorList.addAll(this.validateWholeApiSet(newReleaseVersionServiceDto));

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
     * @param rvApiDTO   the rvApiDTO to build
     * @param apiVersion the apiVersion to get the information
     * @return the rvApiDTO with specific information
     */
    abstract RVApiDTO buildRVApiDtoSpecificInformation(final RVApiDTO rvApiDTO, final AV apiVersion);

    /**
     * There are some validations that are done over the whole set of apis
     * This functionality have to be delegated to proper implementation.
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @return errorList where errors are accumulated
     */
    abstract List<ValidationErrorDto> validateWholeApiSet(
            final NewReleaseVersionServiceDto newReleaseVersionServiceDto);

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
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto that contains info about the rvs
     * @param apiVersion                  the api Version to be validate
     * @param asyncApiPath                the async api path
     * @param tagName                     the tag name
     * @return validation error list
     */
    abstract List<ValidationErrorDto> specificValidationsApi(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                                             final AV apiVersion, final String asyncApiPath, final String tagName);

    /**
     * Initialize the ApiServed and ApiConsumed arrays if necessary
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     */
    private void initializenewReleaseVersionServiceApiArrays(final NewReleaseVersionServiceDto newReleaseVersionServiceDto)
    {
        if (newReleaseVersionServiceDto.getApisServed() == null)
        {
            newReleaseVersionServiceDto.setApisServed(new RVApiDTO[0]);
        }
        if (newReleaseVersionServiceDto.getApisConsumed() == null)
        {
            newReleaseVersionServiceDto.setApisConsumed(new RVApiDTO[0]);
        }
    }

    /**
     * Common and specific validations for apiVersion
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param apiVersion                  apiVersion
     * @param asyncApiPath                asyncApiPath
     * @param tagName                     tagName
     * @return validation error list
     */
    private List<ValidationErrorDto> validateApi(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                                 final AV apiVersion, final String asyncApiPath, final String tagName)
    {
        // Common validations
        List<ValidationErrorDto> errorList = new ArrayList<>();
        if (Objects.isNull(apiVersion))
        {
            log.warn("[AbstractAsyncapiApiDefinitionValidatorImpl] -> [validateApi]: the asyncApi definition saved into the git repository does not match in database." +
                    " Path:" + asyncApiPath + " and tagName:" + tagName);

            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(),
                    API_PATH_NOT_FOUND, API_PATH_NOT_FOUND_ASYNC_MSG + asyncApiPath);
        }

        // Specific validation for backtoback or backtofront
        errorList.addAll(this.specificValidationsApi(newReleaseVersionServiceDto, apiVersion, asyncApiPath, tagName));

        return errorList;
    }

    /**
     * Build the RvApiDto with common and specific (backToback or backtoFront) information
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param apiVersion                  apiVersion
     * @param isImplementedAsServed       isImplementedAsServed
     */
    private void buildRVApiDto(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                               final AV apiVersion, final boolean isImplementedAsServed)
    {
        RVApiDTO rvApiDTO = new RVApiDTO();
        if (apiVersion != null && apiVersion.getApi() != null && apiVersion.getApi().getProduct() != null)
        {
            // Build common dto info
            A api = apiVersion.getApi();
            rvApiDTO.setUuaa(api.getProduct().getUuaa());
            rvApiDTO.setProduct(api.getProduct().getName());
            rvApiDTO.setProductId(api.getProduct().getId());
            rvApiDTO.setId(apiVersion.getId());
            rvApiDTO.setApiName(api.getName());
            rvApiDTO.setVersion(apiVersion.getVersion());
            rvApiDTO.setDescription(apiVersion.getDescription());
            rvApiDTO.setModality(api.getApiModality().getModality());
            rvApiDTO.setExternal(false);

            // Build specific info for each asyncapi implementation
            rvApiDTO = buildRVApiDtoSpecificInformation(rvApiDTO, apiVersion);

            // Associate rvapiDto to newRvsDTO
            this.addRVApiDtoToNewReleaseVersionServiceDTO(newReleaseVersionServiceDto, rvApiDTO, isImplementedAsServed);

        }
        else
        {
            log.warn("[AbstractAsyncapiApiDefinitionValidatorImpl] -> [buildRVApiDto]: Skipping buildRVApiDTO because ApiVersion, Api or Api.product are null");
        }
    }

    /**
     * Get an asyncAPI file from VCS and map it to a NovaAsyncAPI
     *
     * @param folder         folder
     * @param definitionPath local path in the service where the api is storedstored
     * @param repoId         repo Id
     * @param tagName        tag name
     * @return a well formed NovaAsyncAPI
     */
    private byte[] getRawAsyncAPIfromVCS(final String folder, final String definitionPath,
                                         final int repoId, final String tagName)
    {
        String filePath = folder + "/" + definitionPath;
        return versioncontrolsystemClient.getFileFromProject(filePath, repoId, tagName);
    }

    /**
     * Associate the RVApiDTO to the newReleaseVErsionServiceDTO as api consumed or api served.
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param rvApiDto                    rvApiDto
     * @param isImplementedAsServed       isImplementedAsServed
     */
    private void addRVApiDtoToNewReleaseVersionServiceDTO(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                                          final RVApiDTO rvApiDto,
                                                          boolean isImplementedAsServed)
    {
        if (isImplementedAsServed)
        {
            newReleaseVersionServiceDto.setApisServed(
                Stream.concat( Arrays.stream(newReleaseVersionServiceDto.getApisServed()), Stream.of(rvApiDto))
                    .toArray(RVApiDTO[]::new));
        }
        else
        {
            newReleaseVersionServiceDto.setApisConsumed(
                    Stream.concat( Arrays.stream(newReleaseVersionServiceDto.getApisConsumed()), Stream.of(rvApiDto))
                            .toArray(RVApiDTO[]::new));
        }
    }
}

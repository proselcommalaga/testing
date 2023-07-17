package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.AsyncBackToBackChannelType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations.AsyncBackToBackApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * BackToBack validator
 */
@Slf4j
@Service
public class AsyncBackToBackApiDefinitionValidatorImpl
        extends AbstractAsyncApiDefinitionValidatorImpl<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
        implements IApiDefinitionValidator
{

    /**
     * It is a real validator
     */
    private static final boolean IS_VALIDATOR = true;

    private final AsyncBackToBackApiVersionRepository asyncBackToBackApiVersionRepository;
    private final Utils utils;

    /**
     * Instantiates a new Asyncapi validator.
     *  @param versioncontrolsystemClient          the versioncontrolsystem client
     * @param asyncBackToBackApiVersionRepository the async api
     * @param utils
     */
    @Autowired
    public AsyncBackToBackApiDefinitionValidatorImpl(
            final VersioncontrolsystemClientImpl versioncontrolsystemClient,
            AsyncBackToBackApiVersionRepository asyncBackToBackApiVersionRepository, final Utils utils)
    {
        super(versioncontrolsystemClient);
        this.asyncBackToBackApiVersionRepository = asyncBackToBackApiVersionRepository;
        this.utils = utils;
    }

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
     * Validate asyncapi
     *
     * @param novaYml                     Nova.yml with the asyncapi's info
     * @param newReleaseVersionServiceDto service
     * @param repoId                      repository id
     * @param tag                         tag
     * @param releaseName                 Release Version Name
     */
    @Override
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateApi(final NovaYml novaYml,
                                                            final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                                            final int repoId, final String tag, final String releaseName)
    {
        log.info("[AsyncBackToBackApiDefinitionValidatorImpl] -> [validateAndAssociateAsyncApi]: Starting validation in Thread: " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + " for repoId:" + repoId +
                " tag:" +tag+" releaseName:"+releaseName);

        return super.validateAndAssociateAsyncApi(newReleaseVersionServiceDto,
                repoId, tag, novaYml);
        // validación especifica
    }

    /**
     * Abstract method to check if the database contains the HashMd5 of the file
     *
     * @param rawAsyncAPIfromVCS raw file
     * @return true if the HashMd5 exists
     */
    @Override
    AsyncBackToBackApiVersion apiVersionByMd5Hash(final byte[] rawAsyncAPIfromVCS)
    {
        return asyncBackToBackApiVersionRepository.
                findByDefinitionFileHash(this.utils.calculeMd5Hash(rawAsyncAPIfromVCS));
    }

    @Override
    RVApiDTO buildRVApiDtoSpecificInformation(final RVApiDTO apiDto, final AsyncBackToBackApiVersion apiVersion)
    {
        //Policies does not exist in BackToBack. So, we set true to do not block the api
        apiDto.setPoliciesConfigured(true);
        return apiDto;
    }

    @Override
    List<ValidationErrorDto> validateWholeApiSet(
            final NewReleaseVersionServiceDto newReleaseVersionServiceDto)
    {
        return Collections.emptyList();
    }

    @Override
    Set<String> getAsyncAPIDefinitionList(final NovaYml novaYml)
    {
        Set<String> asyncApiSet = new HashSet<>();
        if (novaYml.getAsyncapisBackToBack() != null )
        {
            asyncApiSet = novaYml.getAsyncapisBackToBack().getAsyncApis();
        }
        return asyncApiSet;
    }

    @Override
    boolean isImplementedAsServed(final NovaYml novaYml, final AsyncBackToBackApiVersion apiVersion)
    {
        // If channel is publish, we assume that is served
        return Objects.nonNull(apiVersion) && Objects.nonNull(apiVersion.getAsyncBackToBackApiChannel()) &&
                AsyncBackToBackChannelType.PUBLISH.equals(apiVersion.getAsyncBackToBackApiChannel().getChannelType());
    }

    @Override
    List<ValidationErrorDto> specificValidationsApi(final NewReleaseVersionServiceDto newReleaseVersionServiceDto, final AsyncBackToBackApiVersion apiVersion, final String asyncApiPath, final String tagName)
    {
        // No validation to add right now, prepared to add here all the specific validations
        return Collections.emptyList();
    }
}

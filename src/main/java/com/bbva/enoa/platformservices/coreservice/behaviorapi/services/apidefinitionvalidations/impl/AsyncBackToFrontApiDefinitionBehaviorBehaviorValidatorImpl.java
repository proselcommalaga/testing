package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVApiDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.interfaces.IApiDefinitionBehaviorValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations.AsyncBackToFrontApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * BackToFront validator
 */
@Slf4j
@Service
public class AsyncBackToFrontApiDefinitionBehaviorBehaviorValidatorImpl
        extends AbstractAsyncApiDefinitionBehaviorValidatorImpl<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
        implements IApiDefinitionBehaviorValidator
{

    /**
     * It is a real validator
     */
    private static final boolean IS_VALIDATOR = true;

    private final AsyncBackToFrontApiVersionRepository asyncBackToFrontApiVersionRepository;
    private final Utils utils;

    /**
     * Instantiates a new Asyncapi validator.
     *
     * @param versioncontrolsystemClient           the versioncontrolsystem client
     * @param asyncBackToFrontApiVersionRepository the async api
     * @param utils
     */
    @Autowired
    public AsyncBackToFrontApiDefinitionBehaviorBehaviorValidatorImpl(
            final VersioncontrolsystemClientImpl versioncontrolsystemClient,
            final AsyncBackToFrontApiVersionRepository asyncBackToFrontApiVersionRepository,
            final Utils utils)
    {
        super(versioncontrolsystemClient);
        this.asyncBackToFrontApiVersionRepository = asyncBackToFrontApiVersionRepository;
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
     * @param novaYml          Nova.yml with the asyncapi's info
     * @param bvServiceInfoDTO service
     * @param repoId           repository id
     * @param tag              tag
     */
    @Override
    @Transactional
    public List<ValidationErrorDto> validateAndAssociateApiBehavior(final NovaYml novaYml, final BVServiceInfoDTO bvServiceInfoDTO, final int repoId, final String tag)
    {
        // TODO: Pendiente de implementar cuando sea necesario
        log.info("[AsyncBackToFrontApiDefinitionValidatorImpl] -> [validateAndAssociateAsyncApiBehavior]: NOT IMPLEMENTED");

        return new ArrayList<>();

    }

    @Override
    AsyncBackToFrontApiVersion apiVersionByMd5Hash(final byte[] rawAsyncAPIfromVCS)
    {
        return null;
    }

    @Override
    BVApiDTO buildRVApiDtoSpecificInformation(final BVApiDTO bvApiDTO, final AsyncBackToFrontApiVersion apiVersion)
    {
        return null;
    }

    @Override
    List<ValidationErrorDto> validateWholeApiSet(final BVServiceInfoDTO bvServiceInfoDTO)
    {
        return null;
    }

    @Override
    Set<String> getAsyncAPIDefinitionList(final NovaYml novaYml)
    {
        return null;
    }

    @Override
    boolean isImplementedAsServed(final NovaYml novaYml, final AsyncBackToFrontApiVersion apiVersion)
    {
        return false;
    }

    @Override
    List<ValidationErrorDto> specificValidationsApi(final BVServiceInfoDTO bvServiceInfoDTO, final AsyncBackToFrontApiVersion apiVersion, final String asyncApiPath, final String tagName)
    {
        return null;
    }
}

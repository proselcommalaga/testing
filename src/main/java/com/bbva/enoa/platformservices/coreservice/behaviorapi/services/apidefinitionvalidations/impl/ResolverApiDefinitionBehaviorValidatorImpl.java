package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.util.ParallelUtils;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.interfaces.IApiDefinitionBehaviorValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Yml (Swagger | AsyncAPI) Validator
 */
@Service
@Primary
@Slf4j
public class ResolverApiDefinitionBehaviorValidatorImpl implements IApiDefinitionBehaviorValidator
{
    private final Executor asyncExecutor;

    private final List<IApiDefinitionBehaviorValidator> validators;

    @Autowired
    public ResolverApiDefinitionBehaviorValidatorImpl(
            final List<IApiDefinitionBehaviorValidator> validators, @Qualifier("taskExecutor") final Executor asyncExecutor
    )
    {
        this.asyncExecutor = asyncExecutor;
        if (validators != null)
        {
            this.validators = validators.stream().filter(IApiDefinitionBehaviorValidator::isValidator).collect(Collectors.toList());
        }
        else
        {
            this.validators = new ArrayList<>();
        }
    }

    /**
     * It is NOT a real validator. It is the resolver
     */
    private final boolean isValidator = false;

    /**
     * Check if it is the resolver of validators or a real validator
     *
     * @return true if it is a real validator. False if it is the resolver
     */
    @Override
    public boolean isValidator()
    {
        return this.isValidator;
    }

    /**
     * Validate YML
     *
     * @param novaYml          Nova.yml with the api's info
     * @param bvServiceInfoDTO dto
     * @param repoId           repository id
     * @param tag              tag
     */
    @Override
    public List<ValidationErrorDto> validateAndAssociateApiBehavior(NovaYml novaYml, BVServiceInfoDTO bvServiceInfoDTO,
                                                                    int repoId, String tag)
    {

        log.info("[ResolverApiDefinitionBehaviorValidatorImpl] -> [validateApi]: Starting validation in Thread " +
                Thread.currentThread().getId() + "-" + Thread.currentThread().getName());

        List<Supplier<List<ValidationErrorDto>>> validatorSuppliers = validators.stream()
                .map(validator -> (Supplier<List<ValidationErrorDto>>) () -> validator.validateAndAssociateApiBehavior(novaYml, bvServiceInfoDTO, repoId, tag))
                .collect(Collectors.toList());

        List<ValidationErrorDto> errorsList =
                ParallelUtils.executeAsyncFaultTolerant(validatorSuppliers, asyncExecutor).stream().map(asyncResult -> {
                    final List<ValidationErrorDto> result;
                    if (asyncResult.isErroneous())
                    {
                        log.error("Exception executing validator thread: ", asyncResult.getCauseError());
                        throw new NovaException(ReleaseVersionError.getValidationUnexpectedError());
                    }
                    else
                    {
                        result = asyncResult.getValue();
                    }
                    return result;
                }).flatMap(Collection::stream).collect(Collectors.toList());

        log.debug("Validation errors found: [" + errorsList.stream().map(ValidationErrorDto::toString).collect(Collectors.joining(",")) + "]");

        return errorsList;
    }

}

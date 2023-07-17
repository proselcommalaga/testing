package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.util.ParallelUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
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
public class ResolverApiDefinitionValidatorImpl implements IApiDefinitionValidator
{
	private final Executor asyncExecutor;

	private final List<IApiDefinitionValidator> validators;

	@Autowired
	public ResolverApiDefinitionValidatorImpl(
			final List<IApiDefinitionValidator> validators, @Qualifier("taskExecutor") final Executor asyncExecutor
	)
	{
		this.asyncExecutor = asyncExecutor;
		if(validators!=null)
		{
			this.validators = validators.stream().filter(IApiDefinitionValidator::isValidator).collect(Collectors.toList());
		}
		else
		{
			this.validators = new ArrayList<>();
		}
	}

	/** It is NOT a real validator. It is the resolver*/
	private final boolean isValidator = false;

	/**
	 * Check if it is the resolver of validators or a real validator
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
	 * @param novaYml                     Nova.yml with the api's info
	 * @param newReleaseVersionServiceDto dto
	 * @param repoId                      repository id
	 * @param tag                         tag
	 */
	@Override
	public List<ValidationErrorDto> validateAndAssociateApi(NovaYml novaYml, NewReleaseVersionServiceDto newReleaseVersionServiceDto,
			int repoId, String tag, String releaseVersionName)
	{

		log.info("[ResolverApiDefinitionValidatorImpl] -> [validateApi]: Starting validation in Thread "+
				Thread.currentThread().getId() +"-"+Thread.currentThread().getName());

		/*
		The map with the lambda:
		   .map(validator -> (Supplier<List<ValidationErrorDto>>) () -> validator.validateApi(effective final params))
		is equivalent to:
		  .map(validador ->
				new Supplier<List<ValidationErrorDto>>(){
					@Override public List<ValidationErrorDto> get()
					{
						return validador.validateApi(novaYml, newReleaseVersionServiceDto, repoId, tag, releaseVersionName);
					}
				}
		  )
		 */
		List<Supplier<List<ValidationErrorDto>>> validatorSuppliers = validators.stream()
				.map(validator -> (Supplier<List<ValidationErrorDto>>) () -> validator.validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, repoId, tag, releaseVersionName))
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

		log.debug("Validation errors found: [" + errorsList.stream().map(ValidationErrorDto::toString) .collect( Collectors.joining( "," )) + "]");

		return errorsList;
	}

}

package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations.AsyncBackToFrontApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ApiValidationUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_IMPLEMENTED_TWICE;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_IMPLEMENTED_TWICE_MSG;

/**
 * BackToFront validator
 */
@Slf4j
@Service
public class AsyncBackToFrontApiDefinitionValidatorImpl
		extends AbstractAsyncApiDefinitionValidatorImpl<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
		implements IApiDefinitionValidator
{

	/** It is a real validator*/
	private static final boolean IS_VALIDATOR = true;

	private final AsyncBackToFrontApiVersionRepository asyncBackToFrontApiVersionRepository;
	private final Utils utils;

	/**
	 * Instantiates a new Asyncapi validator.
	 * @param versioncontrolsystemClient          the versioncontrolsystem client
	 * @param asyncBackToFrontApiVersionRepository the async api
	 * @param utils
	 */
	@Autowired
	public AsyncBackToFrontApiDefinitionValidatorImpl(
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
	 * @param releaseName          Release Version Name
	 */
	@Override
	@Transactional
	public List<ValidationErrorDto> validateAndAssociateApi(final NovaYml novaYml,
			final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
			final int repoId, final String tag, final String releaseName)
	{
		log.info("[AsyncBackToFrontApiDefinitionValidatorImpl] -> [validateAndAssociateAsyncApi]: Starting validation in Thread: " +
				Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + " for repoId:" + repoId +
				" tag:" +tag+" releaseName:"+releaseName);

		return super.validateAndAssociateAsyncApi(newReleaseVersionServiceDto,
				repoId, tag, novaYml);
	}

	/**
	 * Abstract method to check if the database contains the HashMd5 of the file
	 * @param rawAsyncAPIfromVCS raw file
	 * @return true if the HashMd5 exists
	 */
	@Override
	AsyncBackToFrontApiVersion apiVersionByMd5Hash(final byte[] rawAsyncAPIfromVCS)
	{
		return asyncBackToFrontApiVersionRepository.
				findByDefinitionFileHash(this.utils.calculeMd5Hash(rawAsyncAPIfromVCS));
	}

	@Override
	RVApiDTO buildRVApiDtoSpecificInformation(final RVApiDTO apiDto, final AsyncBackToFrontApiVersion apiVersion)
	{
		//BasePathSwagger, para BackToFront es el base path usado para el
		//meta protocolo (STOMP) para establecer la comunicación HTTP con XMAS.
		//Este parámetro no está definido en el documento de asyncapi, sino que
		//lo establecemos nosotros con una constante.
		apiDto.setBasePathSwagger(apiVersion.getApi().getBasePathSwagger());
		apiDto.setBasePathApi(apiVersion.getBasePathXmas());
		apiDto.setPoliciesConfigured(
				ApiPolicyStatus.arePoliciesSet(apiVersion.getApi().getPolicyStatus()));

		return apiDto;
	}

	@Override
	List<ValidationErrorDto> validateWholeApiSet(
			final NewReleaseVersionServiceDto newReleaseVersionServiceDto)
	{
		List<ValidationErrorDto> errorList = new ArrayList<>();

		//First, check if there are any api implemented twice
		List<RVApiDTO> rvApiDTOList = Arrays.asList(newReleaseVersionServiceDto.getApisServed());
		Set<RVApiDTO> rvApiDTOSet = new HashSet<>(rvApiDTOList);
		if (rvApiDTOList.size() > rvApiDTOSet.size())
		{
			StringBuilder errorText = new StringBuilder();
			errorText.append("Apis implemented: [").
					append(rvApiDTOList.stream().map(Object::toString).collect(Collectors.joining(", "))).
					append("]. ").
					append(API_IMPLEMENTED_TWICE_MSG);
			ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), API_IMPLEMENTED_TWICE,
					errorText.toString());
		}

		return errorList;
	}

	@Override
	protected Set<String> getAsyncAPIDefinitionList(final NovaYml novaYml)
	{
		Set<String> asyncApiSet = new HashSet<>();
		if (novaYml.getAsyncapisBackToFront() != null )
		{
			asyncApiSet = novaYml.getAsyncapisBackToFront().getAsyncApis();
		}
		return asyncApiSet;
	}

	@Override
	boolean isImplementedAsServed(final NovaYml novaYml, final AsyncBackToFrontApiVersion apiVersion)
	{
		return ApiValidationUtils.hasNovaymlAnApiImplementedAsServed(novaYml);
	}

	@Override
	List<ValidationErrorDto> specificValidationsApi(final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
													final AsyncBackToFrontApiVersion apiVersion, final String asyncApiPath,
													final String tagName)
	{
		// No validation to add right now, prepared to add here all the specific validations
		return Collections.emptyList();
	}
}

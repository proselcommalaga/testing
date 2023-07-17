package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;

import java.util.List;

/**
 * YML (Swagger | AsyncApi) validator interface
 */
public interface IApiDefinitionValidator
{
	/**
	 * Validate yml
	 *
	 * @param novaYml                     Nova.yml with the api's info
	 * @param newReleaseVersionServiceDto service
	 * @param repoId                      repository id
	 * @param tag                         tag
	 * @param releaseVersionName          Release Version Name
	 */
	List<ValidationErrorDto> validateAndAssociateApi(NovaYml novaYml, NewReleaseVersionServiceDto newReleaseVersionServiceDto,
			int repoId, String tag, String releaseVersionName);

	/**
	 * Check if it is the resolver of validators or a real validator
	 * @return true if it is a real validator. False if it is the resolver
	 */
	boolean isValidator();
}

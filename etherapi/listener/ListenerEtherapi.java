package com.bbva.enoa.platformservices.coreservice.etherapi.listener;

import com.bbva.enoa.apirestgen.etherapi.model.EtherConfigStatusDTO;
import com.bbva.enoa.apirestgen.etherapi.model.EtherDeploymentServiceInventoryDto;
import com.bbva.enoa.apirestgen.etherapi.model.EtherLandingZoneInventoryDto;
import com.bbva.enoa.apirestgen.etherapi.server.spring.nova.rest.IRestListenerEtherapi;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener Ether api
 *
 * @author David Ramirez
 */
@Slf4j
@Service
public class ListenerEtherapi implements IRestListenerEtherapi
{
	/**
	 * IEtherService etherService
	 * Service to implement ether bussiness
	 */
	private final IEtherService etherService;


	/**
	 * Constructor
	 * @param pEtherService p ether service
	 */
	@Autowired
	public ListenerEtherapi(final IEtherService pEtherService)
	{
		this.etherService = pEtherService;
	}

	/**
	 * Returns the configuration status for the environment (int, pre, pro) and type (deploy or logging) selected
	 * @param novaMetadata - novaMetadata
	 * @param productId    - productId
	 * @param environment  - int, pre or pro
	 * @return             - The searched configuration
	 * @throws Errors      - Exceptions
	 */
	@Override
	@LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
	public EtherConfigStatusDTO[] getConfigurationStatus(final NovaMetadata novaMetadata, final Integer productId, final String environment) throws Errors
	{
		return this.etherService.getEtherConfigurationStatus(productId, environment);
	}

	@Override
	@LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
	public EtherLandingZoneInventoryDto[] getEtherLandingZones(final NovaMetadata novaMetadata, final String environment, final String uuaa) throws Errors
	{
		return this.etherService.getEtherLandingZones(environment, uuaa);
	}

	/**
	 * Execute the configuration for the environment (int, pre, pro) and type (deploy or logging) selected
	 * @param novaMetadata - novaMetadata
	 * @param environment  - int, pre or pro
	 * @param productId    - productId
	 * @return             - The final configuration, of the error if it exists.
	 * @throws Errors      - Exceptions
	 */
	@Override
	@LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
	public EtherConfigStatusDTO configureEtherInfrastructure(final NovaMetadata novaMetadata, final Integer productId, final String environment, final String namespace) throws Errors
	{
		// Get the iv-user
		final String ivUser = MetadataUtils.getIvUser(novaMetadata);
		return this.etherService.configureEtherInfrastructure(ivUser, environment, productId, namespace);
	}

	@Override
	@LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
	public EtherDeploymentServiceInventoryDto[] getDeploymentServicesByEnvironment(NovaMetadata novaMetadata, String environment) throws Errors
	{
		return this.etherService.getDeploymentServicesByEnvironment(novaMetadata, environment);
	}

	@Override
	@LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
	public EtherDeploymentServiceInventoryDto getDeploymentServiceByInstanceId(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
	{
		return this.etherService.getDeploymentServiceByInstanceId(instanceId);
	}
}

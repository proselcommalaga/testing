package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.client.feign.nova.rest.IRestHandlerApigatewaymanagerapi;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.client.feign.nova.rest.IRestListenerApigatewaymanagerapi;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.client.feign.nova.rest.impl.RestHandlerApigatewaymanagerapi;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMApiDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMCreateProfilingDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMCreatePublicationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerServiceDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerValidationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesResponseDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRegisterApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveProfilingDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemovePublicationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMUpdatePublicationDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.core.novaheaderserver.context.NovaRequestContext;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API Gateway Client
 */
@Service
public class ApiGatewayManagerClientImpl implements IApiGatewayManagerClient
{

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayManagerClientImpl.class);
    private static final String SUCCESSFULLY_CALLED_PARAMETERIZED_CHUNK = "[{}] -> [{}]: Successfully called. Returned [{}]";

    @Value("${nova.gatewayServices.user:nova}")
    private String novaUser;

    @Value("${nova.gatewayServices.pass:nova2018}")
    private String novaPassword;
    /**
     * Nova context
     */
    private final NovaRequestContext context;

    /**
     * Handler
     */
    private RestHandlerApigatewaymanagerapi restHandlerApigatewayapi;

    /**
     * API services.
     */
    private final IRestHandlerApigatewaymanagerapi restHandler;

    /**
     * ApigatewayClient Constructor
     *
     * @param context     novaRequestContext
     * @param restHandler restHandler instance
     */
    @Autowired
    public ApiGatewayManagerClientImpl(final NovaRequestContext context,
                                       final IRestHandlerApigatewaymanagerapi restHandler)
    {

        this.restHandler = restHandler;
        this.context = context;
    }

    /**
     * Init
     */
    @PostConstruct
    public void init()
    {

        this.restHandlerApigatewayapi = new RestHandlerApigatewaymanagerapi(restHandler);
    }

    @Override
    public AGMDockerValidationDTO[] generateDockerKey(final AGMDockerServiceDTO[] serviceNames, final String environment)
    {

        // Received parameters
        LOG.debug("[{}] -> [{}]: for the environment [{}] and services [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.GENERATE_DOCKER_KEY, environment, serviceNames);

        // Creates response object
        SingleApiClientResponseWrapper<AGMDockerValidationDTO[]> response = new SingleApiClientResponseWrapper<>();

        try
        {
            // Sets Credentials on Header
            this.setCredentials();

            // Do the "restHandler" call
            this.restHandlerApigatewayapi.generateDockerKey(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void generateDockerKey(AGMDockerValidationDTO[] outcome)
                {
                    LOG.debug(SUCCESSFULLY_CALLED_PARAMETERIZED_CHUNK, Constants.API_GATEWAY_CLIENT,
                            Constants.GENERATE_DOCKER_KEY, outcome);

                    response.set(outcome);
                }

                // Error
                @Override
                public void generateDockerKeyErrors(final Errors outcome)
                {

                    LOG.error("[{}] -> [{}]: Error response in invocation generateDockerKeys. Error response [{}]",
                            Constants.API_GATEWAY_CLIENT, Constants.GENERATE_DOCKER_KEY, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getGeneratingDockerKeyError(), outcome);
                }

            }, serviceNames, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }

        return response.get();
    }


    @Override
    public void createPublication(final AGMCreatePublicationDTO createPublicationDTO, final String environment)
    {

        LOG.debug("[{}] -> [{}]: Creating publication for uuaa [{}] and release [{}] on environment [{}]", Constants.API_GATEWAY_CLIENT, Constants.CREATE_PUBLICATION,
                createPublicationDTO.getUuaa(), createPublicationDTO.getRelease(), environment);

        // Creates response object
        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.createPublication(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void createPublication()
                {
                    LOG.debug("[{}] -> [{}]: Created publication: {}", Constants.API_GATEWAY_CLIENT,
                            Constants.CREATE_PUBLICATION, createPublicationDTO);
                }

                // Error
                @Override
                public void createPublicationErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to createPublication, [{}]",
                            Constants.API_GATEWAY_CLIENT, Constants.CREATE_PUBLICATION, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getCreatePublicationError(createPublicationDTO.getUuaa(), createPublicationDTO.getRelease(), environment, outcome), outcome);
                }

            }, createPublicationDTO, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public void removePublication(final AGMRemovePublicationDTO removePublicationDTO, final String environment)
    {

        LOG.debug("[{}] -> [{}]: Removing publication for uuaa [{}] and release [{}] on environment [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.REMOVE_PUBLICATION, removePublicationDTO.getUuaa(), removePublicationDTO.getRelease(), environment);

        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.removePublication(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void removePublication()
                {

                    LOG.debug("[{}] -> [{}]: Removed publication: {}",
                            Constants.API_GATEWAY_CLIENT, Constants.REMOVE_PUBLICATION, removePublicationDTO);
                }

                // Error
                @Override
                public void removePublicationErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to removePublication: {}", Constants.API_GATEWAY_CLIENT,
                            Constants.REMOVE_PUBLICATION, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getRemovePublicationError(removePublicationDTO.getUuaa(), removePublicationDTO.getRelease(), environment, outcome), outcome);
                }

            }, removePublicationDTO, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public void updatePublication(final AGMUpdatePublicationDTO updatePublicationDTO, final String environment)
    {

        LOG.debug("[{}] -> [{}]: Updating publication for uuaa [{}] and release [{}] on environment [{}]", Constants.API_GATEWAY_CLIENT, Constants.UPDATE_PUBLICATION,
                updatePublicationDTO.getUuaa(), updatePublicationDTO.getRelease(), environment);

        // Creates response object
        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.updatePublication(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void updatePublication()
                {
                    LOG.debug("[{}] -> [{}]: Created publication: {}", Constants.API_GATEWAY_CLIENT,
                            Constants.UPDATE_PUBLICATION, updatePublicationDTO);
                }

                // Error
                @Override
                public void updatePublicationErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to updatePublication, [{}]",
                            Constants.API_GATEWAY_CLIENT, Constants.UPDATE_PUBLICATION, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getUpdatePublicationError(updatePublicationDTO.getUuaa(), updatePublicationDTO.getRelease(), environment, outcome), outcome);
                }

            }, updatePublicationDTO, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public void createRegister(final AGMRegisterApiDTO registerApiDTO)
    {
        LOG.debug("[{}] -> [{}]: Creating register for uuaa [{}], basepath [{}] and apiName [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.CREATE_REGISTER, registerApiDTO.getUuaa(), registerApiDTO.getBasepath(), registerApiDTO.getApiName());

        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.createRegister(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void createRegister()
                {
                    LOG.debug("[{}] -> [{}]: Created register: {}",
                            Constants.API_GATEWAY_CLIENT, Constants.CREATE_REGISTER, registerApiDTO);
                }

                // Error
                @Override
                public void createRegisterErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to createRegister {}", Constants.API_GATEWAY_CLIENT,
                            Constants.CREATE_REGISTER, outcome.getBodyExceptionMessage());
                    throw new NovaException(ApiManagerError.getCreateRegisterError(registerApiDTO.getUuaa(), registerApiDTO.getBasepath(),
                            registerApiDTO.getApiName()), outcome);
                }

            }, registerApiDTO);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public void removeRegister(final AGMRemoveApiDTO removeApiDTO)
    {
        LOG.debug("[{}] -> [{}]: Removing register for uuaa [{}], basepath [{}] and apiName [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.REMOVE_REGISTER, removeApiDTO.getUuaa(), removeApiDTO.getBasepath(), removeApiDTO.getApiName());

        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.removeRegister(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void removeRegister()
                {
                    LOG.debug("[{}] -> [{}]: Removed register: {}",
                            Constants.API_GATEWAY_CLIENT, Constants.REMOVE_REGISTER, removeApiDTO);
                }

                // Error
                @Override
                public void removeRegisterErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to removeRegister {}", Constants.API_GATEWAY_CLIENT,
                            Constants.REMOVE_REGISTER, outcome.getBodyExceptionMessage());
                    throw new NovaException(ApiManagerError.getCreateRegisterError(removeApiDTO.getUuaa(), removeApiDTO.getBasepath(),
                            removeApiDTO.getApiName()), outcome);
                }

            }, removeApiDTO);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public AGMPoliciesResponseDTO getPolicies(final AGMApiDetailDTO apiDetailDTO)
    {
        // Received parameters
        LOG.debug("[{}] -> [{}]: Retrieving policies for the api [{}] of uuaa [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.GET_POLICIES, apiDetailDTO.getApiName(), apiDetailDTO.getUuaa());

        // Creates response object
        SingleApiClientResponseWrapper<AGMPoliciesResponseDTO> response = new SingleApiClientResponseWrapper<>();

        try
        {
            // Sets Credentials on Header
            this.setCredentials();

            // Do the "restHandler" call
            this.restHandlerApigatewayapi.getPolicies(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void getPolicies(AGMPoliciesResponseDTO outcome)
                {
                    LOG.debug(SUCCESSFULLY_CALLED_PARAMETERIZED_CHUNK, Constants.API_GATEWAY_CLIENT,
                            Constants.GET_POLICIES, outcome);

                    response.set(outcome);
                }

                // Error
                @Override
                public void getPoliciesErrors(final Errors outcome)
                {

                    LOG.error("[{}] -> [{}]: Error response in invocation getPolicies. Error response [{}]",
                            Constants.API_GATEWAY_CLIENT, Constants.GET_POLICIES, outcome.getBodyExceptionMessage());
                    AGMPoliciesResponseDTO agmPoliciesResponseDTO = new AGMPoliciesResponseDTO();
                    AGMPoliciesDTO[] agmPoliciesDTO = new AGMPoliciesDTO[0];
                    agmPoliciesResponseDTO.setPolicies(agmPoliciesDTO);
                    response.set(agmPoliciesResponseDTO);
                }

            }, apiDetailDTO);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }

        return response.get();
    }

    @Override
    public String[] getRoles(final String uuaa, final String environment)
    {
        // Received parameters
        LOG.debug("[{}] -> [{}]: Retrieving roles for uuaa [{}] on environment [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.GET_ROLES, uuaa, environment);

        // Creates response object
        SingleApiClientResponseWrapper<String[]> response = new SingleApiClientResponseWrapper<>();

        try
        {
            // Sets Credentials on Header
            this.setCredentials();

            // Do the "restHandler" call
            this.restHandlerApigatewayapi.getRoles(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void getRoles(String[] outcome)
                {
                    LOG.debug(SUCCESSFULLY_CALLED_PARAMETERIZED_CHUNK, Constants.API_GATEWAY_CLIENT,
                            Constants.GET_ROLES, outcome);

                    response.set(outcome);
                }

                // Error
                @Override
                public void getRolesErrors(final Errors outcome)
                {

                    LOG.error("[{}] -> [{}]: Error response in invocation getRoles. Error response [{}]",
                            Constants.API_GATEWAY_CLIENT, Constants.GET_ROLES, outcome.getBodyExceptionMessage());
                    throw new NovaException(ApiManagerError.getRolesFromCESError(uuaa, environment), outcome);
                }

            }, environment, uuaa);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }

        return response.get();
    }

    @Override
    public void createProfiling(final AGMCreateProfilingDTO createProfilingDTO, final String environment)
    {
        LOG.debug("[{}] -> [{}]: Creating profiling for uuaa [{}] in environment [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.CREATE_PROFILING, createProfilingDTO.getUuaa(), environment);

        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.createProfiling(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void createProfiling()
                {
                    LOG.debug("[{}] -> [{}]: Created profiling: {}",
                            Constants.API_GATEWAY_CLIENT, Constants.CREATE_PROFILING, createProfilingDTO);
                }

                // Error
                @Override
                public void createProfilingErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to createProfiling {}", Constants.API_GATEWAY_CLIENT,
                            Constants.CREATE_PROFILING, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getCreateProfilingError(createProfilingDTO.getUuaa(), environment), outcome);
                }

            }, createProfilingDTO, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }

    @Override
    public void removeProfiling(final AGMRemoveProfilingDTO removeProfilingDTO, final String environment)
    {
        LOG.debug("[{}] -> [{}]: Removing profiling for uuaa [{}] in environment [{}]", Constants.API_GATEWAY_CLIENT,
                Constants.REMOVE_PROFILING, removeProfilingDTO.getUuaa(), environment);

        try
        {
            // Sets Credentials on Header
            this.setCredentials();
            // Do the "restHandler" call
            this.restHandlerApigatewayapi.removeProfiling(new IRestListenerApigatewaymanagerapi()
            {

                // Success
                @Override
                public void removeProfiling()
                {
                    LOG.debug("[{}] -> [{}]: Removed profiling: {}",
                            Constants.API_GATEWAY_CLIENT, Constants.REMOVE_PROFILING, removeProfilingDTO);
                }

                // Error
                @Override
                public void removeProfilingErrors(final Errors outcome)
                {
                    LOG.error("[{}] -> [{}]: Error response in invocation to removeProfiling {}", Constants.API_GATEWAY_CLIENT,
                            Constants.REMOVE_PROFILING, outcome.getBodyExceptionMessage());
                    throw new NovaException(DeploymentError.getRemoveProfilingError(removeProfilingDTO.getUuaa(), environment), outcome);
                }

            }, removeProfilingDTO, environment);
        }
        finally
        {
            // Cleans credentials from Header
            this.cleanCredentials();
        }
    }


    /**
     * This method unsets the credentials on the requests and removes the basic authentication from nova context request
     */
    private void cleanCredentials()
    {

        // removes Basic Authentication header
        this.context.setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    /**
     * This Method is in charge to set Credentials to the request for Basic Authentication User and Password
     */
    private void setCredentials()
    {

        // Set User and password
        String userPassword = this.novaUser + ":" + this.novaPassword;

        // encodes user and password
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes(StandardCharsets.UTF_8));

        // Sets Basic Authentication Header
        this.context.setHeader(Constants.AUTHORIZATION_HEADER, String.format("%s %s", Constants.BASIC, encoding));
    }

}

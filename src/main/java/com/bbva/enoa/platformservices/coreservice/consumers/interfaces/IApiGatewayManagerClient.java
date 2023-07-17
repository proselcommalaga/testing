package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;

/**
 * This Interfaces expose ApiGateway Client services, to publish API's, remove it form Api Gateway
 *
 */
public interface IApiGatewayManagerClient
{

    /**
     * Generate dockerkey
     *
     * @param environment Environment INT, PRE, PRO
     * @param serviceName ServiceName for which the docker key will be generated
     * @return DockerValidationDto with docker key
     */
    AGMDockerValidationDTO[] generateDockerKey(final AGMDockerServiceDTO[] serviceName, final String environment);


    /**
     * This method is in charge of publishing API details into APIGateway.
     *
     * @param createPublicationDTO      detail of the publication
     * @param environment environment
     */
    void createPublication(final AGMCreatePublicationDTO createPublicationDTO, final String environment);

    /**
     * remove publication from api Gateway
     *
     * @param removePublicationDTO  release to be removed
     * @param environment environment
     */
    void removePublication(final AGMRemovePublicationDTO removePublicationDTO, final String environment);

    /**
     * update publication on api Gateway
     *
     * @param updatePublicationDTO info for updating
     * @param environment          environment
     */
    void updatePublication(final AGMUpdatePublicationDTO updatePublicationDTO, final String environment);

    /**
     * Creates an API register from CIBGW
     *
     * @param registerApiDTO api information
     */
    void createRegister(final AGMRegisterApiDTO registerApiDTO);

    /**
     * Removes an API register from CIBGW
     *
     * @param removeApiDTO api information
     */
    void removeRegister(final AGMRemoveApiDTO removeApiDTO);

    /**
     * Retrieves an api's policies
     *
     * @param apiDetailDTO apin information
     * @return list of policies
     */
    AGMPoliciesResponseDTO getPolicies(final AGMApiDetailDTO apiDetailDTO);

    /**
     * Retrieves roles for an uuaa in a environment
     *
     * @param uuaa        uuaa
     * @param environment environment
     * @return list of roles
     */
    String[] getRoles(final String uuaa, final String environment);

    /**
     * Add resources to roles in CES
     *
     * @param createProfilingDTO profiling info
     * @param environment        environment
     */
    void createProfiling(final AGMCreateProfilingDTO createProfilingDTO, final String environment);

    /**
     * Remove profiling at CES
     *
     * @param removeProfilingDTO profiling info
     * @param environment        environment
     */
    void removeProfiling(final AGMRemoveProfilingDTO removeProfilingDTO, final String environment);
}

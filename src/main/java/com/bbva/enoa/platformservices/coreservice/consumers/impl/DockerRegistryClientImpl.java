package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.dockerregistryapi.client.feign.nova.rest.IRestHandlerDockerregistryapi;
import com.bbva.enoa.apirestgen.dockerregistryapi.client.feign.nova.rest.IRestListenerDockerregistryapi;
import com.bbva.enoa.apirestgen.dockerregistryapi.client.feign.nova.rest.impl.RestHandlerDockerregistryapi;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * DockerRegistryClientImpl
 */
@Service
@Slf4j
public class DockerRegistryClientImpl implements IDockerRegistryClient
{
    private static final String IS_IMAGE_IN_REGISTRY = "isImageInRegistry";

    @Autowired
    private IRestHandlerDockerregistryapi restInterface;

    /**
     * API services.
     */
    private RestHandlerDockerregistryapi restHandler;

    @Override
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerDockerregistryapi(this.restInterface);
    }

    @Override
    public boolean isImageInRegistry(String ivUser, String nameImage)
    {
        // Printout the received parameter 'outcome'
        log.debug("[{}] -> [{}]: nameImage {}", Constants.DOCKER_REGISTRY_CLIENT, IS_IMAGE_IN_REGISTRY, nameImage);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>(false);

        this.restHandler.getImageByTag(
                new IRestListenerDockerregistryapi()
                {

                    @Override
                    public void getImageByTag(final String outcome)
                    {
                        log.debug("[{}] -> [{}]: Image {} found in registry", Constants.DOCKER_REGISTRY_CLIENT, IS_IMAGE_IN_REGISTRY, outcome);
                        response.set(true);

                    }

                    @Override
                    public void getImageByTagErrors(Errors outcome)
                    {
                        log.warn("[{}] -> [{}]: Error response in invocation to getImageByTag Docker Registry. Error message: [{}]",
                                Constants.DOCKER_REGISTRY_CLIENT, IS_IMAGE_IN_REGISTRY, outcome.getBodyExceptionMessage());
                        response.set(false);
                    }
                }, nameImage);


        return response.get();
    }
}

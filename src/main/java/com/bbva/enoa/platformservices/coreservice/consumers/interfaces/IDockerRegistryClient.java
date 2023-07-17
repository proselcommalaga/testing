package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import javax.annotation.PostConstruct;

/**
 * DockerRegistryClientImpl
 */
public interface IDockerRegistryClient
{
    /**
     * Init the handler and listener.
     */
    @PostConstruct
    void init();

    /**
     * Successful call
     * If Image response is null or empty then the Image does not exist in the repository else it does.
     *
     * @param ivUser    user
     * @param nameImage name of image to validate if exist in docker registry
     * @return true if image exist in repository
     */
    boolean isImageInRegistry(String ivUser, String nameImage);
}

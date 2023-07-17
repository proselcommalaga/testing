package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces;

/**
 * The interface Callable. wrapper to make calls over jaxrs client
 *
 * @param <T> the type parameter
 * @param <U> the type parameter
 */
@FunctionalInterface
public interface Callable<T,U>
{
    /**
     * Call.
     *
     * @param config  the config
     * @param handler the handler
     * @throws Exception the exception
     */
    void call(T config,U handler) throws Exception;
}

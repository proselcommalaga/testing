package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces;

/**
 * The interface Callable. wrapper to make calls over jaxrs client
 *
 * @param <T> the type parameter
 * @param <U> the type parameter
 * @param <R> the type parameter
 */
@FunctionalInterface
public interface CallableReturnable<T,U,R>
{
    /**
     * Call.
     *
     * @param config  the config
     * @param handler the handler
     * @return the r
     * @throws Exception  the exception
     */
    R call(T config,U handler) throws Exception;
}

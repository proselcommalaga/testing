package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces;

/**
 * The interface Nova agent caller. Implements that interface to define how to call an specific api
 * served in the NOVA AGENT service in a give environment.
 *
 * @param <T> the type parameter. The configuration needed to make de call.
 * @param <U> the type parameter. The Type of the restHandler for that specific api.
 */
public interface INovaAgentCaller<T, U>
{
    /**
     * Call.
     *
     * @param environment    the environment
     * @param methodCallable the method callable
     */
    void callOnEnvironment(final String environment, Callable<T, U> methodCallable);

    /**
     * Call on environment with return r.
     *
     * @param <R>            the type parameter
     * @param environment    the environment
     * @param methodCallable the method callable
     * @return the r
     */
    <R> R callOnEnvironmentWithReturn(final String environment, CallableReturnable<T, U, R> methodCallable);
}

package com.bbva.enoa.platformservices.coreservice.common.interfaces;

/**
 * Functional Interface that given an exception manages it and throws another
 * exception
 * 
 * @author XE63267
 *
 * @param <T> Exception
 */
@FunctionalInterface
public interface ExceptionProcessor<T extends Throwable>
{

    /**
     * Processes an exception and may throw another exception
     * 
     * @param t
     *            Exception to process
     * @throws T
     *             Exception thrown by this function
     */
    public void process(Throwable t) throws T;

}
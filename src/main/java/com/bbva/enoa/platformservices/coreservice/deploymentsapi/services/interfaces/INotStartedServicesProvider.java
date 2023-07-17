package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import java.util.List;

/**
 * Interface for not started services provider object,
 */
public interface INotStartedServicesProvider
{
    /**
     * Returns a list of "finalName:version" strings for not started services of a plan.
     *
     * @param versionedFinalNames A "finalName:version" string list for searching purposes.
     * @param environment         The execution environment of the services.
     * @return A list of "finalName:version" strings for not started services of a plan.
     */
    List<String> getNotStartedVersionedNamesFromServiceExecutionHistory(List<String> versionedFinalNames, final String environment);
}

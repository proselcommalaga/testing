package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Batch schedule interface
 */
public interface IBatchScheduleService
{
    /**
     * Add all the batch schedule services into the release version
     *
     * @param releaseVersion the release version to add the batch schedule service
     * @throws NovaException NovaException
     */
    void addBatchSchedulServices(ReleaseVersion releaseVersion) throws NovaException;

    /**
     * Delete all the batch schedule services into the release version
     *
     * @param releaseVersion the release version to add the batch schedule service
     * @throws NovaException NovaException
     */
    void deleteBatchScheduleServices(ReleaseVersion releaseVersion) throws NovaException;

    /**
     * Get a deployment batch schedule
     *
     * @param releaseVersionServiceId a release version service id
     * @param deploymentPlanId        a deployment plan id
     * @return a deployment batch schedule DTO
     */
    DeploymentBatchScheduleDTO getDeploymentBatchSchedule(final Integer releaseVersionServiceId, final Integer deploymentPlanId);

    /**
     * Get the scheduler yml in string format
     * @param batchSchedulerService the batch scheduler service
     * @return a scheduler yml converted to string
     * @throws NovaException NovaException if error
     */
    String getSchedulerYmlStringFile(ReleaseVersionService batchSchedulerService) throws NovaException;

    /**
     * Build a batch name list from scheduler yml file
     * @param schedulerYmlFileString the scheduler yml file in string format
     * @param batchSchedulerServiceName the batch scheduler service name
     * @return a batch names list
     * @throws NovaException NovaException if error
     */
    List<String> buildBatchNameList(String schedulerYmlFileString, String batchSchedulerServiceName) throws NovaException;
}

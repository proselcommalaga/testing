package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;

import java.util.Calendar;
import java.util.List;

public interface IRepositoryManagerService
{
    /**
     * Save deployment plan
     *
     * @param plan plan
     */
    void savePlan(DeploymentPlan plan);

    /**
     * Find plan
     *
     * @param id id
     * @return plan
     */
    DeploymentPlan findPlan(Integer id);

    /**
     * Find plan by environment and status
     *
     * @param deploymentStatus deployment status
     * @param environment      environment
     * @return a deployment plan list
     */
    List<DeploymentPlan> findByStatusAndEnvironmentAndUndeploymentDateNotNull(DeploymentStatus deploymentStatus, Environment environment);

    /**
     * Find release version list by status
     *
     * @param releaseVersionStatus the status of the release version
     * @return the release version list
     */
    List<ReleaseVersion> findReleaseVersionByStatus(ReleaseVersionStatus releaseVersionStatus);

    /**
     * Save deployment service
     *
     * @param service service
     */
    void saveService(DeploymentService service);

    /**
     * Flush service repository
     */
    void flushServiceRepository();

    /**
     * Save deployment subsystem
     *
     * @param subsystem deployment subsystem
     */
    void saveSubsystem(DeploymentSubsystem subsystem);

    /**
     * Get all deployment instance of service type provided before creation day or date provided
     *
     * @param calendar        the date before provided
     * @param serviceTypeList the service type list to get all the deployment instance
     * @return a list of deployment instance of service type before to date provided
     */
    List<DeploymentInstance> findBatchDeploymentInstance(Calendar calendar, List<ServiceType> serviceTypeList);

    /**
     * Delete a deployment instance from bbdd
     *
     * @param deploymentInstance a deployment instance to delete
     */
    void deleteDeploymentInstance(DeploymentInstance deploymentInstance);
}

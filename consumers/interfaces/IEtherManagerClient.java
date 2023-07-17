package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckEtherResourcesStatusRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckReadyToDeployRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherConfigurationRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherManagerConfigStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherUserManagementDTO;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermission;

public interface IEtherManagerClient
{
    /**
     * Deploy a plan on ether infrastructure
     *
     * @param etherDeploymentDTO deployment data transfer object
     * @return true when send the request in other case false
     */
    boolean deployEtherPlan(EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Configure services of plan in order to be able to log/trace in Ether
     *
     * @param etherDeploymentDTO contains the list of services to configure
     * @return true when all services have been successfully configured
     */
    boolean configureServicesForLogging(EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Remove plan
     *
     * @param etherDeploymentDTO plan to remove
     * @return true if request has been sent in other case false
     */
    boolean removeEtherPlan(EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Replace an ether plan
     *
     * @param etherDeploymentDTO    plan to replace
     * @param newEtherDeploymentDTO new plan
     * @return true when the request is ok in other case false
     */
    boolean replaceEtherPlan(EtherDeploymentDTO etherDeploymentDTO, EtherDeploymentDTO newEtherDeploymentDTO);

    /**
     * Get the number of running instances on a container cloud for a service.
     *
     * @param etherDeploymentDTO service
     * @return Number of running instances.
     */
    ServiceStatusDTO getServiceStatus(final EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Checks if ether resources are ready to deploy
     *
     * @param checkReadyToDeployRequestDTO request data
     * @return true if resources are ready
     */
    boolean readyToDeploy(CheckReadyToDeployRequestDTO checkReadyToDeployRequestDTO);

    /**
     * Get the number of running instances on a container cloud for a deployment.
     *
     * @param etherDeploymentDTO service
     * @return Number of running instances.
     */
    StatusDTO getDeploymentStatus(final EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Get the number of running instances on a container cloud for a subsystem
     *
     * @param etherSubsystemDTOArray list of subsystems
     * @return Number of running services
     */
    EtherSubsystemStatusDTO[] getSubsystemStatus(final EtherSubsystemDTO[] etherSubsystemDTOArray);

    /**
     * Start a service deployed on cloud infrastructure
     *
     * @param etherDeploymentDTO deployment service
     */
    void startEtherService(final EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Stop a service deployed on cloud infrastructure
     *
     * @param etherDeploymentDTO deployment service
     */
    void stopEtherService(final EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Restart a service deployed on cloud infrastructure
     *
     * @param etherDeploymentDTO deployment service
     */
    void restartEtherService(final EtherDeploymentDTO etherDeploymentDTO);

    /**
     * Gets all the information status about ether for the product
     *
     * @param etherResourcesStatusRequestDTO Contains al the information needed for executing the request
     * @return The configuration Status
     */
    EtherManagerConfigStatusDTO checkEtherResourcesStatus(
            CheckEtherResourcesStatusRequestDTO etherResourcesStatusRequestDTO);

    /**
     * Execute the configuration of the namespace informed in the RQ
     *
     * @param etherManagerConfigurationRequest Contains al the information needed for executing the request
     * @return The configuration Status
     */
    EtherManagerConfigStatusDTO configureEtherInfrastructure(EtherConfigurationRequestDTO etherManagerConfigurationRequest);

    /**
     * Method to add users to Ether group of the product
     *
     * @param etherUserManagementDTO Contains the name of the product and the users to add
     */
    void addUsersToGroup(EtherUserManagementDTO etherUserManagementDTO);

    /**
     * Method to remove users from the Ether group of the product
     *
     * @param etherUserManagementDTO Contains the name of the product and the users to remove
     */
    void removeUsersFromGroup(EtherUserManagementDTO etherUserManagementDTO);

    /**
     * Method to set the permission in ETHER
     *
     * @param externalUserPermission the permission to create
     * @param namespace              the namespace
     * @param environment            the environment
     */
    void setPermission(ExternalUserPermission externalUserPermission, String namespace, String environment);

    /**
     * Method to unset the permission in ETHER
     *
     * @param externalUserPermission the permission to create
     * @param namespace              the namespace
     * @param environment            the environment
     */
    void unsetPermission(ExternalUserPermission externalUserPermission, String namespace, String environment);
}

package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * The interface Behavior configuration service.
 */
public interface IBehaviorConfigurationService
{
    /**
     * Get all behavior versions bv behavior version summary info dto [ ].
     *
     * @param productId the product id
     * @param status    the status
     * @return the bv behavior version summary info dto [ ]
     */
    //BVBehaviorVersionSummaryInfoDTO[] getAllBehaviorVersions(Integer productId, String status);

    /**
     * Gets behavior broker configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior broker configuration resource
     */
    BVBrokersResourceDto getBehaviorBrokerConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior connectors configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior connectors configuration resource
     */
    BVConnectorsResourceDto getBehaviorConnectorsConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior filesystems configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior filesystems configuration resource
     */
    BVFilesystemResourceDto getBehaviorFilesystemsConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior hardware configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior hardware configuration resource
     */
    BVHardwareResourceDto getBehaviorHardwareConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior jvm configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior jvm configuration resource
     */
    BVJVMResourceDto getBehaviorJvmConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior properties configuration resource.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior properties configuration resource
     */
    BVPropertiesResourceDto getBehaviorPropertiesConfigurationResource(Integer behaviorServiceId);

    /**
     * Gets behavior service summary resources.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior service summary resources
     */
    BVBehaviorServiceConfigurationSummary getBehaviorServiceSummaryResources(Integer behaviorServiceId);

    /**
     * Gets behavior version.
     *
     * @param behaviorVersionId the behavior version id
     * @return the behavior version
     */
    BVBehaviorVersionSummaryInfoDTO getBehaviorVersion(Integer behaviorVersionId);

    /**
     * Get behavior versions subsystems bv behavior version subsystem dto [ ].
     *
     * @param behaviorVersionId the behavior version id
     * @return the bv behavior version subsystem dto [ ]
     */
    BVBehaviorVersionSubsystemDTO[] getBehaviorVersionsSubsystems(Integer behaviorVersionId);

    /**
     * Update behavior broker configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorBrokerConfigurationResource(BVConfigurableBroker[] resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Update behavior connectors configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorConnectorsConfigurationResource(BVConfigurableConnector[] resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Update behavior filesystems configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorFilesystemsConfigurationResource(BVConfigurableFilesystem[] resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Update behavior hardware configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorHardwareConfigurationResource(BVConfigurableHardware resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Update behavior jvm configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorJvmConfigurationResource(BVConfigurableJvm resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Update behavior properties configuration resource.
     *
     * @param resourceDtoToUpdate the resource dto to update
     * @param behaviorServiceId   the behavior service id
     */
    void updateBehaviorPropertiesConfigurationResource(BVConfigurableProperty[] resourceDtoToUpdate, Integer behaviorServiceId);

    /**
     * Gets behavior version costs info.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior version costs info
     */
    BVBehaviorServiceBudgetInfo getBehaviorVersionCostsInfo(Integer behaviorServiceId);

    /**
     * Configure behavior environment bv behavior service configuration summary.
     *
     * @param behaviorServiceId the behavior service id
     */
    BehaviorServiceConfiguration configureBehaviorEnvironment(Integer behaviorServiceId);

    /**
     * Gets behavior execution info.
     *
     * @param behaviorServiceId the behavior service id
     * @return the behavior execution info
     */
    BVExecutionInfoDto getBehaviorExecutionInfo(Integer behaviorServiceId);

    /**
     * Start a behavior service configuration execution
     *
     * @param userCode                user
     * @param behaviorBatchParams     behavior service params
     * @param behaviorConfigurationId behavior service configuration
     */
    void executeBehaviorConfiguration(final String userCode, final BVBehaviorParamsDTO behaviorBatchParams, final Integer behaviorConfigurationId) throws NovaException;

    /**
     * Stop a behavior service configuration execution
     *
     * @param ivUser              user
     * @param behaviorExecutionId behavior execution id
     */
    void stopBehaviorConfigurationExecution(String ivUser, Integer behaviorExecutionId);

    /**
     * Remove a behavior execution
     *
     * @param behaviorInstanceId the behavior service id
     */
    void removeBehaviorInstanceExecution(Integer behaviorInstanceId);

    /**
     * Path to obtain all the behavior versions execution results
     *
     * @param behaviorServiceId the behavior service id
     * @param behaviorServiceExecFrom the behavior version execution from date in milliseconds
     * @param behaviorServiceExecTo the behavior version execution to date in milliseconds
     * @param pageSize the behavior version execution from date in milliseconds
     * @param pageNumber the behavior version execution to date in milliseconds
     * @return the executions list for a concrete behavior service
     */
    BVExecutionPageableDTO getBehaviorServiceExecutions(Integer behaviorServiceId, Long behaviorServiceExecFrom, Long behaviorServiceExecTo, Integer pageNumber, Integer pageSize);

    /**
     * Path to obtain all the behavior version execution results
     *
     * @param behaviorVersionId the behavior version id
     * @param pageSize the behavior version execution from date in milliseconds
     * @param pageNumber the behavior version execution to date in milliseconds
     * @param behaviorServiceName the behavior version name to filter
     * @return the executions list for a concrete behavior service
     */
    BVExecutionPageableDTO getBehaviorVersionExecutions(final Integer behaviorVersionId, String behaviorServiceName, Integer pageSize, Integer pageNumber);
}
package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplacePlanQueryResult;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplaceServiceInfo;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentReplacePlanServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeploymentReplacePlanService implements IDeploymentReplacePlanServiceImpl
{
    /**
     * Entity manager
     */
    private final EntityManager entityManager;

    /**
     * Deployment service repository
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * LibraryManager service
     */
    private final ILibraryManagerService libraryManagerService;


    /**
     * JdkParameter repository
     */
    private final JdkParameterRepository jdkParameterRepository;

    /**
     * Service for manager how deploy the releases
     * @param entityManager
     * @param deploymentServiceRepository
     * @param libraryManagerService
     * @param jdkParameterRepository
     */
    @Autowired
    public DeploymentReplacePlanService(final EntityManager entityManager, final DeploymentServiceRepository deploymentServiceRepository,
            final ILibraryManagerService libraryManagerService, final JdkParameterRepository jdkParameterRepository)
    {
        this.entityManager = entityManager;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.libraryManagerService = libraryManagerService;
        this.jdkParameterRepository = jdkParameterRepository;
    }

    ////////////////////////////////////////////////////////////////// PRIVATE METHODS ///////////////////////

    public Map<String, List<DeploymentService>> getUncommonReplacePlan(Integer planId, Integer newPlanId)
    {
        List<ReplaceServiceInfo> newReplacePlanQueryResultList = this.getAllReplaceServiceInfo(this.buildReplacePlanQueryResultList(newPlanId));
        List<ReplaceServiceInfo> oldReplacePlanQueryResultList = this.getAllReplaceServiceInfo(this.buildReplacePlanQueryResultList(planId));

        return this.getReplacePlanDifferences(newReplacePlanQueryResultList, oldReplacePlanQueryResultList);
    }



    private Map<String, List<DeploymentService>> getReplacePlanDifferences(final List<ReplaceServiceInfo> newReplaceServiceInfoList, List<ReplaceServiceInfo> oldReplaceServiceInfoList)
    {
        Map<String, List<DeploymentService>> updateReplacePlanService = new HashMap<>();
        //Build a map with all object from old replace service list where service name key
        Map<String, ReplaceServiceInfo> oldReplaceServiceInfoMap = oldReplaceServiceInfoList.stream().collect(Collectors.toMap(ReplaceServiceInfo::getServiceName, Function.identity()));

        List<DeploymentService> createServiceList = new ArrayList<>();
        List<DeploymentService> removeServiceList = new ArrayList<>();
        List<DeploymentService> updateInstanceList = new ArrayList<>();
        List<DeploymentService> restartServiceList = new ArrayList<>();

        Map<Integer, Set<LMLibraryEnvironmentsByServiceDTO>> librariesMap = this.getUsedLibraries(newReplaceServiceInfoList, oldReplaceServiceInfoList);

        for (ReplaceServiceInfo newReplaceServiceInfo : newReplaceServiceInfoList)
        {
            ReplaceServiceInfo replaceServiceInfo = oldReplaceServiceInfoMap.get(newReplaceServiceInfo.getServiceName());

            if (replaceServiceInfo != null)
            {
                // Check version && Check if the services does not use any library
                if (this.validateSameServiceVersion(newReplaceServiceInfo, replaceServiceInfo)
                        && this.isServiceLibrariesFree(replaceServiceInfo.getDeploymentService().getService().getId(), librariesMap))
                {
                    // Check if exist any change, in this case we create the new service and remove the old one.
                    this.validateReplaceServiceChanged(createServiceList, removeServiceList, updateInstanceList,
                            newReplaceServiceInfo, replaceServiceInfo);

                    // Check properties change, in this case the service may be stopped
                    this.validateReplaceServicePropertiesChanged(restartServiceList, newReplaceServiceInfo, replaceServiceInfo);
                }
                else
                {
                    createServiceList.add(newReplaceServiceInfo.getDeploymentService());
                    removeServiceList.add(replaceServiceInfo.getDeploymentService());
                }
                //Remove service which checked. The result of this list are all services which exist in old plan and don´t exist in the new one
                oldReplaceServiceInfoMap.remove(replaceServiceInfo.getServiceName());
            }
            else
            {
                createServiceList.add(newReplaceServiceInfo.getDeploymentService());
            }
        }

        removeServiceList.addAll(new ArrayList<>(oldReplaceServiceInfoMap.values().stream().map(ReplaceServiceInfo::getDeploymentService).collect(Collectors.toList())));

        restartServiceList.removeAll(removeServiceList);

        updateReplacePlanService.put(DeploymentConstants.CREATE_SERVICE, createServiceList);
        updateReplacePlanService.put(DeploymentConstants.REMOVE_SERVICE, removeServiceList);
        updateReplacePlanService.put(DeploymentConstants.RESTART_SERVICE, restartServiceList);
        updateReplacePlanService.put(DeploymentConstants.UPDATE_INSTANCE, updateInstanceList);

        if(log.isDebugEnabled())
        {
            log.debug("[DeploymentReplacePlanService] -> [getReplacePlanDifferences]: createServiceList: {} ", createServiceList.stream()
                    .map(d -> d.getService().toString() + ";" + d.toString())
                    .collect(Collectors.joining("|")));
            log.debug("[DeploymentReplacePlanService] -> [getReplacePlanDifferences]: removeServiceList: {} ", removeServiceList.stream()
                    .map(d -> d.getService().toString() + ";" + d.toString())
                    .collect(Collectors.joining("|")));
            log.debug("[DeploymentReplacePlanService] -> [getReplacePlanDifferences]: restartServiceList: {} ", restartServiceList.stream()
                    .map(d -> d.getService().toString() + ";" + d.toString())
                    .collect(Collectors.joining("|")));
            log.debug("[DeploymentReplacePlanService] -> [getReplacePlanDifferences]: updateInstanceList: {} ", updateInstanceList.stream()
                    .map(d -> d.getService().toString() + ";" + d.toString())
                    .collect(Collectors.joining("|")));
        }
        return updateReplacePlanService;
    }

    /**
     * Check if both service are the same version
     *
     * @param newReplaceServiceInfo new service
     * @param oldReplaceServiceInfo old service
     * @return true if is the same service in other case false
     */
    private boolean validateSameServiceVersion(final ReplaceServiceInfo newReplaceServiceInfo, final ReplaceServiceInfo oldReplaceServiceInfo)
    {
        return newReplaceServiceInfo.getServiceVersion().equals(oldReplaceServiceInfo.getServiceVersion());
    }


    private void validateReplaceServiceChanged(List<DeploymentService> createServiceList,
                                               List<DeploymentService> removeServiceList,
                                               List<DeploymentService> updateInstanceList,
                                               final ReplaceServiceInfo newReplaceServiceInfo,
                                               final ReplaceServiceInfo replaceServiceInfo)
    {
        if (!Objects.equals(newReplaceServiceInfo.getHardwarePack(), replaceServiceInfo.getHardwarePack()) ||
                !areFilesystemsEquals(newReplaceServiceInfo.getFilesystems(), replaceServiceInfo.getFilesystems()) ||
                isChangeJVM(newReplaceServiceInfo, replaceServiceInfo) ||
                loggingConfigurationChanged(newReplaceServiceInfo, replaceServiceInfo) ||
                !this.validateCommonLogicalConnector(newReplaceServiceInfo.getLogicalConnectorList(), replaceServiceInfo.getLogicalConnectorList()) ||
                !this.validateApis(newReplaceServiceInfo.getDeploymentService().getService(), newReplaceServiceInfo.getDeploymentService().getService()))
        {
            createServiceList.add(newReplaceServiceInfo.getDeploymentService());
            removeServiceList.add(replaceServiceInfo.getDeploymentService());
        }
        else if (!Objects.equals(newReplaceServiceInfo.getNumberOfInstance(), replaceServiceInfo.getNumberOfInstance()))
        {
            log.debug("[DeploymentReplacePlanService] -> [validateReplaceServiceChanged]: add to update instance number: {}", newReplaceServiceInfo.getDeploymentService());
            updateInstanceList.add(newReplaceServiceInfo.getDeploymentService());
        }
    }

    private boolean isChangeJVM(final ReplaceServiceInfo newReplaceServiceInfo, final ReplaceServiceInfo replaceServiceInfo)
    {
        log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: validate if change some JVM parameter");
        if(!newReplaceServiceInfo.getMemoryFactor().equals(replaceServiceInfo.getMemoryFactor()))
        {
            log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: Change getMemoryFactor");
            return true;
        }
        List<JdkParameter> oldJdkParam = jdkParameterRepository.findByDeploymentService(replaceServiceInfo.getDeploymentService().getId());
        List<JdkParameter> newJdkParam = jdkParameterRepository.findByDeploymentService(newReplaceServiceInfo.getDeploymentService().getId());

        if ((oldJdkParam == null && newJdkParam !=null) || (oldJdkParam != null && newJdkParam ==null))
        {
            log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: One jdk parameter is null but not the other");
            return true;
        }
        if(oldJdkParam==null && newJdkParam==null)
        {
            log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: jdkParam are nulls in both releases");
            return false;
        }
        Set<JdkParameter> oldJdkParameterSet =new HashSet<>(oldJdkParam);
        Set<JdkParameter> newJdkParameterSet =new HashSet<>(newJdkParam);
        if(oldJdkParameterSet.size()!=newJdkParameterSet.size())
        {
            log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: jdkparameters size different betwen releaes");
            return true;
        }
        for(JdkParameter o:oldJdkParameterSet)
        {
            if(!newJdkParameterSet.contains(o))
            {
                log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: Jdkparameter [{}] in old options but not the new", o.getName());
                return true;
            }
        }
        log.info("[DeploymentReplacePlanService] -> [isChangeJVM]: No changes in JVM 11 params");
        return false;
    }

    private Map<Integer, Set<LMLibraryEnvironmentsByServiceDTO>> getUsedLibraries(final List<ReplaceServiceInfo> newReplaceServiceInfoList,
            List<ReplaceServiceInfo> oldReplaceServiceInfoList)
    {
        List<Integer> rvsServiceIdList = new ArrayList<>();
        rvsServiceIdList.addAll(newReplaceServiceInfoList.stream().map( l -> l.getDeploymentService().getService().getId()).collect(Collectors.toList()));
        rvsServiceIdList.addAll(oldReplaceServiceInfoList.stream().map( l -> l.getDeploymentService().getService().getId()).collect(Collectors.toList()));
        String usage = newReplaceServiceInfoList.stream().
                map( l -> l.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()).findFirst().get();
        List<LMLibraryEnvironmentsByServiceDTO> librariesList = libraryManagerService.getUsedLibrariesByServices(rvsServiceIdList, usage);

        if(log.isDebugEnabled())
        {
            log.debug( "[DeploymentReplacePlanService] -> [getUsedLibraries]: the used libraries for all the services in the replacement are: [{}]",
                    librariesList);
        }

        return librariesList.stream().collect(
                Collectors.groupingBy(
                        LMLibraryEnvironmentsByServiceDTO::getReleaseVersionServiceId,
                        Collectors.mapping(
                                Function.identity(),
                                Collectors.toSet()
                        )
                )
        );

    }

    /**
     * Checks if the service with serviceId is not using any library
     * @param serviceId serviceId
     * @param librariesMap librariesMap
     * @return true if the replaceServiceInfo is not using any library
     */
    private boolean isServiceLibrariesFree(final Integer serviceId,
            final Map<Integer, Set<LMLibraryEnvironmentsByServiceDTO>> librariesMap)
    {
        boolean isLibraryFree = !librariesMap.containsKey(serviceId);

        if(log.isDebugEnabled())
        {
            log.debug( "[DeploymentReplacePlanService] -> [isServiceLibrariesFree]: Check if the service with Id [{}] is not using any library: [{}]",
                    serviceId, isLibraryFree);
        }

        return isLibraryFree;
    }

    /**
     * Checks if the logging configuration has changed and the service should be replaced
     *
     * @param newReplaceServiceInfo new service to deploy
     * @param replaceServiceInfo    deployed service
     * @return true if the logging configuration changed
     */
    private boolean loggingConfigurationChanged(final ReplaceServiceInfo newReplaceServiceInfo, final ReplaceServiceInfo replaceServiceInfo)
    {
        final DeploymentPlan oldPlan = replaceServiceInfo.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan();
        final DeploymentPlan newPlan = newReplaceServiceInfo.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan();

        final Platform oldSelectedLogging = oldPlan.getSelectedLogging();
        final Platform newSelectedLogging = newPlan.getSelectedLogging();

        final String oldEtheLoggingNS = oldPlan.getEtherNs();
        final String newEtherLoggingNS = newPlan.getEtherNs();

        return
                // different selected logging
                oldSelectedLogging != newSelectedLogging
                        // same logging
                        || (
                        // selected logging -> Ether
                        PlatformUtils.isPlanLoggingInEther(newPlan)
                                // logging namespace has changed
                                && !StringUtils.equals(oldEtheLoggingNS, newEtherLoggingNS)
                );
    }

    /**
     * Whether the two DeploymentServiceFilesystem contain the same filesystems
     * @param deploymentServiceFilesystems1 First list
     * @param deploymentServiceFilesystems2 Second list
     * @return True if the two DeploymentServiceFilesystem contain the same filesystems, false otherwise
     */
    private boolean areFilesystemsEquals(List<DeploymentServiceFilesystem> deploymentServiceFilesystems1, List<DeploymentServiceFilesystem> deploymentServiceFilesystems2)
    {
        if (deploymentServiceFilesystems1.size() != deploymentServiceFilesystems2.size())
        {
            return false;
        }
        else
        {
            for (DeploymentServiceFilesystem deploymentServiceFilesystemIn1: deploymentServiceFilesystems1)
            {
                boolean result = false;
                for (DeploymentServiceFilesystem deploymentServiceFilesystemIn2: deploymentServiceFilesystems2)
                {
                    if (deploymentServiceFilesystemIn1.getFilesystem().getId().equals(deploymentServiceFilesystemIn2.getFilesystem().getId()) &&
                    deploymentServiceFilesystemIn1.getVolumeBind().equals(deploymentServiceFilesystemIn2.getVolumeBind()))
                    {
                        result = true;
                        break;
                    }
                }
                if (!result)
                {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Validate the logical connector for both release version service list
     *
     * @param newLogicalConnectorList newLogicalConnectorList
     * @param oldLogicalConnectorList oldLogicalConnectorList
     * @return true if have changes in other case false
     */
    private boolean validateCommonLogicalConnector(final List<LogicalConnector> newLogicalConnectorList, final List<LogicalConnector> oldLogicalConnectorList)
    {
        return newLogicalConnectorList.equals(oldLogicalConnectorList);
    }

    private boolean validateApis(final ReleaseVersionService newService, final ReleaseVersionService oldService)
    {
        return newService.getServers().equals(oldService.getServers()) && newService.getConsumers().equals(oldService.getConsumers());
    }

    private void validateReplaceServicePropertiesChanged(List<DeploymentService> restartServiceList,
                                                         final ReplaceServiceInfo newReplaceServiceInfo,
                                                         final ReplaceServiceInfo replaceServiceInfo)
    {
        if (newReplaceServiceInfo.getServicePropertyMap().hashCode() != replaceServiceInfo.getServicePropertyMap().hashCode())
        {
            restartServiceList.add(replaceServiceInfo.getDeploymentService());
        }
    }

    private List<ReplaceServiceInfo> getAllReplaceServiceInfo(final List<ReplacePlanQueryResult> replacePlanQueryResultList)
    {
        Map<String, String> allServicePropertyMap = new HashMap<>();
        List<ReplaceServiceInfo> replaceServiceInfoList = new ArrayList<>();
        String newServiceName = "";

        for (ReplacePlanQueryResult replacePlanQueryResult : replacePlanQueryResultList)
        {
            if (replacePlanQueryResult.getServiceName() != null && !"".equals(replacePlanQueryResult.getServiceName()))
            {
                if (newServiceName.equals(replacePlanQueryResult.getServiceName()))
                {
                    allServicePropertyMap.put(replacePlanQueryResult.getPropertyDefinitionName(), replacePlanQueryResult.getConfigurationValue());
                }
                else
                {
                    allServicePropertyMap = new HashMap<>();
                    ReplaceServiceInfo replaceServiceInfo = new ReplaceServiceInfo();

                    replaceServiceInfo.setDeploymentService(replacePlanQueryResult.getDeploymentService());
                    replaceServiceInfo.setFilesystems(replacePlanQueryResult.getFilesystems());
                    replaceServiceInfo.setHardwarePack(replacePlanQueryResult.getHardwarePack());
                    replaceServiceInfo.setLogicalConnectorList(replacePlanQueryResult.getLogicalConnectorList());
                    replaceServiceInfo.setNumberOfInstance(replacePlanQueryResult.getNumberOfInstance());
                    replaceServiceInfo.setServiceName(replacePlanQueryResult.getServiceName());
                    replaceServiceInfo.setServiceVersion(replacePlanQueryResult.getServiceVersion());
                    replaceServiceInfo.setMemoryFactor(replacePlanQueryResult.getMemoryFactor());

                    allServicePropertyMap.put(replacePlanQueryResult.getPropertyDefinitionName(), replacePlanQueryResult.getConfigurationValue());
                    replaceServiceInfo.setServicePropertyMap(allServicePropertyMap);

                    replaceServiceInfoList.add(replaceServiceInfo);
                    newServiceName = replacePlanQueryResult.getServiceName();
                }
            }
        }

        return replaceServiceInfoList;
    }

    /**
     * Build a replace plan query result object list from a deployment plan id.
     * --- Create a native query
     * --- Execute native query
     * --- Create the ReplacePlanQueryResult object from query result
     *
     * @param deploymentPlanId the deployment plan id
     * @return a ReplacePlanQueryResult object list
     */
    private List<ReplacePlanQueryResult> buildReplacePlanQueryResultList(final Integer deploymentPlanId)
    {
        // 1. Create the native SQL query
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT DS.ID, PD.NAME, CV.VALUE ")
                .append("FROM DEPLOYMENT_PLAN DP ")
                .append("INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID ")
                .append("INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID ")
                .append("INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID ")
                .append("LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID ")
                .append("LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID ")
                .append("LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID ")
                .append("WHERE DP.ID =")
                .append(deploymentPlanId)
                .append(" ORDER BY DS.ID ASC ");

        // 2. Create JPA Query from native query
        Query query = this.entityManager.createNativeQuery(queryBuilder.toString());

        // 3. Execute the SQL Query.
        List<Object[]> replacePlanQueryResultList = query.getResultList();

        //4. Build replace
        return this.buildReplacePlanQueryResultList(replacePlanQueryResultList);
    }

    /**
     * Build a ReplacePlanQueryResultList from query result
     *
     * @param queryResult the native query result
     * @return a buildReplacePlanQueryResult object List
     */
    private List<ReplacePlanQueryResult> buildReplacePlanQueryResultList(final List<Object[]> queryResult)
    {
        List<ReplacePlanQueryResult> replacePlanQueryResultList = new ArrayList<>();

        for (Object[] entry : queryResult)
        {
            DeploymentService deploymentService = this.deploymentServiceRepository.getOne((Integer) entry[0]);

            String propertyDefinitionName = "";
            String configurationValue = "";
            if (entry[1] != null && entry[2] != null)
            {
                propertyDefinitionName = entry[1].toString();
                configurationValue = entry[2].toString();
            }

            replacePlanQueryResultList.add(new ReplacePlanQueryResult(deploymentService, deploymentService.getService().getFinalName(), deploymentService.getService().getVersion(),
                    deploymentService.getHardwarePack(), deploymentService.getDeploymentServiceFilesystems(), propertyDefinitionName, configurationValue,
                    deploymentService.getNumberOfInstances(), deploymentService.getMemoryFactor()));
        }

        return replacePlanQueryResultList;
    }
}

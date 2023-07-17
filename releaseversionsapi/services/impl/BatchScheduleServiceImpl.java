package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.SchedulerManagerClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.schedulerparserlib.Parser;
import com.bbva.enoa.utils.schedulerparserlib.exceptions.SchedulerParserException;
import com.bbva.enoa.utils.schedulerparserlib.parameters.BuilderCreator;
import com.bbva.enoa.utils.schedulerparserlib.parameters.Parameters;
import com.bbva.enoa.utils.schedulerparserlib.processor.ParsedInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Batch schedule service implementation
 */
@Slf4j
@Service
public class BatchScheduleServiceImpl implements IBatchScheduleService
{
    /**
     * Scheduler Manager implementation
     */
    @Autowired
    private SchedulerManagerClientImpl schedulerManagerClient;

    //////////////////////////////////////// IMPLEMENTATION ////////////////////////////////////////

    @Override
    @Transactional(readOnly = true)
    public void addBatchSchedulServices(ReleaseVersion releaseVersion) throws NovaException
    {
        //[1] Check the all Release Version Services and get all services type = BATCH SCHEDULER and if it exists, get all the BATCH services
        List<ReleaseVersionService> batchSchedulerServiceList;
        List<ReleaseVersionService> allBatchServiceList;

        //  [1.1] Find if there is some BATCH SCHEDULER service
        batchSchedulerServiceList = releaseVersion.getSubsystems()
                .stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(versionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(versionService.getServiceType()))
                .collect(Collectors.toList());
        log.debug("[{}] -> [buildBatchSchedulerService]: the batch scheduler service list is: [{}].", Constants.RELEASE_VERSIONS_API_NAME, batchSchedulerServiceList);

        //  [1.2] If there is Batch Scheduler service, get all BATCH service for each subsystem. If not, continue next method
        if (batchSchedulerServiceList.isEmpty())
        {
            log.debug("[{}] -> [buildBatchSchedulerService]: There are not any Batch scheduler service. Continue.", Constants.RELEASE_VERSIONS_API_NAME);
        }
        else
        {
            // Get all BATCH service for each subsystem
            allBatchServiceList = releaseVersion.getSubsystems()
                    .stream()
                    .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                    .filter(versionService -> ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType().equals(versionService.getServiceType())
                            || ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK.getServiceType().equals(versionService.getServiceType())
                            || ServiceType.NOVA_BATCH.getServiceType().equals(versionService.getServiceType())
                            || ServiceType.NOVA_SPRING_BATCH.getServiceType().equals(versionService.getServiceType())
                            || ServiceType.BATCH_PYTHON.getServiceType().equals(versionService.getServiceType()))
                    .collect(Collectors.toList());
            log.debug("[{}] -> [buildBatchSchedulerService]: all the batch service from all subsystem are: [{}].", Constants.RELEASE_VERSIONS_API_NAME, allBatchServiceList);

            // [2] Check that there are not batch name serviec duplicated and return a bath name map
            Map<String, ReleaseVersionService> allBatchServicesNameMap = this.buildAllBatchNameServicesMap(allBatchServiceList);

            // For each Batch Scheduler service, get the BATCH service names from scheduler.yml
            batchSchedulerServiceList.forEach(batchSchedulerService ->
            {
                String schedulerYmlFileString = this.getSchedulerYmlStringFile(batchSchedulerService);
                if (Strings.isNullOrEmpty(schedulerYmlFileString))
                {
                    String errorMessage = "[ReleaseVersionsAPI] -> [buildBatchSchedulerService]: the scheduler yml is empty or bad formed";
                    log.error(errorMessage);
                    throw new NovaException(ReleaseVersionError.getNoSuchSchedulerYmlError(), errorMessage);
                }
                else
                {
                    // [3] Get the batch service names from scheduler.yml
                    List<String> batchNamesList = this.buildBatchNameList(schedulerYmlFileString, batchSchedulerService.getServiceName());

                    // [4] Check that batch service name list are contained into the All Batch Service list. Save all the batch id and batch name in a map
                    Map<Integer, String> batchIdServiceNameMap = this.buildBatchIdServiceNameMap(allBatchServicesNameMap, batchNamesList);

                    // [5] Call scheduler manager client to save the Batch scheduler
                    this.schedulerManagerClient.saveBatchSchedulerService(batchSchedulerService.getId(), schedulerYmlFileString.getBytes(), batchSchedulerService.getServiceName(),
                            batchSchedulerService.getNovaYml().getContents(), batchIdServiceNameMap);
                }
            });
        }
    }

    @Override
    public void deleteBatchScheduleServices(final ReleaseVersion releaseVersion) throws NovaException
    {
        releaseVersion.getSubsystems()
                .stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices()
                        .stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.removeBatchSchedulerService(releaseVersionService.getId()));
    }

    @Override
    public DeploymentBatchScheduleDTO getDeploymentBatchSchedule(final Integer releaseVersionServiceId, final Integer deploymentPlanId)
    {
        return this.schedulerManagerClient.getDeploymentBatchSchedule(releaseVersionServiceId, deploymentPlanId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getSchedulerYmlStringFile(final ReleaseVersionService batchSchedulerService) throws NovaException
    {
        LobFile schedulerYmlLobFile = batchSchedulerService.getProjectDefinitionFile();

        if (schedulerYmlLobFile == null)
        {
            String errorMessage = "[ReleaseVersionsAPI] -> [buildBatchSchedulerService]: the batch scheduler: [" + batchSchedulerService.getServiceName() + "] " +
                    "does not have scheduler.yml or does not found.";
            log.error(errorMessage);
            throw new NovaException(ReleaseVersionError.getNoSuchSchedulerYmlError(), errorMessage);
        }

        return schedulerYmlLobFile.getContents();
    }

    @Override
    public List<String> buildBatchNameList(final String schedulerYmlFileString, final String batchSchedulerServiceName) throws NovaException
    {
        List<String> batchNamesList = new ArrayList<>();

        try
        {
            // Parse scheduler yml file to JAVA Object (Parsed Info)
            Parameters parameters = BuilderCreator.newParametersBuilder().setFileContentWithExtension(schedulerYmlFileString, "yml").build();
            ParsedInfo parsedInfo = new Parser(null, parameters).parse();

            //[3.1] Get the BATCH names from scheduler.yml. It means, for each com.bbva.enoa.platformservices.historicalloaderservice.step, get the job type = batch, and in this case, get the batch name
            parsedInfo.getSteps()
                    .getStepsMap()
                    .entrySet()
                    .stream()
                    .flatMap(entry -> entry.getValue().getJobs().stream())
                    .forEachOrdered(job ->
                    {
                        if (job.getType().equalsIgnoreCase("batch"))
                        {
                            batchNamesList.add(job.getServiceName());
                        }
                    });
        }
        catch (SchedulerParserException e)
        {
            String errorMessage = "[ReleaseVersionsAPI] -> [buildBatchNameList]: the scheduler.yml from batch scheduler service name " +
                    "[" + batchSchedulerServiceName + "] is not valid or contains errors.";
            log.error(errorMessage);
            throw new NovaException(ReleaseVersionError.getSchedulerYmlError(), errorMessage);
        }

        log.debug("[{}] -> [buildBatchNameList]: the batch name list from scheduler yml is: [{}].", Constants.RELEASE_VERSIONS_API_NAME, batchNamesList);
        return batchNamesList;
    }

    ///////////////////////////////////////////   PRIVATE METHOD  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Build a map of the batch services. The map is built with the batch name of a provided batch name list. The batch name must be contained into the all batch service name map
     * Map: integer = release version service id of the batch
     * string = release version service name (batch name)
     *
     * @param allBatchServicesNameMap list with all the batch services of the subsystem
     * @param batchNamesList          the batch name list
     * @return a map order by id release version service of the batch names
     * @throws NovaException NovaException if error
     */
    private Map<Integer, String> buildBatchIdServiceNameMap(final Map<String, ReleaseVersionService> allBatchServicesNameMap, final List<String> batchNamesList) throws NovaException
    {
        Map<Integer, String> batchIdServiceNameMap = new ConcurrentHashMap<>();

        batchNamesList.forEach(batchName ->
        {
            ReleaseVersionService releaseVersionServiceBatch = allBatchServicesNameMap.get(batchName);
            if (releaseVersionServiceBatch == null)
            {
                String errorMessage = "[ReleaseVersionsAPI] -> [buildBatchIdServiceNameMap]: the batch service name: [" + batchName + "] " +
                        "from scheduler.yml does not found in any subsystem of the release version";
                log.error(errorMessage);
                throw new NovaException(ReleaseVersionError.getBatchServiceNotFoundError(), errorMessage);
            }
            else
            {
                batchIdServiceNameMap.put(releaseVersionServiceBatch.getId(), releaseVersionServiceBatch.getServiceName());
            }
        });

        log.debug("[{}] -> [buildBatchIdServiceNameMap]: the batch id and service name map is: [{}].", Constants.RELEASE_VERSIONS_API_NAME, batchIdServiceNameMap);
        return batchIdServiceNameMap;
    }

    /**
     * Build a map with all the Batch services contained into the subsystem
     * Map: string - batch name
     * ReleaseVersionService - Batch service associated to the name
     *
     * @param allBatchServiceList the batch service list
     * @return a map with all batch of the service
     * @throws NovaException NovaException if error
     */
    private Map<String, ReleaseVersionService> buildAllBatchNameServicesMap(List<ReleaseVersionService> allBatchServiceList) throws NovaException
    {
        // [2] Review if there are duplicated BATCH service name. In Batch scheduler service, is not allow duplicated batch service name.
        // Save all the bath into a map.<String(serviceName), ReleaseVersionService>
        Map<String, ReleaseVersionService> allBatchServicesNameMap = new ConcurrentHashMap<>();
        allBatchServiceList.forEach(releaseVersionService ->
        {
            if (allBatchServicesNameMap.containsKey(releaseVersionService.getServiceName()))
            {
                String errorMessage = "[ReleaseVersionsAPI] -> [buildBatchSchedulerService]: the BATCH service with name: [" + releaseVersionService.getServiceName() + "] is duplicated. In Batch Scheduler is not" +
                        "allow duplicated batch service names.";
                log.error(errorMessage);
                throw new NovaException(ReleaseVersionError.getBatchServiceDuplicatedError(), errorMessage);
            }
            else
            {
                allBatchServicesNameMap.putIfAbsent(releaseVersionService.getServiceName(), releaseVersionService);
            }
        });

        log.debug("[{}] -> [buildAllBatchNameServicesMap]: all the batch service name - release version service map is: [{}].", Constants.RELEASE_VERSIONS_API_NAME, allBatchServicesNameMap);
        return allBatchServicesNameMap;
    }
}

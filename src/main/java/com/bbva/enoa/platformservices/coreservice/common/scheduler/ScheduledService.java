package com.bbva.enoa.platformservices.coreservice.common.scheduler;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRepositoryManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionService;
import com.bbva.enoa.utils.logutils.annotations.TraceBackgroundProcess;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Component to schedule periodic task for Scheduler manager
 */
@Slf4j
@Component
public class ScheduledService
{
    /**
     * Name for the deployment plan storage cleaner task
     */
    private static final String DEPLOYMENT_PLAN_STORAGE_CLEANER = "DeploymentPlanStorageCleaner";

    /**
     * Name for the release versions storage cleaner task
     */
    private static final String RELEASE_VERSION_STORAGE_CLEANER = "ReleaseVersionStorageCleaner";

    /**
     * Name for the batch deployment instance task
     */
    private static final String BATCH_DEPLOYMENT_INSTANCE_CLEANER = "BatchDeploymentInstanceCleaner";

    /**
     * Name for the deployment plan change storage cleaner task
     */
    private static final String DEPLOYMENT_PLAN_CHANGE_STORAGE_CLEANER = "DeploymentPlanChangeStorageCleaner";

    /**
     * Name for the deployment plan storage cleaner task
     */
    private static final String STORAGE_UNDEPLOYED_PLAN_TASK = "StorageUndeployedPlan";

    private static final String SERVICE_EXECUTION_HISTORY_CLEANER = "ServiceExecutionHistoryCleaner";

    /**
     * Repository Manager
     */
    @Autowired
    private IRepositoryManagerService repositoryManagerService;

    /**
     * Deployments Service
     */
    @Autowired
    private IDeploymentsService deploymentsService;

    /**
     * Release version service
     */
    @Autowired
    private IReleaseVersionService releaseVersionService;

    /**
     * Deployment plan repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;


    /**
     * Deployment plan change repository
     */
    @Autowired
    private DeploymentChangeRepository deploymentChangeRepository;

    @Autowired
    private ServiceExecutionHistoryRepository serviceExecutionHistoryRepository;

    @Autowired
    private DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Max days for storage a deployment plans
     */
    @Value("${nova.storageDays.deploymentPlanInt: 30.0}")
    private long storageDaysDeploymentPlanInt;

    /**
     * Max days for storage a deployment plans
     */
    @Value("${nova.storageDays.deploymentPlanPre: 30.0}")
    private long storageDaysDeploymentPlanPre;

    /**
     * Max days for storage a deployment plans
     */
    @Value("${nova.storageDays.deploymentPlanPro: 60.0}")
    private long storageDaysDeploymentPlanPro;

    /**
     * Release version days saved
     */
    @Value("${nova.storageDays.releaseVersionStorage: 60.0}")
    private long releaseVersionStorageDaysSaved;

    /**
     * Deployment plan change days saved
     */
    @Value("${nova.storageDays.deploymentPlanChange: 90.0}")
    private long deploymentPlanChangeStorageDaysSaved;

    /**
     * Deployment plan change days saved
     */
    @Value("${nova.storageDays.undeployedPlanStorage: 60.0}")
    private long undeployedPlanDaysSaved;

    /**
     * Deployment batch instance days saved
     */
    @Value("${nova.storageDays.deploymentBatchInstance: 35.0}")
    private int deploymentBatchInstanceStorageDaysSaved;

    @Value("${nova.storageDays.serviceExecutionHistory: 120}")
    private int serviceExecutionHistoryDaysSaved;

    /**
     * Delete all deployment plan created before the purge days
     * Only clean if the status of deployment plan is STORAGE
     * By default, launched each 24 hours
     */
    @Scheduled(cron = "${nova.scheduledTasksCron.deploymentPlanCron:0 0 2 * * ?}")
    @SchedulerLock(name = DEPLOYMENT_PLAN_STORAGE_CLEANER, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    public void cleanStorageDeploymentPlan()
    {
        log.info("[ScheduledService] -> [cleanStorageDeploymentPlan]: beginning of Scheduler task with name: [{}]", DEPLOYMENT_PLAN_STORAGE_CLEANER);

        log.debug("[ScheduledService] -> [cleanStorageDeploymentPlan]: Scheduler -> starting to clean storage deployment plan INT before: [{}] - deployment plan PRE before: [{}] and deployment plan PRO before: [{}]",
                this.storageDaysDeploymentPlanInt, this.storageDaysDeploymentPlanPre, this.storageDaysDeploymentPlanPro);

        // Get in milliseconds, the resolved to do task days back limit
        long purgeTimeStorageDeploymentPlanInt = System.currentTimeMillis() - (this.storageDaysDeploymentPlanInt * 24L * 60L * 60L * 1000L);
        Date daysBeforeDeleteDeploymentPlanInt = new Date(purgeTimeStorageDeploymentPlanInt);
        log.info("[ScheduledService] -> [cleanStorageDeploymentPlan]: days back to storage the deployment plan INT: [{}]", daysBeforeDeleteDeploymentPlanInt);

        // Delete to do task resolved except deployment task
        this.deleteOldStorageDeploymentPlan(daysBeforeDeleteDeploymentPlanInt, Environment.INT);

        // Get in milliseconds, the resolved to do task days back limit
        long purgeTimeStorageDeploymentPlanPre = System.currentTimeMillis() - (this.storageDaysDeploymentPlanPre * 24L * 60L * 60L * 1000L);
        Date daysBeforeDeleteDeploymentPlanPre = new Date(purgeTimeStorageDeploymentPlanPre);
        log.info("[ScheduledService] -> [cleanStorageDeploymentPlan]: days back to storage the deployment plan PRE: [{}]", daysBeforeDeleteDeploymentPlanPre);

        // Delete to do task resolved except deployment task
        this.deleteOldStorageDeploymentPlan(daysBeforeDeleteDeploymentPlanPre, Environment.PRE);

        // Get in milliseconds, the resolved to do task days back limit
        long purgeTimeStorageDeploymentPlanPro = System.currentTimeMillis() - (this.storageDaysDeploymentPlanPro * 24L * 60L * 60L * 1000L);
        Date daysBeforeDeleteDeploymentPlanPro = new Date(purgeTimeStorageDeploymentPlanPro);
        log.info("[ScheduledService] -> [cleanStorageDeploymentPlan]: days back to storage the deployment plan PRO: [{}]", daysBeforeDeleteDeploymentPlanPro);

        // Delete to do task resolved except deployment task
        this.deleteOldStorageDeploymentPlan(daysBeforeDeleteDeploymentPlanPro, Environment.PRO);

        log.info("[ScheduledService] -> [cleanStorageDeploymentPlan]: finished of Scheduler task with name: [{}]", DEPLOYMENT_PLAN_STORAGE_CLEANER);
    }

    /**
     * Delete all release versions created before the purge days
     * Only clean if the status of release version is on STORAGE status
     * By default, launched each 24 hours
     */
    @Scheduled(cron = "${nova.scheduledTasksCron.releaseVersionCron:0 0 3 * * ?}")
    @SchedulerLock(name = RELEASE_VERSION_STORAGE_CLEANER, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    public void cleanStorageReleaseVersion()
    {
        log.info("[ScheduledService] -> [cleanStorageReleaseVersion]: beginning of Scheduler task with name: [{}]", RELEASE_VERSION_STORAGE_CLEANER);

        //  delete all nova batch schedule instances
        this.deleteOldReleaseVersions();

        log.info("[ScheduledService] -> [cleanStorageReleaseVersion]: finished of Scheduler task with name: [{}]", RELEASE_VERSION_STORAGE_CLEANER);
    }

    /**
     * Delete all deployment instance of batch service before the purge days
     * By default, launched each 24 hours
     */
    @Scheduled(cron = "${nova.scheduledTasksCron.batchInstanceCron:0 0 4 * * ?}")
    @SchedulerLock(name = BATCH_DEPLOYMENT_INSTANCE_CLEANER, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    public void cleanBatchDeploymentInstance()
    {
        log.info("[ScheduledService] -> [cleanBatchDeploymentInstance]: beginning of Scheduler task with name: [{}]", BATCH_DEPLOYMENT_INSTANCE_CLEANER);

        //  delete all nova batch deployment instances
        this.deleteOldBatchDeploymentInstances();

        log.info("[ScheduledService] -> [cleanBatchDeploymentInstance]: finished of Scheduler task with name: [{}]", BATCH_DEPLOYMENT_INSTANCE_CLEANER);
    }

    /**
     * Delete all deployment plan changes of deployment plan on status DEPLOYED
     * By default, launched each 24 hours
     */
    @Scheduled(cron = "${nova.scheduledTasksCron.deploymentPlanChangeCron:0 0 5 * * ?}")
    @SchedulerLock(name = DEPLOYMENT_PLAN_CHANGE_STORAGE_CLEANER, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    @Transactional
    public void cleanDeploymentPlanChanges()
    {
        log.info("[ScheduledService] -> [cleanDeploymentPlanChanges]: beginning of Scheduler task with name: [{}]", DEPLOYMENT_PLAN_CHANGE_STORAGE_CLEANER);

        // Delete old deployment plan change
        this.deleteOldDeploymentPlanChange();

        log.info("[ScheduledService] -> [cleanDeploymentPlanChanges]: finished of Scheduler task with name: [{}]", DEPLOYMENT_PLAN_CHANGE_STORAGE_CLEANER);
    }

    /**
     * Storage all undeployed plan on status UNDEPLOYED before limit time
     * By default, launched each 24 hours
     */
    @Scheduled(cron = "${nova.scheduledTasksCron.undeployedPlanCron:0 0 6 * * ?}")
    @SchedulerLock(name = STORAGE_UNDEPLOYED_PLAN_TASK, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    public void storageUndeployedPlan()
    {
        log.info("[ScheduledService] -> [storageUndeployedPlan]: beginning of Scheduler task with name: [{}]", STORAGE_UNDEPLOYED_PLAN_TASK);

        // Get in milliseconds, the deployment plan changes days saved limit
        Date daysBefore = new Date(System.currentTimeMillis() - (this.undeployedPlanDaysSaved * 24L * 60L * 60L * 1000L));
        log.info("[ScheduledService] -> [storageOldUndeploymedPlan]: storage deployment plans before undeploy day : [{}] from historical days saved: [{}]", daysBefore, this.undeployedPlanDaysSaved);

        // Storage old deployment plan for INT
        this.storageOldUndeploymedPlan(Environment.INT, daysBefore);
        // Storage old deployment plan for PRE
        this.storageOldUndeploymedPlan(Environment.PRE, daysBefore);
        // Storage old deployment plan for PRO
        this.storageOldUndeploymedPlan(Environment.PRO, daysBefore);

        log.info("[ScheduledService] -> [storageUndeployedPlan]: finished of Scheduler task with name: [{}]", STORAGE_UNDEPLOYED_PLAN_TASK);
    }

    @Scheduled(cron = "${nova.scheduledTasksCron.cleanServiceExecutionHistoryCron:0 30 5 * * ?}")
    @SchedulerLock(name = SERVICE_EXECUTION_HISTORY_CLEANER, lockAtMostFor = "25h", lockAtLeastFor = "23h")
    @TraceBackgroundProcess
    public void cleanServiceExecutionHistory()
    {
        log.info("[ScheduledService] -> [storageUndeployedPlan]: beginning of Scheduler task with name: [{}]", SERVICE_EXECUTION_HISTORY_CLEANER);
        List<ServiceExecutionHistory> oldExecutions = getOldExecutions();
        if (existOldExecutions(oldExecutions))
        {
            List<Integer> oldExecutionsDeploymentServiceIds = getOldExecutionsDeploymentServiceIds(oldExecutions);
            List<ServiceExecutionHistory> remainingOldExecutions = oldExecutions;
            if (!oldExecutionsDeploymentServiceIds.isEmpty())
            {
                log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: getting still running service executions.");
                List<ServiceExecutionHistory> stillRunningServices = serviceExecutionHistoryRepository.findStillRunningServices(oldExecutionsDeploymentServiceIds, Boolean.TRUE);
                if (stillRunningServices.isEmpty())
                {
                    log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: no running service executions have been found.");
                }
                else
                {
                    log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: popping still running service executions.");
                    remainingOldExecutions = oldExecutions.stream().filter(i -> !stillRunningServices.contains(i)).collect(Collectors.toList());
                    log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: updating last execution date for still running service executions.");
                    updateStillRunningServicesLastModified(stillRunningServices);
                }
            }
            if (existOldExecutions(remainingOldExecutions))
            {
                log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: deleting from database.");
                serviceExecutionHistoryRepository.deleteAll(remainingOldExecutions);
            }
        }
        log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: finished of Scheduler task with name: [{}]", SERVICE_EXECUTION_HISTORY_CLEANER);
    }

    /////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////

    private boolean existOldExecutions(List<ServiceExecutionHistory> oldExecutions)
    {
        if (oldExecutions.isEmpty())
        {
            log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: no service executions found to be cleaned. Finishing task.");
            return false;
        }
        return true;
    }

    private List<ServiceExecutionHistory> getOldExecutions()
    {
        log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: beginning of Scheduler task with name: [{}]", SERVICE_EXECUTION_HISTORY_CLEANER);
        final LocalDateTime now = LocalDate.now().atStartOfDay();
        final LocalDateTime startDateTime = now.minus(serviceExecutionHistoryDaysSaved, ChronoUnit.DAYS);
        final long startDateMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startDateMillis);
        log.info("[ScheduledService] -> [cleanServiceExecutionHistory]: getting service executions to be deleted from database.");
        return serviceExecutionHistoryRepository.findByLastExecutionBefore(startDate);
    }

    private List<Integer> getOldExecutionsDeploymentServiceIds(final List<ServiceExecutionHistory> oldExecutions)
    {
        List<Integer> oldExecutionsDeploymentServiceIds = new ArrayList<>();
        for (ServiceExecutionHistory oldExecution : oldExecutions)
        {
            Integer deploymentServiceId = oldExecution.getDeploymentServiceId();
            if (deploymentServiceId != null)
            {
                oldExecutionsDeploymentServiceIds.add(deploymentServiceId);
            }
        }
        return oldExecutionsDeploymentServiceIds;
    }

    private void updateStillRunningServicesLastModified(List<ServiceExecutionHistory> stillRunningServices)
    {
        Calendar now = Calendar.getInstance();
        for (ServiceExecutionHistory stillRunningService : stillRunningServices)
        {
            stillRunningService.setLastExecution(now);
        }
        serviceExecutionHistoryRepository.saveAll(stillRunningServices);
    }

    /**
     * Storage old undeployed plan on status UNDEPLOYED
     */
    private void storageOldUndeploymedPlan(final Environment environment, final Date purgeTime)
    {
        // Storage all old undeployed plans before limit time
        List<DeploymentPlan> deploymentPlanList = this.repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(DeploymentStatus.UNDEPLOYED, environment);

        deploymentPlanList.stream().filter(deploymentPlan -> checkUndeploymentDate(deploymentPlan, purgeTime)).forEach(deploymentPlan ->
        {
            try
            {
                log.debug("[ScheduledService] -> [storageOldUndeploymedPlan]: archiving old deploymentPlan id: [{}] older than purge time: [{}] - environment: [{}]", deploymentPlan.getId(), purgeTime, environment);
                this.deploymentsService.archivePlan(deploymentPlan.getId());
                log.debug("[ScheduledService] -> [storageOldUndeploymedPlan]: archived old deploymentPlan id: [{}] older than purge time: [{}] - environment: [{}]", deploymentPlan.getId(), purgeTime, environment);
            }
            catch (Exception exception)
            {
                log.warn("[ScheduledService] -> [storageOldUndeploymedPlan]: the deployment plan id: [{}] - environment: [{}] cannot be archived due to: [{}]", deploymentPlan.getId(), environment.name(), exception.getMessage());
            }
        });
    }

    /**
     * Method to check if the UndeploymentDate is before purge time. If UndeploymentDate is null, it returns false
     *
     * @param deploymentPlan The deployment plan to check
     * @param purgeTime      The purge time
     * @return true if the UndeploymentDate is before purge time
     */
    private static boolean checkUndeploymentDate(final DeploymentPlan deploymentPlan, final Date purgeTime)
    {
        boolean result = false;
        if (deploymentPlan.getUndeploymentDate() == null)
        {
            log.warn("[ScheduledService] -> [checkUndeploymentDate]: DeploymentPlan id: [{}] has NULL UndeploymentDate", deploymentPlan.getId());
        }
        else
        {
            result = deploymentPlan.getUndeploymentDate().getTime().before(purgeTime);
        }

        return result;
    }

    /**
     * Delete old deployment plan change od deployment plan on status DEPLOYED
     */
    private void deleteOldDeploymentPlanChange()
    {
        // Get in milliseconds, the deployment plan changes days saved limit
        Date daysBefore = new Date(System.currentTimeMillis() - (this.deploymentPlanChangeStorageDaysSaved * 24L * 60L * 60L * 1000L));
        log.info("[ScheduledService] -> [deleteOldDeploymentPlanChange]: days before calculated: [{}] from historical days saved: [{}]", daysBefore, this.deploymentPlanChangeStorageDaysSaved);

        // Delete all old deployment changes before limit time
        this.deploymentChangeRepository.getDeploymentChangeByDeploymentPlanStatus(DeploymentStatus.DEPLOYED).stream()
                .filter(deploymentChange -> deploymentChange.getCreationDate().getTime().before(daysBefore))
                .forEach(deploymentChange -> this.deploymentChangeRepository.delete(deploymentChange));
    }

    /**
     * Delete old batch deployment instance
     */
    private void deleteOldBatchDeploymentInstances()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -this.deploymentBatchInstanceStorageDaysSaved);
        log.info("[ScheduledService] -> [deleteOldBatchDeploymentInstances]: days before calculated: [{}] from historical days saved: [{}]", calendar, this.deploymentBatchInstanceStorageDaysSaved);

        List<ServiceType> batchesServiceTypes = Arrays.stream(ServiceType.values()).filter(ServiceType::isBatch).collect(Collectors.toList());

        // Get all deployment instance of batch service type and before deployment batch instance storage days saved
        List<DeploymentInstance> deploymentInstanceList = this.repositoryManagerService.findBatchDeploymentInstance(calendar, batchesServiceTypes);

        for (DeploymentInstance deploymentInstance : deploymentInstanceList)
        {
            log.debug("[ScheduledService] -> [deleteOldBatchDeploymentInstances]: deleting old batch deployment instance: [{}] ", deploymentInstance);
            this.repositoryManagerService.deleteDeploymentInstance(deploymentInstance);
            log.debug("[ScheduledService] -> [deleteOldBatchDeploymentInstances]: deleted old batch deployment instance: [{}] ", deploymentInstance);
        }
    }

    /**
     * Delete old release version storage
     */
    private void deleteOldReleaseVersions()
    {
        // Get in milliseconds, the release version storage days saved limit
        Date daysBefore = new Date(System.currentTimeMillis() - (this.releaseVersionStorageDaysSaved * 24L * 60L * 60L * 1000L));
        log.info("[ScheduledService] -> [deleteOldReleaseVersions]: days before calculated: [{}] from historical days saved: [{}]", daysBefore, this.releaseVersionStorageDaysSaved);

        // Get all release version storage
        List<ReleaseVersion> releaseVersionList = this.repositoryManagerService.findReleaseVersionByStatus(ReleaseVersionStatus.STORAGED);

        releaseVersionList.stream().filter(releaseVersion -> releaseVersion.getCreationDate().getTime().before(daysBefore)).forEach((releaseVersion ->
        {
            try
            {
                log.debug("[ScheduledService] -> [deleteOldReleaseVersions]: deleting old release version id: [{}] older than purge time: [{}]", releaseVersion.getId(), daysBefore);
                this.releaseVersionService.deleteReleaseVersion(Constants.IMMUSER, releaseVersion.getId());
                log.debug("[ScheduledService] -> [deleteOldReleaseVersions]: deleted old release version id: [{}] older than purge time: [{}]", releaseVersion.getId(), daysBefore);
            }
            catch (Exception e)
            {
                log.warn("[ScheduledService] -> [deleteOldReleaseVersions]: the release version id: [{}] cannot be deleted due to: [{}]", releaseVersion.getId(), e.getMessage());
            }
        }));
    }

    /**
     * Delete all deployment plan storage from undeployed date, before purge time in the environment provided in STORAGE status
     *
     * @param purgeTime   purge time
     * @param environment the environment
     */
    private void deleteOldStorageDeploymentPlan(final Date purgeTime, final Environment environment)
    {
        List<DeploymentPlan> deploymentPlanList = this.repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(DeploymentStatus.STORAGED, environment);

        deploymentPlanList.stream().filter(deploymentPlan -> deploymentPlan.getUndeploymentDate().getTime().before(purgeTime)).forEach(deploymentPlan ->
        {
            try
            {
                log.debug("[ScheduledService] -> [deleteOldStorageDeploymentPlan]: deleting old deploymentPlan id: [{}] older than purge time: [{}] - environment: [{}]", deploymentPlan.getId(), purgeTime, environment);
                this.deploymentsService.deletePlan(deploymentPlan.getId());
                log.debug("[ScheduledService] -> [deleteOldStorageDeploymentPlan]: deleted old deploymentPlan id: [{}] older than purge time: [{}] - environment: [{}]", deploymentPlan.getId(), purgeTime, environment);
            }
            catch (Exception exception)
            {
                log.warn("[ScheduledService] -> [deleteOldStorageDeploymentPlan]: the deployment plan id: [{}] - environment: [{}] cannot be deleted due to: [{}]", deploymentPlan.getId(), environment.name(), exception.getMessage());
            }
        });
    }
}

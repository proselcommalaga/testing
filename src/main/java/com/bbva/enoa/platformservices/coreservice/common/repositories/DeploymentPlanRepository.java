package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The interface Deployment plan repository.
 *
 * @author xe30000
 */
@Transactional(readOnly = true)
public interface DeploymentPlanRepository extends JpaRepository<DeploymentPlan, Integer>
{
    /**
     * Gets all the deployments plans from a product on an environment. EXCEPT Storaged Plans
     * Plans could be deployed or not.
     *
     * @param productId   Product ID.
     * @param environment Environment such as INT, PRE and PRO.
     * @return List of deployment plans.
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "    dp.environment = :environment " +
                    " and dp.releaseVersion.release.product.id = :productId " +
                    " and dp.status != com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.STORAGED " +
                    " order by dp.creationDate desc "
    )
    List<DeploymentPlan> getByProductAndEnvironment(
            @Param("productId") int productId,
            @Param("environment") String environment);

    /**
     * Get if a release version has deployment plans not storaged
     *
     * @param versionId {@link ReleaseVersion} ID.
     * @return true if has deployment plan not storaged, false if not
     */
    @Query(
            "select case when count ( dp.id ) > 0 then true else false end " +
                    "from DeploymentPlan dp " +
                    "where " +
                    "    dp.releaseVersion.id = :versionId " +
                    "and dp.status != com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.STORAGED"
    )
    boolean releaseVersionHasPlanNotStorage(@Param("versionId") int versionId);

    /**
     * Checks if a {@link ReleaseVersion} can be storaged
     * by checking if it has any relations to tasks or
     * configurations.
     *
     * @param versionId {@link ReleaseVersion} ID.
     * @return True if version had any deployment on any environment, false if not.
     */
    @Query(
            "select case when count ( dp.id ) > 0 then true else false end " +
                    "from DeploymentPlan dp " +
                    "where " +
                    "    dp.releaseVersion.id = :versionId " +
                    "and " +
                    "    dp.configurationTask is not null " +
                    "and " +
                    "    (dp.configurationTask.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING " +
                    "or   dp.configurationTask.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR)"
    )
    boolean releaseVersionHasTasks(@Param("versionId") int versionId);


    /**
     * Gets only the deployments plans from a product on an environment.
     * that have the given status.
     *
     * @param productId   Product ID.
     * @param environment Environment such as INT, PRE and PRO.
     * @param status      Deployment status such as DEPLOYED or PENDING_TASKS among others.
     * @return List of deployment plans.
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "    dp.environment = :environment " +
                    "and dp.releaseVersion.release.product.id = :productId " +
                    "and dp.status = :status " +
                    "order by dp.creationDate desc "
    )
    List<DeploymentPlan> getByProductAndEnvironmentAndStatus(
            @Param("productId") int productId,
            @Param("environment") String environment,
            @Param("status") DeploymentStatus status);

    /**
     * Gets only the deployments plans from on environment and that have the given status.
     * Condition: the undeployment date can not be null
     *
     * @param status      Deployment status
     * @param environment Environment such as INT, PRE and PRO.
     * @return List of deployment plans.
     */
    List<DeploymentPlan> findByStatusAndEnvironmentAndUndeploymentDateNotNull(DeploymentStatus status, String environment);


    /**
     * Get plans with the given environment, status and platform
     *
     * @param environment    environment
     * @param status         status
     * @param selectedDeploy platform where deployment plan has been deployed
     * @return list of deployment plans
     */
    List<DeploymentPlan> findByEnvironmentAndStatusAndSelectedDeploy(
            @Param("environment") String environment,
            @Param("status") DeploymentStatus status,
            @Param("selectedDeploy") Platform selectedDeploy);

    /**
     * Get plans with the given status and platform
     *
     * @param status         status
     * @param selectedDeploy platform where deployment plan has been deployed
     * @return list of deployment plans
     */
    List<DeploymentPlan> findByStatusAndSelectedDeploy(
            @Param("status") DeploymentStatus status,
            @Param("selectedDeploy") Platform selectedDeploy);

    /**
     * Gets deployment plans from a product on several environments and statuses with pagination
     *
     * @param productId    The ID of the product
     * @param environments list of environments
     * @param statuses     list of statuses
     * @param pageable     paging criteria
     * @return A page of query results
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "    dp.releaseVersion.release.product.id = :productId " +
                    "and (coalesce(:environments, null) is null or dp.environment in (:environments)) " +
                    "and (coalesce(:statuses, null) is null or dp.status in (:statuses)) " +
                    "order by dp.environment asc, dp.creationDate desc "
    )
    Page<DeploymentPlan> getByProductAndEnvironmentsAndStatuses(
            @Param("productId") int productId,
            @Param("environments") List<String> environments,
            @Param("statuses") List<DeploymentStatus> statuses,
            Pageable pageable);

    /**
     * Get by product and environment and status
     *
     * @param productId   product id
     * @param environment environment
     * @param startDate   start date
     * @param endDate     end date
     * @return deployment plans
     */
    @Query("select dp from DeploymentPlan dp " + "where " + "    dp.environment = :environment "
            + "and dp.releaseVersion.release.product.id = :productId " + "and dp.executionDate <= :endDate "
            + "and (dp.undeploymentDate >= :startDate or dp.undeploymentDate IS NULL) "
            + "and (dp.status in (:statusList) or :statusList is null) "
            + "order by dp.creationDate desc ")
    List<DeploymentPlan> getByProductAndEnvironmentAndStatusBetweenDates(@Param(value = "productId") int productId,
                                                                         @Param("statusList") List<DeploymentStatus> statusList,
                                                                         @Param("environment") String environment, @Param("startDate") Calendar startDate,
                                                                         @Param("endDate") Calendar endDate);

    /**
     * Gets only the deployments plans from a release version
     * on an environment that have the given status.
     * <p>
     * If status is DEPLOYED there should be just one plan.
     *
     * @param releaseVersionId Product ID.
     * @param environment      Environment such as INT, PRE and PRO.
     * @param status           Deployment status such as DEPLOYED or PENDING_TASKS among others.
     * @return List of deployment plans.
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "    dp.environment = :environment " +
                    "and dp.releaseVersion.id = :releaseVersionId " +
                    "and dp.status = :status " +
                    "order by dp.creationDate desc "
    )
    List<DeploymentPlan> getByReleaseVersionAndEnvironmentAndStatus(
            @Param("releaseVersionId") int releaseVersionId,
            @Param("environment") String environment,
            @Param("status") DeploymentStatus status);

    /**
     * Get list of deployment plan
     *
     * @param releaseId   release id
     * @param environment environment
     * @param status      status
     * @return deployment plan
     */
    DeploymentPlan findFirstByReleaseVersionReleaseIdAndEnvironmentAndStatus(
            @Param("releaseId") int releaseId,
            @Param("environment") String environment,
            @Param("status") DeploymentStatus status);

    /**
     * Find by deploymentGcsp code
     *
     * @param deploymentGcsp deploymentGcsp instance
     * @return plan
     */
    DeploymentPlan findByGcsp(@Param("deploymentGscp") DeploymentGcsp deploymentGcsp);

    /**
     * Find by deploymentNova code
     *
     * @param deploymentNova deploymentNova instance
     * @return plan
     */
    DeploymentPlan findByNova(@Param("deploymentNova") DeploymentNova deploymentNova);

    /**
     * Checks if any service from a given plan has not any
     * hardware pack or instances set.
     * Services of type {@link com.bbva.enoa.core.novabootstarter.enumerate.ServiceType}.DEPENDENCY are excluded.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return true if any service has no resources or instances, false otherwise.
     */
    @Query(
            "select case when count ( dp.id ) > 0 then true else false end " +
                    "from DeploymentPlan dp " +
                    "   join dp.deploymentSubsystems subs " +
                    "   join subs.deploymentServices s " +
                    "where " +
                    "    dp.id = :deploymentId " +
                    "and s.service.serviceType != 'DEPENDENCY' " +
                    "and s.service.serviceType != 'BATCH_SCHEDULER_NOVA' " +
                    "and s.service.serviceType != 'LIBRARY_JAVA' " +
                    "and s.service.serviceType != 'LIBRARY_NODE' " +
                    "and s.service.serviceType != 'LIBRARY_PYTHON' " +
                    "and s.service.serviceType != 'LIBRARY_THIN2' " +
                    "and s.service.serviceType != 'LIBRARY_TEMPLATE' " +
                    "and (s.numberOfInstances = 0 or s.hardwarePack is null)"
    )
    boolean planHasPendingResources(@Param("deploymentId") int deploymentId);

    /**
     * Checks if there is any {@link Filesystem} with pending configuration in the {@link DeploymentPlan}.
     * This happens when a {@link Filesystem} is {@link FilesystemStatus}.ARCHIVED on any {@link DeploymentService}
     * of the given {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return True if any {@link Filesystem} is {@link FilesystemStatus}.ARCHIVED.
     */
    @Query(
            "select case when count ( dp.id ) > 0 then true else false end " +
                    "from DeploymentPlan dp " +
                    "   join dp.deploymentSubsystems subs " +
                    "   join subs.deploymentServices s " +
                    "   join s.deploymentServiceFilesystems dsf " +
                    "   join dsf.filesystem f " +
                    "where " +
                    "    dp.id = :deploymentId " +
                    "and s.service.serviceType != 'DEPENDENCY' " +
                    "and s.service.serviceType != 'BATCH_SCHEDULER_NOVA' " +
                    "and s.service.serviceType != 'LIBRARY_JAVA' " +
                    "and s.service.serviceType != 'LIBRARY_NODE' " +
                    "and s.service.serviceType != 'LIBRARY_PYTHON' " +
                    "and s.service.serviceType != 'LIBRARY_THIN2' " +
                    "and f.filesystemStatus = com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus.ARCHIVED "
    )
    boolean planHasPendingFilesystems(@Param("deploymentId") int deploymentId);

    /**
     * Checks if there is any {@link LogicalConnector} with pending configuration in the {@link DeploymentPlan}.
     * This happens when a {@link LogicalConnector} is {@link LogicalConnector}.ARCHIVED on any {@link DeploymentService}
     * of the given {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return True if any {@link Filesystem} is {@link FilesystemStatus}.ARCHIVED.
     */
    @Query(
            "select case when count ( dp.id ) > 0 then true else false end " +
                    "from DeploymentPlan dp " +
                    "   join dp.deploymentSubsystems subs " +
                    "   join subs.deploymentServices s " +
                    "   join s.logicalConnectors lc " +
                    "where " +
                    "    dp.id = :deploymentId " +
                    "and s.service.serviceType != 'DEPENDENCY' " +
                    "and s.service.serviceType != 'BATCH_SCHEDULER_NOVA' " +
                    "and s.service.serviceType != 'LIBRARY_JAVA' " +
                    "and s.service.serviceType != 'LIBRARY_NODE' " +
                    "and s.service.serviceType != 'LIBRARY_PYTHON' " +
                    "and s.service.serviceType != 'LIBRARY_THIN2' " +
                    "and lc.logicalConnectorStatus = com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus.ARCHIVED "
    )
    boolean planHasPendingLogicalConnector(@Param("deploymentId") int deploymentId);

    /**
     * Checks if the given {@link DeploymentPlan} has a pending {@link DeploymentTypeChangeTask}
     * associated.
     *
     * @param deploymentId {@link DeploymentPlan} ID.
     * @return True if there is a pending request of deployment type change, false otherwise.
     */
    @Query(
            "select case when count ( e.id ) > 0 then true else false end " +
                    "from DeploymentTypeChangeTask e " +
                    "where " +
                    "    e.deploymentPlan.id = :deploymentId " +
                    "and (e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING" +
                    " or  e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR)"
    )
    boolean planHasPendingDeploymentTypeChangeTask(@Param("deploymentId") int deploymentId);

    /**
     * Get the pending {@link DeploymentTypeChangeTask} of a {@link DeploymentPlan} if any.
     *
     * @param deploymentId {@link DeploymentPlan} plan.
     * @return DeploymentPlan
     */
    @Query(
            "from DeploymentTypeChangeTask e " +
                    "where " +
                    "    e.deploymentPlan.id = :deploymentId " +
                    "and (e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING" +
                    " or  e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR)"
    )
    List<DeploymentTypeChangeTask> getPendingDeploymentTypeChangeTask(@Param("deploymentId") int deploymentId);

    /**
     * Get a pending {@link ManagementActionTask} of a given entity
     * of type {@link DeploymentPlan}, {@link DeploymentSubsystem},
     * {@link DeploymentService} or {@link DeploymentInstance}.
     * .
     *
     * @param relatedId  ID of related entity.
     * @param actionType {@link ToDoTaskType} of type start or stop.
     * @return ManagementActionTask
     */
    @Query(
            "from ManagementActionTask e " +
                    "where " +
                    "    e.relatedId = :relatedId " +
                    "and e.taskType = :actionType " +
                    "and (e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING" +
                    " or  e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR)"
    )
    List<ManagementActionTask> getPendingActionTask(
            @Param("relatedId") int relatedId,
            @Param("actionType") ToDoTaskType actionType);

    /**
     * Checks if the given {@link DeploymentPlan} has a pending {@link DeploymentTask}
     * of the given {@link ToDoTaskType}.
     *
     * @param deploymentId {@link DeploymentPlan} ID.
     * @param taskType     {@link ToDoTaskType}.
     * @return True if there is a pending request of the type, false otherwise.
     */
    @Query(
            "select case when count ( e.id ) > 0 then true else false end " +
                    "from DeploymentTask e " +
                    "where " +
                    "    e.deploymentPlan.id = :deploymentId " +
                    "and e.taskType = :taskType " +
                    "and (e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING " +
                    "or   e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR)"
    )
    boolean planHasPendingDeploymentRequestOnEnvTask(
            @Param("deploymentId") int deploymentId,
            @Param("taskType") ToDoTaskType taskType);

    /**
     * Gets a deployment plan list with the parent id.
     *
     * @param parent parent id
     * @return deploymenyt plan list
     */
    List<DeploymentPlan> findByParent(@Param("parent") DeploymentPlan parent);

    /**
     * Gets only the deployments plans from a product on an environment.
     * that have the given status.
     *
     * @param productId   Product ID.
     * @param environment Environment such as INT, PRE and PRO.
     * @param status      Deployment status such as DEPLOYED or PENDING_TASKS among others.
     * @return List of deployment plans.
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "    dp.environment = :environment " +
                    "and dp.releaseVersion.release.product.id = :productId " +
                    "and dp.status <> :status"
    )
    List<DeploymentPlan> getByProductIdAndEnvironmentAndStatusNot(
            @Param("productId") int productId,
            @Param("environment") String environment,
            @Param("status") DeploymentStatus status);


    @Query(
            "select dp from DeploymentPlan dp " +
                    "inner join fetch dp.planProfiles plan " +
                    "where dp.environment = :env " +
                    "and dp.releaseVersion.release.name = :releaseName " +
                    "and plan.status = :status"
    )
    List<DeploymentPlan> getByReleaseNameAndEnvironmentAndPlanProfileStatus(
            @Param("releaseName") final String releaseName,
            @Param("env") final String env,
            @Param("status") final ProfileStatus status);

    /**
     * Gets deployment plans from list of Ids by product
     *
     * @param productId The product Id
     * @param ids       The Ids to search
     * @param pageable  Page
     * @param statuses  statuses to finda
     * @return The Page of deployment plan found
     */
    @Query(
            "select dp from DeploymentPlan dp " +
                    "where " +
                    "dp.releaseVersion.release.product.id = :productId " +
                    "and dp.status in :statuses " +
                    "and dp.id IN :ids"
    )
    Page<DeploymentPlan> findByProductAndIdInAndStatusIn(
            @Param("productId") int productId,
            @Param("ids") List<Integer> ids,
            @Param("statuses") List<DeploymentStatus> statuses,
            Pageable pageable);

    /**
     * Gets deployment plans from specific search, if any match on
     * - Release version service name OR
     * - Release Version name OR
     * - Release name
     *
     * @param productId  the product id
     * @param searchText Text to search in the specified columns
     * @param statuses   The statuses
     * @param pageable   Page with results
     * @return Page of DeploymentPlans
     */
    @Query(
            value = "SELECT DISTINCT dp.* " +
                    "FROM public.deployment_plan dp " +
                    "LEFT JOIN public.release_version rv ON dp.release_version_id = rv.id " +
                    "LEFT JOIN public.release_version_subsystem rvsub ON rvsub.release_version_id = rv.id " +
                    "LEFT JOIN public.release_version_service rvs ON rvs.version_subsystem_id = rvsub.id " +
                    "LEFT JOIN public.release r ON rv.release_id = r.id " +
                    "WHERE r.product_id = :productId " +
                    "AND dp.status IN (:statuses) " +
                    "AND to_tsvector(rvs.service_name || ' ' || rv.version_name || ' ' || r.name) @@ to_tsquery(:searchText)",
            countQuery = "SELECT COUNT ( DISTINCT  dp.*) " +
                    "FROM public.deployment_plan dp " +
                    "LEFT JOIN public.release_version rv ON dp.release_version_id = rv.id " +
                    "LEFT JOIN public.release_version_subsystem rvsub ON rvsub.release_version_id = rv.id " +
                    "LEFT JOIN public.release_version_service rvs ON rvs.version_subsystem_id = rvsub.id " +
                    "LEFT JOIN public.release r ON rv.release_id = r.id " +
                    "WHERE r.product_id = :productId " +
                    "AND dp.status IN (:statuses) " +
                    "AND to_tsvector(rvs.service_name || ' ' || rv.version_name || ' ' || r.name) @@ to_tsquery(:searchText)",
            nativeQuery = true)
    Page<DeploymentPlan> getDeploymentPlanBySearchFilterAndStatusIn(
            @Param("productId") Integer productId,
            @Param("searchText") String searchText,
            @Param("statuses") List<String> statuses,
            Pageable pageable);

    /**
     * Count how many {@link DeploymentPlan} are in each status. The query can be filtered by environment, UUAA, and platform.
     *
     * @param environment If it's null, this filter won't be applied.
     * @param uuaa        If it's null, this filter won't be applied.
     * @param platform    If it's null, this filter won't be applied.
     * @return A List representing the count.
     */
    @Query(
            value =
                    "select dp.status, count(0) " +
                            "from deployment_plan dp " +
                            "join release_version rv on dp.release_version_id = rv.id " +
                            "join \"release\" r2 on rv.release_id = r2.id " +
                            "join product p2 on r2.product_id = p2.id " +
                            "where (dp.selected_deploy = cast(:platform as text) or coalesce(:platform) is null) " +
                            "and (dp.environment = cast(:environment as text) or coalesce(:environment) is null) " +
                            "and (p2.uuaa = cast(:uuaa as text) or coalesce(:uuaa) is null) " +
                            "group by (dp.status)",
            nativeQuery = true
    )
    List<Map<String, Object>> countStatuses(@Param("environment") String environment, @Param("uuaa") String uuaa, @Param("platform") String platform);

    /**
     * Returns a list of deployed deployment plan ids having related deployment batch instances.
     *
     * @param environment The deployment environment.
     * @param uuaa        The product uuaa.
     * @param platform    The platform for deployment plan.
     * @return A list of deployment plan ids.
     */
    @Query(value = "select dp.id " +
            "from deployment_plan dp  " +
            "inner join release_version rv  " +
            "on rv.id = dp.release_version_id  " +
            "inner join \"release\" r " +
            "on r.id = rv.release_id  " +
            "inner join product p " +
            "on p.id = r.product_id  " +
            "where dp.status = 'DEPLOYED' " +
            "and coalesce(cast(:uuaa as text), p.uuaa) = p.uuaa " +
            "and coalesce(cast(:environment as text), dp.environment) = dp.environment  " +
            "and coalesce(cast(:platform as text), dp.selected_deploy) = dp.selected_deploy", nativeQuery = true)
    List<Integer> findByStatisticsSummaryFilters(final String environment, final String uuaa, final String platform);

    List<DeploymentPlan> findByIdIn(@Param("ids") Set<Integer> ids);
}

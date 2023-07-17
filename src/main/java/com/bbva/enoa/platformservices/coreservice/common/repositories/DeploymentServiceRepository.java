package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xe30000
 */
public interface DeploymentServiceRepository extends JpaRepository<DeploymentService, Integer>
{

    /**
     * Finds a deploymentService by its names
     *
     * @param productName Name of the product
     * @param environment environment service is deployed in
     * @param releaseName Release service belongs to
     * @param subsystemId subsystem service belongs to
     * @param serviceName name of the service
     * @return A deploymentService that comply to all the conditions
     */
    @Query("SELECT s FROM DeploymentService s"
            + " WHERE "
            + "s.service.serviceName=:serviceName"
            + " and "
            + "s.deploymentSubsystem.subsystem.subsystemId=:subsystemId"
            + " and "
            + "s.deploymentSubsystem.deploymentPlan.releaseVersion.release.name=:releaseName"
            + " and "
            + "s.deploymentSubsystem.deploymentPlan.status =com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.DEPLOYED"
            + " and " + "s.deploymentSubsystem.deploymentPlan.environment=:environment" + " and "
            + "(s.deploymentSubsystem.deploymentPlan.releaseVersion.release.product.name=:productName)" + "")
    DeploymentService findDeploymentServiceByServiceInfo(@Param("productName") String productName,
                                                         @Param("environment") String environment, @Param("releaseName") String releaseName,
                                                         @Param("subsystemId") Integer subsystemId,
                                                         @Param("serviceName") String serviceName);


    DeploymentService findByServiceId(@Param("serviceVersionId") Integer id);

    DeploymentService findByServiceIdAndDeploymentSubsystem(@Param("serviceVersionId") Integer serviceVersionId, @Param("deploymentSubsystem") DeploymentSubsystem deploymentSubsystem);

    /**
     * Find all deployment service ids filtered by uuaa.
     *
     * @param uuaa The related product uuaa.
     * @return A list containing deployment service ids.
     */
    @Query("select ds.id from DeploymentService ds inner join ds.service rvs inner join rvs.versionSubsystem rvsub inner join rvsub.releaseVersion rv inner join rv.release r inner join r.product p on p.uuaa = :uuaa")
    List<Integer> findDeploymentServiceIdsByUuaa(@Param("uuaa") String uuaa);

    /**
     * Count how many {@link DeploymentService} are in each status. The query can be filtered by environment, UUAA, and platform.
     *
     * @param environment If it's null, this filter won't be applied.
     * @param uuaa        If it's null, this filter won't be applied.
     * @param platform    If it's null, this filter won't be applied.
     * @return A List representing the count.
     */
    @Query(
            value =
                    "select rvs.service_type, count(0) " +
                            "from deployment_service ds " +
                            "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
                            "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
                            "join release_version rv on dp.release_version_id = rv.id " +
                            "join \"release\" r2 on rv.release_id = r2.id " +
                            "join product p2 on r2.product_id = p2.id " +
                            "join release_version_service rvs on ds.service_id = rvs.id " +
                            "where (dp.selected_deploy = cast(:platform as text) or coalesce(:platform) is null) " +
                            "and (dp.environment = cast(:environment as text) or coalesce(:environment) is null) " +
                            "and (p2.uuaa = cast(:uuaa as text) or coalesce(:uuaa) is null) " +
                            "and dp.status = 'DEPLOYED' " +
                            "group by (rvs.service_type)",
            nativeQuery = true
    )
    List<Map<String, Object>> countStatusesFilteringByDeployed(@Param("environment") String environment, @Param("uuaa") String uuaa, @Param("platform") String platform);

    List<DeploymentService> findByIdIn(@Param("ids") Set<Integer> ids);

    /**
     * Get service name
     *
     * @param serviceId service id
     */
    @Query(value =
            "select rvs.service_name " +
                    "from deployment_service ds " +
                    "join release_version_service rvs on ds.service_id = rvs.id " +
                    "where ds.id = :serviceId",
            nativeQuery = true)
    String getServiceName(@Param("serviceId") Integer serviceId);
}

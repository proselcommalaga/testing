package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.EtherDeploymentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface EtherDeploymentInstanceRepository extends JpaRepository<EtherDeploymentInstance, Integer>
{

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}.
     *
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypes();

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by started.
     *
     * @param started Filter by "started".
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where coalesce(di.started, false) = :started " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByStarted(@Param("started") Boolean started);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by UUAA.
     *
     * @param uuaa The given UUAA.
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
        "join release_version rv on rvs2.release_version_id = rv.id " +
        "join \"release\" r2 on rv.release_id = r2.id " +
        "join product p2 on r2.product_id = p2.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where p2.uuaa = :uuaa " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByUuaa(@Param("uuaa") String uuaa);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by UUAA and started.
     *
     * @param uuaa The given UUAA.
     * @param started Filter by "started".
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
        "join release_version rv on rvs2.release_version_id = rv.id " +
        "join \"release\" r2 on rv.release_id = r2.id " +
        "join product p2 on r2.product_id = p2.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where coalesce(di.started, false) = :started " +
        "and p2.uuaa = :uuaa " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByUuaaAndStarted(@Param("uuaa") String uuaa, @Param("started") Boolean started);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by environment.
     *
     * @param environment The given environment.
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where dp.environment = :environment " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByEnvironment(@Param("environment") String environment);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by environment and started.
     *
     * @param environment The given environment.
     * @param started Filter by "started".
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where coalesce(di.started, false) = :started " +
        "and dp.environment = :environment " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByEnvironmentAndStarted(@Param("environment") String environment, @Param("started") Boolean started);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by environment and UUAA.
     *
     * @param environment The given environment.
     * @param uuaa The given UUAA.
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
        "join release_version rv on rvs2.release_version_id = rv.id " +
        "join \"release\" r2 on rv.release_id = r2.id " +
        "join product p2 on r2.product_id = p2.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where dp.environment = :environment " +
        "and p2.uuaa = :uuaa " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByEnvironmentAndUuaa(@Param("environment") String environment, @Param("uuaa") String uuaa);

    /**
     * Count how many services are of each type for all {@link EtherDeploymentInstance}, filtering by environment, UUAA and started.
     *
     * @param environment The given environment.
     * @param uuaa The given UUAA.
     * @param started Filter by "started".
     * @return A List representing the count.
     */
    @Query(
        value =
        "select rvs.service_type, sum(ds.number_of_instances) as count " +
        "from deployment_instance_ether di " +
        "join deployment_service ds on di.service_id = ds.id " +
        "join release_version_service rvs on ds.service_id = rvs.id " +
        "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
        "join release_version rv on rvs2.release_version_id = rv.id " +
        "join \"release\" r2 on rv.release_id = r2.id " +
        "join product p2 on r2.product_id = p2.id " +
        "join deployment_subsystem ds2 on ds.deployment_subsystem_id = ds2.id " +
        "join deployment_plan dp on ds2.deployment_plan_id = dp.id " +
        "where coalesce(di.started, false) = :started " +
        "and dp.environment = :environment " +
        "and p2.uuaa = :uuaa " +
        "and dp.status = 'DEPLOYED' " +
        "and di.deletion_date is null " +
        "group by (rvs.service_type)",
        nativeQuery = true
    )
    List<Map<String, Object>> countServiceTypesFilteringByEnvironmentAndUuaaAndStarted(@Param("environment") String environment, @Param("uuaa") String uuaa, @Param("started") Boolean started);
}

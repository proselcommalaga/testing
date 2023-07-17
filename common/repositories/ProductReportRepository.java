package com.bbva.enoa.platformservices.coreservice.common.repositories;


import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.view.ProductCSV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product report repository
 */
@Repository
public interface ProductReportRepository extends JpaRepository<Product, Long>, ProductCustomRepository
{
    /**
     * Query of all Products and Subsystem and Services for report purpose
     *
     * @return list of CSV
     */
    @Query(" SELECT  "
            + " distinct "
            + " new "
            + " com.bbva.enoa.platformservices.coreservice.common.view.ProductCSV("
            + " product.name, "
            + " product.uuaa, "
            + " product.description, "
            + " deploymentPlan.environment, "
            + " release.name AS releaseName, "
            + " releaseVersion.versionName, "
            + " deploymentPlan.executionDate, "
            + " releaseVersionService.serviceName, "
            + " releaseVersionService.serviceType, "
            + " deploymentService.numberOfInstances, "
            + " hardwarePack.numCPU, "
            + " hardwarePack.ramMB, "
            + " releaseVersionSubsystem.subsystemId) "
            + " FROM "
            + " Product product "
            + " JOIN product.releases release "
            + " JOIN release.releaseVersions as releaseVersion "
            + " JOIN releaseVersion.deployments as deploymentPlan "
            + " JOIN deploymentPlan.deploymentSubsystems as deploymentSubsystem "
            + " JOIN deploymentSubsystem.deploymentServices as deploymentService "
            + " JOIN deploymentService.hardwarePack as hardwarePack "
            + " JOIN deploymentService.service as releaseVersionService "
            + " JOIN deploymentSubsystem.subsystem as releaseVersionSubsystem "
            + " WHERE deploymentPlan.status='DEPLOYED' "
            + " AND releaseVersionService.serviceType not in ('DEPENDENCY','BATCH_SCHEDULER_NOVA','LIBRARY_JAVA','LIBRARY_PYTHON','LIBRARY_NODE','LIBRARY_THIN2','LIBRARY_TEMPLATE') "
            + " ORDER BY "
            + " product.name, "
            + " releaseName, "
            + " releaseVersion.versionName "
    )
    List<ProductCSV> findProductsCsv();

    /**
     * Gets information for products assigned resources with pagination.
     * <p>
     * An old version of this method accepted Pageable parameter
     * for pagination, but it's buggy for queries using other queries
     * as tables, as this query is. That's the reason because we make
     * a more crafted pagination.
     *
     * If results exists in response, first row has only aggregated information
     * for total CPUs, total instances and total memory of all returned services.
     * If we pass a non existing page for filters returning elements (i.e. page 60
     * of a 2 page results), services won't be returned, but total information (total
     * elements, total aggregated CPU, total aggregated instances and total aggregated
     * assigned memory) still has to be shown. It's standard {@link org.springframework.data.domain.Page}
     * behaviour.
     *
     * From second to last row it returns specific service information.
     *
     * @param environment The environment to be filtered. Mandatory.
     * @param uuaa        The product UUAA to be filtered.
     * @param pageSize    Number of elements per page.
     * @param offset      Number of elements to jump before fetch data.
     * @return A DTO with information for matching products assigned resources.
     */
    @Query(value = "select null as productName " +
            ", null as releaseName " +
            ", null as releaseVersionName " +
            ", null as executionDate " +
            ", null as subsystemId " +
            ", null as serviceName " +
            ", sum(hp.numcpu) over () as cpu " +
            ", cast((sum(ds.number_of_instances) over ()) as bigint) as instances " +
            ", cast((sum(hp.rammb) over ()) as bigint) as memory " +
            ", count(1) over() as totalElements " +
            "from hardware_pack hp " +
            "inner join deployment_service ds " +
            "on ds.hardware_pack_id = hp.id " +
            "inner join deployment_subsystem dsub " +
            "on dsub.id = ds.deployment_subsystem_id " +
            "inner join deployment_plan dp " +
            "on dp.id = dsub.deployment_plan_id " +
            "inner join release_version rv " +
            "on rv.id = dp.release_version_id " +
            "inner join release_version_subsystem rvsub " +
            "on rvsub.release_version_id = rv.id " +
            "inner join release_version_service rvs " +
            "on rvs.version_subsystem_id = rvsub.id and rvs.id = ds.service_id " +
            "inner join release r " +
            "on r.id = rv.release_id " +
            "inner join product p " +
            "on p.id = r.product_id " +
            "where dp.status = 'DEPLOYED' " +
            "and coalesce(cast(:environment as text), dp.environment) = dp.environment " +
            "and coalesce(cast(:uuaa as text), p.uuaa) = p.uuaa " +
            "group by p.name, r.name, rv.version_name, dp.execution_date, rvsub.subsystem_id, rvs.service_name, hp.numcpu, ds.number_of_instances, hp.rammb " +
            "union " +
            "(select p.name as productName " +
            ", r.name as releaseName " +
            ", rv.version_name as releaseVersionName " +
            ", dp.execution_date as executionDate " +
            ", rvsub.subsystem_id as subsystemId " +
            ", rvs.service_name as serviceName " +
            ", sum(hp.numcpu) as cpu " +
            ", sum(cast(coalesce(ds.number_of_instances, 0) as bigint)) as instances " +
            ", sum(cast(coalesce(hp.rammb, 0) as bigint)) as memory " +
            ", 1 as totalElements " +
            "from hardware_pack hp " +
            "inner join deployment_service ds " +
            "on ds.hardware_pack_id = hp.id " +
            "inner join deployment_subsystem dsub " +
            "on dsub.id = ds.deployment_subsystem_id " +
            "inner join deployment_plan dp " +
            "on dp.id = dsub.deployment_plan_id " +
            "inner join release_version rv " +
            "on rv.id = dp.release_version_id " +
            "inner join release_version_subsystem rvsub " +
            "on rvsub.release_version_id = rv.id " +
            "inner join release_version_service rvs " +
            "on rvs.version_subsystem_id = rvsub.id and rvs.id = ds.service_id " +
            "inner join release r " +
            "on r.id = rv.release_id " +
            "inner join product p " +
            "on p.id = r.product_id " +
            "where dp.status = 'DEPLOYED' " +
            "and coalesce(cast(:environment as text), dp.environment) = dp.environment " +
            "and coalesce(cast(:uuaa as text), p.uuaa) = p.uuaa " +
            "group by p.name, r.name, rv.version_name, dp.execution_date, rvsub.subsystem_id, rvs.service_name, hp.numcpu, ds.number_of_instances, hp.rammb " +
            "limit :pageSize offset :offset)" +
            "order by executionDate desc, productName desc", nativeQuery = true)
    List<Object[]> findProductsByAssignedResourcesReportFiltersPageable(@Param("environment") final String environment, @Param("uuaa") final String uuaa, @Param("pageSize") final Integer pageSize, @Param("offset") final Integer offset);
}


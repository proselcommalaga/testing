package com.bbva.enoa.platformservices.coreservice.common.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bbva.enoa.datamodel.model.product.entities.Product;

/**
 * Product report repository
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Product, Long>
{
    /**
     * @return the number of products deployed in Production
     */
    @Query(" SELECT COUNT(DISTINCT product.id) "
         + " FROM "
         + " Product product "
         + " JOIN product.releases release "
         + " JOIN release.releaseVersions as releaseVersion "
         + " JOIN releaseVersion.deployments as deploymentPlan "
         + " JOIN deploymentPlan.deploymentSubsystems as deploymentSubsystem "
         + " JOIN deploymentSubsystem.deploymentServices as deploymentService "
         + " JOIN deploymentService.service as releaseVersionService "
         + " WHERE deploymentPlan.status='DEPLOYED' AND deploymentPlan.environment = 'PRO' "
         + " AND releaseVersionService.serviceType not in ('DEPENDENCY','BATCH_SCHEDULER_NOVA','LIBRARY_JAVA','LIBRARY_PYTHON','LIBRARY_NODE','LIBRARY_THIN2') "

    )
    Long getCountProductsDeployed() ;
    
    /**
     * @return the number of services deployed in Production
     */
    @Query(" SELECT COUNT(DISTINCT deploymentService.id) "
         + " FROM "
         + " DeploymentPlan as deploymentPlan "
         + " JOIN deploymentPlan.deploymentSubsystems as deploymentSubsystem "
         + " JOIN deploymentSubsystem.deploymentServices as deploymentService "
         + " JOIN deploymentService.service as releaseVersionService "
         + " WHERE deploymentPlan.status='DEPLOYED' AND deploymentPlan.environment = 'PRO' "
         + " AND releaseVersionService.serviceType not in ('DEPENDENCY','BATCH_SCHEDULER_NOVA','LIBRARY_JAVA','LIBRARY_PYTHON','LIBRARY_NODE','LIBRARY_THIN2') "
    )
    Long getCountServicesDeployed() ;

}


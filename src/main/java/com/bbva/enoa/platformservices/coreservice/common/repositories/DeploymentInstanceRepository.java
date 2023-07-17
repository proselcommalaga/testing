package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Deployment instance repository
 *
 * @author xe30000
 */
public interface DeploymentInstanceRepository extends JpaRepository<DeploymentInstance, Integer>
{
    /**
     * Get all deployment instance of service type provided before creation day or date provided
     *
     * @param calendar        the date before provided
     * @param serviceTypeList the service type list to get the deployment instance
     * @return a list of deployment instance of service type before to date provided
     */
    List<DeploymentInstance> findByCreationDateBeforeAndService_Service_ServiceTypeIn(Calendar calendar, List<String> serviceTypeList);


    /**
     * Get a NOVA deployment instance by hostName and serviceId
     *
     * @param hostname  hostname where instance is deployed
     * @param serviceId deployment service id
     * @return nova deployment instance
     */
    @Query(
            value =
                    "select * " +
                            "from deployment_instance di " +
                            "where di.host_name = :hostname and di.service_id = :serviceId  ",
            nativeQuery = true
    )
    Optional<NovaDeploymentInstance> getDeploymentInstanceByHostNameAndServiceId(
            @Param("hostname") String hostname,
            @Param("serviceId") Integer serviceId);

}

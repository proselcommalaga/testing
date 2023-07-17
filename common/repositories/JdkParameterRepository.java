package com.bbva.enoa.platformservices.coreservice.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;

import java.util.List;

/**
 * JdkParameter repository
 *
 */
public interface JdkParameterRepository extends JpaRepository<JdkParameter, Integer>
{

    /**
     * Check if a JdkParameter exists
     *
     * @param name name to search
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Find list of JDK parameters of a DeploymentService
     *
     * @param deploymentServiceId deployment service id
     * @return list of JdkParameter
     */
    @Query("SELECT distinct param " +
            "FROM JdkParameter param " +
            "JOIN param.allowedJdkParameterProducts as ajpp " +
            "JOIN ajpp.deploymentServiceAllowedJdkParameterValues dsajpv " +
            "JOIN dsajpv.deploymentService ds " +
            "WHERE ds.id = :deploymentServiceId " +
            "AND ajpp.isDefault = false")
    List<JdkParameter> findByDeploymentService(@Param("deploymentServiceId") final Integer deploymentServiceId);

    /**
     * Check if DeploymentService has set a Jdk parameter
     *
     * @param deploymentServiceId deployment service id
     * @return list of JdkParameter
     */
    @Query("SELECT case when count (param.id) > 0 then true else false end " +
            "FROM JdkParameter param " +
            "JOIN param.allowedJdkParameterProducts as ajpp " +
            "JOIN ajpp.deploymentServiceAllowedJdkParameterValues dsajpv " +
            "JOIN dsajpv.deploymentService ds " +
            "WHERE ds.id = :deploymentServiceId " +
            "AND param.name = :jdkParameterName " +
            "AND ajpp.isDefault = false")
    boolean existsByNameAndDeploymentService(@Param("jdkParameterName") final String jdkParameterName, @Param("deploymentServiceId") final Integer deploymentServiceId);
}

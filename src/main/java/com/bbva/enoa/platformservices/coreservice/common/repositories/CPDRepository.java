package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.CPD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xe30000
 */
@Transactional(readOnly = true)
public interface CPDRepository extends JpaRepository<CPD, Integer>
{

    /**
     * Get a CPD by name, environment and isMainSwarmCluster
     * @param name                  The name.
     * @param environment           The environment.
     * @param isMainSwarmCluster    Whether is main Swarm cluster.
     * @return The CPD.
     */
    CPD getByNameAndEnvironmentAndMainSwarmCluster(final String name, final String environment, final Boolean isMainSwarmCluster);


    /**
     * Get active cpds.
     *
     * @param env  environment
     * @return List of CPD.
     */
    @Query(
            "select cpd from CPD cpd where " +
                    "cpd.active = true and " +
                    "cpd.environment = :env")
    List<CPD> getActiveCpdByEnvironment(@Param("env") String env);

    /**
     * Get CPDs by environment.
     * @param environment The environment.
     * @return List of CPDs.
     */
    List<CPD> getByEnvironment(final String environment);

    /**
     * Get name ordered by Name ascending.
     *
     * @return A List of name, ordered by Name ascending.
     */
    List<CPD> findAllByOrderByNameAsc();
}


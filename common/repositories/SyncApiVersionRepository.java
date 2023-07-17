package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * SyncApiSecurityPolicy Repository
 */
public interface SyncApiVersionRepository extends JpaRepository<SyncApiVersion, Integer>
{

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.uuaa = :uuaa AND av.api.type <> 'EXTERNAL'")
    SyncApiVersion findByApiNameAndVersionAndUuaaAndExternalFalse(@Param("apiName") final String apiName, @Param("version") final String version, @Param("uuaa") final String uuaa);

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.product.id = :productId")
    SyncApiVersion findByProductIdAndApiNameAndVersion(@Param("productId") final Integer productId, @Param("apiName") final String apiName, @Param("version") final String version);

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.uuaa = :uuaa AND av.api.product.id = :productId")
    SyncApiVersion findByApiNameAndVersionAndUuaaAndProductId(@Param("apiName") final String apiName, @Param("version") final String version, @Param("uuaa") final String uuaa, @Param("productId") final Integer productId);

    /**
     * Find the distinct {@link ApiVersion} that are served in a given environment, filtering by a given UUAA.
     */
    @Query(
            value = "select distinct on (sa.uuaa, sa.name, sav.version) * " +
                    "from api_version sav " +
                    "join api sa on sav.api_id = sa.id " +
                    "join api_implementation sai  on sav.id = sai.api_version_id " +
                    "join release_version_service rvs on sai.service_id = rvs.id " +
                    "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
                    "join release_version rv on rvs2.release_version_id = rv.id " +
                    "join deployment_plan dp on rv.id = dp.release_version_id " +
                    "where sai.implemented_as = 'SERVED' " +
                    "and dp.status = 'DEPLOYED' " +
                    "and sa.uuaa = :uuaa " +
                    "and dp.environment = :environment ",
            nativeQuery = true
    )
    List<SyncApiVersion> findDistinctByUuaaAndServedInEnvironment(@Param("uuaa") String uuaa, @Param("environment") String environment);

    /**
     * Find the distinct {@link ApiVersion} that are served in any environment, filtering by a given UUAA.
     */
    @Query(
            value = "select distinct on (sa.uuaa, sa.name, dp.environment, sav.version) * " +
                    "from api_version sav " +
                    "join api sa on sav.api_id = sa.id " +
                    "join api_implementation sai  on sav.id = sai.api_version_id " +
                    "join release_version_service rvs on sai.service_id = rvs.id " +
                    "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
                    "join release_version rv on rvs2.release_version_id = rv.id " +
                    "join deployment_plan dp on rv.id = dp.release_version_id " +
                    "where sai.implemented_as = 'SERVED' " +
                    "and dp.status = 'DEPLOYED' " +
                    "and sa.uuaa = :uuaa ",
            nativeQuery = true
    )
    List<SyncApiVersion> findDistinctByUuaaAndServedInAnyEnvironment(@Param("uuaa") String uuaa);

    /**
     * Find the distinct {@link ApiVersion} that are served in a given environment.
     */
    @Query(
            value = "select distinct on (sa.uuaa, sa.name, sav.version) * " +
                    "from api_version sav " +
                    "join api sa on sav.api_id = sa.id " +
                    "join api_implementation sai  on sav.id = sai.api_version_id " +
                    "join release_version_service rvs on sai.service_id = rvs.id " +
                    "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
                    "join release_version rv on rvs2.release_version_id = rv.id " +
                    "join deployment_plan dp on rv.id = dp.release_version_id " +
                    "where sai.implemented_as = 'SERVED' " +
                    "and dp.status = 'DEPLOYED' " +
                    "and dp.environment = :environment ",
            nativeQuery = true
    )
    List<SyncApiVersion> findDistinctServedInEnvironment(@Param("environment") String environment);

    /**
     * Find the distinct {@link ApiVersion} that are served in any environment.
     */
    @Query(
            value = "select distinct on (sa.uuaa, sa.name, sav.version, dp.environment) * " +
                    "from api_version sav " +
                    "join api sa on sav.api_id = sa.id " +
                    "join api_implementation sai  on sav.id = sai.api_version_id " +
                    "join release_version_service rvs on sai.service_id = rvs.id " +
                    "join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
                    "join release_version rv on rvs2.release_version_id = rv.id " +
                    "join deployment_plan dp on rv.id = dp.release_version_id " +
                    "where sai.implemented_as = 'SERVED' " +
                    "and dp.status = 'DEPLOYED' ",
            nativeQuery = true
    )
    List<SyncApiVersion> findDistinctServedInAnyEnvironment();
}

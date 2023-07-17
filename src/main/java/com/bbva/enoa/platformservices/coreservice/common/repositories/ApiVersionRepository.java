package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * SyncApiSecurityPolicy Repository
 */
public interface ApiVersionRepository extends JpaRepository<ApiVersion<?,?,?>, Integer>
{

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.uuaa = :uuaa AND av.api.type <> 'EXTERNAL'")
    ApiVersion<?,?,?> findByApiNameAndVersionAndUuaaAndExternalFalse(@Param("apiName") final String apiName, @Param("version") final String version, @Param("uuaa") final String uuaa);

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.product.id = :productId")
    ApiVersion<?,?,?> findByProductIdAndApiNameAndVersion(@Param("productId") final Integer productId, @Param("apiName") final String apiName, @Param("version") final String version);

    @Query(value = "SELECT av FROM ApiVersion av WHERE av.version = :version AND av.api.name = :apiName AND av.api.uuaa = :uuaa AND av.api.product.id = :productId")
    ApiVersion<?,?,?> findByApiNameAndVersionAndUuaaAndProductId(@Param("apiName") final String apiName, @Param("version") final String version, @Param("uuaa") final String uuaa, @Param("productId") final Integer productId);

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
    List<ApiVersion<?,?,?>> findDistinctByUuaaAndServedInEnvironment(@Param("uuaa") String uuaa, @Param("environment") String environment);

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
    List<ApiVersion<?,?,?>> findDistinctByUuaaAndServedInAnyEnvironment(@Param("uuaa") String uuaa);

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
    List<ApiVersion<?,?,?>> findDistinctServedInEnvironment(@Param("environment") String environment);

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
    List<ApiVersion<?,?,?>> findDistinctServedInAnyEnvironment();

    @Query(
            value = "select av.* " +
                    "from api_version av " +
                    "     left join api_implementation ai on ai.api_version_id = av.id " +
                    "     left join served_api_implementation_consuming_api_version saicav on saicav.consumed_api_version_id = av.id " +
                    "     left join api_implementation_backward_compatible_api_version aibcav on aibcav.backward_compatible_api_version_id = av.id " +
                    "where av.id in (:apiVersionsIds) " +
                    "  and ai is null " +
                    "  and saicav is null " +
                    "  and aibcav is null",
            nativeQuery = true
    )
    List<ApiVersion<?,?,?>> findAllNotImplemented(@Param("apiVersionsIds") List<Integer> apiVersionsIds);

    @Query(
            value = "select COUNT(distinct(sa.uuaa, sa.name, sav.version, dp.environment)), sa.type " +
                    "from " +
                    "api_version sav " +
                    "     join api sa on sav.api_id = sa.id " +
                    "     join api_implementation sai on sav.id = sai.api_version_id " +
                    "     join release_version_service rvs on sai.service_id = rvs.id " +
                    "     join release_version_subsystem rvs2 on rvs.version_subsystem_id = rvs2.id " +
                    "     join release_version rv on rvs2.release_version_id = rv.id " +
                    "     join deployment_plan dp on rv.id = dp.release_version_id " +
                    "where " +
                    "  sai.implemented_as = 'SERVED' " +
                    "  and dp.status = 'DEPLOYED' " +
                    "  and coalesce(cast(:uuaa as text), sa.uuaa) = sa.uuaa " +
                    "  and coalesce(cast(:environment as text), dp.environment) = dp.environment " +
                    "  and coalesce(cast(:functionality as text), sa.discriminator) = sa.discriminator " +
                    "group by sa.type ",
            nativeQuery = true
    )
    List<Object[]> findAllApisSummary(@Param("uuaa") String uuaa, @Param("environment") String environment, @Param("functionality") String functionality);
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * SyncApiSecurityPolicy Repository
 */
public interface ApiImplementationRepository extends JpaRepository<ApiImplementation<?,?,?>, Integer>
{

    /**
     * Find the {@link ApiImplementation} list for a given {@link ApiVersion} where the release version is in status READY_TO_DEPLOY
     */
    @Query(
            value = "SELECT ai FROM ApiImplementation ai " +
                    "JOIN ai.apiVersion AS av " +
                    "JOIN ai.service AS s " +
                    "JOIN s.versionSubsystem AS vs " +
                    "JOIN vs.releaseVersion AS rv " +
                    "WHERE av.id = :apiVersionId AND rv.status = 'READY_TO_DEPLOY'"
    )
    List<ApiImplementation<?,?,?>> findApiImplementationByReadyReleaseVersion(@Param("apiVersionId") Integer apiVersionId);

    @Query(value = "select ai.* from api_implementation ai" +
            " join served_api_implementation_consuming_api_version av on ai.id = av.served_api_implementation_id" +
            " where av.consumed_api_version_id = :apiVersionId ", nativeQuery = true

    )
    List<ApiImplementation<?,?,?>> findApiImplementationsConsumingApiVersion(@Param("apiVersionId") Integer apiVersionId);

    @Query(value = "select ai.* from api_implementation ai" +
            " join api_implementation_backward_compatible_api_version av on ai.id = av.api_implementation_id" +
            " where av.backward_compatible_api_version_id = :apiVersionId ", nativeQuery = true
    )
    List<ApiImplementation<?,?,?>> findApiImplementationsBackwardCompatibleWithApiVersion(@Param("apiVersionId") Integer apiVersionId);
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Release Version Service Repository
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface ReleaseVersionServiceRepository extends JpaRepository<ReleaseVersionService, Integer>
{

    /**
     * Query to obtain api implementations order by id descending
     *
     * @param apiId    Id to query implementtions for
     * @param pageable Paging criterias
     * @return A page of query results
     */
    @Query(value = "select service from ReleaseVersionService service "
            + "join service.apiImplementations as apiImpl " + "where "
            + "apiImpl.implementedAs=com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs.SERVED" + "  and "
            + "    apiImpl.apiVersion.id =:apiId " + "  and"
            + "    service.versionSubsystem.releaseVersion.status <> com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus.STORAGED "
            + "order by service.id DESC")
    Page<ReleaseVersionService> findDeployedImplementations(@Param("apiId") Integer apiId, Pageable pageable);

    /**
     * Query to obtain api implementations order by id descending
     *
     * @param apiId Id to query implementtions for
     * @return A page of query results
     */
    @Query(value = "select service from ReleaseVersionService service "
            + "join service.apiImplementations as apiImpl " + "where "
            + "apiImpl.implementedAs=com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs.SERVED" + "  and "
            + " ( apiImpl.apiVersion.id =:apiId or :apiId member of apiImpl.backwardCompatibleApis) " + "  and"
            + "    service.versionSubsystem.releaseVersion.status <> com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus.STORAGED "
            + "order by service.id DESC")
    List<ReleaseVersionService> findAllDeployedImplementations(@Param("apiId") Integer apiId);
}

package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Release repository.
 */
@Transactional(readOnly = true)
public interface ReleaseRepository extends JpaRepository<Release, Integer>
{

    /**
     * Check release name existance
     *
     * @param productId   product id
     * @param releaseName release name
     * @return true if name already exists
     */
    @Query(
            "select case when count ( r.id ) > 0 then true else false end " +
                    "from Product p join p.releases r where " +
                    "p.id = :productId " +
                    "and r.name = :releaseName ")
    boolean existsReleaseWithSameName(
            @Param("productId") int productId,
            @Param("releaseName") String releaseName);
}
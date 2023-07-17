package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.common.entities.PlatformProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Platform properties
 */
public interface PlatformPropertyRepository extends JpaRepository<PlatformProperty, Integer>
{

    /**
     * Get value property
     *
     * @param propertyName name property
     * @return value property
     */
    @Query(
            "select p.propertyValue " +
                    "from PlatformProperty p " +
                    "where " +
                    "p.propertyName = :propertyName")
    String findByPropertyName(@Param("propertyName") String propertyName);
}

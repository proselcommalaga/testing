package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.Api;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Nova API Repository
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface ApiRepository extends JpaRepository<Api<?,?,?>, Integer>
{

    List<Api<?,?,?>> findAllByProductId(final Integer productId);

    /**
     * Find all {@link Api} by product, name and uuaa
     *
     * @param productId Product id
     * @param apiName   Name of the API
     * @param uuaa      Uuaa of the API
     * @return sync APIs filtered by product, name and version
     */
    Api<?,?,?> findByProductIdAndNameAndUuaa(final Integer productId, final String apiName, final String uuaa);
}

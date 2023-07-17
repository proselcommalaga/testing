package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Nova API Repository
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface AsyncBackToBackApiRepository extends JpaRepository<AsyncBackToBackApi, Integer>
{
    AsyncBackToBackApi findByProductIdAndNameAndUuaa(final Integer productId, final String apiName, final String uuaa);
}

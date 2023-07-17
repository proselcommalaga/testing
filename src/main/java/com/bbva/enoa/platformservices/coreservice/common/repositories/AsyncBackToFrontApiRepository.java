package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Nova API Repository
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface AsyncBackToFrontApiRepository extends JpaRepository<AsyncBackToFrontApi, Integer>
{

    AsyncBackToFrontApi findByProductIdAndNameAndUuaa(final Integer productId, final String apiName, final String uuaa);

    /**
     * Get the NOVA APIs that are using a MSA document given by its ID.
     *
     * @param msaDocumentId The ID of the MSA document.
     * @return A List of NOVA APIs.
     */
    List<AsyncBackToFrontApi> findByMsaDocumentId(Integer msaDocumentId);

    /**
     * Get the NOVA APIs that are using an ARA document given by its ID.
     *
     * @param araDocumentId The ID of the ARA document.
     * @return A List of NOVA APIs.
     */
    List<AsyncBackToFrontApi> findByAraDocumentId(Integer araDocumentId);

}

package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionSubsystemDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVSubsystemDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;

/**
 * Service DTO builder
 * Builds the DTO of a subsystem
 */
public interface ISubsystemDtoBuilder
{
    /**
     * Builds a NewReleaseVersionSubsystemDto array from all
     * the product releases, tags and services.
     *
     * @param product     Product
     * @param ivUser      user requester
     * @param releaseName Name of the release
     * @return Array of NewReleaseVersionSubsystemDto
     */
    NewReleaseVersionSubsystemDto[] buildNewSubsystemsFromProduct(Product product, String ivUser, String releaseName);

    /**
     * Build a RVSubsystemDTO list with all subsystems of given product
     *
     * @param product product
     * @return an array with all subsystems with their tags
     */
    RVSubsystemDTO[] buildRVSubsystemDTO(Product product);
}

package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVErrorDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceValidationDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVSubsystemTagDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVTagDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.List;

/**
 * Validate tag interface
 */
public interface IBehaviorTagValidator
{
    /**
     * Build a DTO with the result of validate all services from given tag
     *
     * @param ivUser              BBVA user code
     * @param product             product
     * @param tagDTO              tag to validate
     * @param subsystemTagRequest info about subsystem tag
     * @param projectFiles        project names
     * @return list with service validated
     */

    List<BVServiceValidationDTO> buildTagValidationByService(final String ivUser, final Product product, final BVTagDTO tagDTO, final BVSubsystemTagDTO subsystemTagRequest, final List<String> projectFiles);

    /**
     * Build a DTO with the result of validate given tag
     *
     * @param tagDTO                   tag to validate
     * @param subsystemTagRequest      info about subsystem tag
     * @param serviceValidationDTOList validation list
     * @param projectFiles             project names
     * @return list of BVErrorDTO
     */
    BVErrorDTO[] buildTagValidation(final BVTagDTO tagDTO, final BVSubsystemTagDTO subsystemTagRequest, final List<BVServiceValidationDTO> serviceValidationDTOList, final List<String> projectFiles);

}

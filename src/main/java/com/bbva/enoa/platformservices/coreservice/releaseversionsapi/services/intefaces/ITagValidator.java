package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;

import java.util.List;

/**
 * Validate tag interface
 */
public interface ITagValidator
{
    /**
     * Build a DTO with the result of validate given tag
     *
     * @param tagDTO                   tag to validate
     * @param subsystemDTO             info about subsystem tag
     * @param serviceValidationDTOList validation list
     * @param projectFiles             project names
     * @return RVErrorDTO
     */
    RVErrorDTO[] buildTagValidation(final RVTagDTO tagDTO, final RVSubsystemBaseDTO subsystemDTO, final List<RVServiceValidationDTO> serviceValidationDTOList, final List<String> projectFiles);


    /**
     * Build a DTO with the result of validate all services from given tag
     *
     * @param ivUser       bbva user code
     * @param releaseId    release identifier
     * @param tagDTO       tag to validate
     * @param subsystemDTO info about subsystem tag
     * @param projectFiles project names
     * @return list with service validated
     */
    List<RVServiceValidationDTO> buildTagValidationByService(final String ivUser, final Integer releaseId, final RVTagDTO tagDTO, final RVSubsystemBaseDTO subsystemDTO, final List<String> projectFiles);

    /**
     * Validate if given tag is allowed to use multitag
     *
     * @param rvValidationDTO tag to validate
     */
    void validateAllowedMultitag(final RVValidationDTO rvValidationDTO);

}

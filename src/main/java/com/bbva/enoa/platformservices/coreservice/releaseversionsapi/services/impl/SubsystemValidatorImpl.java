package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates if subsystem tag is NOVA compliant.
 * <p>
 * Created by xe52580 on 01/03/2017.
 */
@Slf4j
@Service
public class SubsystemValidatorImpl implements ISubsystemValidator
{
    @Value("${nova.validations.maxServicesInSubsystem}")
    private int maxServices;

    /**
     * Tools service
     */
    @Autowired
    private IToolsClient toolsClient;

    @Override
    public void validateSubsystemTagDto(SubsystemTagDto tag, final SubsystemType subsystemType)
    {
        // Create list of validation errors.
        List<ValidationErrorDto> errorList = new ArrayList<>(Arrays.asList(tag.getValidationErrors()));

        // Check all NOVA rules.

        // Has Services?
        if (tag.getServices().length == 0)
        {
            addError(errorList, tag.getTagName(), Constants.NO_SERVICES_IN_TAG, Constants.NO_SERVICES_IN_TAG_MSG);
        }

        // Allowed num of services on a subsystem is not exceeded.
        this.maxServicesNotExceeded(tag, errorList);

        // Subsystem specific validations
        switch (subsystemType)
        {
            case EPHOENIX:
                this.validateEPhoenixSubsystem(tag, errorList);
                break;
            case LIBRARY:
                this.validateLibrarySubsystem(tag, errorList);
                break;
            case NOVA:
            case FRONTCAT:
                break;
            default:
                addError(errorList, tag.getTagName(), "WRONG_SUBSYSTEM_TYPE", "El tipo de subsistema indicado es inválido.");
        }

        // Add all errors - if any - to the service.
        tag.setValidationErrors(errorList.toArray(new ValidationErrorDto[errorList.size()]));
    }

    @Override
    public void checkReleaseSubsystems(final Release release)
    {
        List<TOSubsystemDTO> subsystemDTOList = this.toolsClient.getProductSubsystems(release.getProduct().getId(), false);

        if (subsystemDTOList.isEmpty())
        {
            throw new NovaException(ReleaseVersionError.getReleaseWithNoSubsystemsError(), "Product has no subsystems. Impossible to create a release version.");
        }
        else
        {
            log.debug("[ReleaseVersionsAPI] -> [checkReleaseSubsystems]: the product associated to the release name: [{}] have subsystems. Continue.", release.getName());
        }
    }


    //////////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    /**
     * Validate an EPhoenix subsystem
     *
     * @param tag       Subsystem tag.
     * @param errorList List of errors.
     */
    private void validateEPhoenixSubsystem(final SubsystemTagDto tag, List<ValidationErrorDto> errorList)
    {
        for (NewReleaseVersionServiceDto dto : tag.getServices())
        {
            if (dto.getServiceType() == null || (!dto.getServiceType().equalsIgnoreCase(ServiceType.EPHOENIX_BATCH.name())
                    && !dto.getServiceType().equalsIgnoreCase(ServiceType.EPHOENIX_ONLINE.name())))
            {
                log.warn("[{}] -> [{}]: El servicio {} es de tipo {}. No puede incluirse en un subsistema Ephoenix!", Constants.RELEASE_VERSIONS_API_NAME, "validateEPhoenixSubsystem", dto.getServiceName(), dto.getServiceType());
                addError(errorList, tag.getTagName(), "WRONG_EPHOENIX_SUBSYSTEM", "Un subsistema ePhoenix únicamente puede contener servicios ePhoenix");
                break;
            }
        }
    }

    /**
     * Validate a NOVA Library subsystem
     *
     * @param tag       Subsystem tag.
     * @param errorList List of errors.
     */
    private void validateLibrarySubsystem(final SubsystemTagDto tag, List<ValidationErrorDto> errorList)
    {
        for (NewReleaseVersionServiceDto dto : tag.getServices())
        {
            if (dto.getServiceType() == null || !ServiceType.isLibrary(ServiceType.getValueOf(dto.getServiceType())))
            {
                log.warn("[{}] -> [{}]: Service {} has type {}. It cannot be included on a NOVA library subsystem.", Constants.RELEASE_VERSIONS_API_NAME, "validateLibrarySubsystem", dto.getServiceName(), dto.getServiceType());
                addError(errorList, tag.getTagName(), Constants.WRONG_LIBRARY_SUBSYSTEM, Constants.WRONG_LIBRARY_SUBSYSTEM_MSG);
                break;
            }
        }
    }


    /**
     * Add an error to the list.
     *
     * @param errorList    List error will be added to.
     * @param systemName   System name.
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     */
    private void addError(
            List<ValidationErrorDto> errorList,
            String systemName,
            String errorCode,
            String errorMessage)
    {
        ValidationErrorDto error = new ValidationErrorDto();
        error.setCode(errorCode);
        error.setMessage(errorMessage);
        errorList.add(error);

        log.warn("[{}] -> [{}]: ", Constants.SUBSYSTEM_VALIDATOR, "addError", Constants.TAG_LOG_ERROR_MSG, systemName, errorMessage);
    }


    /**
     * Checks if the subsystem tag has more than the allowed services.
     *
     * @param tag       Subsystem tag.
     * @param errorList List of errors.
     */
    private void maxServicesNotExceeded(
            SubsystemTagDto tag,
            List<ValidationErrorDto> errorList)
    {
        if (tag.getServices().length > maxServices)
        {
            addError(errorList, tag.getTagName(), Constants.MAX_SERVICES_EXCEEDED, Constants.MAX_SERVICES_EXCEEDED_MSG);
        }
    }

}


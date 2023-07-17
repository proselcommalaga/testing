package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDtoPage;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentChangesDtoBuilder;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Builds an array of {@link DeploymentChangeDto} from the {@link DeploymentChange}
 * of a {@link DeploymentPlan}.
 */
@Slf4j
@Service
public class DeploymentChangesDtoBuilderImpl implements IDeploymentChangesDtoBuilder
{

    /**
     * Product user client
     */
    private final IProductUsersClient userClient;

    /**
     * Default constructor by param
     *
     * @param userClient product user client
     */
    @Autowired
    public DeploymentChangesDtoBuilderImpl(final IProductUsersClient userClient)
    {
        this.userClient = userClient;
    }

    @Override
    public DeploymentChangeDtoPage build(final Page<DeploymentChange> deploymentChangePageable)
    {
        DeploymentChangeDto[] deploymentChangeDtoArray = null;
        if (deploymentChangePageable != null && deploymentChangePageable.getNumberOfElements() > 0)
        {
            int i = 0;

            deploymentChangeDtoArray = new DeploymentChangeDto[deploymentChangePageable.getNumberOfElements()];

            for (final DeploymentChange deploymentChange : deploymentChangePageable.getContent())
            {
                deploymentChangeDtoArray[i] = this.buildDtoFrom(deploymentChange);

                i++;
            }
        }

        final DeploymentChangeDtoPage deploymentChangeDtoPage = new DeploymentChangeDtoPage();

        deploymentChangeDtoPage.setElements(deploymentChangeDtoArray);
        deploymentChangeDtoPage.setElementsCount((long) deploymentChangePageable.getNumberOfElements());
        deploymentChangeDtoPage.setTotalElementsCount(deploymentChangePageable.getTotalElements());
        deploymentChangeDtoPage.setPageNumber((long) deploymentChangePageable.getNumber());
        deploymentChangeDtoPage.setPageSize((long) deploymentChangePageable.getSize());

        // Log and return.
        log.debug("[DeploymentChangesDtoBuilderImpl] -> [DeploymentChangeDtoPage]: Built array of DeploymentChangeDto");

        return deploymentChangeDtoPage;
    }


    /**
     * Builds a DeploymentChangeDto from a {@link DeploymentChange}.
     *
     * @param deploymentChange {@link DeploymentChange}.
     * @return DeploymentChangeDto
     */
    private DeploymentChangeDto buildDtoFrom(final DeploymentChange deploymentChange)
    {
        // Create the DTO.
        DeploymentChangeDto dto = new DeploymentChangeDto();

        // Copy shared properties.
        BeanUtils.copyProperties(deploymentChange, dto);

        // Copy different properties.
        dto.setDate(deploymentChange.getCreationDate().getTimeInMillis());
        dto.setTypeChange(deploymentChange.getType().name());
        dto.setRefId(deploymentChange.getRefId());

        if (Strings.isNullOrEmpty(deploymentChange.getUserCode()))
        {
            log.error("[DeploymentChangesDtoBuilderImpl] -> [DeploymentChangeDto]: the user code is not provided: [null] for deployment deploymentChange: [{}]. Set N/A for this user full name", deploymentChange);
            dto.setUserCode("null");
            dto.setUserCompleteName("Usuario, Nombre y apellidos no disponible");
        }
        else
        {
            try
            {
                USUserDTO usUserDTO = this.userClient.getUser(deploymentChange.getUserCode(), new Errors());
                dto.setUserCode(usUserDTO.getUserCode());
                dto.setUserCompleteName(usUserDTO.getUserName() + " " + usUserDTO.getSurname1() + " " + usUserDTO.getSurname2());
            }
            catch (Errors errors)
            {
                log.warn("[DeploymentChangesDtoBuilderImpl] -> [DeploymentChangeDto]: the user code: [{}] does not found or not available from User Service. It could have be deleted. Deployment deploymentChange: [{}]. Error message: [{}]. Set N/A for this user",
                        deploymentChange.getUserCode(), deploymentChange, errors.getBodyExceptionMessage());
                dto.setUserCode(deploymentChange.getUserCode());
                dto.setUserCompleteName(deploymentChange.getUserCode() + " - Nombre y apellidos no disponible");
            }
        }

        // If deploymentChange is a configuration revision:
        ConfigurationRevision revision = deploymentChange.getConfigurationRevision();
        if (revision != null)
        {
            // Get the revision description.
            dto.setConfRevisionId(revision.getId());
            dto.setConfRevisionDesc(deploymentChange.getDescription());
        }
        // If not:
        else
        {
            // Use the deploymentChange description.
            dto.setConfRevisionDesc(deploymentChange.getDescription());
        }

        log.trace("[DeploymentChangesDtoBuilderImpl] -> [DeploymentChangeDto]: built DeploymentChangeDto with value: {}", dto);
        return dto;
    }
}

package com.bbva.enoa.platformservices.coreservice.packsapi.services;

import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.packsapi.exceptions.PackError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for building hardware pack DTOs.
 */
@Slf4j
@Service
public class HardwarePackDtoBuilder
{
    private IBudgetsService budgetsService;

    /**
     * All args constructor for dependency injection
     *
     * @param budgetsService BudgetsService dependency
     */
    @Autowired
    public HardwarePackDtoBuilder(IBudgetsService budgetsService)
    {
        this.budgetsService = budgetsService;
    }

    /**
     * Builds a {@link HardwarePackDto} from a {@link HardwarePack} entity.
     *
     * @param pack The source {@link HardwarePack}.
     * @return HardwarePackDto.
     */
    public HardwarePackDto build(HardwarePack pack)
    {
        HardwarePackDto dto = new HardwarePackDto();
        BeanUtils.copyProperties(pack, dto);

        // Set different properties.
        dto.setCreationDate(pack.getCreationDate().getTimeInMillis());
        dto.setHardwarePackType(pack.getHardwarePackType().getHardwarePackType());
        dto.setMinJavaVersionSupported(pack.getMinJavaVersionSupported());

        try
        {
            double packPrice = budgetsService.getHardwarePackPrice(pack.getId());
            dto.setPrice(packPrice);

            log.debug(
                    "Successfully built a HardwarePackDto for pack {} with this contents {}",
                    pack.getId(),
                    dto);

            return dto;
        }
        catch (NovaException ex)
        {
            throw new NovaException(PackError.getUnexpectedError(), ex,
                    MessageFormat.format("Unable to get pack {0} price", pack.getId()));
        }
    }


    /**
     * Builds an array of {@link HardwarePackDto} from a list of {@link HardwarePack} entities.
     *
     * @param packList List of {@link HardwarePack}.
     * @return HardwarePackDto[]
     */
    public HardwarePackDto[] build(List<HardwarePack> packList)
    {
        List<HardwarePackDto> dtoList = packList.stream()
                .map(this::build)
                .collect(Collectors.toList());

        log.debug("Successfully built an array of HardwarePackDto");

        return dtoList.toArray(HardwarePackDto[]::new);

    }

}

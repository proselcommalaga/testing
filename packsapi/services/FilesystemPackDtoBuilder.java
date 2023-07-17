package com.bbva.enoa.platformservices.coreservice.packsapi.services;

import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
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
public class FilesystemPackDtoBuilder
{

    private IBudgetsService budgetsService;

    /**
     * All args constructor for dependency injection
     *
     * @param budgetsService BudgetsService dependency
     */
    @Autowired
    public FilesystemPackDtoBuilder(IBudgetsService budgetsService)
    {
        this.budgetsService = budgetsService;
    }

    /**
     * Builds a {@link FilesystemPackDto} from a {@link FilesystemPack} entity.
     *
     * @param pack The source {@link FilesystemPack}.
     * @return FilesystemPackDto.
     */
    public FilesystemPackDto build(FilesystemPack pack)
    {
        FilesystemPackDto dto = new FilesystemPackDto();
        BeanUtils.copyProperties(pack, dto);

        dto.setCreationDate(pack.getCreationDate().getTimeInMillis());
        dto.setEnvironment(pack.getEnvironment());
        dto.setSizeMB(pack.getSizeMB());
        dto.setFilesystemType(pack.getFilesystemType().getFileSystemType());

        try
        {
            double packPrice = budgetsService.getFilesystemPackPrice(pack.getId());
            dto.setPrice(packPrice);

            log.debug(
                    "Successfully built a FilesystemPackDto for pack {} with this contents {}",
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
     * Builds an array of {@link FilesystemPackDto} from a list of {@link FilesystemPack} entities.
     *
     * @param packList List of {@link FilesystemPackDto}.
     * @return FilesystemPackDto[]
     */
    public FilesystemPackDto[] build(List<FilesystemPack> packList)
    {
        List<FilesystemPackDto> dtoList = packList.stream()
                .map(this::build)
                .collect(Collectors.toList());

        log.debug("Successfully built an array of HardwarePackDto");

        return dtoList.toArray(FilesystemPackDto[]::new);
    }

}

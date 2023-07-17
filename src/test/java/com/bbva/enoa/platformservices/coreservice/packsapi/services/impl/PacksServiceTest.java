package com.bbva.enoa.platformservices.coreservice.packsapi.services.impl;

import com.bbva.enoa.apirestgen.packsapi.model.BrokerPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerPackRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemPackRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.HardwarePackRepository;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.BrokerPackDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.FilesystemPackDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.HardwarePackDtoBuilder;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class PacksServiceTest
{
    @InjectMocks
    private PacksService packsService;

    @Mock
    private HardwarePackRepository hardwarePackRepository;
    @Mock
    private FilesystemPackRepository filesystemPackRepository;
    @Mock
    private HardwarePackDtoBuilder hardwarePackDtoBuilder;
    @Mock
    private FilesystemPackDtoBuilder filesystemPackDtoBuilder;
    @Mock
    private BrokerPackRepository brokerPackRepository;
    @Mock
    private BrokerPackDtoBuilder brokerPackDtoBuilder;

    private NovaMetadata novaMetadata = new NovaMetadata();


    @BeforeEach
    public void setUp() throws Exception
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(new String[]{ "CODE" }));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));

        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getOneHardwarePack()
    {
        //WHEN
        Integer id = RandomUtils.nextInt(1, 10);
        HardwarePack pack = new HardwarePack();
        Optional<HardwarePack> optionalPack = Optional.of(pack);
        when(this.hardwarePackRepository.findById(id)).thenReturn(optionalPack);

        HardwarePackDto dto = new HardwarePackDto();
        when(this.hardwarePackDtoBuilder.build(pack)).thenReturn(dto);

        //THEN
        HardwarePackDto response = this.packsService.getOneHardwarePack(id);

        //VERIFY
        assertEquals(dto, response);
    }
    @Test
    public void getOneHardwarePackError()
    {
        Integer id = RandomUtils.nextInt(1, 10);
        Optional<HardwarePack> optionalPack = Optional.empty();
        when(this.hardwarePackRepository.findById(id)).thenReturn(optionalPack);

        assertThrows(NovaException.class, () ->
            this.packsService.getOneHardwarePack(id)
        );
    }



    @Test
    public void getOneFilesystemPack()
    {
        //WHEN
        Integer id = RandomUtils.nextInt(1, 10);
        FilesystemPack pack = new FilesystemPack();
        Optional<FilesystemPack> optionalPack = Optional.of(pack);
        when(this.filesystemPackRepository.findById(id)).thenReturn(optionalPack);

        FilesystemPackDto dto = new FilesystemPackDto();
        when(this.filesystemPackDtoBuilder.build(pack)).thenReturn(dto);

        //THEN
        FilesystemPackDto response = this.packsService.getOneFilesystemPack(id);

        //VERIFY
        assertEquals(dto, response);
    }

    @Test
    public void getOneFilesystemPackError()
    {
        Integer id = RandomUtils.nextInt(1, 10);
        Optional<FilesystemPack> optionalPack = Optional.empty();
        when(this.filesystemPackRepository.findById(id)).thenReturn(optionalPack);

        assertThrows(NovaException.class, () ->
                this.packsService.getOneFilesystemPack(id)
        );
    }

    @Test
    public void getFilesystemPacksWithoutFilesystemType()
    {
        //WHEN
        List<FilesystemPack> packs = new ArrayList<>();
        when(this.filesystemPackRepository.findAll()).thenReturn(packs);

        FilesystemPackDto[] dtoArray = new FilesystemPackDto[0];
        when(this.filesystemPackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        FilesystemPackDto[] response = this.packsService.getFilesystemPacks(null);

        //VERIFY
        assertEquals(dtoArray, response);
    }

    @Test
    public void getFilesystemPacksWithFilesystemType()
    {
        //WHEN
        String filesystemType = FilesystemType.values()[RandomUtils.nextInt(0, FilesystemType.values().length)].getFileSystemType();
        FilesystemType filesystemTypeToFilter = FilesystemType.valueOf(filesystemType);
        List<FilesystemPack> packs = new ArrayList<>();
        when(this.filesystemPackRepository.findAllByFilesystemType(filesystemTypeToFilter)).thenReturn(packs);

        FilesystemPackDto[] dtoArray = new FilesystemPackDto[0];
        when(this.filesystemPackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        FilesystemPackDto[] response = this.packsService.getFilesystemPacks(filesystemType);

        //VERIFY
        assertEquals(dtoArray, response);
    }

    @Test
    public void getHardwarePacksWithoutHardwarePackType()
    {
        //WHEN
        List<HardwarePack> packs = new ArrayList<>();
        when(this.hardwarePackRepository.findAll()).thenReturn(packs);

        HardwarePackDto[] dtoArray = new HardwarePackDto[0];
        when(this.hardwarePackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        HardwarePackDto[] response = this.packsService.getHardwarePacks("11.0.11", null);

        //VERIFY
        assertEquals(dtoArray, response);
    }

    @Test
    public void getHardwarePacksWithHardwarePackType()
    {
        //WHEN
        String hardwarePackType = HardwarePackType.values()[RandomUtils.nextInt(0, HardwarePackType.values().length)].getHardwarePackType();
        HardwarePackType hardwarePackTypeFilter = HardwarePackType.valueOf(hardwarePackType);
        List<HardwarePack> packs = new ArrayList<>();
        when(this.hardwarePackRepository.findAllByHardwarePackType(hardwarePackTypeFilter)).thenReturn(packs);

        HardwarePackDto[] dtoArray = new HardwarePackDto[0];
        when(this.hardwarePackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        HardwarePackDto[] response = this.packsService.getHardwarePacks("11.0.11", hardwarePackType);

        //VERIFY
        assertEquals(dtoArray, response);
    }

    @Test
    public void getOneBrokerPack()
    {
        //WHEN
        Integer id = RandomUtils.nextInt(1, 10);
        BrokerPack pack = new BrokerPack();
        Optional<BrokerPack> optionalPack = Optional.of(pack);
        when(this.brokerPackRepository.findById(id)).thenReturn(optionalPack);

        BrokerPackDto dto = new BrokerPackDto();
        when(this.brokerPackDtoBuilder.build(pack)).thenReturn(dto);

        //THEN
        BrokerPackDto response = this.packsService.getOneBrokerPack(id);

        //VERIFY
        assertEquals(dto, response);
    }

    @Test
    public void getOneBrokerPackError()
    {
        Integer id = RandomUtils.nextInt(1, 10);
        Optional<BrokerPack> optionalPack = Optional.empty();
        when(this.brokerPackRepository.findById(id)).thenReturn(optionalPack);

        assertThrows(NovaException.class, () ->
                this.packsService.getOneBrokerPack(id)
        );
    }

    @Test
    public void getBrokerPacksWithoutHardwareType()
    {
        //WHEN
        List<BrokerPack> packs = new ArrayList<>();
        when(this.brokerPackRepository.findAll()).thenReturn(packs);

        BrokerPackDto[] dtoArray = new BrokerPackDto[0];
        when(this.brokerPackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        BrokerPackDto[] response = this.packsService.getBrokerPacks(null);

        //VERIFY
        assertEquals(dtoArray, response);
    }

    @Test
    public void getBrokerPacksWithHardwareType()
    {
        String hardwarePackType = HardwarePackType.values()[RandomUtils.nextInt(0, HardwarePackType.values().length)].getHardwarePackType();
        HardwarePackType hardwarePackTypeFilter = HardwarePackType.valueOf(hardwarePackType);
        List<BrokerPack> packs = new ArrayList<>();
        when(this.brokerPackRepository.findAllByHardwarePackType(hardwarePackTypeFilter)).thenReturn(packs);

        BrokerPackDto[] dtoArray = new BrokerPackDto[0];
        when(this.brokerPackDtoBuilder.build(packs)).thenReturn(dtoArray);

        //THEN
        BrokerPackDto[] response = this.packsService.getBrokerPacks(hardwarePackType);

        //VERIFY
        assertEquals(dtoArray, response);
    }

}

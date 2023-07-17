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
import com.bbva.enoa.platformservices.coreservice.packsapi.exceptions.PackError;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.BrokerPackDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.FilesystemPackDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.HardwarePackDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.interfaces.IPacksService;
import com.bbva.enoa.platformservices.coreservice.packsapi.util.JVMConverter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PacksService implements IPacksService
{
    private final HardwarePackRepository hardwarePackRepository;

    private final FilesystemPackRepository filesystemPackRepository;

    private final HardwarePackDtoBuilder hardwarePackDtoBuilder;

    private final FilesystemPackDtoBuilder filesystemPackDtoBuilder;

    private final BrokerPackRepository brokerPackRepository;

    private final BrokerPackDtoBuilder brokerPackDtoBuilder;


    /**
     * @param hardwarePackRepository   HardwarePack repository
     * @param filesystemPackRepository FilesystemPack repository
     * @param hardwarePackDtoBuilder   HardwarePack DTO builder
     * @param filesystemPackDtoBuilder FilesystemPack DTO builder
     * @param brokerPackRepository  BrokerPack repository
     * @param brokerPackDtoBuilder Broker DTO builder
     */
    @Autowired
    public PacksService(final HardwarePackRepository hardwarePackRepository, final FilesystemPackRepository filesystemPackRepository,
                        final HardwarePackDtoBuilder hardwarePackDtoBuilder, final FilesystemPackDtoBuilder filesystemPackDtoBuilder,
                        final BrokerPackRepository brokerPackRepository, final BrokerPackDtoBuilder brokerPackDtoBuilder)
    {
        this.hardwarePackRepository = hardwarePackRepository;
        this.filesystemPackRepository = filesystemPackRepository;
        this.hardwarePackDtoBuilder = hardwarePackDtoBuilder;
        this.filesystemPackDtoBuilder = filesystemPackDtoBuilder;
        this.brokerPackRepository = brokerPackRepository;
        this.brokerPackDtoBuilder = brokerPackDtoBuilder;
    }

    @Override
    public HardwarePackDto getOneHardwarePack(Integer packId)
    {
        HardwarePack pack = this.hardwarePackRepository.findById(packId).orElseThrow(() -> new NovaException(PackError.getNoSuchHardwarePackError(),
                MessageFormat.format("Pack {0} not found when getting one hardware pack", packId)));

        return this.hardwarePackDtoBuilder.build(pack);
    }

    @Override
    public FilesystemPackDto[] getFilesystemPacks(String filesystemType)
    {
        final List<FilesystemPack> packs;

        final Optional<String> optFilesystemType = Optional.ofNullable(filesystemType);

        if (optFilesystemType.isPresent())
        {
            // If filesystem type is specified, only packs corresponding to the specified filesystem type are returned
            FilesystemType filesystemTypeToFilter = optFilesystemType
                    .map(FilesystemType::valueOf).get();

            // Get the packs corresponding to filesystem type
            packs = this.filesystemPackRepository.findAllByFilesystemType(filesystemTypeToFilter);

        }
        else
        {
            // If no filesystem type is specified, all packs are returned
            packs = this.filesystemPackRepository.findAll();
        }

        return this.filesystemPackDtoBuilder.build(packs);
    }

    @Override
    public FilesystemPackDto getOneFilesystemPack(Integer packId)
    {
        FilesystemPack pack = this.filesystemPackRepository.findById(packId).orElseThrow(() -> new NovaException(PackError.getNoSuchFilesystemPackError(),
                MessageFormat.format("Pack {0} not found when getting one filesystem pack", packId)));

        return this.filesystemPackDtoBuilder.build(pack);
    }

    @Override
    public HardwarePackDto[] getHardwarePacks(final String jvmVersion, final String hardwarePackType)
    {
        final List<HardwarePack> packs;

        final Optional<String> optHardwarePackType = Optional.ofNullable(hardwarePackType);

        final Integer javaMayorVersion = JVMConverter.convertJVMVersionToJavaMayorVersion(jvmVersion);

        if (optHardwarePackType.isPresent())
        {
            HardwarePackType hardwarePackTypeFilter = optHardwarePackType.map(HardwarePackType::valueOf).get();

            // If hardware type is specified, only packs corresponding to the specified hardware type and java mayor version filter
            packs = this.hardwarePackRepository.findAllByHardwarePackType(hardwarePackTypeFilter).stream()
                    .filter(hardwarePack -> hardwarePack.getMinJavaVersionSupported() <= javaMayorVersion).collect(Collectors.toList());
        }
        else
        {
            // If no hardware pack type, returns all packs filter by java mayor version
            packs = this.hardwarePackRepository.findAllByJavaVersion(javaMayorVersion);
            log.debug("[PacksService] -> [getHardwarePacks] --- Retrieved from database all legacy hardware packs: [{}]", packs);
        }

        return this.hardwarePackDtoBuilder.build(packs);
    }

    @Override
    public HardwarePackDto[] getAllHardwarePacks()
    {
        final List<HardwarePack> packs = this.hardwarePackRepository.findAll();
        // William final List<HardwarePack> packs2 = this.hardwarePackRepository.findAllByNotHardwarePackType(HardwarePackType.PACK_NOVA_BEHAVIOR);
        //final List<HardwarePack> packsWithoutBehavior = packs.stream().filter(pack->!HardwarePackType.PACK_NOVA_BEHAVIOR.equals(pack.getHardwarePackType())).collect(Collectors.toList());
        //PACK_NOVA_BEHAVIOR
        log.debug("[PacksService] -> [getAllHardwarePacks] --- Retrieved from database all hardware packs: [{}]", packs);
        //return this.hardwarePackDtoBuilder.build(packsWithoutBehavior);
        return this.hardwarePackDtoBuilder.build(packs); //William
    }

    @Override
    public BrokerPackDto[] getBrokerPacks(final String hardwarePackType)
    {
        final List<BrokerPack> packs;

        final Optional<String> optHardwarePackType = Optional.ofNullable(hardwarePackType);

        if (optHardwarePackType.isPresent())
        {
            HardwarePackType hardwarePackTypeFilter = optHardwarePackType.map(HardwarePackType::valueOf).get();

            // If hardware type is specified, only packs corresponding to the specified hardware type
            packs = this.brokerPackRepository.findAllByHardwarePackType(hardwarePackTypeFilter).stream().collect(Collectors.toList());
        }
        else
        {
            // If no hardware pack type, returns all packs
            packs = this.brokerPackRepository.findAll();
        }

        return this.brokerPackDtoBuilder.build(packs);
    }

    @Override
    public BrokerPackDto getOneBrokerPack(Integer packId) {
        BrokerPack pack = this.brokerPackRepository.findById(packId).orElseThrow(() -> new NovaException(PackError.getNoSuchBrokerPackError(),
                MessageFormat.format("Pack {0} not found when getting one broker pack", packId)));

        return this.brokerPackDtoBuilder.build(pack);
    }
}

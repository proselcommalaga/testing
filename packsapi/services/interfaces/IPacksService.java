package com.bbva.enoa.platformservices.coreservice.packsapi.services.interfaces;

import com.bbva.enoa.apirestgen.packsapi.model.BrokerPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;

public interface IPacksService
{
    /**
     * @param packId The ID of the pack to get.
     * @return HardwarePack
     */
    HardwarePackDto getOneHardwarePack(final Integer packId);

    /**
     * @param filesystemType filesystem
     * @return FilesystemPackDto[]
     */
    FilesystemPackDto[] getFilesystemPacks(String filesystemType);

    /**
     * @param packId The ID of the {@link FilesystemPack} to get.
     * @return FilesystemPackDto
     */
    FilesystemPackDto getOneFilesystemPack(final Integer packId);

    /**
     * This method has several paths:
     *
     * <ol>
     * <li>The dashboard <strong>sends the hardware pack type (PACK_ETHER or PACK_NOVA)</strong> and <strong>doesn't send the JVM version (at the moment, Java 8 services)</strong>:<br>
     *      In this path we will send the hardware packs associated with the type received, filtered by the packs associated with the legacy version (Java 8).
     * <br><br></li>
     * <li>The dashboard <strong>sends the hardware pack type (PACK_ETHER or PACK_NOVA) and the JVM version</strong>:<br>
     *      In this path we will send the hardware packs associated with the type received, filtered by the packs associated with the JVM version received from the dashboard.
     * <br><br></li>
     * <li>The dashboard <strong>doesn't send the hardware pack type (PACK_ETHER or PACK_NOVA)</strong> and <strong>doesn't send the JVM version (at the moment, Java 8 services)</strong>:<br>
     *      In this path we will send the hardware packs lower or equals to the JVM version 8, known as "legacy packs".
     * <br><br></li>
     * <li>The dashboard <strong>doesn't send the hardware pack type (PACK_ETHER or PACK_NOVA)</strong> and <strong>sends the JVM version (at the moment, Java 11 services: "11.0.11")</strong>:<br>
     *      In this path we will send the hardware packs lower or equals to the JVM version received from the dashboard.
     * <br><br></li>
     * </ol>
     *
     * @param jvmVersion       service's JVM version. This value is going to be parsed into a Integer value that represents the Java's version.
     *                         At the moment, we will receive from the dashboard a null value for the Java 8 JVMs, BUT in case that we'd receive the correct JVM (not null), we can manage it (we can parse the "1.8.121" into the Java 8 value).
     *                         For the services in JVM 11 (Zulu or Corretto, at the moment), we parse the "11.0.11" JVM version into 11 value representing the Java version.
     * @param hardwarePackType hardware pack type. It can be PACK_NOVA or PACK_ETHER.
     * @return HardwarePackDto[] transformed entities into DTOs
     */
    HardwarePackDto[] getHardwarePacks(final String jvmVersion, final String hardwarePackType);

    /**
     * Retrieves all persisted hardware packs stored in database. So in this service, we don't filter by any hardware pack type or Java associated version.
     *
     * @return HardwarePackDto[] transformed entities into DTOs
     */
    HardwarePackDto[] getAllHardwarePacks();

    /**
     * @param hardwarePackType
     * @return
     */
    BrokerPackDto[] getBrokerPacks(final String hardwarePackType);

    /**
     * @param packId The ID of the {@link FilesystemPack} to get.
     * @return FilesystemPackDto
     */
    BrokerPackDto getOneBrokerPack(final Integer packId);
}

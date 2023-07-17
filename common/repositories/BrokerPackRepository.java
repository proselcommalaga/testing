package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Broker Hardware Pack.
 */
public interface BrokerPackRepository extends JpaRepository<BrokerPack, Integer>
{

    /**
     * Returns the broker pack having the unique code.
     *
     * @param code Pack code.
     * @return FilesystemPack.
     */
    BrokerPack findByCode(String code);

    /**
     * Returns the list of broker hardware packs corresponding to a hardware pack type
     *
     * @param hardwarePackTypeFilter {@link HardwarePackType} to filter
     * @return List of {@link HardwarePack}
     */
    List<BrokerPack> findAllByHardwarePackType(HardwarePackType hardwarePackTypeFilter);
}

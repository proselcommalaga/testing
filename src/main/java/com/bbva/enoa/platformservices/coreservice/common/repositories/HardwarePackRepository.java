package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for HardwarePack.
 */
public interface HardwarePackRepository extends JpaRepository<HardwarePack, Integer>
{

    /**
     * Returns the hardware pack having the unique code.
     *
     * @param code Pack code.
     * @return HardwarePack.
     */
    HardwarePack findByCode(String code);

    /**
     * Returns the list of hardware packs corresponding to a hardware pack type
     *
     * @param hardwarePackTypeFilter {@link HardwarePackType} to filter
     * @return List of {@link HardwarePack}
     */
    List<HardwarePack> findAllByHardwarePackType(HardwarePackType hardwarePackTypeFilter);

    // William List<HardwarePack> findAllByNotHardwarePackType(HardwarePackType hardwarePackTypeFiler);

    /**
     * Returns the list of hardware packs with a version lower or equals to the Java version passed by parameter.
     * @param javaVersion version of java run environment
     * @return List of {@link HardwarePack}
     */
    @Query("select h from HardwarePack h where h.minJavaVersionSupported <= :javaVersion")
    List<HardwarePack> findAllByJavaVersion(@Param("javaVersion") Integer javaVersion);

}

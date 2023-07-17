package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for HardwarePack.
 */
public interface FilesystemPackRepository extends JpaRepository<FilesystemPack, Integer>
{

    /**
     * Returns the filesystem pack having the unique code.
     *
     * @param code Pack code.
     * @return FilesystemPack.
     */
    FilesystemPack findByCode(String code);

    /**
     * Returns the list of filesystem packs corresponding to a filesystem type
     * @param filesystemType FilesystemType to filter
     * @return List of FilesystemPack
     */
    List<FilesystemPack> findAllByFilesystemType(FilesystemType filesystemType);
}

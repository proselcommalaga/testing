package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;

import java.util.List;

/**
 * mAPPER FOR DTO2ENTITIES AND VICEVERSA FOR FILESYSTEMSAPI
 * 
 * @author XE63267
 *
 */
public interface IFilesystemsBuilder
{
    /**
     * Builds an array of {@link FilesystemDto} from the {@link Filesystem}
     * of a product.
     *
     * @param filesystems {@link Filesystem}
     * @return FilesystemDto[]
     */
    FilesystemDto[] buildFilesystemDTOArray(List<Filesystem> filesystems);

    /**
     * Builds a {@link FilesystemDto} from a {@link Filesystem}.
     *
     * @param filesystem {@link Filesystem}
     * @return FilesystemDto
     */
    FilesystemDto buildFilesystemDTO(Filesystem filesystem);
}

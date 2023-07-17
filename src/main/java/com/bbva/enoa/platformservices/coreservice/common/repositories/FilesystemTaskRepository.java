package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.todotask.entities.FilesystemTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Filesystem Task repository
 */
public interface FilesystemTaskRepository extends JpaRepository<FilesystemTask, Integer>
{
    /**
     * Find all filesystem task filter by filesystem id
     *
     * @param filesystemId the filesystem Id
     * @return list of filesystem tasks for the give filesystem id
     */
    List<FilesystemTask> findByFilesystemId(Integer filesystemId);
}

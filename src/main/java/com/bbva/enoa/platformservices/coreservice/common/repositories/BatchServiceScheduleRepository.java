package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.BatchServiceSchedule;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * BatchServiceSchedule repository.
 */
@Transactional(readOnly = true)
public interface BatchServiceScheduleRepository extends JpaRepository<BatchServiceSchedule, Integer>
{
    /**
     * Find active batch service schedules by batch service
     *
     * @param batchService batch service
     * @return batch service schedule
     */
    BatchServiceSchedule findByIsActiveTrueAndBatchService(DeploymentService batchService);
}

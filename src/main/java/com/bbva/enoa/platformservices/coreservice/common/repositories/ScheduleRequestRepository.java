package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.batch.entities.ScheduleRequest;
import com.bbva.enoa.datamodel.model.batch.enumerates.ScheduleReqStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, Long>
{
    /**
     * Find by the ID of a "Batch Schedule" document.
     *
     * @param batchScheduleDocumentId The ID of a "Batch Schedule" document.
     * @return A List of entities.
     */
    List<ScheduleRequest> findByBatchScheduleDocumentId(Integer batchScheduleDocumentId);

    /**
     * Find by the ID of a Product, and not in a given status.
     *
     * @param productId             The ID of a the Product.
     * @param scheduleReqStatusList The given status.
     * @return A List of entities.
     */
    List<ScheduleRequest> findByProductIdAndStatusNotIn(Integer productId, List<ScheduleReqStatus> scheduleReqStatusList);

    /**
     * Find by the ID of a Product
     *
     * @param productId The ID of a the Product.
     * @return A List of entities.
     */
    List<ScheduleRequest> findByProductId(Integer productId);
}

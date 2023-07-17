package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Behavior version repository.
 */
public interface BehaviorVersionRepository extends JpaRepository<BehaviorVersion, Integer>
{

    /**
     * Count number of behavior versions by product id and not with status
     *
     * @param productId Product to count release versions for
     * @param status    Status to exclude from count
     * @return Number of release versions found
     */
    int countByProductIdAndStatusNot(final Integer productId, final BehaviorVersionStatus status);

    /**
     * Count by productId and status
     *
     * @param productId product id
     * @param status    status
     * @return a count by product id and status
     */
    int countByProductIdAndStatus(final Integer productId, final BehaviorVersionStatus status);

}
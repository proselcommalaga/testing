package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Calendar;
import java.util.List;

public interface ServiceExecutionHistoryRepository extends JpaRepository<ServiceExecutionHistory, ServiceExecutionHistoryId>
{
    /**
     * Returns a list of versioned final names (finalName:version) of recently started services.
     *
     * @param versionedFinalNames A list of versioned final names for filtering purposes.
     * @param environment         The environment in which the service was started.
     * @param startDate           The minimum service start date.
     * @return A list of versioned final names (finalName:version) of recently started services.
     */
    @Query("select concat(seh.id.finalName, ':', seh.id.version) " +
            "from com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory as seh " +
            "where concat(seh.id.finalName, ':', seh.id.version) in (:versionedFinalNames) " +
            "and seh.id.environment = :environment " +
            "and seh.lastExecution >= :startDate")
    List<String> findExecutedVersionedFinalNamesMatching(List<String> versionedFinalNames, String environment, Calendar startDate);

    /**
     * Returns all service executions started before a given date.
     *
     * @param startDateCalendar The minimum start date.
     * @return All service executions started before a given date.
     */
    List<ServiceExecutionHistory> findByLastExecutionBefore(Calendar startDateCalendar);

    /**
     * Returns all service executions with related deployment service with a started deployment instance.
     *
     * @param deploymentServiceIds A list of deployment service ids for filtering purposes.
     * @param started              Always true. Search only by started instances.
     * @return All service executions with related deployment service with a started deployment instance.
     */
    @Query("select seh " +
            "from com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory as seh " +
            "where exists (" +
            "   select 1 " +
            "   from com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService as ds " +
            "   join ds.instances as inst " +
            "   where ds.id in (:deploymentServiceIds) " +
            "   and seh.deploymentServiceId = ds.id " +
            "   and inst.started = :started" +
            ") ")
    List<ServiceExecutionHistory> findStillRunningServices(List<Integer> deploymentServiceIds, Boolean started);
}

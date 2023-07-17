package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * The interface Filesystem alert repository.
 */
public interface FilesystemAlertRepository extends JpaRepository<FilesystemAlert, Integer>
{



    /**
     * Gets all filesystem of a product on an environment.
     *
     * @param filesystemCode {@link Product} ID for the filesystem is generating the alert
     * @return The filesystem alert information filesystem.
     */
    Optional<FilesystemAlert> findByFilesystemCodeId(int filesystemCode);


    /**
     * Update filesystem alert configuration filesystem alert.
     *
     * @param alertPercentage   the alert percentage
     * @param isActive          the is active
     * @param isMail            the is mail
     * @param isPatrol          the is patrol
     * @param timeBetweenAlerts the time between alerts
     * @param filesystemId      the filesystem alert id
     */
    @Modifying
    @Query(value = "UPDATE FilesystemAlert fsa SET alert_percentage=:alertPercentage, is_active=:isActive, is_mail=:isMail, is_patrol=:isPatrol, time_between_alerts=:timeBetweenAlerts, " +
            "email_addresses=:emailAddresses WHERE fsa.filesystemCode.id=:filesystemId")
    void updateFilesystemAlertConfiguration(@Param("alertPercentage") Integer alertPercentage,@Param("isActive") Boolean isActive, @Param("isMail") Boolean isMail,
                                            @Param("isPatrol") Boolean isPatrol , @Param("timeBetweenAlerts") Integer timeBetweenAlerts, @Param("emailAddresses") String emailAddresses,
                                            @Param("filesystemId") Integer filesystemId);
    }

package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import com.bbva.enoa.apirestgen.brokersapi.model.RateDTO;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class BrokerUtils
{
    /**
     * Get the corresponding FilesystemType value for a given Platform value
     *
     * @param platform input Platform value
     * @return output FilesystemType value
     */
    public static FilesystemType getFilesystemTypeForPlatform(Platform platform)
    {
        if (platform == null)
        {
            throw new NovaException(BrokerError.getNotValidPlatformError(null));
        }
        switch (platform)
        {
            case ETHER:
                return FilesystemType.FILESYSTEM_ETHER;
            case NOVA:
            default:
                return FilesystemType.FILESYSTEM;
        }
    }

    /**
     * Get the corresponding HardwarePackType value for a given Platform value
     *
     * @param platform input Platform value
     * @return output HardwarePackType value
     */
    public static HardwarePackType getHardwarePackTypeForPlatform(Platform platform)
    {
        if (platform == null)
        {
            throw new NovaException(BrokerError.getNotValidPlatformError(null));
        }
        switch (platform)
        {
            case ETHER:
                return HardwarePackType.PACK_ETHER;
            case NOVA:
            default:
                return HardwarePackType.PACK_NOVA;
        }
    }

    /**
     * Generate a ISO String representation of the datetime represented by a Calendar object (from entities to DTOs)
     *
     * @param calendar Calendar object representing specific datetime
     * @return datetime as ISO-formatted String
     */
    public static String formatCalendar(Calendar calendar)
    {
        if (calendar == null)
        {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Calculate rate in messages per minute
     *
     * @param rate rate to transform
     * @return rate in messages per minute
     */
    public static double getRateInMessagesPerMinute(RateDTO rate)
    {
        switch (rate.getUnit())
        {
            case "s":
            case "seg":
                return rate.getValue() * 60;
            case "m":
            case "min":
                return rate.getValue();
            case "h":
            case "hour":
                return rate.getValue() / 60;
            default:
                return 0d;
        }
    }

    private BrokerUtils()
    {
        throw new IllegalStateException("Utility class");
    }
}

package com.bbva.enoa.platformservices.coreservice.filesystemsapi.util;

import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utilities of the Filesystems API
 */
public class Utils
{
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Gets size in kb for a given value(Ex: 512M, 1G, 123k, 0).
     *
     * @param usedFullString the used full string
     * @return the size in kb
     */
    public static Integer getSizeInKB(final String usedFullString)
    {
        if (StringUtils.isBlank(usedFullString))
        {
            throw new NovaException(FilesystemsError.getInvalidSizeError(usedFullString));
        }

        LOG.debug("[Filesystem Utils] -> [getSizeInKB]: Getting size in KBs from size in string: [{}]", usedFullString);

        // Get integer value from string
        String usedSizeString = usedFullString.replaceAll("\\D+", "");

        if (StringUtils.isBlank(usedSizeString))
        {
            throw new NovaException(FilesystemsError.getInvalidSizeError(usedFullString));
        }

        int result = Integer.parseInt(usedSizeString);

        // Get last letter that define size units
        String sizeUnit = usedFullString.replaceAll("[^A-Za-z]+", "");

        LOG.debug("[Filesystem Utils] -> [getSizeInKB]: usedSizeString: [{}], sizeUnit: [{}]", usedSizeString, sizeUnit);
        if (result != 0)
        {
            switch (sizeUnit)
            {
                case "K":
                    break;
                case "M":
                    result = result * 1000;
                    break;
                case "G":
                    result = result * 1000 * 1000;
                    break;
                default:
                    LOG.error("[Filesystem Utils] -> [getSizeInKB]: Unrecognizable size unit: [{}]KB", result);
                    throw new NovaException(FilesystemsError.getUnrecognizableSizeUnitError(sizeUnit));
            }
        }

        LOG.debug("[Filesystem Utils] -> [getSizeInKB]: Obtained size in KBs, result: [{}]KB", result);
        return result;
    }
}

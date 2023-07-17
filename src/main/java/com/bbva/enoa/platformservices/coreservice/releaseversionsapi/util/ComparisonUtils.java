package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.UNSUPPORTED_FORMAT_VERSION;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.UNSUPPORTED_FORMAT_VERSION_MSG;

@Slf4j
public class ComparisonUtils
{

    /**
     * Version comparator
     *
     * @param v1 version 1
     * @param v2 version 2
     * @return 1: if v1 is equal or higher to v2
     * 0: if v2 is higher
     * -1: if error
     */
    public static int compareVersion(String v1, String v2, String serviceName, String typeVersion, List<ValidationErrorDto> errorList)
    {

        if (v1 == null || v2 == null)
        {
            return compareVersionError(v1, v2, serviceName, typeVersion, errorList, "Some provided version is null");
        }
        String[] v1Split = v1.split("\\.");
        String[] versionNova = v2.split("\\.");

        if (versionNova.length > v1Split.length)
        {
            return compareVersionError(v1, v2, serviceName, typeVersion, errorList, "NOVA Version is higher than version provided");
        }

        try
        {
            for (int i = 0; i < versionNova.length; i++)
            {
                // If they are different return 0 or 1, otherwise continue
                if (Integer.parseInt(versionNova[i]) > Integer.parseInt(v1Split[i]))
                {
                    return 0;
                }
                else if (Integer.parseInt(versionNova[i]) < Integer.parseInt(v1Split[i]))
                {
                    return 1;
                }
            }
        }
        catch (NumberFormatException e)
        {
            return compareVersionError(v1, v2, serviceName, typeVersion, errorList, e.getMessage());
        }

        return 1;
    }

    /**
     * Compare two versions
     *
     * @param version1 version 1
     * @param version2 version 2
     * @return 0 if version are equal, positive number if version1 is greater than version2 and negative number if version1 is less than version2
     * @throws NumberFormatException if the version 1 or version 2 params are not valid format
     */
    public static int compareVersions(String version1, String version2) throws NumberFormatException
    {
        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++)
        {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0)
            {
                comparisonResult = compare;
                break;
            }
        }

        return comparisonResult;
    }

    //////////////////////////// PRIVATE METHODS ///////////////////////////////////////////

    /**
     * In case of compare version error, add the erro to the error list
     *
     * @param v1          version provided
     * @param v2          version to check
     * @param serviceName the service name
     * @param typeVersion type of version (nova version, service version, api version etc)
     * @param errorList   the error list to add the erro
     * @return always -1, means is a error
     */
    private static int compareVersionError(String v1, String v2, String serviceName, String typeVersion, List<ValidationErrorDto> errorList, String errorMessage)
    {
        log.warn("[ComparisonUtils] -> [compareVersion]: there was an error trying to obtain the number version. Provided v1: [{}] - Provided v2: [{}] - type version: [{}]. Error message: [{}]", v1, v2, typeVersion, errorMessage);
        ErrorListUtils.addError(errorList, serviceName, UNSUPPORTED_FORMAT_VERSION,
                "The service name: " + serviceName + " with version provided: " + v1 + " or the supported version: " + v2 + " of: " + typeVersion + " is not valid. " + UNSUPPORTED_FORMAT_VERSION_MSG);
        return -1;
    }
}

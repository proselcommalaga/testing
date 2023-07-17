package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.platformservices.coreservice.common.enums.QualityLevel;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;

public class QualityUtils
{

    public static boolean hasQualityAnalysis(ServiceType serviceType)
    {
        return serviceType != ServiceType.BATCH_SCHEDULER_NOVA &&
                serviceType != ServiceType.CDN_POLYMER_CELLS &&
                serviceType != ServiceType.LIBRARY_PYTHON &&
                serviceType != ServiceType.LIBRARY_TEMPLATE;
    }

    /**
     * Get just the module name, without path
     *
     * @param module String initial : ../Construccion/elara/jobs/KKPFWMDC-01-ES
     * @return String to get:   KKPFWMDC-01-ES
     */
    public static String getModuleName(String module)
    {
        int len = module.length();
        int lastInd = module.lastIndexOf('/', len);

        return module.substring(lastInd + 1, len);
    }

    public static String getSonarQualityGate(Integer productQualityLevel)
    {
        switch (QualityLevel.getFromValue(productQualityLevel))
        {
            case LOW:
                return QualityConstants.LOW_SONAR_QUALITY_GATE;
            case MEDIUM:
                return QualityConstants.MEDIUM_SONAR_QUALITY_GATE;
            case HIGH:
                return QualityConstants.HIGH_SONAR_QUALITY_GATE;
            default:
        }       return QualityConstants.MEDIUM_SONAR_QUALITY_GATE;
    }
}

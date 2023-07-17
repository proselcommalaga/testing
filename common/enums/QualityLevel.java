package com.bbva.enoa.platformservices.coreservice.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * quality level enumerate
 */
@RequiredArgsConstructor
public enum QualityLevel
{
    LOW(0),
    MEDIUM(1),
    HIGH(2),

    INVALID(-1);

    @Getter
    private final Integer value;

    public static QualityLevel getFromValue(Integer qualityLevelInteger)
    {
        for (QualityLevel qualityLevel : QualityLevel.values())
        {
          if (qualityLevel.value.equals(qualityLevelInteger))
          {
              return qualityLevel;
          }
        }

        return INVALID;
    }
}
package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterTypeDto;

import java.util.*;

public final class JdkParametersResultSetMapper
{
    private static final int PARAMETER_TYPE_NAME_INDEX = 0;
    private static final int PARAMETER_TYPE_DESCRIPTION_INDEX = 1;
    private static final int PARAMETER_TYPE_EXCLUSIVE_INDEX = 2;
    private static final int PARAMETER_JDK_VERSION_ID_INDEX = 3;
    private static final int PARAMETER_NAME_INDEX = 4;
    private static final int PARAMETER_DESCRIPTION_INDEX = 5;
    private static final int PARAMETER_DEFAULT_INDEX = 6;
    private static final int PARAMETER_VALUE_ID_INDEX = 7;

    private JdkParametersResultSetMapper()
    {
    }

    /**
     * Returns an array of mapped DTO from a given result set extracted from database.
     *
     * @param resultSet The database result set previously extracted.
     * @return An array of mapped DTO from a given result set extracted from database.
     */
    public static JdkParameterTypeDto[] getJdkParametersFrom(List<Object[]> resultSet)
    {
        List<JdkParameterTypeDto> dtos = new ArrayList<>();
        Map<JdkParameterTypeDto, List<JdkParameterDto>> typeParametersMap = new HashMap<>();
        for (final Object[] record : resultSet)
        {
            final JdkParameterTypeDto dto = getBasicParameterTypeDtoFrom(record);
            final List<JdkParameterDto> parameters = typeParametersMap.getOrDefault(dto, new ArrayList<>());
            parameters.add(getParameterFrom(record));
            typeParametersMap.put(dto, parameters);
        }
        final Set<Map.Entry<JdkParameterTypeDto, List<JdkParameterDto>>> typeParametersEntries = typeParametersMap.entrySet();
        for (Map.Entry<JdkParameterTypeDto, List<JdkParameterDto>> entry : typeParametersEntries)
        {
            final JdkParameterTypeDto typedParameterDto = entry.getKey();
            typedParameterDto.setParameters(entry.getValue().toArray(new JdkParameterDto[0]));
            dtos.add(typedParameterDto);
        }
        return dtos.stream().sorted((o1, o2) -> {
            if(o1.getExclude() && !o2.getExclude()) {
                return -1;
            }

            if(!o1.getExclude() && o2.getExclude()) {
                return 1;
            }
            return o1.getName().compareTo(o2.getName());
        }).toArray(JdkParameterTypeDto[]::new);
    }

    private static JdkParameterTypeDto getBasicParameterTypeDtoFrom(final Object[] record)
    {
        JdkParameterTypeDto dto = new JdkParameterTypeDto();
        dto.setName((String) record[PARAMETER_TYPE_NAME_INDEX]);
        final String typeDescription = record[PARAMETER_TYPE_DESCRIPTION_INDEX] != null ? (String) record[PARAMETER_TYPE_DESCRIPTION_INDEX] : null;
        dto.setDescription(typeDescription);
        dto.setExclude((Boolean) record[PARAMETER_TYPE_EXCLUSIVE_INDEX]);
        return dto;
    }

    private static JdkParameterDto getParameterFrom(Object[] record)
    {
        JdkParameterDto dto = new JdkParameterDto();
        dto.setJdkVersionParameterId((Integer) record[PARAMETER_JDK_VERSION_ID_INDEX]);
        dto.setName((String) record[PARAMETER_NAME_INDEX]);
        final String description = record[PARAMETER_DESCRIPTION_INDEX] != null ? (String) record[PARAMETER_DESCRIPTION_INDEX] : null;
        dto.setDescription(description);
        dto.setIsDefault((Boolean) record[PARAMETER_DEFAULT_INDEX]);
        dto.setIsSelected(record[PARAMETER_VALUE_ID_INDEX] != null);
        return dto;
    }
}

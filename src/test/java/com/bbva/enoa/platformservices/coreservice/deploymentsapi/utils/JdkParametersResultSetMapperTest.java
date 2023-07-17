package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterTypeDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class JdkParametersResultSetMapperTest
{
    private static final Object[][] DUMMY_RESULTS = new Object[][]{
            new Object[]{"Garbage Collector", "Description type Garbage Collector", true, 1, "Param1", "Description Param1", true, null},
            new Object[]{"Garbage Collector", "Description type Garbage Collector", true, 2, "Param2", "Description Param2", false, null},
            new Object[]{"Garbage Collector", "Description type Garbage Collector", true, 3, "Param3", "Description Param3", false, null},
            new Object[]{"Logs Garbage Collector", "Description type Logs Garbage Collector", true, 4, "Param4", "Description Param4", true, null},
            new Object[]{"Logs Garbage Collector", "Description type Logs Garbage Collector", true, 5, "Param5", "Description Param5", false, null},
            new Object[]{"Logs Garbage Collector", "Description type Logs Garbage Collector", true, 6, "Param6", "Description Param6", false, null},
            new Object[]{"Miscellaneous", "Description type Miscellaneous", false, 7, "Param7", "Description Param7", false, null},
            new Object[]{"Miscellaneous", "Description type Miscellaneous", false, 8, "Param8", "Description Param8", false, null},
            new Object[]{"Miscellaneous", "Description type Miscellaneous", false, 9, "Param9", "Description Param9", false, null}
    };
    private static final List<Object[]> RESULT_SET = Arrays.asList(DUMMY_RESULTS);
    private static final Object[][] DUMMY_SELECTED_RESULTS = new Object[][]{
            new Object[]{"Garbage Collector", "Description type Garbage Collector", true, 1, "Param1", "Description Param1", true, 11},
            new Object[]{"Logs Garbage Collector", "Description type Logs Garbage Collector", true, 6, "Param6", "Description Param6", false, 12},
            new Object[]{"Miscellaneous", "Description type Miscellaneous", false, 8, "Param8", "Description Param8", false, 13}
    };

    @Test
    public void when_result_set_is_populated_then_return_mapped_dto()
    {
        final JdkParameterTypeDto[] result = JdkParametersResultSetMapper.getJdkParametersFrom(RESULT_SET);

        Object[][] expectedTypeResults = new Object[][]{
                new Object[]{"Garbage Collector", "Description type Garbage Collector", true},
                new Object[]{"Logs Garbage Collector", "Description type Logs Garbage Collector", true},
                new Object[]{"Miscellaneous", "Description type Miscellaneous", false}
        };
        Object[][] expectedParametersResults = new Object[][]{
                new Object[]{1, "Param1", "Description Param1", true},
                new Object[]{2, "Param2", "Description Param2", false},
                new Object[]{3, "Param3", "Description Param3", false},
                new Object[]{4, "Param4", "Description Param4", true},
                new Object[]{5, "Param5", "Description Param5", false},
                new Object[]{6, "Param6", "Description Param6", false},
                new Object[]{7, "Param7", "Description Param7", false},
                new Object[]{8, "Param8", "Description Param8", false},
                new Object[]{9, "Param9", "Description Param9", false}
        };
        Assertions.assertEquals(expectedTypeResults.length, result.length);
        for (int i = 0; i < result.length; i++)
        {
            final JdkParameterTypeDto typeParameterDto = result[i];
            final Object[] expectedTypeRecord = expectedTypeResults[i];
            Assertions.assertEquals(expectedTypeRecord[0], typeParameterDto.getName());
            Assertions.assertEquals(expectedTypeRecord[1], typeParameterDto.getDescription());
            Assertions.assertEquals(expectedTypeRecord[2], typeParameterDto.getExclude());
            for (int i1 = 0; i1 < typeParameterDto.getParameters().length; i1++)
            {
                final JdkParameterDto parameter = typeParameterDto.getParameters()[i1];
                final Object[] expectedParametersRecord = expectedParametersResults[(3 * i) + i1];
                Assertions.assertEquals(expectedParametersRecord[0], parameter.getJdkVersionParameterId());
                Assertions.assertEquals(expectedParametersRecord[1], parameter.getName());
                Assertions.assertEquals(expectedParametersRecord[2], parameter.getDescription());
                Assertions.assertEquals(expectedParametersRecord[3], parameter.getIsDefault());
                Assertions.assertFalse(parameter.getIsSelected());
            }
        }
    }

    @Test
    public void when_any_parameter_has_value_id_then_map_as_selected()
    {
        final JdkParameterTypeDto[] result = JdkParametersResultSetMapper.getJdkParametersFrom(Arrays.asList(DUMMY_SELECTED_RESULTS));

        Assertions.assertEquals(3, result.length);
        Assertions.assertTrue(result[0].getParameters()[0].getIsSelected());
        Assertions.assertTrue(result[1].getParameters()[0].getIsSelected());
        Assertions.assertTrue(result[2].getParameters()[0].getIsSelected());
    }

}
package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonUtilsTest {

    @Test
    void compareVersionIsLessThan()
    {
        int result = ComparisonUtils.compareVersions("11", "11.0.7");
        assertTrue(result < 0);

        int result2 = ComparisonUtils.compareVersions("11.0.12", "11.1.7");
        assertTrue(result2 < 0);
    }

    @Test
    void compareVersionIsEqual()
    {
        int result = ComparisonUtils.compareVersions("1.8.123", "1.8.123");
        assertEquals(0, result);

        int result2 = ComparisonUtils.compareVersions("1.8.123.0", "1.8.123");
        assertEquals(0, result2);
    }

    @Test
    void compareVersionIsGreaterThan()
    {
        int result = ComparisonUtils.compareVersions("12.1", "11.0.7");
        assertTrue(result > 0);

        int result2 = ComparisonUtils.compareVersions("11.1.12", "11.1.7");
        assertTrue(result2 > 0);
    }

}
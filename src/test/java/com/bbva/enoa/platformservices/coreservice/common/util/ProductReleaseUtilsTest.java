package com.bbva.enoa.platformservices.coreservice.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProductReleaseUtilsTest
{

    @Test
    void getMaxReleaseVersions()
    {
        //verify
        Assertions.assertEquals(ProductReleaseUtils.getMaxReleaseVersions(2), 6);
        //verify
        Assertions.assertEquals(ProductReleaseUtils.getMaxReleaseVersions(2), 6);
        //verify
        Assertions.assertEquals(ProductReleaseUtils.getMaxReleaseVersions(3), 8);
        //verify
        Assertions.assertEquals(ProductReleaseUtils.getMaxReleaseVersions(4), 10);
        //verify
        Assertions.assertNotEquals(ProductReleaseUtils.getMaxReleaseVersions(5), 12);
    }
}
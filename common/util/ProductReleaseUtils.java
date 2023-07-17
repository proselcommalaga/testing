package com.bbva.enoa.platformservices.coreservice.common.util;

public final class ProductReleaseUtils
{

    /**
     * Private constructor
     */
    private ProductReleaseUtils()
    {
        super();
    }

    /**
     * Maximum number of release versions
     */
    private static final int MAXIMUM_VERSIONS_6 = 6;

    /**
     * Maximum number of release versions (3 slots)
     */
    private static final int MAXIMUM_VERSIONS_8 = 8;

    /**
     * Maximum number of release versions (4 slots)
     */
    private static final int MAXIMUM_VERSIONS_10 = 10;

    /**
     * Maximum number of release versions (4 slots)
     */
    private static final int RELEASE_SLOTS_3 = 3;

    /**
     * Maximum number of release versions (4 slots)
     */
    private static final int RELEASE_SLOTS_4 = 4;


    /**
     * returns maximum number of release versions based on the product slots
     *
     * @param releaseSlots product slot
     * @return maximum number of release versions
     */
    public static int getMaxReleaseVersions(int releaseSlots)
    {
        int maxVersion;
        switch (releaseSlots)
        {
            case RELEASE_SLOTS_3:
                maxVersion = MAXIMUM_VERSIONS_8;
                break;
            case RELEASE_SLOTS_4:
                maxVersion = MAXIMUM_VERSIONS_10;
                break;
            default:
                maxVersion = MAXIMUM_VERSIONS_6;
                break;
        }
        return maxVersion;
    }


}

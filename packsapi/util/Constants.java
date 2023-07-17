package com.bbva.enoa.platformservices.coreservice.packsapi.util;

/**
 * Class dedicated to write and store the packs constants
 */
public class Constants
{
    /**
     * Literals of
     */
    public static final String PACKS_API_NAME = "PacksAPI";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

    /**
     * Class of constants only for packs errors
     */
    public static class PackConstantErrors
    {
        /**
         * Doc system service error class name
         */
        public static final String PACK_ERROR_CODE_CLASS_NAME = "PackError";

        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "PACKS-000";

        /**
         * No product error
         */
        public static final String NO_SUCH_HARDWARE_ERROR_CODE = "PACKS-001";

        /**
         * No doc system error
         */
        public static final String NO_SUCH_FILE_SYSTEM_ERROR_CODE = "PACKS-002";

        /**
         * No broker hardware error
         */
        public static final String NO_SUCH_BROKER_HARDWARE_ERROR_CODE = "PACKS-003";

        /**
         * Generic message
         */
        public static final String MSG_CONTACT_NOVA = "Please, contact the NOVA Admin team";
    }

}

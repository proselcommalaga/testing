package com.bbva.enoa.platformservices.coreservice.docsystemsapi.util;

import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;

/**
 * Class dedicated to write and store the doc systems constants
 */
public final class Constants
{
    /**
     * Literals of
     */
    public static final String DOC_SYSTEMS_API_NAME = "DocSystemsAPI";

    /**
     * Default type of a DocSystem when it's created by an end-user.
     */
    public static final DocumentType DOC_SYSTEM_TYPE_WHEN_CREATED_BY_USER = DocumentType.FILE;
    /**
     * String constants for the type of resources that may have documentation associated.
     */
    public static final String RESOURCE_LOGICAL_CONNECTOR = "LOGICAL_CONNECTOR";
    public static final String RESOURCE_NOVA_API = "NOVA_API";
    public static final String RESOURCE_FILE_TRANSFER_CONFIG = "FILE_TRANSFER_CONFIG";
    public static final String RESOURCE_SCHEDULE_REQUEST = "SCHEDULE_REQUEST";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

    /**
     * Class of constants only for Doc systems errors
     */
    public static class DocSystemsErrors
    {
        /**
         * Doc system service error class name
         */
        public static final String DOC_SYSTEM_ERROR_CODE_CLASS_NAME = "DocSystemErrorCode";

        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "DOCSYSTEMS-000";

        /**
         * No product error
         */
        public static final String NO_SUCH_PRODUCT_ERROR_CODE = "DOCSYSTEMS-001";

        /**
         * No doc system error
         */
        public static final String NO_SUCH_DOC_SYSTEM_ERROR_CODE = "DOCSYSTEMS-002";

        /**
         * No doc system error
         */
        public static final String REPEATED_DOC_SYSTEM_ERROR_CODE = "DOCSYSTEMS-003";

        /**
         * Invalid field error
         */
        public static final String INVALID_FIELD_ERROR_CODE = "DOCSYSTEMS-004";

        /**
         * Reserved name error
         */
        public static final String DOC_SYSTEM_NAME_RESERVED_ERROR_CODE = "DOCSYSTEMS-005";

        /**
         * Removal Not Allowed Error
         */
        public static final String DOC_SYSTEM_REMOVAL_NOT_ALLOWED_ERROR_CODE = "DOCSYSTEMS-006";

        /**
         * Category
         */
        public static final String DOC_SYSTEM_CATEGORY_CREATION_OR_UPDATE_NOT_ALLOWED_FOR_END_USERS_ERROR_CODE = "DOCSYSTEMS-007";

        /**
         * Update Not Allowed Error
         */
        public static final String DOC_SYSTEM_UPDATE_NOT_ALLOWED_ERROR_CODE = "DOCSYSTEMS-008";

        /**
         * No doc system error
         */
        public static final String NO_SUCH_DOC_SYSTEM_CATEGORY_ERROR_CODE = "DOCSYSTEMS-009";

        /**
         * No doc system error
         */
        public static final String REPEATED_URL_DOC_SYSTEM_ERROR_CODE = "DOCSYSTEMS-010";

        /**
         * Forbidden error
         */
        public static final String FORBIDDEN_ERROR_CODE = "USER-002";

        /**
         * Generic message
         */
        public static final String MSG_CONTACT_NOVA = "Please, contact the NOVA Admin team";
    }

}

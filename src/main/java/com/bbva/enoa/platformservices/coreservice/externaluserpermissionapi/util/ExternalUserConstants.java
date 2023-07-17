package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.util;

/**
 * ExternalUserConstants class for External User Permission
 */
public final class ExternalUserConstants
{
    private ExternalUserConstants()
    {
        // nothing to do
    }

    public static final String EXTERNAL_USER_PERMISSION = "EXTERNAL_USER_PERMISSION";

    public static final class Errors
    {
        public static final String CLASS_NAME = "ExternalUserPermissionError";

        public static final String FORBIDDEN_ERROR = "EX-US-001";
        public static final String BAD_REQUEST_ERROR = "EX-US-002";
        public static final String PRODUCT_NOT_FOUND_ERROR = "EX-US-003";
        public static final String CREATING_PERMISSION_ERROR = "EX-US-004";
        public static final String DELETING_PERMISSION_ERROR = "EX-US-005";
        public static final String PLATFORM_NOT_SUPPORTED_ERROR = "EX-US-006";
        public static final String PERMISSION_NOT_FOUND_ERROR = "EX-US-007";
    }
}

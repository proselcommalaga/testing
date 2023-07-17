package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

/**
 * Permissions over Release operations
 *
 * @author XE63267
 */
public class ReleaseConstants
{


    /**
     * Literal for api name and method of Release API
     */
    public static final String RELEASE_API = "ReleaseAPI";

    /**
     * Constant for literal TC
     **/
    public static final String TC_PRODUCTION_NAME = "TC";

    /**
     * Constant for literal CREATE_RELEASE
     */
    public static final String CREATE_RELEASE_PERMISSION = "CREATE_RELEASE";
    /**
     * Constant for literal DELETE_RELEASE
     */
    public static final String DELETE_RELEASE_PERMISSION = "DELETE_RELEASE";
    /**
     * Constant for literal EDIT_RELEASE
     */
    public static final String EDIT_RELEASE_PERMISSION = "EDIT_RELEASE";
    /**
     * Constant for literal EDIT_RELEASE_CONFIG
     */
    public static final String EDIT_RELEASE_CONFIG_PERMISSION = "EDIT_RELEASE_CONFIG";

    /**
     * Environment
     */
    public enum ENVIRONMENT
    {
        INT, PRE, PRO
    }

    /**
     * Constant class with errors code
     */
    public static final class ReleaseErrors
    {
        /**
         * ReleaseError class name for exception
         */
        public static final String CLASS_NAME = "ReleaseError";

        /**
         * Code: Unexpected internal error
         **/
        public static final String CODE_UNEXPECTED_INTERNAL_ERROR = "RELEASES-000";
        /**
         * Unexpected internal error
         **/
        public static final String MSG_UNEXPECTED_ERROR = "Unexpected internal error";

        /**
         * Code: The given product doesn't exist
         **/
        public static final String CODE_PRODUCT_DOESNT_EXIST = "RELEASES-001";

        /**
         * Code: The given release couldn't be found
         **/
        public static final String CODE_NO_SUCH_RELEASE = "RELEASES-002";
        /**
         * The given release couldn't be found
         **/
        public static final String MSG_NO_SUCH_RELEASE = "The given release couldn't be found";

        /**
         * Code: Tried to remove a release with versions
         **/
        public static final String CODE_REMOVE_RELEASE_WITH_VERSION = "RELEASES-003";
        /**
         * Can't remove a release that have any version
         **/
        public static final String MSG_RELEASE_WITH_VERSIONS = "Can't remove a release that have any version. Review storaged - historical releases - release versions and remove all of them before removing the release.";

        /**
         * Code: There is already a release with the same name
         **/
        public static final String CODE_RELEASE_NAME_DUPLICATED = "RELEASES-004";
        /**
         * Try a different name
         **/
        public static final String MSG_RELEASE_NAME_DUPLICATED = "Try a different name";

        /**
         * Code: Release name is invalid. Please, do not use special characters
         **/
        public static final String CODE_SPECIAL_CHARACTERS = "RELEASES-005";

        /**
         * Code: Format date not valid
         **/
        public static final String CODE_DATE_NOT_VALID = "RELEASES-006";

        /**
         * Code: The current user does not have permission for this operation
         **/
        public static final String CODE_USER_WITHOUT_PERMISSIONS = "USER-002";

        /**
         * Code: The current user does not have permission for this operation
         **/
        public static final String INVALID_DEPLOYMENT_PLATFORM = "RELEASES-008";

        /**
         * Code: The current user does not have permission for this operation
         **/
        public static final String INVALID_LOGGING_PLATFORM = "RELEASES-009";

        /**
         * Code: Default auto manage in PRE disabled
         **/
        public static final String CODE_DEFAULT_AUTOMANAGE_IN_PRE_DISABLED = "RELEASES-010";

        /**
         * Code: Default auto manage in PRO disabled
         **/
        public static final String CODE_DEFAULT_AUTOMANAGE_IN_PRO_DISABLED = "RELEASES-011";

        /**
         * Code: Invalid deployment Type
         **/
        public static final String CODE_INVALID_DEPLOYMENT_TYPE = "RELEASES-012";

        /**
         * Code: Default auto deploy in PRE disabled
         **/
        public static final String CODE_DEFAULT_AUTODEPLOY_IN_PRE_DISABLED = "RELEASES-013";

        /**
         * Code: Default auto deploy in PRO disabled
         **/
        public static final String CODE_DEFAULT_AUTODEPLOY_IN_PRO_DISABLED = "RELEASES-014";

        /**
         * Code: Invalid environment value
         **/
        public static final String CODE_INVALID_ENVIRONMENT_TYPE = "RELEASES-015";

        /**
         * Code: Invalid Release Version Status value
         **/
        public static final String CODE_INVALID_RELEASE_STATUS_VALUE = "RELEASES-016";

        public static final String INVALID_JVM_JDK_VERSIONS = "RELEASES-017";

        public static final String EMPTY_JDK = "RELEASES-018";
    }
}

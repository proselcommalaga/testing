package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.util;

/**
 * Constants for Release Version API
 */
public final class Constants
{
    public static final String USAGE = "BUILD";

    /**
     * Constant class with errors code
     */
    public static final class LibraryError
    {
        /** Hidden constructor */
        private LibraryError()
        {
            super();
        }

        /**
         * Class name for errors
         */
        public static final String CLASS_NAME = "LibraryManagerError";

        //----------------------------
        //      Errors code
        //----------------------------

        /**
         * Unexpected exception error code
         */
       public static final String UNEXPECTED_ERROR = "LIBRARYMANAGER-000";

        /**
         *  Unable to store requirements error code
         */
        public static final String UNABLE_STORE_REQUIREMENTS = "LIBRARYMANAGER-001";

        /**
         *  unable to remove requirements error code
         */
        public static final String UNABLE_REMOVE_REQUIREMENTS = "LIBRARYMANAGER-002";

        /**
         *  unable to get requirements error code
         */
        public static final String UNABLE_GET_REQUIREMENTS = "LIBRARYMANAGER-003";

        /**
         *  unable to publish a library on a enviroment error code
         */
        public static final String UNABLE_PUBLISH_ON_ENVIRONMENT = "LIBRARYMANAGER-004";

        /**
         *  unable to get enviroments error code
         */
        public static final String UNABLE_GET_ENVIRONMENTS = "LIBRARYMANAGER-005";

        /**
         * unable to get libraries of a service error code
         */
        public static final String UNABLE_GET_LIBRARIES_OF_SERVICE = "LIBRARYMANAGER-006";

        /**
         * unable to save libraries of a service error code
         */
        public static final String UNABLE_SAVE_LIBRARIES_OF_SERVICE = "LIBRARYMANAGER-006";

        /**
         *  unable to get the list of services that using a library error code
         */
        public static final String UNABLE_GET_SERVICES_USING_LIBRARY = "LIBRARYMANAGER-007";

        /**
         * unable to remove a library  error code
         */
        public static final String UNABLE_REMOVE_LIBRARY = "LIBRARYMANAGER-008";

        /**
         *  unable to remove usage over a library error code
         */
        public static final String UNABLE_REMOVE_USAGES = "LIBRARYMANAGER-009";

        /**
         *  invalid use of a library into an environment error code
         */
        public static final String INVALID_USE_OF_LIBRARIES_IN_ENVIRONMENT = "LIBRARYMANAGER-010";

        /**
         *  Unable to validate requirements
         */
        public static final String UNABLE_TO_VALIDATE_REQUIREMENTS = "LIBRARYMANAGER-011";

        //----------------------------
        //      Errors actions
        //----------------------------

        /**
         * Generic message for unexpected error.
         */
        public static final String REQUIREMENTS_ERROR_ACTION = "Please, review list of requirements of this library";

        /**
         * Unexpected internal error
         */
        public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";

        /**
         * Problem with the list of libraries declared/used by a service into an environment
         */
        public static final String REVIEW_LIBRARIES_BY_SERVICE = "Please, review libraries declared by service in the current environment";

        /**
         *  Unable to validate requirements
         */
        public static final String UNABLE_TO_VALIDATE_REQUIREMENTS_MSG = "Unable to validate requirements";
    }
}

package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util;

/**
 * Class for constants
 */
public final class QualityConstants
{
    /**
     * SQA Ok state
     */
    public static final String SQA_OK = "OK";

    /**
     * SQA ERROR state
     */
    public static final String SQA_ERROR = "ERROR";

    /**
     * SQA NOT AVAILABLE state
     */
    public static final String SQA_NOT_AVAILABLE = "NOT_AVAILABLE";

    /**
     * SQA NOT AVAILABLE state
     */
    public static final String SQA_ERROR_PROCESSING_MODULE = "INVALID_EPHOENIX_MODULE";

    /**
     * SQA joker word
     */
    public static final String SQA = "SQA";

    /**
     * Check qulity permission
     */
    public static final String CHECK_QUALITY_PERMISSION = "CHECK_QUALITY";

    /**
     * Set report
     */
    public static final String SET_PERFORMANCE_REPORT_PERMISSION = "SET_PERFORMANCE_REPORT";

    public static final String IV_USER = "iv-user";

    /**
     * Literal of
     */
    public static final String QUALITY_MANAGER_API_NAME = "Qualitymanagerapi";

    /**
     * Sonar version 5
     */
    public static final String SONAR_VERSION_5 = "5.1.2";

    /**
     * Sonar quality gate list
     */
    public static final String LOW_SONAR_QUALITY_GATE = "NOVA_BAJO";
    public static final String MEDIUM_SONAR_QUALITY_GATE = "NOVA_MEDIO";
    public static final String HIGH_SONAR_QUALITY_GATE = "NOVA_ALTO";

    /**
     * Empty constructor
     */
    private QualityConstants()
    {
        // Empty constructor
    }

    /**
     * Class of constants only for Doc systems errors
     */
    public static class QualityManagerErrors
    {
        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "QUALITYMANAGER-000";

        public static final String QM_SUBSYSTEM_NOT_FOUND_ERROR_CODE = "QUALITYMANAGER-003";

        /**
         * Invalid release version
         */
        public static final String QM_INVALID_RELEASE_VERSION_ERROR_CODE = "QUALITYMANAGER-006";

        /**
         * Error calling QA service
         */
        public static final String QA_NOT_AVAILABLE_ERROR_CODE = "QUALITYMANAGER-007";

        public static final String QM_PRODUCT_NOT_FOUND_ERROR_CODE = "QUALITYMANAGER-009";

        public static final String SERVICE_VALIDATION_ERROR_CODE = "QUALITYMANAGER-010";

        public static final String PLAN_NOT_FOUND_ERROR_CODE = "QUALITYMANAGER-011";

        public static final String PLAN_NOT_DEPLOYED_ON_PRE_ERROR_CODE = "QUALITYMANAGER-012";

        public static final String ALL_SERVICE_TYPES_HAVE_NOT_QUALITY_ERROR_CODE = "QUALITYMANAGER-013";

        public static final String EPHOENIX_MODULE_POM_ERROR_CODE = "QUALITYMANAGER-014";

        /*
         * Errors Codes for Quality Assurance
         */
        /** Max number of quality analisis reached error code */
        public static final String QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE = "QUALITYASSURANCE-003";

        public static final String QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE = "QUALITYASSURANCE-006";


        /**
         * User permissions
         */
        public static final String FORBIDDEN_ERROR_CODE = "USER-002";

        /**
         * Quality manager error code class name
         */
        public static final String QUALITY_MANAGER_ERROR_CODE_CLASS_NAME = "QualityManagerErrorCode";

        private QualityManagerErrors()
        {
        }
    }

}

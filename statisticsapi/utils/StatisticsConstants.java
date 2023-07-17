package com.bbva.enoa.platformservices.coreservice.statisticsapi.utils;

public class StatisticsConstants
{
    /**
     * Literal for api name and method of Statistics API
     */
    public static final String STATISTICS_API = "StatisticsAPI";


    /**
     * Product types constant
     */
    public static final String LIBRARY = "LIBRARY";
    public static final String NOVA = "NOVA";
    public static final String ETHER = "ETHER";

    /**
     * Header CSV for Error codes catalog
     */
    public static final String HEADER_ERROR_CODES_CATALOG = "Código de Error;Mensaje de Error;Posible solución;Código HTTP Status;Categoria de error \n";

    /**
     * When a parameter takes this value, the results won't be filtered by it
     */
    public static final String NO_FILTER_PARAMETER = "ALL";

    /**
     * When a parameter takes this value, return the results filtered by AVAILABLE_RAM category in other case is a multivalue
     */
    public static final String AVAILABLE_RAM = "AVAILABLE_RAM";

    /**
     * When a parameter takes this value, return the results filtered by AVAILABLE_RAM category in other case is a multivalue
     */
    public static final String NOVA_COINS = "NOVA_COINS";

    /**
     * When a parameter takes this value, return the results with Product ID = NULL
     */
    public static final String FILTER_BY_NULL_PRODUCT_PARAMETER = "NOT_ASSIGNED";

    /**
     * Get statistics since this number of days
     **/
    public static final Integer SINCE_DAYS_AGO = 30;

    /**
     * Get statistics for Subsystem Code Analyses in this status
     **/
    public static final String SUBSYSTEM_CODE_ANALYSES_STATUS = "READY";

    /**
     * Subsystem Code Analyses SQA State OK
     **/
    public static final String SUBSYSTEM_CODE_ANALYSES_SQA_STATE_OK = "OK";

    /**
     * Export header with category
     */
    public static final String[] EXPORT_HEADER_CATEGORY = new String[]{"DATE", "CATEGORY", "VALUE"};

    /**
     * Export header for cloud products
     */
    public static final String[] EXPORT_HEADER_PRODUCT = new String[]{"DATE", "PLATAFORMA", "ENTORNO", "UUAA"};

    /**
     * Export header
     */
    public static final String[] EXPORT_HEADER = new String[]{"DATE", "VALUE"};

    /**
     * Adoption level export header
     */
    public static final String[] ADOPTION_LEVEL_EXPORT_HEADER = new String[]{"ADOPTION_LEVEL", "PLATAFORMAS", "SUMA TOTAL DE EJECUCIONES (%)"};
    
    /**
     * Empty export
     */
    public static final String[] EMPTY_EXPORT = new String[]{"", "", ""};

    /**
     * Export header for brokers
     */
    public static final String[] EXPORT_HEADER_BROKER = new String[]{"FECHA", "UUAA", "ENTORNO", "TIPO", "PLATAFORMA", "ESTADO", "VALOR"};

    /**
     * NEXT GEN constant for export
     **/
    public static final String NEXT_GEN_PARAMETER = "NEXT_GEN (NOVA + EPHOENIX_NOVA + ETHER)";

    /**
     * Legacy constant for export
     **/
    public static final String LEGACY_PARAMETER = "LEGACY (EPHOENIX_LEGACY)";

    /**
     * Category constant for storage historical
     */
    public static final String TOTAL_PARAMETER = "TOTAL";

    /**
     * Constant class with errors code
     */
    public static final class StatisticsErrors
    {
        /**
         * StatisticsErrors class name for exception
         */
        public static final String CLASS_NAME = "StatisticsError";

        /**
         * Generic message, shown when there is not a known solution to an error.
         */
        public static final String MSG_CONTACT_NOVA = "Please, contact the NOVA Admin team";

        /**
         * Unexpected error code
         */
        public static final String UNEXPECTED_ERROR_CODE = "STATISTICS-000";

        /**
         * Release Version Status not valid error
         */
        public static final String RELEASE_VERSION_STATUS_NOT_VALID_ERROR_CODE = "STATISTICS-001";

        /**
         * UUAA not unique not valid error
         */
        public static final String UUAA_NOT_UNIQUE_ERROR_CODE = "STATISTICS-002";

        /**
         * User Service error
         */
        public static final String USER_SERVICE_ERROR_CODE = "STATISTICS-003";

        /**
         * Task Service error
         */
        public static final String TODO_TASK_SERVICE_ERROR_CODE = "STATISTICS-004";

        /**
         * Continuous Integration error
         */
        public static final String CONTINUOUS_INTEGRATION_SERVICE_ERROR_CODE = "STATISTICS-005";

        /**
         * Environment not valid error
         */
        public static final String ENVIRONMENT_NOT_VALID_ERROR_CODE = "STATISTICS-006";

        /**
         * Logical Connector Status not valid error
         */
        public static final String LOGICAL_CONNECTOR_STATUS_NOT_VALID_ERROR_CODE = "STATISTICS-007";

        /**
         * Platform not valid error
         */
        public static final String PLATFORM_NOT_VALID_ERROR_CODE = "STATISTICS-008";

        /**
         * Alert Service error
         */
        public static final String ALERT_SERVICE_ERROR_CODE = "STATISTICS-009";

        /**
         * Filesystems Status not valid error
         */
        public static final String FILESYSTEM_STATUS_NOT_VALID_ERROR_CODE = "STATISTICS-010";

        /**
         * Generate date time parse nova error
         */
        public static final String DATE_TIME_PARSE_ERROR_CODE = "STATISTICS-011";

        /**
         * Generate spring batch already running error code
         */
        public static final String ALREADY_RUNNING_JOB_ERROR_CODE = "STATISTICS-012";

        /**
         * Generate not restartable spring batch job error code
         */
        public static final String NOT_RESTARTABLE_JOB_ERROR_CODE = "STATISTICS-013";

        /**
         * Generate already completed spring batch job error code
         */
        public static final String ALREADY_COMPLETED_JOB_ERROR_CODE = "STATISTICS-014";

        /**
         * Generate spring batch internal job execution error code
         */
        public static final String INTERNAL_JOB_EXECUTION_ERROR_CODE = "STATISTICS-015";

        /**
         * Generate spring batch internal job invalid parameters error code
         */
        public static final String INVALID_JOB_PARAMETERS_ERROR_CODE = "STATISTICS-016";

        /**
         * Sonar version error
         */
        public static final String SONAR_VERSION_ERROR_CODE = "STATISTICS-017";

        /**
         * Export format error
         */
        public static final String EXPORT_FORMAT_ERROR_CODE = "STATISTICS-018";

        /**
         * Creating XLSX error
         */
        public static final String CREATING_XLSX_ERROR_CODE = "STATISTICS-019";

        /**
         * Broker Status not valid error
         */
        public static final String BROKER_STATUS_NOT_VALID_ERROR_CODE = "STATISTICS-020";

        /**
         *
         */
        public static final String FILE_TRANSFER_STATISTICS_ERROR_CODE = "STATISTICS-021";

    }
}
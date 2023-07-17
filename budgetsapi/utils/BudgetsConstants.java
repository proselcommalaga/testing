package com.bbva.enoa.platformservices.coreservice.budgetsapi.utils;

/**
 * Constants for permissions on budgets operations
 * 
 * @author XE63267
 *
 */
public class BudgetsConstants {

    /**
     * Permission to be able to edit a budget.
     */
    public static final String EDIT_BUDGET_PERMISSION = "EDIT_BUDGET";

    /**  Literal for api name and method of Budgets API */
    public static final String BUDGETS_API = "BudgetsAPI";

    /** Pattern for date format*/
    public static final String[] DATE_FORMAT_PATTERNS = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ"};

    /**
     * Constant class with errors code
     */
    public static final class BudgetErrors
    {
        /** BudgetErrors class name for exception*/
        public static final String CLASS_NAME = "BudgetsError";

        /** Unexpected error code */
        public static final String UNEXPECTED_ERROR = "BUDGETSAPI-000";

        /** Error product budget integration error code */
        public static final String ERROR_PRODUCT_BUDGET_INTEGRATION = "BUDGETSAPI-001";

        /** Deployment plan not found error code */
        public static final String DEPLOYMENT_PLAN_NOT_FOUND = "BUDGETSAPI-002";

        /** Invalid date error code */
        public static final String INVALID_DATE_VALUE = "BUDGETSAPI-003";

        /** Cluster not fond*/
        public static final String  CLUSTER_NOT_FOUND= "BUDGETSAPI-004";

        /** Invalid date format error code */
        public static final String INVALID_DATE_FORMAT = "BUDGETSAPI-005";

        /** User permission denied error code */
        public static final String USER_PERMISSION_DENIED = "USER-002";
    }
 }

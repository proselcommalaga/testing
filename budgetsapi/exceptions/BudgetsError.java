package com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.budgetsapi.utils.BudgetsConstants.BudgetErrors;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;


/**
 * BudgetsApi Error class
 */
@ExposeErrorCodes
public class BudgetsError
{

    /**
     * NovaError by Unexpected error 
     * @return a NovaError
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.UNEXPECTED_ERROR,
                "Unexpected internal error",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * NovaError by products integration Error
     * @return a NovaError
     */
    public static NovaError getProductBudgetsApiError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.ERROR_PRODUCT_BUDGET_INTEGRATION,
                "Error in ProductBudgets integration",
                "Check product budgets service",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError by Deployment not Found error
     * @return a NovaError
     */
    public static NovaError getDeploymentPlanNotFoundError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.DEPLOYMENT_PLAN_NOT_FOUND,
                "Deployment plan not found",
                "Check plan existence",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError by invalid date value
     * @return a NovaError
     */
    public static NovaError getInvalidDateValueError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.INVALID_DATE_VALUE,
                "Invalid date value",
                "Check date format",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError by a cluster not found
     * @return a NovaError
     */
    public static NovaError getClusterNotFoundError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.CLUSTER_NOT_FOUND
                ,"Cluster not found",
                "Check clusters",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError by invalid date format
     * @return a NovaError
     */
    public static NovaError getInvalidDateFormatError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.INVALID_DATE_FORMAT,
                "Invalid date format",
                "Check date format",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError by user without permissions
     * @return a NovaError
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(BudgetErrors.CLASS_NAME,
                BudgetErrors.USER_PERMISSION_DENIED,
                "The current user does not have permission for this operation",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

}

package com.bbva.enoa.platformservices.coreservice.etherapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import static com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants.EtherErrors;

/**
 * Error code with all the internal errors for the service.
 *
 * @author David Ramirez
 */
@ExposeErrorCodes
public class EtherError
{

	public static String getClassName()
	{
		return EtherErrors.CLASS_NAME;
	}


	/**
	 * Unexpected error
	 *  @return Nova Error
	 */
	public static NovaError getUnexpectedError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR,
				EtherErrors.MSG_UNEXPECTED_ERROR,
				EtherErrors.MSG_UNEXPECTED_ACTION,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.FATAL);
	}

	/**
	 *  Unexpected error
	 *  @return Nova Error
	 */
	public static NovaError getUnexpectedError(String customErrorMessage)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR,
				customErrorMessage,
				EtherErrors.MSG_UNEXPECTED_ACTION,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.FATAL);
	}

	/**
	 * No Product
	 *  @return Nova Error
	 */
	public static NovaError getNoSuchProductError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_PRODUCT_DOESNT_EXIST,
				EtherErrors.MSG_PRODUCT_DOESNT_EXIST_ERROR,
				EtherErrors.MSG_PRODUCT_DOESNT_EXIST_ACTION,
				HttpStatus.NOT_FOUND,
				ErrorMessageType.ERROR);
	}
	/**
	 * Error when asking for users of a product
	 *  @return Nova Error
	 */
	public static NovaError getUsersNotFoundError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_TEAM_USERS_ERROR,
				EtherErrors.MSG_TEAM_USERS_ERROR,
				EtherErrors.MSG_TEAM_USERS_ACTION,
				HttpStatus.NOT_FOUND,
				ErrorMessageType.CRITICAL);
	}
	/**
	 * Error when asking for users of a product
	 *  @return Nova Error
	 */
	public static NovaError getEtherManagerCallError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_ETHER_MANAGER_CALL_ERROR,
				EtherErrors.MSG_ETHER_MANAGER_CALL_ERROR,
				EtherErrors.MSG_ETHER_MANAGER_CALL_ACTION,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.CRITICAL);
	}

	/**
	 * Error when trying to configure a namespace with an incorrect environment
	 * @param environment The incorrect environment
	 * @return Nova Error
	 */
	public static NovaError getNoSuchEnvironmentError(String environment)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_ENVIRONMENT_ERROR,
				EtherErrors.MSG_ENVIRONMENT_ERROR+": "+environment,
				EtherErrors.MSG_ENVIRONMENT_ACTION,
				HttpStatus.BAD_REQUEST,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to execute a forbidden action
	 * @param permissions The incorrect platform type
	 * @return Nova Error
	 */
	public static NovaError getDoesNotHavePermissionsError(String permissions)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_FORBIDDEN_ERROR,
				EtherErrors.MSG_FORBIDDEN_TYPE_ERROR+": "+permissions,
				EtherErrors.MSG_FORBIDDEN_ACTION+": "+permissions,
				HttpStatus.FORBIDDEN,
				ErrorMessageType.ERROR);
	}

	/**
	 * No instance
	 *
	 * @return Nova Error
	 */
	public static NovaError getNoSuchInstanceError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_INSTANCE_DOESNT_EXIST,
				EtherErrors.MSG_INSTANCE_DOESNT_EXIST_ERROR,
				EtherErrors.MSG_INSTANCE_DOESNT_EXIST_ACTION,
				HttpStatus.NOT_FOUND,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to add users to group
	 *
	 * @param groupName The name of the group
	 * @return Nova Error
	 */
	public static NovaError getAddingUsersError(final String groupName)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_ADDING_USER_ERROR,
				String.format(EtherErrors.MSG_ADDING_USER_ERROR, groupName),
				EtherErrors.MSG_WAIT_RETRY_CONTACT,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to remove users from group of product
	 *
	 * @param groupName The name of the group
	 * @return Nova Error
	 */
	public static NovaError getRemovingUsersError(final String groupName)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_REMOVING_USER_ERROR,
				String.format(EtherErrors.MSG_REMOVING_USER_ERROR, groupName),
				EtherErrors.MSG_WAIT_RETRY_CONTACT,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to check the status of the resources
	 *
	 * @return Nova Error
	 */
	public static NovaError getCheckingEtherResourcesStatusError()
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_CHECK_ETHER_RESOURCES_STATUS_ERROR,
				EtherErrors.MSG_CHECK_ETHER_RESOURCES_STATUS_ERROR,
				EtherErrors.MSG_WAIT_RETRY_CONTACT,
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to configure a namespace with an invalid format.
	 *
	 * @return Nova Error
	 */
	public static NovaError getInvalidNamespaceFormatError(final String namespace)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherErrors.CODE_INVALID_NAMESPACE_FORMAT_ERROR,
				String.format(EtherConstants.EtherErrors.MSG_INVALID_NAMESPACE_FORMAT_ERROR, namespace),
				EtherErrors.MSG_INVALID_NAMESPACE_FORMAT_ACTION,
				HttpStatus.BAD_REQUEST,
				ErrorMessageType.ERROR);
	}

	/**
	 * Error when trying to deploy ether plan from deployment manager
	 *
	 * @param deploymentPlanId the deployment plan id
	 * @return a NovaError
	 */
	public static NovaError getDeployEtherFromDeploymentManagerError(final Integer deploymentPlanId)
	{
		return new NovaError(EtherErrors.CLASS_NAME,
				EtherConstants.EtherErrors.DEPLOY_ETHER_PLAN_DEPLOYMENT_MANAGER_ERROR,
				"Deployment manager failed to deploy ether plan with id: [" + deploymentPlanId + "]",
				"The deployment manager service has failed. Wait and try it again later.",
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageType.ERROR);
	}


}

package com.bbva.enoa.platformservices.coreservice.etherapi.util;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;

import java.util.List;

/**
 * Ether constants
 *
 * @author David Ramirez
 *
 */
public class EtherConstants
{
	/**  Literal for api name and method of EtherAPI */
	public static final String ETHER_API = "EtherAPI";

	/** List of Environments to check the Ether configuration status */
	public static final List<Environment> ENVIRONMENTS_TO_CHECK = List.of(Environment.INT, Environment.PRE, Environment.PRO);

	/** Permission required to configure Ether infrastructure  **/
	public static final String PRODUCT_ETHER_CONFIGURATION_MANAGEMENT = "PRODUCT_ETHER_CONFIGURATION_MANAGEMENT";

	/** Constant class with errors code */
	public static final class EtherErrors
	{
		/** ReleaseError class name for exception */
		public static final String CLASS_NAME = "EtherError";

		/** Prefix for logging errors**/
		public static final String MSG_PREFIX = "EtherAPI: Error caught with code: {}";

		/**
		 * Code: Unexpected internal error
		 **/
		public static final String CODE_UNEXPECTED_INTERNAL_ERROR = "ETHER-000";
		/**
		 * Unexpected internal error
		 **/
		public static final String MSG_UNEXPECTED_ERROR = "Unexpected internal error";
		/**
		 * Unexpected internal error action
		 **/
		public static final String MSG_UNEXPECTED_ACTION = "Unexpected internal error";

		/**
		 * Code: Error obtaining product
		 **/
		public static final String CODE_PRODUCT_DOESNT_EXIST = "ETHER-001";
		/**
		 * The given product doesn't exist
		 **/
		public static final String MSG_PRODUCT_DOESNT_EXIST_ERROR = "The given product doesn't exist";
		/**
		 * Check the product
		 **/
		public static final String MSG_PRODUCT_DOESNT_EXIST_ACTION = "Check the product";

		/**
		 * Code: Error obtaining team users
		 **/
		public static final String CODE_TEAM_USERS_ERROR = "ETHER-002";
		/**
		 * Error obtaining team users
		 **/
		public static final String MSG_TEAM_USERS_ERROR = "Error obtaining team users";
		/**
		 * Check if the team user configuration is correct
		 **/
		public static final String MSG_TEAM_USERS_ACTION = "Check if the team users configuration is correct";

		/**
		 * Code: Error getting the future response for EtherManager
		 **/
		public static final String CODE_ETHER_MANAGER_CALL_ERROR = "ETHER-003";
		/**
		 * Error obtaining team users
		 **/
		public static final String MSG_ETHER_MANAGER_CALL_ERROR = "Error obtaining response from EtherManager";
		/**
		 * Check if the team user configuration is correct
		 **/
		public static final String MSG_ETHER_MANAGER_CALL_ACTION = "Please, contact the NOVA Admin team";

		/**
		 * Code: Error when using an incorrect environment
		 **/
		public static final String CODE_ENVIRONMENT_ERROR = "ETHER-004";
		/**
		 * The environment is incorrect message
		 **/
		public static final String MSG_ENVIRONMENT_ERROR = "The environment is incorrect";
		/**
		 * Use the correct environment: INT, PRE or PRO message
		 **/
		public static final String MSG_ENVIRONMENT_ACTION = "Use the correct environment: INT, PRE or PRO";

		/**
		 * Code: Error obtaining product
		 **/
		public static final String CODE_INSTANCE_DOESNT_EXIST = "ETHER-005";
		/**
		 * The given product doesn't exist
		 **/
		public static final String MSG_INSTANCE_DOESNT_EXIST_ERROR = "The given instance doesn't exist";
		/**
		 * Check the product
		 **/
		public static final String MSG_INSTANCE_DOESNT_EXIST_ACTION = "Check the instance";

		/**
		 * Code: Error adding user to product
		 **/
		public static final String CODE_ADDING_USER_ERROR = "ETHER-006";
		/**
		 * Adding user message
		 **/
		public static final String MSG_ADDING_USER_ERROR = "There was an error adding users to Ether group [%s]";
		/**
		 * Adding user action
		 **/
		public static final String MSG_WAIT_RETRY_CONTACT = "Try again and if the error continuous, please contact the NOVA Admin team";

		/**
		 * Code: Error removing user to product
		 **/
		public static final String CODE_REMOVING_USER_ERROR = "ETHER-007";
		/**
		 * Removing user message
		 **/
		public static final String MSG_REMOVING_USER_ERROR = "There was an error removing users from Ether group [%s]";

		/**
		 * Code: Error checking Ether resources status
		 **/
		public static final String CODE_CHECK_ETHER_RESOURCES_STATUS_ERROR = "ETHER-008";
		/**
		 * Checking Ether resources status
		 **/
		public static final String MSG_CHECK_ETHER_RESOURCES_STATUS_ERROR = "There was an error checking the status of Ether resources";

		/**
		 * Code: Error when using a namespace with an invalid format
		 **/
		public static final String CODE_INVALID_NAMESPACE_FORMAT_ERROR = "ETHER-009";
		/**
		 * Invalid format for namespace
		 **/
		public static final String MSG_INVALID_NAMESPACE_FORMAT_ERROR = "Invalid format for namespace [%s]";
		/**
		 * Check the format of the namespace
		 **/
		public static final String MSG_INVALID_NAMESPACE_FORMAT_ACTION = "Check the format of the namespace";

		/**
		 * Code: Does not have permissions
		 **/
		public static final String CODE_FORBIDDEN_ERROR = "USER-002";
		/**
		 * Forbidden: Does not have permissions
		 **/
		public static final String MSG_FORBIDDEN_TYPE_ERROR = "Forbidden: Does not have the required permission";
		/**
		 * You need the permission
		 **/
		public static final String MSG_FORBIDDEN_ACTION = "You need the permission";
		/**
		 * NovaError for trying to deploy a ether plan from deployment manager
		 */
		public static final String DEPLOY_ETHER_PLAN_DEPLOYMENT_MANAGER_ERROR = "ETHER-010";
	}
}

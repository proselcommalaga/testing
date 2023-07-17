package com.bbva.enoa.platformservices.coreservice.etherapi.util;

import com.bbva.enoa.apirestgen.productsapi.model.EtherConsoleProductCreationNotificationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.etherapi.exceptions.EtherError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions.getCleanUuaa;

/**
 * Utility for checking business validation on Ether operations.
 *
 * @author David Ramirez
 */
@Slf4j
@Service
public class EtherValidator
{
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EtherValidator.class);

	/**
	 * Checks if Product does exist or not.
	 *
	 * @param productId Product ID.
	 * @param product   Product to check.
	 */
	public void checkProductExistence(final int productId, final Product product)
	{
		if (product == null)
		{
			LOG.warn("[{}] -> [{}]: {} {} ", "EtherAPI", "checkProductExistence", EtherConstants.EtherErrors.MSG_PREFIX,
					EtherError.getNoSuchProductError());
			throw new NovaException(EtherError.getNoSuchProductError(), "The product: [" + productId + "] does not exist");
		}
	}

	/**
	 * Checks if environment is a valid value. Returns the Environment if the input is valid.
	 *
	 * @param environment INT, PRE or PRO.
	 * @return The Environment
	 */
	public Environment checkValidEnvironment(final String environment)
	{
		if (environment == null)
		{
			return null;
		}
		if(!EnumUtils.isValidEnum(Environment.class, environment))
		{
			LOG.warn("[{}] -> [{}]: {} {} ", "EtherAPI", "checkValidEnvironment", EtherConstants.EtherErrors.MSG_PREFIX,
					EtherError.getNoSuchEnvironmentError(environment));
			throw new NovaException(EtherError.getNoSuchEnvironmentError(environment), "The environment: [" + environment + "] does not exists");
		}

		final Environment environmentParsed = Environment.valueOf(environment);

		if (!environmentParsed.equals(Environment.PRO) && !environmentParsed.equals(Environment.PRE) && !environmentParsed.equals(Environment.INT))
		{
			final NovaError badEnvironment = EtherError.getNoSuchEnvironmentError(environment);
			LOG.warn("[{}] -> [{}]: {} {} ", "EtherAPI", "checkValidEnvironment", EtherConstants.EtherErrors.MSG_PREFIX, badEnvironment);
			throw new NovaException(badEnvironment, "The environment: [" + environment + "] does not exists");
		}

		return environmentParsed;
	}

	/**
	 * Checks if the fields of the notification are valid.
	 *
	 * @param consoleProductCreationNotification The notification.
	 * @throws NovaException If there are not valid fields.
	 */
	//TODO@Adrián ¿Debería estar en ethermanager?
	public void validateConsoleProductCreationNotification(EtherConsoleProductCreationNotificationDTO consoleProductCreationNotification)
	{
		getCleanUuaa(consoleProductCreationNotification.getUuaa());
		//TODO@Adrián Validar el resto de campos.
	}

	/**
	 * Check whether the format of a namespace is valid.
	 *
	 * @param namespace The given namespace
	 * @throws NovaException Thrown if the format is not valid.
	 */
	public void checkNamespaceFormat(final String namespace)
	{
		if (Strings.isNullOrEmpty(namespace))
		{
			LOG.warn("[{}] -> [{}]: {} {} ", "EtherAPI", "checkNamespaceFormat", EtherConstants.EtherErrors.MSG_PREFIX,
					EtherError.getInvalidNamespaceFormatError(namespace));
			throw new NovaException(EtherError.getInvalidNamespaceFormatError(namespace));
		}
	}
}

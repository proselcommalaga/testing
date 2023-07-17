package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class LibraryManagerErrorTest
{
	@Test
	public void getUnexpectedErrorTest()
	{
		NovaError novaError = LibraryManagerError.getUnexpectedError();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNEXPECTED_ERROR, novaError.getErrorCode()),
				() -> Assertions.assertEquals(Constants.LibraryError.UNEXPECTED_ERROR_MSG, novaError.getErrorMessage()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.FATAL, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableStoreRequirementsErrorTest()
	{
		NovaError novaError = LibraryManagerError.getUnableStoreRequirementsError();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_STORE_REQUIREMENTS, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableRemoveRequirementsErrorTest()
	{
		NovaError novaError = LibraryManagerError.getUnableRemoveRequirementsError();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_REMOVE_REQUIREMENTS, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableGetRequirementsErrorTest()
	{
		NovaError novaError = LibraryManagerError.getUnableGetRequirementsError();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_GET_REQUIREMENTS, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getInvalidUseOfLibraryInEnvironmentErrorTest()
	{
		String env = RandomStringUtils.randomAlphabetic(10);
		NovaError novaError = LibraryManagerError.getInvalidUseOfLibraryInEnvironmentError(env);
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.INVALID_USE_OF_LIBRARIES_IN_ENVIRONMENT, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(novaError.getErrorMessage().contains(env)),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertTrue(novaError.getActionMessage().contains(env)),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableGetLibrariesOfServiceTest()
	{
		NovaError novaError = LibraryManagerError.getUnableGetLibrariesOfService();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_GET_LIBRARIES_OF_SERVICE, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableSaveLibrariesOfServiceTest()
	{
		NovaError novaError = LibraryManagerError.getUnableSaveLibrariesOfService();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_SAVE_LIBRARIES_OF_SERVICE, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableUpdateUsedLibrariesOfServiceTest()
	{
		NovaError novaError = LibraryManagerError.getUnableUpdateUsedLibrariesOfService();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_SAVE_LIBRARIES_OF_SERVICE, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnablePublishOnEnvironmentTest()
	{
		String env = RandomStringUtils.randomAlphabetic(10);
		NovaError novaError = LibraryManagerError.getUnablePublishOnEnvironment(env);
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_PUBLISH_ON_ENVIRONMENT, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(novaError.getErrorMessage().contains(env)),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableGetServicesUsingLibraryTest()
	{
		NovaError novaError = LibraryManagerError.getUnableGetServicesUsingLibrary();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_GET_SERVICES_USING_LIBRARY, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableRemoveLibraryTest()
	{
		NovaError novaError = LibraryManagerError.getUnableRemoveLibrary();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_REMOVE_LIBRARY, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
				() -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void getUnableRemoveUsedLibrariesTest()
	{
		String env = RandomStringUtils.randomAlphabetic(10);
		NovaError novaError = LibraryManagerError.getUnableRemoveUsedLibraries(env);
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_REMOVE_USAGES, novaError.getErrorCode()),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(novaError.getErrorMessage().contains(env)),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}

	@Test
	public void postUnableToValidateRequirementsTest()
	{
		NovaError novaError = LibraryManagerError.postUnableToValidateRequirements();
		Assertions.assertAll("Error generating Library Manager Error Instance",
				() -> Assertions.assertEquals(Constants.LibraryError.UNABLE_TO_VALIDATE_REQUIREMENTS, novaError.getErrorCode()),
				() -> Assertions.assertTrue(Constants.LibraryError.UNABLE_TO_VALIDATE_REQUIREMENTS_MSG.equals(novaError.getErrorMessage())),
				() -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
				() -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
		);
	}
}

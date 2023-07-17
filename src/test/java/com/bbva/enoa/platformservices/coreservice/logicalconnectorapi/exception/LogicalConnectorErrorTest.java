package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogicalConnectorErrorTest
{

	private static final String CLASS_NAME = "LogicalConnectorError";
	private static final String UNEXPECTED_ERROR_CODE = "LOGICALCONNECTOR-000";
	private static final String LOGICALCONNECTOR_001 = "LOGICALCONNECTOR-001";
	private static final String LOGICALCONNECTOR_002 = "LOGICALCONNECTOR-002";
	private static final String LOGICALCONNECTOR_003 = "LOGICALCONNECTOR-003";
	private static final String LOGICALCONNECTOR_004 = "LOGICALCONNECTOR-004";
	private static final String LOGICALCONNECTOR_005 = "LOGICALCONNECTOR-005";
	private static final String LOGICALCONNECTOR_006 = "LOGICALCONNECTOR-006";
	private static final String LOGICALCONNECTOR_007 = "LOGICALCONNECTOR-007";
	private static final String LOGICALCONNECTOR_008 = "LOGICALCONNECTOR-008";
	private static final String LOGICALCONNECTOR_009 = "LOGICALCONNECTOR-009";
	private static final String LOGICALCONNECTOR_010 = "LOGICALCONNECTOR-010";
	private static final String LOGICALCONNECTOR_011 = "LOGICALCONNECTOR-011";
	private static final String LOGICALCONNECTOR_012 = "LOGICALCONNECTOR-012";
	private static final String LOGICALCONNECTOR_013 = "LOGICALCONNECTOR-013";
	private static final String LOGICALCONNECTOR_014 = "LOGICALCONNECTOR-014";
	private static final String LOGICALCONNECTOR_015 = "LOGICALCONNECTOR-015";
	private static final String LOGICALCONNECTOR_016 = "LOGICALCONNECTOR-016";
	private static final String LOGICALCONNECTOR_017 = "LOGICALCONNECTOR-017";
	private static final String LOGICALCONNECTOR_018 = "LOGICALCONNECTOR-018";
	private static final String USER_002 = "USER-002";

	@Test
	public void getClassName()
	{
		assertEquals(CLASS_NAME, LogicalConnectorError.getClassName());
	}

    @Test
    public void getUnexpectedError()
    {
        NovaError novaError = LogicalConnectorError.getUnexpectedError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(UNEXPECTED_ERROR_CODE, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.FATAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getLogicalConnectorNotAvailableError()
    {
        NovaError novaError = LogicalConnectorError.getLogicalConnectorNotAvailableError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_001, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchProductError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchProductError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_002, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchLogicalConnectorError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchLogicalConnectorError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_003, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getLogicalConnectionDeletionError()
    {
        NovaError novaError = LogicalConnectorError.getLogicalConnectionDeletionError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_004, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDeleteUsedLogicalConnectorError()
    {
        NovaError novaError = LogicalConnectorError.getDeleteUsedLogicalConnectorError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_005, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDuplicatedLogicalConnectorNameError()
    {
        NovaError novaError = LogicalConnectorError.getDuplicatedLogicalConnectorNameError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_006, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getTriedToArchiveUsedLogicalConnectorError()
    {
        NovaError novaError = LogicalConnectorError.getTriedToArchiveUsedLogicalConnectorError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_007, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchConnectorTypeError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchConnectorTypeError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_008, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchCPDNameError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchCPDNameError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_009, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getHasToDoTaskPendingError()
    {
        NovaError novaError = LogicalConnectorError.getHasToDoTaskPendingError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_010, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getTriedToArchiveLogicalConnectorNotCreatedError()
    {
        NovaError novaError = LogicalConnectorError.getTriedToArchiveLogicalConnectorNotCreatedError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_011, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getTriedToArchiveLogicalConnectorNotUsedError()
    {
        NovaError novaError = LogicalConnectorError.getTriedToArchiveLogicalConnectorNotUsedError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_012, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getTriedToRestoreLogicalConnectorError()
    {
        NovaError novaError = LogicalConnectorError.getTriedToRestoreLogicalConnectorError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_013, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getCannotRequestPropertiesError()
    {
        NovaError novaError = LogicalConnectorError.getCannotRequestPropertiesError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_014, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchPortalUserError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchPortalUserError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_015, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchJiraProjectKeyError()
    {
        NovaError novaError = LogicalConnectorError.getNoSuchJiraProjectKeyError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_016, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getLogicalConnectorNotCreatedError()
    {
        NovaError novaError = LogicalConnectorError.getLogicalConnectorNotCreatedError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_017, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getActionFrozenError()
    {
        String logicalConnectorName = RandomStringUtils.randomAlphabetic(10);
        Integer logicalConnectorId = RandomUtils.nextInt(1, 10);
        String environment = RandomStringUtils.randomAlphabetic(10);
        String userCode = RandomStringUtils.randomAlphabetic(10);
        NovaError novaError = LogicalConnectorError.getActionFrozenError(logicalConnectorName, logicalConnectorId, environment, userCode);
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(LOGICALCONNECTOR_018, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(logicalConnectorName)),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(logicalConnectorId.toString())),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(environment)),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(userCode)),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getForbiddenError()
    {
        NovaError novaError = LogicalConnectorError.getForbiddenError();
        Assertions.assertAll("Error generating Logical Connector Error Instance",
                () -> assertEquals(USER_002, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }
}
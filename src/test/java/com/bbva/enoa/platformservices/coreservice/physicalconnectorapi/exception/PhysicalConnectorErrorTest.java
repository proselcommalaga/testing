package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhysicalConnectorErrorTest
{

    private static final String CLASS_NAME = "PhysicalConnectorError";
    private static final String UNEXPECTED_ERROR_CODE = "PHYSICALCONNECTOR-000";
    private static final String PHYSICALCONNECTOR_001 = "PHYSICALCONNECTOR-001";
    private static final String PHYSICALCONNECTOR_002 = "PHYSICALCONNECTOR-002";
    private static final String PHYSICALCONNECTOR_003 = "PHYSICALCONNECTOR-003";
    private static final String PHYSICALCONNECTOR_004 = "PHYSICALCONNECTOR-004";
    private static final String PHYSICALCONNECTOR_005 = "PHYSICALCONNECTOR-005";
    private static final String PHYSICALCONNECTOR_006 = "PHYSICALCONNECTOR-006";
    private static final String PHYSICALCONNECTOR_007 = "PHYSICALCONNECTOR-007";
    private static final String PHYSICALCONNECTOR_008 = "PHYSICALCONNECTOR-008";
    private static final String PHYSICALCONNECTOR_009 = "PHYSICALCONNECTOR-009";
    private static final String PHYSICALCONNECTOR_010 = "PHYSICALCONNECTOR-010";
    private static final String PHYSICALCONNECTOR_011 = "PHYSICALCONNECTOR-011";
    private static final String PHYSICALCONNECTOR_012 = "PHYSICALCONNECTOR-012";
    private static final String PHYSICALCONNECTOR_013 = "PHYSICALCONNECTOR-013";
    private static final String PHYSICALCONNECTOR_014 = "PHYSICALCONNECTOR-014";
    private static final String PHYSICALCONNECTOR_015 = "PHYSICALCONNECTOR-015";
    private static final String PHYSICALCONNECTOR_016 = "PHYSICALCONNECTOR-016";
    private static final String PHYSICALCONNECTOR_017 = "PHYSICALCONNECTOR-017";
    private static final String PHYSICALCONNECTOR_018 = "PHYSICALCONNECTOR-018";
    private static final String PHYSICALCONNECTOR_019 = "PHYSICALCONNECTOR-019";
    private static final String PHYSICALCONNECTOR_020 = "PHYSICALCONNECTOR-020";
    private static final String PHYSICALCONNECTOR_021 = "PHYSICALCONNECTOR-021";
    private static final String PHYSICALCONNECTOR_022 = "PHYSICALCONNECTOR-022";
    private static final String PHYSICALCONNECTOR_023 = "PHYSICALCONNECTOR-023";
    private static final String PHYSICALCONNECTOR_024 = "PHYSICALCONNECTOR-024";
    private static final String PHYSICALCONNECTOR_025 = "PHYSICALCONNECTOR-025";
    private static final String PHYSICALCONNECTOR_026 = "PHYSICALCONNECTOR-026";
    private static final String PHYSICALCONNECTOR_027 = "PHYSICALCONNECTOR-027";
    private static final String PHYSICALCONNECTOR_028 = "PHYSICALCONNECTOR-028";
    private static final String PHYSICALCONNECTOR_029 = "PHYSICALCONNECTOR-029";
    private static final String PHYSICALCONNECTOR_030 = "PHYSICALCONNECTOR-030";
    private static final String PHYSICALCONNECTOR_031 = "PHYSICALCONNECTOR-031";
    private static final String PHYSICALCONNECTOR_032 = "PHYSICALCONNECTOR-032";
    private static final String PHYSICALCONNECTOR_033 = "PHYSICALCONNECTOR-033";
    private static final String PHYSICALCONNECTOR_034 = "PHYSICALCONNECTOR-034";
    private static final String USER_002 = "USER-002";

    @Test
    public void getClassName()
    {
        assertEquals(CLASS_NAME, PhysicalConnectorError.getClassName());
    }

    @Test
    public void getUnexpectedError()
    {
        NovaError novaError = PhysicalConnectorError.getUnexpectedError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(UNEXPECTED_ERROR_CODE, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.FATAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableDeleteConnectorTypeDueLogicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableDeleteConnectorTypeDueLogicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_001, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableDeleteConnectorTypeDuePhysicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableDeleteConnectorTypeDuePhysicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_002, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchPhysicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchPhysicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_003, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.CRITICAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToDeletePhysicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToDeletePhysicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_004, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getHasDeletePhysicalConnectorTaskPendingError()
    {
        NovaError novaError = PhysicalConnectorError.getHasDeletePhysicalConnectorTaskPendingError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_005, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDuplicatedPhysicalConnectorNameError()
    {
        String physicalConnectorName = RandomStringUtils.randomAlphabetic(10);
        String environment = RandomStringUtils.randomAlphabetic(10);
        NovaError novaError = PhysicalConnectorError.getDuplicatedPhysicalConnectorNameError(physicalConnectorName, environment);
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_006, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(physicalConnectorName)),
                () -> Assertions.assertTrue(novaError.getErrorMessage().contains(environment)),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getIpNotValidError()
    {
        NovaError novaError = PhysicalConnectorError.getIpNotValidError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_007, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchConnectorTypeError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchConnectorTypeError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_008, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDuplicatedConnectorTypeError()
    {
        NovaError novaError = PhysicalConnectorError.getDuplicatedConnectorTypeError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_009, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchCPDNameError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchCPDNameError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_010, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToDisassociateLogicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToDisassociateLogicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_011, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToAssociateLogicalConnectorDueEnvironmentError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToAssociateLogicalConnectorDueEnvironmentError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_012, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDuplicatedConnectorTypePropertyNameError()
    {
        NovaError novaError = PhysicalConnectorError.getDuplicatedConnectorTypePropertyNameError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_013, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchConnectorTypePropertyIdError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchConnectorTypePropertyIdError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_014, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToAssociateLogicalConnectorDueConnectorTypeError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToAssociateLogicalConnectorDueConnectorTypeError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_015, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToAssociateLogicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToAssociateLogicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_016, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchLogicalConnectorError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchLogicalConnectorError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_017, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.CRITICAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getHasCheckLogicalConnectorPropertyTaskPendingError()
    {
        NovaError novaError = PhysicalConnectorError.getHasCheckLogicalConnectorPropertyTaskPendingError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_018, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getConnectorTypeRequiredError()
    {
        NovaError novaError = PhysicalConnectorError.getConnectorTypeRequiredError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_019, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.BAD_REQUEST, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoFoundPhysicalConnectorPortError()
    {
        NovaError novaError = PhysicalConnectorError.getNoFoundPhysicalConnectorPortError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_020, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.BAD_REQUEST, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getPendingPortPhysicalConnectorStatusError()
    {
        NovaError novaError = PhysicalConnectorError.getPendingPortPhysicalConnectorStatusError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_021, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getInputPortValueAlreadyInUseError()
    {
        NovaError novaError = PhysicalConnectorError.getInputPortValueAlreadyInUseError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_022, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getInputPortNameAlreadyInUseError()
    {
        NovaError novaError = PhysicalConnectorError.getInputPortNameAlreadyInUseError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_023, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getConnectorPropertyDeletionError()
    {
        NovaError novaError = PhysicalConnectorError.getConnectorPropertyDeletionError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_024, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchPortalUserError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchPortalUserError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_025, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getUnableToAssociateLogicalConnectorErrorStatusError()
    {
        NovaError novaError = PhysicalConnectorError.getUnableToAssociateLogicalConnectorErrorStatusError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_026, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getDuplicatedVirtualIpError()
    {
        NovaError novaError = PhysicalConnectorError.getDuplicatedVirtualIpError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_027, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getPhysicalConnectorPortAlreadyInUseError()
    {
        NovaError novaError = PhysicalConnectorError.getPhysicalConnectorPortAlreadyInUseError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_028, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchPhysicalConnectorPortError()
    {
        NovaError novaError = PhysicalConnectorError.getNoSuchPhysicalConnectorPortError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_029, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.CRITICAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getIpPortFormatNotValidError()
    {
        NovaError novaError = PhysicalConnectorError.getIpPortFormatNotValidError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_030, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNumberPortNotValidError()
    {
        NovaError novaError = PhysicalConnectorError.getNumberPortNotValidError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_031, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.CONFLICT, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getRangeNumberPortNotValidError()
    {
        NovaError novaError = PhysicalConnectorError.getRangeNumberPortNotValidError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_032, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getConnectorTypePropertyRequiredError()
    {
        NovaError novaError = PhysicalConnectorError.getConnectorTypePropertyRequiredError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_033, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.BAD_REQUEST, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getPhysicalConnectorRequiredError()
    {
        NovaError novaError = PhysicalConnectorError.getPhysicalConnectorRequiredError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(PHYSICALCONNECTOR_034, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.BAD_REQUEST, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.WARNING, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getForbiddenError()
    {
        NovaError novaError = PhysicalConnectorError.getForbiddenError();
        Assertions.assertAll("Error generating Physical Connector Error Instance",
                () -> assertEquals(USER_002, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> assertEquals(HttpStatus.FORBIDDEN, novaError.getHttpStatus()),
                () -> assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }
}
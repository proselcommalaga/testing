/*********************************************************************************************************************************
 This class has been automatically generated using KLTT-APIRestGenerator project, don't do manual file modifications.
 Tue Nov 28 14:21:35 CET 2017

 "Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements;
 and to You under the Apache License, Version 2.0. "
 **********************************************************************************************************************************/

package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.listener;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorUpdateDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.NewLogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.server.spring.nova.rest.IRestListenerLogicalconnectorapi;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.exception.LogicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener of the Logical Connector API
 * ------------------------------------------------
 * @author BBVA - XE30432
 * ------------------------------------------------
 */
@Service
public class ListenerLogicalconnectorapi implements IRestListenerLogicalconnectorapi
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListenerLogicalconnectorapi.class);

    /**
     * Logical connector service
     */
    private final ILogicalConnectorService iLogicalConnectorService;


    /**
     * Dependency injection constructor
     *
     * @param iLogicalConnectorService logical Connector Service
     */
    @Autowired
    public ListenerLogicalconnectorapi(final ILogicalConnectorService iLogicalConnectorService)
    {
        this.iLogicalConnectorService = iLogicalConnectorService;
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public String testLogicalConnector(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iLogicalConnectorService.testLogicalConnector(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public LogicalConnectorDto getLogicalConnector(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iLogicalConnectorService.getLogicalConnector(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void updateLogicalConnector(NovaMetadata novaMetadata, LogicalConnectorUpdateDto logicalConnectorUpdateDto, Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iLogicalConnectorService.updateLogicalConnector(logicalConnectorUpdateDto, logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void archiveLogicalConnector(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iLogicalConnectorService.archiveLogicalConnector(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getLogicalConnectorsStatuses(NovaMetadata novaMetadata) throws Errors
    {
        return this.iLogicalConnectorService.getLogicalConnectorsStatuses();
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public Boolean isLogicalConnectorFrozen(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        return this.iLogicalConnectorService.isLogicalConnectorFrozen(logicalConnectorId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void deleteLogicalConnector(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iLogicalConnectorService.deleteLogicalConnector(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public LogicalConnectorDto[] getAllFromProduct(final NovaMetadata novaMetadata, final Integer productId,
                                                   final String connectorType, final String environment) throws Errors
    {
        try
        {
            return this.iLogicalConnectorService.getAllFromProduct(productId, connectorType, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public Integer requestProperties(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iLogicalConnectorService.requestProperties(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void restoreLogicalConnector(final NovaMetadata novaMetadata, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iLogicalConnectorService.restoreLogicalConnector(logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE)
    public Integer createLogicalConnector(final NovaMetadata novaMetadata, final NewLogicalConnectorDto logicalConnectorToCreate,
                                          final Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            return this.iLogicalConnectorService.createLogicalConnector(logicalConnectorToCreate, productId, ivUser);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.LOGICAL_CONNECTOR_API, runtimeExceptionErrorCode = LogicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getConnectorTypes(final NovaMetadata novaMetadata) throws Errors
    {
        return this.iLogicalConnectorService.getConnectorTypes();
    }
}

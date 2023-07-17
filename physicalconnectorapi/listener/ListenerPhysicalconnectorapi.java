package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.listener;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.EditPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.server.spring.nova.rest.IRestListenerPhysicalconnectorapi;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception.PhysicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorService;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils.Constants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Listener of the Physical Connector API
 * ------------------------------------------------
 *
 * @author BBVA 
 * ------------------------------------------------
 */
@Component
public class ListenerPhysicalconnectorapi implements IRestListenerPhysicalconnectorapi
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListenerPhysicalconnectorapi.class);

    /**
     * Physical connector service
     */
    private final IPhysicalConnectorService iPhysicalConnectorService;

    /**
     * Dependency injection constructor
     *
     * @param iPhysicalConnectorService physical connector service
     */
    @Autowired
    public ListenerPhysicalconnectorapi(final IPhysicalConnectorService iPhysicalConnectorService)
    {
        this.iPhysicalConnectorService = iPhysicalConnectorService;
    }

    ////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////////////////////////////////

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getManagementTypes(final NovaMetadata novaMetadata) throws Errors
    {
        return this.iPhysicalConnectorService.getManagementTypes();
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void createPhysicalConnector(final NovaMetadata novaMetadata, final NewPhysicalConnectorDto physicalConnectorToCreate) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.createPhysicalConnector(physicalConnectorToCreate, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void deleteConnectorType(final NovaMetadata novaMetadata, final Integer connectorTypeId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.deleteConnectorType(connectorTypeId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getPropertyTypes(final NovaMetadata novaMetadata) throws Errors
    {
        return this.iPhysicalConnectorService.getPropertyTypes();
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void deleteConnectorTypeProperty(final NovaMetadata novaMetadata, final Integer connectorTypeId, final Integer connectorTypePropertyId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.deleteConnectorTypeProperty(connectorTypePropertyId, connectorTypeId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public PhysicalConnectorDto getPhysicalConnector(final NovaMetadata novaMetadata, final Integer physicalConnectorId) throws Errors
    {
        return this.iPhysicalConnectorService.getPhysicalConnector(physicalConnectorId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public Integer deletePhysicalConnector(final NovaMetadata novaMetadata, final Integer physicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iPhysicalConnectorService.deletePhysicalConnector(physicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void disassociateConnectors(final NovaMetadata novaMetadata, final Integer physicalConnectorId, final Integer logicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.disassociateConnectors(physicalConnectorId, logicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void addConnectorTypeProperty(final NovaMetadata novaMetadata, final ConnectorTypePropertyDto property, final Integer connectorTypeId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.addConnectorTypeProperty(property, connectorTypeId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ConnectorTypeDto getConnectorType(final NovaMetadata novaMetadata, final Integer connectorTypeId) throws Errors
    {
        return this.iPhysicalConnectorService.getConnectorType(connectorTypeId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void associateConnectors(final NovaMetadata novaMetadata, final Integer physicalConnectorId, final Integer logicalConnectorId,
                                    final Integer physicalConnectorPortId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.associateConnectors(physicalConnectorId, logicalConnectorId,
                physicalConnectorPortId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public PhysicalConnectorDto[] getAllPhysicalConnectors(final NovaMetadata novaMetadata, final String connectorType, final String environment) throws Errors
    {
        return this.iPhysicalConnectorService.getAllPhysicalConnector(connectorType, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getScopeTypes(final NovaMetadata novaMetadata) throws Errors
    {
        return this.iPhysicalConnectorService.getScopeTypes();
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ConnectorTypePropertyDto[] getConnectorTypesProperties(final NovaMetadata novaMetadata, final Integer connectorTypeId) throws Errors
    {
        return this.iPhysicalConnectorService.getConnectorTypeProperties(connectorTypeId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void createNewConnectorType(final NovaMetadata novaMetadata, final NewConnectorTypeDto newConnectorTypeDto) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.createNewConnectorType(newConnectorTypeDto, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE)
    public void editPhysicalConnector(final NovaMetadata novaMetadata, final EditPhysicalConnectorDto editPhysicalConnectorDto, final Integer physicalConnectorId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iPhysicalConnectorService.editPhysicalConnector(editPhysicalConnectorDto, physicalConnectorId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PHYSICAL_CONNECTOR_API, runtimeExceptionErrorCode = PhysicalConnectorError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ConnectorTypeDto[] getConnectorTypes(final NovaMetadata novaMetadata) throws Errors
    {
        return this.iPhysicalConnectorService.getConnectorTypes();
    }
}


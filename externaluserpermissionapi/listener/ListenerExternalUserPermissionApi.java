package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.listener;

import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PermissionDTO;
import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PlatformDTO;
import com.bbva.enoa.apirestgen.externaluserpermissionapi.server.spring.nova.rest.IRestListenerExternaluserpermissionapi;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.services.interfaces.IExternalUserPermission;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener External User permission
 */
@Slf4j
@Service
public class ListenerExternalUserPermissionApi implements IRestListenerExternaluserpermissionapi
{
    private final IExternalUserPermission externalUserPermission;

    @Autowired
    public ListenerExternalUserPermissionApi(final IExternalUserPermission externalUserPermission)
    {
        this.externalUserPermission = externalUserPermission;
    }

    @Override
    @LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public void deletePermission(final NovaMetadata novaMetadata, final Integer id) throws Errors
    {
        // delete permission
        this.externalUserPermission.deletePermission(id, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    public PermissionDTO[] getPermissions(final NovaMetadata novaMetadata, final Integer productId, final String environment) throws Errors
    {
        // get the saved permissions
        return this.externalUserPermission.getPermissions(environment, productId);
    }

    @Override
    @LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public void createPermission(final NovaMetadata novaMetadata, final PermissionDTO permission) throws Errors
    {
        // create permission
        this.externalUserPermission.createPermission(permission, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = EtherConstants.ETHER_API, runtimeExceptionErrorCode = EtherConstants.EtherErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public PlatformDTO[] getPlatformPermissions(final NovaMetadata novaMetadata, final Integer productId, final String environment) throws Errors
    {
        // get the platform permissions
        return this.externalUserPermission.getPlatformPermissions(environment, productId);
    }
}

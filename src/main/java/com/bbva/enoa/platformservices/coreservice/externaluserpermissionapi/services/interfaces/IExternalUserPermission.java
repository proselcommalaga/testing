package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.services.interfaces;

import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PermissionDTO;
import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PlatformDTO;

public interface IExternalUserPermission
{
    /**
     * Method to create a permission
     *
     * @param permission the permission to create
     * @param ivUser     the iv-user
     */
    void createPermission(PermissionDTO permission, String ivUser);

    /**
     * Method to delete a permission
     *
     * @param id     the id of the permission to delete
     * @param ivUser the iv-user
     */
    void deletePermission(Integer id, String ivUser);

    /**
     * Get the saved permissions
     *
     * @param environment the environment
     * @param productId   the product id
     * @return an array of saved permissions
     */
    PermissionDTO[] getPermissions(String environment, Integer productId);

    /**
     * Method to get the platform permissions
     *
     * @param environment the environment
     * @param productId   the product id
     * @return An array with the platform permissions
     */
    PlatformDTO[] getPlatformPermissions(String environment, Integer productId);
}

package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.usersapi.model.USProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;

import java.util.List;

/**
 * Call to users client to check permissions
 */
public interface IProductUsersClient extends IUsersClient
{

    /**
     * Removes all the users of a product
     *
     * @param ivUser    the user
     * @param productId the product id
     */
    void deleteProductUsers(String ivUser, Integer productId);

    /**
     * Gets the product owners for a given product
     *
     * @param productId the product id
     * @param role      the role
     * @return the list of product owners
     */
    List<USUserDTO> getProductUsersByTeam(Integer productId, String role);

    /**
     * Add a member to a product
     *
     * @param usProductUser user and role
     * @param ivUser        logged user
     * @param productId     product id
     */
    void addUserToProduct(USProductUserDTO usProductUser, String ivUser, Integer productId);

    /**
     * Removes an user from a product
     *
     * @param ivUser        logged user
     * @param productId     product id
     * @param usProductUser the user to remove
     */
    void removeUserFromProduct(String ivUser, Integer productId, USProductUserDTO usProductUser);

    /**
     * Gets the products for a given user
     *
     * @param userCode the user
     * @return the list of products
     */
    int[] getProductsByUser(String userCode);
}

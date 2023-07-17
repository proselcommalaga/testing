package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productsapi.model.ProductRequestDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Services for end point new product request
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public interface INewProductRequestService
{
    /**
     * Create a request for new product
     *
     * @param newProductCreationRequest the request info
     * @param ivUser                    the user asking for product creation
     * @return the id of the created task
     * @throws NovaException in case of error
     */
    Integer newProductRequest(ProductRequestDTO newProductCreationRequest, String ivUser) throws NovaException;

    /**
     * Create a new product for an userCode (the product owner) in inital status: CREATING
     * Insert and save this new product into BBDD
     *
     * @param createNewProduct instance that contains all the parameter for creating the product
     * @param usUser           user that will be associated to the product. This user must be the product owner
     * @return a new product inserted into BBDD
     * @throws NovaException productsAPIException
     */
    Product createAndSaveNewProduct(final ProductRequestDTO createNewProduct, final USUserDTO usUser);
}

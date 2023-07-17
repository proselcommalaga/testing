package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Interface for Api manager repository validator service
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IApiManagerValidator
{

    void checkUploadApiPermission(final ApiType apiType, final Integer productId);

    void checkCreatePolicyTaskPermission(final TaskInfoDto taskInfoDto);

    void checkDeleteApiPermission(final ApiType apiType, final Integer productId);

    /**
     * Checks if a {@link Product} exists in NOVA.
     *
     * @param productId {@link Product} ID.
     * @return product
     */
    Product checkProductExistence(final int productId);

    /**
     * Check task info dto parameters
     *
     * @param taskInfoDto the task info dto
     */
    void checkTaskDtoParameters(final TaskInfoDto taskInfoDto);

    /**
     * Checks if the Api exists in database
     *
     * @param apiId     the api id to check
     * @param productId product id
     * @return the stored Sync Api
     * @throws NovaException if Sync Api does not exists
     */
    Api<?, ?, ?> checkApiExistence(final Integer apiId, final Integer productId);

    /**
     * Checks if the {@link ApiVersion} exists and belongs to the product
     *
     * @param productId  Product id
     * @param apiVersion API id
     * @return Nova API
     */
    ApiVersion<?,?,?> checkApiVersionExistence(final Integer productId, final Integer apiVersion);

    /**
     * Checks if the product exists, then obtains all the {@link Api}s into the repository of a product
     *
     * @param productId Product id
     * @return Nova Api list for the product
     */
    List<Api<?,?,?>> filterByProductId(final int productId);

    /**
     * Checks if a {@link Product} exists in NOVA.
     *
     * @param serviceId Service Id
     * @return service
     */
    ReleaseVersionService checkServiceExistence(final int serviceId);

    /**
     * Checks if the Api Version already existed because it cannot exist twice
     *
     * @param api the api containing the version
     * @param version new version updated
     */
    void assertVersionOfApiNotExists(final Api<?,?,?> api, final String version);

    /**
     * Checks if a {@link ApiVersion} is available to be deleted
     *
     * @param apiVersion sync API version
     */
    ApiErrorList isApiVersionErasable(final ApiVersion<?,?,?> apiVersion);

    /**
     * Validate if exists a DocSystem with the given ID, category and type.
     *
     * @param docSystemId       The given ID.
     * @param docSystemCategory The given category.
     * @param docSystemType     The given type.
     * @return The DocSystem.
     * @throws NovaException If the DocSystem doesn't exist.
     */
    DocSystem validateAndGetDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType);

    <A extends Api<?,?,?>> A findAndValidateOrPersistIfMissing(final A api);

}

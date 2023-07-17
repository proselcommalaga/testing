package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodProfileDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.PolicyTaskReplyParametersDTO;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Interface for API manager listener
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IApiManagerService
{
    /**
     * Get all APIs from a product
     *
     * @param productId Product identifier
     * @return List of API models
     * @throws NovaException exception
     */
    ApiDto[] getProductApis(final Integer productId);

    /**
     * Upload a new API
     *
     * @param upload    Api upload information
     * @param productId Product identifier
     * @return List of errors
     * @throws NovaException exception
     */
    ApiErrorList uploadProductApis(final ApiUploadRequestDto upload, final Integer productId);

    /**
     * Delete product api
     *
     * @param productId    Product identifier
     * @param apiVersionId API identifier
     * @return List of errors
     * @throws NovaException exception
     */
    ApiErrorList deleteProductApi(final Integer productId, final Integer apiVersionId);

    /**
     * Download product api
     *
     * @param productId    Product identifier
     * @param apiVersionId API identifier
     * @param format       Format - YML or JSON
     * @param downloadType Type - server or consumed
     * @return File contents
     * @throws NovaException exception
     */
    byte[] downloadProductApi(final Integer productId, final Integer apiVersionId, final String format, final String downloadType);

    /**
     * Create a task to establish policies
     *
     * @param taskInfoDto task information
     */
    void createApiTask(TaskInfoDto taskInfoDto);

    /**
     * Update api policy status with the closing task status
     *
     * @param parametersDTO parameters wrapper for post process operation.
     */
    void onPolicyTaskReply(PolicyTaskReplyParametersDTO parametersDTO);

    /**
     * Save the plan profile info defined by the user
     *
     * @param planProfileInfo information specified at nova portal
     * @param planId          plan id
     */
    void savePlanApiProfile(final ApiMethodProfileDto[] planProfileInfo, final Integer planId);

    /**
     * Recover DTO to show Api Plan Detail
     *
     * @param planId id of plan
     * @return the dto
     * @throws NovaException in case of error
     */
    ApiPlanDetailDto getPlanApiDetailList(Integer planId);

    /**
     * Get Api Status
     *
     * @return An array containing Api Status
     */
    String[] getApiStatus();

    /**
     * Get Api Types
     *
     * @return An array containing Api Types
     */
    String[] getApiTypes();

    /**
     * Get detail of requested Api
     *
     * @param apiId     api Id
     * @param productId product id
     * @return detail of the api
     * @throws NovaException if the apiId does not exists
     */
    ApiDetailDto getApiDetail(final Integer apiId, final Integer productId);

    /**
     * Get detail of requested Api version
     *
     * @param apiVersionId api version Id
     * @param productId    product id
     * @return detail of the api version
     * @throws NovaException if the api version Id does not exists
     */
    ApiVersionDetailDto getApiVersionDetail(final Integer apiVersionId, final Integer productId , final String filterByDeploymentStatus);

    /**
     * Sets api state to deployed for the apis in the plan
     *
     * @param planId id of the deployed plan
     */
    void refreshDeployedApiVersionsState(Integer planId);

    /**
     * Sets api state to implemented for the apis in the plan if they are not deployed in another plan
     *
     * @param planId id of the deployed plan
     */
    void refreshUndeployedApiVersionsState(Integer planId);

    /**
     * Sets api state to definition for the apis in the release version if they are not implemented in another rv
     *
     * @param releaseVersion
     */
    void refreshUnimplementedApiVersionsState(final ReleaseVersion releaseVersion);

    /**
     * Get the NOVA APIs that are using a MSA document given by its ID.
     *
     * @param msaDocumentId The ID of the MSA document.
     * @return A List of NOVA APIs.
     */
    List<? extends Api<?, ?, ?>> getApisUsingMsaDocument(Integer msaDocumentId);

    /**
     * Get the NOVA APIs that are using an ARA document given by its ID.
     *
     * @param araDocumentId The ID of the ARA document.
     * @return A List of NOVA APIs.
     */
    List<? extends Api<?, ?, ?>> getApisUsingAraDocument(Integer araDocumentId);
}

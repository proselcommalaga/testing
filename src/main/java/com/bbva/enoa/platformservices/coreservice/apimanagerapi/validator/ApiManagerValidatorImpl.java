package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IApiManagerValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Api manager repository validator service
 */
@Service
@AllArgsConstructor
public class ApiManagerValidatorImpl implements IApiManagerValidator
{

    private static final Logger LOG = LoggerFactory.getLogger(ApiManagerValidatorImpl.class);
    protected static final NovaException PERMISSION_DENIED = new NovaException(ApiManagerError.getForbiddenError(), ApiManagerError.getForbiddenError().toString());
    private final ProductRepository productRepository;
    private final ApiRepository apiRepository;
    private final DeploymentPlanRepository deploymentPlanRepository;
    private final ReleaseVersionServiceRepository serviceRepository;
    private final ApiVersionRepository apiVersionRepository;
    private final IDocSystemService docSystemService;
    private final ApiImplementationRepository apiImplementationRepository;
    private final IProductUsersClient usersClient;
    private final NovaContext novaContext;
    private final IApiManagerValidatorModalityBased apiManagerValidatorServiceModalityBased;

    public void checkUploadApiPermission(final ApiType apiType, final Integer productId)
    {
        String ivUser = novaContext.getIvUser();
        switch (apiType)
        {
            case NOT_GOVERNED:
                this.usersClient.checkHasPermission(ivUser, Constants.UPLOAD_API_PERMISSION, productId, PERMISSION_DENIED);
                break;
            case GOVERNED:
                this.usersClient.checkHasPermission(ivUser, Constants.UPLOAD_GOVERNED_API_PERMISSION, PERMISSION_DENIED);
                break;
            case EXTERNAL:
                this.usersClient.checkHasPermission(ivUser, Constants.UPLOAD_EXTERNAL_API_PERMISSION, PERMISSION_DENIED);
                break;
            default:
        }
    }

    public void checkCreatePolicyTaskPermission(final TaskInfoDto taskInfoDto)
    {
        this.usersClient.checkHasPermission(novaContext.getIvUser(), Constants.CREATE_POLICY_TASK_PERMISSION, taskInfoDto.getProductId(), PERMISSION_DENIED);
    }

    public void checkDeleteApiPermission(final ApiType apiType, final Integer productId)
    {
        String ivUser = novaContext.getIvUser();
        switch (apiType)
        {
            case NOT_GOVERNED:
                this.usersClient.checkHasPermission(ivUser, Constants.DELETE_API_PERMISSION, productId, PERMISSION_DENIED);
                break;
            case GOVERNED:
                this.usersClient.checkHasPermission(ivUser, Constants.DELETE_GOVERNED_API_PERMISSION, PERMISSION_DENIED);
                break;
            case EXTERNAL:
                this.usersClient.checkHasPermission(ivUser, Constants.DELETE_EXTERNAL_API_PERMISSION, PERMISSION_DENIED);
                break;
        }
    }

    @Override
    public Product checkProductExistence(final int productId)
    {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NovaException(ApiManagerError.getProductNotFoundError(productId),
                String.format("[%s] -> [%s]: The product ID [%d] does not exists.", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL, "checkProductExistence", productId)));

        LOG.debug("[{}] -> [{}]: Product [{}] has a valid ID", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                "checkProductExistence", productId);

        return product;
    }

    @Override
    public void checkTaskDtoParameters(final TaskInfoDto taskInfoDto)
    {
        if (taskInfoDto.getNovaApiId() == null || taskInfoDto.getProductId() == null)
        {
            throw new NovaException(ApiManagerError.getTaskInfoError());
        }

        if (taskInfoDto.getAraDocumentId() == null || taskInfoDto.getMsaDocumentId() == null)
        {
            throw new NovaException(ApiManagerError.getTaskInfoDocumentsNotProvidedError());
        }
    }

    @Override
    public Api<?, ?, ?> checkApiExistence(final Integer apiId, final Integer productId)
    {
        Api<?, ?, ?> api = this.apiRepository.findById(apiId).orElseThrow(() -> new NovaException(ApiManagerError.getApiNotFoundError(apiId),
                String.format("[%s] -> [%s]: The api ID [%d] does not exists.", Constants
                        .API_MANAGER_VALIDATOR_SERVICE_IMPL, "checkSyncApiExistence", apiId)));
        this.checkIfApiBelongsToProduct(api, productId);

        return api;
    }


    @Override
    public void assertVersionOfApiNotExists(final Api<?,?,?> api, final String version)
    {
        api.getApiVersions().stream()
                .filter(apiVersion -> apiVersion.getVersion().equals(version))
                .findAny()
                .ifPresent(apiVersion -> {
                    throw new NovaException(ApiManagerError.getInvalidNewApiError(api.getName(), version));
                });
    }

    @Override
    public ApiVersion<?,?,?> checkApiVersionExistence(final Integer productId, final Integer apiVersionId)
    {
        ApiVersion<?,?,?> apiVersion = this.apiVersionRepository.findById(apiVersionId).orElseThrow(() -> new NovaException(ApiManagerError.getApiVersionNotFoundError(apiVersionId),
                String.format("[%s] -> [%s]: The api ID [%d] does not exists.", Constants
                        .API_MANAGER_VALIDATOR_SERVICE_IMPL, "checkSyncApiVersionExistence", apiVersionId)));

        LOG.debug("[{}] -> [{}]: API [{}] has a valid ID", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                "checkNovaApiExistence", apiVersionId);
        this.checkIfApiBelongsToProduct(apiVersion.getApi(), productId);

        return apiVersion;
    }

    @Override
    public List<Api<?,?,?>> filterByProductId(final int productId)
    {
        this.checkProductExistence(productId);
        return this.apiRepository.findAllByProductId(productId);
    }

    @Override
    public ReleaseVersionService checkServiceExistence(final int serviceId)
    {
        ReleaseVersionService service = serviceRepository.findById(serviceId).orElseThrow(() -> new NovaException(ApiManagerError.getServiceNotFoundError(serviceId),
                String.format("[%s] -> [%s]: The service ID [%d] does not exists.", Constants
                        .API_MANAGER_VALIDATOR_SERVICE_IMPL, "checkServiceExistence", serviceId)));

        LOG.debug("[{}] -> [{}]: Product [{}] has a valid ID", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                "checkServiceExistence", serviceId);
        return service;
    }

    @Override
    public ApiErrorList isApiVersionErasable(final ApiVersion<?,?,?> apiVersion)
    {
        ApiErrorList apiErrorList = new ApiErrorList();
        List<String> errorList = new ArrayList<>();

        for (ApiImplementation<?,?,?> apiImplementation : apiVersion.getApiImplementations())
        {
            List<String> errorServiceList = new ArrayList<>();

            // DEPLOYED
            if (!this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(apiImplementation.getService().
                            getVersionSubsystem().getReleaseVersion().getId(), Environment.INT.getEnvironment(), DeploymentStatus.DEPLOYED)
                    .isEmpty())
            {
                errorServiceList.add(String.format("DEPLOYED with service [%s] in INT environment", apiImplementation.getService().getServiceName()));
            }

            if (!this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(apiImplementation.getService().
                            getVersionSubsystem().getReleaseVersion().getId(), Environment.PRE.getEnvironment(), DeploymentStatus.DEPLOYED)
                    .isEmpty())
            {
                errorServiceList.add(String.format("DEPLOYED with service [%s] in PRE environment", apiImplementation.getService().getServiceName()));
            }

            if (!this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(apiImplementation.getService().
                            getVersionSubsystem().getReleaseVersion().getId(), Environment.PRO.getEnvironment(), DeploymentStatus.DEPLOYED)
                    .isEmpty())
            {
                errorServiceList.add(String.format("DEPLOYED with service [%s] in PRO environment", apiImplementation.getService().getServiceName()));
            }

            // IMPLEMENT
            if (errorServiceList.isEmpty())
            {
                errorServiceList.add((String.format("IMPLEMENTED with release version [%s] that is [%s]", apiImplementation.getService()
                        .getVersionSubsystem().getReleaseVersion().getVersionName(), apiImplementation.getService().getVersionSubsystem().getReleaseVersion()
                        .getStatus().name())));
            }

            errorList.addAll(errorServiceList);
        }

        for (ApiImplementation<?,?,?> apiImplementationAux : this.apiImplementationRepository.findApiImplementationsConsumingApiVersion(apiVersion.getId()))
        {
            errorList.add((String.format("IMPLEMENTED as CONSUMED_BY in release version [%s] that is [%s]", apiImplementationAux.getService()
                    .getVersionSubsystem().getReleaseVersion().getVersionName(), apiImplementationAux.getService().getVersionSubsystem().getReleaseVersion()
                    .getStatus().name())));
        }

        for (ApiImplementation<?,?,?> apiImplementationAux : this.apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId()))
        {
            errorList.add((String.format("IMPLEMENTED as BACKWARD_COMPATIBLE in release version [%s] that is [%s]", apiImplementationAux.getService()
                    .getVersionSubsystem().getReleaseVersion().getVersionName(), apiImplementationAux.getService().getVersionSubsystem().getReleaseVersion()
                    .getStatus().name())));
        }

        if (errorList.isEmpty())
        {
            LOG.debug("[{}] -> [{}]: API [{}] is not in use and is ready to be deleted", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                    "isErasable", apiVersion.getId());
        }
        else
        {
            LOG.error("[{}] -> [{}]: The api [{}] with version [{}] is not erasable. List of service that are using the  API: [{}]",
                    Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL, "isErasable", apiVersion.getApi().getName(),
                    apiVersion.getVersion(), errorList.toArray(new String[0]));
        }

        apiErrorList.setErrorList(errorList.toArray(new String[0]));

        return apiErrorList;
    }

    @Override
    public DocSystem validateAndGetDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        return this.docSystemService.getDocSystemWithIdAndCategoryAndType(docSystemId, docSystemCategory, docSystemType).orElseThrow(() -> new NovaException(ApiManagerError.getDocSystemNotFoundError(docSystemId, docSystemCategory, docSystemType)));
    }

    @Override
    public <A extends Api<?,?,?>> A findAndValidateOrPersistIfMissing(final A api)
    {
        return (A) this.apiManagerValidatorServiceModalityBased.findAndValidateOrPersistIfMissing(api);
    }

    //////////////////////////////////  PRIVATE METHODS  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Checks if the Sync API belongs to the selected product
     *
     * @param api   Sync API
     * @param productId Product id
     */
    private void checkIfApiBelongsToProduct(final Api<?,?,?> api, final int productId)
    {
        if (api.getProduct().getId() != productId)
        {
            throw new NovaException(ApiManagerError.getInvalidApiForProductError(productId, api.getId()));
        }

        LOG.debug("[{}] -> [{}]: Correctly validated that API [{}] belongs to product [{}]", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                "checkIfApiBelongsToProduct", api.getId(), productId);
    }
}

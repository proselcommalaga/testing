package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services;

import com.bbva.enoa.apirestgen.apimanagerapi.model.*;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.IDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal.IApiManagerServiceModalityBased;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl.RepositoryManagerServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for the API manager listener
 * ------------------------------------------------
 * <p>
 * ------------------------------------------------
 */
@Service
@AllArgsConstructor
public class ApiManagerServiceImpl implements IApiManagerService
{

    private static final Logger LOG = LoggerFactory.getLogger(ApiManagerServiceImpl.class);
    public static final String API_NAME_PARAM = "apiName";
    private final IApiManagerServiceModalityBased apiManagerServiceModalityBased;
    private final IDtoBuilder dtoBuilder;
    private final IApiManagerValidator apiManagerValidator;
    private final RepositoryManagerServiceImpl repositoryManagerService;
    private final PlanProfileRepository planProfileRepository;
    private final ApiMethodRepository apiMethodRepository;
    private final ApiMethodProfileRespository apiMethodProfileRespository;
    private final CesRoleRepository cesRoleRepository;
    private final ApiRepository apiRepository;
    private final DeploymentPlanRepository deploymentPlanRepository;
    private final ApiVersionRepository apiVersionRepository;
    private final INovaActivityEmitter novaActivityEmitter;
    private final ReleaseVersionServiceRepository releaseVersionServiceRepository;
    private final ProfilingUtils profilingUtils;
    private final PlanProfilingUtils planProfilingUtils;

    @Override
    public ApiDto[] getProductApis(final Integer productId)
    {
        return this.dtoBuilder.buildApiDtoArray(productId);
    }

    @Override
    public ApiErrorList uploadProductApis(final ApiUploadRequestDto upload, final Integer productId)
    {

        if (!EnumUtils.isValidEnum(ApiModality.class, upload.getApiModality()))
        {
            final NovaError novaError = ApiManagerError.getApiModalityError(upload.getApiModality(), Arrays.toString(ApiModality.values()));
            throw new NovaException(novaError, novaError.toString());
        }

        if (!EnumUtils.isValidEnum(ApiType.class, upload.getApiType()))
        {
            final NovaError novaError = ApiManagerError.getApiTypeError(upload.getApiType(), Arrays.toString(ApiType.values()));
            throw new NovaException(novaError, novaError.toString());
        }

        this.apiManagerValidator.checkUploadApiPermission(ApiType.valueOf(upload.getApiType()), productId);

        try
        {
            return this.apiManagerServiceModalityBased.uploadApi(upload, productId);
        }
        catch (DataIntegrityViolationException e)
        {
            LOG.error("[ApiManagerServiceImpl] -> [uploadProductApis]: Error saving API information in database", e);
            throw new NovaException(ApiManagerError.getDataBaseApiError(productId, (e.getRootCause() != null ? e.getRootCause().getMessage() : "")));
        }
    }

    @Override
    @Transactional
    public ApiErrorList deleteProductApi(final Integer productId, final Integer apiVersionId)
    {
        LOG.debug("[{}] -> [{}]: Deleting Sync version Api with Id [{}]", Constants.API_MANAGER_VALIDATOR_SERVICE_IMPL,
                "deleteNovaApi", apiVersionId);

        ApiVersion<?, ?, ?> apiVersion = this.apiManagerValidator.checkApiVersionExistence(productId, apiVersionId);
        Api<?, ?, ?> api = apiVersion.getApi();

        //Check permissions by api type
        this.apiManagerValidator.checkDeleteApiPermission(api.getType(), productId);

        ApiErrorList apiErrorList = this.apiManagerValidator.isApiVersionErasable(apiVersion);

        if (apiErrorList.getErrorList().length == 0)
        {
            if (1 == api.getApiVersions().size())
            {
                if (ApiType.EXTERNAL != api.getType())
                {
                    this.apiManagerServiceModalityBased.removeApiRegistration(api);
                }
                this.apiManagerServiceModalityBased.deleteApiTodoTasks(api);
                this.apiRepository.delete(api);
            }
            else
            {
                api.getApiVersions().remove(apiVersion);
            }

            switch (api.getType())
            {
                case NOT_GOVERNED:
                    // Emit Delete Ungoverned Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(productId, ActivityScope.UNGOVERNED_API, ActivityAction.ELIMINATED)
                            .entityId(apiVersion.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .build());
                    break;
                case GOVERNED:
                    // Emit Delete Governed Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(productId, ActivityScope.GOVERNED_API, ActivityAction.ELIMINATED)
                            .entityId(apiVersion.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .build());
                    break;
                case EXTERNAL:
                    // Emit Delete Third Party Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(productId, ActivityScope.THIRD_PARTY_API, ActivityAction.ELIMINATED)
                            .entityId(apiVersion.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .build());
                    break;
                default:
                    LOG.debug("Not supported Api Type: {}", api.getType().getApiType());
            }
        }

        return apiErrorList;
    }

    @Override
    public byte[] downloadProductApi(final Integer productId, final Integer apiVersionId, final String format, final String downloadType)
    {
        ApiVersion<?, ?, ?> apiVersion = this.apiManagerValidator.checkApiVersionExistence(productId, apiVersionId);
        return this.apiManagerServiceModalityBased.downloadProductApi(apiVersion, format, downloadType);
    }

    @Override
    public void createApiTask(final TaskInfoDto taskInfoDto)
    {
        // Check parameters from form (from NOVA Dashboard)
        this.apiManagerValidator.checkTaskDtoParameters(taskInfoDto);
        this.apiManagerValidator.checkCreatePolicyTaskPermission(taskInfoDto);

        Api<?, ?, ?> api = this.apiManagerValidator.checkApiExistence(taskInfoDto.getNovaApiId(), taskInfoDto.getProductId());

        this.apiManagerServiceModalityBased.createApiTask(taskInfoDto, api);
    }

    @Override
    public void onPolicyTaskReply(PolicyTaskReplyParametersDTO parametersDTO)
    {
        String taskStatus = parametersDTO.getTaskStatus();
        if (!EnumUtils.isValidEnum(ToDoTaskStatus.class, taskStatus))
        {
            final NovaError novaError = ApiManagerError.getApiTypeError(taskStatus, Arrays.toString(ToDoTaskStatus.values()));
            throw new NovaException(novaError, novaError.toString());
        }

        Integer productId = parametersDTO.getProductId();
        String apiName = parametersDTO.getApiName();
        String uuaa = parametersDTO.getUuaa();
        Api<?, ?, ?> api = this.apiRepository.findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
        if (api == null)
        {
            final NovaError novaError = ApiManagerError.getApiNotFoundError(productId, apiName, uuaa);
            throw new NovaException(novaError, novaError.toString());
        }

        ToDoTaskStatus toDoTaskStatus = ToDoTaskStatus.valueOf(taskStatus);
        if (ToDoTaskStatus.DONE == toDoTaskStatus)
        {
            api.setHasFrontConsumers(parametersDTO.getApiTaskHasFrontConsumers());
            api.setHasBackConsumers(parametersDTO.getApiTaskHasBackConsumers());
        }

        this.apiManagerServiceModalityBased.onPolicyTaskReply(ToDoTaskStatus.valueOf(taskStatus), api);
        apiRepository.save(api);
    }

    @Override
    @Transactional
    public void savePlanApiProfile(final ApiMethodProfileDto[] planProfileInfo, final Integer planId)
    {
        DeploymentPlan deploymentPlan = this.repositoryManagerService.findPlan(planId);

        if (deploymentPlan == null)
        {
            throw new NovaException(ApiManagerError.getSavePlanProfilingNotFoundError(planId));
        }

        PlanProfile planProfile = this.planProfileRepository.findByDeploymentPlan(deploymentPlan);

        if (planProfile == null)
        {
            throw new NovaException(ApiManagerError.getPlanProfileNotFoundError(planId));
        }

        Arrays.stream(planProfileInfo).forEach(methodProfile -> {
            Optional<ApiMethod> novaApiMethod = this.apiMethodRepository.findById(methodProfile.getMethodId());
            if (novaApiMethod.isEmpty())
            {
                throw new NovaException(ApiManagerError.getApiMethodNotFoundError(planId, methodProfile.getMethodId()));
            }
            ApiMethodProfile apiMethodProfile = this.apiMethodProfileRespository.findByPlanProfileAndApiMethod(planProfile, novaApiMethod.get());
            if (apiMethodProfile == null)
            {
                throw new NovaException(ApiManagerError.getApiMethodProfileNotFoundError(planId, methodProfile.getMethodId()));
            }
            apiMethodProfile.setRoles(Arrays.stream(methodProfile.getAssociatedRolesId()).mapToObj(roleId -> {
                Optional<CesRole> role = this.cesRoleRepository.findById(roleId);
                if (role.isEmpty())
                {
                    throw new NovaException(ApiManagerError.getCesRoleNotFoundError(planId, roleId));
                }
                return role.get();
            }).collect(Collectors.toSet()));
        });
    }

    @Override
    @Transactional
    public ApiPlanDetailDto getPlanApiDetailList(Integer planId)
    {
        DeploymentPlan deploymentPlan = this.repositoryManagerService.findPlan(planId);

        if (deploymentPlan == null)
        {
            throw new NovaException(ApiManagerError.getPlanApiDetailNotFoundError(planId));
        }

        if (!this.profilingUtils.isPlanExposingApis(deploymentPlan))
        {
            return new ApiPlanDetailDto();
        }

        if (CollectionUtils.isEmpty(deploymentPlan.getPlanProfiles()))
        {
            deploymentPlan.addPlanProfile(this.planProfilingUtils.createPlanProfile(deploymentPlan));
        }

        CesRole[] roles = this.profilingUtils.updateRoles(deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa(), deploymentPlan.getEnvironment());

        return this.dtoBuilder.buildApiPlanDetailDto(deploymentPlan, roles);
    }

    @Override
    public String[] getApiStatus()
    {
        return Arrays.stream(ApiState.values()).map(Enum::toString).sorted().toArray(String[]::new);
    }

    @Override
    public String[] getApiTypes()
    {
        return Arrays.stream(ApiType.values()).map(Enum::toString).sorted().toArray(String[]::new);
    }

    @Override
    public ApiDetailDto getApiDetail(final Integer apiId, final Integer productId)
    {
        Api<?, ?, ?> api = this.apiManagerValidator.checkApiExistence(apiId, productId);
        return this.dtoBuilder.buildApiDetailDto(api);
    }

    @Override
    public ApiVersionDetailDto getApiVersionDetail(final Integer apiVersionId, final Integer productId, final String filterByDeploymentStatus)
    {
        ApiVersion<?, ?, ?> apiVersion = this.apiManagerValidator.checkApiVersionExistence(productId, apiVersionId);
        return this.dtoBuilder.buildApiVersionDetailDto(apiVersion, filterByDeploymentStatus);
    }

    @Override
    @Transactional
    public void refreshDeployedApiVersionsState(Integer planId)
    {
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(planId).orElse(null);

        if (deploymentPlan == null)
        {
            LOG.warn("[refreshDeployedApiVersionsState]: Cannot modify ApiState. Plan with id {} not found", planId);
        }
        else
        {
            deploymentPlan.getReleaseVersion().getApiImplementations()
                    .flatMap(apiImplementation -> apiImplementation.getAllApiVersions().stream())
                    .filter(apiVersion -> !apiVersion.getApiState().equals(ApiState.DEPLOYED))
                    .forEach(apiVersion -> apiVersion.setApiState(ApiState.DEPLOYED));
        }
    }

    @Override
    @Transactional
    public void refreshUndeployedApiVersionsState(Integer planId)
    {
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(planId).orElse(null);

        if (deploymentPlan == null)
        {
            LOG.warn("[ApiManagerService] -> [updateUndeployedApis]: Cannot modify ApiState. Plan with id {} not found", planId);
        }
        else
        {
            deploymentPlan.getReleaseVersion().getApiImplementations()
                    .flatMap(apiImplementation -> apiImplementation.getAllApiVersions().stream())
                    .filter(apiVersion -> !this.isContainedApiVersionInAnyOtherDeployedDeploymentPlan(apiVersion, deploymentPlan))
                    .forEach(apiVersion -> apiVersion.setApiState(ApiState.IMPLEMENTED));
        }
    }

    @Override
    @Transactional
    public void refreshUnimplementedApiVersionsState(final ReleaseVersion releaseVersion)
    {
        this.apiVersionRepository.findAllNotImplemented(
                releaseVersion.getApiImplementations()
                        .flatMap(apiImplementation -> apiImplementation.getAllApiVersions().stream())
                        .map(AbstractEntity::getId).collect(Collectors.toList())
        ).forEach(unimplementedApiVersion -> unimplementedApiVersion.setApiState(ApiState.DEFINITION));
    }

    @Override
    public List<? extends Api<?, ?, ?>> getApisUsingMsaDocument(final Integer msaDocumentId)
    {
        return this.apiManagerServiceModalityBased.getApisUsingMsaDocument(msaDocumentId);
    }

    @Override
    public List<? extends Api<?, ?, ?>> getApisUsingAraDocument(final Integer araDocumentId)
    {
        return this.apiManagerServiceModalityBased.getApisUsingAraDocument(araDocumentId);
    }

    private boolean isContainedApiVersionInAnyOtherDeployedDeploymentPlan(ApiVersion<?, ?, ?> apiVersion, DeploymentPlan deploymentPlan)
    {
        return apiVersion.getApiImplementations().stream()
                .filter(apiImplementation -> apiImplementation.getService() instanceof ReleaseVersionService)
                .map(ApiImplementation::getService)
                .flatMap(rvs -> rvs.getVersionSubsystem().getReleaseVersion().getDeployments().stream())
                .anyMatch(dp -> !dp.equals(deploymentPlan) && DeploymentStatus.DEPLOYED == dp.getStatus());
    }

}

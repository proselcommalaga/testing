package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMApiDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesResponseDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicy;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicyId;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApi;
import com.bbva.enoa.datamodel.model.api.entities.SecurityPolicy;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiSecurityPolicyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SecurityPolicyRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@AllArgsConstructor
class SecurizableApiManagerCommonService
{
    private static final Logger LOG = LoggerFactory.getLogger(SecurizableApiManagerCommonService.class);
    public static final String API_NAME_PARAM = "apiName";
    public static final String BASE_PATH_PARAM = "basePathSwagger";
    public static final String TODO_STATUS_PARAM = "todoTaskStatus";
    private final IApiGatewayManagerClient apiGatewayManagerClient;
    private final SecurityPolicyRepository securityPolicyRepository;
    private final ApiSecurityPolicyRepository apiSecurityPolicyRepository;
    private final INovaActivityEmitter novaActivityEmitter;
    private final Utils utils;

    void addPoliciesToApi(final ISecurizableApi<?, ?, ?> api)
    {
        final AGMApiDetailDTO agmApiDetailDTO = new AGMApiDetailDTO();
        agmApiDetailDTO.setApiName(api.getName());
        agmApiDetailDTO.setUuaa(api.getUuaa());
        agmApiDetailDTO.setBasepath(api.getBasePathSwagger());

        final AGMPoliciesResponseDTO agmPoliciesResponseDTO = this.apiGatewayManagerClient.getPolicies(agmApiDetailDTO);

        Arrays.stream(agmPoliciesResponseDTO.getPolicies())
                .flatMap(agmPoliciesDTO -> this.utils.streamOfNullable(agmPoliciesDTO.getPolicies()).map(policy -> Pair.of(agmPoliciesDTO.getEnvironmnet(), policy)))
                .forEach(environmentPolicyTuple -> {
                    final SecurityPolicy securityPolicy = this.securityPolicyRepository.findByCode(environmentPolicyTuple.getRight());

                    if (securityPolicy != null)
                    {
                        final ApiSecurityPolicyId apiSecurityPolicyId = new ApiSecurityPolicyId(api.getId(), securityPolicy.getId(), environmentPolicyTuple.getLeft());

                        final ApiSecurityPolicy apiSecurityPolicy = new ApiSecurityPolicy();
                        apiSecurityPolicy.setId(apiSecurityPolicyId);
                        apiSecurityPolicy.setSecurityPolicy(securityPolicy);
                        apiSecurityPolicy.setApi(api.asApi());

                        this.apiSecurityPolicyRepository.save(apiSecurityPolicy);
                    }
                });
    }

    /**
     * Register activity when policies are resolved
     *
     * @param taskStatus     task status
     * @param api            the sync api
     * @param activityAction the activity action
     */
    void emitNewPoliciesActivityResponse(final ToDoTaskStatus taskStatus, final ISecurizableApi<?,?,?> api, final ActivityAction activityAction)
    {
        switch (api.getType().getApiType())
        {
            case Constants.NOT_GOVERNED_API_TYPE:
                // Emit Policies Request Ungoverned Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(api.getProduct().getId(), ActivityScope.UNGOVERNED_API, activityAction)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                        .addParam(TODO_STATUS_PARAM, taskStatus)
                        .build());
                break;
            case Constants.GOVERNED_API_TYPE:
                // Emit Policies Request Governed Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(api.getProduct().getId(), ActivityScope.GOVERNED_API, activityAction)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                        .addParam(TODO_STATUS_PARAM, taskStatus)
                        .build());
                break;
            case Constants.EXTERNAL_API_TYPE:
                // Emit Policies Request Third Party Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(api.getProduct().getId(), ActivityScope.THIRD_PARTY_API, activityAction)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                        .addParam(TODO_STATUS_PARAM, taskStatus)
                        .build());
                break;
            default:
                LOG.debug("Not supported Api Type: {}", api.getType().getApiType());
        }
    }

}

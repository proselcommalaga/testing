package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CesRoleRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProfilingUtils
{

    private final CesRoleRepository cesRoleRepository;

    private final IApiGatewayManagerClient apiGatewayManagerClient;

    @Value("${nova.gatewayServices.cesRolesEnabled:true}")
    private Boolean cesEnabled;

    @Autowired
    public ProfilingUtils(final CesRoleRepository cesRoleRepository, final IApiGatewayManagerClient apiGatewayManagerClient)
    {
        this.cesRoleRepository = cesRoleRepository;
        this.apiGatewayManagerClient = apiGatewayManagerClient;
    }

    /**
     * Update CesRoles for an uuaa in a environment
     *
     * @param uuaa        uuaa
     * @param environment environment
     * @return updated CES roles
     */
    public CesRole[] updateRoles(final String uuaa, final String environment)
    {
        if (!cesEnabled)
        {
            return new CesRole[0];
        }
        Set<CesRole> originRoles = this.cesRoleRepository.findAllByUuaaAndEnvironment(uuaa, environment);
        Set<CesRole> newRoles = Arrays.stream(this.apiGatewayManagerClient.getRoles(uuaa, environment))
                .map(rol -> new CesRole(uuaa, environment, rol, new HashSet<>()))
                .collect(Collectors.toSet());

        //Deprecated roles are removed from BBDD and existing ones are removed from the set
        Iterator<CesRole> iterator = originRoles.iterator();
        iterator.forEachRemaining(
                cesRole -> {
                    //The rol is removed if it does not exist anymore
                    if (!newRoles.remove(cesRole))
                    {
                        cesRole.cleanApiMethodProfiles();
                        cesRoleRepository.delete(cesRole);
                        iterator.remove();
                    }
                }
        );

        //All remaining roles must be added
        if (newRoles.size() > 0)
        {
            originRoles.addAll(cesRoleRepository.saveAll(newRoles));
        }

        return originRoles.toArray(CesRole[]::new);
    }

    /**
     * Checks whether the plan has api rest services exposing apis to create api profile
     *
     * @param deploymentPlan the plan to check
     * @return whether the plan has apis exposed
     */
    public boolean isPlanExposingApis(final DeploymentPlan deploymentPlan)
    {
        boolean isPlanExposingApis = deploymentPlan.getDeploymentSubsystems().stream()
                .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                .anyMatch(deploymentService -> deploymentService.getService().getServers().size() > 0);
        log.info("[ProfilingUtils] -> [isPlanExposingApis]: the deployment plan id: [{}] - isExposingAPis: [{}]", deploymentPlan.getId(), isPlanExposingApis);

        return isPlanExposingApis;
    }

    /**
     * Checks whether the plan has at least one service with mgw
     *
     * @param deploymentPlan plan to check
     * @return whether the plan has one service with mgw
     */
    public boolean isPlanContainingServicesWithMgw(final DeploymentPlan deploymentPlan)
    {
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isMicrogateway())
                {
                    return true;
                }
            }
        }
        return false;
    }
}

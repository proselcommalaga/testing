package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlanProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PlanProfilingUtils
{

    private final static Logger LOG = LoggerFactory.getLogger(PlanProfilingUtils.class);

    private final DeploymentPlanRepository deploymentPlanRepository;

    private final IApiGatewayService apiGatewayService;

    private final PlanProfileRepository planProfileRepository;

    /*
     * KKPF is NOVA product for CES. We cannot publish its own profiling because profiling publication is done in
     * the same service that is being deployed. It would always fail.
     */
    private final String cesUuaa;

    /**
     * Inner class to compare endpoints profiling
     */
    private static class EndpointProfiling
    {
        private final Verb verb;
        private final String endpoint;
        private final Set<CesRole> roles;

        public EndpointProfiling(final Verb verb, final String endpoint, final Set<CesRole> roles)
        {
            this.verb = verb;
            this.endpoint = endpoint;
            this.roles = roles;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof EndpointProfiling))
            {
                return false;
            }
            EndpointProfiling that = (EndpointProfiling) o;
            return verb == that.verb &&
                    endpoint.equals(that.endpoint) &&
                    roles.size() == that.roles.size() &&
                    roles.containsAll(that.roles);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(verb, endpoint, roles);
        }
    }

    @Autowired
    public PlanProfilingUtils(
            final DeploymentPlanRepository deploymentPlanRepository,
            final IApiGatewayService apiGatewayService,
            final PlanProfileRepository planProfileRepository,
            @Value("${nova.gatewayServices.cesUuaa:KKPF}") String cesUuaa)
    {
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.apiGatewayService = apiGatewayService;
        this.planProfileRepository = planProfileRepository;
        this.cesUuaa = cesUuaa;
    }

    /**
     * Check if the previous profile active plan has the same profiling that the plan willing to deploy
     *
     * @param oldPlan old deployment plan (with active profile)
     * @param newPlan new deployment plan
     * @return whether the profiling is the same
     */
    public boolean isSameProfiling(final DeploymentPlan oldPlan, final DeploymentPlan newPlan)
    {
        LOG.debug("[PlanProfilingUtils] -> [checkPlanProfileChange]: checking same profiling between old plan [{}] and new plan [{}]", oldPlan.getId(), newPlan.getId());
        Set<EndpointProfiling> oldEndpoints = oldPlan.getPlanProfiles().stream()
                .flatMap(planProfile -> planProfile.getApiMethodProfiles().stream())
                .map(apiMethodProfile -> new EndpointProfiling(
                                apiMethodProfile.getApiMethod().getVerb(),
                                apiMethodProfile.getApiMethod().getSecurizableApiVersion().getSecurizableApi().getBasePathSwagger() +
                                        apiMethodProfile.getApiMethod().getEndpoint(),
                                apiMethodProfile.getRoles()
                        )
                )
                .collect(Collectors.toSet());

        Set<EndpointProfiling> newEndpoints = newPlan.getPlanProfiles().stream()
                .flatMap(planProfile -> planProfile.getApiMethodProfiles().stream())
                .map(apiMethodProfile -> new EndpointProfiling(
                                apiMethodProfile.getApiMethod().getVerb(),
                                apiMethodProfile.getApiMethod().getSecurizableApiVersion().getSecurizableApi().getBasePathSwagger() +
                                        apiMethodProfile.getApiMethod().getEndpoint(),
                                apiMethodProfile.getRoles()
                        )
                )
                .collect(Collectors.toSet());

        LOG.debug("[PlanProfilingUtils] -> [checkPlanProfileChange]: checked same profiling between old plan [{}] and new plan [{}]", oldPlan, newPlan);

        boolean isTheSameNumberEndpoints = oldEndpoints.size() == newEndpoints.size();
        boolean containsAllEndpoints = oldEndpoints.containsAll(newEndpoints);
        LOG.info("[PlanProfilingUtils] -> [checkPlanProfileChange]: checked old deployment plan id: [{}] vs new deployment plan id: [{}]. Is the same number endpoints: [{}] -- both plan contains all endpoints: [{}]",
                oldPlan, newPlan, isTheSameNumberEndpoints, containsAllEndpoints);

        return isTheSameNumberEndpoints && containsAllEndpoints;
    }

    /**
     * Recovers the last deployment plan of the environment whose profile is active
     *
     * @param env         environment
     * @param releaseName name of the release to look for
     * @return the plan or null if there is not one
     */
    public DeploymentPlan getLastDeploymentPlanWithActiveProfiling(final String env, final String releaseName)
    {
        List<DeploymentPlan> deploymentPlanList = deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, env, ProfileStatus.ACTIVE);
        if (deploymentPlanList.size() > 0)
        {
            LOG.info("[PlanProfilingUtils] -> [getLastDeploymentPlanWithActiveProfiling]: a old deployment plan with id: [{}] in environment: [{}] for release name: [{}] has been found with profiling ACTIVE",
                    deploymentPlanList.get(0).getId(), env, releaseName);
            return deploymentPlanList.get(0);
        }

        LOG.info("[PlanProfilingUtils] -> [getLastDeploymentPlanWithActiveProfiling]: any deployment plan with profiling ACTIVE has been found in environment: [{}] with release name: [{}]. Return null", env, releaseName);
        return null;
    }

    /**
     * Creates a Plan Profile for given plan
     *
     * @param deploymentPlan plan
     * @return plan profile entity
     */
    public PlanProfile createPlanProfile(final DeploymentPlan deploymentPlan)
    {
        PlanProfile planProfile = new PlanProfile()
                .setDeploymentPlan(deploymentPlan)
                .setStatus(ProfileStatus.PENDING);

        deploymentPlan.getDeploymentSubsystems().stream()
                .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                .flatMap(deploymentService -> deploymentService.getService().getSecurizableServers().stream())
                .flatMap(novaApiImplementation -> novaApiImplementation.getSecurizableApiVersion().getApiMethods().stream())
                .map(novaApiMethod -> new ApiMethodProfile().setApiMethod(novaApiMethod))
                .forEach(planProfile::addApiMethodProfile);

        return planProfile;
    }

    /**
     * Creates a Copy of Plan Profile for given plan
     *
     * @param originalPlan original plan
     * @param copiedPlan   copied plan
     * @return plan profile entity list
     */
    public List<PlanProfile> copyPlanProfile(final DeploymentPlan originalPlan, final DeploymentPlan copiedPlan)
    {

        PlanProfile oldPlanProfile = originalPlan.getPlanProfiles().get(0);
        PlanProfile newPlanProfile = new PlanProfile()
                .setDeploymentPlan(copiedPlan)
                .setStatus(ProfileStatus.PENDING);

        if (copiedPlan.getReleaseVersion().getId().equals(originalPlan.getReleaseVersion().getId()))
        {
            newPlanProfile.setApiMethodProfiles(
                    oldPlanProfile.getApiMethodProfiles().stream()
                            .map(oldApiMethodProfile -> new ApiMethodProfile()
                                    .setPlanProfile(newPlanProfile)
                                    .setApiMethod(oldApiMethodProfile.getApiMethod())
                                    .setRoles(new HashSet<>(oldApiMethodProfile.getRoles()))
                            ).collect(Collectors.toList())
            );
        }
        else
        {
            Set<ISecurizableApiVersion<?, ?, ?>> copiedPlanApiVersions = copiedPlan.getDeploymentSubsystems().stream()
                    .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                    .flatMap(deploymentService -> deploymentService.getService().getSecurizableServers().stream())
                    .map(ISecurizableApiImplementation::getSecurizableApiVersion)
                    .collect(Collectors.toSet());
            Set<ISecurizableApiVersion<?, ?, ?>> originalPlanApiVersions = originalPlan.getDeploymentSubsystems().stream()
                    .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                    .flatMap(deploymentService -> deploymentService.getService().getSecurizableServers().stream())
                    .map(ISecurizableApiImplementation::getSecurizableApiVersion)
                    .collect(Collectors.toSet());

            newPlanProfile.setApiMethodProfiles(
                    oldPlanProfile.getApiMethodProfiles().stream()
                            .filter(oldApiMethodProfile -> copiedPlanApiVersions.contains(oldApiMethodProfile.getApiMethod().getSecurizableApiVersion()))
                            .map(oldApiMethodProfile -> new ApiMethodProfile()
                                    .setPlanProfile(newPlanProfile)
                                    .setApiMethod(oldApiMethodProfile.getApiMethod())
                                    .setRoles(new HashSet<>(oldApiMethodProfile.getRoles()))
                            ).collect(Collectors.toList())
            );
            newPlanProfile.getApiMethodProfiles().addAll(
                    copiedPlanApiVersions.stream()
                            .filter(copiedApiVersion -> !originalPlanApiVersions.contains(copiedApiVersion))
                            .flatMap(copiedApiVersion -> copiedApiVersion.getApiMethods().stream())
                            .map(apiMethod -> new ApiMethodProfile()
                                    .setPlanProfile(newPlanProfile)
                                    .setApiMethod(apiMethod)
                            ).collect(Collectors.toList())
            );
        }
        return Collections.singletonList(newPlanProfile);
    }

    public void checkPlanProfileChange(final DeploymentPlan plan)
    {
        LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: checking plan profile for plan: [{}]", plan.getId());
        DeploymentPlan oldPlan = this.getLastDeploymentPlanWithActiveProfiling(plan.getEnvironment(),
                plan.getReleaseVersion().getRelease().getName());

        if (oldPlan != null)
        {
            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: recovered old plan: [{}]", oldPlan.getId());
        }
        else
        {
            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: no old plan recovered");
        }

        if (oldPlan == null)
        {
            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: create profiling without old plan");

            this.apiGatewayService.createProfiling(plan);

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            // non-API-exposing plans doesn't have PlanProfile
            Optional.ofNullable(this.planProfileRepository.findByDeploymentPlan(plan)).
                    ifPresent(p -> p.setStatus(ProfileStatus.ACTIVE).setActivationDate(calendar.getTime()));
        }
        else if (!this.isSameProfiling(oldPlan, plan))
        {
            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: old plan [{}] and new plan [{}] have different profiling", oldPlan.getId(), plan.getId());

            this.apiGatewayService.removeProfiling(oldPlan);

            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: old plan [{}] profile removed from CES", oldPlan.getId());

            this.planProfileRepository.findByDeploymentPlan(oldPlan).setStatus(ProfileStatus.DEPRECATED);

            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: removed profiling (deprecated) for old plan [{}]", oldPlan.getId());

            this.apiGatewayService.createProfiling(plan);

            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: new plan [{}] profile created in CES", plan.getId());

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            // non-API-exposing plans doesn't have PlanProfile
            Optional.ofNullable(this.planProfileRepository.findByDeploymentPlan(plan))
                    .ifPresent(p -> p.setStatus(ProfileStatus.ACTIVE).setActivationDate(calendar.getTime()));

            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: created profiling for new plan [{}]", plan.getId());
        }
        else
        {
            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: old plan [{}] and new plan [{}] have the same profiling", oldPlan.getId(), plan.getId());

            PlanProfile oldPlanProfile = this.planProfileRepository.findByDeploymentPlan(oldPlan).setStatus(ProfileStatus.DEPRECATED);

            // non-API-exposing plans doesn't have PlanProfile
            Optional.ofNullable(this.planProfileRepository.findByDeploymentPlan(plan))
                    .ifPresent(p -> p.setStatus(ProfileStatus.ACTIVE).setActivationDate(oldPlanProfile.getActivationDate()));

            LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: old plan [{}] profiling copied to new plan [{}]", oldPlan.getId(), plan.getId());
        }
    }

    @Transactional
    public boolean checkProProfileNeedsApproval(final DeploymentPlan deploymentPlan)
    {
        //KKPF cannot use NOVA portal profiling so it doesnt need approval
        if (deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa().equalsIgnoreCase(this.cesUuaa)
                || this.isProfilingConfigurationEmpty(deploymentPlan))
        {
            LOG.warn("[PlanProfiling] -> [checkPlanProfileChange]: this deployment plan id: [{}] belongs to cesUUAA status: [{}]. This UUAA does not need to be profiling itself", deploymentPlan.getId(), this.cesUuaa);
            return false;
        }

        DeploymentPlan oldPlan = this.getLastDeploymentPlanWithActiveProfiling(deploymentPlan.getEnvironment(), deploymentPlan.getReleaseVersion().getRelease().getName());
        return oldPlan == null || !this.isSameProfiling(oldPlan, deploymentPlan);
    }

    /**
     * Check if the Plan Profile entity exists previously in data base (Entity: PlanProfile)
     * This entity is created when the user access to TAB: DeploymentPlan  - API section with the intention of profiling all APIs of this deployment plan.
     *
     * @param deploymentPlan the deployment plan
     * @return return true when:
     * - if plan profile entity is empty or does not exists (this means the user did not access to API section: DeploymentPlan: Configuration: API TAB)
     * - if plan profile exist but any api resource has been assigned to some role.
     * Return false when:
     * - At least one api resource has been profiled (that means, some role has been assigned)
     */
    private boolean isProfilingConfigurationEmpty(DeploymentPlan deploymentPlan)
    {
        if (deploymentPlan.getPlanProfiles().isEmpty())
        {
            LOG.info("[PlanProfiling] -> [checkPlanProfileChange]: the deployment plan id: [{}] has never been profiled. Return true", deploymentPlan.getId());
        }
        else
        {
            for (ApiMethodProfile apiMethodProfile : deploymentPlan.getPlanProfiles().get(0).getApiMethodProfiles())
            {
                if (apiMethodProfile.getRoles().isEmpty())
                {
                    LOG.debug("[PlanProfiling] -> [checkPlanProfileChange]: for plan profiling id: [{}] - api resource:endpoint name: [{}] from deployment plan id: [{}] does NOT have a ROL associated. ",
                            deploymentPlan.getPlanProfiles().get(0).getId(), apiMethodProfile.getApiMethod().getEndpoint(), deploymentPlan.getId());
                }
                else
                {
                    LOG.info("[PlanProfiling] -> [checkPlanProfileChange]: for plan profiling id: [{}] - api resource:endpoint name: [{}] from deployment plan id: [{}] have some ROLs associated: [{}]. " +
                                    "This deployment plan should create a to do task to Profiling office. Return false",
                            deploymentPlan.getPlanProfiles().get(0).getId(), apiMethodProfile.getApiMethod().getEndpoint(), deploymentPlan.getId(), apiMethodProfile.getRoles());
                    return false;
                }
            }

            LOG.warn("[PlanProfiling] -> [checkPlanProfileChange]: for plan profiling id: [{}] - from deployment plan id: [{}] does not have ANY ROL associated to any api resource method.  " +
                            "To apply the profiling, at least one endpoints/resources of the API, must have one ROL associated. Return true",
                    deploymentPlan.getPlanProfiles().get(0).getId(), deploymentPlan.getId());
        }

        return true;
    }

}

package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterTypeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkTypedParametersDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValueId;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkParameterProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceAllowedJdkParameterValueRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentServiceJdkParametersSetter;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class DeploymentServiceJdkParametersSetter implements IDeploymentServiceJdkParametersSetter
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeploymentServiceJdkParametersSetter.class);
    /**
     * Allowed jvm option by jdk repository
     */
    private final AllowedJdkParameterProductRepository allowedJdkParameterProductRepository;
    /**
     * Deployment service jvm option repository
     */
    private final DeploymentServiceAllowedJdkParameterValueRepository deploymentServiceAllowedJdkParameterValueRepository;
    /**
     * JVM jdk checker
     */
    private final JvmJdkConfigurationChecker jvmJdkChecker;
    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    @Autowired
    public DeploymentServiceJdkParametersSetter(final AllowedJdkParameterProductRepository allowedJdkParameterProductRepository, final DeploymentServiceAllowedJdkParameterValueRepository deploymentServiceAllowedJdkParameterValueRepository,
                                                final JvmJdkConfigurationChecker jvmJdkChecker, final INovaActivityEmitter novaActivityEmitter)
    {
        this.allowedJdkParameterProductRepository = allowedJdkParameterProductRepository;
        this.deploymentServiceAllowedJdkParameterValueRepository = deploymentServiceAllowedJdkParameterValueRepository;
        this.jvmJdkChecker = jvmJdkChecker;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    ////////////////////////////////////// IMPLEMENTATIONS ///////////////////////////

    @Override
    public void setJvmOptionsForDeploymentService(final DeploymentService deploymentService, final DeploymentServiceDto deploymentServiceDto, final JdkTypedParametersDto typedParameters)
    {
        final Environment environment = Environment.valueOf(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        boolean isJvmOptionsUpdated = false;

        // 1. Set or updated JVM Options
        if (typedParameters == null || typedParameters.getTypedParameters() == null || (Environment.PRO == environment && !Objects.equals(deploymentServiceDto.getIsJmxUpdated(),Boolean.TRUE)))
        {
            LOG.debug("[DeploymentServiceJdkParametersSetter] -> [setJvmOptionsForDeploymentService]: some of this parameters typedParameters or environment is null. Continue without saving jdk parameters");
        }
        else
        {
            final boolean isMultiJdk = this.jvmJdkChecker.isMultiJdk(deploymentService.getService());
            final Integer deploymentServiceId = deploymentService.getId();

            if (isMultiJdk)
            {
                // Delete old jvm option configurations
                this.deploymentServiceAllowedJdkParameterValueRepository.deleteByDeploymentServiceIds(Set.of(deploymentServiceId));

                // Save new jvm options for the deployment service id
                for (JdkParameterTypeDto jdkParameterTypeDto : typedParameters.getTypedParameters())
                {
                    JdkParameterDto[] parameters = jdkParameterTypeDto.getParameters();
                    if (parameters == null)
                    {
                        continue;
                    }
                    for (JdkParameterDto jdkParameterDto : parameters)
                    {
                        if (Boolean.TRUE.equals(jdkParameterDto.getIsSelected()))
                        {
                            AllowedJdkParameterProduct allowedJdkParameterProduct = this.allowedJdkParameterProductRepository.findById(jdkParameterDto.getJdkVersionParameterId()).orElse(null);

                            if (allowedJdkParameterProduct == null)
                            {
                                LOG.warn("[DeploymentServiceJdkParametersSetter] -> [setJvmOptionsForDeploymentService]: the jdk parameter id: [{}] does not exist into data base", jdkParameterDto.getJdkVersionParameterId());
                            }
                            else
                            {
                                // Save the jvm option into database for this deployment service
                                DeploymentServiceAllowedJdkParameterValueId deploymentServiceAllowedJdkParameterValueId = new DeploymentServiceAllowedJdkParameterValueId();
                                deploymentServiceAllowedJdkParameterValueId.setAllowedJdkParameterProductId(allowedJdkParameterProduct.getId());
                                deploymentServiceAllowedJdkParameterValueId.setDeploymentServiceId(deploymentServiceId);


                                DeploymentServiceAllowedJdkParameterValue deploymentServiceAllowedJdkParameterValue = new DeploymentServiceAllowedJdkParameterValue();
                                deploymentServiceAllowedJdkParameterValue.setId(deploymentServiceAllowedJdkParameterValueId);
                                deploymentServiceAllowedJdkParameterValue.setDeploymentService(deploymentService);
                                deploymentServiceAllowedJdkParameterValue.setAllowedJdkParameterProduct(allowedJdkParameterProduct);

                                this.deploymentServiceAllowedJdkParameterValueRepository.saveAndFlush(deploymentServiceAllowedJdkParameterValue);
                                isJvmOptionsUpdated = true;
                            }
                        }
                    }
                }
            }
            else
            {
                LOG.debug("[DeploymentServiceJdkParametersSetter] -> [setJvmOptionsForDeploymentService]: deployment service with id: [{}] has no configuration for select JDK parameters", deploymentServiceId);
            }
        }

        // 2. Set or update the memory Factor of JVM - Container. Emit JVM changes if the memory factor from DTO is not equals than deployment service memory factor original or the jvmOption has been updated
        if (deploymentService.getMemoryFactor() != deploymentServiceDto.getMemoryFactor() || isJvmOptionsUpdated)
        {
            // Emit JVM configuration Deployment Service Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.JVM_CONFIGURED)
                    .entityId(deploymentService.getId())
                    .environment(environment.name())
                    .addParam("serviceName", deploymentService.getService().getServiceName())
                    .addParam("serviceType", deploymentService.getService().getServiceType())
                    .addParam("memoryFactor", deploymentServiceDto.getMemoryFactor())
                    .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                    .addParam("releaseVersionName", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName())
                    .build());
        }

        // Set memory factor
        deploymentService.setMemoryFactor(deploymentServiceDto.getMemoryFactor());
    }
}
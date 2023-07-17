package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Deployment status service
 *
 * @author XE56809
 */
@Slf4j
@Service
public class DeploymentStatusServiceImpl implements IDeploymentStatusService
{
    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository planRepository;

    /**
     * Deployment subsystem repository
     */
    private final DeploymentSubsystemRepository subsystemRepository;

    /**
     * Deployment service repository
     */
    private final DeploymentServiceRepository serviceRepository;

    /**
     * Deployment instance repository
     */
    private final DeploymentInstanceRepository instanceRepository;

    /**
     * Constructor by params
     * @param planRepository deployment plan repository
     * @param subsystemRepository subsystem repository
     * @param serviceRepository service repository
     * @param instanceRepository instance repository
     */
    @Autowired
    public DeploymentStatusServiceImpl(final DeploymentPlanRepository planRepository,
                                       final DeploymentSubsystemRepository subsystemRepository,
                                       final DeploymentServiceRepository serviceRepository,
                                       final DeploymentInstanceRepository instanceRepository)
    {
        this.planRepository = planRepository;
        this.subsystemRepository = subsystemRepository;
        this.serviceRepository = serviceRepository;
        this.instanceRepository = instanceRepository;
    }

    @Override
    @Transactional
    public ActionStatus getDeploymentPlanStatus(Integer deploymentId)
    {
        ActionStatus actionStatus = new ActionStatus();
        Optional<DeploymentPlan> optional = this.planRepository.findById(deploymentId);
        if (optional.isPresent())
        {
            DeploymentPlan plan = optional.get();
            actionStatus.setStatus(plan.getStatus().getDeploymentStatus());
            actionStatus.setAction(plan.getAction().getAction());
        }
        return actionStatus;
    }

    @Override
    @Transactional
    public ActionStatus getDeploymentSubsystemStatus(Integer subsystemId)
    {
        ActionStatus actionStatus = new ActionStatus();
        Optional<DeploymentSubsystem> optional = this.subsystemRepository.findById(subsystemId);
        if (optional.isPresent())
        {
            DeploymentSubsystem subsystem = optional.get();
            actionStatus.setStatus(subsystem.getDeploymentPlan().getStatus().getDeploymentStatus());
            actionStatus.setAction(subsystem.getAction().getAction());
        }
        return actionStatus;
    }

    @Override
    @Transactional
    public ActionStatus getDeploymentServiceStatus(Integer serviceId)
    {
        ActionStatus actionStatus = new ActionStatus();
        Optional<DeploymentService> optional = this.serviceRepository.findById(serviceId);
        if (optional.isPresent())
        {
            DeploymentService service = optional.get();
            actionStatus.setStatus(service.getDeploymentSubsystem().getDeploymentPlan().getStatus().getDeploymentStatus());
            actionStatus.setAction(service.getAction().getAction());
        }
        return actionStatus;
    }

    @Override
    @Transactional
    public ActionStatus getDeploymentInstanceStatus(Integer instanceId)
    {
        ActionStatus actionStatus = new ActionStatus();
        Optional<DeploymentInstance> optional = this.instanceRepository.findById(instanceId);
        if (optional.isPresent())
        {
            DeploymentInstance instance = optional.get();
            actionStatus.setStatus(instance.getService().getDeploymentSubsystem().getDeploymentPlan().getStatus().getDeploymentStatus());
            actionStatus.setAction(instance.getAction().getAction());
        }
        return actionStatus;
    }

}

package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRepositoryManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing repositories
 *
 * @author XE56809
 */
@Service
public class RepositoryManagerServiceImpl implements IRepositoryManagerService
{
    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository planRepository;

    /**
     * Deployment service repository
     */
    private final DeploymentServiceRepository serviceRepository;

    /**
     * Deployment subsystem repository
     */
    private final DeploymentSubsystemRepository subsystemRepository;

    /**
     * Deployment instance repository
     */
    private final DeploymentInstanceRepository deploymentInstanceRepository;

    /**
     * Release version repository
     */
    private final ReleaseVersionRepository releaseVersionRepository;

    /**
     * Deployment plan change repository
     */
    private final DeploymentChangeRepository deploymentChangeRepository;

    /**
     * Constructor
     *
     * @param planRepository                plan repository
     * @param deploymentServiceRepository   deployment service repository
     * @param deploymentSubsystemRepository deployment subsystem repository
     * @param releaseVersionRepository      release version repository
     * @param deploymentInstanceRepository  deployment Instance Repository
     * @param deploymentChangeRepository    deployment plan change repository
     */
    @Autowired
    public RepositoryManagerServiceImpl(final DeploymentPlanRepository planRepository,
                                        final DeploymentServiceRepository deploymentServiceRepository,
                                        final DeploymentSubsystemRepository deploymentSubsystemRepository,
                                        final ReleaseVersionRepository releaseVersionRepository,
                                        final DeploymentInstanceRepository deploymentInstanceRepository,
                                        final DeploymentChangeRepository deploymentChangeRepository)
    {

        this.planRepository = planRepository;
        this.serviceRepository = deploymentServiceRepository;
        this.subsystemRepository = deploymentSubsystemRepository;
        this.releaseVersionRepository = releaseVersionRepository;
        this.deploymentInstanceRepository = deploymentInstanceRepository;
        this.deploymentChangeRepository = deploymentChangeRepository;
    }

    ///////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////

    @Override
    @Transactional
    public void savePlan(DeploymentPlan plan)
    {
        this.planRepository.saveAndFlush(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentPlan findPlan(final Integer id)
    {
        return this.planRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeploymentPlan> findByStatusAndEnvironmentAndUndeploymentDateNotNull(DeploymentStatus deploymentStatus, Environment environment)
    {
        return this.planRepository.findByStatusAndEnvironmentAndUndeploymentDateNotNull(deploymentStatus, environment.getEnvironment());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseVersion> findReleaseVersionByStatus(ReleaseVersionStatus releaseVersionStatus)
    {
        return this.releaseVersionRepository.findByStatus(ReleaseVersionStatus.STORAGED);
    }

    @Override
    @Transactional
    public void saveService(DeploymentService service)
    {
        this.serviceRepository.save(service);
    }

    @Override
    public void flushServiceRepository()
    {
        this.serviceRepository.flush();
    }

    @Override
    @Transactional
    public void saveSubsystem(DeploymentSubsystem subsystem)
    {
        this.subsystemRepository.save(subsystem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeploymentInstance> findBatchDeploymentInstance(final Calendar calendar, final List<ServiceType> serviceTypeList)
    {
        List<String> serviceTypeNameList = serviceTypeList.stream().map(ServiceType::getServiceType).collect(Collectors.toList());
        return this.deploymentInstanceRepository.findByCreationDateBeforeAndService_Service_ServiceTypeIn(calendar, serviceTypeNameList);
    }

    @Override
    @Transactional
    public void deleteDeploymentInstance(final DeploymentInstance deploymentInstance)
    {
        this.deploymentInstanceRepository.delete(deploymentInstance);
    }
}

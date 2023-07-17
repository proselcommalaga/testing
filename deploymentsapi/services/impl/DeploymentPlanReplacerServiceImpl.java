package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplaceServiceConflict;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplaceSubsystemConflict;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanReplacerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants.ReplaceActionErrors;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for all utilities when replacing a deployment plan
 *
 * @author XE56809
 */
@Slf4j
@Service
public class DeploymentPlanReplacerServiceImpl implements IDeploymentPlanReplacerService
{
    /** Tools client */
    private final IToolsClient toolsClient;

    /**
     * Constructor by params
     * @param toolsClient client
     */
    @Autowired
    public DeploymentPlanReplacerServiceImpl(final IToolsClient toolsClient)
    {
        this.toolsClient = toolsClient;
    }

    /////////////////////////////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////////

    @Override
    public void buildReplacePlanNovaException(DeploymentPlan oldPlan, DeploymentPlan newPlan) throws NovaException
    {
        //Create response list
        List<ReplaceSubsystemConflict> replaceSubsystemConflictList = new ArrayList<>();

        // Check old subsystems
        this.checkOldSubsystems(oldPlan, newPlan, replaceSubsystemConflictList);

        // Check new subsystems
        this.checkNewSubsystems(oldPlan, newPlan, replaceSubsystemConflictList);

        String exceptionMessage = "[DeploymentPlanReplacerService] -> [buildReplacePlanNovaException]: error trying to replace a deployment plan over a new deployment plan. "
                + "List of subsystems/services with the action status for each one: " + Arrays.toString(replaceSubsystemConflictList.toArray());
        throw new NovaException(DeploymentError.getForceReplacePlan(newPlan.getId(), oldPlan.getId()), exceptionMessage);
    }

    /////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////

    /**
     * Check new subsystems
     *
     * @param oldPlan            old plan
     * @param newPlan            new plan
     * @param replaceSubsystemConflictList conflicts
     */
    private void checkNewSubsystems(DeploymentPlan oldPlan, DeploymentPlan newPlan, List<ReplaceSubsystemConflict> replaceSubsystemConflictList)
    {
        for (DeploymentSubsystem newSubsystem : newPlan.getDeploymentSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(newSubsystem.getSubsystem().getSubsystemId());
            //Find new subsystem in old subsystem list
            DeploymentSubsystem oldSubsystem = this.findSubsystem(subsystemDTO.getSubsystemName(), oldPlan.getDeploymentSubsystems());

            //Check create subsystem
            this.checkSubsystemForCreation(replaceSubsystemConflictList, newSubsystem, oldSubsystem);
        }
    }

    /**
     * Check old subsystems
     *
     * @param oldPlan            old plan
     * @param newPlan            new plan
     * @param replaceSubsystemConflictList subsystem conflict list
     */
    private void checkOldSubsystems(DeploymentPlan oldPlan, DeploymentPlan newPlan, List<ReplaceSubsystemConflict> replaceSubsystemConflictList)
    {
        for (DeploymentSubsystem oldSubsystem : oldPlan.getDeploymentSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(oldSubsystem.getSubsystem().getSubsystemId());

            ReplaceSubsystemConflict replaceSubsystemConflict = new ReplaceSubsystemConflict();
            replaceSubsystemConflict.setSubsystemName(subsystemDTO.getSubsystemName());
            replaceSubsystemConflict.setTagName(oldSubsystem.getSubsystem().getTagName());

            //Find old subsystem in new subsystem list
            DeploymentSubsystem newSubsystem = this.findSubsystem(subsystemDTO.getSubsystemName(), newPlan.getDeploymentSubsystems());

            //Check subsystem
            this.checkSubsystem(oldSubsystem, replaceSubsystemConflict, newSubsystem);
            replaceSubsystemConflictList.add(replaceSubsystemConflict);
        }
    }

    /**
     * Check subsystem for creation
     *
     * @param subsystemConflicts subsystem conflicts
     * @param newSubsystem       new subsystem
     * @param oldSubsystem       old subsystem
     */
    private void checkSubsystemForCreation(List<ReplaceSubsystemConflict> subsystemConflicts, DeploymentSubsystem newSubsystem, DeploymentSubsystem oldSubsystem)
    {
        if (oldSubsystem == null)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(newSubsystem.getSubsystem().getSubsystemId());

            ReplaceSubsystemConflict subsystemConflict = new ReplaceSubsystemConflict();
            subsystemConflict.setSubsystemName(subsystemDTO.getSubsystemName());
            subsystemConflict.setTagName(newSubsystem.getSubsystem().getTagName());
            subsystemConflict.setAction(ReplaceActionErrors.ACTION_CREATE);
            subsystemConflict.setReplaceServiceConflictList(new ArrayList<>());
            subsystemConflicts.add(subsystemConflict);
        }
    }

    /**
     * Check subsystem
     *
     * @param oldSubsystem      old subsystem
     * @param replaceSubsystemConflict replace subsystem conflict
     * @param newSubsystem      new subsystem
     */
    private void checkSubsystem(DeploymentSubsystem oldSubsystem, ReplaceSubsystemConflict replaceSubsystemConflict, DeploymentSubsystem newSubsystem)
    {
        if (newSubsystem == null)
        {
            replaceSubsystemConflict.setAction(ReplaceActionErrors.ACTION_REMOVE);
        }
        else
        {
            this.checkSubsystemForConflicts(oldSubsystem, replaceSubsystemConflict, newSubsystem);
        }
    }

    /**
     * Check subsystem for conflicts
     *
     * @param oldSubsystem      old subsystem
     * @param replaceSubsystemConflict replace subsystem conflicts
     * @param newSubsystem      new subsystem
     */
    private void checkSubsystemForConflicts(DeploymentSubsystem oldSubsystem, ReplaceSubsystemConflict replaceSubsystemConflict, DeploymentSubsystem newSubsystem)
    {
        if (oldSubsystem.getSubsystem().getTagName().equals(newSubsystem.getSubsystem().getTagName()))
        {
            replaceSubsystemConflict.setAction(ReplaceActionErrors.ACTION_KEEP);
        }
        else
        {
            replaceSubsystemConflict.setAction(ReplaceActionErrors.ACTION_UPDATE_TAG + newSubsystem.getSubsystem().getTagName());
        }

        this.addServiceConflicts(replaceSubsystemConflict, oldSubsystem, newSubsystem);
    }

    /**
     * Find subsystem name in list
     *
     * @param subsystemName subsystem name
     * @param subsystemList subsystem list
     * @return found subystem or null
     */
    private DeploymentSubsystem findSubsystem(String subsystemName, List<DeploymentSubsystem> subsystemList)
    {
        DeploymentSubsystem response = null;
        for (DeploymentSubsystem subsystem : subsystemList)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(subsystem.getSubsystem().getSubsystemId());
            if (subsystemName.equals(subsystemDTO.getSubsystemName()))
            {
                response = subsystem;
                break;
            }
        }
        return response;
    }

    /**
     * Add service conflicts
     *
     * @param replaceSubsystemConflict replace subsystem conflict
     * @param oldSubsystem      old subsystem
     * @param newSubsystem      new subsystem
     */
    private void addServiceConflicts(ReplaceSubsystemConflict replaceSubsystemConflict,
                                     DeploymentSubsystem oldSubsystem,
                                     DeploymentSubsystem newSubsystem)
    {
        List<ReplaceServiceConflict> replaceServiceConflictList = new ArrayList<>();
        // Check each old service
        this.checkOldServices(oldSubsystem, newSubsystem, replaceServiceConflictList);
        // Check each new service
        this.checkNewServices(oldSubsystem, newSubsystem, replaceServiceConflictList);

        //Create and set array
        replaceSubsystemConflict.setReplaceServiceConflictList(replaceServiceConflictList);
    }

    /**
     * Check new services
     *
     * @param oldSubsystem     old subsystem
     * @param newSubsystem     new subsystem
     * @param replaceServiceConflictList replace service conflicts
     */
    private void checkNewServices(DeploymentSubsystem oldSubsystem, DeploymentSubsystem newSubsystem, List<ReplaceServiceConflict> replaceServiceConflictList)
    {
        for (DeploymentService newService : newSubsystem.getDeploymentServices())
        {
            DeploymentService oldService = this.findService(newService.getService().getArtifactId(),
                    oldSubsystem.getDeploymentServices());
            this.checkServiceForCreation(replaceServiceConflictList, newService, oldService);
        }
    }

    /**
     * Check service for creation
     *
     * @param replaceServiceConflictList replace service conflicts
     * @param newService       new service
     * @param oldService       old service
     */
    private void checkServiceForCreation(List<ReplaceServiceConflict> replaceServiceConflictList, DeploymentService newService, DeploymentService oldService)
    {
        if (oldService == null)
        {
            ReplaceServiceConflict replaceServiceConflict = new ReplaceServiceConflict();
            replaceServiceConflict.setArtifactId(newService.getService().getArtifactId());
            replaceServiceConflict.setNumberOfInstances(newService.getNumberOfInstances());
            replaceServiceConflict.setVersion(newService.getService().getVersion());
            replaceServiceConflict.setAction(ReplaceActionErrors.ACTION_CREATE);
            replaceServiceConflictList.add(replaceServiceConflict);
        }
    }

    /**
     * Check old services
     *
     * @param oldSubsystem     old subsystem
     * @param newSubsystem     new subsystem
     * @param replaceServiceConflictList replace service conflicts
     */
    private void checkOldServices(DeploymentSubsystem oldSubsystem, DeploymentSubsystem newSubsystem, List<ReplaceServiceConflict> replaceServiceConflictList)
    {
        for (DeploymentService oldService : oldSubsystem.getDeploymentServices())
        {
            ReplaceServiceConflict replaceServiceConflict = new ReplaceServiceConflict();
            replaceServiceConflict.setArtifactId(oldService.getService().getArtifactId());
            replaceServiceConflict.setVersion(oldService.getService().getVersion());
            replaceServiceConflict.setNumberOfInstances(oldService.getNumberOfInstances());

            DeploymentService newService = this.findService(oldService.getService().getArtifactId(), newSubsystem.getDeploymentServices());

            //Check service
            this.checkService(oldService, replaceServiceConflict, newService);
            replaceServiceConflictList.add(replaceServiceConflict);
        }
    }

    /**
     * Check service
     *
     * @param oldService      old service
     * @param replaceServiceConflict replace service conflict
     * @param newService      new service
     */
    private void checkService(DeploymentService oldService, ReplaceServiceConflict replaceServiceConflict, DeploymentService newService)
    {
        if ((newService == null) || (!oldService.getService().getVersion().equals(newService.getService().getVersion())))
        {
            replaceServiceConflict.setVersion(oldService.getService().getVersion());
            replaceServiceConflict.setAction(ReplaceActionErrors.ACTION_REMOVE);
        }
        else if (oldService.getNumberOfInstances() != newService.getNumberOfInstances())
        {
            replaceServiceConflict.setAction(ReplaceActionErrors.ACTION_UPDATE_INSTANCES + newService.getNumberOfInstances());
        }
        else
        {
            replaceServiceConflict.setAction(ReplaceActionErrors.ACTION_KEEP);
        }
    }

    /**
     * Find service artifact id in list
     *
     * @param artifactId  artifact id
     * @param serviceList service list
     * @return found service or null
     */
    private DeploymentService findService(String artifactId, List<DeploymentService> serviceList)
    {
        DeploymentService response = null;
        for (DeploymentService service : serviceList)
        {
            if (artifactId.equals(service.getService().getArtifactId()))
            {
                response = service;
                break;
            }
        }
        return response;
    }
}

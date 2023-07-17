package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;


import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentGcspDto;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Deployer gcsp service
 *
 * @author XE72018
 */
@Slf4j
@Service
public class DeploymentGcspServiceImpl implements IDeploymentGcspService
{
    @Value("${nova.deploymentScript.machine}")
    private String machine;
    @Value("${nova.deploymentScript.user}")
    private String user;
    @Value("${nova.deploymentScript.laucher}")
    private String launcherScript;
    @Value("${nova.deploymentScript.starter}")
    private String starterScript;

    /** client of ScheduleRequest */
    private final ScheduleControlMClient scheduleControlMClient;

    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Deployment utils
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * constructor by params
     * @param scheduleControlMClient client
     * @param deploymentPlanRepository repository
     * @param deploymentUtils utils
     */
    @Autowired
    public DeploymentGcspServiceImpl(final ScheduleControlMClient scheduleControlMClient, final DeploymentPlanRepository deploymentPlanRepository,
                                     final DeploymentUtils deploymentUtils)
    {
        this.scheduleControlMClient = scheduleControlMClient;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.deploymentUtils = deploymentUtils;
    }

    @Override
    public void updateGcsp(DeploymentPlan deploymentPlan, DeploymentGcspDto gcspDto)
    {
        DeploymentGcsp gcsp = null;
        if (deploymentPlan.getGcsp() == null)
        {
            gcsp = new DeploymentGcsp();
            deploymentPlan.setGcsp(gcsp);
        }
        else
        {
            gcsp = deploymentPlan.getGcsp();
        }

        //Expected date for deployment
        if (gcspDto.getExpectedDeploymentDate() != null)
        {
            try
            {
                log.debug("API deployment: DeploymentGcspService -> GCSP Expected Deployment Date change from [{}]", gcsp
                        .getExpectedDeploymentDate());
                gcsp.setExpectedDeploymentDate(DateUtils.parseDate(gcspDto.getExpectedDeploymentDate()
                        .replaceAll("Z$", "+0000"), DeploymentConstants.DATE_FORMAT_PATTERNS));
                log.debug("API deployment: DeploymentGcspService -> GCSP Expected Deployment Date change to [{}]", gcsp
                        .getExpectedDeploymentDate());
            }
            catch (ParseException e)
            {
                log.error("Error parsing date [{}]: [{}]", gcspDto.getExpectedDeploymentDate(), e);
                throw new NovaException(DeploymentError.getInvalidDateFormatError(), e.getMessage());
            }
        }

        // Optional release id to undeploy before the deployment
        if (gcspDto.getUndeployRelease() != null)
        {
            if (gcspDto.getUndeployRelease() == 0)
            {
                log.debug("API deployment: DeploymentGcspService -> GCSP Undeploy Release change from [{}]", gcsp.getUndeployRelease());
                gcsp.setUndeployRelease(gcspDto.getUndeployRelease());
                log.debug("API deployment: DeploymentGcspService -> GCSP Undeploy Release change to [{}]", gcsp.getUndeployRelease());
            }
            else
            {
                DeploymentPlan undeployPlan = deploymentPlanRepository.findById(gcspDto.getUndeployRelease()).orElseThrow(()
                        -> new NovaException(DeploymentError.getUndeployPlanNotFoundError(),"Plan with Id [" + gcspDto.getUndeployRelease() +"] not found"));

                if (validateUndeployId(deploymentPlan, gcspDto.getUndeployRelease()))
                {
                    log.debug("API deployment: DeploymentGcspService -> GCSP Undeploy Release change from [{}]", gcsp.getUndeployRelease());
                    gcsp.setUndeployRelease(gcspDto.getUndeployRelease());
                    log.debug("API deployment: DeploymentGcspService -> GCSP Undeploy Release change to [{}]", gcsp.getUndeployRelease());
                }
                else
                {
                    log.error("Plan with Id [{}] is not valid for undeploy action in PRO environment, it is not a PRO plan or does not " +
                            "belongs to the selected plan", gcspDto.getUndeployRelease());
                    throw new NovaException(DeploymentError.getUndeployPlanIsNotValidError(), "");
                }
            }
        }

        // Priority level to deploy
        if (gcspDto.getPriorityLevel() != null)
        {
            log.debug("API deployment: DeploymentGcspService -> GCSP Priority Level change from [{}]", gcsp.getPriorityLevel());
            switch (gcspDto.getPriorityLevel())
            {
                case "PRODUCT":
                    gcsp.setPriorityLevel(DeploymentPriority.PRODUCT);
                    break;
                case "SUBSYSTEM":
                    gcsp.setPriorityLevel(DeploymentPriority.SUBSYSTEM);
                    break;
                case "SERVICE":
                    gcsp.setPriorityLevel(DeploymentPriority.SERVICE);
                    break;
                default:
                    log.warn("Invalid priority level [{}]", gcspDto.getPriorityLevel());
                    break;

            }
            log.debug("API deployment: DeploymentGcspService -> GCSP Priority Level change to [{}]", gcsp.getPriorityLevel());
        }

        // Deployment list of the Subsystems or Services
        if (gcspDto.getDeploymentList() != null)
        {
            log.debug("API deployment: DeploymentGcspService -> GCSP Deployment List change from [{}]", gcsp.getDeploymentList());
            //Transforming the array list into a CSV String
            if (gcsp.getPriorityLevel() == DeploymentPriority.SERVICE || gcsp.getPriorityLevel() == DeploymentPriority.SUBSYSTEM)
            {
                ArrayList<String> list = new ArrayList<>();
                for (int id : gcspDto.getDeploymentList())
                {
                    list.add(String.valueOf(id));
                }
                gcsp.setDeploymentList(String.join(",", list));
            }
            else
            {
                gcsp.setDeploymentList(null);
            }
            log.debug("API deployment: DeploymentGcspService -> GCSP Deployment List change to [{}]", gcsp.getDeploymentList());
        }
    }

    @Override
    public DeploymentGcspDto getGcspDto(DeploymentPlan plan, String ivuser)
    {
        DeploymentGcsp gcsp = plan.getGcsp();

        if (gcsp == null)
        {
            log.debug("API deployment: DeploymentGcspService -> GCSP is EMPTY");
            return new DeploymentGcspDto();
        }

        DeploymentGcspDto dto = new DeploymentGcspDto();

        // Batch Id
        this.getBatchId(plan, ivuser, dto);


        if (gcsp.getExpectedDeploymentDate() == null)
        {
            dto.setExpectedDeploymentDate(null);
            log.debug("API deployment: DeploymentGcspService -> Expected Deployment Date is NULL");
        }
        else
        {
            dto.setExpectedDeploymentDate((gcsp.getExpectedDeploymentDate().toInstant().toString()));
            log.debug("API deployment: DeploymentGcspService -> Expected Deployment Date is [{}]", gcsp.getExpectedDeploymentDate());
        }

        dto.setUndeployRelease(gcsp.getUndeployRelease());
        log.debug("API deployment: DeploymentGcspService -> Release to undeploy is [{}]", gcsp.getUndeployRelease());

        if (gcsp.getPriorityLevel() == null)
        {
            dto.setPriorityLevel(null);
            log.debug("API deployment: DeploymentGcspService -> Priority level is NULL");
        }
        else
        {
            dto.setPriorityLevel(gcsp.getPriorityLevel().getPriority());
            log.debug("API deployment: DeploymentGcspService -> Priority level is [{}]", gcsp.getPriorityLevel());
        }

        if (StringUtils.isEmpty(gcsp.getDeploymentList()) || gcsp.getDeploymentList().isEmpty())
        {
            dto.setDeploymentList(null);
            log.debug("API deployment: DeploymentGcspService -> Deployment List is NULL");
        }
        else
        {
            String[] sList = gcsp.getDeploymentList().split(",");
            int[] numList = new int[sList.length];
            for (int i = 0; i < numList.length; i++)
            {
                numList[i] = Integer.parseInt(sList[i]);
            }
            dto.setDeploymentList(numList);
            log.debug("API deployment: DeploymentGcspService -> Deployment list is [{}]", gcsp.getDeploymentList());
        }

        log.debug("API deployment: DeploymentGcspService -> DeploymentGcspDto created contains: [{}]", dto);

        return dto;
    }

    private void getBatchId(DeploymentPlan plan, String ivuser, DeploymentGcspDto dto)
    {
        String date;
        if (plan.getExecutionDate() == null)
        {
            date = DatatypeConverter.printDateTime(Calendar.getInstance(Locale.FRANCE));
        }
        else
        {
            date = DatatypeConverter.printDateTime(plan.getExecutionDate());
        }

        ScheduleRequest scheduleRequest = this.scheduleControlMClient.getActiveRequestAt(Long.valueOf(plan.getReleaseVersion().getRelease()
                .getProduct().getId()), Environment.PRO.getEnvironment(), date);

        if (scheduleRequest == null)
        {
            dto.setBatchPlanId(null);
            log.debug("Schedule Batch Id is null");
        }
        else
        {
            dto.setBatchPlanId(Math.toIntExact(scheduleRequest.getId()));
            log.debug("Schedule Batch Id is [{}]", scheduleRequest.getId());
        }
    }

    @Override
    public String getDeploymentScript(DeploymentPlan plan)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Para el pase especificado se quieren planificar en nova las siguientes ejecuciones secuenciales:");

        sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

        sb.append("Con el usuario ");
        sb.append(this.user);
        sb.append(" en la máquina ");
        sb.append(this.machine);
        sb.append(" ejecutar ");
        sb.append(this.launcherScript);

        sb.append(" ").append(plan.getId());
        if (plan.getGcsp().getUndeployRelease() != 0)
        {
            sb.append(" ").append(plan.getGcsp().getUndeployRelease());
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append("Esta acción generará primero una tarea de UNDEPLOY_PRO del plan ");
            sb.append(plan.getGcsp().getUndeployRelease());
            sb.append('.');
        }
        sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
        sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
        sb.append("Esta acción generará una tarea de DEPLOY_PRO del plan ");
        sb.append(plan.getId());
        sb.append('.');
        sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
        sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
        sb.append("Si termina con éxito ejecutar:").append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

        sb.append("Con el usuario ");
        sb.append(this.user);
        sb.append(" en la máquina ");
        sb.append(this.machine);
        sb.append(" ejecutar ");
        sb.append(this.starterScript);

        sb.append(" ").append(plan.getId());
        sb.append(" ").append(plan.getGcsp().getPriorityLevel().getPriority());
        if (!plan.getGcsp().getPriorityLevel().equals(DeploymentPriority.PRODUCT))
        {
            sb.append(" ").append(plan.getGcsp().getDeploymentList());
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append("Esta acción generará las tareas necesarias del tipo ");
            sb.append(plan.getGcsp().getPriorityLevel().getPriority());
            sb.append("_START");
        }
        else
        {
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append(java.lang.System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append("Esta acción generará la tarea del tipo RELEASE_START");
        }

        return sb.toString();
    }

    @Override
    public void validateGcspForDeploy(DeploymentPlan plan)
    {
        DeploymentGcsp gcsp = plan.getGcsp();

        if (plan.getStatus().equals(DeploymentStatus.SCHEDULED))
        {
            log.error(" Deployment plan [{}] is already scheduled.", plan.getId());
            throw new NovaException(DeploymentError.getScheduledPlanError(), "");
        }

        if (gcsp.getPriorityLevel() == null)
        {
            log.error("Plan [{}] has an invalid priority level: [{}]", plan.getId(),
                    plan.getGcsp().getPriorityLevel());
            throw new NovaException(DeploymentError.getPriorityLevelNotValidError(), "");
        }
        else if (!(gcsp.getPriorityLevel() == DeploymentPriority.PRODUCT || gcsp.getPriorityLevel() ==
                DeploymentPriority.SERVICE || gcsp
                .getPriorityLevel() == DeploymentPriority.SUBSYSTEM))
        {
            log.error("Plan [{}] has an invalid priority level: [{}]", plan.getId(),
                    plan.getGcsp().getPriorityLevel().getPriority());
            throw new NovaException(DeploymentError.getPriorityLevelNotValidError(), "");
        }
    }

    ///// Private

    /**
     * Validate if the selected id is valid to undeploy.
     *
     * @param deploymentPlan deployment plan
     * @param id             id to undeploy
     * @return true if the id is deployed in PRO for the product of the deployment plan
     */
    boolean validateUndeployId(DeploymentPlan deploymentPlan, int id)
    {
        List<DeploymentPlan> plans = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(),
                Environment.PRO.getEnvironment(),
                DeploymentStatus.DEPLOYED);
        for (DeploymentPlan plan : plans)
        {
            if (plan.getId().equals(id))
            {
                return true;
            }
        }
        return false;
    }
}

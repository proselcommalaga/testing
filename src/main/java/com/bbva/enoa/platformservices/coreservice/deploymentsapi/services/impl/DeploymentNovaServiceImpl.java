package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentNovaDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemDto;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Deployer nova planned service
 *
 * @author XE85599
 */
@Slf4j
@Service
public class DeploymentNovaServiceImpl implements IDeploymentNovaService
{
    /**
     * Scheduler request client
     */
    @Autowired
    private ScheduleControlMClient scheduleControlMClient;

    /**
     * Deployment plan repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Service repository
     */
    @Autowired
    private DeploymentServiceRepository serviceRepository;

    /**
     * DeploymentPlanDtoBuilder service
     */
    @Autowired
    private IDeploymentPlanDtoBuilder deploymentPlanDtoBuilder;

    /**
     * Deployment utils
     */
    @Autowired
    private DeploymentUtils deploymentUtils;

    @Override
    public void updateNova(DeploymentPlan deploymentPlan, DeploymentNovaDto novaDto)
    {
        DeploymentNova nova;
        if (deploymentPlan.getNova() == null)
        {
            nova = new DeploymentNova();
            deploymentPlan.setNova(nova);
        }
        else
        {
            nova = deploymentPlan.getNova();
        }

        //Date time for deployment
        if (novaDto.getDeploymentDateTime() != null)
        {
            try
            {
                log.debug("API deployment: DeploymentNovaService -> Nova Planned Deployment Date time change from [{}]", nova
                        .getDeploymentDateTime());
                nova.setDeploymentDateTime(DateUtils.parseDate(novaDto.getDeploymentDateTime()
                        .replaceAll("Z$", "+0000"), DeploymentConstants.DATE_FORMAT_PATTERNS));
                log.debug("API deployment: DeploymentNovaService -> Nova Planned Deployment Date time change to [{}]", nova
                        .getDeploymentDateTime());
            }
            catch (ParseException e)
            {
                log.error("Error parsing date time [{}]: [{}]", novaDto.getDeploymentDateTime(), e);
                throw new NovaException(DeploymentError.getInvalidDateFormatError(), e);
            }
        }

        // Optional release id to undeploy before the deployment
        if (novaDto.getUndeployRelease() != null)
        {
            if (novaDto.getUndeployRelease() == 0)
            {
                log.debug("API deployment: DeploymentNovaService -> Nova Planned Undeploy Release change from [{}]", nova.getUndeployRelease());
                nova.setUndeployRelease(novaDto.getUndeployRelease());
                log.debug("API deployment: DeploymentNovaService -> Nova Planned Undeploy Release change to [{}]", nova.getUndeployRelease());
            }
            else
            {
                DeploymentPlan undeployPlan = deploymentPlanRepository.findById(novaDto.getUndeployRelease()).orElseThrow(() ->
                        new NovaException(DeploymentError.getUndeployPlanNotFoundError(), "Plan with Id [" + novaDto.getUndeployRelease() + "] not found"));

                if (validateUndeployId(deploymentPlan, novaDto.getUndeployRelease()))
                {
                    log.debug("API deployment: DeploymentNovaService -> Nova Planned Undeploy Release change from [{}]", nova.getUndeployRelease());
                    nova.setUndeployRelease(novaDto.getUndeployRelease());
                    log.debug("API deployment: DeploymentNovaService -> Nova Planned Undeploy Release change to [{}]", nova.getUndeployRelease());
                }
                else
                {
                    log.error("Plan with Id [{}] is not valid for undeploy action in PRO environment, it is not a PRO plan or does not " +
                            "belongs to the selected plan", novaDto.getUndeployRelease());
                    throw new NovaException(DeploymentError.getUndeployPlanIsNotValidError(), "Invalid undeploy plan with Id [" + novaDto.getUndeployRelease() + "]");
                }
            }
        }

        // Priority level to deploy
        if (novaDto.getPriorityLevel() != null)
        {
            log.debug("API deployment: DeploymentNovaService -> Nova Planned Priority Level change from [{}]", nova.getPriorityLevel());

            if(EnumUtils.isValidEnum(DeploymentPriority.class, novaDto.getPriorityLevel()))
            {
                nova.setPriorityLevel( DeploymentPriority.valueOf(novaDto.getPriorityLevel()) );
            }
            else
            {
                log.warn("Invalid priority level [{}]", novaDto.getPriorityLevel());
            }
            log.debug("API deployment: DeploymentNovaService -> Nova Planned Priority Level change to [{}]", nova.getPriorityLevel());
        }

        // Deployment list of the Subsystems or Services
        if (novaDto.getDeploymentList() != null)
        {
            log.debug("API deployment: DeploymentNovaService -> Nova Planned Deployment List change from [{}]", nova.getDeploymentList());
            //Transforming the array list into a CSV String
            if (nova.getPriorityLevel() == DeploymentPriority.SERVICE || nova.getPriorityLevel() == DeploymentPriority.SUBSYSTEM)
            {
                nova.setDeploymentList(
                    Arrays.stream(novaDto.getDeploymentList()).boxed().
                            map(Object::toString).collect(Collectors.joining(","))
                );
            }
            else
            {
                nova.setDeploymentList(null);
            }
            log.debug("API deployment: DeploymentNovaService -> Nova Planned Deployment List change to [{}]", nova.getDeploymentList());
        }
    }

    @Override
    public DeploymentNovaDto getNovaDto(DeploymentPlan plan, String ivuser)
    {
        DeploymentNova nova = plan.getNova();

        if (nova == null)
        {
            log.debug("API deployment: DeploymentNovaService -> Nova Planned is EMPTY");
            return new DeploymentNovaDto();
        }

        log.debug("API deployment: DeploymentNovaService -> Creating DeploymentNovaDto from [{}]", nova.toString());
        DeploymentNovaDto dto = new DeploymentNovaDto();

        // Batch Id
        this.getBatchId(plan, ivuser, dto);


        if (nova.getDeploymentDateTime() == null)
        {
            dto.setDeploymentDateTime(null);
            log.debug("API deployment: DeploymentNovaService -> Deployment Date time is NULL");
        }
        else
        {
            dto.setDeploymentDateTime(nova.getDeploymentDateTime().toInstant().toString());
            log.debug("API deployment: DeploymentNovaService -> Deployment Date time is [{}]", nova.getDeploymentDateTime());
        }

        dto.setUndeployRelease(nova.getUndeployRelease());
        log.debug("API deployment: DeploymentNovaService -> Release to undeploy is [{}]", nova.getUndeployRelease());

        if (nova.getPriorityLevel() == null)
        {
            dto.setPriorityLevel(null);
            log.debug("API deployment: DeploymentNovaService -> Priority level is NULL");
        }
        else
        {
            dto.setPriorityLevel(nova.getPriorityLevel().getPriority());
            log.debug("API deployment: DeploymentNovaService -> Priority level is [{}]", nova.getPriorityLevel());
        }

        if (StringUtils.isEmpty(nova.getDeploymentList()) || nova.getDeploymentList().isEmpty())
        {
            dto.setDeploymentList(null);
            log.debug("API deployment: DeploymentNovaService -> Deployment List is NULL");
        }
        else
        {
            String[] sList = nova.getDeploymentList().split(",");
            int[] numList = new int[sList.length];
            for (int i = 0; i < numList.length; i++)
            {
                numList[i] = Integer.parseInt(sList[i]);
            }
            dto.setDeploymentList(numList);
            log.debug("API deployment: DeploymentNovaService -> Deployment list is [{}]", nova.getDeploymentList());
        }

        log.debug("API deployment: DeploymentNovaService -> DeploymentNovaDto created contains: [{}]", dto.toString());

        return dto;
    }

    private void getBatchId(DeploymentPlan plan, String ivuser, DeploymentNovaDto dto)
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
    public String getDeploymentActions(DeploymentPlan plan)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Para el pase NOVA especificado se quieren planificar las siguientes ejecuciones secuenciales para el Plan de Despliegue con id: ");
        sb.append(plan.getId()).append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

        if (plan.getNova().getUndeployRelease() != 0)
        {
            sb.append("· Una tarea de tipo UNDEPLOY_PRO (repliegue) del Plan de Despliegue con id: ");
            sb.append(plan.getNova().getUndeployRelease()).append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
        }

        sb.append("· Una tarea de tipo DEPLOY_PRO (despliegue) del Plan de Despliegue con id: ");
        sb.append(plan.getId()).append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

        sb.append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

        // start product
        if (DeploymentPriority.PRODUCT.equals(plan.getNova().getPriorityLevel()))
        {
            sb.append("Si termina con éxito, esta acción generará una tarea de tipo RELEASE_START.");
        }

        // start services or start subsystems in a given order
        else
        {
            sb.append("Si termina con éxito, esta acción generará las tareas necesarias de tipo ");
            sb.append(plan.getNova().getPriorityLevel().getPriority()).append("_START").append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
            sb.append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

            String startOrder = Optional.ofNullable(plan.getNova().getDeploymentList()).orElse("");
            log.debug("startOrder: " + startOrder);

            if (!Strings.isNullOrEmpty(startOrder))
            {
                sb.append("Orden de arranque: ").append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));

                DeploymentDto deploymentDto = deploymentPlanDtoBuilder.build(plan, Constants.IMMUSER);

                Arrays.stream(startOrder.split(",")).forEach(str -> {
                    Integer startId = Integer.parseInt(str);
                    String name = "";
                    if (DeploymentPriority.SERVICE.equals(plan.getNova().getPriorityLevel()))
                    {
                        Optional<DeploymentService> deploymentService = serviceRepository.findById(startId);
                        if (deploymentService.isPresent())
                        {
                            name = deploymentService.get().getService().getServiceName();
                        }
                    }
                    else
                    {
                        DeploymentSubsystemDto deploymentSubsystemDto = Arrays.stream(deploymentDto.getSubsystems())
                                .filter(ss -> startId.equals(ss.getId())).findAny().orElse(null);

                        if (deploymentSubsystemDto != null)
                        {
                            name = deploymentSubsystemDto.getSubsystemName();
                        }
                    }
                    sb.append("· ").append(str).append(" - ").append(name).append(System.getProperty(DeploymentConstants.LINE_SEPARATOR_PROPERTY));
                });
            }
        }

        return sb.toString();
    }

    @Override
    public void validateNovaPlannedForDeploy(DeploymentPlan plan)
    {
        DeploymentNova nova = plan.getNova();

        if (nova == null)
        {
            log.error(" Deployment plan [{}] hasn't been configured with the configuration of the Nova deployment.", plan.getId());
            throw new NovaException(DeploymentError.getInvalidNovaPlannedPlanError(plan.getId(), plan.getDeploymentTypeInPro().name(), plan.getStatus().name(), null));
        }

        if (plan.getStatus().equals(DeploymentStatus.SCHEDULED))
        {
            log.error(" Deployment plan [{}] is already scheduled.", plan.getId());
            throw new NovaException(DeploymentError.getScheduledPlanError(), "");
        }

        if (nova.getPriorityLevel() == null)
        {
            log.error("Plan [{}] has an invalid priority level: [{}]", plan.getId(),
                    plan.getNova().getPriorityLevel());
            throw new NovaException(DeploymentError.getPriorityLevelNotValidError(), "");
        }
        else if (!(nova.getPriorityLevel() == DeploymentPriority.PRODUCT || nova.getPriorityLevel() ==
                DeploymentPriority.SERVICE || nova
                .getPriorityLevel() == DeploymentPriority.SUBSYSTEM))
        {
            log.error("Plan [{}] has an invalid priority level: [{}]", plan.getId(),
                    plan.getNova().getPriorityLevel().getPriority());
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

package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSummaryDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

import java.util.List;

public interface IDeploymentPlanDtoBuilder
{
    /**
     * Builds a DeploymentDto from a DeploymentPlan.
     *
     * @param plan   DeploymenPlan
     * @param ivUser iv-User
     * @return DeploymentDto
     */
    DeploymentDto build(DeploymentPlan plan, String ivUser);

    /**
     * Converts a list of DeploymentPlan to an array of DeploymentDto.
     *
     * @param plans  List of {@link DeploymentPlan}.
     * @param ivUser iv-User
     * @return Array of {@link DeploymentDto}.
     */
    DeploymentDto[] build(List<DeploymentPlan> plans, String ivUser);

    /**
     * Builds a {@link DeploymentServiceDto} from a {@link DeploymentService}.
     *
     * @param deploymentService - Source {@code DeploymentService}
     * @return a {@code DeploymentServiceDto}
     */
    DeploymentServiceDto buildDtoFromEntity(DeploymentService deploymentService);

    /**
     * Build summary
     *
     * @param plan   plan
     * @param ivUser user
     * @return summary
     */
    DeploymentSummaryDto buildSummary(DeploymentPlan plan, String ivUser);

    /**
     * Whether a deployment plan is required to have an schedule request with its associated document.
     *
     * @param deploymentPlan The deployment plan.
     * @return true or false.
     */
    Boolean isScheduleRequestAndDocumentRequired(DeploymentPlan deploymentPlan);

    /**
     * Names of the Batches of a Deployment Plan not in a NOVA Scheduler (i.e. that will be started by Control M).
     *
     * @param deploymentPlan The Deploymen Plan.
     * @return The List of names of the Batches.
     */
    List<String> namesOfBatchesServicesNotInSchedulers(DeploymentPlan deploymentPlan);

}

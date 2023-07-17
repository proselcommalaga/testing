package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewDeploymentRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewProjectRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;

/**
 * Issue tracker client interface
 */
public interface IIssueTrackerClient
{
    /**
     * @param newDeploymentRequest deployment request
     * @param productId            the product id
     * @return issue tracker items
     */
    IssueTrackerItem[] createDeploymentRequest(ITNewDeploymentRequest newDeploymentRequest, Integer productId);

    /**
     * @param newProjectRequest project request
     * @return issue tracker items
     */
    IssueTrackerItem createProjectRequest(ITNewProjectRequest newProjectRequest);
}

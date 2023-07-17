package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

/**
 * Create a Deployment Request in Issue Tracker : JIRA
 */
public interface IIssueTrackerService
{
    /**
     * Create request
     * Example:
     * {
     * "projectKey": "newDeploymentRequest",
     * "version": "1",
     * "uuaa": "ENVM",
     * "environmentOwner": "Project Support",
     * "productName": "productName",
     * "reporter": "XE34260"
     * }
     *
     * @param releaseVersion release version
     * @param ivUser         user
     * @return list of issues
     */
    IssueTrackerItem[] createDeploymentRequest(ReleaseVersion releaseVersion, String ivUser);
}

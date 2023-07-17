package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewDeploymentRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IIssueTrackerClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IIssueTrackerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Create a Deployment Request in Issue Tracker : JIRA
 */
@Service
@Slf4j
public class IssueTrackerServiceImpl implements IIssueTrackerService
{

    @Autowired
    private IIssueTrackerClient iIssuetrackerClient;

    @Override
    public IssueTrackerItem[] createDeploymentRequest(final ReleaseVersion releaseVersion, final String ivUser)
    {
        String jiraProjectKey = releaseVersion.getRelease().getProduct().getDesBoard();

        if (StringUtils.isEmpty(jiraProjectKey))
        {
            String message = "[ReleaseVersionAPI] -> [createDeploymentRequest]: The product: [" + releaseVersion.getRelease().getProduct().getName() + "] does not have " +
                    "jira project key. Provide one before continue.";
            log.error("[{}] -> [{}]: The product: [{}] does not have jira project key. Provide one before continue.",
                    Constants.ISSURE_TRACKER_SERVICE, "createDeploymentRequest", releaseVersion.getRelease().getProduct().getName());
            throw new NovaException(ReleaseVersionError.getNoSuchJiraProjectKeyError(), message);
        }

        // Mapear los datos de la ReleaseVersion con el objeto ITNewDeploymentRequest que espera el cliente Feign
        ITNewDeploymentRequest newDeploymentRequest = new ITNewDeploymentRequest();
        newDeploymentRequest.setProjectKey(jiraProjectKey);
        newDeploymentRequest.setUuaa(releaseVersion.getRelease().getProduct().getUuaa());
        newDeploymentRequest.setEnvironmentOwner("Service Support");
        newDeploymentRequest.setProductName(releaseVersion.getRelease().getProduct().getName());
        newDeploymentRequest.setVersion(releaseVersion.getVersionName());
        newDeploymentRequest.setReporter(ivUser);

        // [4] once the ReleaseVersion was built, we are going to create a DeploymentRequest JIRA, so call to IssueTracker.
        return this.iIssuetrackerClient.createDeploymentRequest(newDeploymentRequest, releaseVersion.getRelease().getProduct().getId());
    }

}

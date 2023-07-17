package com.bbva.enoa.platformservices.coreservice.consumers.impl;


import com.bbva.enoa.apirestgen.issuetrackerapi.client.feign.nova.rest.IRestHandlerIssuetrackerapi;
import com.bbva.enoa.apirestgen.issuetrackerapi.client.feign.nova.rest.IRestListenerIssuetrackerapi;
import com.bbva.enoa.apirestgen.issuetrackerapi.client.feign.nova.rest.impl.RestHandlerIssuetrackerapi;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewDeploymentRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewProjectRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.core.novabootstarter.consumers.ApiClientResponseWrapper;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IIssueTrackerClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Feign Client for communication with Issue Tracker.
 */
@Service
public class IssueTrackerClient implements IIssueTrackerClient
{
    private static final Logger LOG = LoggerFactory.getLogger(IssueTrackerClient.class);
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * IRest handler
     */
    private final IRestHandlerIssuetrackerapi iRestHandlerIssuetrackerapi;

    /**
     * Rest handler implementation
     */
    private RestHandlerIssuetrackerapi restHandlerImpl;


    /**
     * To do task service
     */
    private IErrorTaskManager errorTaskMgr;

    /**
     * Bean constructor for dependency injection
     *
     * @param iRestHandlerIssuetrackerapi IRestHandlerIssuetrackerapi dependency
     */
    @Autowired
    public IssueTrackerClient(IRestHandlerIssuetrackerapi iRestHandlerIssuetrackerapi, IErrorTaskManager errorTaskMgr)
    {
        this.iRestHandlerIssuetrackerapi = iRestHandlerIssuetrackerapi;
        this.errorTaskMgr = errorTaskMgr;
    }

    /**
     * Post construct
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerImpl = new RestHandlerIssuetrackerapi(iRestHandlerIssuetrackerapi);
    }

    //////////////////////// IMPLEMENTATIONS /////////////////////////////////////////

    @Override
    public IssueTrackerItem[] createDeploymentRequest(ITNewDeploymentRequest newDeploymentRequest, final Integer productId)
    {
        LOG.debug("[IssueTrackerClient] -> [createDeploymentRequest]: getting Deployment Request from IssueTracker for new deployment request: [{}] and product id: [{}]", newDeploymentRequest, productId);
        IssueTrackerItem[] issueTrackerItems;
        ApiClientResponseWrapper<IssueTrackerItem[], Errors> response = new ApiClientResponseWrapper<>();

        this.restHandlerImpl.createDeploymentRequest(new IRestListenerIssuetrackerapi()
        {
            @Override
            public void createDeploymentRequest(IssueTrackerItem[] issueTrackerItem)
            {
                response.setValue(issueTrackerItem);
                LOG.debug("[IssueTrackerClient] -> [createDeploymentRequest]: created Deployment Request. Response: [{}]", Arrays.toString(issueTrackerItem));
            }

            @Override
            public void createDeploymentRequestErrors(Errors outcome)
            {
                response.setError(outcome);
                LOG.error("[IssueTrackerClient] -> [createDeploymentRequest]: Unable to create deployment request with parameters: [{}] and product id: [{}]. Error message: [{}]", newDeploymentRequest, productId, outcome.getBodyExceptionMessage());
            }
        }, newDeploymentRequest);


        // Check if there was some exception
        this.checkException(response.getError(), newDeploymentRequest, productId);

        // Build the file model if the response was successfully
        issueTrackerItems = response.getValue();

        return issueTrackerItems;
    }

    @Override
    public IssueTrackerItem createProjectRequest(final ITNewProjectRequest newProjectRequest)
    {
        LOG.debug("[IssueTrackerClient] -> [createProjectRequest]: getting Project Request from IssueTracker of new project request: [{}]", newProjectRequest);

        SingleApiClientResponseWrapper<IssueTrackerItem> response = new SingleApiClientResponseWrapper<>(new IssueTrackerItem());
        this.restHandlerImpl.createProjectRequest(new IRestListenerIssuetrackerapi()
        {
            @Override
            public void createProjectRequest(IssueTrackerItem issueTrackerItem)
            {
                response.set(issueTrackerItem);
                LOG.debug("[IssueTrackerClient] -> [createProjectRequest]: created Project Request. Response: [{}]", issueTrackerItem);
            }

            @Override
            public void createProjectRequestErrors(Errors outcome)
            {
                LOG.error("[IssueTrackerClient] -> [createProjectRequest]: Unable to create new project request for: [{}] Error message: [{}]", newProjectRequest, outcome.getBodyExceptionMessage());
            }
        }, newProjectRequest);

        return response.get();
    }

    ///////////////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * Generate to do task exception
     *
     * @param errors               exception
     * @param newDeploymentRequest the new deployment request
     * @param productId            the productId
     */
    private void checkException(final Errors errors, final ITNewDeploymentRequest newDeploymentRequest, final Integer productId)
    {
        if (errors != null)
        {
            // Create the to do task, log the error and
            String todoTaskDescription = "[IssueTrackerClient] -> [checkException]: Impossible to create a deployment request with the following parameters in JIRA:"
                    + LINE_SEPARATOR
                    + " - [Reporter]: " + newDeploymentRequest.getReporter()
                    + LINE_SEPARATOR
                    + " - [Description]: " + newDeploymentRequest.getDescription()
                    + LINE_SEPARATOR
                    + " - [EnvironmentOwner]: " + newDeploymentRequest.getEnvironmentOwner()
                    + LINE_SEPARATOR
                    + " - [ProductName]: " + newDeploymentRequest.getProductName()
                    + LINE_SEPARATOR
                    + " - [Summary]: " + newDeploymentRequest.getSummary()
                    + LINE_SEPARATOR
                    + " - [UUAA]: " + newDeploymentRequest.getUuaa()
                    + LINE_SEPARATOR
                    + " - [Version]: " + newDeploymentRequest.getVersion()
                    + LINE_SEPARATOR
                    + " - [ProjectKey]: " + newDeploymentRequest.getProjectKey();


            NovaError novaError = ReleaseVersionError.getJiraOptionsError();
            Exception exception = new Exception(errors.getFirstErrorMessage().orElse(new ErrorMessage("Unexpected Error")).toString());
            this.errorTaskMgr.createErrorTask(productId, novaError, todoTaskDescription, ToDoTaskType.ISSUE_TRACKER_ERROR, exception);
        }
    }
}

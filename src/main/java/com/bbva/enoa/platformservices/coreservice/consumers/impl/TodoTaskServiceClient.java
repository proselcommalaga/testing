package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestListenerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.impl.RestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.model.*;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

/**
 * Client to invoke Task Service
 *
 * @author Comunytek, S.L.
 */
@Service
public class TodoTaskServiceClient
{

    private static final Logger LOG = LoggerFactory.getLogger(TodoTaskServiceClient.class);
    private static final String ERROR_MESSAGE_CHUNK = "]. Error message: [";

    private static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";

    /**
     * Attribute - Todotask Rest Handler - Interface
     */
    @Autowired
    private IRestHandlerTodotaskapi iRestHandlerTodotaskapi;

    /**
     * Handler of the client of the to do task service
     */
    private RestHandlerTodotaskapi restHandler;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerTodotaskapi(iRestHandlerTodotaskapi);
    }

    /**
     * Create a generic {@link com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask}
     * to be processed with the generic processor.
     *
     * @param ivUser         - User if any.
     * @param previousTaskId - previous task if present
     * @param taskType       - {@code ToDoTaskType} name
     * @param role           - Role name
     * @param message        - Description of the new task
     * @param productId      - Product id
     * @return the generated task Id
     */
    public Integer createGenericTask(
            final String ivUser,
            final Integer previousTaskId,
            final String taskType,
            final String role,
            final String message,
            final Integer productId)
    {
        LOG.debug("Creating task with user [{}], previousTaskId: [{}], todotasktype [{}], role [{}], message [{}] and productId: [{}]",
                ivUser, previousTaskId, taskType, role, message, productId);

        final SingleApiClientResponseWrapper<Integer> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.createTodoTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void createTodoTask(Integer outcome)
            {
                LOG.debug("[createToDoTask] -> Received value for parameter outcome: {}", outcome);
                response.set(outcome);
            }

            @Override
            public void createTodoTaskErrors(final Errors outcome)
            {
                String messageError = "[TodoTask Client API] -> [createGenericTask]: Error creating a generic to do task with user: [" + ivUser + "], type: [" + taskType + "]" +
                        " product id: [" + productId + "] with message: [" + message + ERROR_MESSAGE_CHUNK + outcome.getBodyExceptionMessage() + "]";
                LOG.error(messageError);
                throw new NovaException(DeploymentError.getCreateTaskError(messageError), outcome);
            }
        }, previousTaskId, taskType, role, productId, null, message, null);

        return response.get();
    }


    /**
     * This method is in charge of invoke the {@code createToDoTask} endPoint of service TodoTaskService.
     *
     * @param deploymentId   - Deployment Plan Id
     * @param previousTaskId - previous task if present
     * @param taskType       - {@code ToDoTaskType} name
     * @param role           - Role name
     * @param message        - Description of the new task
     * @param product        - Product instance
     * @return the generated task Id
     */
    public Integer createDeploymentTask(final Integer deploymentId,
                                        final Integer previousTaskId,
                                        final String taskType,
                                        final String role,
                                        final String message,
                                        final Product product)
    {
        // Validate if the product has JIRA project associated
        this.checkIfHasJiraProject(product);

        final SingleApiClientResponseWrapper<Integer> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.createTodoTask(new IRestListenerTodotaskapi()
        {
            /**
             * Successful call
             */
            @Override
            public void createTodoTask(Integer outcome)
            {
                response.set(outcome);
            }

            @Override
            public void createTodoTaskErrors(Errors outcome)
            {
                String error = "Error trying to create a deployment Task for deployment plan: [" + deploymentId + ERROR_MESSAGE_CHUNK + outcome.getBodyExceptionMessage() + "]";
                LOG.error(error);
                throw new NovaException(DeploymentError.getCreateTaskError(error), outcome);
            }
        }, previousTaskId, taskType, role, product.getId(), deploymentId, message, null);
        return response.get();
    }

    /**
     * Create management task
     *
     * @param ivUser         user
     * @param previousTaskId previous task
     * @param taskType       task type
     * @param role           role
     * @param message        message
     * @param product        product instance
     * @param relatedId      related id
     * @return task id
     */
    public Integer createManagementTask(final String ivUser,
                                        final Integer previousTaskId,
                                        final String taskType,
                                        final String role,
                                        final String message,
                                        final Product product,
                                        final Integer relatedId)
    {
        // Validate if the product has JIRA project associated in case of the product exists
        Integer productId = null;

        if (product == null)
        {
            //TODO @Oscar ¿qué sentido tiene hacer esto? Si invocamos al cliente con productId nulo, petará la llamada y devolverá errores. ¿No sería mejor lanzar una excepción y ahorrar una llamada que sabemos que va a fallar?
            LOG.debug("[TodoTaskClientAPI] -> [createManagementTask]: the product is null. Set product Id to null.");
        }
        else
        {
            this.checkIfHasJiraProject(product);
            productId = product.getId();
        }

        LOG.debug("Creating management task with ivUser: [{}], previousTaskId: [{}], taskType: [{}], role: [{}], message: [{}], productId: [{}] and relatedId: [{}]",
                ivUser, previousTaskId, taskType, role, message, productId, relatedId);

        final SingleApiClientResponseWrapper<Integer> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.createTodoTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void createTodoTask(Integer outcome)
            {
                LOG.debug("[createManagementTask] -> Received value for parameter outcome: {}", outcome);
                response.set(outcome);
            }

            @Override
            public void createTodoTaskErrors(Errors outcome)
            {
                String error = "Error creating management task with user: [" + ivUser + "], type: [" + taskType + "] and message: " +
                        " [" + message + ERROR_MESSAGE_CHUNK + outcome.getBodyExceptionMessage() + "]";
                LOG.error(error);
                throw new NovaException(DeploymentError.getCreateTaskError(error), outcome);
            }
        }, previousTaskId, taskType, role, productId, null, message, relatedId);

        return response.get();
    }

    /**
     * Creates a task to establish api policies
     *
     * @param apiTaskCreationDTO task information
     * @return the to do task id or -1 if the to do task is already created
     */
    public Integer createApiTask(final ApiTaskCreationDTO apiTaskCreationDTO)
    {
        SingleApiClientResponseWrapper<Integer> integerSingleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        LOG.debug("[TodoTaskClient] -> [createApiTask]: creating todo task for api: [{}]", apiTaskCreationDTO);

        this.restHandler.createApiTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void createApiTask(final Integer outcome)
            {
                LOG.debug("[TodoTaskClient] -> [createApiTask]: created todo task [{}] for api: [{}]", outcome, apiTaskCreationDTO);
                integerSingleApiClientResponseWrapper.set(outcome);
            }

            @Override
            public void createApiTaskErrors(final Errors outcome)
            {
                LOG.error("[TodoTaskClient] -> [createApiTask]: there was an error calling to create a api task. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(ApiManagerError.getCreateApiTaskError(apiTaskCreationDTO.getApiKey().getUuaa(), apiTaskCreationDTO.getApiKey().getBasePath(), apiTaskCreationDTO.getApiKey().getApiName()));
            }

            @Override
            public void createApiTaskException409(final ExceptionDTO outcome)
            {
                LOG.warn("[TodoTaskClient] -> [createApiTask]: todo task already existed for api: [{}]. No action done", apiTaskCreationDTO);
                integerSingleApiClientResponseWrapper.set(Constants.TODO_TASK_ALREADY_CREATED_CODE);
            }

        }, apiTaskCreationDTO);

        return integerSingleApiClientResponseWrapper.get();
    }

    /**
     * Get the last task to establish api policies
     *
     * @param apiTaskKeyDTO task information
     * @return task for api
     */
    public ApiTaskDTO getApiTask(final ApiTaskKeyDTO apiTaskKeyDTO)
    {

        LOG.debug("[TodoTaskClient] -> [getApiTaskId]: get last todo task for api: [{}]",
                apiTaskKeyDTO);

        SingleApiClientResponseWrapper<ApiTaskDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getApiTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void getApiTask(final ApiTaskDTO outcome)
            {
                LOG.debug("[TodoTaskClient] -> [getApiTaskId]: recovered todo task with [{}] for api: [{}]",
                        Optional.ofNullable(outcome).map(ApiTaskDTO::toString).orElse("null"), apiTaskKeyDTO);
                response.set(outcome);
            }


            @Override
            public void getApiTaskErrors(final Errors outcome)
            {
                LOG.error("[TodoTaskClient] -> [getApiTaskId]: there was an error getting a api task. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(ApiManagerError.getRecoverApiTaskError(apiTaskKeyDTO.getUuaa(), apiTaskKeyDTO.getBasePath(), apiTaskKeyDTO.getApiName()));
            }

        }, apiTaskKeyDTO);

        return response.get();
    }

    /**
     * Get the last task to establish api policies for multiple apis in one call
     *
     * @param apiTaskFilterRequestDTOS tasks information
     * @return list of api tasks
     */
    public ApiTaskFilterResponseDTO[] getApiTaskList(final ApiTaskFilterRequestDTO[] apiTaskFilterRequestDTOS)
    {

        String dtos = Arrays.toString(apiTaskFilterRequestDTOS);
        LOG.debug("[TodoTaskClient] -> [getApiTaskList]: get a list of todo task for apis: [{}]",
                dtos);

        SingleApiClientResponseWrapper<ApiTaskFilterResponseDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.filterApiTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void filterApiTask(final ApiTaskFilterResponseDTO[] outcome)
            {
                LOG.debug("[TodoTaskClient] -> [getApiTaskList]: recovered list of todo task: [{}]",
                        Arrays.toString(outcome));
                response.set(outcome);
            }


            @Override
            public void filterApiTaskErrors(final Errors outcome)
            {
                LOG.error("[TodoTaskClient] -> [filterApiTask]: there was an error filtering a api task. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(ApiManagerError.getRecoverApiTaskListError());
            }

        }, apiTaskFilterRequestDTOS);

        return response.get();
    }

    /**
     * Removes all the tasks related to an api
     *
     * @param apiTaskKeyDTO task information
     */
    public void deleteApiTasks(final ApiTaskKeyDTO apiTaskKeyDTO)
    {

        LOG.debug("[TodoTaskClient] -> [deleteApiTasks]: removing all todo task for api: [{}]",
                apiTaskKeyDTO);

        this.restHandler.deleteApiTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void deleteApiTask()
            {
                LOG.debug("[TodoTaskClient] -> [deleteApiTasks]: removed all todo task for api: [{}]",
                        apiTaskKeyDTO);
            }


            @Override
            public void deleteApiTaskErrors(final Errors outcome)
            {
                LOG.error("[TodoTaskClient] -> [deleteApiTask]: there was an error deleting a api task. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(ApiManagerError.getDeleteApiTaskListError(apiTaskKeyDTO.getUuaa(), apiTaskKeyDTO.getBasePath(), apiTaskKeyDTO.getApiName()));
            }

        }, apiTaskKeyDTO);
    }

    /**
     * Get the list of Tasks created the specified number of days ago, and apply some other optional filters.
     *
     * @param daysAgo   Filter the Tasks this number of days ago.
     * @param productId Filter the Tasks only in this Product. If it's "ALL" or not present, don't apply this filter.
     *                  If it's "NOT_ASSIGNED", return the Tasks not assigned to any product.
     * @param status    Filter the Tasks only in this status. If it's "ALL" or not present, don't apply this filter.
     * @return A DTO containing the total number of tasks, and an array of TaskSummary.
     */
    public TaskSummaryList getTodoTasksSinceDaysAgo(Integer daysAgo, String productId, String status)
    {
        SingleApiClientResponseWrapper<TaskSummaryList> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[TodoTaskServiceClient] -> [getTodoTasksSinceDaysAgo]: getting TODO Tasks since [{}] days ago, with Product ID [{}] and Status [{}]", daysAgo, productId, status);

        this.restHandler.getTodoTasksSinceDaysAgo(new IRestListenerTodotaskapi()
        {
            @Override
            public void getTodoTasksSinceDaysAgo(TaskSummaryList outcome)
            {
                LOG.debug("[TodoTaskServiceClient] -> [getTodoTasksSinceDaysAgo]: successfully got TODO Tasks since [{}] days ago, with Product ID [{}] and Status [{}]", daysAgo, productId, status);
                response.set(outcome);
            }

            @Override
            public void getTodoTasksSinceDaysAgoErrors(Errors outcome)
            {
                LOG.error("[TodoTaskServiceClient] -> [getTodoTasksSinceDaysAgo]: Error trying to get TODO Tasks since [{}] days ago, with Product ID [{}] and Status [{}]: {}", daysAgo, productId, status, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getTodoTaskServiceError(), outcome);
            }

        }, daysAgo, productId, status);

        return response.get();
    }

    /**
     * Method to get if a specif deployment plan have any configuration management task pending.
     *
     * @param deploymentPlanId deploymentPlanId
     * @param taskType         taskType (configurationManagement)
     * @return boolean value
     */
    public boolean getDeploymentPlanManagementConfigurationTask(Integer deploymentPlanId, String taskType)
    {
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[TodoTaskServiceClient] -> [getDeploymentPlanManagementConfigurationTask]: getting [{}] task from deployment plan [{}]", taskType, deploymentPlanId);

        this.restHandler.getPlanConfigurationManagemetTask(new IRestListenerTodotaskapi()
        {
            @Override
            public void getPlanConfigurationManagemetTask(Boolean outcome)
            {
                LOG.debug("[TodoTaskServiceClient] -> [getDeploymentPlanManagementConfigurationTask]: successfully got [{}] task from deployment plan [{}]", taskType, deploymentPlanId);
                response.set(outcome);
            }

            @Override
            public void getPlanConfigurationManagemetTaskErrors(Errors outcome)
            {
                LOG.error("[TodoTaskServiceClient] -> [getDeploymentPlanManagementConfigurationTask]: Error trying to get getting [{}] task from deployment plan [{}]: {}", taskType, deploymentPlanId, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getTodoTaskServiceError(), outcome);
            }

        }, taskType, deploymentPlanId);

        return response.get();
    }

    /////////////////////// PRIVATE METHODS //////////////////////////

    /**
     * Check if the product has Jira project created
     *
     * @param product the product to check
     */
    private void checkIfHasJiraProject(final Product product)
    {
        if (product != null && StringUtils.isEmpty(product.getDesBoard()))
        {
            String messageError = "[ReleaseVersionAPI] -> [createDeploymentRequest]: The product: [" + product.getName() + "] does not have " +
                    "jira project associated. Please, get one from JIRA Team and complete the product section before continue.";
            LOG.error(messageError);
            throw new NovaException(DeploymentError.getNoSuchJiraProjectKeyError(), messageError);
        }
    }

}

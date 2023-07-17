package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestListenerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.impl.RestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.model.TasksProductCreation;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client for creating to do tasks
 */
@Service
public class ProductsAPIToDoTaskService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductsAPIToDoTaskService.class);

    /**
     * To do task error code
     */
    private static final Integer TO_DO_TASK_ERROR_CODE = -1;

    /**
     * Attribute - Todotask Rest Handler - Interface
     */
    @Autowired
    private IRestHandlerTodotaskapi iRestHandlerTodotaskapi;

    /**
     * Handler of the client of the to do task service
     */
    private RestHandlerTodotaskapi restHandlerTodotaskapi;

    /**
     * Constructor. Init the listeners.
     */
    @PostConstruct
    public void init()
    {
        // Task service client handler and listener
        this.restHandlerTodotaskapi = new RestHandlerTodotaskapi(this.iRestHandlerTodotaskapi);
    }

    /**
     * Creates a new product creation request
     *
     * @param productRequest productRequest
     * @return task id created
     */
    public Integer createProductCreationRequestTask(final TasksProductCreation productRequest)
    {
        AtomicReference<Integer> integerAtomicReference = new AtomicReference<>();

        LOG.debug("ProductsAPI: calling ToDoTask Service API -> /task/productcreation for creating new product request with parameters: [{}]", productRequest);
        this.restHandlerTodotaskapi.createProductCreationTask(new IRestListenerTodotaskapi()
        {

            @Override
            public void createProductCreationTask(Integer outcome)
            {

                LOG.debug("ProductsAPI TODO Task Listener (in response to products/newRequest) ->  To Do Task 'product creation request' " +
                        "created succesfully. TaskId [{}]", outcome);
                integerAtomicReference.set(outcome);
            }

            @Override
            public void createProductCreationTaskErrors(final Errors outcome)
            {
                LOG.error("ProductsAPI TODO Task Listener (in response to products/newRequest) -> The todo task service has failed. Error message: [{}] - Error code: [{}]",
                        outcome.getBodyExceptionMessage(), ProductsAPIError.getToDoTaskServiceCallError());
                integerAtomicReference.set(TO_DO_TASK_ERROR_CODE);
            }
        }, productRequest);

        if (integerAtomicReference.get().equals(TO_DO_TASK_ERROR_CODE))
        {
            LOG.error("ProductsAPI: /products/newRequest -> Failed trying to create a new to do task of type 'product creation" +
                    "request with the parameters: [{}] - ProductsAPI Error code: [{}]", productRequest, ProductsAPIError.getToDoTaskServiceCallError());
            throw new NovaException(ProductsAPIError.getToDoTaskServiceCallError(), "ProductsAPI: /products/newRequest ->" +
                    " Failed trying to create a new to do task of type 'product creation request with the parameters: [" + productRequest.toString() + "]");
        }

        return integerAtomicReference.get();
    }

    /**
     * Create a task
     *
     * @param previousTaskId null
     * @param taskType       task type
     * @param role           role to assign the task
     * @param productId      product id that raise the task
     * @param description    description of the problem of the taks
     * @param userCode       user code
     */
    public void createTask(final Integer previousTaskId, final String taskType, final String role, final Integer productId,
                           final String description, final String userCode)
    {

        LOG.debug("ProductsAPI: Creating new TO DO task, calling ToDoTask Service API -> /task with - previousTask: [{}] - type [{}] " +
                        " - assigned to role [{}] - productId: [{}] - Description/message: [{}] - and userCode: [{}]",
                previousTaskId, taskType, role, productId, description, userCode);

        this.restHandlerTodotaskapi.createTodoTask(new IRestListenerTodotaskapi()
        {

            @Override
            public void createTodoTask(Integer outcome)
            {

                LOG.debug("ProductsAPI TODO Task Listener: (response from any call) created Todo task succesfully. Code response [{}]", outcome);
            }

            @Override
            public void createTodoTaskErrors(final Errors outcome)
            {

                LOG.error("ProductsAPI TODO Task Listener: (response) todo task service has failed creating Todo task. Exception: [{}] - Error message: [{}]",
                        outcome.getBodyExceptionMessage(), ProductsAPIError.getToDoTaskServiceCallError());
            }
        }, previousTaskId, taskType, role, productId, null, description, null);
    }
}

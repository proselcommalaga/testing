package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * Client for creating broker to do task
 */
@Service
public class BrokerAPIToDoTaskService
{
    /**
     * callback service
     */
    private final NovaContext novaContext;

    /**
     * The error task manager
     */
    private final IErrorTaskManager errorTaskManager;

    @Autowired
    public BrokerAPIToDoTaskService(final IErrorTaskManager errorTaskManager, final NovaContext novaContext)
    {
        this.errorTaskManager = errorTaskManager;
        this.novaContext = novaContext;
    }

    public Integer createBrokerTodoTask(final Broker broker, final Integer brokerNodeId, final NovaException novaException, final ToDoTaskType toDoTaskType, final String methodName, final String messageInfo)
    {
        NovaError novaError = novaException.getNovaError();
        Product product = broker.getProduct();
        String todoTaskDescription = this.errorTaskManager.buildErrorDescriptionContent(Constants.SERVICE_NAME , Constants.BROKER_CORESERVICE_API_NAME, methodName, messageInfo);
        return this.errorTaskManager.createErrorTaskBroker(product != null ? product.getId() : null, novaError, todoTaskDescription, toDoTaskType.name(), novaException, broker.getId(), brokerNodeId, novaContext.getIvUser());
    }

    public Integer createRequestBrokerTask(final String ivUser, final Broker broker, final Integer brokerNodeId, String errorCode, final ToDoTaskType toDoTaskType, final String messageInfo)
    {
        final Product product = broker.getProduct();
        final String taskDescription = MessageFormat.format("El usuario {0} solicita {1} del producto {2}.", ivUser, messageInfo, product.getName());

        return this.errorTaskManager.createRequestTaskBroker(product != null ? product.getId() : null, errorCode, taskDescription, toDoTaskType.name(), broker.getId(), brokerNodeId, novaContext.getIvUser());
    }
}

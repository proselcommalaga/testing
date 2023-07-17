package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.toolsapi.client.feign.nova.rest.IRestHandlerToolsapi;
import com.bbva.enoa.apirestgen.toolsapi.client.feign.nova.rest.IRestListenerToolsapi;
import com.bbva.enoa.apirestgen.toolsapi.client.feign.nova.rest.impl.RestHandlerToolsapi;
import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * The type Tools client.
 */
@Service
public class ToolsClient implements IToolsClient
{

    private static final Logger LOG = LoggerFactory.getLogger(ToolsClient.class);

    /**
     * Library tools rest handler interface
     */
    @Autowired
    private IRestHandlerToolsapi iRestHandlerToolsapi;

    /**
     * Library tools rest handler
     */
    private RestHandlerToolsapi restHandlerToolsapi;

    /**
     * Initialize the rest handler
     */
    @PostConstruct
    public void initRestHandler()
    {
        this.restHandlerToolsapi = new RestHandlerToolsapi(this.iRestHandlerToolsapi);
    }

    /**
     * Add external tools into a product
     *
     * @param productId Product identifier
     */
    @Override
    public void addExternalToolsToProduct(Integer productId)
    {

        SingleApiClientResponseWrapper<NovaException> error = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.addTools(new IRestListenerToolsapi()
        {
            /**
             * Successful call
             */
            @Override
            public void addTools()
            {
                LOG.debug("[Tools API] -> [addTools]: Add tools to product [{}] was successfull", productId);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome       Error
             */
            @Override
            public void addToolsErrors(Errors outcome)
            {
                LOG.error("[Tools API] -> [addTools]: Error addint tools to product [{}]: {}", productId, outcome.getBodyExceptionMessage());

                error.set(new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [addToolsErrors]: Error trying to call 'addTools' for library " + productId));
            }
        }, productId);

        if (error.get() != null)
        {
            throw error.get();
        }
    }

    /**
     * Remove external tools from a product
     *
     * @param productId Product identifier
     */
    @Override
    public void removeExternalToolsFromProduct(Integer productId)
    {
        SingleApiClientResponseWrapper<NovaException> error = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.removeTools(new IRestListenerToolsapi()
        {
            /**
             * Successful call
             */
            @Override
            public void removeTools()
            {
                LOG.debug("[ProductTools API] -> [removeTools]: Remove tools from product [{}] was successfull", productId);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome        Error
             */
            @Override
            public void removeToolsErrors(Errors outcome)
            {
                LOG.error("[ProductTools API] -> [removeTools]: Error removing tools from product [{}] : {}", productId, outcome.getBodyExceptionMessage());

                error.set(new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [removeToolsErrors]: Error trying to call 'removeTools' for product " + productId));

            }

        }, productId);

        if (error.get() != null)
        {
            throw error.get();
        }
    }

    /**
     * Add user into a external tool
     *
     * @param productUserDto Product user dto
     */
    @Override
    public void addUserTool(TOProductUserDTO productUserDto)
    {
        SingleApiClientResponseWrapper<NovaException> error = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.addUserTool(new IRestListenerToolsapi()
        {
            /**
             * Successful call
             */
            @Override
            public void addUserTool()
            {
                LOG.debug("[Tools API] -> [addUserTool]: Add tools to user [{}] in product [{}] was successfull",
                        productUserDto.getUserCode(), productUserDto.getProductId());
            }

            /**
             * Common error call - Errors
             * @param outcome       error
             */
            @Override
            public void addUserToolErrors(Errors outcome)
            {
                LOG.error("[Tools API] -> [addUserTool]: Error adding tools to user [{}] in product [{}] : {}",
                        productUserDto.getUserCode(), productUserDto.getProductId(), outcome.getBodyExceptionMessage());

                error.set(new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [addUserToolErrors]: Error trying to call 'addUserToolErrors' for user "
                                + productUserDto.getUserCode() + " in product " + productUserDto.getProductId()));
            }
        }, productUserDto);

        if (error.get() != null)
        {
            throw error.get();
        }
    }

    /**
     * Delete all user tools
     *
     * @param productUserDto Product user dto
     */
    @Override
    public void removeUserTool(TOProductUserDTO productUserDto, boolean forceDeletion)
    {
        SingleApiClientResponseWrapper<NovaException> error = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.removeUserTool(new IRestListenerToolsapi()
        {

            /**
             * Successful call
             */
            @Override
            public void removeUserTool()
            {
                LOG.debug("[Tools API] -> [removeUserTool]: Remove tools from user [{}] in product [{}] was successfull",
                        productUserDto.getUserCode(), productUserDto.getProductId());
            }

            /**
             * Common error call - Errors
             *
             * @param outcome       error
             */
            @Override
            public void removeUserToolErrors(Errors outcome)
            {
                LOG.error("[Tools API] -> [removeUserTool]: Error removing tools from user [{}] in product [{}]: {}", productUserDto.getUserCode(), productUserDto.getProductId(), outcome.getBodyExceptionMessage());

                error.set(new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [removeUserToolErrors]: Error trying to call 'removeUserToolErrors' for user "
                                + productUserDto.getUserCode() + " in product " + productUserDto.getProductId()));
            }

        }, productUserDto, forceDeletion);

        if (error.get() != null)
        {
            throw error.get();
        }
    }

    /**
     * Call to tools service for get subsystem
     */
    @Override
    public TOSubsystemDTO getSubsystemById(final Integer subsystemId)
    {
        SingleApiClientResponseWrapper<TOSubsystemDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.getSubsystemById(
                new IRestListenerToolsapi()
                {
                    @Override
                    public void getSubsystemById(TOSubsystemDTO outcome)
                    {
                        response.set(outcome);
                    }

                    @Override
                    public void getSubsystemByIdErrors(Errors outcome)
                    {
                        LOG.error("[Tools API] -> [getSubsystemById]: Error getting subystem [{}] : {} ", subsystemId, outcome.getBodyExceptionMessage());

                        throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                                "[Tools API] -> [getSubsystemById]: Error trying to call 'getSubsystemById' for subsystem "
                                        + subsystemId);
                    }
                }, subsystemId);

        return response.get();
    }

    /**
     * Call to tools service for get subsystem
     */
    @Override
    public TOSubsystemDTO getSubsystemByRepositoryId(final Integer repositoryId)
    {
        SingleApiClientResponseWrapper<TOSubsystemDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.getSubsystemByRepositoryId(new IRestListenerToolsapi()
        {
            @Override
            public void getSubsystemByRepositoryId(TOSubsystemDTO outcome)
            {
                LOG.debug("[Tools API] -> [getSubsystemByRepositoryId]: the TOsubsystemDTO found by repository id: [{}]", outcome);
                response.set(outcome);
            }

            @Override
            public void getSubsystemByRepositoryIdErrors(Errors outcome)
            {
                LOG.error("[Tools API] -> [getSubsystemByRepositoryId]: Error getting subsystem by repository id: [{}] : {}", repositoryId, outcome.getBodyExceptionMessage());

                throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [getSubsystemByRepositoryId]: Error trying to call 'getSubsystemByRepositoryId' for repository "
                                + repositoryId);
            }
        }, repositoryId);

        return response.get();
    }

    /**
     * Call to tools service for get subsystem
     */
    @Override
    public TOSubsystemDTO getSubsystemByProductAndName(final String subsystemName, final Integer productId)
    {
        SingleApiClientResponseWrapper<TOSubsystemDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.getSubsystemByNameAndProduct(new IRestListenerToolsapi()
        {
            @Override
            public void getSubsystemByNameAndProduct(TOSubsystemDTO outcome)
            {
                LOG.debug("[getSubsystemByProductAndName] subsystem found: [{}] for subsystem name: [{}] and productId: [{}]", outcome, subsystemName, productId);
                response.set(outcome);
            }

            @Override
            public void getSubsystemByNameAndProductErrors(Errors outcome)
            {
                LOG.error("[getSubsystemByProductAndName]: Error calling toolService for subsystem name: [{}] and productId: [{}]: Error: {}", subsystemName, productId, outcome.getBodyExceptionMessage());

                throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [getSubsystemByNameAndProduct]: Error trying to call 'getSubsystemByNameAndProduct' filter by name "
                                + subsystemName + "and productId: " + productId);
            }
        }, productId, subsystemName);

        return response.get();
    }

    /**
     * Call to tools service for get product subsystem
     */
    @Override
    public List<TOSubsystemDTO> getProductSubsystems(final Integer productId, final Boolean isBehaviorType)
    {
        SingleApiClientResponseWrapper<TOSubsystemDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerToolsapi.getSubsystems(new IRestListenerToolsapi()
        {
            @Override
            public void getSubsystems(TOSubsystemDTO[] outcome)
            {
                response.set(outcome);
            }

            @Override
            public void getSubsystemsErrors(Errors outcome)
            {
                LOG.error("Tools API] -> [getSubsystems]: Error getting subsystems by productId [{}] :  {}", productId, outcome.getBodyExceptionMessage());

                throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome,
                        "[Tools API] -> [getSubsystems]: Error trying to call 'getSubsystems' for product"
                                + productId);
            }
        }, productId, isBehaviorType);

        return Arrays.asList(response.get());
    }

    @Override
    public List<TOSubsystemDTO> getAllSubsystems()
    {
        SingleApiClientResponseWrapper<TOSubsystemDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[ToolsClient] -> [getAllSubsystems]: getting all Subsystems");
        this.restHandlerToolsapi.getAllSubsystems(new IRestListenerToolsapi()
        {
            @Override
            public void getAllSubsystems(TOSubsystemDTO[] outcome)
            {
                LOG.debug("[ToolsClient] -> [getAllSubsystems]: successfully got all Subsystems");
                response.set(outcome);
            }

            @Override
            public void getAllSubsystemsErrors(Errors outcome)
            {
                LOG.error("[ToolsClient] -> [getAllSubsystems]: Error trying to get all Subsystems: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome, "[ToolsClient] -> [getAllSubsystems]: Error trying to get all Subsystems");
            }
        });

        return Arrays.asList(response.get());
    }

    @Override
    public TOSubsystemsCombinationDTO[] getSubsystemsHistorySnapshot()
    {
        SingleApiClientResponseWrapper<TOSubsystemsCombinationDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.info("[ToolsClient] -> [getSubsystemsHistorySnapshot]: getting subsystems snapshot for statistic history loading.");
        this.restHandlerToolsapi.getSubsystemsHistorySnapshot(new IRestListenerToolsapi()
        {
            @Override
            public void getSubsystemsHistorySnapshot(TOSubsystemsCombinationDTO[] outcome)
            {
                LOG.info("[ToolsClient] -> [getSubsystemsHistorySnapshot]: successfully got subsystems snapshot for statistic history loading.");
                response.set(outcome);
            }

            @Override
            public void getSubsystemsHistorySnapshotErrors(Errors outcome)
            {
                LOG.error("[ToolsClient] -> [getSubsystemsHistorySnapshot]: Error trying to get subsystems snapshot for statistic history loading.: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(getNovaErrorFromClientErrors(outcome), outcome, "[ToolsClient] -> [getSubsystemsHistorySnapshot]: Error trying to get subsystems snapshot for statistic history loading.");
            }
        });

        return response.get();
    }

    private NovaError getNovaErrorFromClientErrors(Errors outcome)
    {
        Exception exception = new Exception(outcome.getFirstErrorMessage().orElse(new ErrorMessage(ProductsAPIError.UNEXPECTED_ERROR_MSG)).toString());

        JsonElement jsonElement = JsonParser.parseString(exception.getMessage());

        String code = this.getStringFromJsonElement(jsonElement, "code", ProductsAPIError.UNEXPECTED_ERROR_CODE);
        String message = this.getStringFromJsonElement(jsonElement, "message", ProductsAPIError.UNEXPECTED_ERROR_MSG);
        String type = this.getStringFromJsonElement(jsonElement, "type", ErrorMessageType.CRITICAL.toString());


        return new NovaError("ProductsError",
                code,
                "Error in invocation to ProductTools Api",
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.valueOf(type));

    }

    private String getStringFromJsonElement(JsonElement element, String param, String defaultValue)
    {
        if (element != null && element.getAsJsonObject() != null && element.getAsJsonObject().get(param) != null)
        {
            return element.getAsJsonObject().get(param).getAsString();
        }
        else
        {
            return defaultValue;
        }
    }
}

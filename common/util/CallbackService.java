package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMCallbackDTO;
import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMResponseDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackDto;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackInfoDto;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherCallbackDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherResponseDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Callback service
 *
 * @author XE56809
 */
@Service
@Transactional
public class CallbackService
{
    /**
     * Empty string
     */
    private static final String EMPTY_STRING = "";
    /**
     * Content type for callbacks
     */
    private static final String CONTENT_TYPE = "";
    /**
     * Http method for callbacks
     */
    private static final String CALLBACK_HTTP_METHOD_DEFAULT = "POST";

    /**
     * basic deploymentmanager callback api, by version
     */
    public static final String DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH = "/deploymentmanagercallbackapi/v9";

    //Callback endpoint for services
    public static final String CALLBACK_SERVICE = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/services/";
    //Callback endpoint for instances
    public static final String CALLBACK_INSTANCE = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/instances/";
    //Callback endpoint for subsystems
    public static final String CALLBACK_SUBSYSTEM = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/subsystems/";
    //Callback endpoint for plans
    public static final String CALLBACK_PLAN = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/plans/";
    // error endpoint
    public static final String ERROR = "error";

    //Start endpoint
    public static final String START = "/start";
    //Start error endpoint
    public static final String START_ERROR = "/start/error";
    //Stop endpoint
    public static final String STOP = "/stop";
    //Stop error endpoint
    public static final String STOP_ERROR = "/stop/error";
    //Deploy endpoint
    public static final String DEPLOY = "/deploy";
    //Deploy error endpoint
    public static final String DEPLOY_ERROR = "/deploy/error";

    //Callback endpoint for DELETE services
    public static final String CALLBACK_DELETE_SERVICE = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/delete/services/";
    //Callback endpoint for DELETE instances
    public static final String CALLBACK_DELETE_INSTANCE = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/delete/instances/";
    //Callback endpoint for DELETE subsystems
    public static final String CALLBACK_DELETE_SUBSYSTEM = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/delete/subsystems/";
    //Callback endpoint for DELETE plans
    public static final String CALLBACK_DELETE_PLAN = DEPLOYMENT_MANAGER_CALLBACK_BASE_PATH + "/delete/plans/";

    //Remove error endpoint
    public static final String REMOVE_ERROR = "/remove/error";
    //Replace endpoint
    public static final String REPLACE = "/replace/";
    //Replace error endpoint
    public static final String REPLACE_ERROR = "/error";
    //Promote endpoint
    public static final String PROMOTE = "/promote";
    //Promote error endpoint
    public static final String PROMOTE_ERROR = "/promote/error";
    //Start endpoint
    public static final String RESTART = "/restart";
    //Start error endpoint
    public static final String RESTART_ERROR = "/restart/error";
    /**
     * Application name
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Build callback
     *
     * @param successUrl success relative path
     * @param errorUrl   error relative path
     * @return callback info dto
     */
    public CallbackInfoDto buildCallback(final String successUrl, final String errorUrl)
    {
        CallbackInfoDto callbackInfoDto = new CallbackInfoDto();
        callbackInfoDto.setSuccessCallback(this.buildCallbackDto(successUrl));
        callbackInfoDto.setErrorCallback(this.buildCallbackDto(errorUrl));
        return callbackInfoDto;
    }

    /**
     * Build callback
     *
     * @param successUrl success relative path
     * @param errorUrl   error relative path
     * @return callback info dto
     */
    public BMResponseDTO buildBehaviorCallback(final String successUrl, final String errorUrl)
    {
        BMResponseDTO bmCallbackDTO = new BMResponseDTO();
        bmCallbackDTO.setSuccess(this.buildBehaviorCallbackDto(successUrl));
        bmCallbackDTO.setError(this.buildBehaviorCallbackDto(errorUrl));
        return bmCallbackDTO;
    }

    /**
     * Build callback for on cloud environment
     *
     * @param successUrl success relative path
     * @param errorUrl   error relative path
     * @return callback info dto
     */
    public EtherResponseDTO buildEtherCallback(final String successUrl, final String errorUrl)
    {
        EtherResponseDTO etherResponseDTO = new EtherResponseDTO();

        etherResponseDTO.setSuccess(this.buildEtherCallbackDto(successUrl));
        etherResponseDTO.setError(this.buildEtherCallbackDto(errorUrl));

        return etherResponseDTO;
    }

    /**
     * Build callback dto
     *
     * @param url url
     * @return callback dto
     */
    private CallbackDto buildCallbackDto(final String url)
    {
        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setBalanced(true);
        callbackDto.setContentType(CONTENT_TYPE);
        callbackDto.setMethod(CALLBACK_HTTP_METHOD_DEFAULT);
        callbackDto.setHost(applicationName.toUpperCase());
        callbackDto.setUrl(url);
        callbackDto.setUserCode(EMPTY_STRING);
        return callbackDto;
    }

    /**
     * Build callback dto
     *
     * @param url url
     * @return callback dto
     */
    private BMCallbackDTO buildBehaviorCallbackDto(final String url)
    {
        BMCallbackDTO bmCallbackDTO = new BMCallbackDTO();

        BeanUtils.copyProperties(this.buildCallbackDto(url), bmCallbackDTO);

        return bmCallbackDTO;
    }

    /**
     * Build callback dto
     *
     * @param url url
     * @return callback dto
     */
    private EtherCallbackDTO buildEtherCallbackDto(final String url)
    {
        EtherCallbackDTO etherCallbackDTO = new EtherCallbackDTO();

        BeanUtils.copyProperties(this.buildCallbackDto(url), etherCallbackDTO);

        return etherCallbackDTO;
    }

}

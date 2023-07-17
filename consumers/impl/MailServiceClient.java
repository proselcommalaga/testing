package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.mailserviceapi.client.feign.nova.rest.IRestHandlerMailserviceapi;
import com.bbva.enoa.apirestgen.mailserviceapi.client.feign.nova.rest.IRestListenerMailserviceapi;
import com.bbva.enoa.apirestgen.mailserviceapi.client.feign.nova.rest.impl.RestHandlerMailserviceapi;
import com.bbva.enoa.apirestgen.mailserviceapi.model.MailInstance;
import com.bbva.enoa.apirestgen.mailserviceapi.model.NotificationParameter;
import com.bbva.enoa.apirestgen.mailserviceapi.model.NotificationParametersArray;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.model.param.MailNotificationParams;
import com.bbva.enoa.platformservices.coreservice.common.util.MailServiceConstants;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mail service client.
 */
@Service
@Slf4j
public class MailServiceClient
{
    /**
     * Rest Handler.
     */
    @Autowired
    private IRestHandlerMailserviceapi iRestHandlerMailserviceapi;
    /**
     * Handler.
     */
    private RestHandlerMailserviceapi restHandlerMailserviceapi;

    /**
     * Constructor. Init the listeners.
     */
    @PostConstruct
    public void init()
    {
        // Task service client handler and listener.
        this.restHandlerMailserviceapi = new RestHandlerMailserviceapi(this.iRestHandlerMailserviceapi);
    }

    /**
     * Sent delete product notification
     *
     * @param productName       product name
     * @param mailAddressesList mail addresses list
     */
    public void sendDeleteProductNotification(final String productName, final List<String> mailAddressesList)
    {
        Map<String, String> notificationParameterMap = new HashMap<>();
        notificationParameterMap.put(Constants.PRODUCT_NAME, productName);
        notificationParameterMap.put(Constants.MAIL_ADDRESSES, this.generateMailAddressesStringList(mailAddressesList));

        this.sendMailNotification(this.getNotificationParametersArray(notificationParameterMap),
                Constants.DELETE_PRODUCT_NOTIFICATION_CODE);
    }

    /**
     * Sent filesystem task notification
     *
     * @param params Necessary parameters for notification mail to be sent.
     */
    public void sendDownloadToPreNotification(final MailNotificationParams params)
    {
        Map<String, String> notificationParameterMap = new HashMap<>();
        notificationParameterMap.put(MailServiceConstants.PRODUCT_NAME, params.getProductName());
        notificationParameterMap.put(MailServiceConstants.PRODUCT_ID, params.getProductId().toString());
        notificationParameterMap.put(MailServiceConstants.ENVIRONMENT, params.getEnvironment());
        notificationParameterMap.put(MailServiceConstants.IV_USER, params.getIvUser());
        notificationParameterMap.put(MailServiceConstants.MAIL_ADDRESS, params.getUserMailAddress());
        notificationParameterMap.put(MailServiceConstants.FILE_PATH, params.getFilesystemPath());
        notificationParameterMap.put(MailServiceConstants.FILE_NAME, params.getFilename());
        notificationParameterMap.put(MailServiceConstants.FILESYSTEM_NAME, params.getFilesystemName());

        this.sendMailNotification(this.getNotificationParametersArray(notificationParameterMap), Constants.DOWNLOAD_FILES_TO_PRE_REQUEST_INFO_CODE);
    }

    /**
     * Sent delete subsystem notification
     *
     * @param productName       product name
     * @param subsystemName     subsystem name
     * @param mailAddressesList mail addresses list
     */
    public void sentDeleteSubsystemNotification(final String productName, final String subsystemName,
                                                final List<String> mailAddressesList)
    {
        Map<String, String> notificationParameterMap = new HashMap<>();
        notificationParameterMap.put(MailServiceConstants.PRODUCT_NAME, productName);
        notificationParameterMap.put(MailServiceConstants.SUBSYSTEM_NAME, subsystemName);
        notificationParameterMap.put(MailServiceConstants.MAIL_ADDRESSES, this.generateMailAddressesStringList(mailAddressesList));

        this.sendMailNotification(getNotificationParametersArray(notificationParameterMap),
                MailServiceConstants.SUBSYSTEM_DELETED_NOTIFICATION_CODE);
    }

    /**
     * Sent User deleted notification info notification
     *
     * @param productName product name
     * @param userCode    user code
     * @param userMail    user mail
     */
    public void sendUserDeletedNotification(final String productName, final String userCode, final String userMail)
    {
        // Success, so send mail notification
        Map<String, String> notificationParameterMap = new HashMap<>();
        notificationParameterMap.put(MailServiceConstants.PRODUCT_NAME, productName);
        notificationParameterMap.put(MailServiceConstants.USER_CODE, userCode);
        notificationParameterMap.put(MailServiceConstants.MAIL_ADDRESS, userMail);

        this.sendMailNotification(getNotificationParametersArray(notificationParameterMap),
                MailServiceConstants.USER_DELETED_NOTIFICATION_CODE);
    }

    /**
     * Sent User tools info notification
     *
     * @param productName product name
     * @param mailAddress mailAddress
     * @param uuaa        uuaa
     * @param userCode    userCode
     * @param password    temporal password generated
     * @param productId   product id
     * @param role        user role in the product
     */
    public void sendUserToolsInfoNotification(final String productName, final String mailAddress, final String uuaa,
                                              final String userCode, final String password, final Integer productId,
                                              final String role)
    {
        // Success, so send mail notification
        Map<String, String> notificationParameterMap = new HashMap<>();
        notificationParameterMap.put(MailServiceConstants.PRODUCT_NAME, productName);
        notificationParameterMap.put(MailServiceConstants.MAIL_ADDRESS, mailAddress);
        notificationParameterMap.put(MailServiceConstants.UUAA, uuaa);
        notificationParameterMap.put(MailServiceConstants.USER_CODE, userCode);
        notificationParameterMap.put(MailServiceConstants.PASSWRD, password);
        notificationParameterMap.put(MailServiceConstants.PRODUCT_ID, productId.toString());
        notificationParameterMap.put(MailServiceConstants.USER_ROLE, role);

        this.sendMailNotification(getNotificationParametersArray(notificationParameterMap),
                MailServiceConstants.USER_TOOLS_INFO_NOTIFICATION_CODE);
    }


    /**
     * Send plan manager resolved notification
     *
     * @param productName product name
     * @param todoTaskId  to do task id
     * @param changedBy   person who change the to do task status
     * @param productId   product id
     * @param planId      plan id
     * @param planAction  plan action (DEPLOY or UNDEPLOY)
     * @param environment environment
     */
    public void sendPlanManagerResolveNotification(final String productName, final Integer todoTaskId, final String environment,
                                                   final String changedBy, final Integer productId, final Integer planId,
                                                   final String planAction)
    {

        Map<String, String> notificationParameterMap = new HashMap<>();

        notificationParameterMap.put(MailServiceConstants.PRODUCT_NAME, productName);
        notificationParameterMap.put(MailServiceConstants.TODO_TASK_ID, todoTaskId.toString());
        notificationParameterMap.put(MailServiceConstants.CHANGED_BY, changedBy);
        notificationParameterMap.put(MailServiceConstants.PRODUCT_ID, productId.toString());
        notificationParameterMap.put(MailServiceConstants.PLAN_ID, planId.toString());
        notificationParameterMap.put(MailServiceConstants.PLAN_ACTION, planAction);
        notificationParameterMap.put(MailServiceConstants.ENVIRONMENT, environment);

        this.sendMailNotification(getNotificationParametersArray(notificationParameterMap),
                MailServiceConstants.PLAN_MANAGER_RESPONSE_NOTIFICATION_CODE);
    }

    /**
     * Get full name of the portal user with the first letter in capital letters
     *
     * @param portalUser portal user to get the name, surname1 and surname2
     * @return the full name of the portal user
     */
    public String getFullName(final USUserDTO portalUser)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (portalUser == null)
        {
            stringBuilder.append("Unknown user");
        }
        else
        {
            stringBuilder.append(portalUser.getUserName().substring(0, 1).toUpperCase());
            stringBuilder.append(portalUser.getUserName().substring(1));
            stringBuilder.append(" ");

            if (portalUser.getSurname1() != null && !portalUser.getSurname1().isEmpty())
            {
                stringBuilder.append(portalUser.getSurname1().substring(0, 1).toUpperCase());
                stringBuilder.append(portalUser.getSurname1().substring(1));
                stringBuilder.append(" ");
            }

            if (portalUser.getSurname2() != null && !portalUser.getSurname2().isEmpty())
            {
                stringBuilder.append(portalUser.getSurname2().substring(0, 1).toUpperCase());
                stringBuilder.append(portalUser.getSurname2().substring(1));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Send admin mail
     *
     * @param mailInstance the mail instance with the content
     */
    public void sendAdminMail(final MailInstance mailInstance)
    {
        this.restHandlerMailserviceapi.sendAdminMail(new IRestListenerMailserviceapi()
        {
            @Override
            public void sendAdminMail()
            {
                log.debug("[MailServiceClient] -> [sendAdminMail] mail sent: [{}]", mailInstance.getSubject());
            }

            @Override
            public void sendAdminMailErrors(Errors outcome)
            {
                log.error("[MailServiceClient] -> [sendAdminMail]: there was an error trying to send a mail instance: [{}]. Error: [{}]", mailInstance, outcome.getBodyExceptionMessage());
            }
        }, mailInstance);
    }

    ////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * Send a notification via email.
     *
     * @param notificationParametersArray notification parameters
     * @param notificationCode            notification code
     */
    private void sendMailNotification(final NotificationParametersArray notificationParametersArray, final Integer notificationCode)
    {
        log.debug("[MailServiceClient] -> [sendMailNotification]: sending notification code: [{}] with the following parameter size: [{}]", notificationCode, notificationParametersArray.getItems().length);

        this.restHandlerMailserviceapi.sendNotification(new IRestListenerMailserviceapi()
        {
            @Override
            public void sendNotification()
            {
                log.debug("[MailServiceClient] -> [sendMailNotification]: the notification code: [{}] with parameters size: [{}] has been sent successfully.", notificationCode, notificationParametersArray.getItems().length);
            }

            @Override
            public void sendNotificationErrors(Errors outcome)
            {
                log.error("[MailServiceClient] -> [sendMailNotification]: the notification code [{}] with parameters: [{}] has failed - IT HAS BEEN NOT SENT. Error Message: [{}]",
                        notificationCode, notificationParametersArray, outcome.getBodyExceptionMessage());

            }
        }, notificationParametersArray, notificationCode);
    }

    /**
     * Method to create the notification parameters array to send notification
     *
     * @param notificationMap map with the notification parameters
     * @return a Notification Parameters Array instance filled with the notification parameters
     */
    private NotificationParametersArray getNotificationParametersArray(final Map<String, String> notificationMap)
    {
        // Create the NotificationParametesArray instance to return and the notification parameters list
        NotificationParametersArray notificationParametersArray = new NotificationParametersArray();
        List<NotificationParameter> notificationParameterList = new ArrayList<>();

        // Get each entry of the map and added to the notification parameters array (fill the notification parameter list)
        for (Map.Entry<String, String> entries : notificationMap.entrySet())
        {
            NotificationParameter notificationParameter = new NotificationParameter();
            notificationParameter.setKey(entries.getKey());
            notificationParameter.setValue(entries.getValue());

            notificationParameterList.add(notificationParameter);
        }

        // Create the notification paramters array
        NotificationParameter[] notifParamsArray = notificationParameterList.toArray(new NotificationParameter[notificationParameterList.size()]);

        // Add the notification parameters arrays with the notification parameter list
        notificationParametersArray.setItems(notifParamsArray);

        return notificationParametersArray;
    }

    /**
     * Generate the mal addresses string list
     * Build comma separated mail addresses list with the format: "adsd@asda.com,gjhds@asd.com,"
     *
     * @param mailAddressesList mail addresses list
     * @return mail addresses list in sting format
     */
    private String generateMailAddressesStringList(List<String> mailAddressesList)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (String mailAddress : mailAddressesList)
        {
            stringBuilder.append(mailAddress);
            stringBuilder.append(",");
        }

        return stringBuilder.toString();
    }

}

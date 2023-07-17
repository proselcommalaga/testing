package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.mailserviceapi.client.feign.nova.rest.IRestHandlerMailserviceapi;
import com.bbva.enoa.apirestgen.mailserviceapi.model.MailInstance;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.platformservices.coreservice.common.model.param.MailNotificationParams;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by xe56809 on 14/03/2018.
 */
@ExtendWith(MockitoExtension.class)
public class MailServiceClientTest
{
    private final MailNotificationParams params = (new MailNotificationParams.MailNotificationParamsBuilder()).productName("Product")
            .productId(1).userMailAddress("").ivUser("").filename("").filesystemName("").filesystemPath("").environment("")
            .build();
    @Mock
    private IRestHandlerMailserviceapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private MailServiceClient client;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.client.init();

        Field field = client.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_send_delete_product_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendDeleteProductNotification("Product", List.of("A", "B"));

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_delete_product_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sendDeleteProductNotification("Product", List.of("A", "B"));

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_send_download_to_pre_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendDownloadToPreNotification(params);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_download_to_pre_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sendDownloadToPreNotification(params);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_send_delete_subsystem_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sentDeleteSubsystemNotification("Product", "", List.of("A"));

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_delete_subsystem_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sentDeleteSubsystemNotification("Product", "", List.of("A"));

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_send_user_deleted_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendUserDeletedNotification("Product", "", "A");

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_user_deleted_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sendUserDeletedNotification("Product", "", "A");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    public void when_send_user_tools_info_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendUserToolsInfoNotification("Product", "", "A", "B", "C", 1, "B");

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_user_tools_info_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sendUserToolsInfoNotification("Product", "", "A", "B", "C", 1, "B");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    public void when_send_plan_manager_resolve_notification_returns_ko_response_then_log_error() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendPlanManagerResolveNotification("Product", 1, "A", "B", 1, 1, "B");

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_send_plan_manager_resolve_notification_returns_ok_response_then_log_debug() throws Exception
    {
        Mockito.when(restHandler.sendNotification(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.sendPlanManagerResolveNotification("Product", 1, "A", "B", 1, 1, "B");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_get_full_name_is_portal_user_null_then_return_unknown_user()
    {
        Assertions.assertEquals("Unknown user", client.getFullName(null));
    }

    @Test
    public void when_get_full_name_is_portal_user_with_null_first_surname_then_return_result_without_first_surname()
    {
        USUserDTO user = DummyConsumerDataGenerator.getDummyUserDto();
        user.setSurname1(null);
        String result = client.getFullName(user);
        Assertions.assertEquals("UserName Surname2", result);
    }

    @Test
    public void when_get_full_name_is_portal_user_with_empty_first_surname_then_return_result_without_first_surname()
    {
        USUserDTO user = DummyConsumerDataGenerator.getDummyUserDto();
        user.setSurname1("");
        String result = client.getFullName(user);
        Assertions.assertEquals("UserName Surname2", result);
    }

    @Test
    public void when_get_full_name_is_portal_user_with_null_second_surname_then_return_result_without_second_surname()
    {
        USUserDTO user = DummyConsumerDataGenerator.getDummyUserDto();
        user.setSurname2(null);
        String result = client.getFullName(user);
        Assertions.assertEquals("UserName Surname ", result);
    }

    @Test
    public void when_get_full_name_is_portal_user_with_both_surnames_then_return_result_with_both_surnames()
    {
        String result = client.getFullName(DummyConsumerDataGenerator.getDummyUserDto());
        Assertions.assertEquals("UserName Surname Surname2", result);
    }

    @Test
    public void when_get_full_name_is_portal_user_with_empty_second_surname_then_return_result_without_second_surname()
    {
        USUserDTO user = DummyConsumerDataGenerator.getDummyUserDto();
        user.setSurname2("");
        String result = client.getFullName(user);
        Assertions.assertEquals("UserName Surname ", result);
    }

    @Test
    public void when_send_admin_mail_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.sendAdminMail(Mockito.any(MailInstance.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.sendAdminMail(new MailInstance());

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(MailInstance.class), Mockito.any());
    }

    @Test
    public void when_send_admin_mail_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.sendAdminMail(Mockito.any(MailInstance.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        MailInstance mailInstance = new MailInstance();
        mailInstance.setSubject("Subject");
        client.sendAdminMail(mailInstance);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString());
    }
}
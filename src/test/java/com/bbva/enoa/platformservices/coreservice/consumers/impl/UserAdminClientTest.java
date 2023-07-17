package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.usersadminapi.client.feign.nova.rest.IRestHandlerUsersadminapi;
import com.bbva.enoa.apirestgen.usersadminapi.model.UATeamUser;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserAdminClientTest
{
    @Mock
    private IRestHandlerUsersadminapi restHandler;
    @InjectMocks
    private UserAdminClient client;


    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(UserAdminClient.class);
        client.init();
    }

    @Test
    public void when_get_team_members_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getTeamMembers(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(Errors.class, () -> client.getTeamMembers("A"));
    }

    @Test
    public void when_get_team_members_returns_ok_response_then_return_result() throws Errors
    {
        UATeamUser[] users = DummyConsumerDataGenerator.getDummyUATeamUsers();
        Mockito.when(restHandler.getTeamMembers(Mockito.anyString())).thenReturn(new ResponseEntity<>(users, HttpStatus.OK));

        List<UATeamUser> result = client.getTeamMembers("A");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(users[0], result.get(0));
    }

}
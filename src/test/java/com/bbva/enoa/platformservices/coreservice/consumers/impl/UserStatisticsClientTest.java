package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.IRestHandlerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.TeamCountDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
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
class UserStatisticsClientTest
{
    @Mock
    private IRestHandlerStatisticsuserapi restHandler;
    @InjectMocks
    private UserStatisticsClient client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(UserStatisticsClient.class);
        client.initRestHandler();
    }

    @Test
    public void when_count_users_in_teams_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.countUsersInTeams()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.countUsersInTeams());
    }

    @Test
    public void when_count_users_in_teams_returns_ok_response_then_return_result()
    {
        TeamCountDTO[] dtos = DummyConsumerDataGenerator.getDummyTeamCountDtos();
        Mockito.when(restHandler.countUsersInTeams()).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        List<TeamCountDTO> result = client.countUsersInTeams();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dtos[0], result.get(0));
    }

    @Test
    public void when_get_user_product_role_history_snapshot_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getUserProductRoleHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getUserProductRoleHistorySnapshot());
    }

    @Test
    public void when_get_user_product_role_history_snapshot_returns_ok_response_then_return_result()
    {
        UserProductRoleHistoryDTO[] dtos = DummyConsumerDataGenerator.getDummyUserProductRoleHistoryDtos();
        Mockito.when(restHandler.getUserProductRoleHistorySnapshot()).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        UserProductRoleHistoryDTO[] result = client.getUserProductRoleHistorySnapshot();

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }
}
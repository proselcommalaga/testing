package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.IRestHandlerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.IRestListenerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.impl.RestHandlerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.TeamCountDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IUserStatisticsClient;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class UserStatisticsClient implements IUserStatisticsClient
{

    private static final Logger LOG = LoggerFactory.getLogger(UserStatisticsClient.class);

    /**
     * User statistics rest handler interface
     */
    @Autowired
    private IRestHandlerStatisticsuserapi iRestHandlerStatisticsuserapi;

    /**
     * User statistics rest handler
     */
    private RestHandlerStatisticsuserapi restHandlerStatisticsuserapi;

    /**
     * Initialize the rest handler
     */
    @PostConstruct
    public void initRestHandler()
    {
        this.restHandlerStatisticsuserapi = new RestHandlerStatisticsuserapi(this.iRestHandlerStatisticsuserapi);
    }

    @Override
    public List<TeamCountDTO> countUsersInTeams()
    {
        SingleApiClientResponseWrapper<TeamCountDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[UserStatisticsClient] -> [countUsersInTeams]: counting users in teams");
        this.restHandlerStatisticsuserapi.countUsersInTeams(new IRestListenerStatisticsuserapi()
        {
            @Override
            public void countUsersInTeams(TeamCountDTO[] outcome)
            {
                LOG.debug("[UserStatisticsClient] -> [countUsersInTeams]: successfully count users in teams");
                response.set(outcome);
            }

            @Override
            public void countUsersInTeamsErrors(Errors outcome)
            {
                LOG.error("[UserStatisticsClient] -> [countUsersInTeams]: Error trying to count users in teams: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUserServiceError(), outcome, "[UserStatisticsClient] -> [countUsersInTeams]: Error trying to count users in teams");
            }
        });

        return Arrays.asList(response.get());
    }

    @Override
    public UserProductRoleHistoryDTO[] getUserProductRoleHistorySnapshot()
    {
        SingleApiClientResponseWrapper<UserProductRoleHistoryDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.info("[UserStatisticsClient] -> [getUserProductRoleHistorySnapshot]: getting users by product and roles for statistic history loading");
        this.restHandlerStatisticsuserapi.getUserProductRoleHistorySnapshot(new IRestListenerStatisticsuserapi()
        {
            @Override
            public void getUserProductRoleHistorySnapshot(UserProductRoleHistoryDTO[] outcome)
            {
                LOG.info("[UserStatisticsClient] -> [getUserProductRoleHistorySnapshot]: successfully got users by product and roles for statistic history loading");
                response.set(outcome);
            }

            @Override
            public void getUserProductRoleHistorySnapshotErrors(Errors outcome)
            {
                LOG.error("[UserStatisticsClient] -> [getUserProductRoleHistorySnapshot]: Error trying to get users by product and roles for statistic history loading: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUserServiceError(), outcome, "[UserStatisticsClient] -> [getUserProductRoleHistorySnapshot]: Error trying to get users by product and roles for statistic history loading");
            }
        });

        return response.get();
    }

    @Override
    public UserProductDTO[] getProductUsers()
    {
        SingleApiClientResponseWrapper<UserProductDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.info("[UserStatisticsClient] -> [getProductUsers]: getting all users  of products");
        this.restHandlerStatisticsuserapi.getProductUsers(new IRestListenerStatisticsuserapi()
        {
            @Override
            public void getProductUsers(UserProductDTO[] outcome)
            {
                LOG.info("[UserStatisticsClient] -> [getProductUsers]: successfully got all users of products");
                response.set(outcome);
            }

            @Override
            public void getProductUsersErrors(Errors outcome)
            {
                LOG.error("[UserStatisticsClient] -> [getProductUsers]: Error trying to get all users of products: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUserServiceError(), outcome, "[UserStatisticsClient] -> [getProductUsers]: Error trying to get all users of products");
            }
        });

        return response.get();
    }
}

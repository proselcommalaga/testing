package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.statisticsuserapi.model.TeamCountDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;

import java.util.List;

public interface IUserStatisticsClient
{
    /**
     * Get how many users (members of at least one product) are in each team (empty teams are omitted).
     *
     * @return How many users (members of at least one product) are in each team (empty teams are omitted).
     */
    List<TeamCountDTO> countUsersInTeams();

    /**
     * Gets an array of DTOs having necessary information for saving product role information in statistic history loading.
     *
     * @return an array of DTOs containing aggregated info for product role com.bbva.enoa.platformservices.historicalloaderservice.step in statistic history loading job.
     */
    UserProductRoleHistoryDTO[] getUserProductRoleHistorySnapshot();

    /**
     *
     */
    UserProductDTO[] getProductUsers();
}

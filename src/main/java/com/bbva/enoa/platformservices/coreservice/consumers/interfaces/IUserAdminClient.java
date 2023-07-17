package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.usersadminapi.model.UATeamUser;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;

import java.util.List;


/**
 * The interface User admin client.
 */
public interface IUserAdminClient {

    /**
     * Gets team members.
     *
     * @param teamCode the team code
     * @return the team members
     * @throws Errors the errors
     */
    List<UATeamUser> getTeamMembers(final String teamCode) throws Errors;

}

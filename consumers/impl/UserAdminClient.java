package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.usersadminapi.client.feign.nova.rest.IRestHandlerUsersadminapi;
import com.bbva.enoa.apirestgen.usersadminapi.client.feign.nova.rest.IRestListenerUsersadminapi;
import com.bbva.enoa.apirestgen.usersadminapi.client.feign.nova.rest.impl.RestHandlerUsersadminapi;
import com.bbva.enoa.apirestgen.usersadminapi.model.UATeamUser;
import com.bbva.enoa.core.novabootstarter.consumers.ApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IUserAdminClient;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * User Admin client
 */
@Service
public class UserAdminClient implements IUserAdminClient
{
    private static final Logger LOG = LoggerFactory.getLogger(UserAdminClient.class);
    /**
     * Rest interfaces
     */
    @Autowired
    private IRestHandlerUsersadminapi restInterface;
    /**
     * API services.
     */
    private RestHandlerUsersadminapi restHandler;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerUsersadminapi(this.restInterface);
    }

    @Override
    public List<UATeamUser> getTeamMembers(String teamCode) throws Errors
    {
        ApiClientResponseWrapper<UATeamUser[], Errors> response = new ApiClientResponseWrapper<>();

        this.restHandler.getTeamMembers(new IRestListenerUsersadminapi()
        {

            @Override
            public void getTeamMembers(UATeamUser[] outcome)
            {
                response.setValue(outcome);
                LOG.debug("[User Admin API] -> [getTeamMembers]: Obtained team members [{}] for teamCode [{}] successfully",
                        Arrays.toString(outcome), teamCode);
            }

            @Override
            public void getTeamMembersErrors(Errors outcome)
            {
                LOG.error("[User Admin API] -> [getTeamMembers]: Error obtaining team members for teamCode [{}]: {} ",
                        teamCode, outcome.getBodyExceptionMessage());
                response.setError(outcome);

            }
        }, teamCode);

        // Check if exception should be throw
        if (response.getError() != null)
        {
            throw response.getError();
        }

        return Arrays.asList(response.getValue());
    }

}

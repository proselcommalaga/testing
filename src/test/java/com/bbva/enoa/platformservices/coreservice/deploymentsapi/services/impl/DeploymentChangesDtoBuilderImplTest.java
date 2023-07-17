package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DeploymentChangesDtoBuilderImplTest
{
    @Mock
    private IProductUsersClient userClient;
    @InjectMocks
    private DeploymentChangesDtoBuilderImpl builder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void build() throws Exception
    {
        ConfigurationRevision revision = new ConfigurationRevision();
        List<DeploymentChange> changeList = new ArrayList<>();
        DeploymentChange change = new DeploymentChange();
        change.setUserCode("CODE");
        change.setCreationDate(Calendar.getInstance());
        change.setType(ChangeType.CONFIGURATION_CHANGE);
        change.setConfigurationRevision(revision);
        changeList.add(change);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setChanges(changeList);
        USUserDTO user = new USUserDTO();
        when(this.userClient.getUser(anyString(), any(Exception.class))).thenReturn(user);
    }
}

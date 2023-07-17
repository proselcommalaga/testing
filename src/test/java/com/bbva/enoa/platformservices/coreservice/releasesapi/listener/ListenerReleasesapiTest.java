package com.bbva.enoa.platformservices.coreservice.releasesapi.listener;

import com.bbva.enoa.apirestgen.releasesapi.model.NewReleaseRequest;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseConfigDto;
import com.bbva.enoa.platformservices.coreservice.releasesapi.services.interfaces.IReleasesService;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ListenerReleasesapiTest
{
    public static final String USER_CODE = "CODE";
    public static final int TEST_SIZE = 32;
    private final NovaMetadata novaMetadata = new NovaMetadata();

    @Mock
    private IReleasesService releasesService;

    @InjectMocks
    private ListenerReleasesapi listenerReleasesapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList(USER_CODE));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCpds() throws Exception
    {
        for (String env : new String[]{"INT", "PRE", "PRO"})
        {
            this.listenerReleasesapi.getCpds(novaMetadata, env);
            verify(this.releasesService, Mockito.times(1)).getCpds(env);
        }
    }

    @Test
    public void createRelease() throws Exception
    {
        NewReleaseRequest newReleaseRequest = new NewReleaseRequest();
        this.listenerReleasesapi.createRelease(novaMetadata, newReleaseRequest);
        verify(this.releasesService, times(1)).createRelease(USER_CODE, newReleaseRequest);
    }

    @Test
    public void releaseInfo() throws Exception
    {
        for (int id = 0; id < TEST_SIZE; id++)
        {
            this.listenerReleasesapi.releaseInfo(novaMetadata, id);
            verify(this.releasesService, times(1)).releaseInfo(id);
        }
    }

    @Test
    public void updateReleaseConfig() throws Exception
    {
        ReleaseConfigDto releaseConfigDto = new ReleaseConfigDto();
        int releaseId = RandomUtils.nextInt(1, 10000);

        for (ReleaseConstants.ENVIRONMENT environment : ReleaseConstants.ENVIRONMENT.values())
        {
            this.listenerReleasesapi.updateReleaseConfig(novaMetadata, releaseConfigDto, releaseId, environment.name());
            verify(this.releasesService, times(1)).updateReleaseConfig(USER_CODE, releaseConfigDto, releaseId, environment);
        }
    }

    @Test
    public void deleteRelease() throws Exception
    {
        for (int id = 0; id < TEST_SIZE; id++)
        {
            this.listenerReleasesapi.deleteRelease(novaMetadata, id);
            verify(this.releasesService, times(1)).deleteRelease(USER_CODE, id);
        }
    }

    @Test
    public void getReleaseVersions() throws Exception
    {
        String status;
        for (int id = 0; id < TEST_SIZE; id++)
        {
            status = RandomStringUtils.randomAlphabetic(TEST_SIZE);
            this.listenerReleasesapi.getReleaseVersions(novaMetadata, id, status);
            verify(this.releasesService, times(1)).getReleaseVersions(id, status);
        }
    }

    @Test
    public void getProductReleases() throws Exception
    {
        for (int id = 0; id < TEST_SIZE; id++)
        {
            this.listenerReleasesapi.getProductReleases(novaMetadata, id);
            verify(this.releasesService, times(1)).getProductReleases(USER_CODE, id);
        }
    }

    @Test
    void getAllReleasesAndServices() throws Errors
    {
        for (int id = 0; id < TEST_SIZE; id++)
        {
            this.listenerReleasesapi.getAllReleasesAndServices(novaMetadata, id);
            verify(this.releasesService, times(1)).getAllReleasesAndServices(id);
        }
    }

    @Test
    void getCpdsHistorical() throws Errors
    {
        String[] current = this.listenerReleasesapi.getCpdsHistorical(novaMetadata);
        verify(this.releasesService, times(1)).getCpdsHistorical();
    }

    @Test
    void getReleasesMaxVersions() throws Errors
    {
        Integer current = this.listenerReleasesapi.getReleasesMaxVersions(novaMetadata, 1);
        verify(this.releasesService, times(1)).getReleasesMaxVersions(1);
    }


}
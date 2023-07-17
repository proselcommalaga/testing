package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IIssueTrackerClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IssueTrackerServiceImplTest
{
    @Mock
    private IIssueTrackerClient iIssuetrackerClient;
    @InjectMocks
    private IssueTrackerServiceImpl issueTrackerService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createDeploymentRequest()
    {
        Product product = new Product();
        product.setDesBoard("DES");
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        IssueTrackerItem[] response = this.issueTrackerService.createDeploymentRequest(releaseVersion, "CODE");
        verify(this.iIssuetrackerClient, times(1)).createDeploymentRequest(any(), any());

        product.setDesBoard(null);
        Assertions.assertThrows(NovaException.class, () -> this.issueTrackerService.createDeploymentRequest(releaseVersion, "CODE"));

    }
}
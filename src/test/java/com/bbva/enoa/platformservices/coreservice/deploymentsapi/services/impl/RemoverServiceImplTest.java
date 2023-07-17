package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RemoverServiceImplTest
{
    //@Rule
    //public final ExpectedException exception = ExpectedException.none();
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IDeploymentManagerClient deploymentManagerClient;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private DeploymentChangeRepository changeRepository;
    @Mock
    private ILibraryManagerService libraryManagerService;
    @InjectMocks
    private RemoverServiceImpl IRemoverService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    /*@Test
    public void removeOnEnvironment() throws Exception
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);
        plan.setId(1);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(this.deploymentManagerClient.removePlan(plan)).thenReturn(true);

        plan.setEnvironment(Environment.PRE);
        this.IRemoverService.undeployPlanOnEnvironment("CODE", 1);
        assertEquals(1, plan.getChanges().size());

        //exception.expect(NovaException.class);
        plan.setEnvironment(Environment.PRO);
        this.IRemoverService.undeployPlanOnEnvironment("CODE", 1);
    }

    @Test
    public void removeOnEnvironmentNull() throws Exception
    {
        //exception.expect(NovaException.class);
        this.IRemoverService.undeployPlanOnEnvironment("CODE", 2);
    }*/

}

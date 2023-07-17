package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionSubsystemDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemDtoBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class NewReleaseVersionDtoBuilderImplTest
{
    @Mock
    private IVersioncontrolsystemClient vcsClient;
    @Mock
    private ISubsystemDtoBuilder iSubsystemDtoBuilder;
    @InjectMocks
    private NewReleaseVersionDtoBuilderImpl newReleaseVersionDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildWithApiRest()
    {
        build(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void buildWithApi()
    {
        build(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void build(ServiceType serviceType)
    {
        Product product = new Product();
        Release release = new Release();
        release.setName("Name");
        NewReleaseVersionServiceDto service = new NewReleaseVersionServiceDto();
        service.setServiceType(serviceType.toString());
        service.setServiceName("Service");
        service.setGroupId("GroupId");
        service.setArtifactId("ArtifactId");
        service.setVersion("1.0.0");
        SubsystemTagDto tag = new SubsystemTagDto();
        tag.setServices(new NewReleaseVersionServiceDto[]{service});
        NewReleaseVersionSubsystemDto subsystem = new NewReleaseVersionSubsystemDto();
        subsystem.setTags(new SubsystemTagDto[]{tag});
        NewReleaseVersionSubsystemDto[] dtos = new NewReleaseVersionSubsystemDto[]{subsystem};
        when(this.iSubsystemDtoBuilder.buildNewSubsystemsFromProduct(product, "CODE", "Name")).thenReturn(dtos);

        NewReleaseVersionDto response = this.newReleaseVersionDtoBuilder.build(release, product, "CODE");
        Assertions.assertEquals("Name", response.getRelease().getReleaseName());
    }
}

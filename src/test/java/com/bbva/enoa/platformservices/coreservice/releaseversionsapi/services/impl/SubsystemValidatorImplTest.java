package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;

public class SubsystemValidatorImplTest
{
    @Mock
    private IToolsClient toolsClient;
    @InjectMocks
    private SubsystemValidatorImpl subsystemValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.subsystemValidator, "maxServices", 10);
    }

    @Test
    public void validateSubsystemTagDtoNOVA()
    {
        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.validateSubsystemTagDto(this.createRandomlySubsystemTagDto(), SubsystemType.NOVA), "There was an unsupported error with a NOVA subsystem");
    }

    @Test
    public void validateSubsystemTagDtoFRONTCAT()
    {
        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.validateSubsystemTagDto(this.createRandomlySubsystemTagDto(), SubsystemType.FRONTCAT), "There was an unsupported error with a NOVA subsystem");
    }

    @Test
    public void validateSubsystemTagDtoEPHOENIX()
    {
        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.validateSubsystemTagDto(this.createRandomlySubsystemTagDto(), SubsystemType.EPHOENIX), "There was an unsupported error with a NOVA subsystem");
    }

    @Test
    public void validateSubsystemTagDtoLIBRARY()
    {
        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.validateSubsystemTagDto(this.createRandomlySubsystemTagDto(), SubsystemType.LIBRARY), "There was an unsupported error with a NOVA subsystem");
    }

    @Test
    public void validateSubsystemTagDtoAnyServices()
    {
        // Given
        SubsystemTagDto tag = this.createRandomlySubsystemTagDto();
        tag.setServices(new NewReleaseVersionServiceDto[0]);
        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.validateSubsystemTagDto(tag, SubsystemType.LIBRARY), "There was an unsupported error with a NOVA subsystem");
    }

    @Test
    public void checkReleaseSubsystems()
    {
        // Given
        Release release = new Release();
        Product productMock = Mockito.mock(Product.class);

        // And
        release.setProduct(productMock);
        Mockito.when(this.toolsClient.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO()));
        Mockito.when(productMock.getId()).thenReturn(1);

        // Then
        Assertions.assertDoesNotThrow(() -> this.subsystemValidator.checkReleaseSubsystems(release), "There was an unsupported error in the checkReleaseSubsystems service");
    }

    @Test
    public void checkReleaseSubsystemsEmptySubsystems()
    {
        // Given
        Release release = new Release();
        Product productMock = Mockito.mock(Product.class);

        // And
        release.setProduct(productMock);
        Mockito.when(this.toolsClient.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of());
        Mockito.when(productMock.getId()).thenReturn(1);

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.subsystemValidator.checkReleaseSubsystems(release), "The checkReleaseSubsystems service didn't throw any NovaException when the ToolsClient don't retrieve anything");
    }


    private TOSubsystemDTO createRandomlyTOSubsystemDTO()
    {
        TOSubsystemDTO result = new TOSubsystemDTO();
        result.fillRandomly(1, false, 1, 3);
        return result;
    }

    /**
     * Creates a new random instance
     *
     * @return a random SubsystemTagDto instance
     */
    private SubsystemTagDto createRandomlySubsystemTagDto()
    {
        SubsystemTagDto result = new SubsystemTagDto();
        result.fillRandomly(1, false, 1, 3);
        return result;
    }


}
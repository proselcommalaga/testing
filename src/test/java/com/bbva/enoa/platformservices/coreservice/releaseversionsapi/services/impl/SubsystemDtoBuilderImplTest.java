package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionSubsystemDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVSubsystemDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ISubsystemValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;

public class SubsystemDtoBuilderImplTest
{
    @Mock
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;
    @Mock
    private ISubsystemValidator iSubsystemValidator;
    @Mock
    private IServiceDtoBuilder iServiceDtoBuilder;
    @Mock
    private IToolsClient toolsService;
    @InjectMocks
    private SubsystemDtoBuilderImpl subsystemDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.subsystemDtoBuilder, "maxTags", 3);
    }

    @Test
    public void buildNewSubsystemsFromProduct()
    {
        // Given
        Product productMock = mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO())); // siguiente empty
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of(this.createRandomlyVCSTag(), this.createRandomlyVCSTag())); // siguiente empty
        when(this.iServiceDtoBuilder.buildServicesFromSubsystemTag(anyInt(), any(SubsystemTagDto.class), any(SubsystemType.class), anyString(), anyString(), any(Product.class), anyString())).thenReturn(List.of(this.createRandomlyNewReleaseVersionServiceDto(), this.createRandomlyNewReleaseVersionServiceDto()));
        doNothing().when(this.iSubsystemValidator).validateSubsystemTagDto(any(SubsystemTagDto.class), any(SubsystemType.class));

        // Then
        NewReleaseVersionSubsystemDto[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildNewSubsystemsFromProduct(productMock, "XE11111", "TESTING"), "There was an unsupported error in the buildNewSubsystemsFromProduct service");
        Assertions.assertNotEquals(0, result.length, "The buildNewSubsystemsFromProduct service didn't create any NewReleaseVersionSubsystemDto");
    }

    @Test
    public void buildNewSubsystemsFromProductSubsystemsEmpty()
    {
        // Given
        Product productMock = mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of());
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of(this.createRandomlyVCSTag(), this.createRandomlyVCSTag()));
        when(this.iServiceDtoBuilder.buildServicesFromSubsystemTag(anyInt(), any(SubsystemTagDto.class), any(SubsystemType.class), anyString(), anyString(), any(Product.class), anyString())).thenReturn(List.of(this.createRandomlyNewReleaseVersionServiceDto(), this.createRandomlyNewReleaseVersionServiceDto()));
        doNothing().when(this.iSubsystemValidator).validateSubsystemTagDto(any(SubsystemTagDto.class), any(SubsystemType.class));

        // Then
        NewReleaseVersionSubsystemDto[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildNewSubsystemsFromProduct(productMock, "XE11111", "TESTING"), "There was an unsupported error in the buildNewSubsystemsFromProduct service");
        Assertions.assertEquals(0, result.length, "The buildNewSubsystemsFromProduct creates new ReleaseVersions even when the getProductSubsystems service (IToolsClient component) didn't retrieve any TOSubsystemDTO");
    }

    @Test
    public void buildNewSubsystemsFromProductTagsEmpty()
    {
        // Given
        Product productMock = mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO()));
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of());
        when(this.iServiceDtoBuilder.buildServicesFromSubsystemTag(anyInt(), any(SubsystemTagDto.class), any(SubsystemType.class), anyString(), anyString(), any(Product.class), anyString())).thenReturn(List.of(this.createRandomlyNewReleaseVersionServiceDto(), this.createRandomlyNewReleaseVersionServiceDto()));
        doNothing().when(this.iSubsystemValidator).validateSubsystemTagDto(any(SubsystemTagDto.class), any(SubsystemType.class));

        // Then
        NewReleaseVersionSubsystemDto[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildNewSubsystemsFromProduct(productMock, "XE11111", "TESTING"), "There was an unsupported error in the buildNewSubsystemsFromProduct");
        Assertions.assertTrue(Arrays.stream(result).allMatch(r -> r.getTags().length == 0), "The buildNewSubsystemsFromProduct creates Tags even when the iVersioncontrolsystemClient component didn't retrieve any tag");
    }

    @Test
    public void buildNewSubsystemsFromProductSuperTagsSize()
    {
        // Given
        Product productMock = mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO()));
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(this.createAListOfVCSTags(this.createRandomlyVCSTag(), this.createRandomlyVCSTag(), this.createRandomlyVCSTag(), this.createRandomlyVCSTag()));
        when(this.iServiceDtoBuilder.buildServicesFromSubsystemTag(anyInt(), any(SubsystemTagDto.class), any(SubsystemType.class), anyString(), anyString(), any(Product.class), anyString())).thenReturn(List.of(this.createRandomlyNewReleaseVersionServiceDto(), this.createRandomlyNewReleaseVersionServiceDto()));
        doNothing().when(this.iSubsystemValidator).validateSubsystemTagDto(any(SubsystemTagDto.class), any(SubsystemType.class));

        // Then
        NewReleaseVersionSubsystemDto[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildNewSubsystemsFromProduct(productMock, "XE11111", "TESTING"), "There was an unsupported error in the buildNewSubsystemsFromProduct service");
        Assertions.assertNotEquals(0, result.length, "The buildNewSubsystemsFromProduct didn't generate any NewReleaseVersionSubsystemDto");
    }

    @Test
    public void buildRVSubsystemDTO()
    {
        // Given
        Product productMock = Mockito.mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO()));
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of(this.createRandomlyVCSTag(), this.createRandomlyVCSTag()));

        // Then
        RVSubsystemDTO[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildRVSubsystemDTO(productMock), "There was an unsupported error in the buildRVSubsystemDTO service");
        Assertions.assertNotEquals(0, result.length, "The buildRVSubsystemDTO didn't generate any RVSubsystemDTO");
    }

    @Test
    public void buildRVSubsystemDTOSubsystemsEmpty()
    {
        // Given
        Product productMock = Mockito.mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of());
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of(this.createRandomlyVCSTag(), this.createRandomlyVCSTag()));

        // Then
        RVSubsystemDTO[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildRVSubsystemDTO(productMock), "There was an unsupported error in the buildRVSubsystemDTO service");
        Assertions.assertEquals(0, result.length, "The buildRVSubsystemDTO creates RVSubsystemDTO even when the getProductSubsystems service (IToolsClient component) didn't retrieve any TOSubsystemDTO");
    }

    @Test
    public void buildRVSubsystemDTOEmptyTags()
    {
        // Given
        Product productMock = Mockito.mock(Product.class);

        // And
        when(productMock.getId()).thenReturn(1);
        when(this.toolsService.getProductSubsystems(anyInt(), anyBoolean())).thenReturn(List.of(this.createRandomlyTOSubsystemDTO(), this.createRandomlyTOSubsystemDTO()));
        when(this.iVersioncontrolsystemClient.getTags(anyInt())).thenReturn(List.of());

        // Then
        RVSubsystemDTO[] result = Assertions.assertDoesNotThrow(() -> this.subsystemDtoBuilder.buildRVSubsystemDTO(productMock), "There was an unsupported error in the buildRVSubsystemDTO service");
        Assertions.assertTrue(Arrays.stream(result).allMatch(rvs -> rvs.getTags().length == 0), "The buildRVSubsystemDTO service creates tags even when the getTags service (iVersioncontrolsystemClient component) didn't retrieve any tag");
    }

    /**
     * Creates a modifiable list with all the tags passed by parameter
     *
     * @param tags Every tag that we want to add into the list
     * @return a modifiable list with all the passed tags
     */
    private List<VCSTag> createAListOfVCSTags(VCSTag... tags)
    {
        return new ArrayList<>(Arrays.asList(tags));
    }

    /**
     * Creates a random instance of the TOSubsystemDTO class
     *
     * @return a random TOSubsystemDTO instance
     */
    private TOSubsystemDTO createRandomlyTOSubsystemDTO()
    {
        TOSubsystemDTO result = new TOSubsystemDTO();
        result.fillRandomly(1, false, 1, 3);
        String subsystemType = Objects.requireNonNull(Arrays.stream(SubsystemType.values()).findAny().orElse(null), "There are no values in SubsystemType enum").toString();
        result.setSubsystemType(subsystemType);
        return result;
    }

    /**
     * Creates a random instance of the VCSTag class
     *
     * @return a random VCSTag instance
     */
    private VCSTag createRandomlyVCSTag()
    {
        VCSTag result = new VCSTag();
        result.fillRandomly(1, false, 1, 3);
        return result;
    }

    /**
     * Creates a random instance of the NewReleaseVersionServiceDto class
     *
     * @return a random NewReleaseVersionServiceDto instance
     */
    private NewReleaseVersionServiceDto createRandomlyNewReleaseVersionServiceDto()
    {
        NewReleaseVersionServiceDto result = new NewReleaseVersionServiceDto();
        result.fillRandomly(1, false, 1, 3);
        return result;
    }
}
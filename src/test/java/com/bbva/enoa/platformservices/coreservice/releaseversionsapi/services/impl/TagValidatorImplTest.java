package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PackageJsonFile;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TagValidatorImplTest
{
    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;

    @Mock
    private IServiceDtoBuilder serviceDtoBuilder;

    @Mock
    private IProjectFileValidator iProjectFileValidator;

    @Mock
    private ReleaseValidator releaseValidator;

    @InjectMocks
    private TagValidatorImpl tagValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.tagValidator, "maxServices", 10);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class TagValidation
    {
        @Test
        @DisplayName("(buildTagValidation) -> Is the happy path correct?")
        void buildTagValidation()
        {
            // given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.FRONTCAT);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(1);

            // then
            RVErrorDTO[] result = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of("projectName1")), "There was an unsupported error in buildTagValidation service");
            Assertions.assertEquals(0, result.length, "There are errors in tag validation! " + Arrays.toString(result));
        }

        @Test
        @DisplayName("(buildTagValidation) -> Is a subsystem tag with no services correct?")
        void buildTagValidationEmpty()
        {
            // Given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.FRONTCAT);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(0);

            // Then
            NovaException ex = Assertions.assertThrows(NovaException.class, () -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of()), "There was an unsupported error in buildTagValidation service");
            Assertions.assertEquals(ReleaseVersionError.getSubsystemTagWithoutServicesError().getErrorCode(), ex.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("(buildTagValidation) -> Is the 'maximum services' validation running?")
        void buildTagValidationExceededMaximum()
        {
            // Given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.FRONTCAT);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(11);


            // Then
            RVErrorDTO[] rvErrorDTOS = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of()), "There was an unsupported error in buildTagValidation service");
            Assertions.assertTrue(Arrays.stream(rvErrorDTOS).allMatch(rvErrorDTO -> rvErrorDTO.getCode() == null || rvErrorDTO.getCode().equals(Constants.MAX_SERVICES_EXCEEDED)));
        }

        @Test
        @DisplayName("(buildTagValidation) -> Is EPhoenix tag validation running?")
        void buildTagValidationEPhoenixSubsystem()
        {
            // Given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.EPHOENIX);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(1).stream()
                    .peek(service -> service.getService().setServiceType(ServiceType.DEPENDENCY.getServiceType()))
                    .collect(Collectors.toList());

            // Then
            RVErrorDTO[] rvErrorDTOS = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of()), "There was an unsupported error in buildTagValidation service");
            Assertions.assertTrue(Arrays.stream(rvErrorDTOS).allMatch(rvErrorDTO -> rvErrorDTO.getCode().equals(Constants.WRONG_EPHOENIX_SUBSYSTEM)));
        }

        @Test
        @Deprecated
        @DisplayName("(buildTagValidation) -> Is Library tag validation running?")
        void buildTagValidationLibrary()
        {
            // Given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.LIBRARY);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(1).stream()
                    .peek(service -> service.getService().setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType()))
                    .collect(Collectors.toList());
            // Then
            RVErrorDTO[] rvErrorDTOS = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of()), "There was an unsupported error in buildTagValidation service");
            Assertions.assertTrue(Arrays.stream(rvErrorDTOS).allMatch(rvErrorDTO -> rvErrorDTO.getCode().equals(Constants.WRONG_LIBRARY_SUBSYSTEM)));
        }

        @Test
        @DisplayName("(buildTagValidation) -> Is validation working with not known dependencies")
        void buildTagValidationWithDependencies()
        {
            // given
            RVTagDTO rvTagDTO = TagValidatorImplTest.createRandomlyRVTagDTO();
            RVSubsystemBaseDTO rvSubsystemBaseDTO = TagValidatorImplTest.createRandomlyRVSubsystemBaseDTO(SubsystemType.FRONTCAT);
            List<RVServiceValidationDTO> rvServiceValidationDTO = TagValidatorImplTest.getServiceValidationDTOList(1);

            // when
            Mockito.when(iVersioncontrolsystemClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("project2"));

            // then
            RVErrorDTO[] rvErrorDTOS = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidation(rvTagDTO, rvSubsystemBaseDTO, rvServiceValidationDTO, List.of()), "There was an unsupported error in buildTagValidation service");
            Assertions.assertTrue(Arrays.stream(rvErrorDTOS).allMatch(error -> error.getCode().equals(Constants.DEPENDENCY_NOT_FOUND)));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class TagValidationByService
    {
        @Test
        @DisplayName("(buildTagValidationByService) -> Is the happy path correct?")
        void testBuildTagValidationByService()
        {
            // given
            final String ivUser = "XE00000";
            final Integer releaseId = 1;
            RVTagDTO tagDTO = Mockito.mock(RVTagDTO.class);
            RVSubsystemBaseDTO subsystemDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            List<String> projectNames = List.of("testing");
            Release releaseMock = Mockito.mock(Release.class);
            ValidatorInputs validatorInputsMock = Mockito.mock(ValidatorInputs.class);
            NewReleaseVersionServiceDto newReleaseVersionServiceDtoMock = Mockito.mock(NewReleaseVersionServiceDto.class);
            PackageJsonFile packageJsonFileMock = Mockito.mock(PackageJsonFile.class);

            // when
            Mockito.when(releaseRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(releaseMock));
            Mockito.when(releaseValidator.checkReleaseExistence(Mockito.anyInt())).thenReturn(releaseMock);
            Mockito.when(subsystemDTO.getRepoId()).thenReturn(1);
            Mockito.when(subsystemDTO.getSubsystemType()).thenReturn(SubsystemType.NOVA.getType());
            Mockito.when(tagDTO.getTagName()).thenReturn("tag");
            Mockito.when(releaseMock.getName()).thenReturn("release");
            Mockito.when(serviceDtoBuilder.buildValidationFileInputs(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(validatorInputsMock);
            Mockito.when(serviceDtoBuilder.buildServiceGeneralInfo(Mockito.anyString(), Mockito.any())).thenReturn(newReleaseVersionServiceDtoMock);
            Mockito.doNothing().when(serviceDtoBuilder).buildServiceGenerics(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
            Mockito.when(validatorInputsMock.getPackageJson()).thenReturn(packageJsonFileMock);
            Mockito.when(packageJsonFileMock.getDependencies()).thenReturn(List.of());
            Mockito.when(newReleaseVersionServiceDtoMock.getValidationErrors()).thenReturn(new ValidationErrorDto[]{});
            Mockito.doNothing().when(iProjectFileValidator).validateServiceProjectFiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString());

            // then
            List<RVServiceValidationDTO> rvServiceValidationDTOList = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO, subsystemDTO, projectNames));
            Assertions.assertTrue(rvServiceValidationDTOList.stream().anyMatch(rvServiceValidationDTO -> rvServiceValidationDTO.getError().length == 0), " There are errors on Tag Validation: " + rvServiceValidationDTOList);
        }

        @Test
        @DisplayName("(buildTagValidationByService) -> DTO Builder couldn't build general info")
        void testBuildTagValidationByServiceReleaseNull()
        {
            // given
            final String ivUser = "XE00000";
            final Integer releaseId = 1;
            RVTagDTO tagDTO = Mockito.mock(RVTagDTO.class);
            RVSubsystemBaseDTO subsystemDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            List<String> projectNames = List.of("testing");
            Release releaseMock = Mockito.mock(Release.class);
            ValidatorInputs validatorInputsMock = Mockito.mock(ValidatorInputs.class);

            // when
            Mockito.when(releaseRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(releaseMock));
            Mockito.when(serviceDtoBuilder.buildValidationFileInputs(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(validatorInputsMock);
            Mockito.when(serviceDtoBuilder.buildServiceGeneralInfo(Mockito.anyString(), Mockito.any())).thenReturn(null);

            // then
            List<RVServiceValidationDTO> rvServiceValidationDTOList = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO, subsystemDTO, projectNames));

            Assertions.assertTrue(rvServiceValidationDTOList
                    .stream()
                    .allMatch(rvServiceValidationDTO ->
                            Arrays.stream(rvServiceValidationDTO.getError())
                                    .allMatch(rvErrorDTO -> rvErrorDTO.getCode().equals(Constants.INVALID_SERVICE_NAME_CODE))
                    ), "There  on Tag Validation: " + rvServiceValidationDTOList);

            Assertions.assertTrue(rvServiceValidationDTOList
                    .stream()
                    .allMatch(rvServiceValidationDTO -> rvServiceValidationDTO.getService().getServiceName().equals(Constants.INVALID_SERVICE_NAME)));
        }

        @Test
        @DisplayName("(buildTagValidationByService) -> Check the legacy methods")
        void testLegacyServiceValidations()
        {
            // given
            final String ivUser = "XE00000";
            final Integer releaseId = 1;
            RVTagDTO tagDTO = Mockito.mock(RVTagDTO.class);
            RVSubsystemBaseDTO subsystemDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            List<String> projectNames = List.of("testing");
            Release releaseMock = Mockito.mock(Release.class);
            ValidatorInputs validatorInputsMock = Mockito.mock(ValidatorInputs.class);
            NewReleaseVersionServiceDto newReleaseVersionServiceDtoMock = Mockito.mock(NewReleaseVersionServiceDto.class);
            PackageJsonFile packageJsonFileMock = Mockito.mock(PackageJsonFile.class);

            // when
            Mockito.when(releaseRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(releaseMock));
            Mockito.when(subsystemDTO.getRepoId()).thenReturn(1);
            Mockito.when(subsystemDTO.getSubsystemType()).thenReturn(SubsystemType.NOVA.getType());
            Mockito.when(tagDTO.getTagName()).thenReturn("tag");
            Mockito.when(serviceDtoBuilder.buildValidationFileInputs(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(validatorInputsMock);
            Mockito.when(serviceDtoBuilder.buildServiceGeneralInfo(Mockito.any(), Mockito.any())).thenReturn(newReleaseVersionServiceDtoMock);
            Mockito.when(validatorInputsMock.isLatestVersion()).thenReturn(false);
            Mockito.when(newReleaseVersionServiceDtoMock.getServiceType()).thenReturn(ServiceType.NODE.getServiceType());
            Mockito.when(tagDTO.getTagUrl()).thenReturn("tag:url");
            Mockito.when(validatorInputsMock.getPackageJson()).thenReturn(packageJsonFileMock);
            Mockito.when(packageJsonFileMock.getDependencies()).thenReturn(List.of());
            Mockito.when(newReleaseVersionServiceDtoMock.getValidationErrors()).thenReturn(new ValidationErrorDto[]{});
            Mockito.when(releaseMock.getName()).thenReturn("releaseName");

            // then
            List<RVServiceValidationDTO> rvServiceValidationDTOList = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO, subsystemDTO, projectNames));
            Assertions.assertTrue(rvServiceValidationDTOList.stream().anyMatch(rvServiceValidationDTO -> rvServiceValidationDTO.getError().length == 0), " There are errors on Tag Validation: " + rvServiceValidationDTOList);
        }

        @Test
        @DisplayName("(buildTagValidationByService) -> Check if the release version service error builder is correct")
        public void testReleaseVersionServiceWithValidationErrors()
        {
            // given
            final String ivUser = "XE00000";
            final Integer releaseId = 1;
            RVTagDTO tagDTO = Mockito.mock(RVTagDTO.class);
            RVSubsystemBaseDTO subsystemDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            List<String> projectNames = List.of("testing");
            Release releaseMock = Mockito.mock(Release.class);
            ValidatorInputs validatorInputsMock = Mockito.mock(ValidatorInputs.class);
            NewReleaseVersionServiceDto newReleaseVersionServiceDtoMock = Mockito.mock(NewReleaseVersionServiceDto.class);
            PackageJsonFile packageJsonFileMock = Mockito.mock(PackageJsonFile.class);
            ValidationErrorDto validationErrorDto = Mockito.mock(ValidationErrorDto.class);

            // when
            Mockito.when(releaseRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(releaseMock));
            Mockito.when(subsystemDTO.getRepoId()).thenReturn(1);
            Mockito.when(subsystemDTO.getSubsystemType()).thenReturn(SubsystemType.NOVA.getType());
            Mockito.when(tagDTO.getTagName()).thenReturn("tag");
            Mockito.when(serviceDtoBuilder.buildValidationFileInputs(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(validatorInputsMock);
            Mockito.when(serviceDtoBuilder.buildServiceGeneralInfo(Mockito.any(), Mockito.any())).thenReturn(newReleaseVersionServiceDtoMock);
            Mockito.when(validatorInputsMock.isLatestVersion()).thenReturn(true);
            Mockito.when(newReleaseVersionServiceDtoMock.getServiceType()).thenReturn(ServiceType.NODE.getServiceType());
            Mockito.when(tagDTO.getTagUrl()).thenReturn("tag:url");
            Mockito.when(validatorInputsMock.getPackageJson()).thenReturn(packageJsonFileMock);
            Mockito.when(packageJsonFileMock.getDependencies()).thenReturn(List.of());
            Mockito.when(newReleaseVersionServiceDtoMock.getValidationErrors()).thenReturn(new ValidationErrorDto[]{validationErrorDto});
            Mockito.when(validationErrorDto.getCode()).thenReturn(Constants.INVALID_LANGUAGE);
            Mockito.when(releaseMock.getName()).thenReturn("releaseName");

            // then
            List<RVServiceValidationDTO> rvServiceValidationDTOList = Assertions.assertDoesNotThrow(() -> tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO, subsystemDTO, projectNames));
            Assertions.assertTrue(rvServiceValidationDTOList.stream().anyMatch(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()).allMatch(rvErrorDTO -> rvErrorDTO.getCode().equals(Constants.INVALID_LANGUAGE))), " There are errors on Tag Validation: " + rvServiceValidationDTOList);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class AllowedMultiTagValidation
    {
        @Test
        @DisplayName("(AllowedMultiTagValidation) -> Is the happy path correct?")
        void ok()
        {
            // given
            RVValidationDTO rvValidationDTO = Mockito.mock(RVValidationDTO.class);
            RVSubsystemBaseDTO rvSubsystemBaseDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            RVTagDTO rvTagDTO = Mockito.mock(RVTagDTO.class);

            // when
            Mockito.when(rvValidationDTO.getSubsystem()).thenReturn(rvSubsystemBaseDTO);
            Mockito.when(rvSubsystemBaseDTO.getSubsystemType()).thenReturn(SubsystemType.LIBRARY.getType());
            Mockito.when(rvValidationDTO.getTags()).thenReturn(new RVTagDTO[]{rvTagDTO});

            // then
            Assertions.assertDoesNotThrow(() -> tagValidator.validateAllowedMultitag(rvValidationDTO), "Library and only one tag is throwing an exception");
        }

        @Test
        @DisplayName("(AllowedMultiTagValidation) -> Is multitag validation working with ")
        void error()
        {
            // given
            RVValidationDTO rvValidationDTO = Mockito.mock(RVValidationDTO.class);
            RVSubsystemBaseDTO rvSubsystemBaseDTO = Mockito.mock(RVSubsystemBaseDTO.class);
            RVTagDTO rvTagDTO = Mockito.mock(RVTagDTO.class);

            // when
            Mockito.when(rvValidationDTO.getSubsystem()).thenReturn(rvSubsystemBaseDTO);
            Mockito.when(rvSubsystemBaseDTO.getSubsystemType()).thenReturn(SubsystemType.NOVA.getType());
            Mockito.when(rvValidationDTO.getTags()).thenReturn(new RVTagDTO[]{rvTagDTO, rvTagDTO});

            // then
            NovaException exception = Assertions.assertThrows(NovaException.class, () -> tagValidator.validateAllowedMultitag(rvValidationDTO), "Library and only one tag is throwing an exception");
            Assertions.assertEquals(ReleaseVersionError.getMultitagNotAllowedError().getErrorCode(), exception.getNovaError().getErrorCode());
        }
    }


    /**
     * Generate a list of service's validation DTOs with the parameter's length
     * @param length how many services do you want on the list
     * @return a list of service's validation DTOs
     */
    private static List<RVServiceValidationDTO> getServiceValidationDTOList(final @NotNull @Valid @Positive Integer length)
    {
        return IntStream.range(0, length).mapToObj(i -> {
            RVServiceValidationDTO serviceValidationDTO = new RVServiceValidationDTO();
            serviceValidationDTO.setError(new RVErrorDTO[0]);
            RVServiceDTO service = new RVServiceDTO();
            service.fillRandomly(10, false, 0, 2);
            service.setServiceType(generateRandomServiceType().getServiceType());
            serviceValidationDTO.setService(service);
            return serviceValidationDTO;
        }).collect(Collectors.toList());
    }

    /**
     * Creates a random RVTagDTO object
     *
     * @return a random RVTagDTO instance
     */
    private static RVTagDTO createRandomlyRVTagDTO()
    {
        RVTagDTO result = new RVTagDTO();
        result.fillRandomly(2, false, 1, 3);
        return result;
    }

    private static ServiceType generateRandomServiceType()
    {
        Random generator = new Random();
        return ServiceType.values()[generator.nextInt(ServiceType.values().length)];
    }

    /**
     * Creates a random RVSubsystemBaseDTO object
     *
     * @return a random RVSubsystemBaseDTO instance
     */
    private static RVSubsystemBaseDTO createRandomlyRVSubsystemBaseDTO(SubsystemType type)
    {
        RVSubsystemBaseDTO result = new RVSubsystemBaseDTO();
        result.fillRandomly(2, false, 1, 3);
        //String subsystemType = Objects.requireNonNull(Arrays.stream(SubsystemType.values()).findAny().orElse(null), "There are no values in SubsystemType enum").toString();
        result.setRepoId(1);
        result.setSubsystemType(type.toString());
        return result;
    }
}

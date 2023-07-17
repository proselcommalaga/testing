package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl;

import com.bbva.enoa.apirestgen.qualityassuranceapi.model.*;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.*;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.enums.QualityLevel;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IQualityAssuranceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.IDependencyResolutionService;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class QualityManagerServiceTest
{
    /**
     * List of service type without analyses
     */
    private static final List<ServiceType> SERVICE_TYPES_WITHOUT_ANALYSES = List.of(ServiceType.BATCH_SCHEDULER_NOVA,
            ServiceType.CDN_POLYMER_CELLS, ServiceType.LIBRARY_PYTHON, ServiceType.LIBRARY_TEMPLATE);

    /**
     * String constant to avoid check a field
     */
    private static final String NO_CHECK = "";

    @InjectMocks
    private QualityManagerService qualityManagerService;

    @Mock
    private VersioncontrolsystemClientImpl versioncontrolsystemClient;

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;

    @Mock
    private IQualityAssuranceClient qualityAssuranceClient;

    @Mock
    private IVersioncontrolsystemClient versionControlSystemClient;

    @Mock
    private DeploymentPlanRepository planRepository;

    @Mock
    private IUsersClient usersService;

    @Mock
    private IServiceDtoBuilder serviceDtoBuilder;

    @Mock
    private IToolsClient toolsService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DocSystemRepository docSystemRepo;

    @Mock
    private IDependencyResolutionService dependencyResolutionService;

    @Mock
    private INovaActivityEmitter activityEmitter;

    @Mock
    private DeploymentUtils deploymentUtils;

    private final String ivUser = "iv-user";


    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(this.usersService).checkHasPermission(Mockito.anyString(), Mockito.anyString(), any());
    }

    @Test
    void requestSubsystemCodeAnalysisNoSubsystem()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);
        int subsystemId = -1;

        when(this.toolsService.getSubsystemById(subsystemId)).thenReturn(null);
        Assertions.assertThrows(NovaException.class,
                () -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystemId, branchName),
                "Expected NovaException by Subsystem not found");

        //check exception
        this.assertNovaException(() -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystemId, branchName),
                QualityConstants.QualityManagerErrors.QM_SUBSYSTEM_NOT_FOUND_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, Constants.MSG_CONTACT_NOVA);
    }

    @Test
    void requestSubsystemCodeAnalysisNoProduct()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);

        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        subsystem.setResourceId(-1);
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.empty());

        Assertions.assertThrows(NovaException.class,
                () -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName),
                "Expected NovaException by Product not found");

        this.assertNovaException(() -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName),
                QualityConstants.QualityManagerErrors.QM_PRODUCT_NOT_FOUND_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, Constants.MSG_CONTACT_NOVA);
    }

    @Test
    void requestSubsystemCodeAnalysisEphoenixSubsystemEmptyServices()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);

        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        subsystem.setSubsystemType(SubsystemType.EPHOENIX.getType());
        Product product = generateRandomProductFromSubsysytem(subsystem);
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));

        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.EPHOENIX), Mockito.eq(Constants.IMMUSER),
                Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName())))
                .thenReturn(Collections.emptyList());

        //Captor to validate arguments passed to qualityAssuranceClient when is a ephoenix subsystem
        final ArgumentCaptor<QAEphoenixSubsystemCodeAnalysisRequest> ephoenixSubReqCaptor
                = ArgumentCaptor.forClass(QAEphoenixSubsystemCodeAnalysisRequest.class);

        final ArgumentCaptor<SubsystemTagDto> subsystemTagDtoCaptor
                = ArgumentCaptor.forClass(SubsystemTagDto.class);

        qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName);

        Mockito.verify(this.qualityAssuranceClient, Mockito.times(1))
                .requestEphoenixSubsystemCodeAnalysis(ephoenixSubReqCaptor.capture(), Mockito.eq(subsystem.getSubsystemId()));

        Mockito.verify(this.serviceDtoBuilder, Mockito.times(1))
                .buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                        subsystemTagDtoCaptor.capture(), Mockito.eq(SubsystemType.EPHOENIX), Mockito.anyString(),
                        Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()));

        QAEphoenixSubsystemCodeAnalysisRequest ephoenixRequest = ephoenixSubReqCaptor.getValue();
        Assertions.assertEquals(branchName, ephoenixRequest.getBranchName());
        Assertions.assertEquals(subsystem.getSubsystemName(), ephoenixRequest.getSubsystemName());
        Assertions.assertEquals(subsystem.getSubsystemType(), ephoenixRequest.getSubsystemType());
        Assertions.assertEquals(product.getUuaa(), ephoenixRequest.getUuaa());

        QAEphoenixJobParametersDTO ephoenixJobParameters = ephoenixRequest.getJobParameters();

        //validations of services
        this.assertQAEphoenixJobEphoenixServicesInQAEphoenixJobParametersDTO(Collections.emptyList(), ephoenixRequest.getJobParameters());
        //validations of params for job
        this.assertQAEphoenixJobParameters(subsystem, product, branchName, ephoenixJobParameters);

        //validations for SubsystemTagDto
        SubsystemTagDto tagDto = subsystemTagDtoCaptor.getValue();
        Assertions.assertEquals(branchName, tagDto.getTagName());
        Assertions.assertEquals(branchName, tagDto.getTagUrl());
    }


    @Test
    void requestSubsystemCodeAnalysisEphoenixNoModules()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);

        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        subsystem.setSubsystemType(SubsystemType.EPHOENIX.getType());
        Product product = generateRandomProductFromSubsysytem(subsystem);
        //Prepare a list of services
        List<NewReleaseVersionServiceDto> servicesList = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
            serviceDto.setServiceName("service-batch-" + i);
            serviceDto.setArtifactId("service-batch-artifactid-" + i);
            serviceDto.setUuaa(product.getUuaa());
            serviceDto.setFolder("folder-batch-" + i);
            serviceDto.setVersion("version-batch.1.0." + i);
            serviceDto.setModules(new String[0]);
            servicesList.add(serviceDto);

            serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
            serviceDto.setServiceName("service-online-" + i);
            serviceDto.setArtifactId("service-online-artifactid-" + i);
            serviceDto.setUuaa(product.getUuaa());
            serviceDto.setFolder("folder-online-" + i);
            serviceDto.setVersion("version-online.1.0." + i);
            serviceDto.setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType());
            serviceDto.setModules(new String[0]);
            servicesList.add(serviceDto);
        }

        // Get names of projects that have a pom.xml, from Product VCS repo and tag.
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));
        //get the list of tag
        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.EPHOENIX), Mockito.anyString(),
                Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(servicesList);


        final ArgumentCaptor<QAEphoenixSubsystemCodeAnalysisRequest> ephoenixSubReqCaptor
                = ArgumentCaptor.forClass(QAEphoenixSubsystemCodeAnalysisRequest.class);

        final ArgumentCaptor<SubsystemTagDto> subsytemTagDtoCaptor
                = ArgumentCaptor.forClass(SubsystemTagDto.class);

        qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName);

        Mockito.verify(this.qualityAssuranceClient, Mockito.times(1))
                .requestEphoenixSubsystemCodeAnalysis(ephoenixSubReqCaptor.capture(), Mockito.eq(subsystem.getSubsystemId()));

        Mockito.verify(this.serviceDtoBuilder, Mockito.times(1))
                .buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                        subsytemTagDtoCaptor.capture(), Mockito.eq(SubsystemType.EPHOENIX), Mockito.anyString(),
                        Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()));


        QAEphoenixSubsystemCodeAnalysisRequest ephoenixRequest = ephoenixSubReqCaptor.getValue();
        Assertions.assertEquals(branchName, ephoenixRequest.getBranchName());
        Assertions.assertEquals(subsystem.getSubsystemName(), ephoenixRequest.getSubsystemName());
        Assertions.assertEquals(subsystem.getSubsystemType(), ephoenixRequest.getSubsystemType());
        Assertions.assertEquals(product.getUuaa(), ephoenixRequest.getUuaa());

        QAEphoenixJobParametersDTO ephoenixJobParameters = ephoenixRequest.getJobParameters();

        //Validations for services
        this.assertQAEphoenixJobEphoenixServicesInQAEphoenixJobParametersDTO(servicesList, ephoenixRequest.getJobParameters());
        //validations of params for job
        this.assertQAEphoenixJobParameters(subsystem, product, branchName, ephoenixJobParameters);

        //validations for SubsystemTagDto
        SubsystemTagDto tagDto = subsytemTagDtoCaptor.getValue();
        Assertions.assertEquals(branchName, tagDto.getTagName());
        Assertions.assertEquals(branchName, tagDto.getTagUrl());
    }

    @Test
    void requestSubsystemCodeAnalysisEphoenix()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);

        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        subsystem.setSubsystemType(SubsystemType.EPHOENIX.getType());
        Product product = generateRandomProductFromSubsysytem(subsystem);
        //Prepare a list of services
        List<NewReleaseVersionServiceDto> servicesList = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
            serviceDto.setServiceName("service-batch-" + i);
            serviceDto.setFolder("folder-batch-" + i);
            serviceDto.setVersion("version-batch.1.0." + i);
            serviceDto.setArtifactId("service-batch-artifactid-" + i);
            serviceDto.setUuaa(product.getUuaa());
            serviceDto.setModules(new String[]{"module1"});
            servicesList.add(serviceDto);

            serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType());
            serviceDto.setServiceName("service-online-" + i);
            serviceDto.setFolder("folder-online-" + i);
            serviceDto.setVersion("version-online.1.0." + i);
            serviceDto.setArtifactId("service-online-artifactid-" + i);
            serviceDto.setUuaa(product.getUuaa());
            serviceDto.setModules(new String[]{"module2"});
            servicesList.add(serviceDto);
        }

        // Get names of projects that have a pom.xml, from Product VCS repo and tag.
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));
        //get the list of tag
        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.EPHOENIX), Mockito.anyString(),
                Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(servicesList);


        final ArgumentCaptor<QAEphoenixSubsystemCodeAnalysisRequest> ephoenixSubReqCaptor
                = ArgumentCaptor.forClass(QAEphoenixSubsystemCodeAnalysisRequest.class);

        final ArgumentCaptor<SubsystemTagDto> subsytemTagDtoCaptor
                = ArgumentCaptor.forClass(SubsystemTagDto.class);

        qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName);

        Mockito.verify(this.qualityAssuranceClient, Mockito.times(1))
                .requestEphoenixSubsystemCodeAnalysis(ephoenixSubReqCaptor.capture(), Mockito.eq(subsystem.getSubsystemId()));

        Mockito.verify(this.serviceDtoBuilder, Mockito.times(1))
                .buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                        subsytemTagDtoCaptor.capture(), Mockito.eq(SubsystemType.EPHOENIX), Mockito.anyString(),
                        Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()));


        QAEphoenixSubsystemCodeAnalysisRequest ephoenixRequest = ephoenixSubReqCaptor.getValue();
        Assertions.assertEquals(branchName, ephoenixRequest.getBranchName());
        Assertions.assertEquals(subsystem.getSubsystemName(), ephoenixRequest.getSubsystemName());
        Assertions.assertEquals(subsystem.getSubsystemType(), ephoenixRequest.getSubsystemType());
        Assertions.assertEquals(product.getUuaa(), ephoenixRequest.getUuaa());

        QAEphoenixJobParametersDTO ephoenixJobParameters = ephoenixRequest.getJobParameters();

        //Validations for servicices
        this.assertQAEphoenixJobEphoenixServicesInQAEphoenixJobParametersDTO(servicesList, ephoenixRequest.getJobParameters());
        //validations of params for job
        this.assertQAEphoenixJobParameters(subsystem, product, branchName, ephoenixJobParameters);

        //validations for SubsystemTagDto
        SubsystemTagDto tagDto = subsytemTagDtoCaptor.getValue();
        Assertions.assertEquals(branchName, tagDto.getTagName());
        Assertions.assertEquals(branchName, tagDto.getTagUrl());
    }


    @Test
    void requestSubsystemCodeAnalysisSubsystemEmpty()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);

        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        subsystem.setSubsystemType(SubsystemType.NOVA.getType());
        Product product = generateRandomProductFromSubsysytem(subsystem);
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));


        //get the list of tag.... without services!
        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.NOVA), Mockito.anyString(),
                Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(Collections.emptyList());

        //by subsystem type
        SubsystemType[] types = new SubsystemType[]{SubsystemType.NOVA, SubsystemType.LIBRARY, SubsystemType.FRONTCAT};
        int subsystemId = subsystem.getSubsystemId();
        for (SubsystemType subsystemType : types)
        {
            subsystem.setSubsystemType(subsystemType.getType());
            subsystem.setSubsystemId(++subsystemId);
            when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);

            Assertions.assertThrows(NovaException.class,
                    () -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName),
                    " A empty list of service into a subsystem type " + subsystemType + " has to launch an NovaException");
        }
    }

    @Test
    void requestSubsystemCodeAnalysis()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);
        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        Product product = generateRandomProductFromSubsysytem(subsystem);


        //all types excepts without analysis and dependency
        EnumSet<ServiceType> validServiceType = EnumSet.complementOf(EnumSet.copyOf(SERVICE_TYPES_WITHOUT_ANALYSES));
        validServiceType.remove(ServiceType.DEPENDENCY);

        //Prepare a list of services
        List<NewReleaseVersionServiceDto> allServices = new ArrayList<>();
        Map<String, NewReleaseVersionServiceDto> servicesFiltered = new HashMap<>();
        //A service with each type.
        for (ServiceType serviceType : ServiceType.values())
        {
            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(serviceType.getServiceType());
            serviceDto.setServiceName("service-" + serviceType);
            serviceDto.setFolder("folder-" + serviceType);
            serviceDto.setVersion("version-" + serviceType + ".1.0");
            serviceDto.setValidationErrors(new ValidationErrorDto[0]);
            allServices.add(serviceDto);

            if (validServiceType.contains(serviceType))
            {
                servicesFiltered.put(serviceDto.getServiceName(), serviceDto);
            }
        }

        // Get names of projects that have a pom.xml, from Product VCS repo and tag.
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));

        //get the list of tag
        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.getValueOf(subsystem.getSubsystemType())),
                Mockito.anyString(), Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(allServices);

        final ArgumentCaptor<QASubsystemCodeAnalysisRequest> qaSubCodeAnalysisRequestCaptor
                = ArgumentCaptor.forClass(QASubsystemCodeAnalysisRequest.class);

        qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName);

        Mockito.verify(this.qualityAssuranceClient, Mockito.times(1))
                .requestSubsystemCodeAnalysis(qaSubCodeAnalysisRequestCaptor.capture(), Mockito.eq(subsystem.getSubsystemId()));

        QASubsystemCodeAnalysisRequest request = qaSubCodeAnalysisRequestCaptor.getValue();

        Assertions.assertEquals(branchName, request.getBranchName());
        Assertions.assertEquals(subsystem.getSubsystemName(), request.getSubsystemName());
        Assertions.assertEquals(subsystem.getSubsystemType(), request.getSubsystemType());
        Assertions.assertEquals(product.getUuaa(), request.getUuaa());

        Assertions.assertEquals(servicesFiltered.size(), request.getServices().length);
        NewReleaseVersionServiceDto expected;
        for (QAServiceDTO current : request.getServices())
        {
            expected = servicesFiltered.get(current.getName());
            Assertions.assertNotNull(expected);

            Assertions.assertEquals(expected.getServiceName(), current.getName());
            Assertions.assertEquals(expected.getServiceType(), current.getType());
            Assertions.assertEquals(expected.getFolder(), current.getFolder());
            Assertions.assertEquals(expected.getVersion(), current.getVersion());
        }
    }

    @Test
    void requestSubsystemCodeAnalysisWithValidationError()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);
        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        Product product = generateRandomProductFromSubsysytem(subsystem);

        //all types excepts without analysis and dependency
        EnumSet<ServiceType> validServiceType = EnumSet.complementOf(EnumSet.copyOf(SERVICE_TYPES_WITHOUT_ANALYSES));
        validServiceType.remove(ServiceType.DEPENDENCY);

        //A service with each type.
        ValidationErrorDto validationError = new ValidationErrorDto();
        for (ServiceType serviceType : validServiceType)
        {
            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceType(serviceType.getServiceType());
            serviceDto.setServiceName("service-" + serviceType);
            serviceDto.setFolder("folder-" + serviceType);
            serviceDto.setVersion("version-" + serviceType + ".1.0");
            validationError.setCode("TEST-0001");
            validationError.setMessage("Error by TEST-0001");
            serviceDto.setValidationErrors(new ValidationErrorDto[]{validationError});

            // Get names of projects that have a pom.xml, from Product VCS repo and tag.
            when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
            when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));

            //get the list of tag
            when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                    any(SubsystemTagDto.class), Mockito.eq(SubsystemType.getValueOf(subsystem.getSubsystemType())),
                    Mockito.anyString(), Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(List.of(serviceDto));

            final ArgumentCaptor<QASubsystemCodeAnalysisRequest> qaSubCodeAnalysisRequestCaptor
                    = ArgumentCaptor.forClass(QASubsystemCodeAnalysisRequest.class);

            Assertions.assertThrows(NovaException.class,
                    () -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName),
                    "A NovaException was expected by ValidationErrors into service");

            //validate Info
            this.assertNovaException(() -> qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName),
                    QualityConstants.QualityManagerErrors.SERVICE_VALIDATION_ERROR_CODE, ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, NO_CHECK);
        }
    }

    @Test
    void requestSubsystemCodeAnalysisWithDependencies()
    {
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);
        TOSubsystemDTO subsystem = getRandomSubsystemDTO();
        Product product = generateRandomProductFromSubsysytem(subsystem);

        //Prepare a list of services
        List<NewReleaseVersionServiceDto> services = new ArrayList<>();
        List<String> dependencies = new ArrayList<>();

        //A daemon service
        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
        serviceDto.setServiceName("service-daemon-1");
        serviceDto.setFolder("folder-daemon");
        serviceDto.setVersion("version-daemon.1.0");
        serviceDto.setValidationErrors(new ValidationErrorDto[0]);
        services.add(serviceDto);

        // 2 dependencies
        serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        serviceDto.setServiceName("service-dependency-1");
        serviceDto.setFolder("folder-dependency");
        serviceDto.setVersion("version-dependency.1.0");
        serviceDto.setValidationErrors(new ValidationErrorDto[0]);
        services.add(serviceDto);
        dependencies.add(serviceDto.getServiceName());

        serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        serviceDto.setServiceName("service-dependency-2");
        serviceDto.setFolder("folder-dependency-2");
        serviceDto.setVersion("version-dependency.2.0");
        serviceDto.setValidationErrors(new ValidationErrorDto[0]);
        services.add(serviceDto);
        dependencies.add(serviceDto.getServiceName());


        // Get names of projects that have a pom.xml, from Product VCS repo and tag.
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystem);
        when(this.productRepository.findById(subsystem.getResourceId())).thenReturn(Optional.of(product));
        when(this.versionControlSystemClient.getDependencies(subsystem.getRepoId(), branchName)).thenReturn(dependencies);

        //get the list of tag
        when(serviceDtoBuilder.buildServicesFromSubsystemTag(Mockito.eq(subsystem.getRepoId()),
                any(SubsystemTagDto.class), Mockito.eq(SubsystemType.getValueOf(subsystem.getSubsystemType())),
                Mockito.anyString(), Mockito.eq(QualityConstants.SQA), Mockito.eq(product), Mockito.eq(subsystem.getSubsystemName()))).thenReturn(services);

        final ArgumentCaptor<QASubsystemCodeAnalysisRequest> qaSubCodeAnalysisRequestCaptor
                = ArgumentCaptor.forClass(QASubsystemCodeAnalysisRequest.class);

        qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystem.getSubsystemId(), branchName);

        Mockito.verify(this.qualityAssuranceClient, Mockito.times(1))
                .requestSubsystemCodeAnalysis(qaSubCodeAnalysisRequestCaptor.capture(), Mockito.eq(subsystem.getSubsystemId()));

        QASubsystemCodeAnalysisRequest request = qaSubCodeAnalysisRequestCaptor.getValue();

        Assertions.assertEquals(branchName, request.getBranchName());
        Assertions.assertEquals(subsystem.getSubsystemName(), request.getSubsystemName());
        Assertions.assertEquals(subsystem.getSubsystemType(), request.getSubsystemType());
        Assertions.assertEquals(product.getUuaa(), request.getUuaa());

        //how it is a service with dependencies, dependencies are not present
        Assertions.assertEquals(1, request.getServices().length, "Dependency services cannot be returned");
    }


    @Test
    void getReleaseVersionCodeAnalysesNoRV()
    {
        int releaseVersionId = -1;
        when(this.releaseVersionRepository.findById(releaseVersionId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NovaException.class,
                () -> qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersionId),
                "Expected NovaException by Subsystem not found");

        //check exception
        this.assertNovaException(() -> qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersionId),
                QualityConstants.QualityManagerErrors.QM_INVALID_RELEASE_VERSION_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, NO_CHECK);
    }

    @Test
    void getReleaseVersionCodeAnalysesBuildingOrErrorRV()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        release.setReleaseVersions(List.of(releaseVersion));
        //RandomSubsystem
        releaseVersion.setSubsystems(List.of(getReleaseVersionSubsystemBy(getRandomSubsystemDTO(), releaseVersion)));
        //Status as Building
        releaseVersion.setStatus(ReleaseVersionStatus.BUILDING);

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        ReleaseVersionCodeAnalyses response = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(QualityConstants.SQA_NOT_AVAILABLE, response.getSqaState());
        Assertions.assertNotNull(response.getSubsystems());
        Assertions.assertEquals(0, response.getSubsystems().length);

        //Same case for Error
        releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
        response = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(QualityConstants.SQA_NOT_AVAILABLE, response.getSqaState());
        Assertions.assertNotNull(response.getSubsystems());
        Assertions.assertEquals(0, response.getSubsystems().length);
    }

    @Test
    void getReleaseVersionCodeAnalysesNoSubsystem()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        release.setReleaseVersions(List.of(releaseVersion));
        // NO Subsystem
        releaseVersion.setSubsystems(Collections.emptyList());
        //Valid STATUS for QA Analysis
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        ReleaseVersionCodeAnalyses response = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(QualityConstants.SQA_OK, response.getSqaState());
        Assertions.assertNotNull(response.getSubsystems());
        Assertions.assertEquals(0, response.getSubsystems().length);
    }


    @Test
    void getReleaseVersionCodeAnalysesNoServices()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        //Valid STATUS for QA Analysis
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        //subsystems (from DTO)
        List<TOSubsystemDTO> subsystemDtoList = new ArrayList<>();
        TOSubsystemDTO subsystemDto;
        //subsystems
        for (int i = 0; i < 3; i++)
        {
            subsystemDto = getRandomSubsystemDTO();
            subsystemDto.setSubsystemId(++id);
            when(this.toolsService.getSubsystemById(subsystemDto.getSubsystemId())).thenReturn(subsystemDto);
            subsystemDtoList.add(subsystemDto);

            //add a ReleaseVersionSubsytem object
            releaseVersion.getSubsystems().add(getReleaseVersionSubsystemBy(subsystemDto, releaseVersion));
        }

        //mock the return of RV
        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));

        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());
        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        Assertions.assertEquals(QualityConstants.SQA_OK, releaseVersionCodeAnalyses.getSqaState());
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(subsystemDtoList.size(), releaseVersionCodeAnalyses.getSubsystems().length);

        ReleaseVersionSubsystem rvsExpected;
        TOSubsystemDTO expected;
        SubsystemCodeAnalyses current;
        for (int i = 0; i < releaseVersionCodeAnalyses.getSubsystems().length; i++)
        {
            rvsExpected = releaseVersion.getSubsystems().get(i);
            expected = subsystemDtoList.get(i);
            current = releaseVersionCodeAnalyses.getSubsystems()[i];
            Assertions.assertEquals(expected.getSubsystemType(), current.getSubsystemType());
            Assertions.assertEquals(expected.getSubsystemName(), current.getSubsystemName());
            Assertions.assertEquals(rvsExpected.getTagName(), current.getTag());
            Assertions.assertNotNull(current.getServices());
            Assertions.assertEquals(0, current.getServices().length);
        }
    }

    @Test
    void getReleaseVersionCodeAnalysesByServicesWithoutQA()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);
        product.setQualityLevel(QualityLevel.HIGH.getValue());

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(++id);
        subsystem.setSubsystemId(id);
        subsystem.setReleaseVersion(releaseVersion);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl("tag-url-" + subsystem.getSubsystemId());
        subsystem.setCompilationJobName("compilationJob-" + +subsystem.getSubsystemId());
        releaseVersion.setSubsystems(List.of(subsystem));

        ReleaseVersionService service = new ReleaseVersionService();
        service.setId(++id);
        service.setServiceName("service-" + id);
        service.setVersion("service.v1.0." + id);
        //set type without analysis
        service.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());

        subsystem.setServices(List.of(service));
        service.setVersionSubsystem(subsystem);

        TOSubsystemDTO subsystemDTO = getSubSystemDTOBy(subsystem);

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystemDTO);

        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        Assertions.assertEquals(QualityConstants.SQA_OK, releaseVersionCodeAnalyses.getSqaState());
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(1, releaseVersionCodeAnalyses.getSubsystems().length);

        SubsystemCodeAnalyses currentSubsytemAnalyses = releaseVersionCodeAnalyses.getSubsystems()[0];
        Assertions.assertEquals(subsystemDTO.getSubsystemType(), currentSubsytemAnalyses.getSubsystemType());
        Assertions.assertEquals(subsystemDTO.getSubsystemName(), currentSubsytemAnalyses.getSubsystemName());
        Assertions.assertEquals(subsystem.getTagName(), currentSubsytemAnalyses.getTag());
        Assertions.assertNotNull(currentSubsytemAnalyses.getServices());
        Assertions.assertEquals(1, currentSubsytemAnalyses.getServices().length);


        ServiceCodeAnalyses current = currentSubsytemAnalyses.getServices()[0];
        Assertions.assertEquals(service.getServiceName(), current.getServiceName());
        Assertions.assertEquals(service.getServiceType(), current.getServiceType());
        Assertions.assertEquals(service.getVersion(), current.getServiceVersion());
        Assertions.assertEquals(QualityConstants.SQA_OK, current.getSqaState());
        Assertions.assertNotNull(current.getAnalysis());
        Assertions.assertEquals(0, current.getAnalysis().length);
    }

    @Test
    void getReleaseVersionCodeAnalysesByServiceValidateQACodeAnalysisRequest()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);
        product.setQualityLevel(QualityLevel.HIGH.getValue());
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(++id);
        subsystem.setSubsystemId(id);
        subsystem.setReleaseVersion(releaseVersion);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl("tag-url-" + subsystem.getSubsystemId());
        subsystem.setCompilationJobName("compilationJob-" + +subsystem.getSubsystemId());
        releaseVersion.setSubsystems(List.of(subsystem));

        ReleaseVersionService service = new ReleaseVersionService();
        service.setId(++id);
        service.setServiceName("service-" + id);
        service.setVersion("service.v1.0." + id);
        service.setServiceType(ServiceType.NOVA.getServiceType());

        subsystem.setServices(List.of(service));
        service.setVersionSubsystem(subsystem);

        TOSubsystemDTO subsystemDTO = getSubSystemDTOBy(subsystem);

        Map<String, CodeAnalysis> codeAnalysisByServiceName = new HashMap<>();
        CodeAnalysis codeAnalysis = new CodeAnalysis();
        codeAnalysis.setId(++id);
        codeAnalysis.setProjectId(++id);
        codeAnalysis.setProjectName("project-" + service.getServiceName());
        codeAnalysis.setAnalysisDate(System.currentTimeMillis());
        codeAnalysis.setSqaState(QualityConstants.SQA_OK);
        codeAnalysis.setLanguage("JAVA");
        codeAnalysis.setHasSonarReport(true);
        codeAnalysis.setSonarState("sonar-state-" + codeAnalysis.getId());
        codeAnalysis.setSonarVersion("sonar-version-" + codeAnalysis.getId());

        codeAnalysisByServiceName.put(service.getServiceName(), codeAnalysis);

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystemDTO);

        when(this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(service.getId())).thenReturn(new CodeAnalysis[]{codeAnalysis});
        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        Assertions.assertEquals(QualityConstants.SQA_OK, releaseVersionCodeAnalyses.getSqaState());
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(1, releaseVersionCodeAnalyses.getSubsystems().length);

        //validation subsystems
        Mockito.verify(this.qualityAssuranceClient).getCodeAnalysesByReleaseVersionService(service.getId());
        this.assertSubsystemCodeAnalysesByReleaseVersionCodeAnalyses(Map.of(subsystemDTO.getSubsystemName(), QualityConstants.SQA_OK),
                codeAnalysisByServiceName, List.of(subsystemDTO), releaseVersion.getSubsystems(), releaseVersionCodeAnalyses);
    }

    @Test
    void getReleaseVersionCodeAnalysesByLibraryValidateQACodeAnalysisRequest()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);
        product.setQualityLevel(QualityLevel.HIGH.getValue());
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(++id);
        subsystem.setSubsystemId(id);
        subsystem.setReleaseVersion(releaseVersion);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl("tag-url-" + subsystem.getSubsystemId());
        subsystem.setCompilationJobName("compilationJob-" + +subsystem.getSubsystemId());
        releaseVersion.setSubsystems(List.of(subsystem));

        ReleaseVersionService service = new ReleaseVersionService();
        service.setId(++id);
        service.setServiceName("library-" + id);
        service.setVersion("library.v1.0." + id);
        service.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());

        subsystem.setServices(List.of(service));
        service.setVersionSubsystem(subsystem);

        TOSubsystemDTO subsystemDTO = getSubSystemDTOBy(subsystem);

        Map<String, CodeAnalysis> codeAnalysisByServiceName = new HashMap<>();
        CodeAnalysis codeAnalysis = new CodeAnalysis();
        codeAnalysis.setId(++id);
        codeAnalysis.setProjectId(++id);
        codeAnalysis.setProjectName("project-" + service.getServiceName());
        codeAnalysis.setAnalysisDate(System.currentTimeMillis());
        codeAnalysis.setSqaState(QualityConstants.SQA_OK);
        codeAnalysis.setLanguage("JAVA");
        codeAnalysis.setHasSonarReport(true);
        codeAnalysis.setSonarState("sonar-state-" + codeAnalysis.getId());
        codeAnalysis.setSonarVersion("sonar-version-" + codeAnalysis.getId());

        codeAnalysisByServiceName.put(service.getServiceName(), codeAnalysis);

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystemDTO);

        when(this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(service.getId())).thenReturn(new CodeAnalysis[]{codeAnalysis});
        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        Assertions.assertEquals(QualityConstants.SQA_OK, releaseVersionCodeAnalyses.getSqaState());
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(1, releaseVersionCodeAnalyses.getSubsystems().length);

        //validation subsystems
        Mockito.verify(this.qualityAssuranceClient).getCodeAnalysesByReleaseVersionService(service.getId());
        this.assertSubsystemCodeAnalysesByReleaseVersionCodeAnalyses(Map.of(subsystemDTO.getSubsystemName(), QualityConstants.SQA_OK),
                codeAnalysisByServiceName, List.of(subsystemDTO), releaseVersion.getSubsystems(), releaseVersionCodeAnalyses);
    }

    @Test
    void getReleaseVersionCodeAnalysesByServicesValidateQACodeAnalysisRequestAllOk()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);
        product.setQualityLevel(QualityLevel.HIGH.getValue());
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(++id);
        subsystem.setSubsystemId(id);
        subsystem.setReleaseVersion(releaseVersion);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl("tag-url-" + subsystem.getSubsystemId());
        subsystem.setCompilationJobName("compilationJob-" + +subsystem.getSubsystemId());
        releaseVersion.setSubsystems(List.of(subsystem));

        TOSubsystemDTO subsystemDTO = getSubSystemDTOBy(subsystem);

        ReleaseVersionService service;
        CodeAnalysis codeAnalysis;
        Map<String, CodeAnalysis> codeAnalysisByServiceName = new HashMap<>();
        for (int i = 0; i < 3; i++)
        {
            service = new ReleaseVersionService();
            service.setId(++id);
            service.setServiceName("service-" + id);
            service.setVersion("service.v1.0." + id);
            service.setServiceType(ServiceType.NOVA.getServiceType());

            subsystem.setServices(List.of(service));
            service.setVersionSubsystem(subsystem);

            codeAnalysis = new CodeAnalysis();
            codeAnalysis.setId(++id);
            codeAnalysis.setProjectId(++id);
            codeAnalysis.setProjectName("project-" + service.getServiceName());
            codeAnalysis.setAnalysisDate(System.currentTimeMillis());
            codeAnalysis.setSqaState(QualityConstants.SQA_OK);
            codeAnalysis.setLanguage("JAVA");
            codeAnalysis.setHasSonarReport(true);
            codeAnalysis.setSonarState("sonar-state-" + codeAnalysis.getId());
            codeAnalysis.setSonarVersion("sonar-version-" + codeAnalysis.getId());

            codeAnalysisByServiceName.put(service.getServiceName(), codeAnalysis);
            when(this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(service.getId())).thenReturn(new CodeAnalysis[]{codeAnalysis});
        }

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystemDTO);

        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        Assertions.assertEquals(QualityConstants.SQA_OK, releaseVersionCodeAnalyses.getSqaState());
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(1, releaseVersionCodeAnalyses.getSubsystems().length);

        //validation subsystems
        this.assertSubsystemCodeAnalysesByReleaseVersionCodeAnalyses(Map.of(subsystemDTO.getSubsystemName(), QualityConstants.SQA_OK),
                codeAnalysisByServiceName, List.of(subsystemDTO), releaseVersion.getSubsystems(), releaseVersionCodeAnalyses);
    }

    @Test
    void getReleaseVersionCodeAnalysesByServicesValidateQACodeAnalysisRequestAllOKOneOk()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        product.setId(++id);
        product.setQualityLevel(QualityLevel.HIGH.getValue());
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());

        Release release = new Release();
        release.setId(++id);
        release.setProduct(product);
        product.setReleases(List.of(release));

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(++id);
        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setSubsystems(new ArrayList<>());
        release.setReleaseVersions(List.of(releaseVersion));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(++id);
        subsystem.setSubsystemId(id);
        subsystem.setReleaseVersion(releaseVersion);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl("tag-url-" + subsystem.getSubsystemId());
        subsystem.setCompilationJobName("compilationJob-" + +subsystem.getSubsystemId());
        subsystem.setServices(new ArrayList<ReleaseVersionService>());
        releaseVersion.setSubsystems(List.of(subsystem));

        TOSubsystemDTO subsystemDTO = getSubSystemDTOBy(subsystem);

        ReleaseVersionService service;
        CodeAnalysis codeAnalysis;
        Map<String, CodeAnalysis> codeAnalysisByServiceName = new HashMap<>();
        for (int i = 0; i < 3; i++)
        {
            service = new ReleaseVersionService();
            service.setId(++id);
            service.setServiceName("service-" + id);
            service.setVersion("service.v1.0." + id);
            service.setServiceType(ServiceType.NOVA.getServiceType());

            subsystem.getServices().add(service);
            service.setVersionSubsystem(subsystem);

            codeAnalysis = new CodeAnalysis();
            codeAnalysis.setId(++id);
            codeAnalysis.setProjectId(++id);
            codeAnalysis.setProjectName("project-" + service.getServiceName());
            codeAnalysis.setAnalysisDate(System.currentTimeMillis());
            codeAnalysis.setSqaState(QualityConstants.SQA_OK);
            codeAnalysis.setLanguage("JAVA");
            codeAnalysis.setHasSonarReport(true);
            codeAnalysis.setSonarState("sonar-state-" + codeAnalysis.getId());
            codeAnalysis.setSonarVersion("sonar-version-" + codeAnalysis.getId());

            codeAnalysisByServiceName.put(service.getServiceName(), codeAnalysis);
            when(this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(service.getId())).thenReturn(new CodeAnalysis[]{codeAnalysis});
        }

        //Edit one codeAnalysis to set ERROR
        codeAnalysisByServiceName.values().stream().findFirst().ifPresent(ca -> ca.setSqaState(QualityConstants.SQA_ERROR));

        when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
        when(this.toolsService.getSubsystemById(subsystem.getSubsystemId())).thenReturn(subsystemDTO);

        ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses = qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersion.getId());

        Assertions.assertNotNull(releaseVersionCodeAnalyses);
        //FIXME : this test fail randomly (because  codeAnalysisByServiceName.values().stream().findFirst() sometimes get last)
        Assertions.assertEquals(QualityConstants.SQA_ERROR, releaseVersionCodeAnalyses.getSqaState(), "With one services with KO analysis, the complete result of the subsystem has to be ERROR");
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(1, releaseVersionCodeAnalyses.getSubsystems().length);

        //validation subsystems
        this.assertSubsystemCodeAnalysesByReleaseVersionCodeAnalyses(Map.of(subsystemDTO.getSubsystemName(), QualityConstants.SQA_ERROR),
                codeAnalysisByServiceName, List.of(subsystemDTO), releaseVersion.getSubsystems(), releaseVersionCodeAnalyses);


    }

    @Test
    void getCodeAnalysisStatusesNull()
    {
        Assertions.assertNotNull(qualityManagerService.getCodeAnalysisStatuses(null));
    }

    @Test
    void getCodeAnalysisStatusesEmpty()
    {
        Assertions.assertNotNull(qualityManagerService.getCodeAnalysisStatuses(new int[0]));
    }

    @Test
    void getCodeAnalysisStatusesNotFound()
    {
        int id = -1;
        when(this.releaseVersionRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertNotNull(qualityManagerService.getCodeAnalysisStatuses(new int[]{id}));
    }

    @Test
    void getCodeAnalysisStatusesNoAvailable()
    {
        List<ReleaseVersion> releaseVersions = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            int id = i + 1;
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setId(id);
            releaseVersion.setQualityValidation(null);
            releaseVersions.add(releaseVersion);
            when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
            CodeAnalysisStatus[] codeAnalysisStatuses = qualityManagerService.getCodeAnalysisStatuses(releaseVersions.stream().mapToInt(AbstractEntity::getId).toArray());
            Assertions.assertNotNull(codeAnalysisStatuses);
            Assertions.assertEquals(i + 1, codeAnalysisStatuses.length);
            Assertions.assertEquals(QualityConstants.SQA_NOT_AVAILABLE, codeAnalysisStatuses[i].getCodeAnalysisStatus());
        }
    }

    @Test
    void getCodeAnalysisStatusesSQAError()
    {
        List<ReleaseVersion> releaseVersions = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            int id = i + 1;
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setId(id);
            releaseVersion.setQualityValidation(false);
            releaseVersions.add(releaseVersion);
            when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
            CodeAnalysisStatus[] codeAnalysisStatuses = qualityManagerService.getCodeAnalysisStatuses(releaseVersions.stream().mapToInt(AbstractEntity::getId).toArray());
            Assertions.assertNotNull(codeAnalysisStatuses);
            Assertions.assertEquals(i + 1, codeAnalysisStatuses.length);
            Assertions.assertEquals(QualityConstants.SQA_ERROR, codeAnalysisStatuses[i].getCodeAnalysisStatus());
        }
    }

    @Test
    void getCodeAnalysisStatusesSQAOk()
    {
        List<ReleaseVersion> releaseVersions = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            int id = i + 1;
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setId(id);
            releaseVersion.setQualityValidation(true);
            releaseVersions.add(releaseVersion);
            when(this.releaseVersionRepository.findById(releaseVersion.getId())).thenReturn(Optional.of(releaseVersion));
            CodeAnalysisStatus[] codeAnalysisStatuses = qualityManagerService.getCodeAnalysisStatuses(releaseVersions.stream().mapToInt(AbstractEntity::getId).toArray());
            Assertions.assertNotNull(codeAnalysisStatuses);
            Assertions.assertEquals(i + 1, codeAnalysisStatuses.length);
            Assertions.assertEquals(QualityConstants.SQA_OK, codeAnalysisStatuses[i].getCodeAnalysisStatus());
        }
    }

    @Test
    void removeQualityInfoNoSubsystem()
    {
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));
        this.qualityManagerService.removeQualityInfo(rv);
        Mockito.verify(qualityAssuranceClient, Mockito.never()).removeCodeAnalyses(Mockito.anyInt());
        Mockito.verify(qualityAssuranceClient, Mockito.only()).deletePerformanceReportsByReleaseVersion(rv.getId());
    }

    @Test
    void removeQualityInfoSubsystemNoServices()
    {
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));
        rv.setSubsystems(List.of(new ReleaseVersionSubsystem()));
        this.qualityManagerService.removeQualityInfo(rv);
        Mockito.verify(qualityAssuranceClient, Mockito.never()).removeCodeAnalyses(Mockito.anyInt());
        Mockito.verify(qualityAssuranceClient, Mockito.only()).deletePerformanceReportsByReleaseVersion(rv.getId());
    }

    @Test
    //TODO@Manu: eliminar ignore una vez corregido el test
    @Ignore
    void removeQualityInfoSubsystemWithServiceNoAnalyses()
    {
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(RandomUtils.nextInt(1, 100));
        rv.setSubsystems(List.of(subsystem));

        ReleaseVersionService serviceNoAnalyses = new ReleaseVersionService();
        serviceNoAnalyses.setId(RandomUtils.nextInt(1, 1000));
        subsystem.setServices(List.of(serviceNoAnalyses));

        //List of service without analyses --> no has to remove!
        for (ServiceType serviceType : SERVICE_TYPES_WITHOUT_ANALYSES)
        {
            //set the type
            serviceNoAnalyses.setServiceType(serviceType.getServiceType());
            Mockito.reset(qualityAssuranceClient);
            this.qualityManagerService.removeQualityInfo(rv);
            //TODO@Manu revisar este parte
            //Mockito.verify(qualityAssuranceClient, Mockito.never()).removeCodeAnalyses(Mockito.anyInt());
            //Mockito.verify(qualityAssuranceClient, Mockito.only()).deletePerformanceReportsByReleaseVersion(rv.getId());
        }
    }

    @Test
    void removeQualityInfoSubsystemWithServiceWithAnalyses()
    {
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));

        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(RandomUtils.nextInt(1, 100));
        rv.setSubsystems(List.of(subsystem));

        ReleaseVersionService service = new ReleaseVersionService();
        subsystem.setServices(List.of(service));

        //List of service without analyses --> no has to remove!
        int id = 0;
        for (ServiceType serviceType : ServiceType.values())
        {
            //Avoid
            if (SERVICE_TYPES_WITHOUT_ANALYSES.contains(serviceType))
            {
                continue;
            }

            //set the type
            service.setServiceType(serviceType.getServiceType());
            service.setId(++id);
            Mockito.reset(qualityAssuranceClient);
            this.qualityManagerService.removeQualityInfo(rv);
            Mockito.verify(qualityAssuranceClient, Mockito.times(1)).removeCodeAnalyses(service.getId());
            Mockito.verify(qualityAssuranceClient, Mockito.times(1)).deletePerformanceReportsByReleaseVersion(rv.getId());
        }
    }

    @Test
    void removeQualityInfo()
    {
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));

        List<ReleaseVersionSubsystem> subsystemList = new ArrayList<>();
        rv.setSubsystems(subsystemList);

        //List of all service autogenerated
        List<ReleaseVersionService> expectedServicesList = new ArrayList<>();

        ReleaseVersionSubsystem subsystem;
        ReleaseVersionService service;
        int serviceId = 0;
        for (int i = 0; i < 3; i++)
        {
            subsystem = new ReleaseVersionSubsystem();
            subsystem.setId(i);
            subsystem.setServices(new ArrayList<>());
            for (int j = 0; j < i + 1; j++)
            {
                service = new ReleaseVersionService();
                service.setId(++serviceId);
                //Set a valid type with code analyses
                service.setServiceType(ServiceType.NOVA.getServiceType());
                subsystem.getServices().add(service);

                //add to expected list
                expectedServicesList.add(service);
            }

            subsystemList.add(subsystem);
        }


        this.qualityManagerService.removeQualityInfo(rv);

        //check than all services has been passes to remove code analyses
        for (ReleaseVersionService rvs : expectedServicesList)
        {
            Mockito.verify(qualityAssuranceClient, Mockito.times(1)).removeCodeAnalyses(rvs.getId());
        }

        //Verify that only one times is delete ReleaseVersion
        Mockito.verify(qualityAssuranceClient, Mockito.times(1)).deletePerformanceReportsByReleaseVersion(rv.getId());
    }

    @Test
    void setPerformanceReportDeploymentPlanNotFound()
    {
        int planId = -1;
        PerformanceReport performanceReport = new PerformanceReport();
        when(this.planRepository.findById(planId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NovaException.class,
                () -> qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport),
                "DeploymentPlan not found has to throw a NOVA Exception");

        //Validate NovaException values
        this.assertNovaException(() -> qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport),
                QualityConstants.QualityManagerErrors.PLAN_NOT_FOUND_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, "", Constants.MSG_CONTACT_NOVA);
    }

    @Test
    void setPerformanceReportDeploymentPlanNOTDeployedInPRE()
    {
        PerformanceReport performanceReport = new PerformanceReport();
        int planId = RandomUtils.nextInt(1, 1000);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(planId);
        when(this.planRepository.findById(planId)).thenReturn(Optional.of(plan));

        for (DeploymentStatus deploymentStatus : DeploymentStatus.values())
        {
            for (Environment environment : Environment.values())
            {
                if (deploymentStatus == DeploymentStatus.DEPLOYED && environment == Environment.PRE)
                {
                    //only good case, ignore
                    continue;
                }
                plan.setEnvironment(environment.getEnvironment());
                plan.setStatus(deploymentStatus);

                Assertions.assertThrows(NovaException.class,
                        () -> qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport),
                        "It is not possible to set PerformanceReport in a DeploymentPlan with status "
                                + deploymentStatus + " in environment " + environment + ". It has to throw a NOVA Exception");

            }
        }

        //Validate NovaException values
        plan.setEnvironment(Environment.INT.getEnvironment());
        plan.setStatus(DeploymentStatus.REJECTED);
        this.assertNovaException(() -> qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport),
                QualityConstants.QualityManagerErrors.PLAN_NOT_DEPLOYED_ON_PRE_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, "", Constants.MSG_CONTACT_NOVA);
    }

    @Test
    void setPerformanceReportDeploymentPlan()
    {
        PerformanceReport performanceReport = new PerformanceReport();
        performanceReport.setLink("link-" + RandomStringUtils.randomAlphabetic(10));
        performanceReport.setRiskLevel(RandomUtils.nextInt(10, 100));
        performanceReport.setDescription("description" + RandomStringUtils.randomAlphabetic(10));

        int planId = RandomUtils.nextInt(1, 1000);
        ReleaseVersion rv = new ReleaseVersion();
        rv.setId(RandomUtils.nextInt(1, 1000));
        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(planId);
        //set deployed in pre
        plan.setEnvironment(Environment.PRE.getEnvironment());
        plan.setStatus(DeploymentStatus.DEPLOYED);
        plan.setReleaseVersion(rv);


        final ArgumentCaptor<QAPerformanceReport> qaPRprtArgumentCaptor
                = ArgumentCaptor.forClass(QAPerformanceReport.class);

        when(this.planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(this.docSystemRepo.findByUrlAndCategoryAndType(performanceReport.getLink(), DocumentCategory.PERFORMANCE_REPORTS, DocumentType.FILE)).thenReturn(Optional.empty());
        qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport);

        Mockito.verify(qualityAssuranceClient, Mockito.times(1))
                .setPerformanceReport(qaPRprtArgumentCaptor.capture(), Mockito.eq(planId), Mockito.eq(rv.getId()));

        QAPerformanceReport current = qaPRprtArgumentCaptor.getValue();
        Assertions.assertEquals(performanceReport.getDescription(), current.getDescription());
        Assertions.assertEquals(performanceReport.getLink(), current.getLink());
        Assertions.assertEquals(performanceReport.getRiskLevel(), current.getRiskLevel());
    }


    @Test
    void deletePerformanceReportNoFound()
    {
        int planId = -1;
        when(planRepository.findById(planId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NovaException.class, () -> qualityManagerService.deletePerformanceReport(ivUser, planId));
        Mockito.verify(qualityAssuranceClient, Mockito.never()).deletePerformanceReport(planId);

        this.assertNovaException(() -> qualityManagerService.deletePerformanceReport(ivUser, planId),
                QualityConstants.QualityManagerErrors.PLAN_NOT_FOUND_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, Constants.MSG_CONTACT_NOVA);
    }

    @Test
    void deletePerformanceReport()
    {
        int planId = -1;
        when(planRepository.findById(planId)).thenReturn(Optional.of(new DeploymentPlan()));
        qualityManagerService.deletePerformanceReport(ivUser, planId);
        Mockito.verify(qualityAssuranceClient, Mockito.only()).deletePerformanceReport(planId);
    }

    @Test
    void getPerformanceReportsNotFound()
    {
        int releaseVersionId = -1;
        when(releaseVersionRepository.findById(releaseVersionId)).thenReturn(Optional.empty());
        Assertions.assertThrows(NovaException.class, () -> qualityManagerService.getPerformanceReports(releaseVersionId));
        Mockito.verify(qualityAssuranceClient, Mockito.never()).getPerformanceReports(releaseVersionId);

        this.assertNovaException(() -> qualityManagerService.getPerformanceReports(releaseVersionId),
                QualityConstants.QualityManagerErrors.QM_INVALID_RELEASE_VERSION_ERROR_CODE,
                ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST, NO_CHECK, NO_CHECK);
    }

    @Test
    void getPerformanceReports()
    {
        int releaseVersionId = RandomUtils.nextInt(1, 1000);
        when(releaseVersionRepository.findById(releaseVersionId)).thenReturn(Optional.of(new ReleaseVersion()));
        PlanPerformanceReport[] expected = new PlanPerformanceReport[0];
        when(qualityAssuranceClient.getPerformanceReports(releaseVersionId)).thenReturn(expected);
        PlanPerformanceReport[] current = qualityManagerService.getPerformanceReports(releaseVersionId);
        Mockito.verify(qualityAssuranceClient, Mockito.only()).getPerformanceReports(releaseVersionId);
        Assertions.assertEquals(expected, current);
    }

    // ---------------------------------------------------
    //              Private methods
    // ---------------------------------------------------

    /**
     * Assertions to check QAEphoenixJobParametersDTO values
     *
     * @param subsystem             original subsystem
     * @param product               original product
     * @param branchName            original branch name
     * @param ephoenixJobParameters instance to Check
     */
    private void assertQAEphoenixJobParameters(final TOSubsystemDTO subsystem, final Product product, final String branchName, final QAEphoenixJobParametersDTO ephoenixJobParameters)
    {
        //Build a map with expected param to jenkins
        Map<String, String> expectedParamMap = new HashMap<>();
        expectedParamMap.put(Constants.PROJECT_UUAA_PARAM, product.getUuaa());
        expectedParamMap.put(Constants.SUBSYSTEM_NAME_PARAM, subsystem.getSubsystemName());
        expectedParamMap.put(Constants.TAG_PARAM, branchName);
        expectedParamMap.put(Constants.RELEASE_NAME_PARAM, QualityConstants.SQA);
        expectedParamMap.put(Constants.RELEASE_VERSION_PARAM, QualityConstants.SQA);

        //Validations for QAEphoenixJobParametersDTO
        Assertions.assertNotNull(ephoenixJobParameters.getJobParameters());
        Assertions.assertEquals(expectedParamMap.size(), ephoenixJobParameters.getJobParameters().length,
                "Different number of params to job. Expected " + expectedParamMap + " but was "
                        + Arrays.toString(ephoenixJobParameters.getJobParameters()));

        String expectedValue;
        for (QAEphoenixJobParameter param : ephoenixJobParameters.getJobParameters())
        {
            expectedValue = expectedParamMap.get(param.getKey());
            Assertions.assertNotNull(expectedValue, "Param");
            Assertions.assertEquals(expectedValue, param.getValue(),
                    "Not expected value for ePoenix param " + param.getKey()
                            + ". expected  [" + expectedValue + "] but was [" + param.getValue() + "]");
        }
    }

    /**
     * Assertions to check QAEphoenixJobParametersDTO values
     *
     * @param expectedList               original NewReleaseVersionServiceDto list
     * @param qaEphoenixJobParametersDTO QAEphoenixJobParametersDTO to validate
     */
    private void assertQAEphoenixJobEphoenixServicesInQAEphoenixJobParametersDTO(final List<NewReleaseVersionServiceDto> expectedList, final QAEphoenixJobParametersDTO qaEphoenixJobParametersDTO)
    {
        Assertions.assertNotNull(qaEphoenixJobParametersDTO, "QAEphoenixJobParametersDTO can not be null");
        QAEphoenixServiceDTO[] ephoenixServiceDTOs = qaEphoenixJobParametersDTO.getEphoenixService();
        Assertions.assertNotNull(ephoenixServiceDTOs, "QAEphoenixServiceDTO array cannot be null");
        Assertions.assertEquals(expectedList.size(), ephoenixServiceDTOs.length, "Ephoenix Services number does not match");

        NewReleaseVersionServiceDto expected;
        QAEphoenixServiceDTO current;
        Map<String, String> expectedParamMap;

        for (int i = 0; i < ephoenixServiceDTOs.length; i++)
        {
            expected = expectedList.get(i);
            current = ephoenixServiceDTOs[i];

            Assertions.assertNotNull(current);
            Assertions.assertNotNull(current.getJobParameters());
            //current version use 4 params: instance name, type, image name and uuaa
            Assertions.assertEquals(4, current.getJobParameters().length);
            expectedParamMap = Arrays.stream(current.getJobParameters()).collect(Collectors.toMap(QAEphoenixJobParameter::getKey, QAEphoenixJobParameter::getValue));

            Assertions.assertEquals(expected.getArtifactId(), expectedParamMap.get(Constants.INSTANCE_NAME_PARAM));
            Assertions.assertEquals(expected.getServiceType().toUpperCase(), expectedParamMap.get(Constants.INSTANCE_TYPE_PARAM));
            Assertions.assertEquals(QualityConstants.SQA, expectedParamMap.get(Constants.IMAGE_NAME_PARAM));
            Assertions.assertEquals(expected.getUuaa(), expectedParamMap.get(Constants.INSTANCE_UUAA_PARAM));
        }
    }

    /**
     * Check values for expected NovaException
     *
     * @param runnable             runnable action to launch nova Exception
     * @param expectedErrorCode    expected ErrorCode
     * @param expectedErrorMsgType expected ErrorMessageType
     * @param expectedHttpStatus   expected HttpStatus
     * @param expectedMessage      expected message if it is present
     * @param expectedAction       expected action message if it is present
     */
    private void assertNovaException(final Runnable runnable, final String expectedErrorCode, final ErrorMessageType expectedErrorMsgType, final HttpStatus expectedHttpStatus, final String expectedMessage, final String expectedAction)
    {
        //Check exception information
        try
        {
            //into runnable is the method to launch an exception
            runnable.run();
            Assertions.fail("Expected NovaException");
        }
        catch (Exception e)
        {
            Assertions.assertTrue(e instanceof NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(expectedErrorCode, novaError.getErrorCode(), "Incorrect ErrorCode ");
            Assertions.assertNotNull(novaError.getErrorMessage());
            if (StringUtils.isNotEmpty(expectedMessage))
            {
                Assertions.assertEquals(expectedMessage, novaError.getErrorMessage(), "Incorrect ErrorMessage");
            }
            Assertions.assertNotNull(novaError.getActionMessage());
            if (StringUtils.isNotEmpty(expectedAction))
            {
                Assertions.assertEquals(expectedAction, novaError.getActionMessage(), "Incorrect ActionMessage");
            }
            Assertions.assertEquals(expectedErrorMsgType, novaError.getErrorMessageType(), "Incorrect ErrorMsgType");
            Assertions.assertEquals(expectedHttpStatus, novaError.getHttpStatus(), "Incorrect HttpStatus");
        }
    }

    /**
     * ssertions to check QAEphoenixJobParametersDTO values  subsystemCodeAnalyses array from ReleaseVersionCodeAnalyses
     *
     * @param expectedSQAStateBySubsystemName expected SQA state by subsystems
     * @param codeAnalysisByServiceName       mpa of expected code analisys by servicename
     * @param subsystemDTOList                list of subsystemDTO original
     * @param rvSubsystems                    list of  ReleaseVersionSubsystems original
     * @param releaseVersionCodeAnalyses      analysis to check
     */
    private void assertSubsystemCodeAnalysesByReleaseVersionCodeAnalyses(final Map<String, String> expectedSQAStateBySubsystemName,
                                                                         final Map<String, CodeAnalysis> codeAnalysisByServiceName,
                                                                         final List<TOSubsystemDTO> subsystemDTOList,
                                                                         final List<ReleaseVersionSubsystem> rvSubsystems,
                                                                         final ReleaseVersionCodeAnalyses releaseVersionCodeAnalyses)
    {
        Assertions.assertNotNull(releaseVersionCodeAnalyses.getSubsystems());
        Assertions.assertEquals(subsystemDTOList.size(), releaseVersionCodeAnalyses.getSubsystems().length);

        SubsystemCodeAnalyses current;
        TOSubsystemDTO subsystemDTO;
        ReleaseVersionSubsystem rvSubsystem;
        for (int i = 0; i < releaseVersionCodeAnalyses.getSubsystems().length; i++)
        {
            current = releaseVersionCodeAnalyses.getSubsystems()[i];
            subsystemDTO = subsystemDTOList.get(i);
            rvSubsystem = rvSubsystems.get(i);
            Assertions.assertEquals(rvSubsystem.getTagName(), current.getTag());
            Assertions.assertEquals(subsystemDTO.getSubsystemName(), current.getSubsystemName());
            Assertions.assertEquals(subsystemDTO.getSubsystemType(), current.getSubsystemType());
            Assertions.assertEquals(expectedSQAStateBySubsystemName.get(subsystemDTO.getSubsystemName()), current.getSqaState());

            assertServiceCodeAnalysesBySubsystemCodeAnalyses(codeAnalysisByServiceName, rvSubsystem.getServices(), current);
        }
    }

    private void assertServiceCodeAnalysesBySubsystemCodeAnalyses(final Map<String, CodeAnalysis> codeAnalysisByServiceName, final List<ReleaseVersionService> rvSubsystemServices, final SubsystemCodeAnalyses subsystemCodeAnalyses)
    {
        Assertions.assertNotNull(subsystemCodeAnalyses.getServices());
        Assertions.assertEquals(rvSubsystemServices.size(), subsystemCodeAnalyses.getServices().length);

        ReleaseVersionService rvs;
        ServiceCodeAnalyses current;
        CodeAnalysis expectedCodeAnalysis;
        CodeAnalysis currentCodeAnalysis;
        for (int i = 0; i < subsystemCodeAnalyses.getServices().length; i++)
        {
            current = subsystemCodeAnalyses.getServices()[i];
            rvs = rvSubsystemServices.get(i);

            Assertions.assertEquals(rvs.getServiceName(), current.getServiceName());
            Assertions.assertEquals(rvs.getServiceType(), current.getServiceType());
            Assertions.assertEquals(rvs.getVersion(), current.getServiceVersion());

            expectedCodeAnalysis = codeAnalysisByServiceName.get(current.getServiceName());
            Assertions.assertNotNull(expectedCodeAnalysis);
            Assertions.assertNotNull(current.getAnalysis());
            Assertions.assertEquals(1, current.getAnalysis().length);
            currentCodeAnalysis = current.getAnalysis()[0];
            Assertions.assertEquals(expectedCodeAnalysis.getAnalysisDate(), currentCodeAnalysis.getAnalysisDate());
            Assertions.assertEquals(expectedCodeAnalysis.getId(), currentCodeAnalysis.getId());
            Assertions.assertEquals(expectedCodeAnalysis.getLanguage(), currentCodeAnalysis.getLanguage());
            Assertions.assertEquals(expectedCodeAnalysis.getSqaState(), currentCodeAnalysis.getSqaState());
            Assertions.assertEquals(expectedCodeAnalysis.getProjectId(), currentCodeAnalysis.getProjectId());
            Assertions.assertEquals(expectedCodeAnalysis.getProjectName(), currentCodeAnalysis.getProjectName());
            Assertions.assertEquals(expectedCodeAnalysis.getSonarState(), currentCodeAnalysis.getSonarState());
            Assertions.assertEquals(expectedCodeAnalysis.getSonarVersion(), currentCodeAnalysis.getSonarVersion());

        }
    }

    /**
     * Create a Random TOSubsystemDTO
     *
     * @return TOSubsystemDTO randomly initialized
     */
    private TOSubsystemDTO getRandomSubsystemDTO()
    {
        TOSubsystemDTO subsystem = new TOSubsystemDTO();
        subsystem.setSubsystemId(RandomUtils.nextInt(1, 1000));
        subsystem.setRepoId(RandomUtils.nextInt(1, 1000));
        subsystem.setSubsystemName("subsystem-" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setSubsystemType(SubsystemType.NOVA.getType());
        subsystem.setDescription("description-" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setUrl("url-" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setQualityUrl("quality-url-" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setJenkinsUrl("jenkins-url-" + RandomStringUtils.randomAlphabetic(10));

        return subsystem;
    }

    /**
     * Create a Random TOSubsystemDTO
     *
     * @return TOSubsystemDTO randomly initialized
     */
    private ReleaseVersionSubsystem getReleaseVersionSubsystemBy(TOSubsystemDTO dto, ReleaseVersion rv)
    {
        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setId(dto.getSubsystemId());
        subsystem.setSubsystemId(dto.getSubsystemId());
        subsystem.setReleaseVersion(rv);
        subsystem.setTagName("tag-name-" + subsystem.getSubsystemId());
        subsystem.setTagUrl(dto.getUrl());
        subsystem.setServices(new ArrayList<>());
        return subsystem;
    }

    private Product generateRandomProductFromSubsysytem(final TOSubsystemDTO subsystem)
    {
        Product product = new Product();
        product.setId(subsystem.getResourceId());
        product.setProductStatus(ProductStatus.READY);
        String key = RandomStringUtils.randomAlphabetic(10);
        product.setName("product" + key);
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());
        product.setDescription("description: " + key);
        product.setCriticalityLevel(RandomUtils.nextInt(0, 5));
        product.setQualityLevel(QualityLevel.values()[RandomUtils.nextInt(0, QualityLevel.values().length - 1)].getValue());
        product.setDesBoard("desboard-" + key);
        product.setType("NOVA");
        product.setRemedySupportGroup("remedy-" + key);
        product.setPhone(RandomStringUtils.randomNumeric(5));
        product.setImage("logo");
        product.setProductStatus(ProductStatus.READY);
        product.setDefaultAutodeployInPre(RandomUtils.nextBoolean());
        product.setDefaultAutodeployInPro(RandomUtils.nextBoolean());
        product.setDefaultAutomanageInPre(RandomUtils.nextBoolean());
        product.setDefaultAutomanageInPro(RandomUtils.nextBoolean());

        product.setDefaultDeploymentTypeInPro(DeploymentType.values()[RandomUtils.nextInt(0, DeploymentType.values().length)]);

        product.setReleases(new ArrayList<>());
        product.setCategories(new ArrayList<>());
        product.setDocSystems(new ArrayList<>());
        product.setLogicalConnectors(new ArrayList<>());
        product.setFilesystems(new ArrayList<>());
        return product;
    }

    /**
     * Get TOSubsystemDTO from ReleaseVersionSubsystem
     *
     * @param rvSubsystem original ReleaseVersionSubsystem
     * @return TOSubsystemDTO
     */
    private TOSubsystemDTO getSubSystemDTOBy(final ReleaseVersionSubsystem rvSubsystem)
    {

        TOSubsystemDTO subsystem = new TOSubsystemDTO();
        subsystem.setRepoId(rvSubsystem.getId());
        subsystem.setSubsystemId(rvSubsystem.getSubsystemId());
        subsystem.setSubsystemName("subsystem-" + rvSubsystem.getSubsystemId());
        subsystem.setSubsystemType(SubsystemType.NOVA.getType());
        subsystem.setDescription("description-" + rvSubsystem.getSubsystemId());
        subsystem.setUrl(rvSubsystem.getTagUrl());
        subsystem.setQualityUrl("quality-url-" + rvSubsystem.getSubsystemId());
        subsystem.setJenkinsUrl("jenkins-url-" + rvSubsystem.getSubsystemId());

        return subsystem;
    }
}

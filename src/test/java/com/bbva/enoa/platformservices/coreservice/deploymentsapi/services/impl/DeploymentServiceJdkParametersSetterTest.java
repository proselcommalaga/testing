package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkParameterProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceAllowedJdkParameterValueRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

//@RunWith(MockitoJUnitRunner.class)
public class DeploymentServiceJdkParametersSetterTest
{
    private static final Environment VALID_ENVIRONMENT = Environment.PRE;
    private static final Environment INVALID_ENVIRONMENT = Environment.PRO;
    private static final Object[][] JDK_PARAMETERS = new Object[][]{
            new Object[]{10, "-XX:+UseSerialGC", true},
            new Object[]{11, "-XX:+UseParallelGC", false},
            new Object[]{12, "-XX:+UseParNewGC", false},
            new Object[]{13, "-XX:NumberOfGCLogFiles=10", true},
            new Object[]{14, "-XX:NumberOfGCLogFiles=20", false},
            new Object[]{18, "-XX:+OptimizeStringConcat", false},
            new Object[]{17, "-XX:+UseCompressedStrings", false},
            new Object[]{16, "-XX:+UseStringCache", false},
            new Object[]{15, "-XX:+UseStringDeduplication", false}
    };
    private List<AllowedJdkParameterProduct> storedItems;
    @Mock
    private AllowedJdkParameterProductRepository allowedJdkParameterProductRepository;
    @Mock
    private JvmJdkConfigurationChecker jvmJdkChecker;
    @Mock
    private DeploymentServiceAllowedJdkParameterValueRepository paramValueRepository;
    @InjectMocks
    private DeploymentServiceJdkParametersSetter setter;
    private final DeploymentServiceDto deploymentServiceDto = new DeploymentServiceDto();

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(DeploymentServiceJdkParametersSetter.class);
        deploymentServiceDto.setMemoryFactor(75);
        deploymentServiceDto.setDeploymentPlanId(1);
    }
/*
    @Test
    public void when_typed_parameters_are_null_then_do_nothing()
    {
        setter.setJvmOptionsForDeploymentService(getDummyDeploymentService(VALID_ENVIRONMENT), null, null);

        Mockito.verify(jvmJdkChecker, Mockito.times(0)).isMultiJdk(Mockito.any(ReleaseVersionService.class));
    }

    @Test
    public void when_nested_typed_parameters_are_null_then_do_nothing()
    {
        setter.setJvmOptionsForDeploymentService(getDummyDeploymentService(VALID_ENVIRONMENT), deploymentServiceDto, new JdkTypedParametersDto());

        Mockito.verify(jvmJdkChecker, Mockito.times(0)).isMultiJdk(Mockito.any(ReleaseVersionService.class));
    }

    @Test
    public void when_nested_typed_parameters_have_null_parameters_then_add_nothing() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenReturn(true);

        setter.setJvmOptionsForDeploymentService(getDummyDeploymentService(VALID_ENVIRONMENT), deploymentServiceDto, getParsedResponse("jdk_parameters_response_with_null_parameters.json"));

        Mockito.verify(paramValueRepository, Mockito.times(0)).findByDeploymentServiceId(Mockito.anyInt());
    }

    @Test
    public void when_nested_typed_parameters_have_empty_parameters_then_add_nothing() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenReturn(true);

        DeploymentService deploymentService = getDummyDeploymentService(VALID_ENVIRONMENT);
        deploymentService.getDeploymentSubsystem().getDeploymentPlan().setParent(new DeploymentPlan());
        setter.setJvmOptionsForDeploymentService(deploymentService, deploymentServiceDto, getParsedResponse("jdk_parameters_response_with_empty_parameters.json"));

        verify(paramValueRepository, Mockito.times(0)).findByDeploymentServiceId(anyInt());
    }



    //@Test(expected = NovaException.class)
    public void when_deployment_service_validation_throws_exception_then_throw_exception() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenThrow(new NovaException(DeploymentError.getDeploymentServiceNotJdkParameterizableError(1)));

        DeploymentService deploymentService = getDummyDeploymentService(INVALID_ENVIRONMENT);
        deploymentService.getDeploymentSubsystem().getDeploymentPlan().setParent(new DeploymentPlan());
        setter.setJvmOptionsForDeploymentService(getDummyDeploymentService(VALID_ENVIRONMENT), deploymentServiceDto, getParsedResponse("jdk_parameters_response_without_selection.json"));

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_service_is_not_java_11_or_higher_then_do_not_save() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenReturn(false);

        DeploymentService deploymentService = getDummyDeploymentService(VALID_ENVIRONMENT);
        deploymentService.getDeploymentSubsystem().getDeploymentPlan().setParent(new DeploymentPlan());
        setter.setJvmOptionsForDeploymentService(deploymentService, deploymentServiceDto, getParsedResponse("jdk_parameters_response_without_selection.json"));

        Mockito.verify(allowedJdkParameterProductRepository, Mockito.times(0)).saveAll(Mockito.anyIterable());
    }

    @Test
    public void when_there_are_no_excluding_selected_values_then_select_default_parameters() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenReturn(true);
        Mockito.when(allowedJdkParameterProductRepository.findByIdIn(Mockito.anySet())).thenReturn(getDummyAllowedJdkParameterProducts());
        Mockito.when(allowedJdkParameterProductRepository.saveAll(Mockito.anyIterable())).thenAnswer((Answer<Void>) invocationOnMock -> {
            storedItems = invocationOnMock.getArgument(0);
            return null;
        });

        DeploymentService deploymentService = getDummyDeploymentService(VALID_ENVIRONMENT);
        deploymentService.getDeploymentSubsystem().getDeploymentPlan().setParent(new DeploymentPlan());
        setter.setJvmOptionsForDeploymentService(deploymentService, deploymentServiceDto, getParsedResponse("jdk_parameters_response_without_selection.json"));

        Mockito.verify(allowedJdkParameterProductRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
        Map<Integer, Integer> expectedResultsMap = new HashMap<>()
        {
            {
                put(10, 1);
                put(13, 1);
            }
        };
        final Set<Integer> storedIds = expectedResultsMap.keySet();
        for (final AllowedJdkParameterProduct storedItem : storedItems)
        {
            final Integer id = storedItem.getId();
            final List<DeploymentServiceAllowedJdkParameterValue> assignedParamValues = storedItem.getDeploymentServiceAllowedJdkParameterValues();
            if (storedIds.contains(id))
            {
                Assertions.assertEquals(expectedResultsMap.get(id).intValue(), assignedParamValues.size());
            }
            else
            {
                Assertions.assertEquals(0, assignedParamValues.size());
            }
        }
    }

    @Test
    public void when_exist_selected_and_not_default_excluding_value_then_apply_selected_value() throws IOException
    {
        Mockito.when(jvmJdkChecker.isMultiJdk(Mockito.any(ReleaseVersionService.class))).thenReturn(true);
        Mockito.when(allowedJdkParameterProductRepository.findByIdIn(Mockito.anySet())).thenReturn(getDummyAllowedJdkParameterProducts());
        Mockito.when(allowedJdkParameterProductRepository.saveAll(Mockito.anyIterable())).thenAnswer((Answer<Void>) invocationOnMock -> {
            storedItems = invocationOnMock.getArgument(0);
            return null;
        });

        DeploymentService deploymentService = getDummyDeploymentService(VALID_ENVIRONMENT);
        deploymentService.getDeploymentSubsystem().getDeploymentPlan().setParent(new DeploymentPlan());
        setter.setJvmOptionsForDeploymentService(deploymentService, deploymentServiceDto, getParsedResponse("jdk_parameters_response_with_selection.json"));

        Mockito.verify(allowedJdkParameterProductRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
        Map<Integer, Integer> expectedResultsMap = new HashMap<>()
        {
            {
                put(11, 1);
                put(14, 1);
                put(17, 1);
            }
        };
        final Set<Integer> storedIds = expectedResultsMap.keySet();
        for (final AllowedJdkParameterProduct storedItem : storedItems)
        {
            final Integer id = storedItem.getId();
            final List<DeploymentServiceAllowedJdkParameterValue> assignedParamValues = storedItem.getDeploymentServiceAllowedJdkParameterValues();
            if (storedIds.contains(id))
            {
                Assertions.assertEquals(expectedResultsMap.get(id).intValue(), assignedParamValues.size());
            }
            else
            {
                Assertions.assertEquals(0, assignedParamValues.size());
            }
        }
    }

    private DeploymentService getDummyDeploymentService(Environment environment)
    {
        DeploymentService service = new DeploymentService();
        service.setId(1);
        service.setService(getDummyReleaseVersionService());
        service.setDeploymentSubsystem(getDummyDeploymentSubsystem(environment));
        return service;
    }

    private DeploymentSubsystem getDummyDeploymentSubsystem(Environment environment)
    {
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(getDummyDeploymentPlan(environment));
        return deploymentSubsystem;
    }

    private DeploymentPlan getDummyDeploymentPlan(Environment environment)
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(environment);
        deploymentPlan.setReleaseVersion(getDummyReleaseVersion());
        return deploymentPlan;
    }

    private ReleaseVersionService getDummyReleaseVersionService()
    {
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setId(1);
        releaseVersionService.setVersionSubsystem(getDummyReleaseVersionSubsystem());
        return releaseVersionService;
    }

    private ReleaseVersionSubsystem getDummyReleaseVersionSubsystem()
    {
        ReleaseVersionSubsystem item = new ReleaseVersionSubsystem();
        item.setId(1);
        item.setReleaseVersion(getDummyReleaseVersion());
        return item;
    }

    private JdkTypedParametersDto getParsedResponse(String fileName) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        final InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("responses/" + fileName);
        if (fileStream == null)
        {
            return new JdkTypedParametersDto();
        }
        return mapper.readValue(new String(fileStream.readAllBytes()), JdkTypedParametersDto.class);
    }

    private AllowedJdkParameterProduct getDummyAllowedJdkParameterProduct()
    {
        Object[] jdkParameter = JDK_PARAMETERS[0];
        AllowedJdkParameterProduct allowedJdkParameterProduct = new AllowedJdkParameterProduct();
        allowedJdkParameterProduct.setId((Integer) jdkParameter[0]);
        Product product = new Product();
        product.setId(1);
        allowedJdkParameterProduct.setProduct(product);
        AllowedJdk allowedJdk = new AllowedJdk();
        allowedJdk.setJdk("Zulu JDK");
        allowedJdk.setJvmVersion("11.0.7");
        allowedJdk.setId(1);
        allowedJdkParameterProduct.setAllowedJdk(allowedJdk);
        JdkParameter parameter = new JdkParameter();
        parameter.setName((String) jdkParameter[1]);
        allowedJdkParameterProduct.setJdkParameter(parameter);
        allowedJdkParameterProduct.setIsDefault((Boolean) jdkParameter[2]);
        return allowedJdkParameterProduct;
    }
*/
}
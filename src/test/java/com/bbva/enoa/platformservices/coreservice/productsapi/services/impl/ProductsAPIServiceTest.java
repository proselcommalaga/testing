package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;
/*
import com.bbva.enoa.apirestgen.etherapi.model.EtherConfigStatusDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductBaseDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductManagementConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductRequestDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DestinationPlatformLoggingType;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ExceptionProcessor;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.UserValidationService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBatchManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILogsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductConfigService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.IErrorCode;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProductsAPIServiceTest
{

    private static final Logger LOG = LoggerFactory.getLogger(ProductsAPIServiceTest.class);

    @Mock
    private ProductRepository productRepository;
    @Mock
    private IErrorTaskManager errorTaskMgr;
    @Mock
    private IProductUsersClient usersClient;
    @Mock
    private IDocSystemService docSystemService;
    @Mock
    private UserValidationService userValidationService;
    @Mock
    private ToolsClient toolsService;
    @Mock
    private IProductConfigService productInfraConfigService;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IEtherService etherService;
    @Mock
    private ILogsClient logsClient;
    @Mock
    private IBatchManagerClient batchManagerClient;
    @Mock
    private NewProductRequestServiceImpl newProductRequestServiceImpl;
    @InjectMocks
    private ProductsAPIService productsAPIService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createNewProduct()
    {
        ProductRequestDTO createNewProduct = new ProductRequestDTO();
        createNewProduct.setName("Name");
        createNewProduct.setUuaa("UUAA");
        Product product = this.generateProduct();
        product.setId(RandomUtils.nextInt(0, 9999));
        USUserDTO usUser = new USUserDTO();
        usUser.setEmail("somebody@bbva.com");
        //When
        when(this.newProductRequestServiceImpl.createAndSaveNewProduct(createNewProduct, usUser)).thenReturn(product);
        when(this.productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        Product response = this.productsAPIService.createNewProduct(createNewProduct);
        assertEquals(product, response);
    }

    @Test
    public void validateUserErrorExceptionProcessorTest() throws Errors
    {

        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        NovaException novaException =  new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "ErrorMessage");

        when(this.usersClient.getUser(anyString(), any(Errors.class))).thenThrow(novaException);

        Assertions.assertThrows(NovaException.class, ()-> this.productsAPIService.createNewProduct(productRequestDTO));

        try{
            this.productsAPIService.createNewProduct(productRequestDTO);
        }catch(NovaException e){
            Assertions.assertEquals(novaException.getErrorCode().toString(), e.getErrorCode().toString());
            LOG.error(e.getNovaError().toString());
        }
    }

    @Test
    public void validateUserErrorUnexpectedExceptionTest()
    {

        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        NovaException novaException =  new NovaException(ProductsAPIError.getUnexpectedError(), "ErrorMessage");

        when(this.userValidationService.getUser(anyString(),any(ExceptionProcessor.class))).thenThrow(novaException);

        Assertions.assertThrows(NovaException.class, ()-> this.productsAPIService.createNewProduct(productRequestDTO));

        try{
            this.productsAPIService.createNewProduct(productRequestDTO);
        }catch(NovaException e){
            Assertions.assertEquals(novaException.getErrorCode(), e.getErrorCode());
            LOG.error(e.getNovaError().toString());
        }
    }

    @Test
    public void validateUserErroruserDoesNotExistTest()
    {

        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        NovaException novaException =  new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "ErrorMessage");

        when(this.userValidationService.getUser(anyString(),any(ExceptionProcessor.class))).thenThrow(novaException);

        Assertions.assertThrows(NovaException.class, ()-> this.productsAPIService.createNewProduct(productRequestDTO));

        try{
            this.productsAPIService.createNewProduct(productRequestDTO);
        }catch(NovaException e){
            Assertions.assertEquals(novaException.getErrorCode(), e.getErrorCode());
            LOG.error(e.getNovaError().toString());
        }
    }

    @Test
    public void createNewProductWithDigits()
    {
        ProductRequestDTO createNewProduct = new ProductRequestDTO();
        createNewProduct.setName("Name1");
        createNewProduct.setUuaa("UUA1");


        Product product = this.generateProduct();
        product.setId(RandomUtils.nextInt(0, 9999));
        product.setEmail("email@bbva.com");

        USUserDTO usUser = new USUserDTO();
        usUser.setEmail("somebody@bbva.com");
        when(this.newProductRequestServiceImpl.createAndSaveNewProduct(createNewProduct, usUser)).thenReturn(product);
        when(this.productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));

        doNothing().when(this.logsClient).createLogRateThresholdEvents(anyInt(), anyString());

        Product response = this.productsAPIService.createNewProduct(createNewProduct);
        assertEquals(product, response);
        verify(this.logsClient).createLogRateThresholdEvents(product.getId(), product.getEmail());
    }

    @Test
    public void getAllProducts()
    {
        Product product = this.generateProduct();
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(this.productRepository.findAll()).thenReturn(productList);
        ProductDTO[] response = this.productsAPIService.getAllProducts();
        assertEquals(1, response[0].getProductId().intValue());
    }

    @Test
    public void getAllProductsNoProductList()
    {
        Product product = this.generateProduct();

        when(this.productRepository.findAll()).thenReturn(null);
        when(this.errorTaskMgr.createErrorTask(anyInt(), any(IErrorCode.class), anyString(), any(ToDoTaskType.class), any(Exception.class), anyString(), anyInt())).thenReturn(1234);

        Assertions.assertThrows(NovaException.class, () -> this.productsAPIService.getAllProducts());

    }

    @Test
    public void getAllProductsByStatus()
    {
        Product product = this.generateProduct();
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(this.productRepository.findByProductStatusType(Mockito.eq(ProductStatus.READY), any())).thenReturn(productList);
        ProductDTO[] response = this.productsAPIService.getAllProductsByStatus("READY", null);
        assertTrue(response.length > 0);
        assertEquals(1, response[0].getProductId().intValue());
    }

    @Test
    public void getAllProductsByUser()
    {
        Product product = new Product();
        int[] productIds = new int[]{1};
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);
        when(this.usersClient.getProductsByUser("CODE")).thenReturn(productIds);
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.productRepository.findByIdsType(Mockito.eq(productIds), any())).thenReturn(Collections.singletonList(product));
        ProductDTO[] response = this.productsAPIService.getProducts(null, "CODE");
        assertEquals(1, response[0].getProductId().intValue());
    }

    @Test
    public void getUuaasTest()
    {
        Product product = this.generateProduct();
        when(this.productRepository.findAllByOrderByUuaaAsc()).thenReturn(Collections.singletonList(product));
        String[] response = this.productsAPIService.getUuaas();
        assertEquals("UUAA", response[0]);
    }

    @Test
    public void getUuaasNovaEphoenixLegacyTest()
    {
        String[] uuaas = new String[2];
        uuaas[0] = "ENOA";
        uuaas[1] = "XCLI";
        when(this.batchManagerClient.getUuaasEphoenixLegacy()).thenReturn(uuaas);
        String[] response = this.productsAPIService.getUuaasNovaEphoenixLegacy();
        assertEquals("ENOA", response[0]);
    }

    @Test
    public void getUuaasEphoenixLegacyTest()
    {
        String[] uuaas = new String[2];
        uuaas[0] = "ENOA";
        uuaas[1] = "XCLI";
        when(this.batchManagerClient.getUuaasEphoenixLegacy()).thenReturn(uuaas);
        String[] response = this.productsAPIService.getUuaasNovaEphoenixLegacy();
        assertEquals("XCLI", response[1]);
    }

    @Test
    public void getTypesTest()
    {
        Product product = this.generateProduct();
        when(this.productRepository.findAllByOrderByTypeAsc()).thenReturn(Collections.singletonList(product));
        String[] response = this.productsAPIService.getTypes();
        assertEquals("TYPE", response[0]);
    }

    @Test
    public void getProductsByUserUsingAllProductCall()
    {
        Product product = this.generateProduct();

        when(this.productRepository.findAll()).thenReturn(Collections.singletonList(product));

        ProductDTO[] response = this.productsAPIService.getProducts(null, "");

        verify(this.productRepository, times(1)).findAll();

        assertEquals(1, response[0].getProductId().intValue());
        assertEquals("NAME", response[0].getName());
        assertEquals("IMAGE", response[0].getImage());
    }

    @Test
    public void getBaseProductTest()
    {
        Product product = this.generateProduct();

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductBaseDTO productBaseDTO = this.productsAPIService.getBaseProduct(product.getId());

        assertEquals(product.getId(), productBaseDTO.getProductId());
        assertEquals(product.getName(), productBaseDTO.getName());
        assertEquals(product.getDescription(), productBaseDTO.getDescription());
        assertEquals(product.getUuaa(), productBaseDTO.getUuaa());
        assertEquals(product.getType(), productBaseDTO.getProductType());
    }

    @Test
    public void updateProductStatus()
    {
        Product product = this.generateProduct();
        product.setProductStatus(ProductStatus.CREATING);

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));
        Product response = this.productsAPIService.updateProductStatus(1, "READY");
        assertEquals(ProductStatus.READY, response.getProductStatus());
    }

    @Test
    public void addUserToProduct()
    {
        Product product = this.generateProduct();
        USUserDTO usUser = new USUserDTO();
        usUser.setUserCode("CODE");
        usUser.setEmail("code@bbva.com");

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE");
        verify(this.toolsService, times(1)).addUserTool(any());
    }

    /*@Test FIXME
    public void addUserToProductManageEtherNamesapacesUsersIntAddTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingInt(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("LOGGINGETHER");
        product.setEtherNsLoggingPre("");
        product.setEtherNsLoggingPro("");


        when(this.etherService.getEtherConfigurationStatus(anyInt(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).addUsersToGroup(any(),any(Product.class));

        this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.INT.toString());
        verify(this.etherService, times(1)).addUserToGroupInLoggingNamespace(Environment.INT.toString(), product, "CODE", product.getEtherNsLoggingInt());
        }

    @Test
    public void addUserToProductManageEtherNamesapacesUsersPreAddTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingPre(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("");
        product.setEtherNsLoggingPre("LOGGINGETHER");
        product.setEtherNsLoggingPro("");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).addUserToGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());

        this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.PRE.toString());
        verify(this.etherService, times(1)).addUserToGroupInLoggingNamespace(Environment.PRE.toString(), product, "CODE", product.getEtherNsLoggingPre());

    }

    @Test
    public void addUserToProductManageEtherNamesapacesUsersProAddTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingPro(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("");
        product.setEtherNsLoggingPre("");
        product.setEtherNsLoggingPro("LOGGINGETHER");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).addUserToGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());

        this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.PRO.toString());
        verify(this.etherService, times(1)).addUserToGroupInLoggingNamespace(Environment.PRO.toString(), product, "CODE", product.getEtherNsLoggingPro());

    }

    @Test
    public void addUserToProductManageEtherNamesapacesUsersProRuntimeExceptionTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingPro(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("");
        product.setEtherNsLoggingPre("");
        product.setEtherNsLoggingPro("LOGGINGETHER");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));

        doThrow(RuntimeException.class).when(this.etherService).addUserToGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());
        //Catch working runtime
        Assertions.assertDoesNotThrow(()-> this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE"));

        doThrow(Errors.class).when(this.etherService).addUserToGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());
        //Catch working errors
        Assertions.assertDoesNotThrow(()-> this.productsAPIService.addUserToProduct("CODE", 1, "CODE", "ROLE"));

    }

    @Test
    public void removeUserFromProduct()
    {
        Product product = this.generateProduct();
        USUserDTO usUser = new USUserDTO();

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");
        verify(this.usersClient, times(1)).removeUserFromProduct(any(), any(), any());
    }

    @Test
    public void removeUserFromProductManageEtherNamesapacesUsersIntDeletionTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingInt(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("LOGGINGETHER");
        product.setEtherNsLoggingPre("");
        product.setEtherNsLoggingPro("");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).deleteUserFromGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());

        this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.INT.toString());
        verify(this.etherService, times(1)).deleteUserFromGroupInLoggingNamespace(Environment.INT.toString(), product, "CODE", product.getEtherNsLoggingInt());

    }

    @Test
    public void removeUserFromProductManageEtherNamesapacesUsersPreDeletionTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingPre(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("");
        product.setEtherNsLoggingPre("LOGGINGETHER");
        product.setEtherNsLoggingPro("");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).deleteUserFromGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());

        this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.PRE.toString());
        verify(this.etherService, times(1)).deleteUserFromGroupInLoggingNamespace(Environment.PRE.toString(), product, "CODE", product.getEtherNsLoggingPre());

    }

    @Test
    public void removeUserFromProductManageEtherNamesapacesUsersProDeletionTest() throws Errors
    {
        Product product = this.generateProduct();
        EtherConfigStatusDTO etherConfigStatusDTO = this.generateEtherConfigStatusDTO();
        USUserDTO usUser = new USUserDTO();

        product.setEnabledLoggingPro(DestinationPlatformLoggingType.ALL);
        product.setEtherNsLoggingInt("");
        product.setEtherNsLoggingPre("");
        product.setEtherNsLoggingPro("LOGGINGETHER");


        when(this.etherService.getConfigurationStatus(anyInt(),anyString(),anyString())).thenReturn(new EtherConfigStatusDTO[]{etherConfigStatusDTO});
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(any(), any())).thenReturn(Optional.of(usUser));
        doNothing().when(this.etherService).deleteUserFromGroupInLoggingNamespace(anyString(),any(Product.class), anyString(), anyString());

        this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");

        verify(this.productRepository, times(1)).findById(product.getId());
        verify(this.etherService, times(1)).getConfigurationStatus(product.getId(), PlatformType.LOGGING.toString(), Environment.PRO.toString());
        verify(this.etherService, times(1)).deleteUserFromGroupInLoggingNamespace(Environment.PRO.toString(), product, "CODE", product.getEtherNsLoggingPro());

    }

    @Test
    public void removeUserFromProductUserValidationFail()
    {
        Product product = this.generateProduct();
        NovaException novaExceptionTest = new NovaException(ProductsAPIError.getUnexpectedError(), "ErrorMessage");

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(anyString(), any(ExceptionProcessor.class))).thenThrow(novaExceptionTest);

        try
        {
            this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(novaExceptionTest.getErrorCode(), e.getErrorCode());
            LOG.error(e.getNovaError().toString());
        }
    }

    @Test
    public void removeUserFromProductUserValidationFail2()
    {
        Product product = this.generateProduct();
        NovaException novaExceptionTest = new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "ErrorMessage");

        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.userValidationService.getUser(anyString(), any(ExceptionProcessor.class))).thenThrow(novaExceptionTest);

        try
        {
            this.productsAPIService.removeUserFromProduct("CODE", 1, "CODE");
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(novaExceptionTest.getErrorCode(), e.getErrorCode());
            LOG.error(e.getNovaError().toString());
        }
    }

    @Test
    public void updateProjectKeyTest()
    {
        Product product = this.generateProduct();

        Mockito.<Optional<Product>>when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

        this.productsAPIService.updateProjectKey(1, "PROJECTKEY", "CODE");
        verify(this.productRepository, times(1)).saveAndFlush(product);
    }

    @Test
    public void updateProjectKeyNoProductTest()
    {

        Mockito.<Optional<Product>>when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        this.productsAPIService.updateProjectKey(1, "PROJECTKEY", "CODE");
        verify(this.productRepository, times(0)).saveAndFlush(any(Product.class));
    }

    @Test
    public void updateProjectKeyNoProjectKeyTest()
    {

        Mockito.<Optional<Product>>when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        this.productsAPIService.updateProjectKey(1, "", "CODE");
        verify(this.productRepository, times(0)).saveAndFlush(any(Product.class));
    }

    @Test
    public void getProductSummaryTest()
    {

        Product product = this.generateProduct();

        Mockito.<Optional<Product>>when(this.productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductSummaryDTO productSummaryDTOReturned = this.productsAPIService.getProductSummary(1);
        Assertions.assertEquals(1, productSummaryDTOReturned.getProductId());
        Assertions.assertEquals("UUAA", productSummaryDTOReturned.getUuaa());
        Assertions.assertEquals("IMAGE", productSummaryDTOReturned.getImage());
        Assertions.assertEquals("NAME", productSummaryDTOReturned.getName());
        Assertions.assertEquals("DESCRIPTION", productSummaryDTOReturned.getDescription());
    }


    @Test
    public void updateProductConfigurationTest()
    {
        Product product = this.generateProduct();

        ProductConfigurationDTO productConfigurationDTO = new ProductConfigurationDTO();
        productConfigurationDTO.fillRandomly(2, false, 0, 3);
        ProductManagementConfigurationDTO productManagementConfigurationDTO = new ProductManagementConfigurationDTO();
        productManagementConfigurationDTO.fillRandomly(2, false, 0, 3);
        productManagementConfigurationDTO.setDefaultDeploymentTypeInPro(DeploymentType.NOVA_PLANNED.toString());
        productConfigurationDTO.setManagement(productManagementConfigurationDTO);


        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));
        doNothing().when(this.productInfraConfigService).updateProductConfiguration(any(Product.class), any(ProductConfigurationDTO.class));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

        this.productsAPIService.updateProductConfiguration("CODE", productConfigurationDTO, 1);

        verify(this.usersClient, times(1)).checkHasPermission(anyString(), anyString(), any(NovaException.class));
        verify(this.productRepository, times(1)).findById(1);
        verify(this.productInfraConfigService, times(1)).updateProductConfiguration(product, productConfigurationDTO);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        verify(this.productRepository, times(1)).saveAndFlush(product);
    }

    @Test
    public void updateCommonConfigurationTest()
    {
        Product product = this.generateProduct();

        ProductCommonConfigurationDTO productCommonConfigurationDTO = new ProductCommonConfigurationDTO();
        productCommonConfigurationDTO.fillRandomly(2, false, 0, 3);

        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));
        doNothing().when(this.productInfraConfigService).updateCommonConfiguration(any(Product.class), any(ProductCommonConfigurationDTO.class));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.productRepository.save(any(Product.class))).thenReturn(product);

        this.productsAPIService.updateCommonConfiguration("CODE", productCommonConfigurationDTO, 1);

        verify(this.usersClient, times(1)).checkHasPermission(anyString(), anyString(), any(NovaException.class));
        verify(this.productRepository, times(1)).findById(1);
        verify(this.productInfraConfigService, times(1)).updateCommonConfiguration(product, productCommonConfigurationDTO);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        verify(this.productRepository, times(1)).save(product);
    }

    private ProductRequestDTO generateProductRequestDTO()
    {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.fillRandomly(2,false,0,3);;
        return productRequestDTO;
    }

    private Product generateProduct()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        product.setImage("IMAGE");
        product.setName("NAME");
        product.setDescription("DESCRIPTION");
        product.setType("TYPE");
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);
        product.setEnabledLoggingInt(DestinationPlatformLoggingType.ALL);

        return product;
    }

    private EtherConfigStatusDTO generateEtherConfigStatusDTO()
    {
        EtherConfigStatusDTO etherConfigStatusDTO = new EtherConfigStatusDTO();
        etherConfigStatusDTO.fillRandomly(2,false,0,3);
        etherConfigStatusDTO.setStatus(Constants.OK);

        return etherConfigStatusDTO;
    }
}

 */
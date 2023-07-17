package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.ProductRequestDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.TasksProductCreation;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NewProductRequestServiceImplTest
{

    @Mock
    private Product product;
    @Mock
    private CPDRepository cpdRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IProductUsersClient productUsersClient;
    @Mock
    private ProductsAPIToDoTaskService toDoTaskService;

    @InjectMocks
    private NewProductRequestServiceImpl newProductRequestServiceImpl;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newProductRequest()
    {

        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        Integer todoTaskId = 0;

        doNothing().when(this.productUsersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));
        when(this.toDoTaskService.createProductCreationRequestTask(this.generateTaskProductCreation())).thenReturn(todoTaskId);

        Integer returnedValue = this.newProductRequestServiceImpl.newProductRequest(productRequestDTO, "CODE");

        verify(this.toDoTaskService, times(1)).createProductCreationRequestTask(any(TasksProductCreation.class));
        Assertions.assertEquals(0, returnedValue);

    }

    @Test
    public void createAndSaveNewProductTest()
    {

        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        USUserDTO usUserDTO = this.generateUSUserDTO();
        CPD cpd = this.generateCpd();
        Category category = this.generateCategory();

        when(this.productRepository.saveAndFlush(any(Product.class))).thenReturn(this.product);
        when(this.categoryRepository.findByName(anyString())).thenReturn(category);

        Product productReturned = this.newProductRequestServiceImpl.createAndSaveNewProduct(productRequestDTO, usUserDTO);

        verify(this.productRepository, times(1)).saveAndFlush(any(Product.class));

        Assertions.assertEquals(this.product, productReturned);
    }

    @Test
    public void createAndSaveNewProductNoArgsTest()
    {

        Assertions.assertThrows(NullPointerException.class, () -> this.newProductRequestServiceImpl.createAndSaveNewProduct(null, null));

    }

    private ProductRequestDTO generateProductRequestDTO()
    {

        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.fillRandomly(3, false, 1, 3);
        productRequestDTO.setName("NOVAProductToTest");
        productRequestDTO.setUuaa("UUAA");
        productRequestDTO.setProductId(2);
        productRequestDTO.setCreationTaskId(3);

        return productRequestDTO;
    }

    private USUserDTO generateUSUserDTO()
    {

        USUserDTO usUserDTO = new USUserDTO();
        usUserDTO.setUserCode("USERCODE");
        usUserDTO.setTeams(new String[]{"TEAM1"});
        usUserDTO.setEmail("email@bbva.com");
        usUserDTO.setActive(false);
        usUserDTO.setUserName("NAME");
        usUserDTO.setSurname1("SURNAME");
        usUserDTO.setSurname2("SURNAME2");

        return usUserDTO;
    }

    private Category generateCategory()
    {

        Category categoryElement = new Category();
        categoryElement.setId(1);
        categoryElement.setName("typedCategoryName1");

        return categoryElement;
    }

    private TasksProductCreation generateTaskProductCreation()
    {
        TasksProductCreation tasksProductCreation = new TasksProductCreation();
        tasksProductCreation.fillRandomly(1, false, 2, 4);
        tasksProductCreation.setProductName("NOVAProductToTest");
        tasksProductCreation.setUuaa("UUAA");
        return tasksProductCreation;
    }

    private CPD generateCpd()
    {

        CPD cpd = new CPD();
        cpd.setId(1);
        cpd.setActive(true);
        cpd.setAddress("ADRESS");
        cpd.setElasticSearchCPDName("CPDNAME");
        cpd.setEnvironment(Environment.PRO.getEnvironment());
        cpd.setFilesystem("FILESYSTEM");
        cpd.setLabel("LABEL");
        cpd.setName("NAME");
        cpd.setRegistry("REGISTRY");


        return cpd;
    }

}
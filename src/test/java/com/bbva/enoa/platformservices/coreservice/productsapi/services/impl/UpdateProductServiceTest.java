package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UpdateProductServiceTest
{
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IProductUsersClient usersClient;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @InjectMocks
    private UpdateProductService updateProductService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void updateProductTest() throws JsonProcessingException
    {
        Product product = this.generateProduct();
        ProductSummaryDTO productSummaryDTO = this.generateProductSummaryDTO();

        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(this.productRepository.saveAndFlush(any())).thenReturn(product);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));

        Product productReturned = this.updateProductService.updateProduct(productSummaryDTO, "CODE");

        Assertions.assertNotNull(productReturned);
        verify(this.productRepository, times(1)).findById(productSummaryDTO.getProductId());
        verify(this.productRepository, times(1)).saveAndFlush(product);
        verify(this.usersClient, times(1)).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());

        final Map<String, Object> activityParams = new HashMap<>();
        String categories = product.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "));
        activityParams.put("image", product.getImage());
        activityParams.put("description", product.getDescription());
        activityParams.put("remedySupportGroup", product.getRemedySupportGroup());
        activityParams.put("phone", product.getPhone());
        activityParams.put("desBoard", product.getDesBoard());
        activityParams.put("categories", categories);
        activityParams.put("type", product.getType());
        activityParams.put("email", product.getEmail());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    public void updateProductErrorTest()
    {
        Product product = this.generateProduct();
        ProductSummaryDTO updateProduct = this.generateProductSummaryDTO();

        // Forcing different uuaa to generate error route
        updateProduct.setUuaa("AAUU");

        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(product));

        Assertions.assertThrows(NovaException.class, () -> this.updateProductService.updateProduct(updateProduct, "CODE"));
    }

    @Test
    public void updateProductWrongMailTest()
    {
        Product product = this.generateProduct();
        ProductSummaryDTO updateProduct = this.generateProductSummaryDTO();

        // 2 email address into email field is not allowed
        updateProduct.setEmail("name@bbva.com, name.contractor@bbva.com");

        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(product));

        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.updateProductService.updateProduct(updateProduct, "CODE"));
        NovaError expected = ProductsAPIError.getWrongEmailFormatError();
        NovaError current = exception.getErrorCode();

        Assertions.assertEquals(expected.getErrorMessage(), current.getErrorMessage());
        Assertions.assertEquals(expected.getErrorCode(), current.getErrorCode());
        Assertions.assertEquals(expected.getActionMessage(), current.getActionMessage());
        Assertions.assertEquals(expected.getHttpStatus(), current.getHttpStatus());
    }

    @Test
    public void updateProductAndSaveBBDDTest()
    {
        Product productToUpdate = this.generateProduct();
        ProductSummaryDTO productSummaryDTO = this.generateProductSummaryDTO();

        when(this.productRepository.saveAndFlush(any())).thenReturn(productToUpdate);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));

        Assertions.assertDoesNotThrow(() -> this.updateProductService.updateProductAndSaveBBDD("CODE", productSummaryDTO, productToUpdate));
        verify(this.productRepository, times(1)).saveAndFlush(productToUpdate);
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
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfig = new PlatformConfig();
        platformConfig.setConfigurationType(ConfigurationType.LOGGING);
        platformConfig.setProductId(1);
        platformConfig.setPlatform(Platform.NOVAETHER);
        platformConfig.setEnvironment(Environment.INT.getEnvironment());
        platformConfig.setIsDefault(true);
        platformConfigList.add(platformConfig);

        return product;
    }

    private ProductSummaryDTO generateProductSummaryDTO()
    {
        ProductSummaryDTO productSummaryDto = new ProductSummaryDTO();
        productSummaryDto.fillRandomly(2, false, 0, 3);
        productSummaryDto.setUuaa("UUAA");
        productSummaryDto.setImage("IMAGE");
        productSummaryDto.setName("NAME");
        productSummaryDto.setDescription("DESCRIPTION");
        productSummaryDto.setEmail("NAME.LASTNAME.CONTRACTOR@BBVA.COM");

        return productSummaryDto;
    }

}

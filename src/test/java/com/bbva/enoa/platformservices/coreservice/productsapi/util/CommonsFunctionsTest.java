package com.bbva.enoa.platformservices.coreservice.productsapi.util;

import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class CommonsFunctionsTest
{

	@Mock
    private ProductRepository productsApiProductRepository;

	@BeforeEach
    public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test
    public void validateProductNameAndUuaaName()
	{
		CommonsFunctions.validateProductNameAndUuaaName("Name", "UUAA", this.productsApiProductRepository);

		when(this.productsApiProductRepository.findByName("NAME")).thenReturn(new Product());

        Assertions.assertThrows(NovaException.class, ()->CommonsFunctions.validateProductNameAndUuaaName("Name", "UUAA", this.productsApiProductRepository));
	}

	@Test public void validateProductNameAndUuaaNameError()
	{
        Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProductNameAndUuaaName("Name", null, this.productsApiProductRepository));
	}

	@Test public void validateProductNameAndUuaaNameError2()
	{
		Product product = new Product();
		when(this.productsApiProductRepository.findByUuaaIgnoreCase("UUAA")).thenReturn(product);

        Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProductNameAndUuaaName("Name", "UUAA", this.productsApiProductRepository));
	}

	@Test public void validateProduct()
	{
		Product product = new Product();
		when(this.productsApiProductRepository.findById(1)).thenReturn(Optional.of(product));
		CommonsFunctions.validateProduct(this.productsApiProductRepository, 1);

        Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProduct(this.productsApiProductRepository, 2));
	}

	@Test
    public void testValidateNullUuaa()
	{
        Assertions.assertThrows(NovaException.class, ()->CommonsFunctions.validateProductNameAndUuaaName("name", null, this.productsApiProductRepository));
	}
	@Test
    public void testValidateEmptyUuaa()
	{
		Assertions.assertThrows(NovaException.class, ()->CommonsFunctions.validateProductNameAndUuaaName("name", "", this.productsApiProductRepository));
	}

	@Test
    public void testValidateProductNameForbiddenChars()
	{
        Assertions.assertThrows(NovaException.class, ()->CommonsFunctions.validateProductNameAndUuaaName("name (name)", "", this.productsApiProductRepository));
	}

	@Test public void testValidateWrongSizeUUAA()
	{

		for (int i = 1; i < CommonsFunctions.UUAA_LENGTH; i++)
		{
		    final int ii = i;
            Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProductNameAndUuaaName("name", StringUtils.leftPad("", ii, "A"), this.productsApiProductRepository));
		}

		Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProductNameAndUuaaName("name", StringUtils.leftPad("", CommonsFunctions.UUAA_LENGTH + 1, "A"), this.productsApiProductRepository));
	}

	@Test public void testValidateValidUUAA()
	{
	    // normal uuaa with same char
		String uuaa = "AAAA";
        CommonsFunctions.validateProductNameAndUuaaName("name", uuaa, this.productsApiProductRepository);

        //uuaa with others chars
        uuaa = "XYWZ";
        CommonsFunctions.validateProductNameAndUuaaName("name", uuaa, this.productsApiProductRepository);

        //uuaa with digits
        uuaa = "XYW1";
        CommonsFunctions.validateProductNameAndUuaaName("name", uuaa, this.productsApiProductRepository);

        //uuaa only digits
        uuaa = "1234";
        CommonsFunctions.validateProductNameAndUuaaName("name", uuaa, this.productsApiProductRepository);
    }

    @Test
    public void validateProductUuaaTest(){
	    Product product = this.generateProduct();
        String uuaaArgument = "UUAA";

	    when(this.productsApiProductRepository.findOneByUuaa(uuaaArgument)).thenReturn(product);

	    Assertions.assertDoesNotThrow(()->CommonsFunctions.validateProductUuaa(this.productsApiProductRepository, uuaaArgument));

    }

    @Test
    public void validateProductUuaaErrorTest(){
        Product product = this.generateProduct();
        String uuaaArgument = "UUAA";

        when(this.productsApiProductRepository.findOneByUuaa(uuaaArgument)).thenReturn(null);

        Assertions.assertThrows(NovaException.class , ()->CommonsFunctions.validateProductUuaa(this.productsApiProductRepository, uuaaArgument));
    }

	@Test
	public void validateProductSummaryUpdateTest(){
		//validating a list of valid emails
		String[] emails = {"name@bbva.com", " name@bbva.com", "name@bbva.com ","name.lastname@bbva.com","name.lastname.contractor@bbva.com",
				"name-lastname.contractor@bbva.com","name_lastname.contractor@bbva.com"};

		ProductSummaryDTO productSummaryDTO = new ProductSummaryDTO();
		for(String email : emails)
		{
			productSummaryDTO.fillRandomly(1,true, 0, 1);
			productSummaryDTO.setProductId(RandomUtils.nextInt());
			productSummaryDTO.setEmail(email);
			Assertions.assertDoesNotThrow( ()-> CommonsFunctions.validateProductSummaryUpdate(productSummaryDTO));
		}

		//validating invalid list of mails
		String[] wrongs = {"name@@bbva.com", "name.bbva.com","namebbva.com",
				"name@bbva,com","name@bbva_com","name@bbva-com","name@bbva:com","name@bbva/com",
				"name-lastname.contractor@bbva.com,name_lastname.contractor@bbva.com"};

		for(String email : wrongs)
		{
			productSummaryDTO.fillRandomly(1,true, 0, 1);
			productSummaryDTO.setProductId(RandomUtils.nextInt());
			productSummaryDTO.setEmail(email);
			Assertions.assertThrows(NovaException.class, ()-> CommonsFunctions.validateProductSummaryUpdate(productSummaryDTO));
		}

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
        platformConfig.setIsDefault(true);
        platformConfig.setEnvironment(Environment.INT.getEnvironment());
        platformConfig.setPlatform(Platform.NOVAETHER);
        platformConfig.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigList.add(platformConfig);

        product.setPlatformConfigList(platformConfigList);

        return product;
    }

}

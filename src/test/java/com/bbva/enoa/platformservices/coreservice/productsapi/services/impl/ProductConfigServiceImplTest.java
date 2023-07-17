package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.EtherConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfigDTO;
import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfiguredByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.NovaConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductManagementConfByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceConfigurationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.LogLevel;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ProductConfigServiceImplTest
{
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CPDRepository cpdRepository;

    @InjectMocks
    private ProductConfigServiceImpl productConfigService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class CommonConfiguration
    {
        @Test
        @DisplayName("Get Common Configuration")
        public void getCommonConfiguration()
        {
            Product product = generateProduct();

            // Expected response
            ProductCommonConfigurationDTO productCommonConfigurationDTO = new ProductCommonConfigurationDTO();
            productCommonConfigurationDTO.setCriticalityLevel(1);
            productCommonConfigurationDTO.setDevelopment(1);
            productCommonConfigurationDTO.setQualityLevel(1);
            productCommonConfigurationDTO.setReleaseSlots(2);
            productCommonConfigurationDTO.setBatchLimitRepeat(10);

            ProductCommonConfigurationDTO response = productConfigService.getCommonConfigurationFromProductEntity(product);

            assertEquals(productCommonConfigurationDTO, response);
        }

        @Test
        @DisplayName("Update product common configuration -> OK")
        void ok()
        {
            Product product = generateProduct();

            ProductCommonConfigurationDTO commonConfigurationDTO = new ProductCommonConfigurationDTO();
            commonConfigurationDTO.setDevelopment(0);
            commonConfigurationDTO.setQualityLevel(1);
            commonConfigurationDTO.setCriticalityLevel(2);
            commonConfigurationDTO.setReleaseSlots(3);
            commonConfigurationDTO.setBatchLimitRepeat(5);

            when(productRepository.save(any(Product.class))).thenReturn(product);

            productConfigService.updateCommonConfiguration(product, commonConfigurationDTO);

            verify(productRepository).save(product);
            verifyNoMoreInteractions(productRepository);
            assertEquals(0, product.getDevelopment());
            assertEquals(1, product.getQualityLevel());
            assertEquals(2, product.getCriticalityLevel());
            assertEquals(3, product.getReleaseSlots());
        }

        @Nested
        class InvalidReleaseSlotsNumber
        {
            @Test
            @DisplayName("Update product common configuration -> higher than 4 error")
            void higherThan4()
            {
                Product product = generateProduct();

                ProductCommonConfigurationDTO commonConfigurationDTO = new ProductCommonConfigurationDTO();
                commonConfigurationDTO.setDevelopment(0);
                commonConfigurationDTO.setQualityLevel(1);
                commonConfigurationDTO.setCriticalityLevel(2);
                commonConfigurationDTO.setReleaseSlots(5);
                commonConfigurationDTO.setBatchLimitRepeat(5);

                assertThrows(
                        NovaException.class,
                        () -> productConfigService.updateCommonConfiguration(product, commonConfigurationDTO)
                );
                verifyNoMoreInteractions(productRepository);
            }

            @Test
            @DisplayName("Update product common configuration -> less than 4 error")
            void lessThan2()
            {
                Product product = generateProduct();

                ProductCommonConfigurationDTO commonConfigurationDTO = new ProductCommonConfigurationDTO();
                commonConfigurationDTO.setDevelopment(0);
                commonConfigurationDTO.setQualityLevel(1);
                commonConfigurationDTO.setCriticalityLevel(2);
                commonConfigurationDTO.setReleaseSlots(1);
                commonConfigurationDTO.setBatchLimitRepeat(1);

                assertThrows(
                        NovaException.class,
                        () -> productConfigService.updateCommonConfiguration(product, commonConfigurationDTO)
                );
                verifyNoMoreInteractions(productRepository);
            }
        }
    }


    @Nested
    class ManagementConfiguration
    {
        @DisplayName("Get Management Configuration - INT")
        @Test
        public void getManagementConfigurationFromProductEntityInt()
        {
            Environment env = Environment.INT;
            Product product = generateProductWithManagementConfig();

            ProductManagementConfByEnvDTO result = productConfigService.getManagementConfigurationFromProductEntityByEnv(product, env);

            assertEquals(new ProductManagementConfByEnvDTO(), result);
        }
        @DisplayName("Get Management Configuration - PRE")
        @Test
        public void getManagementConfigurationFromProductEntityPre()
        {
            Environment env = Environment.PRE;
            Product product = generateProductWithManagementConfig();

            ProductManagementConfByEnvDTO expected = new ProductManagementConfByEnvDTO();
            expected.setDefaultAutodeploy(true);
            expected.setDefaultAutomanage(true);

            ProductManagementConfByEnvDTO result = productConfigService.getManagementConfigurationFromProductEntityByEnv(product, env);

            assertEquals(expected, result);
        }
        @DisplayName("Get Management Configuration - PRO")
        @Test
        public void getManagementConfigurationFromProductEntityPro()
        {
            Environment env = Environment.PRO;
            Product product = generateProductWithManagementConfig();

            ProductManagementConfByEnvDTO expected = new ProductManagementConfByEnvDTO();
            expected.setDefaultAutodeploy(true);
            expected.setDefaultAutomanage(true);
            product.setDefaultDeploymentTypeInPro(CommonsFunctions.validateDeploymentType("ON_DEMAND"));

            ProductManagementConfByEnvDTO result = productConfigService.getManagementConfigurationFromProductEntityByEnv(product, env);

            assertEquals(expected, result);
        }

        @Nested
        class Update
        {
            @Test
            @DisplayName("updateManagementConfiguration INT")
            public void updateManagementConfigurationInt()
            {
                Environment env = Environment.INT;
                Product product = generateProductWithManagementConfig();

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateManagementConfigurationByEnv(null, product, env);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
            }
            @Test
            @DisplayName("updateManagementConfiguration PRE")
            public void updateManagementConfigurationPre()
            {
                Environment env = Environment.PRE;
                Product product = generateProductWithManagementConfig();

                ProductManagementConfByEnvDTO productManagementConfPre = new ProductManagementConfByEnvDTO();
                productManagementConfPre.setDefaultAutodeploy(false);
                productManagementConfPre.setDefaultAutomanage(false);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateManagementConfigurationByEnv(productManagementConfPre, product, env);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(productManagementConfPre.getDefaultAutodeploy(), product.getDefaultAutodeployInPre());
                assertEquals(productManagementConfPre.getDefaultAutomanage(), product.getDefaultAutomanageInPre());
            }

            @Test
            @DisplayName("updateManagementConfiguration PRO")
            public void updateManagementConfigurationPro()
            {
                Environment env = Environment.PRO;
                Product product = generateProductWithManagementConfig();

                ProductManagementConfByEnvDTO productManagementConfPro = new ProductManagementConfByEnvDTO();
                productManagementConfPro.setDefaultAutodeploy(false);
                productManagementConfPro.setDefaultAutomanage(false);
                productManagementConfPro.setDefaultDeploymentTypeInPro("NOVA_PLANNED");

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateManagementConfigurationByEnv(productManagementConfPro, product, env);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(productManagementConfPro.getDefaultAutodeploy(), product.getDefaultAutodeployInPro());
                assertEquals(productManagementConfPro.getDefaultAutomanage(), product.getDefaultAutomanageInPro());
                assertEquals(productManagementConfPro.getDefaultDeploymentTypeInPro(), product.getDefaultDeploymentTypeInPro().getType());
            }

        }

    }

    @Nested
    class ServiceConfiguration
    {
        @Nested
        class GetServiceConfigurationByEnv
        {
            @Test
            @DisplayName("Get Service Configuration INT")
            public void getServiceConfigurationInt()
            {
                Environment env = Environment.INT;
                Product product = generateProductWithServiceConf();

                ProductServiceConfigurationDTO productServiceConfigurationInt = new ProductServiceConfigurationDTO();
                productServiceConfigurationInt.setCesEnabled(true);
                productServiceConfigurationInt.setMicrogwLogLevel("INFO");

                ProductServiceConfigurationDTO result = productConfigService.getServiceConfigurationFromProductEntityByEnv(product, env);

                assertEquals(productServiceConfigurationInt, result);
            }

            @Test
            @DisplayName("Get Service Configuration PRE")
            public void getServiceConfigurationPre()
            {
                Environment env = Environment.PRE;
                Product product = generateProductWithServiceConf();

                ProductServiceConfigurationDTO productServiceConfigurationPre = new ProductServiceConfigurationDTO();
                productServiceConfigurationPre.setCesEnabled(true);
                productServiceConfigurationPre.setMicrogwLogLevel("INFO");

                ProductServiceConfigurationDTO result = productConfigService.getServiceConfigurationFromProductEntityByEnv(product, env);

                assertEquals(productServiceConfigurationPre, result);
            }

            @Test
            @DisplayName("Get Service Configuration PRO")
            public void getServiceConfigurationPro()
            {
                Environment env = Environment.PRO;
                Product product = generateProductWithServiceConf();

                ProductServiceConfigurationDTO productServiceConfigurationPro = new ProductServiceConfigurationDTO();
                productServiceConfigurationPro.setCesEnabled(true);
                productServiceConfigurationPro.setMicrogwLogLevel("INFO");

                ProductServiceConfigurationDTO result = productConfigService.getServiceConfigurationFromProductEntityByEnv(product, env);

                assertEquals(productServiceConfigurationPro, result);
            }
        }

        @Nested
        class UpdateServiceConfigurationByEnv
        {

            @Test
            @DisplayName("Update Service Configuration INT")
            public void updateProductServiceConfigurationInt()
            {
                Product product = generateProductWithServiceConf();
                Environment env = Environment.INT;

                ProductServiceConfigurationDTO productServiceConfigurationInt = new ProductServiceConfigurationDTO();
                productServiceConfigurationInt.setMicrogwLogLevel(LogLevel.INFO.name());
                productServiceConfigurationInt.setCesEnabled(true);

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

                productConfigService.updateProductServiceConfigurationByEnv(productServiceConfigurationInt, product, env);

                verify(productRepository).saveAndFlush(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(productServiceConfigurationInt.getMicrogwLogLevel(), product.getMgwLogLevelInt().name());
                assertEquals(productServiceConfigurationInt.getCesEnabled(), product.isCesEnabledInt());
            }

            @Test
            @DisplayName("Update Service Configuration PRE")
            public void updateProductServiceConfigurationPre()
            {
                Product product = generateProduct();
                Environment env = Environment.PRE;

                ProductServiceConfigurationDTO productServiceConfigurationPre = new ProductServiceConfigurationDTO();
                productServiceConfigurationPre.setMicrogwLogLevel(LogLevel.INFO.name());
                productServiceConfigurationPre.setCesEnabled(true);

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

                productConfigService.updateProductServiceConfigurationByEnv(productServiceConfigurationPre, product, env);

                verify(productRepository).saveAndFlush(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(productServiceConfigurationPre.getMicrogwLogLevel(), product.getMgwLogLevelPre().name());
                assertEquals(productServiceConfigurationPre.getCesEnabled(), product.isCesEnabledPre());
            }

            @Test
            @DisplayName("Update Service Configuration PRO")
            public void updateProductServiceConfigurationPro()
            {
                Product product = generateProduct();
                Environment env = Environment.PRO;

                ProductServiceConfigurationDTO productServiceConfigurationPro = new ProductServiceConfigurationDTO();
                productServiceConfigurationPro.setMicrogwLogLevel(LogLevel.INFO.name());
                productServiceConfigurationPro.setCesEnabled(true);

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

                productConfigService.updateProductServiceConfigurationByEnv(productServiceConfigurationPro, product, env);

                verify(productRepository).saveAndFlush(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(productServiceConfigurationPro.getMicrogwLogLevel(), product.getMgwLogLevelPro().name());
                assertEquals(productServiceConfigurationPro.getCesEnabled(), product.isCesEnabledPro());
            }
        }
    }

    @Nested
    class NovaInfrastructureConfiguration
    {
        @Nested
        class GetNovaInfrastructureConfigurationByEnv
        {
            @Test
            @DisplayName("Get Nova Configuration INT")
            public void getNovaConfigurationInt()
            {
                Product product = generateProductWithNovaConf();
                Environment env = Environment.INT;

                NovaConfigurationDTO result = productConfigService.getNovaConfigurationFromProductEntity(product, env);

                assertEquals(new NovaConfigurationDTO(), result);
            }

            @Test
            @DisplayName("Get Nova Configuration PRE")
            public void getNovaConfigurationPre()
            {
                Product product = generateProductWithNovaConf();
                Environment env = Environment.PRE;

                NovaConfigurationDTO result = productConfigService.getNovaConfigurationFromProductEntity(product, env);

                assertEquals(new NovaConfigurationDTO(), result);
            }

            @Test
            @DisplayName("Get Nova Configuration PRO")
            public void getNovaConfigurationPro()
            {
                Product product = generateProductWithNovaConf();
                Environment env = Environment.PRO;

                NovaConfigurationDTO result = productConfigService.getNovaConfigurationFromProductEntity(product, env);

                assertNotNull(result);
                assertFalse(result.getMulticpd());
                assertEquals(generateCpd().getName(), result.getCpd());
            }
        }

        @Nested
        class UpdateNovaInfrastructureConfigurationByEnv
        {
            @Test
            @DisplayName("Update Nova Infrastructure INT")
            public void updateProductNovaInfrastructureConfInt()
            {
                Product product = generateProduct();
                Environment env = Environment.INT;

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

                productConfigService.updateProductNovaInfrastructureConf(product, env, null);

                verify(productRepository).saveAndFlush(product);
                verifyNoMoreInteractions(productRepository);
            }

            @Test
            @DisplayName("Update Nova Infrastructure PRE")
            public void updateProductNovaInfrastructureConfPre()
            {
                Product product = generateProduct();
                Environment env = Environment.PRE;

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

                productConfigService.updateProductNovaInfrastructureConf(product, env, null);

                verify(productRepository).saveAndFlush(product);
                verifyNoMoreInteractions(productRepository);
            }

            @Test
            @DisplayName("Update Nova Infrastructure PRO")
            public void updateProductNovaInfrastructureConfPro()
            {
                Product product = generateProductWithNovaConf();
                Environment env = Environment.PRO;
                CPD cpd = generateCpd();

                NovaConfigurationDTO novaConfDTO = new NovaConfigurationDTO();
                novaConfDTO.setCpd(generateCpd().getName());
                novaConfDTO.setMulticpd(true);

                when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
                when(cpdRepository.getByNameAndEnvironmentAndMainSwarmCluster(anyString(), anyString(), anyBoolean())).thenReturn(cpd);

                productConfigService.updateProductNovaInfrastructureConf(product, env, novaConfDTO);

                verify(productRepository).saveAndFlush(product);
                verify(cpdRepository).getByNameAndEnvironmentAndMainSwarmCluster(anyString(), anyString(), anyBoolean());
                verifyNoMoreInteractions(productRepository);
            }
        }
    }

    @Nested
    class EtherInfrastructureConfiguration
    {
        @Nested
        class GetEtherInfrastructureConfigurationByEnv
        {
            @Test
            @DisplayName("Get Ether Configuration INT")
            public void getEtherConfigurationInt()
            {
                String namespace = "namespace";
                Environment env = Environment.INT;
                Product product = generateProduct();
                product.setEtherNsInt(namespace);

                EtherConfigurationDTO result = productConfigService.getEtherConfigurationFromProductEntity(product, env);

                assertEquals(namespace, result.getNamespace());
            }

            @Test
            @DisplayName("Get Ether Configuration PRE")
            public void getEtherConfigurationPre()
            {
                String namespace = "namespace";
                Environment env = Environment.PRE;
                Product product = generateProduct();
                product.setEtherNsPre(namespace);

                EtherConfigurationDTO result = productConfigService.getEtherConfigurationFromProductEntity(product, env);

                assertEquals(namespace, result.getNamespace());
            }

            @Test
            @DisplayName("Get Ether Configuration PRO")
            public void getEtherConfigurationPro()
            {
                String namespace = "namespace";
                Environment env = Environment.PRO;
                Product product = generateProduct();
                product.setEtherNsPro(namespace);

                EtherConfigurationDTO result = productConfigService.getEtherConfigurationFromProductEntity(product, env);

                assertEquals(namespace, result.getNamespace());
            }
        }

        @Nested
        class UpdateEtherInfrastructureConfigurationByEnv
        {
            @Test
            @DisplayName("Update Ether Infrastructure INT")
            public void updateProductEtherInfrastructureConfInt()
            {
                String namespace = "namespace";
                Environment env = Environment.INT;
                Product product = generateProduct();
                product.setEtherNsInt("stringTestInt");
                product.setEtherNsInt("stringTestPre");
                product.setEtherNsInt("stringTestPro");
                EtherConfigurationDTO etherConfDTO = new EtherConfigurationDTO();
                etherConfDTO.setNamespace(namespace);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateProductEtherInfrastructureConf(product, env, etherConfDTO);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(namespace, product.getEtherNsInt());
            }

            @Test
            @DisplayName("Update Ether Infrastructure PRE")
            public void updateProductEtherInfrastructureConfPre()
            {
                String namespace = "namespace";
                Environment env = Environment.PRE;
                Product product = generateProduct();
                product.setEtherNsInt("stringTestInt");
                product.setEtherNsPre("stringTestPre");
                product.setEtherNsPro("stringTestPro");
                EtherConfigurationDTO etherConfDTO = new EtherConfigurationDTO();
                etherConfDTO.setNamespace(namespace);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateProductEtherInfrastructureConf(product, env, etherConfDTO);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(namespace, product.getEtherNsPre());
            }

            @Test
            @DisplayName("Update Ether Infrastructure PRO")
            public void updateProductEtherInfrastructureConfPro()
            {
                String namespace = "namespace";
                Environment env = Environment.PRO;
                Product product = generateProduct();
                product.setEtherNsInt("stringTestInt");
                product.setEtherNsPre("stringTestPre");
                product.setEtherNsPro("stringTestPro");
                EtherConfigurationDTO etherConfDTO = new EtherConfigurationDTO();
                etherConfDTO.setNamespace(namespace);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                productConfigService.updateProductEtherInfrastructureConf(product, env, etherConfDTO);

                verify(productRepository).save(product);
                verifyNoMoreInteractions(productRepository);
                assertEquals(namespace, product.getEtherNsPro());
            }

            @Test
            @DisplayName("Throw Duplicated Namespaces Exception")
            public void namespacesDuplicatedException()
            {
                String namespace = "namespace";
                Environment env = Environment.PRO;
                Product product = generateProduct();
                product.setEtherNsInt("namespace");
                product.setEtherNsPre("stringTestPre");
                product.setEtherNsPro("stringTestPro");
                EtherConfigurationDTO etherConfDTO = new EtherConfigurationDTO();
                etherConfDTO.setNamespace(namespace);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                assertThrows(
                        NovaException.class,
                        () -> productConfigService.updateProductEtherInfrastructureConf(product, env, etherConfDTO));
                verifyNoMoreInteractions(productRepository);
            }
        }
    }

    @Nested
    class GeneralInfrastructureSelected
    {
        @Nested
        class GetGeneralInfrastructureSelectedByEnv
        {
            @Test
            @DisplayName("Get Infrastructure selected")
            public void getGeneralInfrastructureConfig()
            {
                Environment env = Environment.INT;
                Product product = generateProductDeploymentConfigurationDTO();

                InfrastructureConfiguredByEnvDTO infrastructureConfiguredByEnvDTO = generateInfrastructureConfiguredByEnvDTO();

                InfrastructureConfiguredByEnvDTO result = productConfigService.getGeneralInfrastructureConfigFromProductEntityByEnv(product, env);

                assertEquals(infrastructureConfiguredByEnvDTO.getDeployInfra().length, result.getDeployInfra().length);
                assertEquals(infrastructureConfiguredByEnvDTO.getLoggingInfra().length, result.getLoggingInfra().length);
            }
        }

        @Nested
        class UpdateGeneralInfrastructureSelectedByEnv
        {
            //TODO@Guille
        }
    }




    // ############################## PRIVATE METHODS ##############################
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
        product.setCriticalityLevel(1);
        product.setDevelopment(1);
        product.setQualityLevel(1);
        product.setReleaseSlots(2);
        product.setBatchLimitRepeat(10);
        return product;
    }

    private Product generateProductWithManagementConfig()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        product.setImage("IMAGE");
        product.setName("NAME");
        product.setDescription("DESCRIPTION");
        product.setType("TYPE");
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);

        product.setDefaultAutodeployInPre(true);
        product.setDefaultAutomanageInPre(true);
        product.setDefaultAutodeployInPro(true);
        product.setDefaultAutomanageInPro(true);
        product.setDefaultDeploymentTypeInPro(CommonsFunctions.validateDeploymentType("ON_DEMAND"));


        return product;
    }

    private Product generateProductWithServiceConf()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        product.setImage("IMAGE");
        product.setName("NAME");
        product.setDescription("DESCRIPTION");
        product.setType("TYPE");
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);

        product.setMgwLogLevelInt(LogLevel.INFO);
        product.setMgwLogLevelPre(LogLevel.INFO);
        product.setMgwLogLevelPro(LogLevel.INFO);

        product.setCesEnabledInt(true);
        product.setCesEnabledPre(true);
        product.setCesEnabledPro(true);


        return product;
    }

    private Product generateProductWithNovaConf()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        product.setImage("IMAGE");
        product.setName("NAME");
        product.setDescription("DESCRIPTION");
        product.setType("TYPE");
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);

        product.setMultiCPDInPro(false);
        product.setCPDInPro(generateCpd());


        return product;
    }

    private Product generateProductDeploymentConfigurationDTO()
    {
        Product product = generateProduct();

        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig deployNova = new PlatformConfig();
        deployNova.setProductId(product.getId());
        deployNova.setPlatform(Platform.NOVA);
        deployNova.setIsDefault(true);
        deployNova.setConfigurationType(ConfigurationType.DEPLOY);
        deployNova.setEnvironment(Environment.INT.getEnvironment());
        deployNova.setId(1);
        platformConfigList.add(deployNova);
        PlatformConfig deployEther= new PlatformConfig();
        deployEther.setProductId(product.getId());
        deployEther.setPlatform(Platform.NOVA);
        deployEther.setIsDefault(true);
        deployEther.setConfigurationType(ConfigurationType.DEPLOY);
        deployEther.setEnvironment(Environment.INT.getEnvironment());
        deployEther.setId(1);
        platformConfigList.add(deployEther);
        PlatformConfig deployAws = new PlatformConfig();
        deployAws.setProductId(product.getId());
        deployAws.setPlatform(Platform.NOVA);
        deployAws.setIsDefault(true);
        deployAws.setConfigurationType(ConfigurationType.DEPLOY);
        deployAws.setEnvironment(Environment.INT.getEnvironment());
        deployAws.setId(1);
        platformConfigList.add(deployAws);

        PlatformConfig loggingNova = new PlatformConfig();
        loggingNova.setProductId(product.getId());
        loggingNova.setPlatform(Platform.NOVA);
        loggingNova.setIsDefault(true);
        loggingNova.setConfigurationType(ConfigurationType.LOGGING);
        loggingNova.setEnvironment(Environment.INT.getEnvironment());
        loggingNova.setId(2);
        platformConfigList.add(loggingNova);
        PlatformConfig loggingEther = new PlatformConfig();
        loggingEther.setProductId(product.getId());
        loggingEther.setPlatform(Platform.ETHER);
        loggingEther.setIsDefault(true);
        loggingEther.setConfigurationType(ConfigurationType.LOGGING);
        loggingEther.setEnvironment(Environment.INT.getEnvironment());
        loggingEther.setId(2);
        platformConfigList.add(loggingEther);
        PlatformConfig loggingNovaEther = new PlatformConfig();
        loggingNovaEther.setProductId(product.getId());
        loggingNovaEther.setPlatform(Platform.NOVAETHER);
        loggingNovaEther.setIsDefault(true);
        loggingNovaEther.setConfigurationType(ConfigurationType.LOGGING);
        loggingNovaEther.setEnvironment(Environment.INT.getEnvironment());
        loggingNovaEther.setId(2);
        platformConfigList.add(loggingNovaEther);
        PlatformConfig loggingAws = new PlatformConfig();
        loggingAws.setProductId(product.getId());
        loggingAws.setPlatform(Platform.AWS);
        loggingAws.setIsDefault(true);
        loggingAws.setConfigurationType(ConfigurationType.LOGGING);
        loggingAws.setEnvironment(Environment.INT.getEnvironment());
        loggingAws.setId(2);
        platformConfigList.add(loggingAws);

        product.setPlatformConfigList(platformConfigList);

        return product;
    }

    public InfrastructureConfiguredByEnvDTO generateInfrastructureConfiguredByEnvDTO()
    {
        InfrastructureConfiguredByEnvDTO infrastructureConfiguredByEnvDTO = new InfrastructureConfiguredByEnvDTO();
        InfrastructureConfigDTO[] deployArray = new InfrastructureConfigDTO[4];
        InfrastructureConfigDTO[] logArray = new InfrastructureConfigDTO[4];

        InfrastructureConfigDTO deployInfraNova = new InfrastructureConfigDTO();
        deployInfraNova.setInfrastructureName(Platform.NOVA.getName());
        deployInfraNova.setIsDefault(true);
        deployInfraNova.setIsEnabled(true);
        deployArray[0] = deployInfraNova;
        InfrastructureConfigDTO deployInfraEther = new InfrastructureConfigDTO();
        deployInfraEther.setInfrastructureName(Platform.ETHER.getName());
        deployInfraEther.setIsDefault(false);
        deployInfraEther.setIsEnabled(false);
        deployArray[1] = deployInfraEther;
        InfrastructureConfigDTO deployInfraAws = new InfrastructureConfigDTO();
        deployInfraAws.setInfrastructureName(Platform.AWS.getName());
        deployInfraAws.setIsDefault(false);
        deployInfraAws.setIsEnabled(false);
        deployArray[2] = deployInfraAws;
        InfrastructureConfigDTO deployInfraNovaEhter = new InfrastructureConfigDTO();
        deployInfraNovaEhter.setInfrastructureName(Platform.NOVAETHER.getName());
        deployInfraNovaEhter.setIsDefault(false);
        deployInfraNovaEhter.setIsEnabled(false);
        deployArray[3] = deployInfraNovaEhter;

        InfrastructureConfigDTO logInfraNova = new InfrastructureConfigDTO();
        logInfraNova.setInfrastructureName(Platform.NOVA.getName());
        logInfraNova.setIsDefault(true);
        logInfraNova.setIsEnabled(true);
        logArray[0] = logInfraNova;
        InfrastructureConfigDTO logInfraEther = new InfrastructureConfigDTO();
        logInfraEther.setInfrastructureName(Platform.ETHER.getName());
        logInfraEther.setIsDefault(false);
        logInfraEther.setIsEnabled(false);
        logArray[1] = logInfraEther;
        InfrastructureConfigDTO logInfraAws = new InfrastructureConfigDTO();
        logInfraAws.setInfrastructureName(Platform.AWS.getName());
        logInfraAws.setIsDefault(false);
        logInfraAws.setIsEnabled(false);
        logArray[2] = logInfraAws;
        InfrastructureConfigDTO logInfraNovaEther = new InfrastructureConfigDTO();
        logInfraNovaEther.setInfrastructureName(Platform.NOVAETHER.getName());
        logInfraNovaEther.setIsDefault(false);
        logInfraNovaEther.setIsEnabled(false);
        logArray[3] = logInfraNovaEther;

        infrastructureConfiguredByEnvDTO.setDeployInfra(deployArray);
        infrastructureConfiguredByEnvDTO.setLoggingInfra(logArray);

        return infrastructureConfiguredByEnvDTO;
    }

}
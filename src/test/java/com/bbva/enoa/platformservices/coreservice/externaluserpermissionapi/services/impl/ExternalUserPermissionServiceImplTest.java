package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.services.impl;
import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PermissionDTO;
import com.bbva.enoa.apirestgen.externaluserpermissionapi.model.PlatformDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermission;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermissionConfig;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ExternalUserPermissionConfigRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ExternalUserPermissionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.util.ExternalUserConstants;
import com.bbva.enoa.utils.clientsutils.model.Activity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalUserPermissionServiceImplTest
{
    @Mock
    private ExternalUserPermissionConfigRepository configRepository;

    @Mock
    private IProductUsersClient usersClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IEtherManagerClient etherManagerClient;

    @Mock
    private ExternalUserPermissionRepository externalUserPermissionRepository;

    @Mock
    private INovaActivityEmitter novaActivityEmitter;

    @InjectMocks
    private ExternalUserPermissionServiceImpl permissionService;

    @Test
    void testCreatePermission_NullPermissionError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(null, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [data] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_NullPlatformError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);

        final PermissionDTO permissionDTO = new PermissionDTO();

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [platform] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_EmptyEnvironmentError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment("");

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [environment] does not have the correct value []", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_EmptyPermissionError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission("");

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [permission] does not have the correct value []", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_NullPermissionTypeError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [permission type] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_NullServiceError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [service] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_NullResourceTypeError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [resource type] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_EmptyUserError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser("");

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [user] does not have the correct value []", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_NullProductError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final Integer product = -1;

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser(user);
        permissionDTO.setProductId(product);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [product] does not have the correct value [-1]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_ProductNotFoundError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final Integer productId = RandomUtils.nextInt(1, 30);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser(user);
        permissionDTO.setProductId(productId);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.productRepository.fetchById(eq(productId))).thenReturn(null);

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.PRODUCT_NOT_FOUND_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The product id ["+productId+"] was not found in NOVA database", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_BadPlatformError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.NOVA;
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final Integer productId = RandomUtils.nextInt(1, 30);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser(user);
        permissionDTO.setProductId(productId);

        final Product product = Mockito.mock(Product.class);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.productRepository.fetchById(eq(productId))).thenReturn(product);

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.PLATFORM_NOT_SUPPORTED_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The platform ["+platform+"] is not supported", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testCreatePermission_BadEnvironmentError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.ETHER;
        final Environment environment = Environment.LAB_PRO;
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final Integer productId = RandomUtils.nextInt(1, 30);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser(user);
        permissionDTO.setProductId(productId);

        final Product product = Mockito.mock(Product.class);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.productRepository.fetchById(eq(productId))).thenReturn(product);

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.createPermission(permissionDTO, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [environment] does not have the correct value [LAB_PRO]", novaException.getNovaError().getErrorMessage());
    }

    @ParameterizedTest
    @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
    void testCreatePermission(final Environment environment)
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Platform platform = Platform.ETHER;
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final String resources = RandomStringUtils.randomAlphabetic(9);
        final String namespace = RandomStringUtils.randomAlphabetic(10);
        final Integer productId = RandomUtils.nextInt(1, 30);

        final PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPlatform(platform.name());
        permissionDTO.setEnvironment(environment.name());
        permissionDTO.setPermission(permission);
        permissionDTO.setPermissionType(permissionType);
        permissionDTO.setService(service);
        permissionDTO.setResourceType(resourceType);
        permissionDTO.setUser(user);
        permissionDTO.setProductId(productId);
        permissionDTO.setResources(resources);

        final Product product = Mockito.mock(Product.class);
        switch (environment)
        {
            case INT:
                when(product.getEtherNsInt()).thenReturn(namespace);
                break;
            case PRE:
                when(product.getEtherNsPre()).thenReturn(namespace);
                break;
            case PRO:
                when(product.getEtherNsPro()).thenReturn(namespace);
                break;
        }

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.productRepository.fetchById(eq(productId))).thenReturn(product);

        this.permissionService.createPermission(permissionDTO, ivUser);

        // then
        final ArgumentCaptor<ExternalUserPermission> permissionCaptor = ArgumentCaptor.forClass(ExternalUserPermission.class);
        verify(this.etherManagerClient, times(1)).setPermission(permissionCaptor.capture(), eq(namespace), eq(environment.name()));

        Assertions.assertEquals(platform, permissionCaptor.getValue().getPlatform());
        Assertions.assertEquals(environment.getEnvironment(), permissionCaptor.getValue().getEnvironment());
        Assertions.assertEquals(permission, permissionCaptor.getValue().getPermission());
        Assertions.assertEquals(permissionType, permissionCaptor.getValue().getPermissionType());
        Assertions.assertEquals(service, permissionCaptor.getValue().getService());
        Assertions.assertEquals(user, permissionCaptor.getValue().getConsumer());
        Assertions.assertEquals(productId, permissionCaptor.getValue().getProductId());
        Assertions.assertEquals(resources, permissionCaptor.getValue().getResources());
        Assertions.assertEquals(resourceType, permissionCaptor.getValue().getResourceType());
        Assertions.assertEquals(productId, permissionCaptor.getValue().getProductId());
        Assertions.assertEquals(namespace, permissionCaptor.getValue().getProductProperty());
        Assertions.assertNull(permissionCaptor.getValue().getId());

        final ArgumentCaptor<ExternalUserPermission> permissionSavingCaptor = ArgumentCaptor.forClass(ExternalUserPermission.class);
        verify(this.externalUserPermissionRepository, times(1)).saveAndFlush(permissionSavingCaptor.capture());
        Assertions.assertEquals(permissionCaptor.getValue(), permissionSavingCaptor.getValue());

        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(Activity.class));
    }

    @Test
    void testDeletePermission_NullIdError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.deletePermission(null, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [permission id] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testDeletePermission_PermissionNotFoundError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Integer permission = RandomUtils.nextInt(1, 20);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.externalUserPermissionRepository.findById(eq(permission))).thenReturn(Optional.empty());

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.deletePermission(permission, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.PERMISSION_NOT_FOUND_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The permission id ["+permission+"] was not found in NOVA database", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testDeletePermission_BadPlatformError()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Integer permission = RandomUtils.nextInt(1, 20);

        final ExternalUserPermission externalUserPermission = Mockito.mock(ExternalUserPermission.class);
        when(externalUserPermission.getPlatform()).thenReturn(Platform.NOVA);

        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.externalUserPermissionRepository.findById(eq(permission))).thenReturn(Optional.of(externalUserPermission));

        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.deletePermission(permission, ivUser));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.PLATFORM_NOT_SUPPORTED_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The platform [NOVA] is not supported", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testDeletePermission()
    {
        // given
        final String ivUser = RandomStringUtils.randomAlphabetic(5);
        final Integer permission = RandomUtils.nextInt(1, 20);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final ExternalUserPermission externalUserPermission = Mockito.mock(ExternalUserPermission.class);
        when(externalUserPermission.getPlatform()).thenReturn(Platform.ETHER);
        when(externalUserPermission.getProductProperty()).thenReturn(namespace);
        when(externalUserPermission.getEnvironment()).thenReturn(environment.getEnvironment());


        // when
        doNothing().when(this.usersClient).checkHasPermission(eq(ivUser), eq(ExternalUserConstants.EXTERNAL_USER_PERMISSION), any(RuntimeException.class));

        when(this.externalUserPermissionRepository.findById(eq(permission))).thenReturn(Optional.of(externalUserPermission));

        this.permissionService.deletePermission(permission, ivUser);

        // then
        verify(this.etherManagerClient, times(1)).unsetPermission(eq(externalUserPermission), eq(namespace), eq(environment.name()));
        verify(this.externalUserPermissionRepository, times(1)).deleteById(eq(permission));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(Activity.class));
    }

    @Test
    void testGetPermissions_EmptyEnvironmentError()
    {
        // given
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        // when
        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.getPermissions(environment.name(), null));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [product] does not have the correct value [null]", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testGetPermissions()
    {
        // given
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Integer productId = RandomUtils.nextInt(1, 30);
        final Platform platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final String permission = RandomStringUtils.randomAlphabetic(6);
        final String permissionType = RandomStringUtils.randomAlphabetic(3);
        final String service = RandomStringUtils.randomAlphabetic(4);
        final String resourceType = RandomStringUtils.randomAlphabetic(7);
        final String user = RandomStringUtils.randomAlphabetic(8);
        final String resources = RandomStringUtils.randomAlphabetic(9);
        final String namespace = RandomStringUtils.randomAlphabetic(10);
        final Integer id = RandomUtils.nextInt(1, 30);

        final ExternalUserPermission externalUserPermission = new ExternalUserPermission();
        externalUserPermission.setId(id);
        externalUserPermission.setPlatform(platform);
        externalUserPermission.setEnvironment(environment.getEnvironment());
        externalUserPermission.setPermission(permission);
        externalUserPermission.setPermissionType(permissionType);
        externalUserPermission.setService(service);
        externalUserPermission.setResourceType(resourceType);
        externalUserPermission.setConsumer(user);
        externalUserPermission.setProductId(productId);
        externalUserPermission.setResources(resources);
        externalUserPermission.setProductProperty(namespace);

        // when
        when(this.externalUserPermissionRepository.findByProductIdAndEnvironment(eq(productId), eq(environment.getEnvironment()))).thenReturn(List.of(externalUserPermission));

        final PermissionDTO[] permissionDTOS = this.permissionService.getPermissions(environment.name(), productId);

        // then
        Assertions.assertEquals(1, permissionDTOS.length);
        Assertions.assertEquals(platform.name(), permissionDTOS[0].getPlatform());
        Assertions.assertEquals(permission, permissionDTOS[0].getPermission());
        Assertions.assertEquals(permissionType, permissionDTOS[0].getPermissionType());
        Assertions.assertEquals(environment.name(), permissionDTOS[0].getEnvironment());
        Assertions.assertEquals(service, permissionDTOS[0].getService());
        Assertions.assertEquals(resources, permissionDTOS[0].getResources());
        Assertions.assertEquals(resourceType, permissionDTOS[0].getResourceType());
        Assertions.assertEquals(user, permissionDTOS[0].getUser());
        Assertions.assertEquals(id, permissionDTOS[0].getId());
        Assertions.assertEquals(productId, permissionDTOS[0].getProductId());
    }

    @Test
    void testGetPermissions_NullProductIdError()
    {
        // when
        final NovaException novaException = Assertions.assertThrows(NovaException.class, () -> this.permissionService.getPermissions("", null));

        // then
        Assertions.assertEquals(ExternalUserConstants.Errors.BAD_REQUEST_ERROR, novaException.getNovaError().getErrorCode());
        Assertions.assertEquals("The field [environment] does not have the correct value []", novaException.getNovaError().getErrorMessage());
    }

    @Test
    void testGetPlatformPermissions()
    {
        // given
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Integer productId = RandomUtils.nextInt(1, 30);

        final ExternalUserPermissionConfig config1 = new ExternalUserPermissionConfig();
        config1.setPlatform(Platform.NOVA);
        config1.setService("service1");
        config1.setResourceType("type1");
        config1.setPermission("permission1");
        config1.setPermissionType("permissionType1");

        final ExternalUserPermissionConfig config2 = new ExternalUserPermissionConfig();
        config2.setPlatform(Platform.NOVA);
        config2.setService("service1");
        config2.setResourceType("type1");
        config2.setPermission("permission2");
        config2.setPermissionType("permissionType2");

        final ExternalUserPermissionConfig config3 = new ExternalUserPermissionConfig();
        config3.setPlatform(Platform.NOVA);
        config3.setService("service1");
        config3.setResourceType("type2");
        config3.setPermission("permission1");
        config3.setPermissionType("permissionType1");

        final ExternalUserPermissionConfig config4 = new ExternalUserPermissionConfig();
        config4.setPlatform(Platform.NOVA);
        config4.setService("service2");
        config4.setResourceType("type1");
        config4.setPermission("permission1");
        config4.setPermissionType("permissionType1");

        final ExternalUserPermissionConfig config5 = new ExternalUserPermissionConfig();
        config5.setPlatform(Platform.ETHER);
        config5.setService("service1");
        config5.setResourceType("type1");
        config5.setPermission("permission1");
        config5.setPermissionType("permissionType1");

        when(configRepository.findAll()).thenReturn(List.of(config1, config2, config3, config4, config5));

        // when
        final PlatformDTO[] platformDTOArray = this.permissionService.getPlatformPermissions(environment.name(), productId);

        // then
        Assertions.assertEquals(2, platformDTOArray.length);
        Assertions.assertEquals(Platform.NOVA.getName(), platformDTOArray[0].getName());
        Assertions.assertEquals(2, platformDTOArray[0].getServices().length);
        Assertions.assertEquals("service1", platformDTOArray[0].getServices()[0].getName());
        Assertions.assertEquals(0, platformDTOArray[0].getServices()[0].getResourcesType()[0].getResources().length);

        Assertions.assertEquals(2, platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions().length);
        Assertions.assertEquals("permission1", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermission());
        Assertions.assertEquals("permissionType1", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermissionType());
        Assertions.assertEquals("permission2", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[1].getPermission());
        Assertions.assertEquals("permissionType2", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[1].getPermissionType());

        Assertions.assertEquals(2, platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions().length);
        Assertions.assertEquals("permission1", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermission());
        Assertions.assertEquals("permissionType1", platformDTOArray[0].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermissionType());


        Assertions.assertEquals("service2", platformDTOArray[0].getServices()[1].getName());
        Assertions.assertEquals(1, platformDTOArray[0].getServices()[1].getResourcesType()[0].getPermissions().length);
        Assertions.assertEquals("permission1", platformDTOArray[0].getServices()[1].getResourcesType()[0].getPermissions()[0].getPermission());
        Assertions.assertEquals("permissionType1", platformDTOArray[0].getServices()[1].getResourcesType()[0].getPermissions()[0].getPermissionType());



        Assertions.assertEquals(Platform.ETHER.getName(), platformDTOArray[1].getName());
        Assertions.assertEquals(1, platformDTOArray[1].getServices().length);
        Assertions.assertEquals("service1", platformDTOArray[1].getServices()[0].getName());
        Assertions.assertEquals(0, platformDTOArray[1].getServices()[0].getResourcesType()[0].getResources().length);
        Assertions.assertEquals(1, platformDTOArray[1].getServices()[0].getResourcesType()[0].getPermissions().length);
        Assertions.assertEquals("permission1", platformDTOArray[1].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermission());
        Assertions.assertEquals("permissionType1", platformDTOArray[1].getServices()[0].getResourcesType()[0].getPermissions()[0].getPermissionType());


    }
}
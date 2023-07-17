package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.ManagementConfigDto;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseEnvConfigDto;
import com.bbva.enoa.apirestgen.releasesapi.model.SelectedPlatformsDto;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReleaseValidatorTest
{
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ReleaseRepository releaseRepository;
    @InjectMocks
    private ReleaseValidator releaseValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void checkProductExistenceOk()
    {
        Assertions.assertDoesNotThrow( () ->  releaseValidator.checkProductExistence(new Product()),
                "A valid product does not throw a exception" );
    }
    @Test
    void checkProductExistenceOkById()
    {
        Product p = new Product();
        p.setId(RandomUtils.nextInt(0,1000));
        Assertions.assertDoesNotThrow( () ->  releaseValidator.checkProductExistence(p.getId(), p),
                "A valid product does not throw a exception" );
    }

    @Test
    void checkProductExistenceNull()
    {
       Assertions.assertThrows(NovaException.class,
               () ->  releaseValidator.checkProductExistence(null), "A null has to throw a NovaException" );
    }

    @Test
    void checkProductExistenceNullById()
    {
        Assertions.assertThrows(NovaException.class,() ->  releaseValidator.checkProductExistence(1, null),
                "A null product has to throw a NovaException" );
    }

    @Test
    void checkProductExistenceNullMessage()
    {
        try
        {
            releaseValidator.checkProductExistence(null);
        }
        catch (Exception e)
        {
            Assertions.assertTrue(e instanceof  NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_PRODUCT_DOESNT_EXIST,novaError.getErrorCode());
            Assertions.assertNotNull(novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.ERROR,novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.NOT_FOUND,novaError.getHttpStatus());
        }
    }

    @Test
    void checkProductExistenceNullMessageById()
    {
        try
        {
            releaseValidator.checkProductExistence(1, null);
            Assertions.fail("Expected Exception");
        }
        catch (Exception e)
        {
            Assertions.assertTrue(e instanceof  NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_PRODUCT_DOESNT_EXIST,novaError.getErrorCode());
            Assertions.assertNotNull(novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.ERROR,novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.NOT_FOUND,novaError.getHttpStatus());
        }
    }

    @Test
    void checkReleaseExistenceNull()
    {
        Release release = null;
        Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkReleaseExistence(release), "A null Release has to throw a NovaException");
    }

    @Test
    void checkReleaseExistenceNoNull()
    {
        Release release = new Release();
        Assertions.assertDoesNotThrow(()-> releaseValidator.checkReleaseExistence(release), "A non-null Release does not to throw a NovaException");
    }

    @Test
    void checkReleaseExistenceNotFound()
    {
        Integer id = 0;
        Mockito.when(releaseRepository.findById(id)).thenReturn(Optional.ofNullable(null));
        Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkReleaseExistence(id), "A not found Release has to throw a NovaException");
        Mockito.verify(this.releaseRepository,Mockito.times(1)).findById(id);
    }

    @Test
    void checkReleaseExistenceNotFoundCheckException()
    {
        Integer id = 0;
        Mockito.when(releaseRepository.findById(id)).thenReturn(Optional.ofNullable(null));
        try
        {
            releaseValidator.checkReleaseExistence(id);
            Assertions.fail("Expected Exception");
        }
        catch (Exception e)
        {
            Mockito.verify(this.releaseRepository,Mockito.times(1)).findById(id);
            Assertions.assertTrue(e instanceof  NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_NO_SUCH_RELEASE,novaError.getErrorCode());
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.MSG_NO_SUCH_RELEASE, novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.ERROR,novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.NOT_FOUND,novaError.getHttpStatus());
        }
    }

    @Test
    void checkReleaseExistence()
    {
        Integer id = RandomUtils.nextInt(1,100);
        Release expected = new Release();
        expected.setId(id);
        Mockito.when(releaseRepository.findById(id)).thenReturn(Optional.of(expected));
        Release current  = releaseValidator.checkReleaseExistence(id);
        Mockito.verify(this.releaseRepository,Mockito.times(1)).findById(id);
        Assertions.assertEquals(expected,current);
    }

    @Test
    public void existsReleaseWithSameNameOk()
    {
        int id = RandomUtils.nextInt(1,100);
        String name = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(this.releaseRepository.existsReleaseWithSameName(id, name)).thenReturn(true);

        String other = RandomStringUtils.randomAlphabetic(10) + "other";
        Assertions.assertDoesNotThrow( () -> this.releaseValidator.existsReleaseWithSameName(id, other));
        Mockito.verify(this.releaseRepository,Mockito.times(1)).existsReleaseWithSameName(id,other);
    }
    @Test
    public void existsReleaseWithSameNameKo()
    {
        int id = RandomUtils.nextInt(1,100);
        String name = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(this.releaseRepository.existsReleaseWithSameName(id, name)).thenReturn(true);
        Assertions.assertThrows(NovaException.class, () -> this.releaseValidator.existsReleaseWithSameName(id, name));
        Mockito.verify(this.releaseRepository,Mockito.times(1)).existsReleaseWithSameName(id,name);
    }

    @Test
    public void existsReleaseWithSameNameKoCheckException()
    {
        int id = RandomUtils.nextInt(1,100);
        String name = RandomStringUtils.randomAlphabetic(10);
        Mockito.when(this.releaseRepository.existsReleaseWithSameName(id, name)).thenReturn(true);

        try{
            this.releaseValidator.existsReleaseWithSameName(id, name);
            Assertions.fail("Expected Exception");
        }
        catch (Exception e)
        {
            Mockito.verify(this.releaseRepository,Mockito.times(1)).existsReleaseWithSameName(id,name);
            Assertions.assertTrue(e instanceof  NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_RELEASE_NAME_DUPLICATED,novaError.getErrorCode());
            Assertions.assertNotNull(novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.WARNING,novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.CONFLICT,novaError.getHttpStatus());
        }
    }

    @Test
    void checkReleaseNameOk()
    {
        String validName = "abcdefghijklmnotqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Assertions.assertDoesNotThrow(() -> this.releaseValidator.checkReleaseName(validName),"A valid name for a Release cannot throw a Exception");
    }

    @Test
    void checkReleaseNameKo()
    {
        //Null name is invalid
        Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(null),"null is not a valid name for Release ");

        //Empty name is invalid
        Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(""),"empty is not a valid name for Release ");

        //Invalids chars
        String invalidChars = "!@#·$~%&¬/()=?¿+*`^[]´¨{},;.:-_<>ºª\\|\"";
        Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(invalidChars),"empty is not a valid name for Release ");

        //Checker of a valid name
        String validName = "Release001";
        Assertions.assertDoesNotThrow(() -> this.releaseValidator.checkReleaseName(validName),"A valid name for a Release cannot throw a Exception");


        for(char c : invalidChars.toCharArray())
        {
            //Each invalid char is an invalid name by itself
            Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(String.valueOf(c)),"invalid char  is not a valid name for Release ");
            //A valid name with an invalid char is invalid name (independent of the position)
            Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(validName + c),"A valid name + invalid char is not a valid name for Release ");
            Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(c + validName ),"An invalid char +  valid name is not a valid name for Release ");
            Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(c + validName + c),"A valid name surrounded by invalid chars  is not a valid name for Release ");
            Assertions.assertThrows(NovaException.class, ()-> this.releaseValidator.checkReleaseName(validName + c + validName ),"A valid name + invalid char + valid name is not a valid name for Release ");
        }
    }

    @Test
    void checkReleaseNameMessage()
    {
        //Invalids chars
        String invalidName = "@Invalid!Name?";
        try
        {
            this.releaseValidator.checkReleaseName(invalidName);
            Assertions.fail("Expected Exception");
        }
        catch(Exception e)
        {
            Assertions.assertTrue(e instanceof  NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertTrue(e.getMessage().contains(invalidName));
            Assertions.assertNotNull(novaError);
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_SPECIAL_CHARACTERS,novaError.getErrorCode());
            Assertions.assertNotNull(novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.WARNING,novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.CONFLICT,novaError.getHttpStatus());
        }
    }

    @Test
    void checkInputDeploymentPlatformValueNull()
    {
        //if everything is null -> return null
        Platform current = releaseValidator.checkInputDeploymentPlatformValue(null, null, null, null);
        Assertions.assertNull(current);
    }

    @Test
    void checkInputDeploymentPlatformValueOldValue()
    {
        //inputs
        List<Platform> availablePlatformDeploy = Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList());
        ReleaseEnvConfigDto releaseEnvConfig;
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();

        //All environment
        for(Environment environment : Environment.values())
        {
            //all destination platform
            for(Platform expected  : Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
            {
                releaseEnvConfig = null;
                //if everything is null -> return old value (expected)
                Platform current = releaseValidator.checkInputDeploymentPlatformValue(availablePlatformDeploy, releaseEnvConfig, expected, environment.name());
                Assertions.assertEquals(expected,current, "Invalid input values does not return old DeploymentPlatformType value");

                //if releaseEnvConfig has a SelectedPlatform as null -> return old value (expected)
                releaseEnvConfig = new ReleaseEnvConfigDto();
                releaseEnvConfig.setSelectedPlatforms(null);
                releaseEnvConfig.setManagementConfig(null);
                current = releaseValidator.checkInputDeploymentPlatformValue(availablePlatformDeploy, releaseEnvConfig, expected, environment.name());
                Assertions.assertEquals(expected,current, "No SelectedPlatform input values does not return old DeploymentPlatformType value");

                //if releaseEnvConfig has a SelectedPlatform with DeploymentPlatform as null-> return old value (expected)
                selectedPlatformsDto.setDeploymentPlatform(null);
                releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
                releaseEnvConfig.setManagementConfig(managementConfigDto);
                current = releaseValidator.checkInputDeploymentPlatformValue(availablePlatformDeploy, releaseEnvConfig, expected, environment.name());
                Assertions.assertEquals(expected,current, "SelectedPlatform as empty values does not return old DeploymentPlatformType value");

                //if releaseEnvConfig has a SelectedPlatform with DeploymentPlatform as empty-> return old value (expected)
                selectedPlatformsDto.setDeploymentPlatform("");
                releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
                current = releaseValidator.checkInputDeploymentPlatformValue(availablePlatformDeploy, releaseEnvConfig, expected, environment.name());
                Assertions.assertEquals(expected,current, "SelectedPlatform as empty values values does not return old DeploymentPlatformType value");
            }
        }

    }

    @Test
    void checkInputDeploymentPlatformValueAll()
    {

        //inputs -> all deployment types allowed
        List<Platform> availablePlatformDeploy = Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList());

        Platform old  = Platform.NOVA;
        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);

        Platform current;

        for(Environment environment : Environment.values())
        {
            for(Platform type : Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
            {
                selectedPlatformsDto.setDeploymentPlatform(type.name());
                current = releaseValidator.checkInputDeploymentPlatformValue(availablePlatformDeploy, releaseEnvConfig, old, environment.name());
                Assertions.assertNotNull(current);
                Assertions.assertEquals(type, current);
            }
        }

    }

    @Test
    void checkInputDeploymentPlatformValueNoAllowed()
    {
        Platform old  = Platform.NOVA;
        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);

        Platform current;
        for(Environment environment : Environment.values())
        {
            //Only NOVA is allowed
            final Platform availablePlatformDeployNOVA = Platform.NOVA;
            selectedPlatformsDto.setDeploymentPlatform(Platform.NOVA.name());
            current = releaseValidator.checkInputDeploymentPlatformValue(Collections.singletonList(availablePlatformDeployNOVA), releaseEnvConfig, old, environment.name());
            Assertions.assertNotNull(current);
            Assertions.assertEquals(Platform.NOVA, current);

            //If try another... exception
            selectedPlatformsDto.setDeploymentPlatform(Platform.ETHER.name());
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputDeploymentPlatformValue(Collections.singletonList(availablePlatformDeployNOVA), releaseEnvConfig, old, environment.name()));


            //Only ETHER is allowed
            final Platform availablePlatformDeployETHER = Platform.ETHER;
            selectedPlatformsDto.setDeploymentPlatform(Platform.ETHER.name());
            current = releaseValidator.checkInputDeploymentPlatformValue(Collections.singletonList(availablePlatformDeployETHER), releaseEnvConfig, old, environment.name());
            Assertions.assertNotNull(current);
            Assertions.assertEquals(Platform.ETHER, current);

            //If try another... exception
            selectedPlatformsDto.setDeploymentPlatform(Platform.NOVA.name());
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputDeploymentPlatformValue(Collections.singletonList(availablePlatformDeployETHER), releaseEnvConfig, old, environment.name()));
        }

    }

    @Test
    void checkInputDeploymentPlatformValueInvalidDestinationPlatform()
    {
        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);
        selectedPlatformsDto.setDeploymentPlatform("INVALID");

        for(Environment environment : Environment.values())
        {
            for(Platform availablePlatformDeployType : Platform.values())
            {
                Assertions.assertThrows(NovaException.class,
                        () ->  releaseValidator.checkInputDeploymentPlatformValue(Collections.singletonList(Platform.NOVA), releaseEnvConfig, Platform.NOVA, environment.name()));
            }
        }
    }

    @Test
    void checkInputLoggingPlatformValueNull()
    {
        //if everything is null -> return null
        Platform current = releaseValidator.checkInputLoggingPlatformValue(null, null, null, null,null);
        Assertions.assertNull(current);
    }

    @Test
    void checkInputLoggingPlatformValueOldValue()
    {
        //inputs
        Platform destinationDeploy = Platform.NOVA;
        Platform availableDestLogging = Platform.NOVAETHER;
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ReleaseEnvConfigDto releaseEnvConfig;

        for(Environment environment : Environment.values())
        {
            for(Platform expected : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                //if everything is null -> return old value (expected)
                releaseEnvConfig = null;
                Platform current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableDestLogging), releaseEnvConfig, expected, destinationDeploy, environment.name());
                Assertions.assertEquals(expected,current, "Invalid input values does not return old DestinationPlatformLoggingType value");

                //if releaseEnvConfig has a SelectedPlatform as null -> return old value (expected)
                releaseEnvConfig = new ReleaseEnvConfigDto();
                releaseEnvConfig.setSelectedPlatforms(null);
                releaseEnvConfig.setManagementConfig(null);
                current = current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableDestLogging), releaseEnvConfig, expected, destinationDeploy, environment.name());
                Assertions.assertEquals(expected,current, "No SelectedPlatform input values does not return old DestinationPlatformLoggingType value");

                //if releaseEnvConfig has a SelectedPlatform with LoggingPlatform as null-> return old value (expected)
                releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
                releaseEnvConfig.setManagementConfig(managementConfigDto);
                selectedPlatformsDto.setLoggingPlatform(null);
                current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableDestLogging), releaseEnvConfig, expected, destinationDeploy, environment.name());
                Assertions.assertEquals(expected,current, "SelectedPlatform as empty values does not return old DestinationPlatformLoggingType value");

                //if releaseEnvConfig has a SelectedPlatform with LoggingPlatform as empty-> return old value (expected)
                selectedPlatformsDto.setLoggingPlatform("");
                current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableDestLogging), releaseEnvConfig, expected, destinationDeploy, environment.name());
                Assertions.assertEquals(expected,current, "SelectedPlatform as empty values values does not return old DestinationPlatformLoggingType value");
            }
        }

    }

    @Test
    void checkInputLoggingPlatformValueAll()
    {
        //inputs -> all deployment types allowed
        Platform destinationDeploy = Platform.NOVA;
        List<Platform> availableDestLogging = new ArrayList<>();
        availableDestLogging.add(Platform.NOVA);
        availableDestLogging.add(Platform.ETHER);
        availableDestLogging.add(Platform.AWS);
        availableDestLogging.add(Platform.NOVAETHER);

        Platform old  = Platform.NOVAETHER;

        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);

        Platform current;
        for(Environment environment : Environment.values())
        {
            for(Platform type : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                selectedPlatformsDto.setLoggingPlatform(type.name());
                current = releaseValidator.checkInputLoggingPlatformValue(availableDestLogging, releaseEnvConfig, old, destinationDeploy, environment.name());
                Assertions.assertNotNull(current);
                Assertions.assertEquals(type, current);
            }
        }
    }

    @Test
    void checkInputLoggingPlatformValueOnlyNOVAAllowed()
    {
        Platform destinationDeploy = Platform.NOVA;
        final Platform old  = Platform.NOVAETHER;

        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);

        //Only NOVA is allowed
        final Platform availableLoggingNOVA = Platform.NOVA;
        Platform current;
        for(Environment environment : Environment.values())
        {
            selectedPlatformsDto.setLoggingPlatform(Platform.NOVA.name());
            current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableLoggingNOVA), releaseEnvConfig, old, destinationDeploy, environment.name());
            Assertions.assertNotNull(current);
            Assertions.assertEquals(Platform.NOVA, current);

            //If try another... exception
            selectedPlatformsDto.setLoggingPlatform(Platform.ETHER.name());
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availableLoggingNOVA), releaseEnvConfig, old, destinationDeploy, environment.name()));
        }
    }

    @Test
    void checkInputLoggingPlatformValueETHERAllowed()
    {
        Platform destinationDeploy = Platform.ETHER;
        final Platform old  = Platform.NOVAETHER;

        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);

            //Only ETHER is allowed
        final Platform availablePlatformLoggingETHER = Platform.ETHER;
        Platform current;
        for(Environment environment : Environment.values())
        {
            selectedPlatformsDto.setLoggingPlatform(Platform.ETHER.name());
            current = releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availablePlatformLoggingETHER), releaseEnvConfig, old, destinationDeploy, environment.name());
            Assertions.assertNotNull(current);
            Assertions.assertEquals(Platform.ETHER, current);

            //If try another... exception
            selectedPlatformsDto.setLoggingPlatform(Platform.NOVA.name());
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(availablePlatformLoggingETHER), releaseEnvConfig, old, destinationDeploy, environment.name()));


            //if Destination Platform DEPLOY is not ETHER --> Exception
            selectedPlatformsDto.setLoggingPlatform(Platform.NOVA.name());
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(Platform.NOVAETHER), releaseEnvConfig, old, destinationDeploy, environment.name()));
            Assertions.assertThrows(NovaException.class, ()-> releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(Platform.NOVA), releaseEnvConfig, old, destinationDeploy, environment.name()));

        }
    }

    @Test
    void checkInputLoggingPlatformValueInvalidDestination()
    {
        ReleaseEnvConfigDto releaseEnvConfig = new ReleaseEnvConfigDto();
        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        releaseEnvConfig.setSelectedPlatforms(selectedPlatformsDto);
        releaseEnvConfig.setManagementConfig(managementConfigDto);
        selectedPlatformsDto.setLoggingPlatform("INVALID");

        for(Environment environment : Environment.values())
        {
            for(Platform destinationPlatformLoggingType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                Assertions.assertThrows(NovaException.class,
                        () ->  releaseValidator.checkInputLoggingPlatformValue(Collections.singletonList(destinationPlatformLoggingType), releaseEnvConfig, Platform.NOVAETHER, Platform.NOVA,
                                environment.name()));
            }
        }
    }
}


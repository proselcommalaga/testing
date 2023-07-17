package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodDto;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.IProfilableApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiMethodProfileRespository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ProfilableDtoBuilderImplTest
{
    @Mock
    private ApiMethodProfileRespository apiMethodProfileRespository;
    @InjectMocks
    private ProfilableDtoBuilderImpl profilableDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiMethodProfileRespository
        );
    }

    @Nested
    class BuildApiMethodDtoArray
    {
        @Test
        @DisplayName("Get API methods -> API method profile not found")
        public void apiMethodProfileNotFound()
        {
            // Given
            final PlanProfile planProfile = buildPlanProfile();
            final String releaseName = getRandomString(25);

            final IProfilableApiVersion apiVersion = buildMockedApiVersion();

            final ApiMethod apiMethod = buildApiMethod();

            when(apiVersion.getApiMethods())
                    .thenReturn(List.of(apiMethod));

            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any()))
                    .thenReturn(null);

            // When
            final NovaException exception = assertThrows(
                    NovaException.class,
                    () -> profilableDtoBuilder.buildApiMethodDtoArray(apiVersion, releaseName, planProfile)
            );

            // Then
            assertEquals(ApiManagerError.getApiMethodProfileNotFoundError(planProfile.getId(), apiMethod.getEndpoint(), apiMethod.getVerb().getVerb()).getErrorCode(), exception.getErrorCode().getErrorCode());

            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);
        }

        @Test
        @DisplayName("Get API methods -> API method profile found, but withour roles")
        public void apiMethodProfileFoundAndWithoutRoles()
        {
            // Given
            final PlanProfile planProfile = buildPlanProfile();
            final String releaseName = getRandomString(25);

            final IProfilableApiVersion apiVersion = buildMockedApiVersion();

            final ApiMethod apiMethod = buildApiMethod();

            when(apiVersion.getApiMethods())
                    .thenReturn(List.of(apiMethod));

            final ApiMethodProfile apiMethodProfile = buildApiMethodProfile(null);

            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any()))
                    .thenReturn(apiMethodProfile);

            // When
            final NovaException exception = assertThrows(
                    NovaException.class,
                    () -> profilableDtoBuilder.buildApiMethodDtoArray(apiVersion, releaseName, planProfile)
            );

            // Then
            assertEquals(ApiManagerError.getApiMethodProfileRolesNotFound(planProfile.getId(), apiMethodProfile.getId()).getErrorCode(), exception.getErrorCode().getErrorCode());

            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);

        }

        @Test
        @DisplayName("Get API methods -> OK")
        public void apiMethodProfileFoundAndWithRoles()
        {
            // Given
            final PlanProfile planProfile = buildPlanProfile();
            final String releaseName = getRandomString(25);

            String basePath = getRandomString(255);

            final IProfilableApiVersion apiVersion = buildMockedApiVersion();

            final ApiMethod apiMethod = this.buildApiMethod();

            final CesRole cesRole = buildCesRole();
            final ApiMethodProfile apiMethodProfile = buildApiMethodProfile(cesRole);

            when(apiVersion.getApiMethods())
                    .thenReturn(List.of(apiMethod));
            when(apiVersion.getBasePath())
                    .thenReturn(basePath);

            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any())).thenReturn(apiMethodProfile);

            // When
            final ApiMethodDto[] retValue = profilableDtoBuilder.buildApiMethodDtoArray(apiVersion, releaseName, planProfile);

            // Then
            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);

            assertEquals(1, retValue.length);
            assertEquals(apiMethod.getId(), retValue[0].getMethodId());
            assertEquals(apiMethod.getDescription(), retValue[0].getDescription());
            assertEquals(apiMethod.getEndpoint(), retValue[0].getEndpoint());
            assertEquals(apiMethod.getVerb().name(), retValue[0].getVerb());
            assertEquals(releaseName + ":" + apiMethod.getVerb().getVerb() + ":" + basePath + apiMethod.getEndpoint(), retValue[0].getSecurityResourceName());
            assertEquals(1, retValue[0].getAssociatedRoles().length);
            assertEquals(cesRole.getId(), retValue[0].getAssociatedRoles()[0].getRoleId());
            assertEquals(cesRole.getRol(), retValue[0].getAssociatedRoles()[0].getRoleName());
        }

        private PlanProfile buildPlanProfile()
        {
            final PlanProfile planProfile = new PlanProfile();
            planProfile.setId(RandomUtils.nextInt(0, 10000));

            return planProfile;
        }

        private String getRandomString(final int length)
        {
            return RandomStringUtils.randomAlphanumeric(length);
        }

        private IProfilableApiVersion buildMockedApiVersion()
        {
            return Mockito.mock(IProfilableApiVersion.class, Answers.RETURNS_DEEP_STUBS);
        }

        private ApiMethod buildApiMethod()
        {
            return (ApiMethod) new ApiMethod()
                    .setEndpoint(RandomStringUtils.randomAlphanumeric(125))
                    .setVerb(Verb.values()[RandomUtils.nextInt(0, Verb.values().length)])
                    .setDescription(RandomStringUtils.randomAlphanumeric(100))
                    .setId(RandomUtils.nextInt(0, 10000));
        }

        private ApiMethodProfile buildApiMethodProfile(final CesRole cesRole)
        {
            final ApiMethodProfile apiMethodProfile = new ApiMethodProfile();
            apiMethodProfile.setId(RandomUtils.nextInt(0, 10000));

            if (null == cesRole)
            {
                apiMethodProfile.setRoles(null);
            }
            else
            {
                apiMethodProfile.setRoles(Collections.singleton(cesRole));
            }

            return apiMethodProfile;
        }

        private CesRole buildCesRole()
        {
            return (CesRole) new CesRole()
                    .setRol(RandomStringUtils.randomAlphanumeric(15))
                    .setId(RandomUtils.nextInt(0, 10000));
        }
    }
}
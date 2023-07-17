package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentLabel;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentLabelRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DeploymentUtilsTest
{

    @Mock
    private DeploymentLabelRepository deploymentLabelRepository;
    @InjectMocks
    private DeploymentUtils deploymentUtils;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

    }

    @Nested
    class GetNumberOfInstancesForEphoenixService
    {
        @Test
        public void withoutEnvironment()
        {

            String environment = "";
            String serviceName = "testing.service.name";

            when(deploymentLabelRepository.findFirstByEnvironmentAndServiceName(environment, serviceName)).thenReturn(null);

            NovaException exception = assertThrows(NovaException.class, () -> deploymentUtils.getNumberOfInstancesForEphoenixService(environment, serviceName));
            assertEquals(DeploymentConstants.DeployErrors.NO_LABEL_EPHOENIX_SERVICE_ERROR, exception.getErrorCode().getErrorCode());
        }

        @Test
        public void withoutServiceName()
        {
            String environment = Environment.PRE.getEnvironment();
            String serviceName = "";

            DeploymentPlan deploymentPlan = new DeploymentPlan();

            when(deploymentLabelRepository.findFirstByEnvironmentAndServiceName(environment, serviceName)).thenReturn(null);

            NovaException exception = assertThrows(NovaException.class, () -> deploymentUtils.getNumberOfInstancesForEphoenixService(environment, serviceName));
            assertEquals(DeploymentConstants.DeployErrors.NO_LABEL_EPHOENIX_SERVICE_ERROR, exception.getErrorCode().getErrorCode());
        }

        @Test
        public void withEnvironmentAndServiceNameOK()
        {
            String environment = Environment.PRE.getEnvironment();
            String serviceName = "testing.service.name";
            String labels = "productpre1,spring1,productpre2";
            DeploymentLabel deploymentLabel = new DeploymentLabel();
            deploymentLabel.setLabels(labels);

            when(deploymentLabelRepository.findFirstByEnvironmentAndServiceName(environment, serviceName)).thenReturn(deploymentLabel);

            int numberOfInstances = deploymentUtils.getNumberOfInstancesForEphoenixService(environment, serviceName);

            assertEquals(3, numberOfInstances);
        }

        @Test
        public void withEnvironmentAndServiceNameAndEmptyLabels()
        {
            String environment = Environment.PRE.getEnvironment();
            String serviceName = "testing.service.name";
            String labels = "";
            DeploymentLabel deploymentLabel = new DeploymentLabel();
            deploymentLabel.setLabels(labels);

            when(deploymentLabelRepository.findFirstByEnvironmentAndServiceName(environment, serviceName)).thenReturn(deploymentLabel);

            NovaException exception = assertThrows(NovaException.class, () -> deploymentUtils.getNumberOfInstancesForEphoenixService(environment, serviceName));
            assertEquals(DeploymentConstants.DeployErrors.NO_LABEL_EPHOENIX_SERVICE_ERROR, exception.getErrorCode().getErrorCode());
        }

        @Test
        public void withEnvironmentAndServiceNameAndNullLabels()
        {
            String environment = Environment.PRE.getEnvironment();
            String serviceName = "testing.service.name";
            String labels = null;
            DeploymentLabel deploymentLabel = new DeploymentLabel();
            deploymentLabel.setLabels(labels);

            when(deploymentLabelRepository.findFirstByEnvironmentAndServiceName(environment, serviceName)).thenReturn(deploymentLabel);

            NovaException exception = assertThrows(NovaException.class, () -> deploymentUtils.getNumberOfInstancesForEphoenixService(environment, serviceName));
            assertEquals(DeploymentConstants.DeployErrors.NO_LABEL_EPHOENIX_SERVICE_ERROR, exception.getErrorCode().getErrorCode());
        }
    }
}

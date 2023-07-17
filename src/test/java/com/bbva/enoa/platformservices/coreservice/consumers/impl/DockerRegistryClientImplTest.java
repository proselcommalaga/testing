package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.dockerregistryapi.client.feign.nova.rest.IRestHandlerDockerregistryapi;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DockerRegistryClientImplTest
{
    @Mock
    private IRestHandlerDockerregistryapi restHandler;
    @InjectMocks
    private DockerRegistryClientImpl client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(DockerRegistryClientImpl.class);
        client.init();
    }

    @Test
    public void when_is_image_in_registry_returns_ko_response_then_return_false()
    {
        Mockito.when(restHandler.getImageByTag(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.isImageInRegistry("A", "B");

        Assertions.assertFalse(result);
    }

    @Test
    public void when_is_image_in_registry_returns_ok_response_then_return_true()
    {
        Mockito.when(restHandler.getImageByTag(Mockito.anyString())).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        boolean result = client.isImageInRegistry("A", "B");

        Assertions.assertTrue(result);
    }
}
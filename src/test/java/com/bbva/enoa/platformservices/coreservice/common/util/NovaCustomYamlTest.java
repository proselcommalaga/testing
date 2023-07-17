package com.bbva.enoa.platformservices.coreservice.common.util;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NovaCustomYamlTest
{

    @Mock
    private NovaCustomObjectMapperFactory novaCustomObjectMapperFactory;
    @InjectMocks
    private NovaCustomYaml novaCustomYaml;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                novaCustomObjectMapperFactory
        );
    }

    @Nested
    class Pretty
    {
        @Test
        @DisplayName("Create pretty object writer -> with args ok")
        public void withArgsOk()
        {
            Map<YAMLGenerator.Feature, Boolean> featuresMap = Collections.emptyMap();
            var objectMapper = Mockito.mock(ObjectMapper.class);
            var objectWriter = Mockito.mock(ObjectWriter.class);

            when(novaCustomObjectMapperFactory.createYaml(any())).thenReturn(objectMapper);
            when(objectMapper.writer(any(PrettyPrinter.class))).thenReturn(objectWriter);

            ObjectWriter ret = novaCustomYaml.pretty(featuresMap);

            assertEquals(objectWriter, ret);
            verify(novaCustomObjectMapperFactory).createYaml(featuresMap);
            verify(objectMapper).writer(any(PrettyPrinter.class));

        }

        @Test
        @DisplayName("Create pretty object writer -> without args ok")
        public void withoutArgsOk()
        {
            var objectMapper = Mockito.mock(ObjectMapper.class);
            var objectWriter = Mockito.mock(ObjectWriter.class);

            when(novaCustomObjectMapperFactory.createYaml(any())).thenReturn(objectMapper);
            when(objectMapper.writer(any(PrettyPrinter.class))).thenReturn(objectWriter);

            ObjectWriter ret = novaCustomYaml.pretty();

            assertEquals(objectWriter, ret);
            verify(novaCustomObjectMapperFactory).createYaml(null);
            verify(objectMapper).writer(any(PrettyPrinter.class));

        }

    }
    
}

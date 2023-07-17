package com.bbva.enoa.platformservices.coreservice.common.util;

import io.swagger.models.Info;
import io.swagger.models.Swagger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SwaggerConverterTest
{

    private final static String SHORT_DESCRIPTION = "Short description";
    private final static String LONG_DESCRIPTION = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía " +
            "un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. " +
            "Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lantejas los viernes, " +
            "algún palomino de añadidura los domingos, consumían las tres partes de su hacienda.";

    private SwaggerConverter swaggerConverter;

    @BeforeEach
    public void setUp() throws Exception
    {
        var novaCustomObjectMapperFactory = new NovaCustomObjectMapperFactory();
        var novaCustomYaml = new NovaCustomYaml(novaCustomObjectMapperFactory);
        this.swaggerConverter = new SwaggerConverter(novaCustomYaml);
    }

    @Nested
    class SwaggerYamlToByteArray
    {
        @Test
        void shortDescription()
        {
            // Input data
            Swagger swaggerObject = new Swagger();
            Info info = new Info();
            info.setDescription(SHORT_DESCRIPTION);
            swaggerObject.setInfo(info);

            // Expected result
            String expected = String.format(
                    "---\n" +
                    "swagger: \"2.0\"\n" +
                    "info:\n" +
                    "  description: \"%s\"\n",
                    SHORT_DESCRIPTION);

            // Exercise
            String actual = new String(swaggerConverter.swaggerYamlToByteArray(swaggerObject));

            // Verify
            assertEquals(expected, actual);
        }

        @Test
        void longDescription()
        {
            // Input data
            Swagger swaggerObject = new Swagger();
            Info info = new Info();
            info.setDescription(LONG_DESCRIPTION);
            swaggerObject.setInfo(info);

            // Expected result
            String expected = String.format(
                    "---\n" +
                    "swagger: \"2.0\"\n" +
                    "info:\n" +
                    "  description: \"%s\"\n",
                    LONG_DESCRIPTION);

            // Exercise
            String actual = new String(swaggerConverter.swaggerYamlToByteArray(swaggerObject));

            // Verify
            assertEquals(expected, actual);
        }

    }

    @Nested
    class SwaggerJsonToByteArray
    {
        @Test
        void longDescription()
        {
            // Input data
            Swagger swaggerObject = new Swagger();
            Info info = new Info();
            info.setDescription(LONG_DESCRIPTION);
            swaggerObject.setInfo(info);

            // Expected result
            String expected = String.format(
                    "{%n" +
                    "  \"swagger\" : \"2.0\",%n" +
                    "  \"info\" : {%n" +
                    "    \"description\" : \"%s\"%n" +
                    "  }%n" +
                    "}",
                    LONG_DESCRIPTION);

            // Exercise
            String actual = new String(swaggerConverter.swaggerJsonToByteArray(swaggerObject));

            // Verify
            assertEquals(expected, actual);
        }
    }


}
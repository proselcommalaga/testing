package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.LinkedHashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class SwaggerConverter
{
    private final SwaggerParser swaggerParser = new SwaggerParser();
    private final NovaCustomYaml novaCustomYaml;

    public byte[] swaggerYamlToByteArray(Swagger swagger)
    {
        try
        {
            // We can't use the following commented line due to https://github.com/FasterXML/jackson-dataformats-text/issues/215 (withoutFeatures is ignored)
            // Yaml.pretty().withoutFeatures(YAMLGenerator.Feature.SPLIT_LINES).writeValueAsBytes(swagger);
            // So, instead, we use our own custom implementation.
            return this.novaCustomYaml.pretty(Map.of(
                    YAMLGenerator.Feature.SPLIT_LINES, false // Disable long lines splitting (more than 67 characters long with at least one space).
            )).writeValueAsBytes(swagger);
        }
        catch (JsonProcessingException e)
        {
            LinkedHashMap<String, Object> customProperties = (LinkedHashMap<String, Object>) swagger.getVendorExtensions().get("x-generator-properties");
            throw new NovaException(ApiManagerError.getSwaggerToJsonError(customProperties.get("business-unit").toString().toUpperCase(), swagger.getInfo().getTitle(),
                    swagger.getInfo().getVersion()), e.getMessage());
        }

    }

    public byte[] swaggerJsonToByteArray(Swagger swagger)
    {
        try
        {
            return Json.pretty().writeValueAsBytes(swagger);
        }
        catch (JsonProcessingException e)
        {
            LinkedHashMap<String, Object> customProperties = (LinkedHashMap<String, Object>) swagger.getVendorExtensions().get("x-generator-properties");
            throw new NovaException(ApiManagerError.getSwaggerToJsonError(customProperties.get("business-unit").toString().toUpperCase(), swagger.getInfo().getTitle(),
                    swagger.getInfo().getVersion()), e.getMessage());
        }

    }

    public Swagger parseSwaggerFromString(String swaggerContent)
    {
        return swaggerParser.parse(swaggerContent);
    }

}

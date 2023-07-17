package com.bbva.enoa.platformservices.coreservice.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.jackson.mixin.ResponseSchemaMixin;
import io.swagger.models.Response;
import io.swagger.util.DeserializationModule;
import io.swagger.util.ReferenceSerializationConfigurer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Rewriting of part of the class {@link io.swagger.util.ObjectMapperFactory}, due to https://github.com/FasterXML/jackson-dataformats-text/issues/215,
 * which makes something like the following useless:
 * Yaml.pretty().withoutFeatures(YAMLGenerator.Feature.SPLIT_LINES).writeValueAsBytes(swagger);
 * See also {@link NovaCustomYaml}.
 */
@Component
public class NovaCustomObjectMapperFactory
{

    public ObjectMapper create(JsonFactory jsonFactory)
    {
        return new ObjectMapper(jsonFactory);
    }

    /**
     * See {@link io.swagger.util.ObjectMapperFactory}.
     *
     * @param features Enable or disable features of the YAML factory.
     * @return An ObjectMapper.
     */
    public ObjectMapper createYaml(Map<YAMLGenerator.Feature, Boolean> features) {
        // Use our own factory disabling and enabling features:
        YAMLFactory yamlFactory = YAMLFactory.builder().build();
        if (features != null)
        {
            features.forEach((feature, enable) -> {
                if (enable)
                {
                    yamlFactory.enable(feature);
                }
                else
                {
                    yamlFactory.disable(feature);
                }
            });
        }
        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        // includePathDeserializer and includeResponseDeserializer are always true:
        Module deserializerModule = new DeserializationModule(true, true);

        // The rest of the code is the same as the original:
        mapper.registerModule(deserializerModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);

        ReferenceSerializationConfigurer.serializeAsComputedRef(mapper);

        return mapper;
    }

}

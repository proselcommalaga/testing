package com.bbva.enoa.platformservices.coreservice.common.util;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Rewriting of the class {@link io.swagger.util.Yaml}, due to https://github.com/FasterXML/jackson-dataformats-text/issues/215,
 * which makes something like the following useless:
 * Yaml.pretty().withoutFeatures(YAMLGenerator.Feature.SPLIT_LINES).writeValueAsBytes(swagger);
 * See also {@link NovaCustomObjectMapperFactory}.
 */
@Component
@AllArgsConstructor
public class NovaCustomYaml
{
    private final NovaCustomObjectMapperFactory novaCustomObjectMapperFactory;

    /**
     * See {@link io.swagger.util.Yaml}.
     *
     * @param features Enable or disable features of the YAML factory.
     * @return An ObjectWriter.
     */
    public ObjectWriter pretty(Map<YAMLGenerator.Feature, Boolean> features)
    {
        return this.novaCustomObjectMapperFactory.createYaml(features).writer(new DefaultPrettyPrinter());
    }

    /**
     * See {@link io.swagger.util.Yaml}.
     *
     * @return An ObjectMapper.
     */
    public ObjectWriter pretty()
    {
        return this.pretty(null);
    }

}

package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

@Component
public class NovaAgentUtils
{
    public ErrorMessage getErrorMessageFromJson(String string)
    {
        Gson gson = new Gson();
        return gson.fromJson(string, ErrorMessage.class);
    }
}

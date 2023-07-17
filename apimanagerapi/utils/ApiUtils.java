package com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ApiUtils
{
    /**
     * BAse64 decoding
     *
     * @param value encoded string value
     * @return decoded string
     */
    public String decodeBase64(String value)
    {
        byte[] decodedValue = Base64.getDecoder().decode(value);  // Basic Base64 decoding
        return new String(decodedValue, StandardCharsets.UTF_8);
    }

    public String buildXmasApiBasePath(final String uuaa, final String title, final String version)
    {
        return String.format("/%s/%s/%s", uuaa, title, version).toLowerCase();
    }

    /**
     * Adds a slash at the beginning of the base path
     *
     * @param basePath Base path as String
     * @return basepath with a slash at the beginning
     */
    public String ensureStartingSlash(final String basePath)
    {
        String newBasePath = basePath;
        if (basePath == null)
        {
            newBasePath = Constants.PATH_SLASH;
        }
        else if (basePath.indexOf(Constants.PATH_SLASH) != 0)
        {
            newBasePath = Constants.PATH_SLASH + basePath;
        }

        return newBasePath;
    }
}

package com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiUtilsTest
{
    private final ApiUtils apiUtils = new ApiUtils();

    @Nested
    class EnsureStartingSlash
    {
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Ensure starting slash -> null and empty base path")
        public void nullAndEmpty(final String originalBasePath)
        {
            final var ret = apiUtils.ensureStartingSlash(originalBasePath);

            assertEquals(Constants.PATH_SLASH, ret);
        }

        @Test
        @DisplayName("Ensure starting slash -> Path without starting slash")
        public void pathWithoutFinalSlash()
        {
            final var originalBasePath = "basepath";

            final String ret = apiUtils.ensureStartingSlash(originalBasePath);

            assertEquals(Constants.PATH_SLASH + originalBasePath, ret);
        }

        @Test
        @DisplayName("Ensure starting slash -> do nothing")
        public void doNothing()
        {
            final var originalBasePath = "/basepath";

            final String ret = apiUtils.ensureStartingSlash(originalBasePath);

            assertEquals(originalBasePath, ret);
        }
    }

    @Nested
    class BuildXmasApiBasePath
    {
        @Test
        @DisplayName("Build XMAS API base path -> ok")
        public void ok()
        {
            final var uuaa = "JGMV";
            final var title = "title";
            final var version = "version";

            final String ret = apiUtils.buildXmasApiBasePath(uuaa, title, version);

            assertEquals(String.format("/%s/%s/%s", uuaa, title, version).toLowerCase() , ret);
        }
    }

    @Nested
    class DecodeBase64
    {
        @Test
        @DisplayName("Decode Base64 -> ok")
        public void ok()
        {
            final var content = "Esto es una prueba de Base64";
            final byte[] encodeContent = Base64.getEncoder().encode(content.getBytes());

            final String ret = apiUtils.decodeBase64(new String(encodeContent));

            assertEquals(content , ret);
        }
    }

    
}

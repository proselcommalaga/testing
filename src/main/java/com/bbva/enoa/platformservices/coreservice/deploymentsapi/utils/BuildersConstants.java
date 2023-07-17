package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

public final class BuildersConstants
{
    private BuildersConstants()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static final class BuilderErrors
    {
        private BuilderErrors()
        {
            throw new AssertionError("Suppress default constructor for noninstantiability");
        }

        public static final String CLASS_NAME = "BuilderError";

        public static final String NO_AVAILABLE_BUILDER_IMPLEMENTATION = "BUILDER-001";

        public static final String NO_AVAILABLE_JMX_PARAMETER = "BUILDER-002";

    }
}

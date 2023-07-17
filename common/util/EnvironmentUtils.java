package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;

import java.util.Optional;

public final class EnvironmentUtils
{

    /**
     * This method will return the previous environments except the case of Environment.PRECON, Environment.PORTAL and Environment.LOCAL.
     *
     * @return a present value if you are not checking a deployment plan in a environment as Environment.PRECON, Environment.PORTAL or Environment.LOCAL
     */
    public static Optional<Environment> getPrevious(Environment environment)
    {
        Optional<Environment> previous;

        switch (environment)
        {
            case INT:
            case LAB_INT:
            case STAGING_INT:
                previous = Optional.of(Environment.LOCAL);
                break;
            case PRE:
                previous = Optional.of(Environment.INT);
                break;
            case PRO:
                previous = Optional.of(Environment.PRE);
                break;
            case LAB_PRE:
                previous = Optional.of(Environment.LAB_INT);
                break;
            case LAB_PRO:
                previous = Optional.of(Environment.LAB_PRE);
                break;
            case STAGING_PRE:
                previous = Optional.of(Environment.STAGING_INT);
                break;
            case STAGING_PRO:
                previous = Optional.of(Environment.STAGING_PRE);
                break;
            default:
                previous = Optional.empty();
                break;
        }

        return previous;
    }
}

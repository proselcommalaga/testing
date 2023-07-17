package com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.BuildersConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public final class BuildersError
{
    private BuildersError()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static NovaError getNoAvailableBuilderImplementation()
    {
        return new NovaError(BuildersConstants.BuilderErrors.CLASS_NAME, BuildersConstants.BuilderErrors.NO_AVAILABLE_BUILDER_IMPLEMENTATION,
                "Unexpected internal error.",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    public static NovaError getNoAvailableJmxParameter()
    {
        return new NovaError(BuildersConstants.BuilderErrors.CLASS_NAME, BuildersConstants.BuilderErrors.NO_AVAILABLE_JMX_PARAMETER,
                " Error trying to get JmxParameter from Database",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

}

package com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception;

import java.util.ArrayList;
import java.util.List;

public class DefinitionFileException extends Exception
{
    private final List<String> errorList;

    public DefinitionFileException(final List<String> errorList)
    {
        this.errorList = new ArrayList<>(errorList);
    }

    public List<String> getErrorList()
    {
        return new ArrayList<>(this.errorList);
    }

    public String[] getErrorArray()
    {
        return this.errorList.toArray(new String[0]);
    }

}

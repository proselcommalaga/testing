package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.model;

/**
 * Enumerate representing available action to make in the physical connector port
 */
public enum PhysicalConnectorPortAction
{
    CREATE("CREATE"),

    DELETE("DELETE");

    /**
     * The action name to do
     */
    private String actionName;

    /**
     * Builder.
     *
     * @param actionName the action to implement
     */
    private PhysicalConnectorPortAction(final String actionName)
    {
        this.actionName = actionName;
    }

    /**
     * This method return the current action name
     *
     * @return current action name
     */
    public String getActionName()
    {
        return this.actionName;
    }
}

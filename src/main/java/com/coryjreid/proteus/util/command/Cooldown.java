package com.coryjreid.proteus.util.command;

/**
 * @author Cory J. Reid
 * @version 1.0, 30 May 2017
 * @since 1.0
 */
public enum Cooldown
{
    NONE("None"), GLOBAL("Global"), USER("User");

    private String name;

    private Cooldown(String n)
    {
        name = n;
    }

    @Override
    public String toString()
    {
        return name;
    }
}

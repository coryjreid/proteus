package com.coryjreid.proteus.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton class for global bot properties. Initializes the bot with default values,
 * but also reads in user-defined custom values from config file.
 * Access the instance via BotProperties.getInstance().
 *
 * @author      Cory J. Reid
 * @version     1.0, 20 May 2017
 * @since       1.0
 */
public final class BotProperties
{
    private static final BotProperties INSTANCE = new BotProperties();
    private final Properties PROPS;

    /**
     * Constructor. Defines setting defaults and stores user-defined configurations, if any.
     */
    private BotProperties()
    {
        // build default PROPS
        final Properties DEFAULTS = new Properties();
        DEFAULTS.setProperty("prefix", "!");
        DEFAULTS.setProperty("replyUnknownError", "false");
        DEFAULTS.setProperty("replyArgumentError", "true");
        DEFAULTS.setProperty("replyPermissionError", "false");
        DEFAULTS.setProperty("replyThrottleError", "true");
        DEFAULTS.setProperty("rolesOnJoin", "null");
        PROPS = new Properties(DEFAULTS);

        // load user-configured PROPS
        try
        {
            FileInputStream config = new FileInputStream("config");
            PROPS.load(config);
            config.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Getter. Command prefix the bot will scan for.
     *
     * @return the prefix
     */
    public String getPrefix()
    {
        return PROPS.getProperty("prefix");
    }

    /**
     * Getter. Whether or not the bot should reply to requests for unknown commands.
     *
     * @return replyUnknownError
     */
    public boolean getReplyUnknownError()
    {
        return parseBoolean("replyUnknownError");
    }

    /**
     * Getter. Whether or not the bot should reply to invalid argument usage.
     *
     * @return replyArgumentError
     */
    public boolean getReplyArgumentError()
    {
        return parseBoolean("replyArgumentError");
    }

    /**
     * Getter. Whether or not the bot should reply to requests for commands users have insufficient permissions to use.
     *
     * @return replyPermissionError
     */
    public boolean getReplyPermissionError()
    {
        return parseBoolean("replyPermissionError");
    }

    /**
     * Getter. Whether or not the bot should reply to requests for commands still on cool down (throttled).
     *
     * @return replyThrottleError
     */
    public boolean getReplyThrottleError()
    {
        return parseBoolean("replyThrottleError");
    }

    /**
     * Getter. Array of Role IDs to assign to new members on join or null if none.
     *
     * @return the array of role IDs or null
     */
    public String[] getRolesOnJoin() {
        String ids = PROPS.getProperty("rolesOnJoin");
        return (ids.equals("null") ? null : ids.split(","));
    }

    /**
     * @return the singleton instance
     */
    public static BotProperties getInstance()
    {
        return INSTANCE;
    }

    /**
     * Helper. Parse setting values as booleans.
     *
     * @param s the setting to get and parse
     * @return  the result of parsing the setting
     */
    private boolean parseBoolean(String s)
    {
        return Boolean.parseBoolean(PROPS.getProperty(s));
    }
}

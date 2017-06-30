package com.coryjreid.proteus.util.command;

import net.dv8tion.jda.core.Permission;

/**
 * Intermediary class which provides access to command properties declared in the CommandInfo annotation.
 * <p>
 * This class merely provides an interface between the CommandInfo annotations and the rest of the application.
 * Its sole purpose is to make it easier to quickly access information pertaining to commands.
 * One of these objects should be available in every command.
 *
 * @author      Cory J. Reid
 * @version     1.0, 19 May 2017
 * @since       1.0
 */
public class CommandProperties
{
    private CommandInfo info;

    public CommandProperties(Command c) { info = c.getClass().getAnnotation(CommandInfo.class); }

    public String getName() { return info.name(); }

    public String[] getAliases() { return info.aliases(); }

    public String getGroup() { return info.group(); }

    public boolean getListed() { return Boolean.parseBoolean(info.listed()); }


    public Cooldown getCooldownScope() { return info.cdscope(); }

    public int getCooldownTime() { return info.cdtime(); }

    public String getDescription() { return info.description(); }

    public String getDetails() { return info.details(); }

    public String[] getExamples() { return info.examples(); }

    public Permission getPermission() { return info.permission(); }
}

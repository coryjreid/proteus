package com.coryjreid.proteus.util.command;

import net.dv8tion.jda.core.Permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Definition of the @CommandInfo annotation and must annotate every command the bot is to use.
 * <p>
 * CommandInfo annotates every command and specifies information pertaining to the command.
 * This information is utilized in the processing of the commands, namely determining
 * execution settings such as name, aliases, etc.
 * Command defaults are specified here with name being the only required definition.
 *
 * @author      Cory J. Reid
 * @version     1.0, 18 May 2017
 * @since       1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo
{
    String name();

    String[] aliases() default {};

    String group() default "general";

    String listed() default "true";

    String description() default "";

    String details() default "";

    String[] examples() default {};

    Cooldown cdscope() default Cooldown.NONE;

    int cdtime() default 0;

    Permission permission() default Permission.UNKNOWN;
}

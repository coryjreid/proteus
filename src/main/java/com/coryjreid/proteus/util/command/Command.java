package com.coryjreid.proteus.util.command;

import com.martiansoftware.jsap.JSAP;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Base for all commands.
 * <p>
 * Any command which implements this interface must handle its "action" in its run method.
 * The run method is expected to command responses to the user on success if any is warranted.
 * The parseArgs default method will respond on execution error (invalid arguments) by default.
 * This can be adjusted in config.properties if desired.
 * It is anticipated that some commands may need to command errors regardless of this setting; in this case, simply
 * override the parseArgs method.
 *
 * @author Cory J. Reid
 * @version 1.0, 26 May 2017
 * @since 1.0
 */

@CommandInfo(
        name = "",
        aliases = {""},
        group = "general",
        listed = "true",
        description = "",
        details = "",
        examples = {""},
        cdscope = Cooldown.NONE,
        cdtime = 0,
        permission = Permission.UNKNOWN
)
public interface Command
{
    /**
     * @return an instance of CommandProperties, REQUIRED
     */
    CommandProperties getProperties();

    /**
     * @return an instance of JSAP, REQUIRED
     */
    JSAP getArgumentParser();

    /**
     * Method called from the {@link}CommandHandler which parses arguments passed to command and replies if necessary.
     * <p>
     * A default implementation is provided to ensure all commands command errors if desired.
     * It is permissible to override this default behavior for custom responses.
     *
     * @param e    which event triggered this command
     * @param args passed to the command from the message which triggered the command
     */
    void parseArgs(MessageReceivedEvent e, String args);
}

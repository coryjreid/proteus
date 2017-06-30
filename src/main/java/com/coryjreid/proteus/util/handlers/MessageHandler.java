package com.coryjreid.proteus.util.handlers;

import com.coryjreid.proteus.util.BotProperties;
import com.coryjreid.proteus.util.command.Command;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

/**
 * A class for parsing messages received by the bot and determining if they are commands.
 * <p>
 * Messages received by the bot are passed to this class which then determines if the message
 * is possibly a command (namely, not sent by a bot and begins with the prefix).
 * If so, it parses the message to separate the command and the arguments and passes these
 * to the {@link}CommandHandler which it stores.
 *
 * @author      Cory J. Reid
 * @version     1.0, 25 May 2017
 * @since       1.0
 */
public class MessageHandler extends ListenerAdapter
{
    private CommandHandler ch;

    /**
     * Sole constructor which registers the provided <code>cmds</code> with the {@link}CommandHandler.
     *
     * @param cmds the commands to register and handle, not null
     */
    public MessageHandler(Command...cmds) { ch = new CommandHandler(cmds); }

    /**
     * Called automatically every time a message is received and parses the message. If the message is a command
     * it is passed to the CommandHandler for execution.
     *
     * @param event the event to parse
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        final SelfUser bot = event.getJDA().getSelfUser();
        final Message message = event.getMessage();
        final String prefix = BotProperties.getInstance().getPrefix();

        String msg = message.getRawContent().trim();

        // if message from bot or is not in a text channel  or doesn't start with prefix or a bot mention, return
        if (
                event.getAuthor().isBot()
                || !event.isFromType(ChannelType.TEXT)
                || !(msg.startsWith(prefix)
                || msg.startsWith(bot.getAsMention()))
            ) { return; }

        msg = msg.replaceFirst(bot.getAsMention(), "").replaceFirst(prefix, "").trim();

        String[] msgParts = msg.split(" ", 2);
        String cmdName = msgParts[0].toLowerCase();
        String args = (msgParts.length > 1 ? msgParts[1] : "");

        ch.runCommand(cmdName, event, args);
    }
}

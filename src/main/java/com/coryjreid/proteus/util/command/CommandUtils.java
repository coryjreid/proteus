package com.coryjreid.proteus.util.command;

import com.coryjreid.proteus.util.BotProperties;
import com.coryjreid.proteus.util.ErrorMessage;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Iterator;
import java.util.Random;

/**
 * Base class for all commands which provides default methods for argument parsing.
 *
 * @author Cory J. Reid
 * @version 1.0, 02 Jun 2017
 * @since 1.0
 */
abstract public class CommandUtils implements Command
{
    protected final CommandProperties props = new CommandProperties((Command)this);
    protected final JSAP argParser = new JSAP();

    /**
     * Method which contains the actual code specific to the command.
     *
     * @param event which triggered this command, used for replying
     * @param args passed to the command from the message which triggered the command
     */
    abstract protected void run(MessageReceivedEvent event, JSAPResult args);

    @Override
    public void parseArgs(MessageReceivedEvent event, String args)
    {
        final String prefix = BotProperties.getInstance().getPrefix();
        final boolean REPLY_ARGUMENT_ERROR = BotProperties.getInstance().getReplyArgumentError();
        final JSAP argParser = getArgumentParser();
        final JSAPResult parsedArgs = argParser.parse(args);

        if (!parsedArgs.success())
        {
            if (REPLY_ARGUMENT_ERROR) {
                Iterator errors = parsedArgs.getErrorMessageIterator();
                StringBuilder errorString = new StringBuilder();

                while (errors.hasNext()) errorString.append(errors.next()).append("\n");

                ErrorMessage.generate(event, this, true, errorString.toString());
            }
        }
        else run(event, parsedArgs);
    }

    @Override
    public CommandProperties getProperties()
    {
        return props;
    }

    @Override
    public JSAP getArgumentParser()
    {
        return argParser;
    }

    /**
     * Get an EmbedBuilder with default color and footer
     *
     * @param event the event which generated this response
     * @return      the EmbedBuilder with default footer and color options
     */
    protected EmbedBuilder getDefaultEmbed(MessageReceivedEvent event, String title)
    {
        final String cmdTitle = (title != null ? "Proteus | " + title : "Proteus");
        final User bot = event.getJDA().getSelfUser();
        final User author = event.getAuthor();
        final String username = author.getName() + "#" + author.getDiscriminator();

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(getRandomColor())
                .setAuthor(cmdTitle, "http://proteusbot.tk/", bot.getEffectiveAvatarUrl())
                .setFooter("Requested by " + username, author.getAvatarUrl());

        return builder;
    }

    /**
     * Generates a random color for message embeds.
     *
     * @return a color to be used in a MessageEmbed
     */
    protected Color getRandomColor()
    {
        Random rand = new Random();
        float r = (float) (rand.nextFloat() / 2f + 0.5);
        float g = (float) (rand.nextFloat() / 2f + 0.5);
        float b = (float) (rand.nextFloat() / 2f + 0.5);

        return new Color(r, g, b);
    }
}

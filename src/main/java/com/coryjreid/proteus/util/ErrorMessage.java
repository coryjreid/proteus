package com.coryjreid.proteus.util;

import com.coryjreid.proteus.util.command.Command;
import com.coryjreid.proteus.util.command.CommandProperties;
import com.martiansoftware.jsap.JSAP;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Random;

/**
 * Generates pretty error messages via Discord embeds and sends them to whatever channel triggered the error.
 *
 * @author Cory J. Reid
 * @version 1.0, 04 Jun 2017
 * @since 1.0
 */
final public class ErrorMessage
{
    private static final String TITLE_PREFIX = "\u26A0 ";

    private ErrorMessage() {}

    public static void generate(MessageReceivedEvent e, String...msgs)
    {
        final MessageChannel channel = e.getChannel();
        final User author = e.getAuthor();
        final StringBuilder errorMessage = new StringBuilder();
        final User bot = e.getJDA().getSelfUser();

        for (String msg : msgs) errorMessage.append(msg).append("\n");


        final MessageEmbed.Field field =
                new MessageEmbed.Field("Error Message(s)", errorMessage.toString(), false);

        EmbedBuilder response = new EmbedBuilder()
                .setColor(getRandomColor())
                .setAuthor("Proteus | Error", "http://proteusbot.tk/", bot.getEffectiveAvatarUrl())
                .setFooter("Requested by " + author.getName() + "#" + author.getDiscriminator(), author.getAvatarUrl())
                .appendDescription("An error has occurred.")
                .addField(field);

        channel.sendMessage(response.build()).queue();
    }

    public static void generate(MessageReceivedEvent e, Command c, boolean u, String...msgs)
    {
        final CommandProperties props = c.getProperties();
        final JSAP argParser = c.getArgumentParser();
        final MessageChannel channel = e.getChannel();
        final User author = e.getAuthor();
        final User bot = e.getJDA().getSelfUser();
        final String prefix = BotProperties.getInstance().getPrefix();
        final StringBuilder errorMessage = new StringBuilder();

        EmbedBuilder error = new EmbedBuilder()
                .setColor(getRandomColor())
                .setAuthor("Proteus | Error", "http://proteusbot.tk/", bot.getEffectiveAvatarUrl())
                .setFooter("Requested by " + author.getName() + "#" + author.getDiscriminator(), author.getAvatarUrl())
                .appendDescription("There was an error executing " + prefix + props.getName() + ".");

        if (u) error.addField("Command Usage", prefix + props.getName() + " " + argParser.getUsage(), false);

        for (String msg : msgs) errorMessage.append(msg).append("\n");

        error.addField(new MessageEmbed.Field("Error Message(s)", errorMessage.toString(), false));

        channel.sendMessage(error.build()).queue();
    }

    /**
     * Generates a random color for message embeds.
     *
     * @return a color to be used in a MessageEmbed
     */
    private static Color getRandomColor()
    {
        Random rand = new Random();
        float r = (float) (rand.nextFloat() / 2f + 0.5);
        float g = (float) (rand.nextFloat() / 2f + 0.5);
        float b = (float) (rand.nextFloat() / 2f + 0.5);

        return new Color(r, g, b);
    }
}

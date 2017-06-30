package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.ErrorMessage;
import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Iterator;
import java.util.List;

/**
 * A command for generating simple polls (powered by reactions) in Discord. Also used for viewing results of polls.
 *
 * @author Cory J. Reid
 * @version 1.0, 03 Jun 2017
 * @since 1.0
 */
@CommandInfo(
        name = "poll",
        group = "utility",
        description = "Create polls for people to vote on with up to 11 options or view poll results.",
        details = "There are two actions available: `create` or `results`. "
                + "This command creates simple polls that people can vote on directly in Discord via reactions. "
                + "There is a limit of 11 options. Creating a poll without any options will default it to a \"yes/no\" "
                + "poll.",
        examples = {"poll create \"Will you be buying Destiny 2?\"",
                    "poll create \"Is Dauntless the best clan?\" yes no \"who's dauntless?\"",
                    "poll results 000000000000000000"}
)
public class Poll extends CommandUtils
{
    private String[] emoji = {"0\u20e3", "1\u20e3", "2\u20e3", "3\u20e3", "4\u20e3", "5\u20e3", "6\u20e3", "7\u20e3",
            "8\u20e3", "9\u20e3", "\uD83D\uDD1F"};

    public Poll()
    {
        try
        {
            argParser.registerParameter(
                    new UnflaggedOption("action", EnumeratedStringParser.getParser("create; results", false), true,
                    "The action the poll command should perform. One of: `create` or `results`.")
            );
            argParser.registerParameter(
                    new UnflaggedOption("query", JSAP.STRING_PARSER, true,
                            "The poll question/prompt or desired poll ID.")
            );
            argParser.registerParameter(
                    new UnflaggedOption("options", JSAP.STRING_PARSER, false, "The poll options to provide.")
                            .setGreedy(true)
            );
        }
        catch (JSAPException e)
        {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        final String action = args.getString("action");
        final String query = replaceMentions(event, args.getString("query"));
        final String[] options = replaceMentions(event, args.getStringArray("options"));
        final MessageChannel channel = event.getChannel();
        final Guild guild = event.getGuild();

        if (action.equals("create"))
        {
            if (options.length == 1)
            {
                ErrorMessage.generate(event, this, false, "Polls must have more than 1 option.");
                return;
            }

            if (options.length > emoji.length)
            {
                ErrorMessage.generate(event, this, false, "Polls cannot have more than " + emoji.length + " options.");
                return;
            }

            StringBuilder optionString = new StringBuilder();
            int optionCount = (options.length == 0 ? 2 : options.length);
            EmbedBuilder poll = getDefaultEmbed(event, "Poll")
                    .appendDescription("@everyone please vote by clicking on the reaction which best matches your ")
                    .appendDescription("vote.");

            if (options.length == 0)
            {
                optionString.append(emoji[0]).append("  ").append("Yes").append("\n");
                optionString.append(emoji[1]).append("  ").append("No").append("\n");
            }
            else
            {
                for (int i = 0; i < options.length; i++)
                {
                    optionString.append(emoji[i]).append("  ").append(WordUtils.capitalize(options[i])).append("\n");
                }
            }

            // add our field detailing voting options
            String q = (!query.endsWith("?") ? query + "?" : query);
            poll.addField(q, optionString.toString(), false);

            channel.sendMessage("Creating poll...").queue(sent -> {
                poll.addField("Poll Results ID", getFormattedResultId(sent.getId()), false);
                sent.editMessage(poll.build()).queue(s -> {
                    for (int i = 0; i < optionCount; i++) s.addReaction(emoji[i]).queue();
                    s.pin().queue();
                });
            });
        }

        if (action.equals("results"))
        {
            long id = 0L;
            try
            {
                id = Long.parseLong(query.replace("-", ""));
            }
            catch (NumberFormatException e)
            {
                ErrorMessage.generate(event, this, false, "ID `" + query + "` is not a valid `Poll Results ID`.");
                return;
            }

            TextChannel pollChannel = getPollChannel(guild, id);
            if (pollChannel == null)
            {
                ErrorMessage.generate(event, "Cannot locate poll `" + query + "`.");
                return;
            }

            pollChannel.getMessageById(id).queue(success -> {
                if (!isPoll(success))
                {
                    ErrorMessage.generate(event, this, false, "ID `" + query + "` is not a poll.");
                    return;
                }

                channel.sendMessage("Located poll! Tabulating results...").queue(res -> {
                    MessageEmbed.Field field = getOptionsField(success);
                    String[] values = field.getValue().split("\n");
                    EmbedBuilder results = getDefaultEmbed(event, "Poll Results")
                            .addField("Poll Query", field.getName(), false)
                            .appendDescription("The results for the poll are displayed below.");

                    List reactions = success.getReactions();
                    for (int i = 0; i < values.length; i++)
                    {
                        StringBuilder votees = new StringBuilder();
                        MessageReaction reaction = (MessageReaction) reactions.get(i);
                        Iterator users = reaction.getUsers().iterator();
                        int count = 1;
                        while (users.hasNext())
                        {
                            User u = (User) users.next();
                            if (!u.isBot()) votees.append(count++).append(". ").append(u.getAsMention()).append("\n");
                        }
                        if (votees.length() == 0) votees.append("None :cry:");
                        results.addField(values[i] + " (" + (count-1) + ")", votees.toString(), true);
                    }

                    res.editMessage(results.build()).queue();
                });
            });
        }
    }

    /**
     * Helper to determine if message contains a poll we created
     *
     * @param msg the message to check
     * @return    true if we created this poll, false otherwise
     */
    private boolean isPoll(Message msg)
    {
        List embeds = msg.getEmbeds();
        if (embeds.size() > 0)
        {
            MessageEmbed embed = msg.getEmbeds().get(0);
            if (embed.getFields().size() > 0)
            {
                MessageEmbed.Field field = embed.getFields().get(0);
                if (field.getValue().contains(emoji[0])) return true;
            }
        }
        return false;
    }

    /**
     * Search for TextChannel containing the requested poll
     *
     * @param guild the Guild in which this poll exists
     * @param id    the id of the poll
     * @return      the TextChannel containing the poll or null if not found
     */
    private TextChannel getPollChannel(Guild guild, long id)
    {
        List channels = guild.getTextChannels();
        for (Object o : channels)
        {
            final TextChannel c = (TextChannel) o;
            try
            {
                final Message m = c.getMessageById(id).complete();
                return c;
            }
            catch (Exception e)
            {
                // System.err.println(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets the field containing the options in the desired poll.
     *
     * @param msg the message containing the poll
     * @return  the field containing the list of poll options
     */
    private MessageEmbed.Field getOptionsField(Message msg)
    {
        return msg.getEmbeds().get(0).getFields().get(0);
    }

    /**
     * Helper method for formatting poll result IDs for display
     *
     * @param id the ID to be formatted
     * @return   the formatted ID to be displayed
     */
    private String getFormattedResultId(String id)
    {
        int numCharsForDash = 4;
        StringBuilder resultId = new StringBuilder(id);
        int i = resultId.length() - numCharsForDash;
        while (i > 0)
        {
            resultId.insert(i, "-");
            i = i - numCharsForDash;
        }
        return resultId.toString();
    }

    /**
     * Replaces all mentions in str with a human-readable name
     *
     * @param event the event which triggered the command
     * @param str   the string to replace the @mentions in
     * @return      the string with human-readable names
     */
    private String replaceMentions(MessageReceivedEvent event, String str)
    {
        final Guild guild = event.getGuild();
        final List mentionedUsers = event.getMessage().getMentionedUsers();
        final List mentionedChannels = event.getMessage().getMentionedChannels();
        final List mentionedRoles = event.getMessage().getMentionedRoles();

        str = str.replace("<@!", "<@");

        if (mentionedUsers.size() > 0)
        {
            for (Object o : mentionedUsers)
            {
                User u = (User) o;
                Member m = guild.getMember(u);
                str = str.replace(u.getAsMention(), m.getEffectiveName());
            }
        }

        if (mentionedChannels.size() > 0)
        {
            for (Object o : mentionedChannels)
            {
                Channel c = (Channel) o;
                TextChannel t = guild.getTextChannelById(c.getId());
                str = str.replace(t.getAsMention(), t.getName());
            }
        }

        if (mentionedRoles.size() > 0)
        {
            for (Object o : mentionedRoles)
            {
                Role r = (Role) o;
                str = str.replace(r.getAsMention(), r.getName());
            }
        }

        return str;
    }

    /**
     * Overload of replaceMentions(event, str). Calls replaceMentions(event, str) on each value of strs.
     *
     * @param event the event which triggered this command
     * @param strs  the array of strings to replace @mentions in
     * @return      the array with all @mentions replaced via human-readable names
     */
    private String[] replaceMentions(MessageReceivedEvent event, String[] strs)
    {
        for (int i = 0; i < strs.length; i++) strs[i] = replaceMentions(event, strs[i]);
        return strs;
    }
}

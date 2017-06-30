package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.BotProperties;
import com.coryjreid.proteus.util.ErrorMessage;
import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.coryjreid.proteus.util.command.Cooldown;
import com.martiansoftware.jsap.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;

/**
 * // TODO Write this
 *
 * @author Cory J. Reid
 * @version 1.0, 11 Jun 2017
 * @since 1.0
 */
@CommandInfo(
        name = "destiny",
        aliases = {"d", "stats"},
        group = "utility",
        description = "Quickly access Destiny information and player statistics.",
        details = "An implementation of the Twitch !destiny command. Console defaults to Xbox. Specify a Playstation "
                + "player by adding the `-p` flag to your command. Link your account to execute commands on yourself "
                + "without having to specify your gamertag by running `!destiny setplayer <gamertag>` for Xbox and "
                + "`!destiny setplayer <gamertag> -p` for Playstation. @mentioning users with spaces does not require "
                + "any double quotes but manually typing a gamertag with spaces __does__ require double quotes.",
        examples = {"destiny nightfall", "destiny kd drlupo- -p", "destiny trialskd @user",
                "destiny raids \"a lethal n00b\""},
        cdscope = Cooldown.USER,
        cdtime = 5000
)
public class Destiny extends CommandUtils
{
    private final String PREFIX = BotProperties.getInstance().getPrefix();
    private final HashSet<String> noGtCommands = new HashSet<>();

    public Destiny()
    {
        // register commands which do not require a gamertag
        noGtCommands.add("stats");
        noGtCommands.add("about");
        noGtCommands.add("install");
        noGtCommands.add("status");
        noGtCommands.add("contact");
        noGtCommands.add("help");
        noGtCommands.add("actions");
        noGtCommands.add("commands");
        noGtCommands.add("isup");
        noGtCommands.add("cooldown");
        noGtCommands.add("settings");
        noGtCommands.add("donate");
        noGtCommands.add("nightfall");
        noGtCommands.add("coe");
        noGtCommands.add("dailystory");
        noGtCommands.add("dailycrucible");
        noGtCommands.add("heroic");
        noGtCommands.add("weeklycrucible");
        noGtCommands.add("xur");

        try
        {
            argParser.registerParameter(new UnflaggedOption("command", JSAP.STRING_PARSER, true,
                    "The command to execute."));
            argParser.registerParameter(new UnflaggedOption("gamertag", JSAP.STRING_PARSER, false,
                    "The gamertag to search for."));
            argParser.registerParameter(new Switch("playstation", 'p', "playstation",
                    "Search for a Playstation user."));
        }
        catch (JSAPException e)
        {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        final Member author = guild.getMember(event.getAuthor());
        final String command = args.getString("command");
        final String console = (args.getBoolean("playstation") ? "ps" : "xbox");
        final String gamertag = (containsMention(event) ? getMentionedGamertag(event) : args.getString("gamertag"));
        final String authorGt = getCleanGamertag(author.getEffectiveName());

        try
        {
            String noSpaceGt = (gamertag != null ? gamertag.replace(" ", "+") : null);
            String query = (noSpaceGt == null ? command + " " + console : command + " " + noSpaceGt + " " + console);
            String url = "https://2g.be/twitch/destinyv2.php?"
                       + "user="
                       + URLEncoder.encode(getCleanGamertag(author.getEffectiveName()).replace(" ", "+"), "UTF8")
                       + "&query="
                       + URLEncoder.encode(query, "UTF8")
                       + "&bot=proteus&defaultconsole=xbox";

            channel.sendMessage("Running your query (can take up to 60 seconds) ...").queue(msg -> {
                Unirest.setTimeouts(60000, 60000);
                Unirest.get(url).asStringAsync(new Callback<String>() {
                    public void failed(UnirestException ue)
                    {
                        msg.delete().queue();
                        ErrorMessage.generate(event, ue.getMessage());
                        return;
                    }

                    public void cancelled() {
                        msg.delete().queue();
                        ErrorMessage.generate(event, "The request was cancelled. :cry:");
                        return;
                    }

                    public void completed(HttpResponse<String> response) {
                        String urlGt = authorGt.replace(" ", "+");
                        String result = response.getBody().replace(urlGt + ": ", "");

                        if (result.contains("time-out error"))
                        {
                            msg.delete().queue();
                            ErrorMessage.generate(event, "The search timed out. Please try again.");
                            return;
                        }

                        if (result.contains("can't find"))
                        {
                            msg.delete().queue();
                            ErrorMessage.generate(event, "Can not find `" + gamertag + "` in the Destiny Database.");
                            return;
                        }

                        if (result.contains("Usage: !destiny <action> <username> <xbox/ps>"))
                        {
                            msg.delete().queue();
                            ErrorMessage.generate(event, "Can execute `" + command + "` as a valid " + PREFIX
                                    + "destiny command.");
                            return;
                        }

                        if (result.contains("No gamertag given (Tip: Use '!destiny setplayer"))
                        {
                            msg.delete().queue();
                            ErrorMessage.generate(event, "No Gamertag provided. "
                                            + "Tip: use `" + PREFIX + "destiny setplayer <gamertag>` to link your Xbox "
                                            + "account or use `" + PREFIX + "destiny setplayer <gamertag> -p` to link "
                                            + "your Playstation account.");
                            return;
                        }

                        EmbedBuilder embed = getDefaultEmbed(event, "Destiny")
                                .appendDescription("Information and stat lookup tool for Destiny provided by ")
                                .appendDescription("[DestinyCommand](https://2g.be/twitch/destiny/).")
                                .addField("Query Result", result, false);

                        if (!noGtCommands.contains(command))
                        {
                            String effectiveGamertag = (gamertag == null ? authorGt : gamertag);
                            embed.addField("Command", command, true);
                            embed.addField("Gamertag", effectiveGamertag, true);
                            embed.addField("Console", console, true);
                        }

                        msg.editMessage(embed.build()).queue();
                    }
                });
            });
        }
        catch (UnsupportedEncodingException e)
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Determines if there was a user @mentioned for searching
     *
     * @param event the event which triggered the command
     * @return      true if a user is mentioned and false otherwise
     */
    private boolean containsMention(MessageReceivedEvent event)
    {
        User bot = event.getJDA().getSelfUser();
        List mentions = event.getMessage().getMentionedUsers();

        if (mentions.size() > 0)
        {
            if (mentions.contains(bot) && mentions.size() >= 2) return true;
            if (!mentions.contains(bot) && mentions.size() >= 1) return true;
        }

        return false;
    }

    /**
     * Helper to get the gamertag of the user to search for
     *
     * @param event the event which triggered the command
     * @return      the gamertag
     */
    private String getMentionedGamertag(MessageReceivedEvent event)
    {
        final Guild guild = event.getGuild();
        final User bot = event.getJDA().getSelfUser();
        final List mentions = event.getMessage().getMentionedUsers();
        final int index = (mentions.contains(bot) ? 1 : 0);
        final Member m = guild.getMember((User) mentions.get(index));

        String gamertag = m.getEffectiveName();

        return getCleanGamertag(gamertag);
    }

    /**
     * Helper to remove timezone information from nicknames. This has the effect of returning the gamertag.
     *
     * @param ugly the gamertag with timezone info to remove
     * @return     the plain gamertag without timezone info
     */
    private String getCleanGamertag(String ugly)
    {
        if (ugly.contains(" [")) ugly = ugly.substring(0, ugly.indexOf(" ["));

        return ugly.trim().toLowerCase();
    }
}

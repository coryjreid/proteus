package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.ErrorMessage;
import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

/**
 * // TODO Write this
 *
 * @author Cory J. Reid
 * @version 1.0, 09 Jun 2017
 * @since 1.0
 */
@CommandInfo(
        name = "ban",
        group = "administration",
        description = "Quickly ban members from this server.",
        details = "A quick-access command for Admins to permanently ban users. Users removed via this command will be "
                + "unable to rejoin this server unless their ban is revoked by the server owner.\n\nCannot ban when:"
                + "\n  - you lack the `Ban Members` permission"
                + "\n  - `<user>` has a higher role than you"
                + "\n  - `<user>` has the same role as the bot",
        examples = {"ban @user", "ban @user \"The reason\""},
        permission = Permission.BAN_MEMBERS
)
public class Ban extends CommandUtils
{
    public Ban()
    {
        try
        {
            argParser.registerParameter(new UnflaggedOption("user", JSAP.STRING_PARSER, true,
                    "The user to ban as an @mention."));
            argParser.registerParameter(new UnflaggedOption("reason", JSAP.STRING_PARSER, false,
                    "The reason for the ban."));
        }
        catch (JSAPException e)
        {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        final MessageChannel channel = event.getChannel();
        final Guild guild = event.getGuild();
        final GuildController controller = guild.getController();
        final List users = event.getMessage().getMentionedUsers();
        final String reason = args.getString("reason");

        if (users.size() == 0)
        {
            ErrorMessage.generate(event, "Must mention at least one user.");
            return;
        }

        if (users.size() > 1)
        {
            ErrorMessage.generate(event, "Cannot ban more than one user at a time.");
            return;
        }

        final User user = (User) event.getMessage().getMentionedUsers().get(0);

        if (!PermissionUtil.canInteract(guild.getMember(event.getAuthor()), guild.getMember(user)))
        {
            ErrorMessage.generate(event, "You possess insufficient privileges to ban " + user.getAsMention() + ".");
            return;
        }

        if (!PermissionUtil.canInteract(guild.getMember(event.getJDA().getSelfUser()), guild.getMember(user)))
        {
            ErrorMessage.generate(event, "Bot possess insufficient privileges to ban " + user.getAsMention() + ".");
            return;
        }

        try
        {
            controller.ban(user, 0, reason).queue(suc ->
            {
                EmbedBuilder reply = getDefaultEmbed(event, "Ban")
                        .appendDescription("Successfully banned ")
                        .appendDescription(user.getAsMention())
                        .appendDescription(".");

                if (reason != null) reply.addField("Reason", reason, false);

                channel.sendMessage(reply.build()).queue();
            });
        }
        catch (PermissionException | IllegalArgumentException | GuildUnavailableException e)
        {
            ErrorMessage.generate(event, "Error banning " + user.getAsMention() + ". " + e.getMessage());
        }
    }
}

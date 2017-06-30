package com.coryjreid.proteus.util.handlers;

import com.coryjreid.proteus.util.BotProperties;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Listener which adds preconfigured roles to users on join.
 *
 * @author Cory J. Reid
 * @version 1.0, 30 Jun 2017
 * @since 1.0
 */
public class GuildMemberHandler extends ListenerAdapter
{
    private BotProperties properties = BotProperties.getInstance();

    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        GuildController controller = new GuildController(guild);
        String[] roleIds = properties.getRolesOnJoin();

        if (roleIds != null)
        {
            LinkedList<Role> roles = new LinkedList<>();
            for (String id : roleIds) roles.add(guild.getRoleById(id));
            controller.addRolesToMember(member, roles).queue();
        }
    }
}

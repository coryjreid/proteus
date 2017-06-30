package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.coryjreid.proteus.util.command.Cooldown;
import com.martiansoftware.jsap.JSAPResult;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.temporal.ChronoUnit;

@CommandInfo(
        name = "ping",
        aliases = {"p"},
        group = "admin",
        listed = "false",
        description = "A simple command to test if the bot is working.",
        details = "This command is utilized to test if the bot is working."
                + " It is simple and only available to the server owner.",
        examples = {"ping"},
        cdscope = Cooldown.USER,
        cdtime = 5000,
        permission = Permission.ADMINISTRATOR
)
public class Ping extends CommandUtils
{
    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        event.getChannel().sendMessage("Pinging!").queue(pinging -> {
            pinging.editMessage(
                    "Ping: " + event.getMessage().getCreationTime().until(pinging.getCreationTime(),
                            ChronoUnit.MILLIS) + "ms").queue();
        });
    }
}

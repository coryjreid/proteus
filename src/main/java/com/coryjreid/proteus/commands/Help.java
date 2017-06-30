package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.BotProperties;
import com.coryjreid.proteus.util.ErrorMessage;
import com.coryjreid.proteus.util.command.Command;
import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandProperties;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.coryjreid.proteus.util.handlers.CommandHandler;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

@CommandInfo(
        name = "help",
        aliases = {"h"},
        group = "utility",
        description = "Displays information on an available command or which commands are available.",
        details = "This command is meant to be a quick-access method to view all possible commands."
                + " You can search for any command by providing its name (or an alias) to !help to"
                + " view detailed information.",
        examples = {"help", "help poll"}
)
public class Help extends CommandUtils
{
    private final String PREFIX = BotProperties.getInstance().getPrefix();

    public Help()
    {
        try
        {
            argParser.registerParameter(new UnflaggedOption("command", JSAP.STRING_PARSER, false,
                    "The command name/alias for which you wish to view detailed information about."));
        } catch (JSAPException e)
        {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void run(MessageReceivedEvent e, JSAPResult args)
    {
        EmbedBuilder b = getDefaultEmbed(e, "Help");

        if(args.contains("command"))
        {
            String cmdName = args.getString("command").toLowerCase().replace("!", "");
            Command c = CommandHandler.getCommand(cmdName);

            if (c != null)
            {
                CommandProperties props = c.getProperties();

                b.setTitle(":ok_hand::skin-tone-1: Help for " + PREFIX + cmdName);
                b.addField("Usage",  PREFIX + cmdName + " " + c.getArgumentParser().getUsage(), false);

                if (!props.getDescription().equals("")) b.setDescription(c.getProperties().getDescription());

                b.addField("Cooldown", (props.getCooldownScope() + ": " + props.getCooldownTime()/1000) + "s", true);

                b.addField("Group", WordUtils.capitalizeFully(props.getGroup()), true);

                String listed = (props.getListed() ? "Yes" : "No");
                b.addField("Listed", listed, true);

                String perm = (props.getPermission() == Permission.UNKNOWN ? "None" : props.getPermission().getName());
                b.addField("Permission Requirement", perm, true);

                StringBuilder aliases = new StringBuilder();
                if (props.getAliases().length > 0)
                {
                    aliases.append(PREFIX).append(props.getName()).append(" ");
                    for (String alias : props.getAliases())
                    {
                        if (!alias.equals("")) aliases.append(PREFIX).append(alias).append(" ");
                    }
                } else aliases.append("None");
                b.addField("Aliases", aliases.toString(), true);

                if (props.getExamples().length > 0)
                {
                    StringBuilder exampleText = new StringBuilder();
                    for (String example : props.getExamples())
                    {
                        exampleText.append(PREFIX).append(example).append("\n");
                    }
                    b.addField("Examples", exampleText.toString(), true);
                }

                if (!props.getDetails().equals("")) b.addField("Details", props.getDetails(), false);

                if (!c.getArgumentParser().getHelp().equals(""))
                {
                    b.addField("Argument Details", c.getArgumentParser().getHelp(), false);
                }
            }
            else
            {
                // couldn't find a command
                StringBuilder reply = new StringBuilder();
                reply.append("The command ")
                        .append(PREFIX)
                        .append(cmdName)
                        .append(" cannot be found as a registered command! Please check your spelling and try again.")
                        .append(" You can also try ")
                        .append(PREFIX)
                        .append("help to get a list of all available commands.");

                ErrorMessage.generate(e, reply.toString());
                return;
            }
        }
        else
        {
            TreeMap<String, TreeSet<String>> groups = CommandHandler.getGroups();

            b.setTitle(":ok_hand::skin-tone-1: Available Commands");
            b.appendDescription("List of all available commands and their usage.")
                    .appendDescription(" <Argument>s are required and [<argument>]s are optional.")
                    .appendDescription(" Use ")
                    .appendDescription(PREFIX)
                    .appendDescription("help [<command name | alias>] to view command specifics.");

            for (String groupName : groups.keySet())
            {
                TreeSet<String> cmdSet = groups.get(groupName);
                Iterator cmdIterator = cmdSet.iterator();
                StringBuilder cmdString = new StringBuilder();

                while (cmdIterator.hasNext())
                {
                    String cmdName = (String)cmdIterator.next();
                    Command cmd = CommandHandler.getCommand(cmdName);

                    cmdString.append(PREFIX)
                            .append(cmdName)
                            .append(" ")
                            .append(cmd.getArgumentParser().getUsage())
                            .append("\n");

                    if (cmdSet.last().equals(cmdName)) cmdString.append("\n");
                }

                b.addField(WordUtils.capitalizeFully(groupName), cmdString.toString(), false);
            }
        }

        e.getChannel().sendMessage(b.build()).queue();
    }

    private MessageEmbed singleCommandEmbed(EmbedBuilder b, JSAPResult args)
    {
        String cmdName = args.getString("command").toLowerCase().replace("!", "");
        Command c = CommandHandler.getCommand(cmdName);

        if (c != null)
        {
            CommandProperties props = c.getProperties();

            b.setTitle(":ok_hand::skin-tone-1: Help for " + PREFIX + cmdName);
            b.addField("Usage",  PREFIX + cmdName + " " + c.getArgumentParser().getUsage(), false);

            if (!props.getDescription().equals("")) b.setDescription(c.getProperties().getDescription());

            b.addField("Cooldown", (props.getCooldownScope() + ": " + props.getCooldownTime()/1000) + "s", true);

            b.addField("Group", WordUtils.capitalizeFully(props.getGroup()), true);

            String listed = (props.getListed() ? "Yes" : "No");
            b.addField("Listed", listed, true);

            String perm = (props.getPermission() == Permission.UNKNOWN ? "None" : props.getPermission().getName());
            b.addField("Permission Requirement", perm, true);

            StringBuilder aliases = new StringBuilder();
            aliases.append(PREFIX).append(props.getName()).append(" ");
            if (props.getAliases().length > 0)
            {
                for (String alias : props.getAliases())
                {
                    if (!alias.equals("")) aliases.append(PREFIX).append(alias).append(" ");
                }
            }
            b.addField("Aliases", aliases.toString(), true);

            if (props.getExamples().length > 0)
            {
                StringBuilder exampleText = new StringBuilder();
                for (String example : props.getExamples())
                {
                    exampleText.append(PREFIX).append(example).append("\n");
                }
                b.addField("Examples", exampleText.toString(), true);
            }

            if (!props.getDetails().equals("")) b.addField("Details", props.getDetails(), false);

            if (!c.getArgumentParser().getHelp().equals(""))
            {
                b.addField("Argument Details", c.getArgumentParser().getHelp(), false);
            }
        }
        else
        {
            // couldn't find a command
            b.setTitle(":warning: Help for " + PREFIX + cmdName);
            StringBuilder reply = new StringBuilder();
            reply.append("The command ")
                    .append(PREFIX)
                    .append(cmdName)
                    .append(" cannot be found as a registered command! Please check your spelling and try again.")
                    .append(" You can also try ")
                    .append(PREFIX)
                    .append("help to get a list of all available commands.");

            b.setDescription(reply);
        }
        return b.build();
    }

    private MessageEmbed allCommandsEmbed(EmbedBuilder b)
    {
        TreeMap<String, TreeSet<String>> groups = CommandHandler.getGroups();

        b.setTitle(":ok_hand::skin-tone-1: Available Commands");
        b.appendDescription("List of all available commands and their usage.")
                .appendDescription(" <Argument>s are required and [<argument>]s are optional.")
                .appendDescription(" Use ")
                .appendDescription(PREFIX)
                .appendDescription("help [<command name | alias>] to view command specifics.");

        for (String groupName : groups.keySet())
        {
            TreeSet<String> cmdSet = groups.get(groupName);
            Iterator cmdIterator = cmdSet.iterator();
            StringBuilder cmdString = new StringBuilder();

            while (cmdIterator.hasNext())
            {
                String cmdName = (String)cmdIterator.next();
                Command cmd = CommandHandler.getCommand(cmdName);

                cmdString.append(PREFIX)
                        .append(cmdName)
                        .append(" ")
                        .append(cmd.getArgumentParser().getUsage())
                        .append("\n");

                if (cmdSet.last().equals(cmdName)) cmdString.append("\n");
            }

            b.addField(WordUtils.capitalizeFully(groupName), cmdString.toString(), false);
        }
        return b.build();
    }
}

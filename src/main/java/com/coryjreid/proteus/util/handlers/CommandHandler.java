package com.coryjreid.proteus.util.handlers;

import com.coryjreid.proteus.util.BotProperties;
import com.coryjreid.proteus.util.ErrorMessage;
import com.coryjreid.proteus.util.command.Command;
import com.coryjreid.proteus.util.command.CommandProperties;
import com.coryjreid.proteus.util.command.Cooldown;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for the actual maintaining and running of commands.
 * <p>
 * There should only be one CommandHandler object in the entire application and it lives in the MessageHandler.
 * Our actual repository of commands exists here.
 * Commands are executed via this class, and this class handles the actual execution and error handling of commands.
 *
 * @author Cory J. Reid
 * @version 1.0, 25 May 2017
 * @since 1.0
 */
public class CommandHandler
{
    private static final boolean REPLY_UNKNOWN_COMMAND  = BotProperties.getInstance().getReplyUnknownError();
    private static final boolean REPLY_PERMISSION_ERROR = BotProperties.getInstance().getReplyPermissionError();
    private static final boolean REPLY_THROTTLE_ERROR   = BotProperties.getInstance().getReplyThrottleError();
    private static final String  PREFIX                 = BotProperties.getInstance().getPrefix();
    private static final String  CD_TABLE_PREFIX        = "cmd_cooldown_";

    private static HashMap<String, String>              keys      = new HashMap<>();
    private static HashMap<String, Command>             commands  = new HashMap<>();
    private static TreeMap<String, TreeSet<String>>     groups    = new TreeMap<>();
    private static Connection                           db        = null;

    /**
     * Constructor receives the commands via arguments and stores them.
     *
     * @param cmds the commands which are registered and available for use
     */
    public CommandHandler(Command...cmds)
    {
        // connect to our database
        try
        {
            db = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
        } catch (SQLException e) { e.printStackTrace(); }

        // build our command repository
        for(Command c : cmds)
        {
            CommandProperties props = c.getProperties();

            // save our command names/aliases
            keys.put(props.getName(), props.getName());
            for(String a : props.getAliases()) { keys.put(a, props.getName()); }

            // save our command group
            if (props.getListed())
            {
                if (groups.containsKey(props.getGroup()))
                {
                    // we already have that group, add it to the existing values
                    TreeSet<String> group = groups.get(props.getGroup());
                    group.add(props.getName());
                }
                else
                {
                    TreeSet<String> temp = new TreeSet<>();
                    temp.add(props.getName());
                    groups.put(props.getGroup(), temp);
                }
            }

            // setup cooldown database tables
            if (props.getCooldownScope() != Cooldown.NONE)
            {
                try
                {
                    Statement stmt = db.createStatement();
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + CD_TABLE_PREFIX + props.getName() + " (" +
                                "id bigint PRIMARY KEY NOT NULL," +
                                "last_run bigint NOT NULL," +
                                "replied boolean NOT NULL" +
                            ");");
                    stmt.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }

            // save our command
            commands.put(props.getName(), c);
        }
    }

    /**
     * Executes the cmdName's parseArgs method assuming cmdName is a valid command and is not inhibited.
     *
     * @param cmdName the name of the command (without prefix) to attempt running
     * @param e       the event which yielded the cmdName
     * @param args    the arguments, if any, passed to cmdName
     */
    public void runCommand(String cmdName, MessageReceivedEvent e, String args)
    {
        Command cmd = getCommand(cmdName);

        if (cmd == null)
        {
            if (REPLY_UNKNOWN_COMMAND)
            {
                ErrorMessage.generate(e, "Unknown Command Error", PREFIX + cmdName + " is an unknown command.");
            }
            return;
        }

        CommandProperties cmdInfo = cmd.getProperties();

        // if we have permission and are not throttled, parseArgs and update our lastRun timestamp
        if (!permissionInhibit(cmdInfo, e) && !cooldownInhibit(cmdInfo, e))
        {
            cmd.parseArgs(e, args);
            updateLastRun(cmdInfo, e.getAuthor(), e.getGuild());
        }
    }

    public static Command getCommand(String cmd) { return commands.get(keys.get(cmd)); }

    public static TreeMap<String, TreeSet<String>> getGroups() { return groups; }

    /**
     * Inhibitor. Determines if the user has permission to parseArgs the command.
     * Replies if inhibited and REPLY_PERMISSION_ERROR is true.
     *
     * @param info the command's properties used for checking the permission
     * @param e    the event which yielded the request
     * @return     true if cannot parseArgs, false otherwise
     */
    private boolean permissionInhibit(CommandProperties info, MessageReceivedEvent e)
    {
        if (info.getPermission() != Permission.UNKNOWN)
        {
            if (!PermissionUtil.checkPermission(e.getMember(), info.getPermission()))
            {
                if (REPLY_PERMISSION_ERROR)
                {
                    ErrorMessage.generate(e, getCommand(info.getName()), false,
                            "Insufficient privileges to execute " + PREFIX + info.getName() + ".");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Inhibitor. Determines if the command is on cooldown.
     * Replies if inhibited and REPLY_THROTTLE_ERROR is true.
     *
     * @param info the command's properties used for checking the throttle
     * @param e    the event which yielded the request
     * @return     true if cannot parseArgs, false otherwise
     */
    private boolean cooldownInhibit(CommandProperties info, MessageReceivedEvent e)
    {
        if (info.getCooldownScope() != Cooldown.NONE)
        {
            long id = getCooldownId(info.getCooldownScope(), e.getAuthor(), e.getGuild());
            String table = CD_TABLE_PREFIX + info.getName();

            try (Statement stmt = db.createStatement())
            {
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE id=" + id + ";");
                while (rs.next())
                {
                    if (System.currentTimeMillis()-rs.getLong("last_run") < info.getCooldownTime())
                    {
                        if (REPLY_THROTTLE_ERROR && !rs.getBoolean("replied"))
                        {
                            int secs = info.getCooldownTime()/1000;
                            String secString = (secs == 1 ? "second" : "seconds");
                            String response = "Cooldown of " + secs + " " + secString
                                            + " required before running " + PREFIX + info.getName() + " again.";

                            ErrorMessage.generate(e, getCommand(info.getName()), false, response);

                            updateReplied(info, e.getAuthor(), e.getGuild());
                        }
                        return true;
                    }
                }
                stmt.close();
            } catch (SQLException e1) { e1.printStackTrace(); }
        }
        return false;
    }

    /**
     * Update the database if the command is on cooldown.
     *
     * @param info the properties for the command this cooldown is for
     * @param u the User to use if cdscope is Cooldown.USER
     * @param g the Guild to use if cdscope is Cooldown.GLOBAL
     */
    private void updateLastRun(CommandProperties info, User u, Guild g)
    {
        if (info.getCooldownScope() != Cooldown.NONE)
        {
            long id = getCooldownId(info.getCooldownScope(), u, g);
            String table = CD_TABLE_PREFIX + info.getName();

            try (Statement stmt = db.createStatement())
            {
                boolean rs = stmt.execute("INSERT INTO " + table + " (id, last_run, replied)"
                        + " VALUES (" + id + ", " + System.currentTimeMillis() + ", false)"
                        + " ON CONFLICT (id) DO UPDATE SET last_run = excluded.last_run, replied = excluded.replied;");
                stmt.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Update the database if we've replied to the user that they are on cooldown.
     *
     * @param info the properties for the command this cooldown is for
     * @param u the User to use if cdscope is Cooldown.USER
     * @param g the Guild to use if cdscope is Cooldown.GLOBAL
     */
    private void updateReplied(CommandProperties info, User u, Guild g)
    {
        long id = getCooldownId(info.getCooldownScope(), u, g);
        String table = CD_TABLE_PREFIX + info.getName();

        try (Statement stmt = db.createStatement())
        {
            boolean rs = stmt.execute("UPDATE " + table + " SET replied = true WHERE id = " + id + ";");
            stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Get the id that is to be used to store the cooldown information.
     *
     * @param c the command's cdscope
     * @param u the User to use if c is Cooldown.USER
     * @param g the Guild to use if c is Cooldown.GlOBAL
     * @return the id that is stored in the database
     */
    private long getCooldownId(Cooldown c, User u, Guild g)
    {
        long id = 0L;
        switch (c)
        {
            case USER:
                id = u.getIdLong();
                break;
            case GLOBAL:
                id = g.getIdLong();
                break;
        }
        return id;
    }
}

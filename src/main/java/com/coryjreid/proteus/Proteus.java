package com.coryjreid.proteus;

import com.coryjreid.proteus.commands.*;
import com.coryjreid.proteus.util.handlers.GuildMemberHandler;
import com.coryjreid.proteus.util.handlers.MessageHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

/**
 * Main class for the bot.
 *
 * Created by Cory J. Reid on 5/15/2017.
 */
public class Proteus
{
    private static JDA jda;

    /**
     * Main thread for the bot. Builds the JDA and logs in.
     * Commands are registered here with the MessageHandler().
     *
     * @param args command line arguments - not used
     */
    public static void main(String[] args)
    {
        try
        {
            // build our JDA instance
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(System.getenv("DISCORD_TOKEN"))
                    .addEventListener(new GuildMemberHandler())
                    .addEventListener(new MessageHandler(
                            new Ping(),
                            new Help(),
                            new Tacos(),
                            new Poll(),
                            new Kitty(),
                            new Ban(),
                            new Destiny()
                    ))
                    .buildBlocking();

        }
        catch (InterruptedException | RateLimitedException | LoginException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

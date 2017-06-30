package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.coryjreid.proteus.util.command.Cooldown;
import com.martiansoftware.jsap.JSAPResult;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * // TODO Write this
 *
 * @author Cory J. Reid
 * @version 1.0, 06 Jun 2017
 * @since 1.0
 */
@CommandInfo(
        name = "kitty",
        aliases = {"cat"},
        group = "fun",
        description = "Get cute kitty pics!",
        details = "Scours the web via http://thecatapi.com/ for cute kitty pictures and attempts to give you one.",
        examples = {"kitty", "cat"},
        cdscope = Cooldown.USER,
        cdtime = 10000,
        permission = Permission.UNKNOWN
)
public class Kitty extends CommandUtils
{
    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        EmbedBuilder kitty = getDefaultEmbed(event, "Kitty");

        channel.sendMessage("Finding you a kitty...").queue(msg -> {
            String[] urls = getUrls();
            while (!isOk(urls[0])) urls = getUrls();
            kitty.setImage(urls[0]).appendDescription(urls[1]);
            msg.editMessage(kitty.build()).queue();
        });
    }

    private boolean isOk(String url)
    {
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            con.disconnect();
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private String[] getUrls()
    {
        String[] urls = new String[2]; // 0 = url, 1 = source_url
        try
        {
            String xml = Unirest.get("http://thecatapi.com/api/images/get")
                    .queryString("api_key", "MTY3OTA0")
                    .queryString("format", "xml")
                    .queryString("results_per_page", "1")
                    .queryString("type", "jpg,gif,png")
                    .queryString("size", "med")
                    .asString()
                    .getBody();

            urls[0] = xml.substring(xml.indexOf("<url>")+5, xml.indexOf("</url>"));
            urls[1] = xml.substring(xml.indexOf("<source_url>")+12, xml.indexOf("</source_url>"));
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
        }

        return urls;
    }
}

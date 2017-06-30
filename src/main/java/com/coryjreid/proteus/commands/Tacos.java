package com.coryjreid.proteus.commands;

import com.coryjreid.proteus.util.command.CommandInfo;
import com.coryjreid.proteus.util.command.CommandUtils;
import com.coryjreid.proteus.util.command.Cooldown;
import com.martiansoftware.jsap.JSAPResult;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Random;

/**
 * Taco command.
 *
 * @author Cory J. Reid
 * @version 1.0, 01 Jun 2017
 * @since 1.0
 */
@CommandInfo(
        name = "tacos",
        aliases = {"taco"},
        group = "fun",
        description = "Get some :taco:s.",
        details = "I'll generate ya some tacos!",
        examples = {"tacos", "taco"},
        cdscope = Cooldown.USER,
        cdtime = 15000
)
public class Tacos extends CommandUtils
{
    @Override
    protected void run(MessageReceivedEvent event, JSAPResult args)
    {
        EmbedBuilder embed = getDefaultEmbed(event, "Tacos");
        InsultGenerator ig = new InsultGenerator();
        int maxTacos = 100;
        int randomTacos = (int) (Math.floor(Math.random()*(maxTacos +1)));
        String tacoString = (randomTacos == 1 ? "taco" : "tacos");
        StringBuilder sb = new StringBuilder();
        String words = event.getAuthor().getAsMention() + " the " + WordUtils.capitalizeFully(ig.getInsult())
                     + " stole " + randomTacos + " " + tacoString + " from the local taco truck.";

        for (int i = 0; i < randomTacos; i++) {
            String taco = "\uD83C\uDF2E";
            sb.append(taco).append(" ");
        }

        embed.appendDescription(words).addField("Here's your reward...", sb.toString(), false);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    /**
     * Generates 21,883,470 different possible insulting names.
     */
    class InsultGenerator
    {
        private final Random random = new Random();
        private final String[] adjectives1 = {"tossing", "bloody", "shitting", "wanking", "stinky", "raging", "dementing",
                "dumb", "dipping", "fucking", "instant", "dipping", "holy", "maiming", "cocking", "ranting", "twunting",
                "hairy", "spunking", "flipping", "slapping", "sodding", "blooming", "frigging", "sponglicking", "guzzling",
                "glistering", "cock wielding", "failed", "artist formally known as", "unborn", "pulsating", "naked",
                "throbbing", "lonely", "failed", "stale", "spastic", "senile", "strangely shaped", "virgin", "bottled",
                "twin-headed", "fat", "gigantic", "sticky", "prodigal", "bald", "bearded", "horse-loving", "spotty",
                "spitting", "dandy", "fritzl-admiring", "friend of a", "indeterminable", "overrated", "fingerlicking",
                "diaper-wearing", "leg-humping", "gold-digging", "mong loving", "trout-faced", "cunt rotting",
                "flip-flopping", "rotting", "inbred", "badly drawn", "undead", "annoying", "whoring", "leaking", "dripping",
                "racist", "slutty", "cross-eyed", "irrelevant", "mental", "rotating", "scurvy looking", "rambling",
                "gag sacking", "cunting", "wrinkled old", "dried out", "sodding", "funky", "silly", "unhuman", "bloated",
                "wanktastic", "bum-banging", "cockmunching", "animal-fondling", "stillborn", "scruffy-looking",
                "hard-rubbing", "rectal", "glorious", "eye-less", "constipated", "bastardized", "utter",
                "hitler's personal", "irredeemable", "complete", "enormous", "probing", "dangling", "go suck a",
                "fuckfaced", "broadfaced", "titless", "son of a", "demonizing", "pigfaced", "treacherous", "retarded",
                "twittering", "one-balled", "dickless", "long-titted", "unimaginable", "bawdy", "lumpish", "wayward",
                "assbackward", "fawning", "clouted", "spongy", "spleeny", "foolish", "idle-minded", "brain-boiled",
                "crap-headed", "jizz-draped"};
        private final String[] nouns1 = {"cock", "tit", "cunt", "wank", "piss", "crap", "shit", "arse", "sperm",
                "nipple", "anus", "colon", "shaft", "dick", "poop", "semen", "slut", "suck", "earwax", "fart",
                "scrotum", "cock-tip", "tea-bag", "jizz", "cockstorm", "bunghole", "food trough", "bum",
                "butt", "shitface", "ass", "nut", "ginger", "llama", "tramp", "fudge", "vomit", "cum", "lard",
                "puke", "sphincter", "nerf", "turd", "cocksplurt", "cockthistle", "dickwhistle", "gloryhole",
                "gaylord", "spazz", "nutsack", "fuck", "spunk", "shitshark", "shitehawk", "fuckwit",
                "dipstick", "asswad", "chesticle", "clusterfuck", "douchewaffle", "retard", "bukake"};
        private final String[] nouns2 = {"force", "bottom", "hole", "goatse", "testicle", "balls", "bucket",
                "biscuit", "stain", "boy", "flaps", "erection", "mange", "twat", "twunt", "mong", "spack", "diarrhea",
                "sod", "excrement", "faggot", "pirate", "wipe", "sock", "sack", "barrel", "head", "zombie", "alien",
                "minge", "candle", "torch", "pipe", "bint", "jockey", "udder", "pig", "dog", "cockroach",
                "worm", "MILF", "sample", "infidel", "spunk-bubble", "stack", "handle", "badger", "wagon", "bandit",
                "lord", "bogle", "bollock", "tranny", "knob", "nugget", "king", "hole", "kid", "trailer", "lorry", "whale",
                "rag", "foot", "pile", "waffle", "bait", "barnacle", "clotpole", "dingleberry", "maggot"};
        private final String[] adjectives2 = {"licker", "raper", "lover", "shiner", "blender", "fucker", "jacker",
                "butler", "packer", "rider", "wanker", "sucker", "felcher", "wiper", "experiment", "bender", "dictator",
                "basher", "piper", "slapper", "fondler", "plonker", "bastard", "handler", "herder", "fan", "amputee",
                "extractor", "professor", "graduate", "voyeur", "hogger", "collector", "detector", "sniffer"};


        private String getInsult()
        {
            StringBuilder sb = new StringBuilder();

            switch (random.nextInt(6))
            {
                case 0:
                    sb.append(adjectives1[random.nextInt(adjectives1.length)])
                            .append(" ")
                            .append(nouns1[random.nextInt(nouns1.length)])
                            .append(" ")
                            .append(nouns2[random.nextInt(nouns2.length)]);
                    break;
                case 1:
                    sb.append(adjectives1[random.nextInt(adjectives1.length)])
                            .append(" ")
                            .append(nouns1[random.nextInt(nouns1.length)])
                            .append(" ")
                            .append(adjectives2[random.nextInt(adjectives2.length)]);
                    break;
                case 2:
                    sb.append(adjectives1[random.nextInt(adjectives1.length)])
                            .append(" ")
                            .append(nouns2[random.nextInt(nouns2.length)])
                            .append(" ")
                            .append(adjectives2[random.nextInt(adjectives2.length)]);
                    break;
                case 3:
                    sb.append(nouns1[random.nextInt(nouns1.length)])
                            .append(" ")
                            .append(nouns2[random.nextInt(nouns2.length)]);
                    break;
                case 4:
                    sb.append(nouns1[random.nextInt(nouns1.length)])
                            .append(" ")
                            .append(adjectives2[random.nextInt(adjectives2.length)]);
                    break;
                case 5:
                    sb.append(nouns2[random.nextInt(nouns2.length)])
                            .append(" ")
                            .append(adjectives2[random.nextInt(adjectives2.length)]);
                    break;
                case 6:
                    // the super special
                    sb.append(adjectives1[random.nextInt(adjectives1.length)])
                            .append(" ")
                            .append(nouns1[random.nextInt(nouns1.length)])
                            .append(" ")
                            .append(nouns2[random.nextInt(nouns2.length)])
                            .append(" ")
                            .append(adjectives2[random.nextInt(adjectives2.length)]);
                    break;
            }

            return sb.toString();
        }
    }
}

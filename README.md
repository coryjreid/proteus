# Proteus - A Discord Chat Bot
Proteus is a chat bot for Discord built on Java utilizing [JDA](https://github.com/DV8FromTheWorld/JDA "JDA Repository")
. It is designed to run on Heroku via an executable JAR file. 

## Proteus Features
* Dependencies and build managed via Gradle
* Built using IntelliJ IDEA
* Simple `config` file to tweak global bot settings
  * Command prefix, default: `!`
  * Reply to unknown commands, default: `false`
  * Reply to argument errors, default: `true`
  * Reply to invalid permission errors, default: `false`
  * Reply to cooldown errors, default: `true`
  * Roles on join, default: `null`
* Messages generated via embeds for readability
* Advanced argument parsing via [Java Simple Argument Parser](http://www.martiansoftware.com/jsap/) (JSAP)
* Detailed error responses for invalid argument usage
* Command cooldowns per user or globally
* Command aliases for accessing the same command with different names (woo, shorthand!)
* Command restrictions based on presence of Discord permissions
* Commands optionally listed publicly via `!help` command
* Command groups for better organization
* Command examples in `!help` for clarity
* Automatically add roles to users on join (or don't, it's optional!)
* Bot responds to commands (with prefix) or @mentions (commands with or without prefix)

## Proteus Commands
* `!help`: Display information on all available commands or detailed information for a specific command
* `!ping`: Check if the bot is alive via a ping to the Discord API
* `!tacos`: Generate some tacos and an insult
* `!kitty`: Get a cute picture of a cat from [TheCatAPI](http://thecatapi.com)
* `!poll`: Generate polls or view poll results in Discord (vote via reactions, up-to 11 total options)
* `!ban`: Quickly ban a user from Discord
* `!destiny`: Quickly access information and player statistics for Destiny 
  (Powered by [DestinyCommand](https://2g.be/twitch/destiny/))

## Deploying to Heroku
The bot is designed to run on Heroku via an executable JAR file. A Gradle `fatJar` task has been defined to easily build
 and prepare a JAR file with all dependencies for deployment. It will be generated in 
 `./build/libs/proteus-fat-VERSION.jar`. To deploy to Heroku you need to install the `heroku-cli-deploy` plugin first. 
 
 Install this plugin locally via the following command:
 
`heroku plugins:install heroku-cli-deploy`

Now follow these steps to deploy to Heroku:
1. Create your app on Heroku:  
   `heroku create --no-remote`
2. Generate your fatJar:  
   `./gradlew fatJar`
3. Create your settings file:  
   `cp config.example config`
4. Deploy to Heroku (from project directory run):  
   `heroku deploy:jar ./build/libs/proteus-fat-VERSION.jar -i ./config --app HEROKU-APP-NAME`
     
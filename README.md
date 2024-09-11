# Atlantis
...is an extensive Java framework based on [JBWAPI 2.1.0](https://github.com/JavaBWAPI/JBWAPI). Last updated **2024/09**.

![image](https://github.com/user-attachments/assets/14be34c7-5b0f-4dd8-a9a8-132904fc5f16)

Atlantis wraps everything heavily, so you don't need to write yourself countless lines of tedious code that every bot unfortunately needs to have to do the simplest stuff. It has quite clean and re-usable code (at least it has high priority on my list). 

# What it can do
- all three races can be supported, but it mainly focuses on Terran
- enables to you to run your bot in *one* click from your IDE (IntelliJ IDEA recommend), just "Run" your project and it will auto run Chaoslauncher and Starcraft
- auto-modifies bwapi.ini so you don't need to do it yourself! Can change your race, enemy race, map played and game type from AConfig class
- includes tests and mini-maps to test on
- handy keyboard shortcuts like: speed faster/slower (+/-), exit (Esc), screen centering on combat unit (c)
- customizable build orders
- takes care of the economy for you (gathering, construction)
- assigns workers to optimal mineral fields
- scouts to find enemy base and detects the build order used
- if enemy goes air/hidden units responds by building e.g. Missile Turrets
- micro-managers specific to units
- auto-expansions
- evaluation of unit chance to win the Near skirmish and retreating when needed
- advanced and code wrappers for selecting units that use cache

# How to install
The latest stable version is in the `master` branch. In `develop` you will find latest changes, but they might not work as expected.

* IMPORTANT: In order to use BWAPI you have to create player profile in StarCraft. Just run StarCraft, click Single Player, BroodWar and create profile with any name. If you skip this step you will get an error.
* First we need to install BWAPI in version 4.4.0, so [click here to get it](https://github.com/bwapi/bwapi/releases/download/v4.4.0/BWAPI_Setup.exe). Installation of BWAPI is a hard thing for beginners, so make sure you follow [this tutorial](http://sscaitournament.com/index.php?action=tutorial) closely.
* Feel free to read the tutorial even further to see how a simple non-Atlantis bot would look like.
* Make sure to follow [these steps from Atlantis Wiki](https://github.com/Ravaelles/Atlantis/wiki)
* If you've done everything correctly, you should be good to go. Run Main class of Atlantis in IDE.

# Quick usage
- Install Atlantis and use your preferred IDE.
- See `AConfig` class and edit our race and enemy race to play against default AI
- Run the application and that's it, it will automatically run StarCraft and Chaoslauncher.
- If you got any error it probably means you've screwed the BWAPI installation.
- Use keys 1 to 9 and +/- to change game speed; 1 is normal, 9 skips much more frames making the game crazy fast, PauseBreak to stop the game.
- Esc makes the bot to exit gently, killing StarCraft and ChaosLauncher processes on exit

# AI tournaments
You probably know it already, but take a look at: http://sscaitournament.com/

Starcraft bots that plays there online 365/24/7. Pretty cool, eh?

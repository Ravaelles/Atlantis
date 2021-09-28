# Atlantis
...is an extensive Java framework based on BWMirror 2.5 and BWAPI 4.1.2 with one goal in mind:
*Make it much, much easier to create new bot starting from zero*.

![image](https://i.ibb.co/db9BXDT/Atlantis.png)

Atlantis wraps everything heavily, so you don't need to write yourself countless lines of tedious code that every bot unfortunately needs to have to do the simplest stuff. It has quite clean and re-usable code (at least it has high priority on my list). 

# What it can do
- all three races are supported
- enables to you to run your bot in *one* click from your IDE, just run it and it will run Chaoslauncher and Starcraft
- build orders are fully customizable and very easy to use, see bwapi/read/build_orders files; multiple notations are accepted including Liquipedia notation, see an example [here](https://github.com/Ravaelles/Atlantis/blob/develop/bwapi-data/AI/build_orders/Terran/1%20Fact%20FE.txt#L26); you can also switch active build order easily
- takes care of the economy for you (gathering, construction)
- assigns workers to optimal mineral fields
- scouts to find enemy base and detects the build order used
- if enemy goes air/hidden units responds by building e.g. Missile Turrets
- micro-managers that handle unit's behavior (e.g. run from nearby Zealots)
- auto-expansion when minerals exceed e.g. 450 minerals
- evaluation of unit chance to win the nearby skirmish and retreating when needed
- advanced and clean code wrappers for selecting units, types etc

And some extras:
- provides handy keyboard shortcuts like: speed faster/slower (+/-), exit (~), painting modes (1,2,3)
- modifies bwapi.ini so you don't need to do it yourself! Can change your race, enemy race, map played and game type (for UMT maps which are great for micro tweaking)

# What actually is Atlantis?
It's powerful set of tools that are based on [BWMirror](https://github.com/vjurenka/BWMirror) in version 2.5. The library (BWMirror) is heavily wrapped in numerous helper methods/modules as BWMIrror pretty much doesn't do anything by itself and most people are interested in combat, not searching for nearest free mineral field for harvesting or debugging Extractor construction. Trust me, it can take tens of hours to solve these problems. Prior to Atlantis I've written three bots, so I know how it is ;__:

# How to install
Atlantis is actively developed, but it's quite capable already. The latest stable version is in the `master` branch. In `develop` you will find latest changes, but they might not work as expected.

* IMPORTANT: In order to use BWAPI you have to create player profile in StarCraft. Just run StarCraft, click Single Player, BroodWar and create profile with any name. If you skip this step you will get an error.
* First we need to install BWAPI in version 4.1.2, so [click here to get BWAPI_412_Setup](https://github.com/bwapi/bwapi/releases/download/v4.1.2/BWAPI_412_Setup.exe). Installation of BWAPI is a hard thing for beginners, so make sure you follow [this tutorial](http://sscaitournament.com/index.php?action=tutorial) closely.
* Feel free to read the tutorial even further to see how a simple non-Atlantis bot would look like.
* If you installed BWAPI checkout this repository and make new project in either NetBeans or Eclipse (New Project -> Java project with existing sources). For more instructions see the tutorial above. If you encounter any problem with the install, again, follow the tutorial.
* If you've done everything correctly, you're free to go. The main class is of Atlantis is called Main. Most of Atlantis classes and wrappers are prefixed with "A".

# Quick usage
- Install Atlantis and open your preferred IDE.
- (optional) Go to `atlantis/AtlantisConfig` class and edit our race and enemy race as Atlantis can play all three races.
- Run the application and that's it, it will automatically run StarCraft and Chaoslauncher.
- If you got any error it probably means you've screwed the BWAPI installation.
- Use keys 1,2,3 to change painting mode level; more painting, slower it runs, but gives more information.
- Use keys +/- to change the game speed, use PauseBreak to stop the game.
- Tilde (~) makes the bot to exit gently, killing StarCraft and ChaosLauncher processes.

# Code structure

Main framework class is called `Atlantis` and it's built on top of BWMirror `BWEventListener` class. It contains all events like: new game frame, unit destroyed, unit created etc.

  * Atlantis.matchFrame()
    * AGameCommander.update()
	  * AStrategyCommander.update()
	    - responsible for detecting enemy strategy
	  * AProductionCommander.update()
	  	- takes care of build orders and issues proper commands
	  	- automatically builds supply units (Supply Depots, Pylons, Overlords) when needed 
	  	- ensures constructions are finished and have builders assigned
	  	- handles expansion (new bases)
	  * AWorkerCommander.update()
	  	- assign workers to gather resources
	  	- transfer workers between bases when needed
	  	- @ToDo: defend against rushes and enemy units (Cannon Rush, Gas steal)
	  * ACombatCommander.update()
	  	- takes control of every combat unit
	  	- decide whether to fight or retreat
		- avoid close or hidden melee units
	  	- choose best targets to attack (prioritize Siege Tanks)
	  * AScoutManager.update()
	  	- send worker to scout the map in search of enemy
	  	- roam around enemy base for as long possible in order to detect his build order	  
	  * APainter.paint()
    	- paints life bars over units
    	- displays places of construction that haven't started yet
    	- displays current production queue
    	- displays tooltips over units that make debugging easier

# AI tournaments
Take a look at this site: http://sscaitournament.com/

It's tournament for Starcraft bots that plays matches online 364/24/7. Pretty cool, eh?
There you will find all infos about participation and submission of your bot. 

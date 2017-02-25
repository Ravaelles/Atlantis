# Atlantis
...is an extensive framework based on BWMirror 2.5 to write your very own Starcraft Broodwar bot in Java and it aims to achieve one thing:
*Make it much, much easier to create new bot starting from zero*.

![alt tag](http://s15.postimg.org/mnsu7qnt7/Atlantis_Tide.png)

It is being ported to BWMirror 2.5, which runs with BWAPI 4.1.2. Atlantis wraps everything heavily, so you don't need to write yourself countless lines of tedious code that every bot unfortunately needs to have to do the simplest stuff.

# What it can do
Modular approach with cleanest code possible (that's the priority) is offered by core classes which are prefixed with "Atlantis". So what has been implemented so far is:
- all three races are supported
- enables to you to run your bot in *one* click from your IDE, just run it and it will run Chaoslauncher and Starcraft
- provides keyboard shortcuts like speed faster/slower (+/-), exit (Esc), painting modes (1,2,3)
- build orders are fully customizable, see bwapi/read/build_orders files; multiple notations are accepted including Liquipedia notaton
- takes care of the economy for you (gathering, construction)
- workers are assigned to optimal mineral fields
- scouts in order to find enemy base and detect the build order used
- micro-managers that handle unit's behavior (e.g. run from nearby Zealots)
- auto-expansion when minerals exceed e.g. 450 minerals
- evaluation of unit chance to win the nearby skirmish and retreating when needed
- advanced and clean code wrappers for selecting units, types etc

# What actually is Atlantis?
It's powerful set of tools that are based on [BWMirror](https://github.com/vjurenka/BWMirror) in version 2.5. The library (BWMirror) is heavily wrapped in numerous helper methods/modules as BWMIrror pretty much doesn't do anything by itself and most people are interested in combat, not searching for nearest free mineral field for harvesting or debugging Extractor construction. Trust me, it can take tens of hours to solve these problems. Prior to Atlantis I've written three bots, so I know how it is ;__:

# How to install
Atlantis is actively developed, but it's quite capable already. The latest stable version is in the `master` branch. In `develop` you will find latest changes, but they might not work as expected.

* IMPORTANT: In order to use BWAPI you have to create player profile in StarCraft. Just run StarCraft, click Single Player, BroodWar and create profile with any name. If you skip this step you will get an error.
* First install BWAPI in version 4.1.2 (link: https://github.com/bwapi/bwapi/releases/download/v4.1.2/BWAPI_412_Setup.exe)
* Make sure to read the readme very carefully. The installation of BWAPI is a hard thing for beginners, so make sure you understand it.
* I suggest to take a quick look at the tutorial here (http://sscaitournament.com/index.php?action=tutorial) to understand how the bridge between the game actually works and what it does.
* Checkout this repository and make a new project in either NetBeans or Eclipse, for more instructions see the tutorial above. If you encounter any problem with the install, again, follow the tutorial.
* If you've done everything correctly, you're free to go. The main class is of Atlantis is called Main. Most of Atlantis classes and wrappers are prefixed with "A".

# Quick usage
- Install Atlantis and open your preferred IDE.
- (optional) Go to `atlantis/AtlantisConfig` class and edit our race and enemy race as Atlantis can play all three races.
- Run the application and that's it, it will automatically run StarCraft and Chaoslauncher.
- If you got any error it probably means you've screwed the BWAPI installation.
- Use keys 1,2,3 to change painting mode level; more painting, slower it runs, but gives more information.
- Use keys +/- to change the game speed.

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

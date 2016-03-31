# Atlantis
...is an extensive framework based on [BWMirror 2.5](https://github.com/vjurenka/BWMirror) to write your very own Starcraft Broodwar bot in Java and it aims to achieve one thing:
*Make it much, much easier to create new bot starting from zero*.

![alt tag](http://s15.postimg.org/mnsu7qnt7/Atlantis_Tide.png)

# What it can do
Modular approach with cleanest code possible (that's our priority) is offered by core classes which are prefixed with "Atlantis". So what has been implemented so far is:
- all three races are supported
- build orders are customizable using files in bwapi/read/build_orders, files are pretty self-explanatory
- takes care of the economy for you, allowing you to focus on the military instead (just define build orders)
- workers are assigned to optimal mineral fields
- customizable auto-scouting for the enemy location
- basic micro-managers that handle unit's behavior in the fight
- auto-expansion once minerals exceed e.g. 350 minerals
- evaluating chances to win the combat and retreating if needed
- advanced and clean code wrappers for selecting units, types and so on

# What actually is Atlantis?
It's powerful set of tools that are based on [BWMirror in version 2.5](https://github.com/vjurenka/BWMirror). The library (BWMirror) is heavily wrapped in numerous helper methods / modules as BWMIrror pretty much doesn't do anything by itself and most people are interested in combat, not searching for nearest free mineral field for harvesting or debugging Extractor construction. Trust me, it can take tens of hours to solve these problems. Prior to Atlantis I've written three bots, so I know how it is ;__:

# How to install
Atlantis is actively developed, but it's quite capable already. The latest stable version is in the `master` branch. In `develop` you will find latest changes, but they might not work as expected.

* First install [BWAPI in version 4.1.2](https://github.com/bwapi/bwapi/releases/download/v4.1.2/BWAPI_412_Setup.exe)
* Make sure to read the readme absolutely carefully. The installation of BWAPI can be really hard thing.
* At least take a quick look at tutorial here (http://sscaitournament.com/index.php?action=tutorial) to understand how the BWMirror bridge between the game actually works and what it does
* Checkout this repository and make a new project in either NetBeans or Eclipse, for more instructions see the tutorial above. For all the bugfixing, again, follow the tutorial.
* If you've done everything correctly, you're free to go. The main class is of AtlantisTide is called Main. Atlantis is only a family of all products that will be built upon this framework.
* Atlantis is capable of auto-detecting the race it plays. If you want to change your race, modify this line "race = Protoss" in your bwapi.ini file.

# Code structure
To be explained and improved...
  * Atlantis.matchFrame()
    * AtlantisGameCommander.update()
	  * AtlantisWorkerCommander.update();
	  	- gathering resources
	  	- transfers workers between bases if needed
	  	- defence against rushes
	  * AtlantisCombatCommander.update();
	  	- controls all combat units
	  	- decides whether to fight or retreat
	  	- chooses best targets to attack
	  	- checks if unit is wounded and if so, retreats
	  * AtlantisScoutManager.update();
	  	- sends unit to scout the map in search of enemy
	  	- scouts the possible location for the next base
	  * AtlantisProductionCommander.update();
	  	- takes care of build orders and issues proper commands
	  	- automatically builds supply units when needed 
	  	- ensures constructions are finished and have proper builders
	  	- requests construction of new base (expands)
	  * AtlantisPainter.paint();
    		- paints life bars over units
    		- displays places of construction that haven't started yet
    		- displays current production queue
    		- displays tooltips over units that make debugging easier

# AI tournaments
Take a look at this site: http://sscaitournament.com/

It's tournament for Starcraft bots where all bots play online matches that are transmitted 364/24/7. Pretty cool, eh?
There you can will find all the information about participation and submission of your bot. 
Adun Toridas!

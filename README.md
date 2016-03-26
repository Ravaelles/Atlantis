# Atlantis
...is a framework to write your very own Starcraft Broodwar bot in Java and it aims to achieve one thing:
*Make it much, much easier to create new bot starting from zero*.

![alt tag](http://s15.postimg.org/mnsu7qnt7/Atlantis_Tide.png)

It is being ported to BWMirror 2.5, which runs with BWAPI 4.1.2. Atlanti wraps everything heavily, so you don't need to write yourself countless lines of tedious code that every bot unfortunately needs to have to even do the most basic stuff.

# What it can do
Modular approach with cleanest code possible (that's my priority) is offered by core classes which are prefixed with "Atlantis". So what has been implemented so far is:
- all three races are supported
- build orders are customizable using txt files in bwapi/read/build_orders, the file is pretty self-explanatory
- takes care of the economy for you, allowing you to focus on the military instead
- initially workers as assigned to the optimal mineral fields
- all subsequent workers also choose the best minerals
- customizable auto-scouting for the enemy location
- basic micro-managers that handle unit's behavior in the fight
- auto-expansion once minerals exceed e.g. 350 minerals

# How to install
Atlantis is still in the development, but it's quite capable already. The latest stable version is in the `master` branch. In `develop` you will find latest changes, but they doesn't necessarily work.

* First install BWAPI in version 3.7.5 (link: https://github.com/bwapi/bwapi/releases/download/v3.7.5/BWAPI.3.7.5.7z)
* Make sure to read the readme absolutely carefully. The installation of BWAPI can be really hard thing.
* At least take a quick look at tutorial here (http://sscaitournament.com/index.php?action=tutorial) to understand how the bridge between the game actually works and what it does
* Checkout this repository and make a new project in either NetBeans or Eclipse, for more instructions see the tutorial above. For all the bugfixing, again, follow the tutorial.
* If you've done everything correctly, you're free to go. The main class is of AtlantisTide is called Main. Atlantis is only a family of all products that will be built upon this framework.
* Atlantis is capable of auto-detecting the race it plays. If you want to change your race, modify this line "race = Protoss" in your bwapi.ini file.

# Code structure
To be explained and improved...
  * Atlantis.matchFrame()
    * AtlantisGameCommander.update()
	  * AtlantisWorkerCommander.update();
	  * AtlantisCombatCommander.update();
	  * AtlantisScoutManager.update();
	  * AtlantisBuildingsCommander.update();
	  * AtlantisProductionCommander.update();
	  * AtlantisPainter.paint();
    
# AI tournaments
Take a look at this site: http://sscaitournament.com/

It's tournament for Starcraft bots that plays matches online 364/24/7. Pretty cool, eh?
There you will find all infos about participation and submission of your bot. 

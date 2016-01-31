# Atlantis
...is a framework to write your very own Starcraft Broodwar bot in Java and it aims to achieve one thing:
*Make it much, much easier to create new bot starting from zero*.

It uses JNIBWAPI as a base, but wraps everything heavily, so you don't need to write yourself countless lines of tedious code that every bot unfortunately needs to have to even do the most basic stuff.

# How to use
Atlantis is still in the development, but it's quite capable already. The latest stable version is in the `master` branch. In `develop` you will find latest changes, but it doesn't necessarily work.

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

# Code structure
To be improved...
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
You will find there all info about participation and submission of your bot. 

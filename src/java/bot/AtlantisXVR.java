package bot;

import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;

public class AtlantisXVR {

	/**
	 * Sets up Atlantis config and runs the bot.
	 */
	public static void main(String[] args) {

		// Set up some very basic config
		AtlantisConfig.MY_RACE = RaceTypes.Terran;
		AtlantisConfig.BASE = UnitTypes.Terran_Command_Center;
		AtlantisConfig.WORKER = UnitTypes.Terran_SCV;

		// Starts bot using Atlantis
		Atlantis atlantis = new Atlantis();
		atlantis.start();
	}

}

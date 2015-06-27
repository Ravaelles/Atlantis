package bot;

import jnibwapi.types.UnitType.UnitTypes;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;

public class AtlantisXVR {

	/**
	 * Runs the bot.
	 */
	public static void main(String[] args) {
		AtlantisConfig atlantisConfig = new AtlantisConfig();
		AtlantisConfig.BASE = UnitTypes.Terran_Command_Center;
		AtlantisConfig.WORKER = UnitTypes.Terran_SCV;

		Atlantis atlantis = new Atlantis(atlantisConfig);
		atlantis.start();
	}

}

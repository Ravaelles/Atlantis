package bot;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;

public class AtlantisXVR {

	/**
	 * Sets up Atlantis config and runs the bot.
	 */
	public static void main(String[] args) {

		// Set up some very basic config
		AtlantisConfig.useConfigForTerran();

		// Starts bot using Atlantis
		Atlantis atlantis = new Atlantis();
		atlantis.start();
	}

}

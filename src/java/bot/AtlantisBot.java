package bot;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.production.strategies.DefaultTerranProductionStrategy;

public class AtlantisBot {

	/**
	 * Sets up Atlantis config and runs the bot.
	 */
	public static void main(String[] args) {

		// Set up some very basic config
		AtlantisConfig.useConfigForTerran();
		AtlantisConfig.useProductionStrategy(new DefaultTerranProductionStrategy());

		// Starts bot using Atlantis
		Atlantis atlantis = new Atlantis();
		atlantis.start();
	}

}

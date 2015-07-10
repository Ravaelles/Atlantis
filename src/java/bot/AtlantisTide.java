package bot;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.production.strategies.DefaultTerranProductionStrategy;

/**
 * This is the main class of the bot. Here everything starts.
 * 
 * "A journey of a thousand miles begins with a single step." - Lao Tse
 */
public class AtlantisTide {

	/**
	 * Sets up Atlantis config and runs the bot.
	 */
	public static void main(String[] args) {

		// Set up base configuration based on race used.
		AtlantisConfig.useConfigForTerran();

		// Adjust various parameters according to your needs.
		AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 15;

		// Set production strategy (build orders) to use. It can be always changed dynamically.
		AtlantisConfig.useProductionStrategy(new DefaultTerranProductionStrategy());

		// Create Atlantis object to use for this bot. It wraps JNIBWAPI functionality.
		Atlantis atlantis = new Atlantis();

		// Starts bot.
		atlantis.start();
	}

}

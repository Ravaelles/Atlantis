package bot;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.production.strategies.AtlantisProductionStrategy;
import jnibwapi.types.RaceType;
import jnibwapi.types.RaceType.RaceTypes;

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
//        RaceType racePlayed = RaceTypes.Protoss;
//        RaceType racePlayed = RaceTypes.Terran;
        RaceType racePlayed = RaceTypes.Zerg;

        // Set up base configuration based on race used.
        if (racePlayed.equals(RaceType.RaceTypes.Protoss)) {
            AtlantisConfig.useConfigForProtoss();
        } else if (racePlayed.equals(RaceType.RaceTypes.Terran)) {
            AtlantisConfig.useConfigForTerran();
        } else if (racePlayed.equals(RaceType.RaceTypes.Zerg)) {
            AtlantisConfig.useConfigForZerg();
        }

        // --------------------------------------------------------------------
        // Adjust various parameters according to your needs.
        AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 5;

        // Set production strategy (build orders) to use. It can be always changed dynamically.
        AtlantisConfig.useProductionStrategy(AtlantisProductionStrategy.getAccordingToRace());

        // Create Atlantis object to use for this bot. It wraps JNIBWAPI functionality.
        Atlantis atlantis = new Atlantis();

        // Starts bot.
        atlantis.start();
    }

}

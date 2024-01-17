package atlantis.game.race;

import atlantis.config.AtlantisRaceConfig;
import bwapi.Race;
import main.Main;

public class MyRace {
    /**
     * Returns true if user plays as Terran.
     */
    public static boolean isPlayingAsTerran() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Terran".equals(Main.OUR_RACE);

        return AtlantisRaceConfig.MY_RACE.equals(Race.Terran);
    }

    /**
     * Returns true if user plays as Protoss.
     */
    public static boolean isPlayingAsProtoss() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Protoss".equals(Main.OUR_RACE);

        return AtlantisRaceConfig.MY_RACE.equals(Race.Protoss);
    }

    /**
     * Returns true if user plays as Zerg.
     */
    public static boolean isPlayingAsZerg() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Zerg".equals(Main.OUR_RACE);

        return AtlantisRaceConfig.MY_RACE.equals(Race.Zerg);
    }
}

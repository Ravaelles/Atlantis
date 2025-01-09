package atlantis.util;

import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;
import bwapi.Race;
import main.Main;

public class We {
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static Race race() {
        return (Race) cache.get(
            "race",
            -1,
            () -> {
                if (We.protoss()) {
                    return Race.Protoss;
                }
                else if (We.terran()) {
                    return Race.Terran;
                }
                else if (We.zerg()) {
                    return Race.Zerg;
                }

//                System.err.println("Unable to identify race");

                if (Env.isTesting()) return Race.Terran;
                else {
                    ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Unable to identify race");
                    return null;
                }
            }
        );
    }

    public static boolean terran() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Terran".equals(Main.ourRace());

        return AtlantisRaceConfig.MY_RACE.equals(Race.Terran);
    }

    public static boolean protoss() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Protoss".equals(Main.ourRace());

        return AtlantisRaceConfig.MY_RACE.equals(Race.Protoss);
    }

    public static boolean zerg() {
        if (AtlantisRaceConfig.MY_RACE == null) return "Zerg".equals(Main.ourRace());

        return AtlantisRaceConfig.MY_RACE.equals(Race.Zerg);
    }

    public static boolean haveBase() {
        return Count.bases() >= 1;
    }
}

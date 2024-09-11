package atlantis.util;

import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.game.race.MyRace;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;
import bwapi.Race;

public class We {
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static Race race() {
        return (Race) cache.get(
            "race",
            -1,
            () -> {
                if (MyRace.isPlayingAsProtoss()) {
                    return Race.Protoss;
                }
                else if (MyRace.isPlayingAsTerran()) {
                    return Race.Terran;
                }
                else if (MyRace.isPlayingAsZerg()) {
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
        return MyRace.isPlayingAsTerran() || Race.Terran.equals(AtlantisRaceConfig.MY_RACE);
    }

    public static boolean protoss() {
        return MyRace.isPlayingAsProtoss();
    }

    public static boolean zerg() {
        return MyRace.isPlayingAsZerg();
    }

    public static boolean haveBase() {
        return Count.bases() >= 1;
    }
}

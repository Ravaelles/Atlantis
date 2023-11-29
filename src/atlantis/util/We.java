package atlantis.util;

import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.game.AGame;
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
                if (AGame.isPlayingAsProtoss()) {
                    return Race.Protoss;
                }
                else if (AGame.isPlayingAsTerran()) {
                    return Race.Terran;
                }
                else if (AGame.isPlayingAsZerg()) {
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
        return AGame.isPlayingAsTerran() || Race.Terran.equals(AtlantisRaceConfig.MY_RACE);
    }

    public static boolean protoss() {
        return AGame.isPlayingAsProtoss();
    }

    public static boolean zerg() {
        return AGame.isPlayingAsZerg();
    }

    public static boolean haveBase() {
        return Count.bases() >= 1;
    }
}

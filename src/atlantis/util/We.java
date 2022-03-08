package atlantis.util;

import atlantis.game.AGame;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;
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
                    else {
                        return Race.Zerg;
                    }
                }
        );
    }

    public static boolean terran() {
        return AGame.isPlayingAsTerran();
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

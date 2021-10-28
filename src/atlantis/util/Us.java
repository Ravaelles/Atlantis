package atlantis.util;

import atlantis.AGame;
import bwapi.Race;

public class Us {

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

    public static boolean isTerran() {
        return AGame.isPlayingAsTerran();
    }

    public static boolean isProtoss() {
        return AGame.isPlayingAsProtoss();
    }

    public static boolean isZerg() {
        return AGame.isPlayingAsZerg();
    }

}

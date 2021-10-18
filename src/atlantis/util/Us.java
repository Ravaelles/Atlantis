package atlantis.util;

import atlantis.AGame;

public class Us {

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

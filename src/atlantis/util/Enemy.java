package atlantis.util;

import atlantis.AGame;

public class Enemy {

    public static boolean terran() {
        return AGame.isEnemyTerran();
    }

    public static boolean protoss() {
        return AGame.isEnemyProtoss();
    }

    public static boolean zerg() {
        return AGame.isEnemyZerg();
    }

}

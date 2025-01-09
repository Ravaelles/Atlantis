package atlantis.game.race;

import atlantis.game.A;
import atlantis.game.AGame;
import bwapi.Race;

public class EnemyRace {
    protected static Race enemyRace = null;

    /**
     * Returns true if enemy plays as Terran.
     */
    public static boolean isEnemyTerran() {
        return Race.Terran.equals(enemyRace());
    }

    /**
     * Returns true if enemy plays as Protoss.
     */
    public static boolean isEnemyProtoss() {
        return Race.Protoss.equals(enemyRace());
    }

    /**
     * Returns true if enemy plays as Zerg.
     */
    public static boolean isEnemyZerg() {
        return Race.Zerg.equals(enemyRace());
    }

    public static Race enemyRace() {
        if (enemyRace != null) return enemyRace;

//        System.err.println("AGame.enemy().getRace() = " + AGame.enemy().getRace());

        if (!A.isUms()) return enemyRace = AGame.enemy().getRace();

//        System.err.println("DefineEnemyRaceForCustomMaps.define() = " + DefineEnemyRaceForCustomMaps.define());

        return enemyRace = DefineEnemyRaceForCustomMaps.define();
    }
}

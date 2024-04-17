package atlantis.game.race;

import atlantis.game.AGame;
import bwapi.Race;

public class EnemyRace {


    /**
     * Returns true if enemy plays as Terran.
     */
    public static boolean isEnemyTerran() {
        return AGame.enemy().getRace().equals(Race.Terran);
    }

    /**
     * Returns true if enemy plays as Protoss.
     */
    public static boolean isEnemyProtoss() {
        System.err.println("AGame.enemy().getRace() = " + AGame.enemy().getRace());
        return AGame.enemy().getRace().equals(Race.Protoss);
    }

    /**
     * Returns true if enemy plays as Zerg.
     */
    public static boolean isEnemyZerg() {
        return AGame.enemy().getRace().equals(Race.Zerg);
    }
}

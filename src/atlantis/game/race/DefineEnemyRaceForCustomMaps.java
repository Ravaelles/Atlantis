package atlantis.game.race;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import bwapi.Race;

public class DefineEnemyRaceForCustomMaps {
    public static Race define() {
        AUnit firstEnemy = EnemyUnits.discovered().first();

        if (firstEnemy == null) {
            System.err.println("COULDNT DETERMINE ENEMY RACE");
            return null;
        }

        return firstEnemy.type().ut().getRace();
    }
}

package atlantis.game.listeners;

import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnit;

public class OnEnemyUnitShow {
    public static void update(AUnit unit) {
        if (unit == null) return;

        // A re-sight arrives as show, never discover (the id was seen
        // before) - so a unit the fog map lost stays lost no matter how
        // often the army looks at it (live: visible units with no wrapper
        // halve the enemy sum on contact loss, NEXT #2). Wrap what show
        // sees when nothing remembers it; known units are untouched.
        // Enemy-only: show fires for every unit, including ours.
        if (unit.isEnemy() && EnemyUnits.getFoggedUnit(unit) == null) {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);
        }

        EnemyOnCloseIsland.potentialEnemyOnIsland(unit);
    }
}

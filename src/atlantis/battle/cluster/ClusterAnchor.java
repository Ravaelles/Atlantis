package atlantis.battle.cluster;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Where the one cluster belongs. Pure location answers, no orders, no
 * state - the commander decides posture and tactic, this only names the
 * point on the map. Separated so anchor logic changes without touching
 * engagement logic (SRP: locate vs fight).
 */
public class ClusterAnchor {

    public static HasPosition defendAnchor() {
        return Select.mainOrAnyBuilding();
    }

    public static HasPosition attackAnchor() {
        AUnit base = EnemyUnits.enemyBase();
        if (base != null) return base.position();
        HasPosition main = EnemyUnits.enemyMainBase();
        if (main != null) return main;
        // Unknown enemy: march on the nearest unexplored start location
        // (UAlbertaBot's "explore until we find a target"). Sitting home
        // on ATTACK played 18 minutes to 0/0 - measured 2026-10-17.
        AUnit ownMain = Select.mainOrAnyBuilding();
        HasPosition unexplored = ownMain != null
            ? BaseLocations.nearestUnexploredStartingLocation(ownMain.position())
            : null;
        if (unexplored != null) return unexplored;
        return Select.mainOrAnyBuilding();
    }
}

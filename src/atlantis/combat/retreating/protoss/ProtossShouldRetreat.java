package atlantis.combat.retreating.protoss;

import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossShouldRetreat {
    public static boolean shouldRetreat(AUnit unit) {
        Selection enemies = enemies(unit);
        Selection friends = unit.friendsNear().notRunning();

        if (ProtossSmallScaleRetreat.shouldSmallScaleRetreat(unit, friends, enemies)) {
            RetreatManager.GLOBAL_RETREAT_COUNTER++;
            return true;
        }

//                    if (shouldLargeScaleRetreat(enemies)) {
//                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
//                        return true;
//                    }

//                    if (shouldRetreatDueToSquadMostlyRetreating()) {
//                        unit.addLog("SquadMostlyRetreating");
//                        return true;
//                    }

        return false;
    }

    // =========================================================

    private static Selection enemies(AUnit unit) {
        return unit.enemiesNear().canAttack(unit, 6);
    }
}

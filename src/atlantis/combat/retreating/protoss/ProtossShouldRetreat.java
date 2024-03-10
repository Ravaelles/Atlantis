package atlantis.combat.retreating.protoss;

import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossShouldRetreat {
    public static boolean shouldRetreat(AUnit unit) {
        Selection enemies = enemies(unit);
        Selection friends = friends(unit);

        if (ProtossSmallScaleRetreat.shouldSmallScaleRetreat(unit, friends, enemies)) {
            if (unit.isLeader()) {
                RetreatManager.GLOBAL_RETREAT_COUNTER++;
            }
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

    private static Selection friends(AUnit unit) {
//        return unit.friendsNear().notRunning();
        return unit.friendsNear().havingAtLeastHp(30);
    }

    private static Selection enemies(AUnit unit) {
        return unit.enemiesNear().canAttack(unit, 6);
    }
}

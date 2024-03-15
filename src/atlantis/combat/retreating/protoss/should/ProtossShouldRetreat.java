package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.small_scale.ProtossSmallScaleRetreat;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossShouldRetreat extends Manager {
    public ProtossShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossSmallScaleRetreat.class,
        };
    }

//    public static boolean shouldRetreat(AUnit unit) {
//        Selection enemies = enemies(unit);
//        if (enemies.empty()) return false;
//
//        Selection friends = friends(unit);
//
//        if (ProtossSmallScaleRetreat.shouldSmallScaleRetreat(unit, friends, enemies)) {
//            if (unit.isLeader()) {
//                RetreatManager.GLOBAL_RETREAT_COUNTER++;
//            }
//            return true;
//        }
//
////                    if (shouldLargeScaleRetreat(enemies)) {
////                        RetreatManager.GLOBAL_RETREAT_COUNTER++;
////                        return true;
////                    }
//
////                    if (shouldRetreatDueToSquadMostlyRetreating()) {
////                        unit.addLog("SquadMostlyRetreating");
////                        return true;
////                    }
//
//        return false;
//    }

    // =========================================================

    public static Selection friends(AUnit unit) {
//        return unit.friendsNear().notRunning();
        return unit.friendsNear().combatUnits().havingAtLeastHp(18);
    }

    public static Selection enemies(AUnit unit) {
        return unit.enemiesNear().combatUnits().canAttack(unit, 7);
    }
}

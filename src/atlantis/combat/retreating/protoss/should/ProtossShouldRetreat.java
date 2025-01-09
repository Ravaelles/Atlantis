package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.big_scale.ProtossFullRetreat;
import atlantis.combat.retreating.protoss.small_scale.ProtossMeleeSmallScaleRetreat;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossShouldRetreat extends Manager {
    public ProtossShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossFullRetreat.class,
            ProtossMeleeSmallScaleRetreat.class,
        };
    }

//    public static boolean shouldRetreat(AUnit unit) {
//        Selection enemies = enemies(unit);
//        if (enemies.empty()) return false;
//
//        Selection friends = friends(unit);
//
//        if (ProtossMeleeSmallScaleRetreat.shouldSmallScaleRetreat(unit, friends, enemies)) {
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
        return unit.friendsNear().combatUnits().havingAtLeastHp(22);
    }

    public static Selection enemies(AUnit unit) {
        return unit.enemiesNear().combatUnits().canAttack(unit, 7);
    }
}

package atlantis.combat.retreating;

import atlantis.units.AUnit;

public class TempDontRetreat {

    /**
     * Allow Tanks and Marines to shoot even when retreating.
     */
    protected  boolean temporarilyDontRetreat () {
        return false;
//        if (unit.isRanged()) {
//
//            if (unit.enemiesNear().ranged().atLeast(2)) {
//                return false;
//            }
//
//            // No enemy can shoot us
//            if (unit.enemiesNear().inShootRangeOf(unit).empty()) {
//                unit.addFileLog("TemporarilyDontRetreat");
//                return true;
//            }
//
////            if (unit.isMissionDefend() && unit.groundWeaponCooldown() <= 3
////                && (unit.hp() >= 25 || unit.meleeEnemiesNearCount(2.7) == 0)) {
////                unit.addLog("TempAttack(MD)");
////                return true;
////            }
//
////            if (unit.isMissionAttack()
//            if ((unit.groundWeaponCooldown() <= 3 || unit.lastAttackFrameMoreThanAgo(30 * 5))
//                && (unit.hp() >= 25 || unit.meleeEnemiesNearCount(2.7) == 0)) {
//                unit.addFileLog("TempAttack(MA)");
//                return true;
//            }
//        }
//
//        return false;
    }
}

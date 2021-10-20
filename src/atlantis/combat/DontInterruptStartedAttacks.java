package atlantis.combat;

import atlantis.AGame;
import atlantis.units.AUnit;

public class DontInterruptStartedAttacks {

//    private static boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean shouldNotBeInterruptedStartedAttack(AUnit unit) {

        int lastAttackFrame = AGame.framesAgo(unit._lastAttackFrame);
        int lastStartingAttack = AGame.framesAgo(unit._lastStartingAttack);
        int cooldown = unit.getCooldownCurrent();
//        int friends = Select.ourCombatUnits().inRadius(2.5, unit).count();

        // =========================================================

        if (unit.isAttackFrame()) {
            unit.setTooltip("Attack frame(" + lastAttackFrame + "/" + lastStartingAttack + ")");
            return true;
        }

        if (unit.isStartingAttack() || unit.lastStartedAttackLessThanAgo(5)) {
            unit.setTooltip("Starts attack(" + lastAttackFrame + "/" + lastStartingAttack + ")");
            return true;
        }

        if (cooldown <= 3 && unit.lastAttackOrderLessThanAgo(9)) {
            unit.setTooltip("Attack(" + lastAttackFrame + "/" + lastStartingAttack + " // " + cooldown + ")");
            return true;
        }

//        if (unit.isAttacking() && (cooldown <= 3 || cooldown >= cooldownAbsolute - 10)) {
//        if (unit.isAttacking()) {
//            unit.setTooltip("Shooting(" + lastAttackFrame + "/" + unit.getCooldownCurrent() + ")");
////            if (DEBUG && AGame.getTimeFrames() > 50) {
//            if (DEBUG) {
//                System.out.println(
//                        AGame.now() + " - " +
//                        "#"+ unit.getID() + "  " +
//                        " DONT_INT(" + lastAttackFrame + "/" + lastStartingAttack +") " +
//                        ", COOL = " + unit.getCooldownCurrent() +
//                        ", DIST = " + unit.distanceTo(unit.getTarget()) +
//                        ", TAR = " + unit.getTarget().getShortName()
//                );
//            }
//            return true;
//        }

//        if (lastAttackFrame <= 8 || lastStartingAttack <= 16) {
//            return true;
//        }

//        if (!unit.isAttacking()) {
//            unit.setTooltip("AAA(" + unit.getCooldownCurrent() + ")");
//            return false;
//        }

//        if (unit.getCooldownCurrent() >= 4) {
//            unit.setTooltip("BBB(" + unit.getCooldownCurrent() + ")");
//            return false;
//        }

//        if (unit.getLastOrderFramesAgo() <= unit.getCooldownAbsolute() - 4) {
//            unit.setTooltip("CCC(" + unit.getCooldownCurrent() + ")");
//            return false;
//        }

//        if (unit.getLastOrderFramesAgo() <= unit.getCooldownAbsolute() - 4) {
////            unit.setTooltip("CCC(" + unit.getCooldownCurrent() + ")");
//            unit.setTooltip("CCC(" + lastAttackFrame + "/" + lastStartingAttack + ")");
//            return false;
//        }

        return false;

//        if (unit.getCooldownCurrent() == 0 || unit.getLastOrderFramesAgo() <= unit.getCooldownCurrent() - 4) {
//            double minDistToContinueAttack = 2.6 + unit.getWoundPercent() / 40.0;
//            if (unit.getHPPercent() >= 95 || Select.enemyRealUnits().melee().inRadius(minDistToContinueAttack, unit).isEmpty()) {
//                unit.setTooltip("Shoot");
//            }
//            unit.setTooltip("Attacking");
//            return true;
//        }

//        if (!unit.isAttacking() && unit.getLastOrderFramesAgo() <= 2) {
//            unit.setTooltip("Dont disturb (" + unit.getLastOrderFramesAgo() + ")");
//            return true;
//        }

//                ((!unit.type().isTank() || unit.getGroundWeaponCooldown() <= 0) && unit.isStartingAttack())
//                && unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
    }

}

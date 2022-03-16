package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.List;

public class TerranTankWhenSieged extends TerranTank {

    protected static boolean updateSieged(AUnit unit) {
//        if (handleShootingAtInvisibleUnits(unit)) {
//            return true;
//        }

        if (shouldNotThinkAboutUnsieging(unit)) {
            return false;
        }

        if (
            unit.enemiesNear().isEmpty()
                && unit.lastAttackFrameMoreThanAgo(30 * 3 + (unit.id() % 3))
                && unit.distToSquadCenter() >= 8
        ) {
            return wantsToUnsiege(unit, "Reposition");
        }

        if (allowToUnsiegeToMove(unit)) {
            return wantsToUnsiege(unit, "WantsToMove");
        }

        // Mission is CONTAIN
        if (
            Missions.isGlobalMissionContain()
                && unit.squad().distToFocusPoint() < 7.9
                && unit.lastAttackOrderLessThanAgo(7 * 30)
        ) {
            return false;
        }

//        if (
//            tooLonely(unit)
//                && !hasJustSiegedRecently(unit)
//                && unit.noCooldown()
//                && unit.lastStartedAttackMoreThanAgo(140)
//        ) {
////            System.out.println("LAST SIEGE = " + unit.lastActionAgo(Actions.SIEGE));
////            System.out.println("LAST UNSIEGE = " + unit.lastActionAgo(Actions.UNSIEGE));
//            return wantsToUnsiege(unit, "TooLonely");
//        }

        return false;
    }

    // =========================================================

    private static boolean shouldNotThinkAboutUnsieging(AUnit unit) {
//        unit.setTooltip(unit + ", SIEGED ago = " + unit.lastActionAgo(Actions.SIEGE), false);
//        unit.setTooltip(unit + ", UNSIEGED ago = " + unit.lastActionAgo(Actions.UNSIEGE), false);

        if (unit.cooldownRemaining() > 0) {
            return true;
        }

        if (unit.lastActionLessThanAgo(30 * (7 + (unit.idIsOdd() ? 3 : 0)), Actions.SIEGE)) {
            return true;
        }

        return false;
    }

    private static boolean wantsToUnsiege(AUnit unit, String log) {
        if (
            hasJustSiegedRecently(unit)
                || unit.lastAttackFrameLessThanAgo(30 * (1 + unit.id() % 5))
        ) {
            return false;
        }

        unit.setTooltipTactical(log);
        unit.addLog(log);
        unit.unsiege();
        return true;
    }

    // =========================================================

    private static boolean allowToUnsiegeToMove(AUnit unit) {
        if (unit.cooldownRemaining() == 0 && (A.now() % (1 + unit.id()) <= 1)) {
            return true;
        }

        return false;
    }

//    private static boolean handleShootingAtInvisibleUnits(AUnit unit) {
//        if (unit.lastActionLessThanAgo(55, Actions.ATTACK_POSITION)) {
//            unit.setTooltipTactical("SmashingHidden");
//            unit.addLog("SmashingHidden");
////            System.out.println(A.now() + " waiting " + unit.idWithHash());
//            return true;
//        }
//
//        List<AUnit> cloaked = Select.enemy()
//            .effCloaked()
//            .groundUnits()
//            .inRadius(11.9, unit)
//            .list();
//
//        AUnit enemy = Select.enemy().first();
////        System.out.println("enemy = " + enemy);
////        System.out.println("enemy position = " + enemy.position() + " // " + unit.distTo(enemy));
////        System.out.println("enemy isDetected = " + enemy.isDetected());
////        System.out.println("enemy hp = " + enemy.hp());
////        System.out.println("enemy type = " + enemy.type());
//
////        System.out.println("-----");
////        System.out.println("EnemyUnits.discovered() A = " + Select.enemy().size());
////        System.out.println("EnemyUnits.discovered() B = " + Select.enemy().effCloaked().size());
////        System.out.println("EnemyUnits.discovered() C = " + Select.enemy().effCloaked() .inRadius(11.9, unit).size());
//
//        for (AUnit cloakedUnit : cloaked) {
////            System.out.println(cloakedUnit + " // " + cloakedUnit.position());
//            double dist = cloakedUnit.position().distTo(unit);
//            if (4 <= dist && dist <= 12) {
////                System.err.println("Dist ok, can fire! dist=" + dist + " // cooldown=" + unit.cooldownRemaining());
////                if (unit.lastActionMoreThanAgo(30, Actions.ATTACK_POSITION)) {
////                }
////                System.out.println("SHOOT AT " + cloakedUnit.position());
//                unit.attackPosition(cloakedUnit.position());
//                unit.setTooltipTactical("SMASH!");
//                unit.addLog("SMASH!");
//
////                System.out.println(A.now() + " FIRED " + unit.idWithHash());
//                return true;
//            }
//        }
//
////        for (AUnit enemy : Select.enemy().effCloaked().groundUnits().inRadius(12, unit).list()) {
////            if (enemy.distTo(unit) >= unit.groundWeaponMinRange()) {
////                if (unit.lastActionMoreThanAgo(30, UnitActions.ATTACK_POSITION)) {
////                    unit.setTooltip("SMASH invisible!");
////                    unit.attackPosition(enemy.position());
////                }
////                unit.setTooltip("SmashInvisible");
////                return true;
////            }
////        }
//
//        return false;
//    }

}

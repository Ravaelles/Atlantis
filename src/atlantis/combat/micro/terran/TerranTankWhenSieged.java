package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

import java.util.List;

public class TerranTankWhenSieged extends TerranTank {

    protected static boolean updateSieged(AUnit unit) {
        if (shouldNotThinkAboutUnsieging(unit)) {
            return false;
        }

        if (handleShootingAtInvisibleUnits(unit)) {
            return true;
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

        if (
            tooLonely(unit)
                && !hasJustSiegedRecently(unit)
                && unit.noCooldown()
                && unit.lastStartedAttackMoreThanAgo(140)
        ) {
//            System.out.println("LAST SIEGE = " + unit.lastActionAgo(Actions.SIEGE));
//            System.out.println("LAST UNSIEGE = " + unit.lastActionAgo(Actions.UNSIEGE));
            return wantsToUnsiege(unit, "TooLonely");
        }

        return false;
    }

    // =========================================================

    private static boolean shouldNotThinkAboutUnsieging(AUnit unit) {
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
        if (unit.cooldownRemaining() == 0 && (A.now() % unit.id() <= 1)) {
            return true;
        }

        return false;
    }

    private static boolean handleShootingAtInvisibleUnits(AUnit tank) {
        if (tank.lastActionLessThanAgo(55, Actions.ATTACK_POSITION)) {
            tank.setTooltipTactical("SmashInvisible!");
            tank.addLog("SmashInvisible!");
            return true;
        }

        List<AUnit> cloaked = EnemyUnits.discovered()
            .effCloaked()
            .groundUnits()
            .inRadius(11.9, tank)
            .list();
        for (AUnit cloakedUnit : cloaked) {
//            System.out.println(cloakedUnit + " // " + cloakedUnit.position());
            if (cloakedUnit.distTo(tank) >= tank.groundWeaponMinRange()) {
//                if (tank.lastActionMoreThanAgo(30, Actions.ATTACK_POSITION)) {
//                }
//                System.out.println("SHOOT AT " + cloakedUnit.position());
                tank.attackPosition(cloakedUnit.position());
                tank.setTooltipTactical("SmashInvisible");
                tank.addLog("SmashInvisible");
                return true;
            }
        }

//        for (AUnit enemy : Select.enemy().effCloaked().groundUnits().inRadius(12, tank).list()) {
//            if (enemy.distTo(tank) >= tank.groundWeaponMinRange()) {
//                if (tank.lastActionMoreThanAgo(30, UnitActions.ATTACK_POSITION)) {
//                    tank.setTooltip("SMASH invisible!");
//                    tank.attackPosition(enemy.position());
//                }
//                tank.setTooltip("SmashInvisible");
//                return true;
//            }
//        }

        return false;
    }

}

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
                && unit.lastAttackFrameMoreThanAgo(30 * 4 + (unit.id() % 3))
                && unit.distToSquadCenter() >= 8
        ) {
            return wantsToUnsiege(unit, "Reposition");
        }

        if (allowToUnsiegeToMove(unit)) {
            return wantsToUnsiege(unit, "WantsTomove");
        }

        // Mission is CONTAIN
        if (
            Missions.isGlobalMissionContain()
                && unit.squad().distToFocusPoint() < 9.9
                && unit.lastAttackOrderLessThanAgo(7 * 30)
        ) {
            return false;
        }

        if (tooLonely(unit) && !hasJustSiegedRecently(unit)) {
            System.out.println("LAST SIEGE = " + unit.lastActionAgo(Actions.SIEGE));
            System.out.println("LAST UNSIEGE = " + unit.lastActionAgo(Actions.UNSIEGE));
            return wantsToUnsiege(unit, "TooLonely");
        }

        return false;
    }

    // =========================================================

    private static boolean shouldNotThinkAboutUnsieging(AUnit unit) {
        if (unit.lastActionLessThanAgo(30 * (9 + (unit.idIsOdd() ? 4 : 0)), Actions.SIEGE)) {
            return true;
        }

        return false;
    }

    private static boolean wantsToUnsiege(AUnit unit, String log) {
        if (
            hasJustSiegedRecently(unit)
                || unit.lastAttackFrameLessThanAgo(30 * 9)) {
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
        List<AUnit> cloaked = EnemyUnits.visibleAndFogged()
            .effCloaked()
            .havingPosition()
            .groundUnits()
            .inRadius(11.9, tank)
            .list();
        for (AUnit cloakedUnit : cloaked) {
            if (cloakedUnit.distTo(tank) >= tank.groundWeaponMinRange()) {
//                if (tank.lastActionMoreThanAgo(30, Actions.ATTACK_POSITION)) {
//                }
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

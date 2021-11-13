package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.Units;
import bwapi.Color;

public class WantsToAvoid {

    public static boolean units(AUnit unit, Units enemies) {
        if (shouldNotAvoid(unit, enemies)) {
            return false;
        }

        // =========================================================

        if (!shouldAvoid(unit, enemies)) {
            if ((new FightInsteadAvoid(unit, enemies)).shouldFight() && !AAttackEnemyUnit.shouldNotAttack(unit)) {
                return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
            }
        }

        // =========================================================

        if (enemies.size() == 1) {
            return Avoid.unit(unit, enemies.first());
        }
        else {
            return Avoid.groupOfUnits(unit, enemies);
        }
    }

    // =========================================================

    private static boolean shouldAvoid(AUnit unit, Units enemies) {
        return unit.isWorker() || unit.isScout() || unit.isSquadScout();
    }

    private static boolean shouldNotAvoid(AUnit unit, Units enemies) {
//        if (unit.isJustShooting()) {
//            return true;
//        }

//        if (!We.isTerran() && unit.lastUnderAttackMoreThanAgo(150) && unit.lastStartedAttackLessThanAgo(10)) {
//            unit.setTooltip("Kill");
//            return true;
//        }

//        if (unit.isRanged() && (unit.lastStartedAttackMoreThanAgo( 10) || unit.cooldownRemaining() == 0)) {
//            return true;
//        }

        // Running is not viable - so many other units nearby, would get stuck
        if (Select.all().inRadius(0.4, unit).count() >= 6) {
//            APainter.paintCircleFilled(unit, 8, Color.Black);
//            System.err.println("Dont avoid " + Select.all().inRadius(0.4, unit).count());
            return true;
        }


//                || unit.lastActionLessThanAgo(15, UnitActions.LOAD)
//                || (
//                unit.isRunning()
//                        && ATransportManager.hasNearbyTransportAssigned(unit)
//                        && ATransportManager.getTransportAssignedToUnit(unit).distToMoreThan(unit, 1)
//        )

        return false;
    }

}

package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;

public class WantsToAvoid {

    public static boolean units(AUnit unit, Units enemies) {
        if (shouldNotAvoid(unit, enemies)) {
            return false;
        }

        if ((new FightInsteadAvoid(unit, enemies)).shouldFight()) {
            return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        }

        else if (enemies.size() == 1) {
            return Avoid.unit(unit, enemies.first());
        }
        else {
            return Avoid.groupOfUnits(unit, enemies);
        }
    }

    // =========================================================

    private static boolean shouldNotAvoid(AUnit unit, Units enemies) {
//        if (unit.isJustShooting()) {
//            return true;
//        }

//        if (!Us.isTerran() && unit.lastUnderAttackMoreThanAgo(150) && unit.lastStartedAttackLessThanAgo(10)) {
//            unit.setTooltip("Kill");
//            return true;
//        }

//        if (unit.isRanged() && (unit.lastStartedAttackMoreThanAgo( 10) || unit.cooldownRemaining() == 0)) {
//            return true;
//        }

        // Running is not viable - so many other units nearby, would get stuck
        if (Select.all().inRadius(0.4, unit).count() >= 6) {
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

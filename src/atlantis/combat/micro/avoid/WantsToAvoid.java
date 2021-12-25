package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;
import bwapi.Color;

public class WantsToAvoid {

    public static boolean units(AUnit unit, Units enemies) {
        if (shouldNeverAvoidIf(unit, enemies)) {
            return false;
        }

        // =========================================================

        if (!shouldAlwaysAvoid(unit, enemies)) {
            if (
                    !unit.hasNoWeaponAtAll()
                    && (new FightInsteadAvoid(unit, enemies)).shouldFight()
            ) {
//                System.err.println("FIGHT INSTEAD AVOID " + unit.namePlusId() + " // " + unit.hp());
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

    private static boolean shouldAlwaysAvoid(AUnit unit, Units enemies) {
        if (unit.isWorker() || unit.isScout() || unit.isSquadScout() || unit.hpLessThan(17)) {
            return true;
        }

        return false;
    }

    private static boolean shouldNeverAvoidIf(AUnit unit, Units enemies) {
        if (unit.isAir()) {
            return false;
        }

        // Running is not viable - so many other units nearby, we would get stuck, better fight
        if (Select.all().inRadius(0.4, unit).count() >= 6) {
            APainter.paintCircleFilled(unit, 8, Color.Black);
//            System.err.println(unit + " fight cause clustered " + Select.all().inRadius(0.4, unit).count());
            return true;
        }

        return false;
    }

}

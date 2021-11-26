package atlantis.combat.micro.avoid;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;

public class WantsToAvoid {

    public static boolean units(AUnit unit, Units enemies) {
        if (shouldNeverAvoidIf(unit, enemies)) {
            return false;
        }

        if (shouldAlwaysAvoid(unit, enemies)) {
            return true;
        }

        // =========================================================

        if (
                (new FightInsteadAvoid(unit, enemies)).shouldFight()
                        && !AAttackEnemyUnit.shouldNotAttack(unit)
        ) {
            return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
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
        if (unit.isWorker() || unit.isScout() || unit.isSquadScout()) {
            return true;
        }

        return false;
    }

    private static boolean shouldNeverAvoidIf(AUnit unit, Units enemies) {

        // Running is not viable - so many other units nearby, we would get stuck, better fight
        if (Select.all().inRadius(0.4, unit).count() >= 6) {
//            APainter.paintCircleFilled(unit, 8, Color.Black);
//            System.err.println("Dont avoid " + Select.all().inRadius(0.4, unit).count());
            return true;
        }

        return false;
    }

}

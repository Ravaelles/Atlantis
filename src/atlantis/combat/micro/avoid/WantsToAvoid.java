package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.Color;

public class WantsToAvoid {

    public static boolean unitOrUnits(AUnit unit, Units enemies) {
        if (shouldNeverAvoidIf(unit, enemies)) {
            return false;
        }

        // =========================================================

        if (!shouldAlwaysAvoid(unit, enemies)) {
            if (
                    unit.hasAnyWeapon() && (new FightInsteadAvoid(unit, enemies)).shouldFight()
            ) {
                APainter.paintCircle(unit, 10, Color.Green);
                APainter.paintCircle(unit, 11, Color.Green);

                return AAttackEnemyUnit.handleAttackNearEnemyUnits(unit);
            }
        }

        // =========================================================

//        if (unit.isDragoon()) {
//            A.printStackTrace();
//        }

        if (enemies.size() == 1) {
            return Avoid.singleUnit(unit, enemies.first());
        }
        else {
            return Avoid.groupOfUnits(unit, enemies);
        }
    }

    // =========================================================

    private static boolean shouldAlwaysAvoid(AUnit unit, Units enemies) {
        if (unit.isWorker() || unit.isScout()) {
            unit.addLog("AlwaysAvoid");
            return true;
        }

        if (unit.hpLessThan(17) && !enemies.onlyMelee() && !Enemy.terran()) {
            unit.addLog("AlmostDead");
            return true;
        }

        if (unit.isSquadScout() && unit.isWounded() && unit.friendsNear().inRadius(3, unit).isEmpty()) {
            unit.addLog("SquadScoutAvoid");
            return true;
        }

        return false;
    }

    private static boolean shouldNeverAvoidIf(AUnit unit, Units enemies) {
        if (unit.isWorker() && enemies.onlyMelee()) {
            return unit.hp() >= 40;
        }

        if (unit.isWorker() || unit.isAir()) {
            return false;
        }

        // Running is not viable - so many other units Near, we would get stuck, better fight
        if (Select.all().inRadius(0.4, unit).count() >= 6) {
            APainter.paintCircleFilled(unit, 8, Color.Black);
//            System.err.println(unit + " fight cause clustered " + Select.all().inRadius(0.4, unit).count());
            return true;
        }

        return false;
    }

}

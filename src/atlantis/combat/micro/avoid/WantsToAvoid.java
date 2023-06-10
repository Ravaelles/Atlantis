package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
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
//                APainter.paintCircle(unit, 10, Color.Green);
//                APainter.paintCircle(unit, 11, Color.Green);

            unit.addFileLog("NOT shouldAlwaysAvoid");
            return AAttackEnemyUnit.handleAttackNearEnemyUnits(unit);
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
        if (!unit.hasAnyWeapon()) {
            return false;
        }

        if (
                unit.isMarine() && !A.isUms() && GamePhase.isEarlyGame() && unit.isRetreating()
                && (unit.hp() >= 24 && unit.cooldownRemaining() >= 1)
        ) {
            unit.addLog("DearGod");
            return true;
        }

        if (unit.isWorker() || unit.isScout()) {
            unit.addLog("AlwaysAvoid");
            return true;
        }

        if (unit.hpLessThan(17) && !enemies.onlyMelee() && !Enemy.terran()) {
            if (!unit.isMelee() && !unit.isMissionDefendOrSparta()) {
                unit.addLog("AlmostDead");
                return true;
            }
        }

        if (unit.isSquadScout() && unit.isWounded() && unit.friendsNear().inRadius(3, unit).isEmpty()) {
            unit.addLog("SquadScoutAvoid");
            return true;
        }

        if (new FightInsteadAvoid(unit, enemies).shouldFight()) {
            unit.addLog("SHOULD FightInsteadAvoid");
            return false;
        }

        return true;
    }

    private static boolean shouldNeverAvoidIf(AUnit unit, Units enemies) {
        if (unit.isWorker() && enemies.onlyMelee()) {
            return unit.hp() >= 40;
        }

        if (unit.isTank() && unit.cooldownRemaining() <= 0) {
            return true;
        }

        if (unit.isWorker() || unit.isAir()) {
            return false;
        }

        return false;
    }

}

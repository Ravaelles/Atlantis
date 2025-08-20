package atlantis.combat.eval.tweaks;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class EvalChokeTweaks {
    public static boolean applyHugePenaltyWhenCrossingChoke(AUnit unit) {
        if (Enemy.terran()) return false;

        return A.s % 12 <= 9
            && unit.enemiesNear().combatUnits().atLeast(3)
            && unit.ourToEnemyRangedUnitRatio() <= 1.7;
    }

    public static double nearChokePenalty(AUnit unit) {
        if (A.supplyUsed(185) || A.minerals() >= 1500) return 0;

        AUnit enemy = unit.enemiesNear().combatUnits().groundUnits().nearestTo(unit);
        if (enemy == null) return 0;

        AChoke choke = unit.nearestChoke();
        if (choke == null) return 0;

        double distToChoke = unit.distTo(choke);
        if (distToChoke >= 10) return 0;

        if (applyHugePenaltyVsZergEarly(unit, enemy, choke, distToChoke)) {
            return -2;
        }

        double enemyDistToChoke = enemy.distTo(choke);
        if (enemyDistToChoke > distToChoke || enemyDistToChoke > unit.distTo(enemy)) return 0;

        return unit.nearestChokeCenterDist() <= 9 ? chokePenalty(unit, choke, distToChoke) : 0;
    }

    private static boolean applyHugePenaltyVsZergEarly(AUnit unit, AUnit enemy, AChoke choke, double distToChoke) {
        if (!Enemy.zerg()) return false;
        if (Count.ourCombatUnits() >= 12) return false;
        if (Army.strengthWithoutCB() >= 350) return false;

        return true;
    }

    public static double chokePenalty(AUnit unit, AChoke choke, double distToChoke) {
        boolean missionDefendOrSparta = unit.isMissionDefendOrSparta();

        if (missionDefendOrSparta && unit.distToMain() <= 40) return 0;

        if (Count.ourCombatUnits() <= 30) {
            if (choke.width() <= 3.1) return -4;
            if (choke.width() <= 4) return -3.5;
        }

        if (applyHugePenaltyWhenCrossingChoke(unit)) return -3;

        double nearEnemyBuildings = unit.enemiesNear().buildings().notEmpty()
            ? (-0.15 + (Count.ourCombatUnits() <= 12 ? -0.15 : 0))
            : 0;

        return -2
            + nearEnemyBuildings
            + (missionDefendOrSparta ? -0.2 : 0)
            + (Army.strengthWithoutCB() <= 170 ? -0.5 : 0);
    }
}

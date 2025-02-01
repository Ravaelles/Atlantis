package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceForge {
    public static boolean produce() {
        if (Have.forge()) return false;

        if (predictEnemyDT()) return produce(1);

        int buildAtSupply = buildAtSupply();

        return produce(buildAtSupply);
    }

    private static boolean predictEnemyDT() {
        if (Count.ourCombatUnits() <= 3) return false;

        if (EnemyInfo.goesOrHasHiddenUnits()) return true;
        if (Army.strength() >= 120 && EnemyUnits.zealots() >= 4 && EnemyUnits.dragoons() <= 2) return true;

        return false;
    }

    private static boolean produce(int buildAtSupply) {
        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge);
    }

    private static int buildAtSupply() {
        if (EnemyStrategy.get().isGoingHiddenUnits()) return 24;

        if (Enemy.protoss() && !EnemyInfo.hasRanged()) return 34;
        if (Enemy.zerg()) return 42;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return 62;
    }
}

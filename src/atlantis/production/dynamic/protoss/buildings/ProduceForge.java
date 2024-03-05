package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceForge {
    public static void produce() {
        if (Have.forge()) return;

        int buildAtSupply = buildAtSupply();
        DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge);
    }

    private static int buildAtSupply() {
        if (EnemyStrategy.get().isGoingHiddenUnits()) return 24;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return 42;
    }
}

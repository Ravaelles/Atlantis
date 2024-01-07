package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceForge {
    public static void produce() {
        int buildAtSupply = EnemyStrategy.get().isRushOrCheese() ? 46 : 36;
        DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge);
    }
}

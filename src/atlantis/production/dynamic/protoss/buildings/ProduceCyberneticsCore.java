package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.generic.OurArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceCyberneticsCore {
    public static void produce() {
        if (Have.cyberneticsCore()) return;

        int buildAtSupply = buildAtSupply();
        DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge);
    }

    private static int buildAtSupply() {
        if (EnemyStrategy.get().isGoingHiddenUnits()) return 34;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return OurArmyStrength.relative() >= 120 ? 20 : 30;
    }
}

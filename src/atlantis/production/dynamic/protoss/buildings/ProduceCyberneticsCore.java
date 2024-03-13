package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceCyberneticsCore {
    public static boolean produce() {
        if (Have.cyberneticsCore()) return false;

        int buildAtSupply = buildAtSupply();
        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Cybernetics_Core);
    }

    private static int buildAtSupply() {
        if (A.hasMinerals(230)) return 0;

        if (EnemyStrategy.get().isGoingHiddenUnits()) return 34;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return OurArmy.strength() >= 120 ? 20 : 30;
    }
}

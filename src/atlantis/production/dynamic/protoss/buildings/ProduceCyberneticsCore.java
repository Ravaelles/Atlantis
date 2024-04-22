package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.protoss.units.DragoonInsteadZealot;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceCyberneticsCore {
    public static boolean produce() {
        if (Have.cyberneticsCore()) return false;

//        int buildAtSupply = buildAtSupply();
        if (needOne()) {
            return AddToQueue.withHighPriority(Protoss_Cybernetics_Core) != null;
        }
//        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Cybernetics_Core);

        return false;
    }

    private static boolean needOne() {
        return (A.supplyUsed(buildAtSupply()) || DragoonInsteadZealot.dragoonInsteadOfZealot())
            && Count.withPlanned(Protoss_Cybernetics_Core) == 0;
    }

    private static int buildAtSupply() {
        if (A.hasMinerals(250) && A.supplyUsed(16)) return 0;

        if (EnemyStrategy.get().isGoingHiddenUnits()) return 34;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return OurArmy.strength() >= 120 ? 20 : 30;
    }
}

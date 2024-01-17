package atlantis.production.dynamic.expansion.protoss;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ProtossExpansionCommander extends Commander {
    public ProtossExpansionCommander() {
    }

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(67)
            && Count.ourOfTypeUnfinished(AUnitType.Protoss_Nexus) < maxBasesAtATime()
            && ShouldExpand.shouldExpand();
    }

    @Override
    protected void handle() {
//        System.err.println("@ " + A.now() + " ProtossExpansionCommander ");
        requestNewBase();
    }

    private static void requestNewBase() {
        ProductionOrder productionOrder = AddToQueue.maxAtATime(AtlantisRaceConfig.BASE, maxBasesAtATime());

        if (productionOrder != null && Count.bases() <= maxBasesAtATime()) productionOrder.setModifier("NATURAL");
    }

    private static int maxBasesAtATime() {
        return A.minerals() <= 620 ? 1 : 2;
    }
}

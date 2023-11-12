package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.dynamic.expansion.secure.SecuringBase;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ExpansionCommander extends Commander {
    public ExpansionCommander() {
    }

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(37)
            && Have.barracks()
//            && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) <= 1
            && CountInQueue.count(AUnitType.Terran_Bunker) <= 0
            && ShouldExpand.shouldBuildNewBase();
//        return ShouldExpand.shouldBuildNewBase();
    }

    @Override
    protected void handle() {
//        System.err.println("ExpansionCommander.handle() @ " + A.now());
        prepareNewBase();
    }

    protected void prepareNewBase() {
//        System.err.println("@ " + A.now() + " newExpansionIsSecured() - " + newExpansionIsSecured());
        if (newExpansionIsSecured()) {
            requestNewBase();
        }
        else {
            secureNewBase();
        }
    }

    private void secureNewBase() {
        (new SecuringBase(NextBasePosition.nextBasePosition())).secure();
    }

    protected boolean newExpansionIsSecured() {
        if (!We.terran()) return true;

        return A.hasMinerals(500) || (new SecuringBase(NextBasePosition.nextBasePosition())).isSecure();
    }

    private static void requestNewBase() {
        // ZERG case
        if (We.zerg()) {
            AddToQueue.withStandardPriority(AtlantisRaceConfig.BASE, Select.naturalOrMain());
        }

        // TERRAN and PROTOSS
        else {
            ProductionOrder productionOrder = AddToQueue.maxAtATime(AtlantisRaceConfig.BASE, 1);

            if (productionOrder != null && Count.bases() <= 1) productionOrder.setModifier("NATURAL");
        }
    }
}

package atlantis.production.dynamic.expansion.zerg;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.dynamic.expansion.secure.terran.SecuringBaseAsTerran;
import atlantis.production.dynamic.expansion.terran.TerranEarlyExpansion;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ZergExpansionCommander extends Commander {
    public ZergExpansionCommander() {
    }

    @Override
    public boolean applies() {
        return We.zerg()
            && A.everyNthGameFrame(67)
            && ShouldExpand.shouldExpand();
    }

    @Override
    protected void handle() {
        prepareNewBase();
    }

    protected void prepareNewBase() {
        requestNewBase();
    }

    private static void requestNewBase() {
        AddToQueue.withStandardPriority(AtlantisRaceConfig.BASE, Select.naturalOrMain());
    }
}

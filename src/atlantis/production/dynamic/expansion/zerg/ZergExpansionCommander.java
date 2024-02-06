package atlantis.production.dynamic.expansion.zerg;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.add.AddToQueue;
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

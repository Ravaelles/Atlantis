package atlantis.production.dynamic.expansion.terran;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.expansion.secure.terran.SecuringBaseAsTerran;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

public class TerranExpansionCommander extends Commander {
    private SecuringBaseAsTerran securingBase;

    @Override
    public boolean applies() {
        return We.terran()
            && Have.barracks()
//            && Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) <= 1
            && CountInQueue.count(AUnitType.Terran_Bunker) <= 0
            && A.everyNthGameFrame(67)
            && ShouldExpand.shouldExpand();
    }

    @Override
    protected void handle() {
        securingBase = (new SecuringBaseAsTerran(NextBasePosition.nextBasePosition()));

//        System.err.println("ExpansionCommander.handle() @ " + A.now());
        prepareNewBase();
    }

    protected void prepareNewBase() {
//        System.err.println("@ " + A.now() + " newExpansionIsSecured() - " + newExpansionIsSecured());
        if (canProceedWithBaseConstruction()) {
            requestNewBase();
        }
        else {
            secureNewBase();
        }
    }

    private boolean canProceedWithBaseConstruction() {
        if (TerranEarlyExpansion.shouldExpandEarly()) return true;

        return newExpansionIsSecured();
    }

    private void secureNewBase() {
        securingBase.secureWithCombatBuildings();
    }

    protected boolean newExpansionIsSecured() {
        return securingBase.isSecure();
    }

    private static void requestNewBase() {
//        // ZERG case
//        if (We.zerg()) {
//            AddToQueue.withStandardPriority(AtlantisRaceConfig.BASE, Select.naturalOrMain());
//        }
//
//        // TERRAN and PROTOSS
//        else {
        ProductionOrder productionOrder = AddToQueue.maxAtATime(AtlantisRaceConfig.BASE, 1);

        if (productionOrder != null && Count.bases() <= 1) productionOrder.setModifier("NATURAL");
//        }
    }
}

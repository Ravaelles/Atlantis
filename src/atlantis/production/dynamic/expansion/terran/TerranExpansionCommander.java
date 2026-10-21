package atlantis.production.dynamic.expansion.terran;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructions.position.base.NextBasePosition;
import atlantis.production.dynamic.expansion.secure.terran.SecuringBaseAsTerran;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
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
//            && CountInQueue.count(AUnitType.Terran_Bunker) <= 0
            && A.everyNthGameFrame(67)
            && TerranShouldExpand.shouldExpand();
    }

    @Override
    protected boolean handle() {
        securingBase = (new SecuringBaseAsTerran(NextBasePosition.nextBasePosition()));

//        System.err.println("ExpansionCommander.handle() @ " + A.now());
        prepareNewBase();
        return false;
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
        // NOTE: maxAtATime counts nextOrders(), which skips IN_PROGRESS - the
        // Protoss twin of this call re-added a sibling Nexus 9 s after the
        // first (NEXT #58, fixed in ProtossExpandNow). Same shape here,
        // unmeasured on Terran - do not fix blind.
        ProductionOrder productionOrder = AddToQueue.maxAtATime(AtlantisRaceConfig.BASE, 1);

        if (productionOrder != null && Count.bases() <= 1) productionOrder.setModifier("NATURAL");
//        }
    }
}

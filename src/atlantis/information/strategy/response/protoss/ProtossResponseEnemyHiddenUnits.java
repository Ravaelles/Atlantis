package atlantis.information.strategy.response.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.production.constructions.cancelling.CriticalCancelPending;
import atlantis.production.dynamic.protoss.buildings.ProduceCannonAtNaturalOrMain;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProtossResponseEnemyHiddenUnits extends StrategyResponse {
    private int cannonNeeded;

    public boolean handle() {
        buildCannons();
        cancelBasesBeingConstructed();

        return true;
    }

    private void cancelBasesBeingConstructed() {
        if (Count.observers() == 0 && !Have.observatory()) {
            CriticalCancelPending.cancelBases();
        }
    }

    private void buildCannons() {
        cannonNeeded = cannonNeeded();
        cancelConstructionsIfNeededToGetMinerals();

        A.errPrintln("cannonsNeeded = " + cannonNeeded);

        for (int i = 0; i < cannonNeeded; i++) {
            ProduceCannonAtNaturalOrMain.produce();
        }
    }

    private static int cannonNeeded() {
        return Enemy.zerg()
            ? 2
            : (Enemy.protoss() ? 3 : 2);
    }

    private void cancelConstructionsIfNeededToGetMinerals() {
        int mineralsNeeded = cannonNeeded * 150;
        int mineralsMissing = mineralsNeeded - A.minerals();

        if (mineralsMissing > 90) {
            CriticalCancelPending.cancelToGetMinerals(mineralsMissing);
        }
    }
}

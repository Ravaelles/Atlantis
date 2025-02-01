package atlantis.information.strategy.response.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.production.constructions.cancelling.CriticalCancelPending;
import atlantis.production.dynamic.protoss.buildings.ProduceCannonAtNatural;

public class ProtossResponseEnemyHiddenUnits extends StrategyResponse {
    private int cannonNeeded;

    public void handle() {
        cannonNeeded = cannonNeeded();
        cancelConstructionsIfNeededToGetMinerals();

        for (int i = 0; i < cannonNeeded; i++) {
            ProduceCannonAtNatural.produce();
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

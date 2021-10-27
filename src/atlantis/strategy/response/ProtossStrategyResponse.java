package atlantis.strategy.response;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;

public class ProtossStrategyResponse extends AStrategyResponse {

    protected static boolean rushDefense(AStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        return true;
    }

    protected static int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

}

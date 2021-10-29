package atlantis.strategy.response.zerg;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.strategy.response.AStrategyResponse;

public class ZergStrategyResponse extends AStrategyResponse {

    protected static int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 4 : 3;
    }

}

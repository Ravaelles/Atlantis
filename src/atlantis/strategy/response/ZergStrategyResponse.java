package atlantis.strategy.response;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;

public class ZergStrategyResponse extends AStrategyResponse {

    protected static int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 4 : 3;
    }

}

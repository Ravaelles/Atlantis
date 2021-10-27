package atlantis.strategy.response;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;

public class ProtossStrategyResponse extends AStrategyResponse {

    protected static boolean rushDefense(AStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        AStrategyInformations.antiLandBuildingsNeeded(rushDefenseDefensiveBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected static int rushDefenseDefensiveBuildingsNeeded(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

}

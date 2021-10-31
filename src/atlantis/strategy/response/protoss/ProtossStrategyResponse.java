package atlantis.strategy.response.protoss;

import atlantis.combat.missions.Missions;
import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.strategy.response.AStrategyResponse;

public class ProtossStrategyResponse extends AStrategyResponse {

    // === Rushes ======================================================

    @Override
    protected boolean rushDefence(AStrategy enemyStrategy) {
        Missions.setGlobalMissionDefend();

        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        AStrategyInformations.setAntiLandBuildingsNeeded(rushDefenseDefensiveBuildingsNeeded(enemyStrategy));
        return true;
    }

    @Override
    protected int rushDefenseDefensiveBuildingsNeeded(AStrategy enemyStrategy) {
        return 1;
//        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    // === Air units ======================================================

    @Override
    protected void handleAirUnitsDefence() {
        AStrategyInformations.setAntiAirBuildingsNeeded(1);
    }

}

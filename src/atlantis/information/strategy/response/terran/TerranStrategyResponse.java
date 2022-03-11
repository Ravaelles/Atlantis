package atlantis.information.strategy.response.terran;

import atlantis.combat.missions.Missions;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.strategy.response.AStrategyResponse;

public class TerranStrategyResponse extends AStrategyResponse {

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            Missions.forceGlobalMissionDefend("Rush defence");
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) {
            return false;
        }

        OurStrategicBuildings.setAntiLandBuildingsNeeded(rushDefenseCombatBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 0;
//        return 1;
//        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

}

package atlantis.strategy.response.protoss;

import atlantis.combat.missions.Missions;
import atlantis.strategy.AStrategy;
import atlantis.strategy.decisions.OurStrategicBuildings;
import atlantis.strategy.response.AStrategyResponse;

public class ProtossStrategyResponse extends AStrategyResponse {

    // === Rushes ======================================================

    @Override
    protected boolean rushDefence(AStrategy enemyStrategy) {
        Missions.setGlobalMissionDefend();

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) {
            return false;
        }

        OurStrategicBuildings.setAntiLandBuildingsNeeded(rushDefenseCombatBuildingsNeeded(enemyStrategy));
        return true;
    }

    @Override
    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 1;
//        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    // === Air units ======================================================

    @Override
    protected void handleAirUnitsDefence() {
        OurStrategicBuildings.setAntiAirBuildingsNeeded(1);
    }

}

package atlantis.information.strategy.response.protoss;

import atlantis.combat.missions.Missions;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.AStrategyResponse;

public class ProtossStrategyResponse extends AStrategyResponse {

    // === Rushes ======================================================

    @Override
    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame() && !ArmyStrength.weAreMuchStronger()) {
            Missions.forceGlobalMissionDefend("Rush defence");
            return true;
        }

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

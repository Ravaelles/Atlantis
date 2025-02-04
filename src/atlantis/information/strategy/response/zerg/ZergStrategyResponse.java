package atlantis.information.strategy.response.zerg;

import atlantis.combat.missions.Missions;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;

public class ZergStrategyResponse extends RaceStrategyResponse {
    @Override
    public void onEnemyGoesHiddenUnits() {
    }

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            if (!ArmyStrength.weAreMuchStronger()) {
                Missions.forceGlobalMissionDefend("Rush defence");
                return true;
            }
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 0;
    }

}

package atlantis.information.strategy.response.zerg;

import atlantis.battle.cluster.Missions;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.units.AUnit;

public class ZergStrategyResponse extends RaceStrategyResponse {
    @Override
    public boolean requestDetection(AUnit enemyUnit) {
        return true;
    }

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

        return !shouldSkipAntiRushCombatBuilding(enemyStrategy);
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 0;
    }

}

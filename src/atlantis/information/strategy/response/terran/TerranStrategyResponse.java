package atlantis.information.strategy.response.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.AGame;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;

public class TerranStrategyResponse extends RaceStrategyResponse {
    @Override
    public void onEnemyGoesHiddenUnits() {
        (new TerranResponseEnemyHiddenUnits()).handle();
    }

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            if (!ArmyStrength.weAreMuchStronger() && AGame.killsLossesResourceBalance() < 600) {
                Missions.forceGlobalMissionDefend("Rush defence");
                return true;
            }
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
//        return 0;
        return 1;
//        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }
}

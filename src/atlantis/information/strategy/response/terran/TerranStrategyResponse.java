package atlantis.information.strategy.response.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.units.AUnit;

public class TerranStrategyResponse extends RaceStrategyResponse {
    @Override
    public boolean requestDetection(AUnit enemyUnit) {
        return (new TerranResponseEnemyHiddenUnits()).handle();
    }

    @Override
    public void onEnemyGoesHiddenUnits() {
        System.out.println(A.minSec() + " TerranStrategyResponse.onEnemyGoesHiddenUnits");
        (new TerranResponseEnemyHiddenUnits()).handle();
    }

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            if (Army.strength() <= 120 && AGame.killsLossesResourceBalance() < 500) {
                Missions.forceGlobalMissionDefend("Rush defence");
                return true;
            }
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 1;
//        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

    // === Air units ======================================================

    @Override
    public void handleAirUnitsDefence() {
        TerranAirDefence.update();
    }
}

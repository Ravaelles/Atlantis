package atlantis.information.strategy.response.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

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
            int strength = Army.strength();

            if (Enemy.zerg() && strength <= 105 && EnemyUnits.combatUnits() >= 4) {
                requestBunkerNearMain();

                Missions.forceGlobalMissionDefend("Rush bunker defence");
                return true;
            }

            if (strength <= 120 && AGame.killsLossesResourceBalance() < 500) {
                Missions.forceGlobalMissionDefend("Rush defence");
                return true;
            }
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
    }

    private void requestBunkerNearMain() {
        if (Count.withPlanned(AUnitType.Terran_Bunker) > 0) return;

        APosition at = Select.mainOrAnyBuildingPosition().translateTilesTowards(3, Chokes.mainChoke());
        AddToQueue.withTopPriority(AUnitType.Terran_Bunker, at);
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

package atlantis.information.strategy.response.protoss;

import atlantis.battle.cluster.Missions;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ProtossStrategyResponse extends RaceStrategyResponse {

    // === Rushes ======================================================

    @Override
    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame() && !ArmyStrength.weAreMuchStronger() && !shouldIgnoreRushBecauseWeHaveGoons()) {
            Missions.forceGlobalMissionDefend("Rush defence");

            return true;
        }

        return !shouldSkipAntiRushCombatBuilding(enemyStrategy);
    }

    private boolean shouldIgnoreRushBecauseWeHaveGoons() {
        if (!Enemy.protoss()) return false;

        int goons = Count.dragoonsWithUnfinished();

        return goons > 0 && goons >= 1.5 * EnemyUnits.dragoons();
    }

    // =========================================================

    @Override
    public boolean requestDetection(AUnit enemyUnit) {
        // Single policy (NEXT #228): the legacy queue writes died with the
        // drain - nothing converts them into constructions, so they only
        // polluted Have/planned reads. Detection orders come from the V2
        // goal layer (addDetectionGoals, pinned HiddenUnitResponseTest),
        // fed by the EnemyFlags latch the hidden-units handler sets.
        return true;
    }

    @Override
    public void onEnemyGoesHiddenUnits() {
        (new ProtossResponseEnemyHiddenUnits()).handle();
    }

    // =========================================================

    @Override
    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 2;
//        return enemyStrategy.isGoingCheese() ? 3 : 2;
    }

    // === Air units ======================================================

    @Override
    public void handleAirUnitsDefence() {
    }

}

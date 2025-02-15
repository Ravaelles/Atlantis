package atlantis.information.strategy.response.protoss;

import atlantis.combat.missions.Missions;
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

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) return false;

        return true;
    }

    private boolean shouldIgnoreRushBecauseWeHaveGoons() {
        if (!Enemy.protoss()) return false;

        int goons = Count.dragoonsWithUnfinished();
        return goons > 0 && goons >= 1.5 * EnemyUnits.dragoons();
    }

    // =========================================================

    @Override
    public boolean requestDetection(AUnit enemyUnit) {
        return RequestProtossDetection.needDetectionAgainst(enemyUnit);
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

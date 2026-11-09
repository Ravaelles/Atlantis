package atlantis.information.strategy.response.protoss;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.units.AUnit;

public class ProtossStrategyResponse extends RaceStrategyResponse {

    // === Rushes ======================================================

    @Override
    protected boolean rushDefence(AStrategy enemyStrategy) {
        // The DEFEND half lived here as a global-mission force; the
        // posture owns it now (RUSH_DEFENCE, owner 2026-11-09, one
        // decider): early + rushed + not much stronger + no ignoring
        // goon mass holds every frame instead of only on response
        // ticks. What stays here is the building half (whether the
        // anti-rush cannons still go up). The goon-mass ignore below
        // died with the force: its exact numbers live in the posture
        // rule now (RUSH_DEFENCE_GOON_RATIO), and two copies that can
        // disagree are how the split started.
        return !shouldSkipAntiRushCombatBuilding(enemyStrategy);
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

package atlantis.information.strategy.response.protoss;

import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.units.AUnit;

public class ProtossStrategyResponse extends RaceStrategyResponse {

    // === Rushes ======================================================
    // (ported: the defend half lives in PostureReasons as RUSH_DEFENCE
    // now - one decider, every frame. The response chain below it was
    // a no-op (ignored return, zero callers for the building counters)
    // and is deleted, not ported.)

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

    // === Air units ======================================================

    @Override
    public void handleAirUnitsDefence() {
    }

}

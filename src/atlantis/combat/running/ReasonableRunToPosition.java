package atlantis.combat.running;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ReasonableRunToPosition {
    private final atlantis.combat.running.ARunningManager ARunningManager;

    public ReasonableRunToPosition(atlantis.combat.running.ARunningManager ARunningManager) {
        this.ARunningManager = ARunningManager;
    }

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position
    ) {
        return ARunningManager.runPositionFinder.isPossibleAndReasonablePosition(unit, position);
    }

    public boolean isPossibleAndReasonablePosition(
        AUnit unit, APosition position, boolean includeNearWalkability, String charForIsOk, String charForNotOk
    ) {
        return ARunningManager.runPositionFinder.isPossibleAndReasonablePosition(
            unit, position, includeNearWalkability, charForIsOk, charForNotOk
        );
    }
}
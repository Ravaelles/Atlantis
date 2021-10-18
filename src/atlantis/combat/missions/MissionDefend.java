package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.micro.terran.TerranInfantryManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class MissionDefend extends Mission {

    protected MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPointManager();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        APosition focusPoint = focusPoint();
        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        return moveToDefendFocusPoint(unit, focusPoint);
    }

    // =========================================================

    private boolean moveToDefendFocusPoint(AUnit unit, APosition focusPoint) {

        // === Load infantry into bunkers ==========================

        if (AGame.isPlayingAsTerran() && TerranInfantryManager.tryLoadingInfantryIntoBunkerIfPossible(unit)) {
            return true;
        }

        // =========================================================

        if (unit.distanceTo(focusPoint) >= 5) {
            unit.move(focusPoint, UnitActions.MOVE_TO_FOCUS, "MoveToDefend");
            return true;
        }
        else if (unit.distanceTo(focusPoint) >= 4) {
            if (unit.isMoving()) {
                unit.holdPosition("DefendHere");
            }
            return true;
        } else {
            unit.moveAwayFrom(focusPoint, 0.5, "TooFar");
            return true;
        }
    }

}

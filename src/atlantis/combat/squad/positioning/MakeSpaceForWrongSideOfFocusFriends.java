package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.OnWrongSideOfFocusPoint;
import atlantis.game.A;
import atlantis.terran.chokeblockers.ChokeBlockersAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MakeSpaceForWrongSideOfFocusFriends extends Manager {
    private AUnit friendToLetGo;

    public MakeSpaceForWrongSideOfFocusFriends(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMoving() && unit.lastActionLessThanAgo(30, Actions.MOVE_SPACE)) return true;

        return unit.isMissionSparta()
//            && A.everyNthGameFrame(35)
            && ChokeBlockersAssignments.get().isChokeBlocker(unit)
            && friendNearOnWrongSideOfFocus();
    }

    private boolean friendNearOnWrongSideOfFocus() {
        for (AUnit friend : unit.friendsNear().groundUnits().combatUnits().list()) {
            if (friend.isActiveManager(OnWrongSideOfFocusPoint.class)) {
                friendToLetGo = friend;
                return true;
            }
        }

        return false;
    }

    public Manager handle() {
        if (
            friendToLetGo != null &&
//                unit.moveAwayFrom(friendToLetGo, 3, Actions.MOVE_SPACE, "Space4Friend")
                unit.moveToMain(Actions.MOVE_SPACE, "Space4Friend")
        ) {
            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - helping...");
            return usedManager(this);
        }

        return null;
    }
}

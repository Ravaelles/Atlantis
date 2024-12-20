package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class LeaderTooLowCohesion extends MissionManager {
    //    private AChoke focusPoint;
    private HasPosition goTo;

    public LeaderTooLowCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {

//        focusPoint = Chokes.natural();
//        goTo = goTo();

        if (focusPoint == null || unit.distTo(focusPoint) <= 8) return false;
        if (OurArmy.strength() >= 220) return false;

        return unit.squad() != null && unit.squadSize() >= 7 && unit.squad().cohesionPercent() <= 45;
    }

    @Override
    protected Manager handle() {
        if (unit.isOvercrowded()) return null;
        if (enoughFriendsNearby()) return null;

//        if (unit.move(focusPoint, Actions.MOVE_FORMATION, "Leader2Focus")) return usedManager(this);
        HasPosition goTo = goTo();
        if (goTo == null) return null;
        if (unit.distTo(goTo) >= 20) return null;

        if (
            (unit.isMoving() && A.fr % 10 != 0) || unit.move(goTo, Actions.MOVE_FORMATION, "LeaderCohesion")
        ) return usedManager(this);

        return null;
    }

    private HasPosition goTo() {
        Squad squad = unit.squad();
        if (squad == null) return null;

        return squad.center();
    }

    private boolean enoughFriendsNearby() {
        int minFriends = Math.min(unit.squadSize() - 2, 5);

        return unit.friendsNear().groundUnits().inRadius(2.3, unit).size() >= minFriends;
    }
}

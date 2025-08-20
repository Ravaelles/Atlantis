package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.Squad;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

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

        if (focus == null || unit.distTo(focus) <= 8) return false;
        if (Army.strength() >= 300 && Count.ourCombatUnits() >= 15) return false;
        if (unit.nearestChokeDist() <= 3) return false;

        return unit.squad() != null
            && unit.squadSize() >= 4
            && unit.squad().cohesionPercent() <= (Army.strengthWithoutCB() <= 170 ? 85 : 75);
    }

    @Override
    protected Manager handle() {
        if (unit.isOvercrowded()) return null;
//        if (enoughFriendsNearby()) return null;

//        if (unit.move(focusPoint, Actions.MOVE_FORMATION, "Leader2Focus")) return usedManager(this);
//        HasPosition goTo = goTo();
//        if (goTo == null) return null;
//        if (unit.distTo(goTo) >= 20) return null;

//        if (
//            (unit.isMoving() && A.now % 10 != 0) || unit.move(goTo, Actions.MOVE_FORMATION, "LeaderCohesion")
//        ) return usedManager(this);

        AUnit enemy = unit.nearestCombatEnemy();
        if (unit.distTo(enemy) <= 10) {
            if (unit.moveAwayFrom(enemy, 1, Actions.MOVE_FORMATION, "CohesionAway")) {
                System.out.println("LowCoh A");
                return usedManager(this);
            }
        }

        if (unit.isMoving() && unit.holdPosition(Actions.MOVE_FORMATION, "CohesionHold")) {
            System.out.println("LowCoh B");
            return usedManager(this);
        }

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

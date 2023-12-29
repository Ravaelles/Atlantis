package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AdvanceAsALeader extends MissionManager {
    public AdvanceAsALeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return squad.isLeader(unit);
    }

    @Override
    protected Manager handle() {
//        if (unit.isMissionAttackOrGlobalAttack()) return null;

        if (handleWhenLonely()) {
            return usedManager(this);
        }

        int cohesionPercent = unit.squad().cohesionPercent();
        int friendsNear = unit.friendsInRadius(7).count();

        if (cohesionPercent <= 84 && friendsNear <= squad.size() * 0.7) {
            actWithCohesionTooLow("LeaderWaitA");
            return usedManager(this);
        }

        if (cohesionPercent <= 74 && friendsNear <= squad.size() * 0.8) {
            actWithCohesionTooLow("LeaderWaitB");
            return usedManager(this);
        }

        if (cohesionPercent <= 69) {
            actWithCohesionTooLow("LeaderWaitC");
            return usedManager(this);
        }

        return null;
    }

    private boolean handleWhenLonely() {
        Squad squad = unit.squad();
        if (squad.size() <= 3) return false;

//        Selection friends = unit().friendsNear().combatUnits().inRadius(2, unit);
//        if (friends.empty()) {
        APosition moveTo = squad.center();
        if (moveTo != null && moveTo.distTo(unit) > 4) {
            unit.move(moveTo, Actions.MOVE_FORMATION, "LeaderLonely");
            return true;
        }
//        }

        return false;
    }

    private boolean actWithCohesionTooLow(String tooltip) {
        HasPosition moveTo = null;

        Selection tanks = Select.ourTanks().exclude(unit);
        if (tanks.count() >= 3) {
            moveTo = tanks.nearestTo(unit);
            unit.move(moveTo, Actions.MOVE_FORMATION, "LeaderMergeTanks");
            return true;
        }

//        HasPosition moveTo = unit.friendsNear().mostDistantTo(Select.mainOrAnyBuilding());
//        APosition moveTo = squad.center();
        moveTo = squad.center();
//        APosition moveTo = squad.averageUnit();

        if (moveTo != null && unit.distTo(moveTo) > 4) {
            unit.move(moveTo, Actions.MOVE_FORMATION, tooltip);
            return true;
        }

        if (!unit.squad().isCohesionPercentOkay() && A.seconds() % 4 <= 1) {
            if (unit.friendsNear().inRadius(5, unit).atLeast(5)) {
                unit.holdPosition("LeaderWaiting");
                return true;
            }
            else {
                AUnit nearestFriend = unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);
                if (nearestFriend != null) {
                    unit.move(nearestFriend, Actions.MOVE_FORMATION, "LeaderMergeFriend");
                    return true;
                }
            }
        }

        return false;
    }
}

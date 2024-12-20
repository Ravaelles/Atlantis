package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class LeaderWait extends MissionManager {
    public LeaderWait(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        double avgDistError = SquadDistanceToFocus.avgSquadDistErrorComparedToLeader(squad, unit, CurrentFocusChoke.get());

        System.err.println("avgDistError = " + avgDistError);

        return avgDistError > 2;
    }

    @Override
    protected Manager handle() {
//        if (unit.isMissionAttackOrGlobalAttack()) return null;

//        if (handleWhenTooFarFromSquadCenter()) return usedManager(this, "ToSquadCenter");
//        if (ToLastSquadTarget.goTo(unit)) return usedManager(this, "ToSquadTarget");

//        if (handleWhenLonely()) return usedManager(this, "LonelyLeader");
//
//        int cohesionPercent = unit.squad().cohesionPercent();
//        int friendsNear = unit.friendsInRadius(3).count();
//
//        if (cohesionPercent <= 84 && friendsNear <= squad.size() * 0.7) {
//            actWithCohesionTooLow("LeaderWaitA");
//            return usedManager(this);
//        }
//
//        if (cohesionPercent <= 74 && friendsNear <= squad.size() * 0.8) {
//            actWithCohesionTooLow("LeaderWaitB");
//            return usedManager(this);
//        }

//        if (cohesionPercent <= 69) {
//            actWithCohesionTooLow("LeaderWaitC");
//            return usedManager(this);
//        }

        return null;
    }

    private boolean handleWhenTooFarFromSquadCenter() {
        double distToSquadCenter = unit.distToSquadCenter();
        if (distToSquadCenter <= 3) return false;

        if (
            distToSquadCenter >= 3
                || unit.friendsNear().groundUnits().inRadius(3, unit).atMost(2)
        ) {
            return unit.move(squad.center(), Actions.MOVE_FORMATION, "LeaderToSquadCenter");
        }

        return false;
    }

    private boolean handleWhenLonely() {
        if (!isLonely()) return false;

        AUnit moveTo = whenLonelyGoTo(squad);
        if (moveTo != null && moveTo.distTo(unit) > 3) {
            unit.move(moveTo, Actions.MOVE_FORMATION, "LeaderLonely");
            return true;
        }

        return false;
    }

    private boolean isLonely() {
        Squad squad = unit.squad();
        if (squad.size() <= 2) return false;

        return unit.friendsNear().inRadius(5, unit).atMost(squad.size() / 2);
    }

    private AUnit whenLonelyGoTo(Squad squad) {
        return squad.units().exclude(unit).nearestTo(squad.center());
//        return squad.units().exclude(unit).groundUnits().mostDistantTo(Select.mainOrAnyBuilding());
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

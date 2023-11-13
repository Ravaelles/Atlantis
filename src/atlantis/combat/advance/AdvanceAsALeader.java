package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

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

    private boolean actWithCohesionTooLow(String tooltip) {
//        APosition moveTo = squad.center();
        HasPosition moveTo = unit.friendsNear().mostDistantTo(Select.mainOrAnyBuilding());

        if (moveTo != null) {
            unit.move(moveTo, Actions.MOVE_FORMATION, tooltip);
            return true;
        }

        return false;
    }
}

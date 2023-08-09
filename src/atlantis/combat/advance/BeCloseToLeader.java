package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class BeCloseToLeader extends MissionManager {
    public BeCloseToLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isAir()
            && !squad.isLeader(unit)
            && unit.mission().focusPoint() != null
            && unit.friendsInRadiusCount(3) <= 9
            && (unit.noCooldown() && unit.enemiesNearInRadius(7) == 0);
    }

    @Override
    protected Manager handle() {
        if (shouldGetBackToLeader()) {
            unit.move(squad.leader(), Actions.MOVE_FORMATION, "CloserToLeader");
            return usedManager(this);
        }

        return null;
    }

    private boolean shouldGetBackToLeader() {
        AUnit leader = squad.leader();

        if (unit.distTo(leader) >= 9) return true;

        if (leader.friendsNear().inRadius(2, leader).count() >= 7) {
            return false;
        }

        if (
            squad.cohesionPercent() <= 70
                && unit.friendsInRadiusCount(1) <= 4
        ) return true;

        return false;
    }
}

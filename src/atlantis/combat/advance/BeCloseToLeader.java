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
        return !unit.isAir() && !squad.isLeader(unit) && unit.enemiesNearInRadius(7) == 0;
    }

    @Override
    public Manager handle() {
        if (
            squad.cohesionPercent() <= 70
                && unit.friendsInRadiusCount(1) <= 4
        ) {
            unit.move(squad.leader(), Actions.MOVE_FORMATION, "CloserToLeader");
            return usedManager(this);
        }

        return null;
    }
}

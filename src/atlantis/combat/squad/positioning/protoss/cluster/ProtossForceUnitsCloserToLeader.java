package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossForceUnitsCloserToLeader extends Manager {
    public ProtossForceUnitsCloserToLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isCombatUnit()
            && unit.isGroundUnit()
            && !unit.isRunningOrRetreating()
            && unit.lastUnderAttackMoreThanAgo(50)
            && unit.distToLeader() >= 10
            && unit.friendsInRadiusCount(4) <= 5
            && (unit.cooldown() >= 10 || unit.lastStartedAttackMoreThanAgo(20));
    }

    @Override
    public Manager handle() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        if (unit.move(leader, Actions.MOVE_FORMATION, "ForceToLeader")) {
            return usedManager(this);
        }

        return null;
    }
}


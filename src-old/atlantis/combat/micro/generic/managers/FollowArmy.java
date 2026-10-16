package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FollowArmy extends Manager {
    private HasPosition followPoint;

    public FollowArmy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        HasPosition squadCenter = unit.squadCenter();
        followPoint = squadCenter;

        if (followPoint != null) {
            AUnit nearestInvisibleEnemy = unit.enemiesNear().effUndetected().nearestTo(squadCenter);
            if (nearestInvisibleEnemy != null) followPoint = nearestInvisibleEnemy;
        }

        return followPoint != null;
    }

    public Manager handle() {
        if (followPoint == null || followPoint.position() == null) return null;

        if (followPoint.distTo(unit) > 0.5) {
            unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowArmy", true);
            return usedManager(this);
        }

        return null;
    }
}

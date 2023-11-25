package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FollowAlphaScout extends Manager {
    private HasPosition followPoint;

    public FollowAlphaScout(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        followPoint = Alpha.get().squadScout();

        if (followPoint != null && followPoint.distTo(unit) < 15) {
            return true;
        }

        return followPoint != null;
    }

    public Manager handle() {
        unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowSquadScout", true);
        return usedManager(this);
    }
}

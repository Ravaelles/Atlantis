package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.attack.focus.MissionAttackFocusPoint;
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
        followPoint = getFollowPoint();

        if (followPoint != null && followPoint.distTo(unit) < 15) {
            return true;
        }

        return followPoint != null;
    }

    private static HasPosition getFollowPoint() {
        AUnit squadScout = Alpha.get().squadScout();

        if (squadScout == null) return null;

        AUnit basePoint = squadScout;
        AFocusPoint focus = squadScout.mission().focusPoint();

        if (basePoint == null || focus == null) return null;

        return basePoint.translateTilesTowards(10, focus);
    }

    public Manager handle() {
        unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowSquadScout", true);
        return usedManager(this);
    }
}

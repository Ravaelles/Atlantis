package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FollowAlphaLeader extends Manager {
    private HasPosition followPoint;

    public FollowAlphaLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLeader()) return false;

        followPoint = followPoint();
        if (followPoint != null && followPoint.hasPosition() && followPoint.distTo(unit) < 20 && followPoint.isWalkable()) {
            return true;
        }

        return false;
    }

    private HasPosition followPoint() {
        AUnit leader = Alpha.get().leader();
        if (leader == null || leader.isDead()) return null;

        HasPosition basePoint = leader;
        AFocusPoint focus = leader.mission().focusPoint();

        if (focus == null) return null;

        HasPosition towards = unit.squadCenter();
        if (towards != null) basePoint = basePoint.translatePercentTowards(50, towards);
        basePoint = basePoint.translateTilesTowards(10, focus);

        AUnit undetected = EnemyUnits.discovered().inRadius(25, unit).havingWeapon().effUndetected().first();
        if (undetected != null && undetected.hasPosition()) {
            basePoint = basePoint.translatePercentTowards(undetected, 85);
        }
        else {
            AUnit cloakable = EnemyUnits.discovered().inRadius(18, unit).havingWeapon().cloakable().first();
            if (cloakable != null && cloakable.hasPosition()) {
                basePoint = basePoint.translatePercentTowards(cloakable, 85);
            }
        }

        return basePoint;
    }

    public Manager handle() {
        unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowLeader", true);
        return usedManager(this);
    }
}

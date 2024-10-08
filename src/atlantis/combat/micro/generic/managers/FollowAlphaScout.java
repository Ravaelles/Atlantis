package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.attack.focus.MissionAttackFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
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

        if (followPoint != null && followPoint.distTo(unit) < 20) {
            return true;
        }

        return followPoint != null;
    }

    private HasPosition getFollowPoint() {
        AUnit squadScout = Alpha.get().squadScout();

        if (squadScout == null) return null;

        HasPosition basePoint = squadScout;
        AFocusPoint focus = squadScout.mission().focusPoint();

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
        unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowSquadScout", true);
        return usedManager(this);
    }
}

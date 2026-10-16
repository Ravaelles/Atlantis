package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

public class FollowAlphaLeader extends Manager {
    private HasPosition followPoint;

    public FollowAlphaLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.isLeader()) return false;
//        if (unit.isIdle()) return true;

        followPoint = followPoint();
        if (followPoint != null && followPoint.hasPosition()) {
//            AAdvancedPainter.paintCircleFilled(followPoint, 20, Color.Cyan);
            return true;
        }

        return false;
    }

    private HasPosition followPoint() {
        AUnit leader = Alpha.get().leader();
        if (leader == null || leader.isDead() || unit.isLeader()) return null;

        HasPosition basePoint = leader;
        AFocusPoint focus = leader.mission().focusPoint();
//        if (focus == null) return leader;
//
//        HasPosition towards = unit.squadCenter();
//        if (towards == null) return leader;

        if (focus != null) {
//            basePoint = basePoint.translatePercentTowards(50, focus);
            basePoint = basePoint.translateTilesTowards(10, focus);
        }

        Selection enemies = EnemyUnits.discovered().inRadius(25, unit);

        AUnit nearestEnemy = enemies.nearestTo(basePoint);
        if (nearestEnemy != null && focus != null) basePoint = basePoint.translateTilesTowards(9, focus);

//        AAdvancedPainter.paintCircleFilled(towards, 16, Color.Orange);

        if (basePoint == null) return Select.ourCombatUnits().first();

        AUnit undetected = enemies.havingWeapon().effUndetected().first();
        if (undetected != null && undetected.hasPosition()) {
            basePoint = basePoint.translatePercentTowards(undetected, 85);
        }
        else {
            AUnit cloakable = enemies.havingWeapon().cloakable().first();
            if (cloakable != null && cloakable.hasPosition()) {
                basePoint = basePoint.translatePercentTowards(cloakable, 85);
            }
        }

        if (basePoint == null) return Select.ourCombatUnits().first();

        if (leader.distTo(basePoint) > 15) {
            basePoint = leader.translateTilesTowards(15, basePoint);
        }

        if (basePoint == null) return Select.ourCombatUnits().first();

        return basePoint;
    }

    public Manager handle() {
        if (followPoint == null || !followPoint.hasPosition()) {
            if (Count.ourCombatUnits() >= 2) {
                ErrorLog.printMaxOncePerMinute("IdleFollowAlphaLeader: followPoint is null for " + unit);
            }
            return null;
        }
        if (unit.distTo(followPoint) < 0.5) return null;

        if (canFollowAhead() && unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowLeaderA", true)) {
            return usedManager(this);
        }

        AUnit leader = Alpha.alphaLeader();
        if (leader != null && unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowLeaderB")) {
            return usedManager(this);
        }

        return null;
    }

    private boolean canFollowAhead() {
        return unit.lastPositionChangedAgo() <= 100 || (A.s % 6 <= 1);
    }
}

package atlantis.combat.missions.attack;

import atlantis.combat.missions.WeDontKnowWhereEnemyIs;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;

public class MoveToAttackFocusPoint extends Manager {

    public MoveToAttackFocusPoint(AUnit unit) {
        super(unit);
    }

    public boolean move(MissionAttack mission) {
        AFocusPoint focusPoint = mission.focusPoint();

        // Invalid focus point, no enemy can be found, roam around map
//        if (focusPoint == null) {
//            if ((!unit.isAttackingOrMovingToAttack() || unit.looksIdle())) {
//                return WeDontKnowWhereEnemyIs.update(mission, unit);
//            }
//            return true;
//        }

        if (shouldSkip()) return true;

        if (advance(unit, focusPoint)) {
//            unit.setTooltipTactical("#MA:Advance" + AAttackEnemyUnit.canAttackEnemiesNowString());
            return true;
        }

        return WeDontKnowWhereEnemyIs.update(mission, unit);
    }

    private boolean shouldSkip() {
        if (unit.lastPositioningActionLessThanAgo(13)) return true;

//        if (unit.lastPositioningActionMoreThanAgo(30)) {
//        }

        if (ASquadCohesionManager.update() != null) {
            return true;
        }

        return false;
    }

    private boolean advance(AFocusPoint focusPoint) {
        if (unit.squad().isLeader()) {
            return AdvanceAsLeader.advanceAsLeader(unit, focusPoint);
        }

        if (
            MoveToAttackAsTerran.handledTerranAdvance()
            || tooLonely(unit, focusPoint)
        ) {
            return true;
        }

        return false;
    }

    private boolean tooLonely(AFocusPoint focusPoint) {
        AUnit leader = unit.squad().leader();
        if (leader == null) {
            return false;
        }

        if (unit.distTo(leader) > 5 && unit.friendsInRadiusCount(3) <= 8) {
            unit.move(leader, Actions.MOVE_FORMATION, "Coordinate");
            return true;
        }

        return false;
    }
}

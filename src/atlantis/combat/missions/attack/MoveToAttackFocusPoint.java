package atlantis.combat.missions.attack;

import atlantis.combat.missions.WeDontKnowWhereEnemyIs;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.units.AUnit;

public class MoveToAttackFocusPoint {

    public static boolean move(AUnit unit, MissionAttack mission) {
        AFocusPoint focusPoint = mission.focusPoint();

        // Invalid focus point, no enemy can be found, roam around map
//        if (focusPoint == null) {
//            if ((!unit.isAttackingOrMovingToAttack() || unit.looksIdle())) {
//                return WeDontKnowWhereEnemyIs.update(mission, unit);
//            }
//            return true;
//        }

        if (shouldSkip(unit)) return true;

        if (advance(unit, focusPoint)) {
//            unit.setTooltipTactical("#MA:Advance" + AAttackEnemyUnit.canAttackEnemiesNowString(unit));
            return true;
        }

        return WeDontKnowWhereEnemyIs.update(mission, unit);
    }

    private static boolean shouldSkip(AUnit unit) {
        if (unit.lastPositioningActionLessThanAgo(13)) return true;

//        if (unit.lastPositioningActionMoreThanAgo(30)) {
//        }

        if (ASquadCohesionManager.update(unit)) {
            return true;
        }

        return false;
    }

    private static boolean advance(AUnit unit, AFocusPoint focusPoint) {
        if (
            MoveToAttackAsTerran.handledTerranAdvance(unit)
            || tooLonely(unit, focusPoint)
        ) {
            return true;
        }

        return false;
    }

    private static boolean tooLonely(AUnit unit, AFocusPoint focusPoint) {
        AUnit centerUnit = unit.squad().centerUnit();
        if (centerUnit == null) {
            return false;
        }

        return unit.distTo(centerUnit) >= 5 && unit.friendsInRadiusCount(3) <= 8;
    }
}

package atlantis.combat.missions;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class WeDontKnowWhereEnemyIs {

    public static boolean update(Mission mission, AUnit unit) {
//        if (unit.isMoving() && unit.enemiesNear().empty()) {
        if (unit.isMoving() && unit.lastActionLessThanAgo(30 * 2)) {
            return false;
        }

//        if (!unit.isIdle()) {
//            return false;
//        }

        if (handleNearEnemy(mission, unit)) {
            return true;
        }

        if (handleAnyEnemy(mission, unit)) {
            return true;
        }

        // Go to random UNEXPLORED
        if ((A.isUms() || A.chance(10)) && (mission.temporaryTarget() == null || mission.temporaryTarget().isExplored())) {
            mission.setTemporaryTarget(AMap.randomUnexploredPosition(unit.position()));
//            if (temporaryTarget != null) {
//            System.out.println("Go to unexplored " + temporaryTarget);
//            }
        }

        // Go to random INVISIBLE
        if (mission.temporaryTarget() == null || mission.temporaryTarget().isPositionVisible()) {
            mission.setTemporaryTarget(AMap.randomInvisiblePosition());
//            if (temporaryTarget != null) {
//            System.out.println("Go to invisible " + temporaryTarget);
//            }
        }

        if (
            mission.temporaryTarget() != null
                && unit.move(mission.temporaryTarget(), Actions.MOVE_ENGAGE, "#FindEnemy", true)
        ) {
//            APainter.paintLine(unit.position(), temporaryTarget, Color.Yellow);
            return true;
        }
        else {
//            if (!AGame.isUms()) {
//                System.err.println("No invisible position found");
//            }
            return false;
        }
    }

    private static boolean handleNearEnemy(Mission mission, AUnit unit) {
        AUnit nearestEnemy = unit.enemiesNear().canBeAttackedBy(unit, 20).nearestTo(unit);

        if (nearestEnemy != null) {
            mission.setTemporaryTarget(nearestEnemy.position());
            unit.setTooltip("FindEnemy&Attack");
            if (mission.temporaryTarget() != null) {
                if (nearestEnemy.u() != null && unit.attackUnit(nearestEnemy)) {
                    unit.setTooltip("TempEnemy", true);
                    return true;
                }
                else if (unit.move(mission.temporaryTarget(), Actions.MOVE_EXPLORE, "ExploreNow", false)) return true;
            }
        }

        return false;
    }

    private static boolean handleAnyEnemy(Mission mission, AUnit unit) {
        AUnit foggedEnemy = EnemyUnits.discovered().groundUnits().nearestTo(unit);

        if (foggedEnemy != null) {
            mission.setTemporaryTarget(foggedEnemy.position());
            unit.setTooltip("FindFogged");
            if (mission.temporaryTarget() != null) {
                if (foggedEnemy.u() != null && foggedEnemy.effVisible() && unit.attackUnit(foggedEnemy)) {
                    unit.setTooltip("AnyEnemy", true);
                    return true;
                }
                else if (unit.move(
                    mission.temporaryTarget(), Actions.MOVE_EXPLORE, "FindFogged", false
                )) return true;
            }
        }

        return false;
    }
}

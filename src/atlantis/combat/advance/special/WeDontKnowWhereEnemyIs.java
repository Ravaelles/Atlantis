package atlantis.combat.advance.special;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class WeDontKnowWhereEnemyIs extends MissionManager {
    public WeDontKnowWhereEnemyIs(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !EnemyInfo.hasDiscoveredAnyBuilding();
    }

    protected Manager handle(AUnit unit) {
        if (!unit.looksIdle() && unit.isActiveManager(this)) {
            return continueUsingLastManager();
        }

        if (shouldGoToRandomUnexplored()) {
            return goToRandomUnexplored();
        }

        return goToRandomInvisible();
    }

    private Manager goToRandomUnexplored() {
        APosition unexploredPosition = AMap.randomUnexploredPosition(unit);
        if (unexploredPosition != null) {
            unit.move(unexploredPosition, Actions.MOVE_EXPLORE, "GoToUnexpl", true);
            return usedManager(this);
        }

        return null;
    }

    private Manager goToRandomInvisible() {
        APosition invisiblePosition = AMap.randomInvisiblePosition(unit);
        if (invisiblePosition != null) {
            unit.move(invisiblePosition, Actions.MOVE_EXPLORE, "GoToRand", true);
            return usedManager(this);
        }

        unit.setTooltip("DontKnowWhatToDo");
        return null;
    }

    private boolean shouldGoToRandomUnexplored() {
        return !EnemyInfo.weKnowAboutAnyRealUnit() && (focusPoint == null || focusPoint.isPositionVisible());
    }

//    private static boolean handleNearEnemy(Mission mission, AUnit unit) {
//        AUnit nearestEnemy = unit.enemiesNear().canBeAttackedBy(unit, 20).nearestTo(unit);
//
//        if (nearestEnemy != null) {
//            mission.setTemporaryTarget(nearestEnemy.position());
//            unit.setTooltip("FindEnemy&Attack");
//            if (mission.temporaryTarget() != null) {
//                if (nearestEnemy.u() != null && unit.attackUnit(nearestEnemy)) {
//                    unit.setTooltip("TempEnemy", true);
//                    return true;
//                }
//                else if (unit.move(mission.temporaryTarget(), Actions.MOVE_EXPLORE, "ExploreNow", false)) return true;
//            }
//        }
//
//        return false;
//    }
//
//    private static boolean handleAnyEnemy(Mission mission, AUnit unit) {
//        AUnit foggedEnemy = EnemyUnits.discovered().groundUnits().nearestTo(unit);
//
//        if (foggedEnemy != null) {
//            mission.setTemporaryTarget(foggedEnemy.position());
//            unit.setTooltip("FindFogged");
//            if (mission.temporaryTarget() != null) {
//                if (foggedEnemy.u() != null && foggedEnemy.effVisible() && unit.attackUnit(foggedEnemy)) {
//                    unit.setTooltip("AnyEnemy", true);
//                    return true;
//                }
//                else if (unit.move(
//                    mission.temporaryTarget(), Actions.MOVE_EXPLORE, "FindFogged", false
//                )) return true;
//            }
//        }
//
//        return false;
//    }
}

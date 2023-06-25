package atlantis.combat.missions.attack;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.missions.WeDontKnowEnemyEnemyUnit;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import bwapi.Color;

public class MissionAttackAdvance {

    public static boolean advance(AUnit unit, MissionAttack mission) {
        AFocusPoint focusPoint = mission.focusPoint();

        // Invalid focus point, no enemy can be found, roam around map
//        if (focusPoint == null && (!unit.isAttackingOrMovingToAttack() || unit.isIdle())) {
//            return handleWeDontKnowWhereTheEnemyBaseIs(unit);
//        }

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        // Focus point is well known
//        if (focusPoint != null && unit.lastPositioningActionMoreThanAgo(40)) {
        if (focusPoint != null) {
            if (
                unit.lastPositioningActionMoreThanAgo(40)
                || unit.isIdle()
                || !unit.isMoving()
                || !unit.isAttacking()
            ) {
                if (AdvanceUnitsManager.attackMoveToFocusPoint(unit, focusPoint)) {
                    unit.setTooltipTactical("#MA:Advance" + AAttackEnemyUnit.canAttackEnemiesNowString(unit));
                    return true;
                }
            }

            unit.setTooltip("Advancing...");
            return false;
        }
        else {
            unit.setTooltipTactical("#MA-NoFocus");
            return WeDontKnowEnemyEnemyUnit.handleWeDontKnowWhereToFindEnemy(mission, unit);
        }
    }
}

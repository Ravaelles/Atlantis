package atlantis.terran;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class TerranFlyingBuildingScoutManager extends Manager {
    public TerranFlyingBuildingScoutManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isABuilding() && unit.isLifted();
    }

    public Manager handle() {
        if (updateFlyingBuilding()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean updateFlyingBuilding() {
        APainter.paintCircle(unit, new int[]{7, 10, 13, 16}, Color.Grey);

        if (avoidCombatBuildings()) return unit.moveAwayFrom(unit, 3, "BloodyBuilding", Actions.MOVE_SAFETY);

        if (underAttack()) return unit.moveAwayFrom(unit, 3, "UnderFire", Actions.MOVE_SAFETY);

        if (goToFocusPoint()) return true;

        return false;
    }

    private boolean goToFocusPoint() {
        APosition focusPoint = flyingBuildingFocusPoint();

        // Move towards focus point if needed
        if (focusPoint != null) {
            double distToFocusPoint = focusPoint.distTo(unit);

            if (distToFocusPoint > 0.5) {
                unit.moveStrategic(focusPoint, Actions.MOVE_SPECIAL, "Fly baby!");
                return true;
            }
        }
        return false;
    }

    private boolean underAttack() {
        if (unit.lastUnderAttackLessThanAgo(30 * 3)) {
//            APosition median = Alpha.get().median();
//            if (median != null) {
//                unit.moveStrategic(median, Actions.MOVE_SAFETY, "UnderFire");
//                return true;
//            }
            AUnit enemy = unit.enemiesNear().canAttack(unit, 3).nearestTo(unit);
            if (enemy != null) {
                return true;
            }
        }
        return false;
    }

    private boolean avoidCombatBuildings() {
        Selection combatBuildings = unit.enemiesNear()
            .combatBuildingsAntiAir()
            .inRadius(7.8, unit);

        if (combatBuildings.notEmpty()) {
            return true;
        }
        return false;
    }

//    private APosition flyingBuildingFocusPoint() {
//        APosition containFocusPoint = Missions.globalMission().focusPoint();
//        APosition attackFocusPoint = Missions.ATTACK.focusPoint();
//
//        if (containFocusPoint != null && attackFocusPoint != null) {
//            return containFocusPoint.translateTilesTowards(attackFocusPoint, 4);
//        }
//
//        return containFocusPoint;
////        return Select.ourTanks().first().position();
//    }

    private APosition flyingBuildingFocusPoint() {
        APosition focusPoint = Alpha.get().median();
        if (focusPoint != null) {
            APosition attackFocusPoint = Missions.ATTACK.focusPoint();

            if (attackFocusPoint != null) {
                focusPoint = focusPoint.translateTilesTowards(8, attackFocusPoint);
            }

            return focusPoint;
        }

        return null;
    }
}


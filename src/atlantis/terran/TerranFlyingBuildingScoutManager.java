package atlantis.terran;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

import java.util.ArrayList;
import java.util.Iterator;

public class TerranFlyingBuildingScoutManager {

    private static final ArrayList<AUnit> flyingBuildings = new ArrayList<>();

    // =========================================================

    public static void update() {
        if (AGame.isUms()) {
            return;
        }

        if (needNewFlyingBuilding()) {
            liftABuildingAndFlyAmongStars();
        }

        updateIfBuildingNeedsToBeLifted();

        for (Iterator<AUnit> it = flyingBuildings.iterator(); it.hasNext(); ) {
            if (updateFlyingBuilding(it.next())) {
                return;
            }
        }
    }

    // =========================================================

    private static void updateIfBuildingNeedsToBeLifted() {
        for (AUnit building : Select.ourBuildings().list()) {
            if (!building.u().canLift()) {
                continue;
            }

            if (!building.isLifted() && building.isUnderAttack(20) && building.hpPercent() <= 36) {
                building.lift();
                return;
            }

            if (building.isLifted() && building.lastUnderAttackLessThanAgo(30 * 4)) {
                APosition median = Alpha.get().median();
                if (median != null) {
                    building.moveStrategic(median, Actions.MOVE_ENGAGE, "AwayFromAlpha");
                    return;
                }
            }
        }
    }

    private static boolean updateFlyingBuilding(AUnit flyingBuilding) {
        if (!flyingBuilding.isAlive()) {
            flyingBuildings.remove(flyingBuilding);
            return true;
        }

        APainter.paintCircle(flyingBuilding, new int[] { 7, 10, 13, 16 }, Color.Grey);

        Selection combatBuildings = flyingBuilding.enemiesNear()
            .combatBuildingsAntiAir()
            .inRadius(7.8, flyingBuilding);
        if (combatBuildings.notEmpty()) {
            return flyingBuilding.moveAwayFrom(flyingBuilding, 3, "BloodyBuilding", Actions.MOVE_SAFETY);
        }

        if (flyingBuilding.lastUnderAttackLessThanAgo(30 * 3)) {
//            APosition median = Alpha.get().median();
//            if (median != null) {
//                flyingBuilding.moveStrategic(median, Actions.MOVE_SAFETY, "UnderFire");
//                return true;
//            }
            AUnit enemy = flyingBuilding.enemiesNear().canAttack(flyingBuilding, 3).nearestTo(flyingBuilding);
            if (enemy != null) {
                return flyingBuilding.moveAwayFrom(flyingBuilding, 3, "UnderFire", Actions.MOVE_SAFETY);
            }
        }

        APosition focusPoint = flyingBuildingFocusPoint();

        // Move towards focus point if needed
        if (focusPoint != null) {
            double distToFocusPoint = focusPoint.distTo(flyingBuilding);

            if (distToFocusPoint > 2) {
                flyingBuilding.moveStrategic(focusPoint, Actions.MOVE_SPECIAL, "Fly baby!");
                return true;
            }
        }

        return false;
    }

    private static APosition flyingBuildingFocusPoint() {
        APosition containFocusPoint = Missions.globalMission().focusPoint();
        APosition attackFocusPoint = Missions.ATTACK.focusPoint();

        if (containFocusPoint != null && attackFocusPoint != null) {
            return containFocusPoint.translateTilesTowards(attackFocusPoint, 6);
        }

        return containFocusPoint;
//        return Select.ourTanks().first().position();
    }

    // =========================================================

    private static boolean needNewFlyingBuilding() {
        if (!flyingBuildings.isEmpty()) {
            return false;
        }

        return Select.ourWithUnfinished(AUnitType.Terran_Machine_Shop).atLeast(1)
                || Select.ourTanks().atLeast(3);
    }

    private static void liftABuildingAndFlyAmongStars() {
        AUnit flying = Select.ourOfType(AUnitType.Terran_Barracks).free().first();
        if (flying != null) {
            flying.lift();
            flyingBuildings.add(flying);
        }
    }

    // =========================================================

    public static boolean isFlyingBuilding(AUnit unit) {
        return unit.type().isBuilding() && flyingBuildings.contains(unit);
    }

}

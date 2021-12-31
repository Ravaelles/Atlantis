package atlantis.combat.micro.terran;

import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.Color;
import bwapi.TechType;


public class TerranVulture {

    public static boolean update(AUnit unit) {
        return handlePlantMines(unit);
    }
    
    // =========================================================

    private static boolean handlePlantMines(AUnit unit) {

        // Unit gets status "stuck" after mine has been planted, being the only way I know of to
        // define that a mine planting has been finished.
//        if (unit.isUnitAction(UnitActions.USING_TECH) && (unit.isStuck() || unit.isIdle() || !unit.isMoving())) {
        if (unit.isUnitAction(UnitActions.USING_TECH) && unit.lastOrderFramesAgo() > 15) {
            unit.setUnitAction(UnitActions.STOP);
            unit.setTooltip("Planted!");
            return false;
        }

        // Can't allow to interrupt
        if (unit.isUnitAction(UnitActions.USING_TECH) && unit.lastActionLessThanAgo(15, UnitActions.USING_TECH)) {
            unit.setTooltip("Planting mine");
            return true;
        }

        // === Use mines section ======================================================

        // If out of mines or mines ain't researched, don't do anything.
        if (unit.minesCount() <= 0 || !ATech.isResearched(TechType.Spider_Mines)) {
            return false;
        }

//        if (fightEnemyUsingMinesNextToThem(unit)) {
//            return true;
//        }

        // Disallow mines close to buildings
        AUnit nearestBuilding = Select.ourBuildings().nearestTo(unit);
        if (nearestBuilding != null && nearestBuilding.distTo(unit) <= 7) {
            unit.setTooltip("Don't mine");
            return false;
        }
        
        // If enemies are too close don't do it
//        if (Select.enemyRealUnits().inRadius(6, unit).count() > 0) {
//            return false;
//        }
        
        // If too many our units around, don't mine
        if (Select.ourCombatUnits().inRadius(7, unit).count() >= 4) {
            return false;
        }

        // Place mines in standard positions
        if (plantStandardMine(unit)) {
            return true;
        }
        
        return false;
    }

    // =========================================================

    private static boolean fightEnemyUsingMinesNextToThem(AUnit unit) {
//        if (unit.hp() <= 45 && !unit.isUnitAction(UnitActions.USING_TECH)) {
//            return false;
//        }

        // First define closest enemy
        AUnit nearestEnemy = unit.enemiesNearby().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Protoss_Dragoon
        ).inRadius(8, unit).nearestTo(unit);

        if (nearestEnemy != null) {
            APainter.paintTextCentered(unit.translateByTiles(0, -1.2), A.trueFalse(unit.isBraking()) + "," + A.trueFalse(unit.isIdle()), Color.Orange);

            // Define center of other enemy units nearby
            HasPosition enemiesCenter = nearestEnemy
                    .friendsNearby()
                    .groundUnits()
                    .inRadius(2, nearestEnemy)
                    .center()
                    .makeWalkable();

            APosition finalPlace = enemiesCenter.position();

            APainter.paintCircleFilled(finalPlace, 16, Color.White);
            System.out.println("unit.distTo(enemiesCenter) = " + unit.distTo(finalPlace));

            if (unit.distToLessThan(enemiesCenter, 1.5)) {
                finalPlace = unit.position();
            }
            else if (unit.distToLessThan(enemiesCenter, 2.8) && (unit.isBraking() || unit.isIdle() || unit.hasNotMovedInAWhile())) {
                finalPlace = unit.position();
            }
            else {
//                unit.move(finalPlace.translatePercentTowards(10, unit), UnitActions.MOVE_TO_ENGAGE, "GoAndMine");
//                return true;
            }

//            plantMineAt(unit, enemiesCenter.translateTilesTowards(unit, 0.7));

            APainter.paintCircleFilled(enemiesCenter, 24, Color.Yellow);
            if (finalPlace != null) {
                APainter.paintCircleFilled(enemiesCenter, 24, Color.Red);
                System.out.println("finalPlace = " + finalPlace);
                plantMineAt(unit, finalPlace);
                unit.setTooltip("UseMine");
                return true;
            }
        }

        return false;
    }

    private static boolean plantStandardMine(AUnit unit) {
        Selection nearbyMines = Select.ourOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(8, unit);
        if ((nearbyMines.count() <= 3 || (unit.minesCount() >= 3 && nearbyMines.count() <= 4))
                && nearbyMines.inRadius(2, unit).atMost(1)) {
            plantMineAt(unit, unit.position());
            unit.setTooltip("Plant mine");
            return true;
        }

        return false;
    }

    private static void plantMineAt(AUnit unit, APosition position) {
        unit.useTech(TechType.Spider_Mines, position);
        unit.setUnitAction(UnitActions.USING_TECH, TechType.Spider_Mines, position);
    }

}

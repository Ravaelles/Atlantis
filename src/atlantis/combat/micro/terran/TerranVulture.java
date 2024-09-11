package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;
import bwapi.TechType;


public class TerranVulture extends Manager {
    public TerranVulture(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isVulture();
    }

    public boolean update() {
        return handlePlantMines();
    }

    // =========================================================

    private boolean handlePlantMines() {

        // Unit gets status "stuck" after mine has been planted, being the only way I know of to
        // define that a mine planting has been finished.
//        if (unit.isUnitAction(UnitActions.USING_TECH) && (unit.isStuck() || unit.isIdle() || !unit.isMoving())) {
        if (unit.isAction(Actions.USING_TECH) && unit.lastActionFramesAgo() > 15) {
            unit.setAction(Actions.STOP);
            unit.setTooltipTactical("Planted!");
            return false;
        }

        // Can't allow to interrupt
        if (unit.isAction(Actions.USING_TECH) && unit.lastActionLessThanAgo(15, Actions.USING_TECH)) {
            unit.setTooltipTactical("Planting mine");
            return true;
        }

        // === Use mines section ======================================================

        // If out of mines or mines ain't researched, don't do anything.
        if (unit.minesCount() <= 0 || !ATech.isResearched(TechType.Spider_Mines)) return false;

//        if (fightEnemyUsingMinesNextToThem()) {
//            return true;
//        }

        // Disallow mines close to buildings
        AUnit nearestBuilding = Select.ourBuildings().nearestTo(unit);
        if (nearestBuilding != null && nearestBuilding.distTo(unit) <= 7) {
            unit.setTooltipTactical("Don't mine");
            return false;
        }

        // If enemies are too close don't do it
//        if (Select.enemyRealUnits().inRadius(6, unit).count() > 0) {
//            return false;
//        }

        // If too many our units around, don't mine
        if (Select.ourCombatUnits().inRadius(7, unit).count() >= 4) return false;

        // Place mines in standard positions
        if (plantStandardMine()) return true;

        return false;
    }

    // =========================================================

//    private boolean fightEnemyUsingMinesNextToThem() {
////        if (unit.hp() <= 45 && !unit.isUnitAction(UnitActions.USING_TECH)) {
////            return false;
////        }
//
//        // First define closest enemy
//        AUnit nearestEnemy = unit.enemiesNear().ofType(
//            AUnitType.Terran_Siege_Tank_Siege_Mode,
//            AUnitType.Terran_Siege_Tank_Tank_Mode,
//            AUnitType.Protoss_Dragoon
//        ).inRadius(8, unit).nearestTo(unit);
//
//        if (nearestEnemy != null) {
//            APainter.paintTextCentered(unit.translateByTiles(0, -1.2), A.trueFalse(unit.isBraking()) + "," + A.trueFalse(unit.isIdle()), Color.Orange);
//
//            // Define center of other enemy units Near
//            HasPosition enemiesCenter = nearestEnemy
//                .friendsNear()
//                .groundUnits()
//                .inRadius(2, nearestEnemy)
//                .center()
//                .makeWalkable(5);
//
//            APosition finalPlace = enemiesCenter.position();
//
//            APainter.paintCircleFilled(finalPlace, 16, Color.White);
//            System.out.println("unit.distTo(enemiesCenter) = " + unit.distTo(finalPlace));
//
//            if (unit.distToLessThan(enemiesCenter, 1.5)) {
//                finalPlace = unit.position();
//            }
//            else if (unit.distToLessThan(enemiesCenter, 2.8) && (unit.isBraking() || unit.isIdle() || unit.hasNotMovedInAWhile())) {
//                finalPlace = unit.position();
//            }
//            else {
////                unit.move(finalPlace.translatePercentTowards(10, unit), UnitActions.MOVE_TO_ENGAGE, "GoAndMine");
////                return true;
//            }
//
////            plantMineAt(unit, enemiesCenter.translateTilesTowards(unit, 0.7));
//
//            APainter.paintCircleFilled(enemiesCenter, 24, Color.Yellow);
//            if (finalPlace != null) {
//                APainter.paintCircleFilled(enemiesCenter, 24, Color.Red);
//                System.out.println("finalPlace = " + finalPlace);
//                plantMineAt(finalPlace);
//                unit.setTooltipTactical("UseMine");
//                return true;
//            }
//        }
//
//        return false;
//    }

    private boolean plantStandardMine() {
        Selection NearMines = Select.ourOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(8, unit);
        if ((NearMines.count() <= 3 || (unit.minesCount() >= 3 && NearMines.count() <= 4))
            && NearMines.inRadius(2, unit).atMost(1)) {
            plantMineAt(unit.position());
            unit.setTooltipTactical("Plant mine");
            return true;
        }

        return false;
    }

    private void plantMineAt(APosition position) {
        unit.useTech(TechType.Spider_Mines, position);
        unit.setAction(Actions.USING_TECH, TechType.Spider_Mines, position);
    }

}

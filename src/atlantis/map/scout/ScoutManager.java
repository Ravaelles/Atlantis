package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.ARegion;
import atlantis.map.ARegionBoundary;
import atlantis.map.Bases;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ScoutManager extends Manager {
//    public boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = true;
    public static boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = false;
    public static Positions<ARegionBoundary> scoutingAroundBasePoints = new Positions<>();

    private static int unitingAroundBaseNextPolygonIndex = -1;
    private static HasPosition unitingAroundBaseLastPolygonPoint = null;
    private static boolean unitingAroundBaseWasInterrupted = false;
    private static boolean unitingAroundBaseDirectionClockwise = true;
    private static APosition nextPositionTounit = null;
    private static ARegion enemyBaseRegion = null;

    public ScoutManager(AUnit unit) {
        super(unit);
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.isScout();
    }

    @Override
    public Manager handle() {
        unit.setTooltipTactical("unit...");

//        if (AvoidCriticalUnits.update()) {
//            unit.setTooltip("Daaaamn!");
//            return;
//        }
//
//        if (AvoidCombatBuildings.handle()) {
//            unit.setTooltip("Eh!");
//            return;
//        }
//
//        if (AvoidEnemies.avoidEnemiesIfNeeded()) {
//            return;
//        }

        if (unit.isRunning()) {
            nextPositionTounit = null;
            unitingAroundBaseWasInterrupted = true;
            if (A.seconds() >= 300) {
                handleScoutFreeBases();
            }
        }

        // =========================================================

        // We don't know any enemy building, unit nearest starting location.
        if (!EnemyInfo.hasDiscoveredAnyBuilding()) {
            tryFindingEnemy();
        }

        // unit other bases
        else if (!ScoutCommander.anyScoutBeenKilled) {
            if (roamAroundEnemyBase()) return usedManager(this);
        }

        // Map roaming
        else {
            if (handleScoutFreeBases()) return usedManager(this);
        }

        return null;
    }

    // =========================================================

    private boolean handleScoutFreeBases() {
        if (nextPositionTounit != null && !nextPositionTounit.isPositionVisible()) {
            return unit.move(nextPositionTounit, Actions.MOVE_SCOUT, "unitBases" + A.now(), true);
        }

        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        APosition position = enemyBuilding != null ? enemyBuilding.position() : unit.position();
        nextPositionTounit = Bases.nearestUnexploredStartingLocation(position);
        if (nextPositionTounit != null) {
            return false;
        }

        nextPositionTounit = Bases.randomInvisibleStartingLocation();
        return false;
    }

    /**
     * We don't know any enemy building, unit nearest starting location.
     */
    public boolean tryFindingEnemy() {
        if (unit == null) {
            return true;
        }
        unit.setTooltipTactical("Find enemy");

        // Define center point for our searches
        AUnit ourMainBase = Select.main();
        if (ourMainBase == null && A.notUms()) {
            return false;
        }

        // =========================================================
        // Get nearest unexplored starting location and go there

        HasPosition startingLocation;
        if (unit.is(AUnitType.Zerg_Overlord) || ScoutCommander.allScouts().size() > 1) {
            startingLocation = Bases.startingLocationBasedOnIndex(
                    unit.getUnitIndexInBwapi()// UnitUtil.getUnitIndex()
            );
        } else {
            startingLocation = Bases.nearestUnexploredStartingLocation(unit.position());
        }

        // =========================================================

        if (
            startingLocation != null
            && unit.move(startingLocation, Actions.MOVE_EXPLORE, "Explore", true)
        ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Roam around enemy base to get information about build order for as long as possible.
     */
    public boolean roamAroundEnemyBase() {
        unitingAroundBaseWasInterrupted = false;

        // === Remain at the enemy base if it's known ==============

        AUnit enemyBase = EnemyUnits.enemyBase();
        APosition enemy = null;

        if (enemyBase != null) {
            enemy = enemyBase.position();
        }

        if (enemy == null) {
            AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
            if (enemyBuilding != null) {
                enemy = enemyBuilding.position();
            }
        }
//        APosition enemyBase = Select.main().position();

        if (enemy != null) {
            enemyBaseRegion = enemy.region();

            if (scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints();
            }

            defineNextPolygonPointForEnemyBaseRoamingUnit();
            if (
                unitingAroundBaseLastPolygonPoint != null
                && unit.move(unitingAroundBaseLastPolygonPoint, Actions.MOVE_EXPLORE, "RoamAround", true)
            ) {
                return true;
            } else {
                unit.setTooltipTactical("Can't find polygon point");
            }
        }

        return false;
    }

    // =========================================================

    private void unitForTheNextBase() {
        APosition baseLocation = Bases.nearestUnexploredStartingLocation(unit.position());
        if (
            baseLocation != null
            && unit.move(baseLocation.position(), Actions.MOVE_EXPLORE, "Explore next base", true)
        ) {
            return;
        }
    }

    private void defineNextPolygonPointForEnemyBaseRoamingUnit() {
//        if ((new AvoidEnemies(unit)).handle() != null) {
//            unit.setTooltipTactical("ChangeOfPlans");
//            return;
//        }

        unitingAroundBaseLastPolygonPoint = nextPointAroundEnemyBase();

        if (APainter.paintingMode == APainter.MODE_FULL_PAINTING) {
            APainter.paintLine(
                unitingAroundBaseLastPolygonPoint, unit.position(), Color.Yellow
            );
        }

        if (MAKE_CAMERA_FOLLOW_unit_AROUND_BASE) {
            CameraCommander.centerCameraOn(unit.position());
        }
    }

    private HasPosition nextPointAroundEnemyBase() {

        // Change roaming direction if we were forced to run from enemy units
        if (unitingAroundBaseWasInterrupted) {
            unitingAroundBaseDirectionClockwise = !unitingAroundBaseDirectionClockwise;
        }

        // Define direction
        HasPosition goTo = null;
        int step = 1;
        do {
            int deltaIndex = unitingAroundBaseDirectionClockwise ? step : -step;

            goTo = unitingAroundBaseLastPolygonPoint != null ? APosition.create(unitingAroundBaseLastPolygonPoint) : null;

            if (goTo == null || unitingAroundBaseWasInterrupted) {
                goTo = useNearestBoundaryPoint(enemyBaseRegion);
            } else {
                if (unit.distTo(goTo) <= 1.8) {
                    unitingAroundBaseNextPolygonIndex = (unitingAroundBaseNextPolygonIndex + deltaIndex)
                        % scoutingAroundBasePoints.size();
                    if (unitingAroundBaseNextPolygonIndex < 0) {
                        unitingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.size() - 1;
                    }

                    goTo = scoutingAroundBasePoints.get(unitingAroundBaseNextPolygonIndex);

                    if (Select.all().inRadius(0.5, goTo).empty()) {
                        break;
                    }
                }
            }

            step++;
        } while (step <= 15);

        unitingAroundBaseWasInterrupted = false;
        return unitingAroundBaseLastPolygonPoint = goTo;
    }

    private void initializeEnemyRegionPolygonPoints() {
        APosition center = (new Positions(enemyBaseRegion.boundaries())).average();

        for (ARegionBoundary position : enemyBaseRegion.boundaries()) {
            double groundDistance = center.distTo(position);

            if (
                    position.isWalkable()
                    && groundDistance >= 2
                    && groundDistance <= 17
            ) {
                scoutingAroundBasePoints.addPosition(position);
            }
        }

//        if (MAKE_CAMERA_FOLLOW_unit_AROUND_BASE) {
//            AGame.changeSpeedTo(1);
//            AGame.changeSpeedTo(1);
//            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
//        }
    }

    private ARegionBoundary useNearestBoundaryPoint(ARegion region) {
        ARegionBoundary nearest = scoutingAroundBasePoints.nearestTo(unit.position());
        unitingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.getLastIndex();
        return nearest;
    }

//    public APosition getUmsFocusPoint(APosition startPosition) {
//        ARegion nearestUnexploredRegion = Regions.getNearestUnexploredRegion(startPosition);
//        return nearestUnexploredRegion != null ? APosition.create(nearestUnexploredRegion.center()) : null;
//    }

    // =========================================================

    public boolean testRoamingAroundBase(AUnit worker) {
        Selection workers = Select.ourWorkers();
        AUnit horse = workers.last();
        if (horse.equals(worker)) {
            roamAroundEnemyBase();
            worker.setTooltipTactical("Patataj");
            return true;
        }
        else {
            worker.setTooltipTactical("Dajesz kurwa!");
            if (A.now() % 50 >= 25) {
                if (worker.move(horse, Actions.MOVE_SPECIAL, "", true)) {
                    worker.setTooltipTactical("Ciśniesz!");
                    return true;
                }
            }
        }

        return true;
    }
}

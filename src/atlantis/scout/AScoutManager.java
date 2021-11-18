package atlantis.scout;

import atlantis.AGame;
import atlantis.CameraManager;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.*;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.position.Positions;
import atlantis.production.orders.BuildOrderSettings;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Selection;
import atlantis.util.A;
import atlantis.util.We;
import bwapi.Color;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class AScoutManager {

//    public static boolean MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE = true;
    public static boolean MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE = false;
    
    // =========================================================
    
    /**
     * Current scout unit.
     */
    private static final ArrayList<AUnit> scouts = new ArrayList<>();

    private static boolean anyScoutBeenKilled = false;
    
    public static Positions<ARegionBoundary> scoutingAroundBasePoints = new Positions<>();
    private static int scoutingAroundBaseNextPolygonIndex = -1;
    private static HasPosition scoutingAroundBaseLastPolygonPoint = null;
    private static boolean scoutingAroundBaseWasInterrupted = false;
    private static boolean scoutingAroundBaseDirectionClockwise = true;
    private static APosition nextPositionToScout = null;

    // =========================================================

    private static boolean update(AUnit scout) {
        scout.setTooltip("Scout...");

        if (AAvoidUnits.avoidEnemiesIfNeeded(scout)) {
            nextPositionToScout = null;
            return handleScoutFreeBases(scout);
        }

        // =========================================================

        // We don't know any enemy building, scout nearest starting location.
        if (!EnemyUnits.hasDiscoveredAnyBuilding()) {
            return tryFindingEnemyBuilding(scout);
        }

        // Roam around enemy base
//        else if (Count.ourCombatUnits() <= 1) {
//            return handleScoutEnemyBase(scout);
//        }

        // Scout other bases
        else {
            return roamAroundEnemyBase(scout);
//            return handleScoutFreeBases(scout);
        }
    }

    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    public static void update() {
//        if (true) return;

        // === Handle UMS ==========================================
        
        if (AGame.isUms()) {
            return;
        }

        // === Act with every scout ================================

        manageScoutAssigned();

        try {
            for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext();) {
                update(iterator.next());
            }
        }
        catch (ConcurrentModificationException ignore) { }
    }

    private static void removeOverlordsAsScouts() {
        if (We.zerg()) {
            if (EnemyUnits.hasDiscoveredAnyBuilding()) {
                scouts.clear();
            }
        }
    }

    // =========================================================

    private static boolean handleScoutFreeBases(AUnit scout) {
        if (nextPositionToScout != null && !nextPositionToScout.isVisible()) {
            return scout.move(nextPositionToScout, UnitActions.SCOUT, "ScoutBases");
        }

        AFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        APosition position = enemyBuilding != null ? enemyBuilding.position() : scout.position();
        nextPositionToScout = Bases.nearestUnexploredStartingLocation(position);
        if (nextPositionToScout != null) {
            return false;
        }

        nextPositionToScout = Bases.randomInvisibleStartingLocation();
        return false;
    }

    /**
     * We don't know any enemy building, scout nearest starting location.
     */
    public static boolean tryFindingEnemyBuilding(AUnit scout) {
        if (scout == null) {
            return true;
        }
        scout.setTooltip("Find enemy");
        //scout.setTooltip("Find enemy");

        // Don't interrupt when moving
//        if (scout.isMoving() || scout.isAttacking()) {
//            return;
//        }
        // Define center point for our searches
        AUnit ourMainBase = Select.main();
        if (ourMainBase == null && A.notUms()) {
            return false;
        }

        // === Handle UMS ==========================================
//        if (AGame.isUms()) {
//            handleUmsExplore(scout);
//            return;
//        }
        // =========================================================
        // Get nearest unexplored starting location and go there

//        APosition startingLocation;
        HasPosition startingLocation;
        if (scout.isType(AUnitType.Zerg_Overlord) || scouts.size() > 1) {
            startingLocation = Bases.startingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = Bases.nearestUnexploredStartingLocation(scout.position());
        }

        // =========================================================
//        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
//        if (enemyBase != null) {
//            Region enemyBaseRegion = AtlantisMap.getRegion(enemyBase);
//            enemyBaseRegion.getPolygon().getCenter()
//        }
        // =========================================================
        if (startingLocation != null) {
            scout.move(startingLocation, UnitActions.EXPLORE, "Explore");
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Roam around enemy base to get information about build order for as long as possible.
     */
    public static boolean roamAroundEnemyBase(AUnit scout) {

        // === Remain at the enemy base if it's known ==============

        APosition enemy = EnemyUnits.enemyBase();
        if (enemy == null) {
            AFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
            if (enemyBuilding != null) {
                enemy = enemyBuilding.position();
            }
        }
//        APosition enemyBase = Select.main().position();

        if (enemy != null) {
            ARegion enemyBaseRegion = enemy.region();

            if (scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints(scout, enemyBaseRegion);
            }

            defineNextPolygonPointForEnemyBaseRoamingUnit(enemyBaseRegion, scout);
            if (scoutingAroundBaseLastPolygonPoint != null) {
                scout.move(scoutingAroundBaseLastPolygonPoint, UnitActions.EXPLORE, "Roam around");
                return true;
            } else {
                scout.setTooltip("Can't find polygon point");
            }
        }

        return false;
    }
    
    // =========================================================

    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private static void manageScoutAssigned() {
        removeDeadScouts();
        removeOverlordsAsScouts();
        removeExcessiveScouts();

        // Build order defines which worker should be a scout
        if (Count.workers() < BuildOrderSettings.scoutIsNthWorker()) {
            return;
        }

        // === Zerg =================================================

        if (We.zerg()) {

//            // We know enemy building
//            if (EnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
//                if (AGame.timeSeconds() < 350) {
//                    if (scouts.isEmpty()) {
//                        for (AUnit worker : Select.ourWorkers().notCarrying().list()) {
//                            if (!worker.isBuilder()) {
//                                scouts.add(worker);
//                                break;
//                            }
//                        }
//                    }
//                }
//            } // Haven't discovered any enemy building
//            else {
//                scouts.clear();
//                scouts.addAll(Select.ourCombatUnits().listUnits());
//            }
        }

        // =========================================================
        // TERRAN + PROTOSS

        else if (scouts.isEmpty()) {
            for (AUnit scout : Select.ourWorkers().notCarrying().sortDataByDistanceTo(Bases.natural(), true)) {
                if (!scout.isBuilder() && !scout.isRepairerOfAnyKind()) {
                    if (scouts.isEmpty()) {
                        System.out.println("Add scout " + scout);
                        scouts.add(scout);
                        return;
                    }
                }
            }
        }
    }

    private static void removeExcessiveScouts() {
        if (scouts.size() > 1) {
            AUnit leaveThisScout = scouts.get(scouts.size() - 1);
            scouts.clear();
            scouts.add(leaveThisScout);
        }
    }

    private static void removeDeadScouts() {
        for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext();) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {
                System.out.println("Remove dead scout " + scout);
                iterator.remove();
                anyScoutBeenKilled = true;
            }
        }
    }

    private static void scoutForTheNextBase(AUnit scout) {
        APosition baseLocation = Bases.nearestUnexploredStartingLocation(scout.position());
        if (baseLocation != null) {
            scout.move(baseLocation.position(), UnitActions.EXPLORE, "Explore next base");
        }
    }

    private static void defineNextPolygonPointForEnemyBaseRoamingUnit(ARegion region, AUnit scout) {
        if (AAvoidUnits.avoidEnemiesIfNeeded(scout)) {
            scout.setTooltip("ChangeOfPlans");
            return;
        }

        // Change roaming direction if we were forced to run from enemy units
        if (scoutingAroundBaseWasInterrupted) {
            scoutingAroundBaseDirectionClockwise = !scoutingAroundBaseDirectionClockwise;
        }
        
        // Define direction
        int deltaIndex = scoutingAroundBaseDirectionClockwise ? 1 : -1;
        
        // =========================================================
        
        HasPosition goTo = scoutingAroundBaseLastPolygonPoint != null
                ? APosition.create(scoutingAroundBaseLastPolygonPoint) : null;

        if (goTo == null || scoutingAroundBaseWasInterrupted) {
            goTo = useNearestBoundaryPoint(region, scout);
        } else {
            if (scout.distTo(goTo) <= 1.8) {
                scoutingAroundBaseNextPolygonIndex = (scoutingAroundBaseNextPolygonIndex + deltaIndex)
                        % scoutingAroundBasePoints.size();
                if (scoutingAroundBaseNextPolygonIndex < 0) {
                    scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.size() - 1;
                }
                
                goTo = scoutingAroundBasePoints.get(scoutingAroundBaseNextPolygonIndex);
            }
        }

//        if (AtlantisMap.getGroundDistance(scout, goTo) < 0.1) {
//            scoutingAroundBaseNextPolygonIndex = (scoutingAroundBaseNextPolygonIndex + 1)
//                        % scoutingAroundBasePoints.size();
//            goTo = (APosition) scoutingAroundBasePoints.get(scoutingAroundBaseNextPolygonIndex);
//        }

        scoutingAroundBaseLastPolygonPoint = goTo;
        scoutingAroundBaseWasInterrupted = false;

        if (APainter.paintingMode == APainter.MODE_FULL_PAINTING) {
            APainter.paintLine(
                    scoutingAroundBaseLastPolygonPoint, scout.position(), Color.Yellow
            );
        }

        if (MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE) {
            CameraManager.centerCameraOn(scout.position());
        }
    }
    
    private static void initializeEnemyRegionPolygonPoints(AUnit scout, ARegion regionToRoam) {
//        Position centerOfRegion = enemyBaseRegion.center();

//        scoutingAroundBasePoints.addPosition(APosition.create(centerOfRegion));
//
//        return;

        for (ARegionBoundary position : regionToRoam.boundaries()) {
//            APosition position = APosition.create(point);

            // Calculate actual ground distance to this position
//            double groundDistance = AMap.getGroundDistance(scout, position);
            double groundDistance = scout.groundDistance(position);

            // Fix problem with some points being unwalkable despite isWalkable being true
//            if (groundDistance < 2) {
//                continue;
//            }
//            position = PositionOperationsWrapper.getPositionMovedPercentTowards(position, centerOfRegion, 3.5);

            // If positions is walkable, not in different region and has path to it, it should be ok
//            if (position.isWalkable() && regionToRoam.getPolygon().isInside(position)
            if (
                    position.isWalkable()
//                    && scout.hasPathTo(position) && groundDistance >= 4
                    && groundDistance >= 2
//                    && groundDistance <= 1.7 * scout.distanceTo(position)
            ) {
                scoutingAroundBasePoints.addPosition(position);
            }
        }

        if (MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE) {
//            AGame.changeSpeedTo(1);
//            AGame.changeSpeedTo(1);
//            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
        }
    }

    private static ARegionBoundary useNearestBoundaryPoint(ARegion region, AUnit scout) {
        ARegionBoundary nearest = scoutingAroundBasePoints.nearestTo(scout.position());
        scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.getLastIndex();
        return nearest;
    }

    public static APosition getUmsFocusPoint(APosition startPosition) {
        ARegion nearestUnexploredRegion = Regions.getNearestUnexploredRegion(startPosition);
        return nearestUnexploredRegion != null ? APosition.create(nearestUnexploredRegion.center()) : null;
    }

    // =========================================================
    /**
     * Returns true if given unit has been assigned to explore the map.
     */
    public static boolean isScout(AUnit unit) {
        return scouts.contains(unit);
    }

    public static boolean hasAnyScoutBeenKilled() {
        return anyScoutBeenKilled;
    }

    public static ArrayList<AUnit> getScouts() {
        return (ArrayList<AUnit>) scouts.clone();
    }

    public static AUnit firstScout() {
        return A.firstElement(scouts);
    }

    // =========================================================

    public static boolean testRoamingAroundBase(AUnit worker) {
        Selection workers = Select.ourWorkers();
        AUnit horse = workers.last();
        if (horse.equals(worker)) {
            AScoutManager.roamAroundEnemyBase(worker);
            worker.setTooltip("Patataj");
            return true;
        }
        else {
            worker.setTooltip("Dajesz kurwa!");
            if (A.now() % 50 >= 25) {
                worker.move(horse, UnitActions.MOVE, "");
                worker.setTooltip("Ciśniesz!");
            }
        }

        return true;
    }
}

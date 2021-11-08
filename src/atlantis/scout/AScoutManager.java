package atlantis.scout;

import atlantis.AGame;
import atlantis.CameraManager;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
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
import atlantis.util.A;
import bwapi.Color;
import bwapi.Position;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class AScoutManager {

//    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = true;
    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = false;
    
    // =========================================================
    
    /**
     * Current scout unit.
     */
    private static final ArrayList<AUnit> scouts = new ArrayList<>();

    private static boolean anyScoutBeenKilled = false;
    
    public static Positions scoutingAroundBasePoints = new Positions();
    private static int scoutingAroundBaseNextPolygonIndex = -1;
    private static APosition scoutingAroundBaseLastPolygonPoint = null;
    private static boolean scoutingAroundBaseWasInterrupted = false;
    private static boolean scoutingAroundBaseDirectionClockwise = true;

    // =========================================================
    
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    public static void update() {

        // === Handle UMS ==========================================
        
        if (AGame.isUms()) {
            return;
        }

        // === Act with every scout ================================
        
        assignScoutIfNeeded();

        try {
            for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext();) {
                update(iterator.next());
            }
        }
        catch (ConcurrentModificationException ignore) { }
    }
    
    private static boolean update(AUnit scout) {
//        if (!scout.isAlive() || AEnemyUnits.enemyBase() != null) {
        if (!scout.isAlive()) {
            scouts.remove(scout);
            return true;
        }
        
        // === Avoid military buildings ============================

        if (AAvoidUnits.avoidEnemiesIfNeeded(scout)) {
            return true;
        }
        
        // =========================================================
        
        // We don't know any enemy building, scout nearest starting location.
        if (!AEnemyUnits.hasDiscoveredEnemyBuilding()) {
            return tryFindingEnemyBuilding(scout);
        }
        
        // Roam around enemy base
//        else if (Count.ourCombatUnits() <= 1) {
//            return handleScoutEnemyBase(scout);
//        }

        // Scout other bases
        else {
            return handleScoutFreeBases(scout);
        }
    }

    // =========================================================

    private static boolean handleScoutFreeBases(AUnit scout) {
        APosition invisibleStartingLocation = BaseLocations.randomInvisibleStartingLocation();
        if (invisibleStartingLocation != null && scout.distToMoreThan(invisibleStartingLocation, 6)) {
            return scout.move(invisibleStartingLocation, UnitActions.SCOUT, "ScoutBases");
        }

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
        AUnit ourMainBase = Select.mainBase();
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
            startingLocation = BaseLocations.getStartingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = BaseLocations.getNearestUnexploredStartingLocation(scout.position());
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
    private static boolean handleScoutEnemyBase(AUnit scout) {

        // === Remain at the enemy base if it's known ==============

        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null) {
            ARegion enemyBaseRegion = Regions.getRegion(enemyBase);

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
    private static void assignScoutIfNeeded() {

        // Build order defines which worker should be a scout
        if (Count.workers() < BuildOrderSettings.scoutIsNthWorker()) {
            return;
        }
        
        // === Remove dead scouts ========================================
        
        removeDeadScouts();
        
        // === Zerg =================================================

        if (AGame.isPlayingAsZerg()) {

            // We know enemy building
            if (AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
                if (AGame.timeSeconds() < 350) {
                    if (scouts.isEmpty()) {
                        for (AUnit worker : Select.ourWorkers().notCarrying().list()) {
                            if (!worker.isBuilder()) {
                                scouts.add(worker);
                                break;
                            }
                        }
                    }
                }
            } // Haven't discovered any enemy building
            else {
                scouts.clear();
                scouts.addAll(Select.ourCombatUnits().listUnits());
            }
        }

        // =========================================================
        // TERRAN + PROTOSS

        else if (scouts.isEmpty()) {
            AUnit scout = Select.ourWorkers().nearestTo(BaseLocations.natural());
            if (!scout.isBuilder()) {
                scouts.add(scout);
                return;
            }
        }
    }

    private static void removeDeadScouts() {
        for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext();) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {
                iterator.remove();
                anyScoutBeenKilled = true;
            }
        }
    }

    private static void scoutForTheNextBase(AUnit scout) {
        APosition baseLocation = BaseLocations.getNearestUnexploredStartingLocation(scout.position());
        if (baseLocation != null) {
            scout.move(baseLocation.position(), UnitActions.EXPLORE, "Explore next base");
        }
    }

    private static void defineNextPolygonPointForEnemyBaseRoamingUnit(ARegion region, AUnit scout) {
        
        // Change roaming direction if we were forced to run from enemy units
        if (scoutingAroundBaseWasInterrupted) {
            scoutingAroundBaseDirectionClockwise = !scoutingAroundBaseDirectionClockwise;
        }
        
        // Define direction
        int deltaIndex = scoutingAroundBaseDirectionClockwise ? 1 : -1;
        
        // =========================================================
        
        APosition goTo = scoutingAroundBaseLastPolygonPoint != null
                ? APosition.create(scoutingAroundBaseLastPolygonPoint) : null;

        if (goTo == null || scoutingAroundBaseWasInterrupted) {
            goTo = useNearestPolygonPoint(region, scout);
        } else {
            if (scout.distTo(goTo) <= 3) {
                scoutingAroundBaseNextPolygonIndex = (scoutingAroundBaseNextPolygonIndex + deltaIndex)
                        % scoutingAroundBasePoints.size();
                if (scoutingAroundBaseNextPolygonIndex < 0) {
                    scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.size() - 1;
                }
                
                goTo = (APosition) scoutingAroundBasePoints.get(scoutingAroundBaseNextPolygonIndex);
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

        if (MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE) {
            CameraManager.centerCameraOn(scout.position());
        }
    }
    
    private static void initializeEnemyRegionPolygonPoints(AUnit scout, ARegion enemyBaseRegion) {
        Position centerOfRegion = enemyBaseRegion.getCenter();

        scoutingAroundBasePoints.addPosition(APosition.create(centerOfRegion));

        return;

//        for (Position point : enemyBaseRegion.getPolygon().getPoints()) {
//            APosition position = APosition.create(point);
//
//            // Calculate actual ground distance to this position
//            double groundDistance = AMap.getGroundDistance(scout, position);
//
//            // Fix problem with some points being unwalkable despite isWalkable being true
//            if (groundDistance < 2) {
//                continue;
//            }
//            position = PositionOperationsWrapper.getPositionMovedPercentTowards(point, centerOfRegion, 3.5);
//
//            // If positions is walkable, not in different region and has path to it, it should be ok
//            if (position.isWalkable() && enemyBaseRegion.getPolygon().isInside(position)
//                    && scout.hasPathTo(position) && groundDistance >= 4
//                    && groundDistance <= 1.7 * scout.distanceTo(position)) {
//                scoutingAroundBasePoints.addPosition(position);
//            }
//        }
//
//        if (MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE) {
//            AGame.changeSpeedTo(1);
//            AGame.changeSpeedTo(1);
//            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
//        }
    }

    private static APosition useNearestPolygonPoint(ARegion region, AUnit scout) {
        APosition nearest = scoutingAroundBasePoints.nearestTo(scout.position());
        scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.getLastIndex();
        return nearest;
    }

    public static APosition getUmsFocusPoint(APosition startPosition) {
        ARegion nearestUnexploredRegion = Regions.getNearestUnexploredRegion(startPosition);
        return nearestUnexploredRegion != null ? APosition.create(nearestUnexploredRegion.getCenter()) : null;
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
}

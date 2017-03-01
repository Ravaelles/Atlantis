package atlantis.scout;

import atlantis.AGame;
import atlantis.AViewport;
import atlantis.AtlantisConfig;
import atlantis.combat.micro.AAvoidMeleeUnitsManager;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AMap;
import atlantis.position.APosition;
import atlantis.position.PositionOperationsWrapper;
import atlantis.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.CodeProfiler;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;
import java.util.ArrayList;
import java.util.Iterator;

public class AScoutManager {

//    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = true;
    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = false;
    
    // =========================================================
    
    /**
     * Current scout unit.
     */
    private static ArrayList<AUnit> scouts = new ArrayList<>();

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

        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode()) {
            return;
        }

        // =========================================================
        
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_SCOUTING);
        assignScoutIfNeeded();

        // =========================================================
        // We don't know any enemy building, scout nearest starting location.
        if (!AEnemyUnits.hasDiscoveredMainEnemyBase()) {
            for (AUnit scout : scouts) {
                tryFindingEnemyBase(scout);
            }
        } 

        // Scout around enemy base
        else {
            for (AUnit scout : scouts) {
                if (scout.isAlive()) {
                    handleScoutEnemyBase(scout);
                }
            }

//            for (AUnit scout : scouts) {
//                scoutForTheNextBase(scout);
//            }
        }
        
        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_SCOUTING);
    }

    // =========================================================

    /**
     * We don't know any enemy building, scout nearest starting location.
     */
    public static void tryFindingEnemyBase(AUnit scout) {
        if (scout == null) {
            return;
        }
        scout.setTooltip("Find enemy");
        //scout.setTooltip("Find enemy");

        // Don't interrupt when moving
//        if (scout.isMoving() || scout.isAttacking()) {
//            return;
//        }
        // Define center point for our searches
        AUnit ourMainBase = Select.mainBase();
        if (ourMainBase == null) {
            return;
        }

        // === Handle UMT ==========================================
//        if (AGame.isUmtMode()) {
//            handleUmtExplore(scout);
//            return;
//        }
        // =========================================================
        // Get nearest unexplored starting location and go there
        BaseLocation startingLocation;
        if (scout.getType().equals(AUnitType.Zerg_Overlord) || scouts.size() > 1) {
            startingLocation = AMap.getStartingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = AMap.getNearestUnexploredStartingLocation(scout.getPosition());
        }

        // =========================================================
//        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
//        if (enemyBase != null) {
//            Region enemyBaseRegion = AtlantisMap.getRegion(enemyBase);
//            enemyBaseRegion.getPolygon().getCenter()
//        }
        // =========================================================
        if (startingLocation != null) {
            scout.setTooltip("Scout!");
            scout.move(startingLocation.getPosition(), UnitActions.EXPLORE);
            return;
        }
    }

    /**
     * Roam around enemy base to get information about build order for as long as possible.
     */
    private static boolean handleScoutEnemyBase(AUnit scout) {

        // === Avoid melee units ===================================
        if (AAvoidMeleeUnitsManager.avoidCloseMeleeUnits(scout)) {
            scoutingAroundBaseWasInterrupted = true;
            return true;
        }

        // === Remain at the enemy base if it's known ==============
        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
            Region enemyBaseRegion = AMap.getRegion(enemyBase);

            if (scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints(scout, enemyBaseRegion);
            }

            defineNextPolygonPointForEnemyBaseRoamingUnit(enemyBaseRegion, scout);
            if (scoutingAroundBaseLastPolygonPoint != null) {
                scout.move(scoutingAroundBaseLastPolygonPoint, UnitActions.EXPLORE);
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
        
        // === Remove dead scouts ========================================
        
        for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext();) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {
                iterator.remove();
                anyScoutBeenKilled = true;
            }
        }
        
        // =========================================================

        // ZERG case
        if (AGame.playsAsZerg()) {

            // We know enemy building
            if (AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
                if (AGame.getTimeSeconds() < 500) {
                    if (scouts.isEmpty()) {
                        for (AUnit worker : Select.ourWorkers().list()) {
                            if (!worker.isBuilder()) {
                                System.err.println(worker.getID());
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
        } // =========================================================
        // TERRAN + PRTOSSS
        else if (scouts.isEmpty() && Select.ourWorkers().count() >= AtlantisConfig.SCOUT_IS_NTH_WORKER) {
            scouts.add(Select.ourWorkers().first());
        }
    }

    private static void scoutForTheNextBase(AUnit scout) {
        BaseLocation baseLocation = AMap.getNearestUnexploredStartingLocation(scout.getPosition());
        if (baseLocation != null) {
            scout.move(baseLocation.getPosition(), UnitActions.MOVE);
        }
    }

    private static void defineNextPolygonPointForEnemyBaseRoamingUnit(Region region, AUnit scout) {
        
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
            if (scout.distanceTo(goTo) <= 3) {
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

        APainter.paintLine(
                scoutingAroundBaseLastPolygonPoint, scout.getPosition(), Color.Yellow
        );

        if (MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE) {
            AViewport.centerScreenOn(scout);
        }
    }
    
    private static void initializeEnemyRegionPolygonPoints(AUnit scout, Region enemyBaseRegion) {
        Position centerOfRegion = enemyBaseRegion.getCenter();

        for (Position point : enemyBaseRegion.getPolygon().getPoints()) {
            APosition position = APosition.create(point);

            // Calculate actual ground distance to this position
            double groundDistance = AMap.getGroundDistance(scout, position);

            // Fix problem with some points being unwalkable despite isWalkable being true
            if (groundDistance < 2) {
                continue;
            }
            position = PositionOperationsWrapper.getPositionMovedPercentTowards(point, centerOfRegion, 3.5);

            // If positions is walkable, not in different region and has path to it, it should be ok
            if (AMap.isWalkable(position) && enemyBaseRegion.getPolygon().isInside(position)
                    && scout.hasPathTo(position) && groundDistance >= 4
                    && groundDistance <= 1.7 * scout.distanceTo(position)) {
                scoutingAroundBasePoints.addPosition(position);
            }
        }

        if (MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE) {
            AGame.changeSpeedTo(1);
            AGame.changeSpeedTo(1);
            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
        }
    }

    private static APosition useNearestPolygonPoint(Region region, AUnit scout) {
        APosition nearest = scoutingAroundBasePoints.nearestTo(scout.getPosition());
        scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.getLastIndex();
        return nearest;
    }

    public static APosition getUmtFocusPoint(APosition startPosition) {
        Region nearestUnexploredRegion = AMap.getNearestUnexploredRegion(startPosition);
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

}

package atlantis.scout;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.AtlantisViewport;
import atlantis.combat.micro.AtlantisAvoidMeleeUnitsManager;
import atlantis.debug.APainter;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.APosition;
import atlantis.wrappers.PositionOperationsHelper;
import atlantis.wrappers.Positions;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;
import java.util.ArrayList;
import javax.swing.ActionMap;

public class AtlantisScoutManager {

//    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = true;
    public static boolean MAKE_VIEWPORT_FOLLOW_SCOUT_AROUND_BASE = false;
    
    // =========================================================
    
    /**
     * Current scout unit.
     */
    private static ArrayList<AUnit> scouts = new ArrayList<>();

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
        assignScoutIfNeeded();

        // =========================================================
        // We don't know any enemy building, scout nearest starting location.
        if (!AtlantisEnemyUnits.hasDiscoveredEnemyBuilding()) {
            for (AUnit scout : scouts) {
                tryFindingEnemyBase(scout);
            }
        } // Scout around enemy base
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
    }

    // =========================================================
    /**
     * Behavior for the scout if we know enemy base location.
     */
    private static void handleScoutWhenKnowEnemyBase(AUnit scout, AUnit enemyBase) {
        tryFindingEnemyBase(scout);

//        // Scout already attacking
//        if (scout.isAttacking()) {
//
//            // Scout is relatively healthy
//            if (scout.getHPPercent() >= 99) {
//                // OK
//            } // Scout is wounded
//            else {
//                scout.move(Select.mainBase(), false);
//            }
//        } // Attack
//        else if (!scout.isStartingAttack()) {
//            scout.attack(enemyBase, false);
//        }
    }

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
            startingLocation = AtlantisMap.getStartingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = AtlantisMap.getNearestUnexploredStartingLocation(scout.getPosition());
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
        if (AtlantisAvoidMeleeUnitsManager.handleAvoidCloseMeleeUnits(scout)) {
            scoutingAroundBaseWasInterrupted = true;
            return true;
        }

        // === Remain at the enemy base if it's known ==============
        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
            Region enemyBaseRegion = AtlantisMap.getRegion(enemyBase);

            if (scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints(scout, enemyBaseRegion);
            }

            defineNextPolygonForEnemyBaseRoamingUnit(enemyBaseRegion, scout);
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

        // ZERG case
        if (AGame.playsAsZerg()) {

            // We know enemy building
            if (AtlantisEnemyUnits.hasDiscoveredEnemyBuilding()) {
                if (AGame.getTimeSeconds() < 600) {
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
        BaseLocation baseLocation = AtlantisMap.getNearestUnexploredStartingLocation(scout.getPosition());
        if (baseLocation != null) {
            scout.move(baseLocation.getPosition(), UnitActions.MOVE);
        }
    }

    private static void defineNextPolygonForEnemyBaseRoamingUnit(Region region, AUnit scout) {
        
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
            AtlantisViewport.centerScreenOn(scout);
        }
    }
    
    private static void initializeEnemyRegionPolygonPoints(AUnit scout, Region enemyBaseRegion) {
        Position centerOfRegion = enemyBaseRegion.getCenter();

        for (Position point : enemyBaseRegion.getPolygon().getPoints()) {
            APosition position = APosition.create(point);

            // Calculate actual ground distance to this position
            double groundDistance = AtlantisMap.getGroundDistance(scout, position);

            // Fix problem with some points being unwalkable despite isWalkable being true
            if (groundDistance < 2) {
                continue;
            }
            position = PositionOperationsHelper.getPositionMovedPercentTowards(point, centerOfRegion, 3);

            // If positions is walkable, not in different region and has path to it, it should be ok
            if (AtlantisMap.isWalkable(position) && enemyBaseRegion.getPolygon().isInside(position)
                    && scout.hasPathTo(position) && groundDistance >= 3
                    && groundDistance <= 1.5 * scout.distanceTo(position)) {
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
        Region nearestUnexploredRegion = AtlantisMap.getNearestUnexploredRegion(startPosition);
        return nearestUnexploredRegion != null ? APosition.create(nearestUnexploredRegion.getCenter()) : null;
    }

    // =========================================================
    /**
     * Returns true if given unit has been assigned to explore the map.
     */
    public static boolean isScout(AUnit unit) {
        return scouts.contains(unit);
    }

}

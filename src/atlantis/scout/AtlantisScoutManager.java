package atlantis.scout;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.combat.micro.AtlantisAvoidMeleeUnitsManager;
import atlantis.debug.AtlantisPainter;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.APosition;
import atlantis.wrappers.Positions;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;
import java.util.ArrayList;
import javax.swing.ActionMap;

public class AtlantisScoutManager {

    /**
     * Current scout unit.
     */
    private static ArrayList<AUnit> scouts = new ArrayList<>();
    
    private static Positions scoutingAroundBasePoints = new Positions();
    private static int scoutingAroundBaseNextPolygonIndex = -1;
    private static Position scoutingAroundBaseLastPolygonPoint = null;
    private static boolean scoutingAroundBaseWasInterrupted = false;

    // =========================================================
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    public static void update() {

        // === Handle UMT ==========================================
        if (AtlantisGame.isUmtMode()) {
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
//        if (AtlantisGame.isUmtMode()) {
//            handleUmtExplore(scout);
//            return;
//        }
        // =========================================================
        // Get nearest unexplored starting location and go there
        BaseLocation startingLocation;
        if (scout.getType().equals(AUnitType.Zerg_Overlord)) {
            startingLocation = AtlantisMap.getStartingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = AtlantisMap.getNearestUnexploredStartingLocation(ourMainBase.getPosition());
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
        else {
            scoutingAroundBaseWasInterrupted = false;
        }

        // === Remain at the enemy base if it's known ==============
        APosition enemyBase = AtlantisEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
            Region enemyBaseRegion = AtlantisMap.getRegion(enemyBase);
            
            if (scoutingAroundBasePoints.isEmpty()) {
                scoutingAroundBasePoints.addPositions(enemyBaseRegion.getPolygon().getPoints());
            }
            
            defineNextPolygonForEnemyBaseRoamingUnit(enemyBaseRegion, scout);
            if (scoutingAroundBaseLastPolygonPoint != null) {
                scout.setTooltip("Scouting around (" + scoutingAroundBaseNextPolygonIndex + ")");
                scout.move(scoutingAroundBaseLastPolygonPoint, UnitActions.EXPLORE);
                return true;
            }
            else {
                scout.setTooltip("Can't find polygon");
            }
        }
        
        return false;
    }

    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private static void assignScoutIfNeeded() {

        // ZERG case
        if (AtlantisGame.playsAsZerg()) {

            // We know enemy building
            if (AtlantisEnemyUnits.hasDiscoveredEnemyBuilding()) {
                if (AtlantisGame.getTimeSeconds() < 600) {
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
        if (scoutingAroundBaseLastPolygonPoint == null) {
//            nextPolygon = getNearestPolygonInRegion(region, scout);
            scoutingAroundBaseNextPolygonIndex = 0;
            scoutingAroundBaseLastPolygonPoint = region.getPolygon().getPoints().get(0);
        }
        else {
//            System.out.println("----");
//            System.out.println("Polygon [" + scoutingAroundBaseLastPolygonPoint.getX() + "," + scoutingAroundBaseLastPolygonPoint.getY());
//            System.out.println("dist = " + scout.distanceTo(scoutingAroundBaseLastPolygonPoint));
            if (scout.distanceTo(scoutingAroundBaseLastPolygonPoint) <= 2.8) {
                scoutingAroundBaseNextPolygonIndex++;
                scoutingAroundBaseNextPolygonIndex = scoutingAroundBaseNextPolygonIndex 
                        % region.getPolygon().getPoints().size(); 
                scoutingAroundBaseLastPolygonPoint = region.getPolygon().getPoints()
                        .get(scoutingAroundBaseNextPolygonIndex);
//                System.out.println("CHANGE Polygon index " + scoutingAroundBaseNextPolygonIndex);
//                System.out.println("dist = " + scout.distanceTo(scoutingAroundBaseLastPolygonPoint));
            }
        }
        
        APosition goTo = APosition.create(scoutingAroundBaseLastPolygonPoint);
        
        if (!AtlantisMap.isWalkable(APosition.create(scoutingAroundBaseLastPolygonPoint))
                || !scout.hasPathTo(APosition.create(scoutingAroundBaseLastPolygonPoint))
                || AtlantisMap.getGroundDistance(scout, goTo) > 2 * scout.distanceTo(goTo)) {
//            System.out.println("REDEFINE");
            
            scoutingAroundBaseNextPolygonIndex++;
            scoutingAroundBaseNextPolygonIndex = scoutingAroundBaseNextPolygonIndex 
                    % region.getPolygon().getPoints().size();
            scoutingAroundBaseLastPolygonPoint = region.getPolygon().getPoints()
                    .get(scoutingAroundBaseNextPolygonIndex);
        }
        
        AtlantisPainter.paintLine(scoutingAroundBaseLastPolygonPoint, scout.getPosition(), Color.Green);
        AtlantisPainter.paintLine(scoutingAroundBaseLastPolygonPoint, scout.getPosition().translateByPixels(0, 1), Color.Green);
        AtlantisPainter.paintLine(scoutingAroundBaseLastPolygonPoint, scout.getPosition().translateByPixels(1, 0), Color.Green);
    }
    
//    private static APosition getNearestPolygonInRegion(Region region, AUnit scout) {
//        APosition nearestPosition = scoutingAroundBasePoints.nearestTo(scout.getPosition());
//        return nearestPosition;
//    }
    
    
//    private static void handleUmtExplore(AUnit scout) {
//        APosition focusPoint = getUmtFocusPoint(scout.getPosition());
//        
//        if (focusPoint != null) {
//            scout.attack(focusPoint, UnitActions.ATTACK_POSITION);
//        }
//    }
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

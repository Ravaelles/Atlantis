package atlantis.map.scout;

import atlantis.combat.micro.avoid.AAvoidEnemies;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraManager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.ARegion;
import atlantis.map.ARegionBoundary;
import atlantis.map.Bases;
import atlantis.map.Regions;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AbstractFoggedUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
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
    private static ARegion enemyBaseRegion = null;

    // =========================================================

    private static void update(AUnit scout) {
        scout.setTooltipTactical("Scout...");

//        if (AAvoidUnits.avoidEnemiesIfNeeded(scout)) {
        if (scout.isRunning()) {
            nextPositionToScout = null;
            scoutingAroundBaseWasInterrupted = true;
            if (A.seconds() >= 300) {
                handleScoutFreeBases(scout);
            }
        }

        // =========================================================

        // We don't know any enemy building, scout nearest starting location.
        if (!EnemyInfo.hasDiscoveredAnyBuilding()) {
            tryFindingEnemy(scout);
        }

        // Scout other bases
        else if (!anyScoutBeenKilled) {
            roamAroundEnemyBase(scout);
        }

        // Map roaming
        else {
            handleScoutFreeBases(scout);
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
            if (EnemyInfo.hasDiscoveredAnyBuilding()) {
                scouts.clear();
            }
        }
    }

    // =========================================================

    private static boolean handleScoutFreeBases(AUnit scout) {
        if (nextPositionToScout != null && !nextPositionToScout.isPositionVisible()) {
            return scout.move(nextPositionToScout, Actions.MOVE_SCOUT, "ScoutBases", true);
        }

        AbstractFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
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
    public static boolean tryFindingEnemy(AUnit scout) {
        if (scout == null) {
            return true;
        }
        scout.setTooltipTactical("Find enemy");

        // Define center point for our searches
        AUnit ourMainBase = Select.main();
        if (ourMainBase == null && A.notUms()) {
            return false;
        }

        // =========================================================
        // Get nearest unexplored starting location and go there

        HasPosition startingLocation;
        if (scout.is(AUnitType.Zerg_Overlord) || scouts.size() > 1) {
            startingLocation = Bases.startingLocationBasedOnIndex(
                    scout.getUnitIndexInBwapi()// UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = Bases.nearestUnexploredStartingLocation(scout.position());
        }

        // =========================================================

        if (startingLocation != null) {
            scout.move(startingLocation, Actions.MOVE_EXPLORE, "Explore", true);
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
        scoutingAroundBaseWasInterrupted = false;

        // === Remain at the enemy base if it's known ==============

        APosition enemy = EnemyUnits.enemyBase();
        if (enemy == null) {
            AbstractFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
            if (enemyBuilding != null) {
                enemy = enemyBuilding.position();
            }
        }
//        APosition enemyBase = Select.main().position();

        if (enemy != null) {
            enemyBaseRegion = enemy.region();

            if (scoutingAroundBasePoints.isEmpty()) {
                initializeEnemyRegionPolygonPoints(scout);
            }

            defineNextPolygonPointForEnemyBaseRoamingUnit(scout);
            if (scoutingAroundBaseLastPolygonPoint != null) {
                scout.move(scoutingAroundBaseLastPolygonPoint, Actions.MOVE_EXPLORE, "RoamAround", true);
                return true;
            } else {
                scout.setTooltipTactical("Can't find polygon point");
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
//                        System.out.println("Add scout " + scout);
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
//                System.out.println("Remove dead scout " + scout);
                iterator.remove();
                anyScoutBeenKilled = true;
            }
        }
    }

    private static void scoutForTheNextBase(AUnit scout) {
        APosition baseLocation = Bases.nearestUnexploredStartingLocation(scout.position());
        if (baseLocation != null) {
            scout.move(baseLocation.position(), Actions.MOVE_EXPLORE, "Explore next base", true);
        }
    }

    private static void defineNextPolygonPointForEnemyBaseRoamingUnit(AUnit scout) {
        if (AAvoidEnemies.avoidEnemiesIfNeeded(scout)) {
            scout.setTooltipTactical("ChangeOfPlans");
            return;
        }

        scoutingAroundBaseLastPolygonPoint = nextPointAroundEnemyBase(scout);

        if (APainter.paintingMode == APainter.MODE_FULL_PAINTING) {
            APainter.paintLine(
                scoutingAroundBaseLastPolygonPoint, scout.position(), Color.Yellow
            );
        }

        if (MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE) {
            CameraManager.centerCameraOn(scout.position());
        }
    }

    private static HasPosition nextPointAroundEnemyBase(AUnit scout) {

        // Change roaming direction if we were forced to run from enemy units
        if (scoutingAroundBaseWasInterrupted) {
            scoutingAroundBaseDirectionClockwise = !scoutingAroundBaseDirectionClockwise;
        }

        // Define direction
        HasPosition goTo = null;
        int step = 1;
        do {
            int deltaIndex = scoutingAroundBaseDirectionClockwise ? step : -step;

            goTo = scoutingAroundBaseLastPolygonPoint != null ? APosition.create(scoutingAroundBaseLastPolygonPoint) : null;

            if (goTo == null || scoutingAroundBaseWasInterrupted) {
                goTo = useNearestBoundaryPoint(enemyBaseRegion, scout);
            } else {
                if (scout.distTo(goTo) <= 1.8) {
                    scoutingAroundBaseNextPolygonIndex = (scoutingAroundBaseNextPolygonIndex + deltaIndex)
                        % scoutingAroundBasePoints.size();
                    if (scoutingAroundBaseNextPolygonIndex < 0) {
                        scoutingAroundBaseNextPolygonIndex = scoutingAroundBasePoints.size() - 1;
                    }

                    goTo = scoutingAroundBasePoints.get(scoutingAroundBaseNextPolygonIndex);

                    if (Select.all().inRadius(0.5, goTo).empty()) {
                        break;
                    }
                }
            }

            step++;
        } while (step <= 15);

        scoutingAroundBaseWasInterrupted = false;
        return scoutingAroundBaseLastPolygonPoint = goTo;
    }

    private static void initializeEnemyRegionPolygonPoints(AUnit scout) {
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

//        if (MAKE_CAMERA_FOLLOW_SCOUT_AROUND_BASE) {
//            AGame.changeSpeedTo(1);
//            AGame.changeSpeedTo(1);
//            APainter.paintingMode = APainter.MODE_FULL_PAINTING;
//        }
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
            worker.setTooltipTactical("Patataj");
            return true;
        }
        else {
            worker.setTooltipTactical("Dajesz kurwa!");
            if (A.now() % 50 >= 25) {
                worker.move(horse, Actions.MOVE_SPECIAL, "", true);
                worker.setTooltipTactical("Ciśniesz!");
            }
        }

        return true;
    }
}

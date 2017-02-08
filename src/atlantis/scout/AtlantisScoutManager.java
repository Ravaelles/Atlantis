package atlantis.scout;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.APosition;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;
import java.util.ArrayList;

public class AtlantisScoutManager {

    /**
     * Current scout unit.
     */
    private static ArrayList<AUnit> scouts = new ArrayList<>();

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

        // We don't know any enemy building, scout nearest starting location.
        if (!AtlantisEnemyUnits.hasDiscoveredEnemyBuilding()) {
            for (AUnit scout : scouts) {
                tryToFindEnemy(scout);
            }
        } else {
            for (AUnit scout : scouts) {
                scoutForTheNextBase(scout);
            }

            // We know enemy building, but don't know any base.
//            AUnit enemyBase = AtlantisEnemyInformationManager.hasDiscoveredEnemyBase();
//            if (enemyBase == null) {
//                // @TODO
//            } // We know the exact location of enemy's base.
//            else {
//                for (AUnit scout : scouts) {
//                    handleScoutWhenKnowEnemyBase(scout, enemyBase);
//                }
//            }
        }
    }

    // =========================================================
    /**
     * Behavior for the scout if we know enemy base location.
     */
    private static void handleScoutWhenKnowEnemyBase(AUnit scout, AUnit enemyBase) {
        tryToFindEnemy(scout);

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
    public static void tryToFindEnemy(AUnit scout) {
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
        if (startingLocation != null) {
            scout.setTooltip("Scout!");
            //scout.setTooltip("Scout!");
            scout.move(startingLocation.getPosition(), UnitActions.MOVE);
        }
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

//    private static void handleUmtExplore(AUnit scout) {
//        APosition focusPoint = getUmtFocusPoint(scout.getPosition());
//        
//        if (focusPoint != null) {
//            scout.attack(focusPoint, UnitActions.ATTACK_POSITION);
//        }
//    }
    
    public static APosition getUmtFocusPoint(APosition startPosition) {
        Region nearestUnexploredRegion = AtlantisMap.getNearestUnexploredRegion(startPosition);
        return nearestUnexploredRegion != null ? APosition.createFrom(nearestUnexploredRegion.getCenter()) : null;
    }

    // =========================================================
    /**
     * Returns true if given unit has been assigned to explore the map.
     */
    public static boolean isScout(AUnit unit) {
        return scouts.contains(unit);
    }

}

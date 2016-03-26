package atlantis.scout;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.information.AtlantisEnemyInformationManager;
import atlantis.enemy.AtlantisMap;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.util.UnitUtil;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import bwta.BaseLocation;
import bwapi.Unit;
import bwapi.UnitType;

public class AtlantisScoutManager {

    /**
     * Current scout unit.
     */
    private static ArrayList<Unit> scouts = new ArrayList<>();

    // =========================================================
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    public static void update() {
        // @FIX: Currently no scout is used !!!!!!!!!!!!!!!!!!!!!!!
//        assignScoutIfNeeded();

        // We don't know any enemy building, scout nearest starting location.
        if (!AtlantisEnemyInformationManager.hasDiscoveredEnemyBuilding()) {
            for (Unit scout : scouts) {
                tryToFindEnemy(scout);
            }
        } else {
            for (Unit scout : scouts) {
                scoutForTheNextBase(scout);
            }

            // We know enemy building, but don't know any base.
//            Unit enemyBase = AtlantisEnemyInformationManager.hasDiscoveredEnemyBase();
//            if (enemyBase == null) {
//                // @TODO
//            } // We know the exact location of enemy's base.
//            else {
//                for (Unit scout : scouts) {
//                    handleScoutWhenKnowEnemyBase(scout, enemyBase);
//                }
//            }
        }
    }

    // =========================================================
    /**
     * Behavior for the scout if we know enemy base location.
     */
    private static void handleScoutWhenKnowEnemyBase(Unit scout, Unit enemyBase) {
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
    public static void tryToFindEnemy(Unit scout) {
        if (scout == null) {
            return;
        }
        TooltipManager.setTooltip(scout, "Find enemy");
        //scout.setTooltip("Find enemy");

        // Don't interrupt when moving
//        if (scout.isMoving() || scout.isAttacking()) {
//            return;
//        }
        // Define center point for our searches
        Unit ourMainBase = Select.mainBase();
        if (ourMainBase == null) {
            return;
        }

        // =========================================================
        // Get nearest unexplored starting location and go there
        BaseLocation startingLocation;
        if (scout.getType().equals(UnitType.Zerg_Overlord)) {
            startingLocation = AtlantisMap.getStartingLocationBasedOnIndex(
                    UnitUtil.getUnitIndex(scout)
            );
        } else {
            startingLocation = AtlantisMap.getNearestUnexploredStartingLocation(ourMainBase.getPosition());
        }

        // =========================================================
        if (startingLocation != null) {
            TooltipManager.setTooltip(scout, "Scout!");
            //scout.setTooltip("Scout!");
            scout.move(startingLocation.getPosition(), false);
        }
    }

    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private static void assignScoutIfNeeded() {

        // ZERG case
        if (AtlantisGame.playsAsZerg()) {
            if (AtlantisEnemyInformationManager.hasDiscoveredEnemyBuilding()) { // We know enemy building
                if (AtlantisGame.getTimeSeconds() < 600) {
                    if (scouts.isEmpty()) {
                        scouts.add(Select.ourWorkers().first());
                    }
                }
            } // Haven't discovered any enemy building
            else {
                scouts.clear();
                scouts.addAll(Select.ourCombatUnits().listUnits());
            }
        } // =========================================================
        // TERRAN + PRTOSSS
        else if (scouts.isEmpty() && AtlantisUnitInformationManager.countOurWorkers() >= AtlantisConfig.SCOUT_IS_NTH_WORKER) {
            scouts.add(Select.ourWorkers().first());
        }
    }

    private static void scoutForTheNextBase(Unit scout) {
        BaseLocation baseLocation = AtlantisMap.getNearestUnexploredStartingLocation(scout.getPosition());
        if (baseLocation != null) {
            scout.move(baseLocation.getPosition());
        }
    }

}

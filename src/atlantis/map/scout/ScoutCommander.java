package atlantis.map.scout;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.base.Bases;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import atlantis.util.We;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class ScoutCommander extends Commander {

    public static boolean anyScoutBeenKilled = false;

    /**
     * Current scout unit.
     */
    private static final ArrayList<AUnit> scouts = new ArrayList<>();

    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    protected void handle() {
        // CodeProfiler.startMeasuring(this);

        // === Handle UMS ==========================================

        if (AGame.isUms()) {
            return;
        }

        // === Act with every scout ================================

        manageScoutAssigned();

        try {
            for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext(); ) {
                AUnit unit = iterator.next();

                if (unit != null && unit.isAlive()) {
                    Manager scoutManager = new ScoutManager(unit);
                    scoutManager.invoke();
                }
            }
        } catch (ConcurrentModificationException ignore) {
        }

        // CodeProfiler.endMeasuring(this);
    }

    // =========================================================

    private void removeOverlordsAsScouts() {
        if (We.zerg()) {
            if (EnemyInfo.hasDiscoveredAnyBuilding()) {
                scouts.clear();
            }
        }
    }


    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private void manageScoutAssigned() {
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
            if (anyScoutBeenKilled && OurStrategy.get().isRushOrCheese()) {
                return;
            }

            for (AUnit scout : Select.ourWorkers().notCarrying().sortDataByDistanceTo(Bases.natural(), true)) {
                if (!scout.isBuilder() && !scout.isRepairerOfAnyKind()) {
                    if (scouts.isEmpty()) {

                        scouts.add(scout);
                        return;
                    }
                }
            }
        }
    }

    private void removeExcessiveScouts() {
        if (scouts.size() > 1) {
            AUnit leaveThisScout = scouts.get(scouts.size() - 1);
            scouts.clear();
            scouts.add(leaveThisScout);
        }
    }

    private void removeDeadScouts() {
        for (Iterator<AUnit> iterator = scouts.iterator(); iterator.hasNext(); ) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {

                iterator.remove();
                anyScoutBeenKilled = true;
            }
        }
    }

    // =========================================================

    public static boolean hasAnyScoutBeenKilled() {
        return anyScoutBeenKilled;
    }

    public static ArrayList<AUnit> allScouts() {
        return scouts;
    }

    /**
     * Returns true if given unit has been assigned to explore the map.
     */
    public static boolean isScout(AUnit unit) {
        return ScoutCommander.allScouts().contains(unit);
    }
}

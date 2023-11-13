package atlantis.map.scout;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.A;
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
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(17);
    }

    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    @Override
    public void handle() {
        // CodeProfiler.startMeasuring(this);

        // === Handle UMS ==========================================

        if (AGame.isUms()) {
            return;
        }

        // === Act with every scout ================================

        manageScoutAssigned();

        try {
            for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
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
                ScoutState.scouts.clear();
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
        if (Count.workers() >= BuildOrderSettings.scoutIsNthWorker())

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

            else if (ScoutState.scouts.isEmpty()) {
                if (ScoutState.scoutsKilledCount <= 1 && OurStrategy.get().isRushOrCheese()) {
                    return;
                }

                for (AUnit scout : Select.ourWorkers().notCarrying().sortDataByDistanceTo(Bases.natural(), true)) {
                    if (!scout.isBuilder() && !scout.isRepairerOfAnyKind()) {
                        if (ScoutState.scouts.isEmpty()) {

                            ScoutState.scouts.add(scout);
                            return;
                        }
                    }
                }
            }
    }

    private void removeExcessiveScouts() {
        if (ScoutState.scouts.size() > 1) {
            AUnit leaveThisScout = ScoutState.scouts.get(ScoutState.scouts.size() - 1);
            ScoutState.scouts.clear();
            ScoutState.scouts.add(leaveThisScout);
        }
    }

    private void removeDeadScouts() {
        for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {

                iterator.remove();
                ScoutState.scoutsKilledCount++;
            }
        }
    }

    // =========================================================

    public static boolean hasAnyScoutBeenKilled() {
        return ScoutState.scoutsKilledCount > 0;
    }

    public static ArrayList<AUnit> allScouts() {
        return ScoutState.scouts;
    }

    /**
     * Returns true if given unit has been assigned to explore the map.
     */
    public static boolean isScout(AUnit unit) {
        return ScoutCommander.allScouts().contains(unit);
    }
}

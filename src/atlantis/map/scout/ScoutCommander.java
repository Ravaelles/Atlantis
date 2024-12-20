package atlantis.map.scout;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class ScoutCommander extends Commander {
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    @Override
    public void handle() {
        // CodeProfiler.startMeasuring(this);

        if (AGame.isUms() && Count.bases() == 0) return;

        // === Act with every scout ================================

        manageScoutsAssigned();

        try {
            for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
                AUnit unit = iterator.next();

                if (unit != null && unit.isAlive()) {
                    Manager scoutManager = new ScoutManager(unit);
                    scoutManager.invokeFrom(this);
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
                removeAllScouts();
            }
        }
    }

    private void removeAllScouts() {
        for (AUnit scout : ScoutState.scouts) {
            scout.setScout(false);
        }

        ScoutState.scouts.clear();
    }


    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private void manageScoutsAssigned() {
        removeDeadScouts();
        removeOverlordsAsScouts();
        removeExcessiveScouts();

        // Build order defines which worker should be a scout
        if (Count.workers() >= BuildOrderSettings.scoutIsNthWorker()) {
            if (We.zerg()) {
            }

            // =========================================================
            // TERRAN + PROTOSS

            else if (ScoutState.scouts.size() < scoutsNeeded()) {
//                if (ScoutState.scoutsKilledCount >= 2) return;
//
//                if (ScoutState.scoutsKilledCount <= 1 && OurStrategy.get().isRushOrCheese()) {
//                    return;
//                }

                assignScout();
            }
        }
    }

    private int scoutsNeeded() {
        return 1;
//        return A.supplyUsed() >= 70 ? 2 : 1;
    }

    private static boolean assignScout() {
        for (AUnit scout : candidates()) {
            if (
                scout.isBuilder()
                    || scout.isRepairerOfAnyKind()
                    || scout.isBuilder()
                    || scout.lastActionLessThanAgo(90, Actions.SPECIAL)
            ) {
                ErrorLog.printMaxOncePerMinute("Scout got mission: " + scout.manager());
                continue;
            }

//            if (ScoutState.scouts.isEmpty()) {
            addScout(scout);
            return true;
//            }
        }

        return false;
    }

    private static void addScout(AUnit newScout) {
        ScoutState.scouts.add(newScout);
        newScout.setScout(true);
    }

    private static List<AUnit> candidates() {
        return FreeWorkers.get().sortDataByDistanceTo(DefineNaturalBase.natural(), true);
    }

    private void removeExcessiveScouts() {
        int scoutsNeeded = scoutsNeeded();

        if (ScoutState.scouts.size() > scoutsNeeded) {
            AUnit leaveThisScout = ScoutState.scouts.get(ScoutState.scouts.size() - scoutsNeeded);
            removeAllScouts();

            addScout(leaveThisScout);
        }
    }

    private void removeDeadScouts() {
        for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
            AUnit scout = iterator.next();
            if (!scout.isAlive()) {
                scout.setScout(false);
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

package atlantis.map.scout;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class ScoutCommander extends Commander {
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     *
     * @return
     */
    @Override
    public boolean handle() {
        // CodeProfiler.startMeasuring(this);

        if (AGame.isUms() && Count.bases() == 0) return false;

        // === Act with every scout ================================

        manageScoutsAssigned();

        try {
            for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
                AUnit unit = iterator.next();

                if (unit != null && unit.isAlive()) {
                    Manager acted = ScoutState.scoutUnitManager.create(unit).invokeFrom(this);
                    if (acted == null) {
                        // An idle scout next to enemies that can attack it
                        // is not watching - it is stuck (measured: idle
                        // minutes among drones while every manager
                        // declines, including avoid). Retreat; the chain
                        // resumes when the danger passes.
                        boolean retreating = false;
                        if (unit.enemiesThatCanAttackMe(9).notEmpty()) {
                            retreating = unit.moveAwayFrom(
                                unit.enemiesThatCanAttackMe(9).center(),
                                12, Actions.MOVE_SCOUT, "ScoutRetreat");
                        }

                        // Telemetry only, never a release: prints solely for
                        // a scout that stays put with no anchor anywhere -
                        // the terminal manager owns every defined location,
                        // so this now means map data is absent, not that
                        // coverage lapsed. A retreating scout is acting,
                        // not idle, and stays silent.
                        if (!retreating) {
                            ErrorLog.printMaxOncePerMinute("Scout idle: chain found nothing to do");
                            unit.setTooltipTactical("Scout idle");
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException ignore) {
        }

        // CodeProfiler.endMeasuring(this);
        return false;
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
//        removeExcessiveScouts();

        // Build order defines which worker should be a scout
        if (Count.workers() >= ScoutState.scoutIsNthWorker) {
            if (We.zerg()) return;

            // =========================================================
            // TERRAN + PROTOSS

            if (ScoutState.scouts.size() < scoutsNeeded()) {
//                if (ScoutState.scoutsKilledCount >= 2) return;
//
//                if (ScoutState.scoutsKilledCount <= 1 && Strategy.get().isRushOrCheese()) {
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
        for (AUnit worker : FreeWorkers.get().list()) {
            if (
                worker.isBuilder()
                    || worker.isRepairerOfAnyKind()
                    || worker.isBuilder()
                    || worker.lastActionLessThanAgo(90, Actions.SPECIAL)
            ) {
                ErrorLog.printMaxOncePerMinute("Scout got mission: " + worker.manager());
                continue;
            }

            if (addScout(worker)) return true;
        }

        return false;
    }

    private static boolean addScout(AUnit newScout) {
        ScoutState.scouts.add(newScout);
        newScout.setScout(true);

        return true;
    }

//    private static List<AUnit> candidates() {
//        return FreeWorkers.get();
//    }

    private void removeExcessiveScouts() {
        int scoutsNeeded = scoutsNeeded();

        if (ScoutState.scouts.size() > scoutsNeeded) {
            AUnit leaveThisScout = ScoutState.scouts.get(ScoutState.scouts.size() - scoutsNeeded);
            removeAllScouts();

            addScout(leaveThisScout);
        }
    }

    private void removeDeadScouts() {        for (Iterator<AUnit> iterator = ScoutState.scouts.iterator(); iterator.hasNext(); ) {
            AUnit scout = iterator.next();
            if (scout.hp() <= 0) {
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

    public static int killedScouts() {
        return ScoutState.scoutsKilledCount;
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

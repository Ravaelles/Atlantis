package atlantis.map.scout;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.OurStrategy;
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
                    scoutManager.invoke(this);
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

            if (We.zerg()) {
            }

            // =========================================================
            // TERRAN + PROTOSS

            else if (ScoutState.scouts.isEmpty()) {
                if (ScoutState.scoutsKilledCount <= 1 && OurStrategy.get().isRushOrCheese()) {
                    return;
                }

                for (AUnit scout : candidates()) {
                    if (
                        scout.isBuilder()
                            || scout.isRepairerOfAnyKind()
                            || scout.isBuilder()
                            || scout.lastActionLessThanAgo(50, Actions.SPECIAL)
                    ) {
                        ErrorLog.printMaxOncePerMinute("Scout got mission: " + scout.manager());
                        continue;
                    }

                    if (ScoutState.scouts.isEmpty()) {

                        ScoutState.scouts.add(scout);
                        return;
                    }
                }
            }
    }

    private static List<AUnit> candidates() {
        return FreeWorkers.get().sortDataByDistanceTo(DefineNaturalBase.natural(), true);
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

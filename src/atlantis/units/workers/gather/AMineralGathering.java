package atlantis.units.workers.gather;

import atlantis.game.neutral.NeutralUnits;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.Collection;
import java.util.List;

/**
 * Auxiliary class that's supposed to assign workers to minerals at the beginning of game.
 */
public class AMineralGathering {

    /**
     * Assign all 4 workers to optimal (closest) minerals fields.
     */
    public static void initialAssignWorkersToMinerals() {
        AUnit mainBase = Select.main();
        if (mainBase == null) {
            return;
        }

        // =========================================================

        // Get minerals near to our main base and sort them from closest to most distant one
        List<AUnit> minerals = (List<AUnit>) Select.minerals()
            .inRadius(10, mainBase)
            .sortDataByDistanceTo(mainBase, true);

        if (minerals.isEmpty()) {
            return;
        }

        // Assign every worker to the next free mineral
        Collection<AUnit> workers = Select.ourWorkers().list();
        int counter = 0;
        for (AUnit worker : workers) {
            AUnit mineral = minerals.get(counter % minerals.size());
            worker.gather(mineral);
            worker.setTooltipTactical("Mandatum!");

            counter++;
        }
    }

    /**
     * Use this method to assign idle workers to gather minerals from optimal mineral field or to gather gas.
     */
    public static boolean gatherResources(AUnit unit) {
        AUnit mineralField = getMineralFieldToGather(unit);

        if (mineralField != null && !unit.isGatheringResources()) {
            unit.setTooltipTactical("Gatherer!");
            unit.setAction(Actions.GATHER_MINERALS);

            if (mineralField.isVisibleUnitOnMap()) {
                if (Select.enemyCombatUnits().countInRadius(10, mineralField) == 0) {
                    unit.gather(mineralField);
                }
                return true;
            } else {
                unit.move(mineralField, Actions.MOVE_REVEAL, "ToFoggedMineral");

                return true;
            }
        }

        return false;
    }

    // =========================================================

    private static AUnit getMineralFieldToGather(AUnit worker) {

        // Get the nearest base for this unit
        AUnit base = Select.ourBases().nearestTo(worker);

        if (base == null) return null;

        // Get minerals near to our main base and sort them from closest to most distant one
        List<AUnit> minerals = Select.minerals().inRadius(AUnit.NEAR_DIST, base).sortDataByDistanceTo(base, true);
        if (!minerals.isEmpty()) {

            // Count how many other workers gather this mineral
            Units mineralsToWorkerCount = new Units();
            Collection<AUnit> ourWorkersInRange = Select.ourWorkers().inRadius(12, base).list();

            for (AUnit mineral : minerals) {
                mineralsToWorkerCount.addUnitWithValue(mineral, 0.0);
                for (AUnit ourWorker : ourWorkersInRange) {
                    if (ourWorker.isGatheringMinerals() && mineral.equals(ourWorker.target())) {
                        mineralsToWorkerCount.incrementValue(mineral);
                    }
                }
            }

            // Get the least gathered mineral. Ties (and with few workers most minerals tie at
            // 0-1 gatherers) must NOT fall back to list order, which is base distance - every
            // worker then targets the same patch, the counts flip frame to frame, and the
            // "least gathered" pick changes every frame, re-targeting each worker constantly.
            // Among equally gathered minerals pick the one closest to THIS worker: that is a
            // total order (worker, mineral) that is stable across frames.
            AUnit bestMineral = null;
            double lowestCount = Double.MAX_VALUE;
            double bestDist = Double.MAX_VALUE;
            for (AUnit mineral : minerals) {
                double count = mineralsToWorkerCount.valueFor(mineral);
                double dist = mineral.distTo(worker);
                if (count < lowestCount || (count == lowestCount && dist < bestDist)) {
                    lowestCount = count;
                    bestDist = dist;
                    bestMineral = mineral;
                }
            }

            if (bestMineral != null) return bestMineral;
        }

        // If no minerals found, return nearest mineral
        AUnit mineral = Select.minerals().nearestTo(worker);
        if (mineral != null) return mineral;

        // Return fogged mineral as last resort

        return NeutralUnits.discovered().minerals().groundNearestTo(worker);
    }

}

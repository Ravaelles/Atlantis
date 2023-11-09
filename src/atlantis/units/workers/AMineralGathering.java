package atlantis.units.workers;

import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

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

        if (mineralField != null && !unit.isGatheringMinerals()) {
            unit.setTooltipTactical("Gatherer!");
            unit.gather(mineralField);
            return true;
        }

        return false;
    }

    // =========================================================

    private static AUnit getMineralFieldToGather(AUnit worker) {

        // Get nearest base for this unit
        AUnit base = Select.ourBases().nearestTo(worker);

        if (base == null) return null;

        // Get minerals near to our main base and sort them from closest to most distant one
        List<AUnit> minerals = Select.minerals().inRadius(15, base).sortDataByDistanceTo(base, true);
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

            // Get the least gathered mineral
            AUnit leastGatheredMineral = mineralsToWorkerCount.unitWithLowestValue();

//            if (leastGatheredMineral != null && leastGatheredMineral.distTo(worker) >= 40) {
//                System.err.println("Fucked up mineral? Dist to worker = " + leastGatheredMineral.distTo(worker));
//            }

            // This is our optimal mineral to gather near given unit
            if (leastGatheredMineral != null) return leastGatheredMineral;
        }

        // If no minerals found, return nearest mineral
        return Select.minerals().nearestTo(worker);
    }

}

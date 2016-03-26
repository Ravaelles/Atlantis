package atlantis.workers;

import atlantis.util.AtlantisUtilities;
import atlantis.wrappers.Select;
import atlantis.wrappers.Units;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bwapi.Unit;

/**
 * Auxiliary class that's supposed to assign workers to minerals at the beginning of game.
 */
public class AtlantisMineralGathering {

    /**
     * Assign all 4 workers to optimal (closest) minerals fields.
     */
    public static void initialAssignWorkersToMinerals() {

        // Get minerals near to our main base and sort them from closest to most distant one
        //TODO check safety of this cast
        List<Unit> minerals = (List<Unit>) Select.minerals().inRadius(12, Select.mainBase().getPosition()).
                sortDataByDistanceTo(Select.mainBase().getPosition(), true);

        // Get our workers
        Collection<Unit> workers = Select.ourWorkers().listUnits();

        // Assign every worker to the next free mineral
        int counter = 0;
        for (Unit unit : workers) {
            Unit mineral = minerals.get(counter);
            unit.gather(mineral, false);

            counter++;
        }
    }

    /**
     * Use this method to assign idle workers to gather minerals from optimal mineral field or to gather gas.
     */
    public static void gatherResources(Unit unit) {
        Unit mineralField = getMineralFieldToGather(unit);
        if (mineralField != null) {
            unit.gather(mineralField);
        }
    }

    // =========================================================
    private static Unit getMineralFieldToGather(Unit worker) {

        // Get nearest base for this unit
        Unit base = Select.ourBases().nearestTo(worker.getPosition());
        if (base == null) {
            return null;
        }

        // Get minerals near to our main base and sort them from closest to most distant one
        List<Unit> minerals = (List<Unit>) Select.minerals().inRadius(12, base.getPosition()).listUnits();
        if (!minerals.isEmpty()) {

            // Count how many other workers gather this mineral
            Map<Unit, Integer> workersPerMineral = new HashMap<>();

            Collection<Unit> ourWorkersInRange = (Collection<Unit>) Select.ourWorkers().inRadius(12, base.getPosition()).list();
//            for (Unit otherWorker : ourWorkersInRange) {
//                if (otherWorker.isGatheringMinerals()) {
//                    Unit mineralMined = otherWorker.getTarget();
//                    if (mineralMined != null) {
//                        //increments the number of workers in this mineral
//                        int previousNumber = (workersPerMineral.get(mineralMined) == null ? 0 : workersPerMineral.get(mineralMined));
//                        workersPerMineral.put(mineralMined, previousNumber + 1);
//                        //minerals.changeValueBy(mineralMined, 1);
//                    }
//                }
//            }
            for (Unit mineral : minerals) {
                for (Unit ourWorker : ourWorkersInRange) {
                    Unit mineralMinedByWorker = ourWorker.getTarget();
                    if (ourWorker.isGatheringMinerals() && mineral.equals(mineralMinedByWorker)) {
                        workersPerMineral.put(mineralMinedByWorker, (workersPerMineral.containsKey(mineral) ? 
                                workersPerMineral.get(mineralMinedByWorker) : 0) + 1);
                    }
                    else {
                        if (!workersPerMineral.containsKey(mineral)) {
                            workersPerMineral.put(mineral, 0);
                        }
                    }
                }
            }

            // Get least gathered mineral
            Unit leastGatheredMineral = null;
            int minimumWorkersPerMineral = 1000;
            for (Entry<Unit, Integer> workersAtMineral : workersPerMineral.entrySet()) {
                if (workersAtMineral.getValue() < minimumWorkersPerMineral) {
                    minimumWorkersPerMineral = workersAtMineral.getValue();
                    leastGatheredMineral = workersAtMineral.getKey();
                    break;
                }
            }

            // This is our optimal mineral to gather near given unit
            return leastGatheredMineral;
        } // If no minerals found, return nearest mineral
        else {
            return Select.minerals().nearestTo(base.getPosition());
        }
    }

}

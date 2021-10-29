package atlantis.workers;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Auxiliary class that's supposed to assign workers to minerals at the beginning of game.
 */
public class AMineralGathering {

    /**
     * Assign all 4 workers to optimal (closest) minerals fields.
     */
    public static void initialAssignWorkersToMinerals() {
        AUnit mainBase = Select.mainBase();
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
        Collection<AUnit> workers = Select.ourWorkers().listUnits();
        int counter = 0;
        for (AUnit worker : workers) {
            AUnit mineral = minerals.get(counter);
            worker.gather(mineral);

            counter++;
        }
    }

    /**
     * Use this method to assign idle workers to gather minerals from optimal mineral field or to gather gas.
     */
    public static void gatherResources(AUnit unit) {
        AUnit mineralField = getMineralFieldToGather(unit);
        if (mineralField != null) {
            unit.gather(mineralField);
        }
    }

    // =========================================================
    
    private static AUnit getMineralFieldToGather(AUnit worker) {

        // Get nearest base for this unit
        AUnit base = Select.ourBases().nearestTo(worker);
        if (base == null) {
            return null;
        }

        // Get minerals near to our main base and sort them from closest to most distant one
        List<AUnit> minerals = Select.minerals().inRadius(12, base).listUnits();
//        System.out.println(Select.minerals().inRadius(12, base));
        if (!minerals.isEmpty()) {

            // Count how many other workers gather this mineral
            Map<AUnit, Integer> workersPerMineral = new HashMap<>();

            Collection<AUnit> ourWorkersInRange = Select.ourWorkers().inRadius(12, base).list();
            for (AUnit mineral : minerals) {
                for (AUnit ourWorker : ourWorkersInRange) {
                    AUnit mineralMinedByWorker = ourWorker.getTarget();
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
            AUnit leastGatheredMineral = null;
            int leastWorkersAtMineral = 99;
            for (Entry<AUnit, Integer> workersAtMineral : workersPerMineral.entrySet()) {
                if (workersAtMineral.getValue() < leastWorkersAtMineral) {
//                    minimumWorkersPerMineral = workersAtMineral.getValue();
                    leastGatheredMineral = workersAtMineral.getKey();
                }
            }

            // This is our optimal mineral to gather near given unit
            return leastGatheredMineral;
        } // If no minerals found, return nearest mineral
        else {
            return Select.minerals().nearestTo(base);
        }
    }

}

package atlantis.workers;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.Select;
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
//            System.err.println("No main base found - skip initial workers assignment");
            AGame.setUmtMode(true);
            return;
        }
        
        // =========================================================

        // Get minerals near to our main base and sort them from closest to most distant one
        List<AUnit> minerals = (List<AUnit>) Select.minerals().inRadius(12, mainBase)
                .sortDataByDistanceTo(mainBase.getPosition(), true);

        // Get our workers
        Collection<AUnit> workers = Select.ourWorkers().listUnits();

        // Assign every worker to the next free mineral
        int counter = 0;
        for (AUnit unit : workers) {
            AUnit mineral = minerals.get(counter);
            unit.gather(mineral);

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
        List<AUnit> minerals = (List<AUnit>) Select.minerals().inRadius(12, base).listUnits();
        if (!minerals.isEmpty()) {

            // Count how many other workers gather this mineral
            Map<AUnit, Integer> workersPerMineral = new HashMap<>();

            Collection<AUnit> ourWorkersInRange = (Collection<AUnit>) Select.ourWorkers().inRadius(12, base).list();
//            for (AUnit otherWorker : ourWorkersInRange) {
//                if (otherWorker.isGatheringMinerals()) {
//                    AUnit mineralMined = otherWorker.getTarget();
//                    if (mineralMined != null) {
//                        //increments the number of workers in this mineral
//                        int previousNumber = (workersPerMineral.get(mineralMined) == null ? 0 : workersPerMineral.get(mineralMined));
//                        workersPerMineral.put(mineralMined, previousNumber + 1);
//                        //minerals.changeValueBy(mineralMined, 1);
//                    }
//                }
//            }
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
            int minimumWorkersPerMineral = 1000;
            for (Entry<AUnit, Integer> workersAtMineral : workersPerMineral.entrySet()) {
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
            return Select.minerals().nearestTo(base);
        }
    }

}

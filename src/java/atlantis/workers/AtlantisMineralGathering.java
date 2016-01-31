package atlantis.workers;

import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import java.util.Collection;
import jnibwapi.Unit;

/**
 * Auxiliary class that's supposed to assign workers to minerals at the beginning of game.
 */
public class AtlantisMineralGathering {

    /**
     * Assign all 4 workers to optimal (closest) minerals fields.
     */
    public static void initialAssignWorkersToMinerals() {

        // Get minerals near to our main base and sort them from closest to most distant one
        Units minerals = SelectUnits.minerals().inRadius(12, SelectUnits.mainBase()).units()
                .sortByDistanceTo(SelectUnits.mainBase(), true);

        // Get our workers
        Collection<Unit> workers = SelectUnits.ourWorkers().list();

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
            unit.gather(mineralField, false);
        }
    }

    // =========================================================
    private static Unit getMineralFieldToGather(Unit worker) {

        // Get nearest base for this unit
        Unit base = SelectUnits.ourBases().nearestTo(worker);
        if (base == null) {
            return null;
        }

        // Get minerals near to our main base and sort them from closest to most distant one
        Units minerals = SelectUnits.minerals().inRadius(12, base).units()
                .sortByDistanceTo(SelectUnits.mainBase(), true);
        
        if (!minerals.isEmpty()) {

            // Count how many other workers gather this mineral
            for (Unit otherWorker : SelectUnits.ourWorkers().inRadius(12, base).list()) {
                if (otherWorker.isGatheringMinerals()) {
                    Unit mineralMined = otherWorker.getTarget();
                    if (mineralMined != null) {
                        minerals.changeValueBy(mineralMined, 1);
                    }
                }
            }

            // Get least gathered mineral
            Unit leastGatheredMineral = minerals.getUnitWithLowestValue();

            // This is our optimal mineral to gather near given unit
            return leastGatheredMineral;
        }
        
        // If no minerals found, return nearest mineral
        else {
            return SelectUnits.minerals().nearestTo(base);
        }
    }

}

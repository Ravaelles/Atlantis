package atlantis.units.workers.gather;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.buildings.NumberOfGasWorkersCommander;
import atlantis.units.select.Select;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class GatherFallback extends Manager {
    public GatherFallback(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker();
    }

    @Override
    public Manager handle() {
        AUnit mineral = fallbackSolutionChooseMineral();
        if (mineral != null) {
            if (unit.gather(mineral)) {
                return usedManager(this, "FallbackGatherMinerals");
            }
        }

//        ErrorLog.debug(unit + " GatherResources FAILED!!! Try mining the nearest mineral.");

        mineral = Select.all().minerals().nearestTo(Select.mainOrAnyBuildingPosition());
        if (mineral != null && unit.gather(mineral)) {
            return usedManager(this);
        }

        return null;
    }


    private AUnit fallbackSolutionChooseMineral() {
        AUnit base = Select.ourBases().nearestTo(unit);
        if (base == null) return null;

        // Get minerals near to our main base and sort them from closest to most distant one
        AUnit mineral = Select.minerals().nearestTo(base);
        if (mineral == null) return null;

        // Randomize
        AUnit randomMineral = Select.minerals().inRadius(12, base).random();
        if (randomMineral != null) return randomMineral;

        return mineral;
    }
}

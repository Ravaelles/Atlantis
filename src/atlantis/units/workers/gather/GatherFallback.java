package atlantis.units.workers.gather;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

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

        // The nearest mineral to the base, deterministically. This was `random()` over the
        // minerals in radius 12, which made the fallback re-target the worker to a DIFFERENT
        // mineral every frame (owner's report: "in every time unit we send workers to a
        // different mineral, so there is no mining at all"). Gather is idempotent only when
        // the target is stable; a random pick per frame is a permanent cancel/re-issue loop.

        return Select.minerals().nearestTo(base);
    }
}

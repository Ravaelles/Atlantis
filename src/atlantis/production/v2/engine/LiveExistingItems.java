package atlantis.production.v2.engine;

import atlantis.information.tech.ATech;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * {@link ExistingItems} answered from the live game, in absolute frames: a
 * completed unit is available now, one under construction at its completion,
 * a requested-but-not-placed building one build time from now (optimistic,
 * but it stops the planner from requesting it a second time). Tech and
 * upgrade levels are available once researched.
 */
public final class LiveExistingItems implements ExistingItems {

    private final int now;

    public LiveExistingItems(int now) {
        this.now = now;
    }

    @Override
    public int availableFrom(Producible item) {
        if (item instanceof UnitProducible)
            return unitAvailableFrom(((UnitProducible) item).type());

        if (item instanceof TechProducible)
            return ATech.isResearched(((TechProducible) item).tech()) ? now : MISSING;

        if (item instanceof UpgradeProducible) {
            UpgradeProducible upgrade = (UpgradeProducible) item;

            return ATech.getUpgradeLevel(upgrade.upgrade()) >= upgrade.level() ? now : MISSING;
        }
        return MISSING;
    }

    private int unitAvailableFrom(AUnitType type) {
        int best = MISSING;
        for (AUnit unit : Select.ourWithUnfinished().ofType(type).list()) {
            int frame = unit.isCompleted() ? now : now + Math.max(0, unit.getRemainingBuildTime());
            if (best < 0 || frame < best)
                best = frame;
            if (best == now)

                return now;
        }
        if (best < 0 && type.isABuilding() && ConstructionRequests.countNotStartedOfType(type) > 0) {
            best = now + type.totalTrainTime();
        }
        return best;
    }

    @Override
    public int inFlightFrom(Producible item) {
        if (!(item instanceof UnitProducible)) return availableFrom(item);

        AUnitType type = ((UnitProducible) item).type();
        int best = MISSING;
        for (AUnit unit : Select.ourWithUnfinished().ofType(type).list()) {
            if (unit.isCompleted()) continue;
            int frame = now + Math.max(0, unit.getRemainingBuildTime());
            if (best < 0 || frame < best)
                best = frame;
        }
        // An attempted-but-unwarped building counts too: between the
        // builder's arrival and the warp the construction is IN_PROGRESS
        // with no unit yet, and without this the row re-fires mid-walk
        // (live: one At-22 Core row warped twice, 142 frames apart, on two
        // tiles). Same optimistic estimate as a requested site. A dead
        // builder breaks the promise instead: its site never warps, and
        // counting it parks the goal forever behind thousands of banked
        // minerals (live V2 stall shape at supply 49 with 5k bank and zero
        // Pylon goals). A re-fire then rescues instead of duplicating.
        if (best < 0 && type.isABuilding() && hasLiveComingBuilding(type)) {
            best = now + type.totalTrainTime();
        }
        return best;
    }

    /**
     * An unwarped construction of this type that can still warp: NOT_STARTED
     * (requested), STARTING (travelling) or IN_PROGRESS with no unit yet
     * (attempted, warping) - and a builder that is alive and not stuck.
     * A dead or wedged builder breaks the promise: its site never warps,
     * and counting it suppresses every later row of the type behind
     * thousands of banked minerals (V2 LIVE stall shape at supply 49 with
     * 5k bank and zero Pylon goals). A re-fire then rescues instead of
     * duplicating.
     */
    private static boolean hasLiveComingBuilding(AUnitType type) {
        for (Construction construction : ConstructionRequests.constructions) {
            if (construction == null || !type.equals(construction.buildingType())) continue;
            if (construction.status() != ConstructionOrderStatus.NOT_STARTED
                && construction.status() != ConstructionOrderStatus.STARTING
                && !(construction.status() == ConstructionOrderStatus.IN_PROGRESS
                    && construction.buildingUnit() == null)) continue;
            AUnit builder = construction.builder();
            if (builder == null) return true;
            if (!builder.isAlive() || builder.isStuck()) continue;

            return true;
        }

        return false;
    }
}

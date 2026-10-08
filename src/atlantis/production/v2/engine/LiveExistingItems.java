package atlantis.production.v2.engine;

import atlantis.information.tech.ATech;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.ExistingItems;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.UpgradeProducible;
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
}

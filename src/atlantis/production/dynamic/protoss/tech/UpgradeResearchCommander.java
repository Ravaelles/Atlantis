package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.information.tech.UpgradeCompletionTracker;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.UpgradeType;

import java.util.List;

public abstract class UpgradeResearchCommander extends Commander {
    public static boolean isBeingResearched(UpgradeType upgrade) {
        List<AUnit> upgraders = Select.ourOfType(AUnitType.from(upgrade.whatUpgrades())).list();

        for (AUnit building : upgraders) {
            if (building.isUpgrading() && building.whatIsUpgrading() == upgrade) {
                return true;
            }
        }

        return false;
    }

    public abstract UpgradeType what();

    protected abstract boolean isEnqueued();

    protected abstract void setEnqueued(boolean isEnqueued);

    /**
     * Queue priority of the research order (D3): STANDARD unless a researcher
     * overrides - Singularity Charge waits above it, so the army pump saves
     * gas instead of out-spending it.
     */
    protected ProductionOrderPriority orderPriority() {
        return ProductionOrderPriority.STANDARD;
    }

    /**
     * Completion observation lives here, never behind the decision gate:
     * applies() runs every visited frame while handle() only runs on a
     * fire, so an upgrade that completes (the gate then refuses with
     * "researched") would never be seen from handle() - measured B-42:
     * Singularity researched live with zero UPGRADE_COMPLETED rows.
     * Final so no researcher can silently drop the observation again.
     */
    @Override
    public final boolean applies() {
        UpgradeCompletionTracker.note(
            what(), ATech.getUpgradeLevel(what()), A.now(), A.minSec());

        return appliesInner();
    }

    protected abstract boolean appliesInner();

    @Override
    protected boolean handle() {
        if (CountInQueue.count(what(), 2) == 0) {
            if (AddToQueue.upgrade(what(), orderPriority())) setEnqueued(true);
        }

        if (ResearchNow.research(what())) {
            setEnqueued(true);
        }
        else if (!isEnqueued() && AddToQueue.upgrade(what())) {
            setEnqueued(true);
        }
        return false;
    }
}

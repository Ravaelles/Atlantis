package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.UpgradeType;

import java.util.List;

public abstract class UpgradeResearchCommander extends Commander {
    protected static boolean isResearched = false;
    protected static boolean enqueued = false;

    public abstract UpgradeType what();

    @Override
    protected void handle() {
        if (CountInQueue.count(what(), 3) > 0) {
            if (AddToQueue.upgrade(what())) enqueued = true;
        }

        if (ResearchNow.research(what())) {
            enqueued = true;
        }
        else if (!enqueued && AddToQueue.upgrade(what())) {
            enqueued = true;
        }
    }

    public static boolean isResearched() {
        return isResearched;
    }

    public static boolean isBeingResearched(UpgradeType upgrade) {
        List<AUnit> upgraders = Select.ourOfType(AUnitType.from(upgrade.whatUpgrades())).list();

        for (AUnit building : upgraders) {
            if (building.isUpgrading() && building.whatIsUpgrading() == upgrade) {
                return true;
            }
        }

        return false;
    }
}

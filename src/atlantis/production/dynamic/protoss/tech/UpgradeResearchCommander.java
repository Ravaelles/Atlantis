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
    public abstract UpgradeType what();
    protected abstract void setEnqueued(boolean isEnqueued);
    protected abstract boolean isEnqueued();

    @Override
    protected boolean handle() {
        if (CountInQueue.count(what(), 2) == 0) {
            if (AddToQueue.upgrade(what())) setEnqueued(true);
        }

        if (ResearchNow.research(what())) {
            setEnqueued(true);
        }
        else if (!isEnqueued() && AddToQueue.upgrade(what())) {
            setEnqueued(true);
        }
        return false;
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

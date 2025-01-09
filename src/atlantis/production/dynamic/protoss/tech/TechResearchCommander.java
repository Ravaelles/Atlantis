package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import bwapi.TechType;
import bwapi.UpgradeType;

public abstract class TechResearchCommander extends Commander {
    protected static boolean isResearched = false;
    protected static boolean enqueued = false;

    public abstract TechType what();

    @Override
    protected void handle() {
        if (CountInQueue.count(what(), 10) > 0) {
            if (AddToQueue.tech(what())) enqueued = true;
        }

        if (ResearchNow.research(what())) {
            enqueued = true;
        }
        else if (!enqueued && AddToQueue.tech(what())) {
            enqueued = true;
        }
    }

    public static boolean isResearched() {
        return isResearched;
    }
}

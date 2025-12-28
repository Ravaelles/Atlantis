package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import bwapi.TechType;

public abstract class TechResearchCommander extends Commander {
    protected abstract void setEnqueued(boolean isEnqueued);
    protected abstract boolean isEnqueued();

    public abstract TechType what();

    @Override
    protected boolean handle() {
        if (CountInQueue.count(what(), 10) > 0) {
            if (AddToQueue.tech(what())) setEnqueued(true);
        }

        if (ResearchNow.research(what())) {
            setEnqueued(true);
        }
        else if (!isEnqueued() && AddToQueue.tech(what())) {
            setEnqueued(true);
        }
        return false;
    }
}

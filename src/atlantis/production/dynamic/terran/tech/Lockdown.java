package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import bwapi.TechType;

public class Lockdown extends Commander {
    @Override
    public boolean applies() {
        return Count.ghosts() >= 1 && ATech.isNotResearchedOrPlanned(TechType.Lockdown);
    }

    @Override
    protected void handle() {
        AddToQueue.tech(TechType.Lockdown);
    }
}

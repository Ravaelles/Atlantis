package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import bwapi.TechType;

public class Lockdown extends Commander {
    @Override
    public boolean applies() {
        return Count.ghosts() >= 1 && ATech.isNotResearchedOrPlanned(TechType.Lockdown);
    }

    @Override
    public void handle() {
        AddToQueue.tech(TechType.Lockdown);
    }
}

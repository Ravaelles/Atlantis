package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.TechType;

public class Stimpacks extends Commander {
    @Override
    public boolean applies() {
        if (!Have.academy()) return false;

        if (OurStrategy.get().goingBio()) {
            if (Count.infantry() >= 8 && A.canAffordWithReserved(100, 100)) {
                if (ATech.isNotResearchedOrPlanned(TechType.Stim_Packs)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.tech(TechType.Stim_Packs);
    }
}

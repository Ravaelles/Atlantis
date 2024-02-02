package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;

import static bwapi.UpgradeType.Leg_Enhancements;

public class LegEnhancements extends Commander {
    @Override
    public boolean applies() {
        if (ATech.isResearched(Leg_Enhancements)) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(350) && A.hasMinerals(400) && (A.hasGas(460) || Count.zealots() >= 3)) {
            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(Leg_Enhancements);
    }
}
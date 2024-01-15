package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;

import static bwapi.UpgradeType.Singularity_Charge;

public class SingularityCharge extends Commander {
    @Override
    public boolean applies() {
        if (ATech.isResearched(Singularity_Charge)) return false;

        int dragoons = Count.dragoons();

        if (!A.hasMinerals(300) && ArmyStrength.ourArmyRelativeStrength() <= 80) return false;

        if (A.hasGas(150) && (A.hasGas(260) || dragoons >= 3)) return true;

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(Singularity_Charge);
    }
}

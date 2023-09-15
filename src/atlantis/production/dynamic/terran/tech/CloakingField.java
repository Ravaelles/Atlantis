package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

import static bwapi.TechType.Cloaking_Field;

public class CloakingField extends Commander {
    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (!Have.controlTower()) return false;
        if (ATech.isResearchedOrPlanned(Cloaking_Field)) return false;

        if (Count.wraiths() >= 1 && !ArmyStrength.weAreWeaker()) return true;
//        if (Count.wraiths() >= (Enemy.terran() ? 2 : 1)) return true;

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.tech(Cloaking_Field);
    }
}

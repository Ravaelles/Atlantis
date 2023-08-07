package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;
import bwapi.TechType;
import bwapi.UpgradeType;

import static bwapi.TechType.Cloaking_Field;

public class CloakingField extends Commander {
    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (ATech.isResearchedOrPlanned(Cloaking_Field)) return false;

        if (Count.wraiths() >= (Enemy.terran() ? 2 : 6)) return true;

        return false;
    }

    @Override
    public void handle() {
        AddToQueue.tech(Cloaking_Field);
    }
}

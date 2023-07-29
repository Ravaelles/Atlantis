package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import bwapi.UpgradeType;

public class U238 extends Commander {
    @Override
    public boolean applies() {
        if (OurStrategy.get().goingBio()) {
            if (Count.infantry() >= 8 && AGame.canAffordWithReserved(100, 100)) {
                if (ATech.isNotResearchedOrPlanned(UpgradeType.U_238_Shells)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void handle() {
        AddToQueue.upgrade(UpgradeType.U_238_Shells);
    }
}

package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.UpgradeType;

public class U238 extends Commander {
    public static UpgradeType upgradeType() {
        return UpgradeType.U_238_Shells;
    }

    @Override
    public boolean applies() {
        if (!Have.academy()) return false;

        if (OurStrategy.get().goingBio()) {
            if (Count.infantry() >= 8 && AGame.canAffordWithReserved(100, 100)) {
                if (ATech.isNotResearchedOrPlanned(upgradeType())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(upgradeType());
    }
}

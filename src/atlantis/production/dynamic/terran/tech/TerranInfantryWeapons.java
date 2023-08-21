package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import bwapi.UpgradeType;

public class TerranInfantryWeapons extends Commander {
    @Override
    public boolean applies() {
        if (OurStrategy.get().goingBio()) {
            int currentUpgradeLevel = upgradeLevel();
            int minInfantry = 12 + currentUpgradeLevel * 9;
            if (
                currentUpgradeLevel <= 2
                    && Count.infantry() >= minInfantry
                    && AGame.canAffordWithReserved(100, 150)
            ) {
                if (ATech.isNotResearchedOrPlanned(UpgradeType.Terran_Infantry_Weapons)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int upgradeLevel() {
        return ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Weapons);
    }

    @Override
    protected void handle() {
        if (upgradeLevel() <= 2) {
            AddToQueue.upgrade(UpgradeType.Terran_Infantry_Weapons);
        }
    }
}

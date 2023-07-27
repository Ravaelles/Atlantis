package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import bwapi.UpgradeType;

public class TerranInfantryArmor extends Commander {
    @Override
    public boolean applies() {
        if (OurStrategy.get().goingBio()) {
            if (OurStrategy.get().goingBio()) {

                int currentUpgradeLevel = ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Armor);
                int minInfantry = 12 + currentUpgradeLevel * 9;
                if (
                    currentUpgradeLevel <= 2
                        && Count.infantry() >= minInfantry
                        && AGame.canAffordWithReserved(100, 150)
                ) {
                    if (ATech.isNotResearchedOrPlanned(AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void handle() {
        AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor);
    }
}

package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import bwapi.UpgradeType;

public class TerranInfantryArmor extends Commander {
    @Override
    public boolean applies() {
        if (A.seconds() <= 600) return false;
        if (A.hasMinerals(700)) return false;

        if (OurStrategy.get().goingBio()) {
            if (OurStrategy.get().goingBio()) {

                int currentUpgradeLevel = ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Armor);
                int minInfantry = 12 + currentUpgradeLevel * 9;
                if (
                    currentUpgradeLevel <= 2
                        && Count.infantry() >= minInfantry
                        && AGame.canAffordWithReserved(100, 150)
                ) {
                    if (ATech.isNotResearchedOrPlanned(UpgradeType.Terran_Infantry_Armor)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor);
    }
}

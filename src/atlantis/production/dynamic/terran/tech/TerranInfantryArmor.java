package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

public class TerranInfantryArmor extends Commander {
    @Override
    public boolean applies() {
        if (!A.canAfford(550 + delayBonus(), 250)) return false;

        if (OurStrategy.get().goingBio() && Count.infantry() >= 8) {
            if (OurStrategy.get().goingBio()) {

                int currentUpgradeLevel = ATech.getUpgradeLevel(UpgradeType.Terran_Infantry_Armor);
                int minInfantry = 12 + currentUpgradeLevel * 9;
                if (
                    currentUpgradeLevel <= 2
                        && Count.infantry() >= minInfantry
                ) {
                    if (ATech.isNotResearchedOrPlanned(UpgradeType.Terran_Infantry_Armor)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int delayBonus() {
        return Enemy.protoss() ? 100 : 0;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(UpgradeType.Terran_Infantry_Armor);
    }
}

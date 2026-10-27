package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.Strategy;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchNow;
import atlantis.units.select.Count;
import bwapi.UpgradeType;

public class TerranInfantryArmor extends Commander {
    public static int upgradeLevel() {
        return ATech.getUpgradeLevel(what());
    }

    public static UpgradeType what() {
        return UpgradeType.Terran_Infantry_Armor;
    }

    @Override
    public boolean applies() {
        if (Count.basesWithUnfinished() <= 1) return false;
        if (!A.canAfford(550 + delayBonus(), 250)) return false;

        if (Strategy.get().goingBio() && Count.infantry() >= 8) {
            if (Strategy.get().goingBio()) {

                int currentUpgradeLevel = ATech.getUpgradeLevel(what());
                int minInfantry = 12 + currentUpgradeLevel * 9;
                if (
                    currentUpgradeLevel <= 2
                        && Count.infantry() >= minInfantry
                ) {
                    return ATech.isNotResearchedOrPlanned(what());
                }
            }
        }

        return false;
    }

    private int delayBonus() {
        return Enemy.protoss() ? 100 : 0;
    }

    @Override
    protected boolean handle() {
        if (upgradeLevel() <= 2) {
            ResearchNow.research(what());
        }
        return false;
    }
}

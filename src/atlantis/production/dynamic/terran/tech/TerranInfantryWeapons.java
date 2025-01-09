package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchNow;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import bwapi.UpgradeType;

public class TerranInfantryWeapons extends Commander {
    @Override
    public boolean applies() {
        if (Count.basesWithUnfinished() <= 1) return false;
        if (!A.canAfford(550 + delayBonus(), 250)) return false;

        if (Strategy.get().goingBio() && Count.infantry() >= 8) {
            int currentUpgradeLevel = upgradeLevel();
            int minInfantry = 12 + currentUpgradeLevel * 9;
            if (
                currentUpgradeLevel <= 2
                    && Count.infantry() >= minInfantry
            ) {
                if (ATech.isNotResearchedOrPlanned(what())) {
                    return true;
                }
            }
        }

        return false;
    }

    private int delayBonus() {
        return Enemy.zerg() ? 100 : 0;
    }

    public static int upgradeLevel() {
        return ATech.getUpgradeLevel(what());
    }

    @Override
    protected void handle() {
        if (upgradeLevel() <= 2) {
            ResearchNow.research(what());
//            AddToQueue.upgrade(UpgradeType.Terran_Infantry_Weapons);
        }
    }

    public static UpgradeType what() {
        return UpgradeType.Terran_Infantry_Weapons;
    }
}

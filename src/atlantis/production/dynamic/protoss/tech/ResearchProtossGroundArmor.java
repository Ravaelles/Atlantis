package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Protoss_Ground_Armor;

public class ResearchProtossGroundArmor extends Commander {
    private static UpgradeType what() {
        return Protoss_Ground_Armor;
    }

    @Override
    public boolean applies() {
        if (!Have.forge()) return false;
        if (Count.ourCombatUnits() <= 10) return false;
        if (ATech.isResearched(what())) return false;
        if (!A.hasMinerals(650) && Count.basesWithUnfinished() <= 1) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;
        if (CountInQueue.count(what(), 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(270 + (Enemy.protoss() ? 100 : 0)) && A.hasMinerals(550)) {
            if (!A.canAfford(500, 200) && Count.ourCombatUnits() <= 17) return false;

            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
//        if (AddToQueue.upgrade(tech())) {
//            Queue.get().history().addNow(tech().name());
//        }
        ResearchNow.research(what());
    }
}

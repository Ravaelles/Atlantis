package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ProtossGroundWeapons extends Commander {
    public static UpgradeType what() {
        return Protoss_Ground_Weapons;
    }

    @Override
    public boolean applies() {
        if (!Have.forge()) return false;
        if (!A.hasMinerals(600) && Count.basesWithUnfinished() <= 1) return false;
        if (isResearched()) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;
        if (CountInQueue.count(what(), 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(150 + (Enemy.zerg() ? 80 : 0)) && A.hasMinerals(290)) {
            if (!A.canAfford(500, 200) && Count.ourCombatUnits() <= 17) return false;

            return true;
        }

        return false;
    }

    private static boolean isResearched() {
        boolean isResearched = ATech.isResearched(what());
        if (!isResearched) return false;

        int upgradeLevel = getCurrentUpgradeLevel();
        if (upgradeLevel >= 3) return true;

        return A.canAfford(600, 400);
    }

    private static int getCurrentUpgradeLevel() {
        return ATech.getUpgradeLevel(what());
    }

    @Override
    protected void handle() {
//        if (AddToQueue.upgrade(what())) {
//            Queue.get().history().addNow(what().name());
//        }
        ResearchNow.research(what());
    }

}

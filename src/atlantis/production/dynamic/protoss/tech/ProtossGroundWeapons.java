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
        if (ATech.isResearched(what())) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;
        if (CountInQueue.count(what(), 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(150 + (Enemy.zerg() ? 80 : 0)) && A.hasMinerals(290)) {
            if (!A.canAfford(500, 200) && Count.ourCombatUnits() <= 17) return false;

            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
//        if (AddToQueue.upgrade(what())) {
//            Queue.get().history().addNow(what().name());
//        }
        ResearchNow.research(what());
    }

}

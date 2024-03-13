package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ProtossGroundWeapons extends Commander {
    public static UpgradeType tech() {
        return Protoss_Ground_Weapons;
    }

    @Override
    public boolean applies() {
        if (!Have.forge()) return false;
        if (ATech.isResearched(tech())) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(tech().name(), 30)) return false;
        if (CountInQueue.count(tech(), 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(200 + (Enemy.zerg() ? 80 : 0)) && A.hasMinerals(550)) {
            if (Count.ourCombatUnits() <= 15) return false;

            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
        if (AddToQueue.upgrade(tech())) {
            Queue.get().history().addNow(tech().name());
        }
    }
}
